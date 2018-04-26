#include <re.h>
#include <rem.h>
#include <baresip.h>
#include <jni.h>

#define UCHAR_MAX 255

struct viddec_state {
	struct mbuf *mb;
	bool got_keyframe;
	size_t frag_start;
	bool frag;
	uint16_t frag_seq;

	struct {
		unsigned n_key;
		unsigned n_lost;
	} stats;
};

typedef struct JNI_Decoder
{
	jclass jni_clz;		//java方法类的jclass对象
	jobject jni_obj;	//java方法类的object
	jmethodID start_mid; //java回调方法
	jmethodID decode_mid;
	jmethodID stop_mid;
} JNI_Decoder;

typedef struct JNI_Baresip_Context
{
	JavaVM *javaVM;

	JNI_Decoder decoder;
} JNI_Baresip_Context;

static JNI_Baresip_Context g_jni_ctx;

enum {
	DECODE_MAXSZ = 524288,
};


void jni_init(JNIEnv *env)
{
    jclass  clz = (*env)->FindClass(env, "com/xyt/codec/Decoder4Jni");
    g_jni_ctx.decoder.jni_clz = (*env)->NewGlobalRef(env, clz);

    jmethodID  construction_id = (*env)->GetMethodID(env, g_jni_ctx.decoder.jni_clz,
                                                     "<init>", "()V");
    jobject    handler = (*env)->NewObject(env, g_jni_ctx.decoder.jni_clz,
                                           construction_id);
    g_jni_ctx.decoder.jni_obj = (*env)->NewGlobalRef(env, handler);

    g_jni_ctx.decoder.start_mid = (*env)->GetMethodID(env, g_jni_ctx.decoder.jni_clz, "start", "(II)V");
	g_jni_ctx.decoder.decode_mid = (*env)->GetMethodID(env, g_jni_ctx.decoder.jni_clz, "decode", "([BI)V");
	g_jni_ctx.decoder.stop_mid = (*env)->GetMethodID(env, g_jni_ctx.decoder.jni_clz, "stop", "()V");
}

jstring stoJstring(JNIEnv *env, const char *pat) {
	jclass strClass = (*env)->FindClass(env, "Ljava/lang/String;");
	jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");
	jbyteArray bytes = (*env)->NewByteArray(env, strlen(pat));
	(*env)->SetByteArrayRegion(env, bytes, 0, strlen(pat), (jbyte *)pat);
	jstring encoding = (*env)->NewStringUTF(env, "utf-8");
	return (jstring)(*env)->NewObject(env, strClass, ctorID, bytes, encoding);
}

void baresip_jni_onload(void *vm, void *env) {
	memset(&g_jni_ctx, 0, sizeof(g_jni_ctx));

	g_jni_ctx.javaVM = (JavaVM *)vm;

	jni_init((void *)env);
}

void jni_run_decoder_start(int w, int h) {
	JavaVM *javaVM = g_jni_ctx.javaVM;
	jobject obj = g_jni_ctx.decoder.jni_obj;
	jmethodID mid = g_jni_ctx.decoder.start_mid;
	JNIEnv *env;
	jint res = (*javaVM)->GetEnv(javaVM, (void **)&env, JNI_VERSION_1_6);

	(*env)->CallVoidMethod(env, obj, mid, w, h);
}

void jni_run_decoder_decode(unsigned char *in_, int in_len) {
	JavaVM *javaVM = g_jni_ctx.javaVM;
	jobject obj = g_jni_ctx.decoder.jni_obj;
	jmethodID mid = g_jni_ctx.decoder.decode_mid;
	JNIEnv *env;
	jint res = (*javaVM)->GetEnv(javaVM, (void **)&env, JNI_VERSION_1_6);

	jbyteArray ba = (*env)->NewByteArray(env, in_len);
	(*env)->SetByteArrayRegion(env, ba, 0, in_len, (jbyte *)in_);

	(*env)->CallVoidMethod(env, obj, mid, ba, in_len);
	(*env)->ReleaseByteArrayElements(env, ba, (jbyte *)in_, 0);
}

void jni_run_decoder_stop() {
	JavaVM *javaVM = g_jni_ctx.javaVM;
	jobject obj = g_jni_ctx.decoder.jni_obj;
	jmethodID mid = g_jni_ctx.decoder.decode_mid;
	JNIEnv *env;
	jint res = (*javaVM)->GetEnv(javaVM, (void **)&env, JNI_VERSION_1_6);

	(*env)->CallVoidMethod(env, obj, mid);
}

static inline int16_t seq_diff(uint16_t x, uint16_t y)
{
	return (int16_t)(y - x);
}

static inline void fragment_rewind(struct viddec_state *vds)
{
	vds->mb->pos = vds->frag_start;
	vds->mb->end = vds->frag_start;
}

static void destructor(void *arg)
{
	struct viddec_state *st = (struct viddec_state *)arg;

	debug("avcodec: decoder stats"
		  " (keyframes:%u, lost_fragments:%u)\n",
		  st->stats.n_key, st->stats.n_lost);

	jni_run_decoder_stop();

	mem_deref(st->mb);
}

int h264_decode_update(struct viddec_state **vdsp, const struct vidcodec *vc,
		const char *fmtp) {
	info("h264_decode_update\n");

	struct viddec_state *st;

	if (!vdsp || !vc) {
		info("param null\n");
		return EINVAL;
	}

	st = mem_zalloc(sizeof(*st), destructor);

	st->mb = mbuf_alloc(1024);
	if (!st->mb) {
		return ENOMEM;
	}

	jni_run_decoder_start(1280, 720);

	struct viddec_state *old;
	old = *vdsp;
	*vdsp = st;

	if (old) {
		mem_deref(old);
	}

	info("h264: video decoder %s (%s)\n", vc->name, fmtp ? fmtp : "");

	return 0;
}

static int ffdecode(struct viddec_state *st, struct vidframe *frame)
{
	info("ffdecode\n");

	int i, got_picture, ret;
	int err = 0;

	st->mb->pos = 0;

	if (!st->got_keyframe) {
		info("avcodec: waiting for key frame ..\n");
		return 0;
	}

	jni_run_decoder_decode(st->mb->buf, st->mb->end);

	// // deocde by h264
	// err = (*(st->decoder))->DecodeFrame2(st->decoder, st->mb->buf, st->mb->end,
	// 	(uint8_t **)(frame->data), &(st->info));

	// info("DecodeFrame 1: %d\n", err);

	// if (err == dsErrorFree && st->info.iBufferStatus == 1) {
	// 	frame->size.w  = st->info.UsrData.sSystemBuffer.iWidth;
	// 	frame->size.h = st->info.UsrData.sSystemBuffer.iHeight;

	// 	frame->linesize[0] = st->info.UsrData.sSystemBuffer.iStride[0];
	// 	frame->linesize[1] = st->info.UsrData.sSystemBuffer.iStride[1];
	// 	// frame->linesize[2] = st->info.UsrData.sSystemBuffer.iStride[1];

	// } else {
	// 	return err;
	// }

	frame->fmt    = VID_FMT_YUV420P;

	return 0;
}

int h264_decode(struct viddec_state *st, struct vidframe *frame,
		bool *intra, bool marker, uint16_t seq, struct mbuf *src) {
	info("h264_decode\n");

	struct h264_hdr h264_hdr;
	const uint8_t nal_seq[3] = {0, 0, 1};
	int err;

	*intra = false;

	err = h264_hdr_decode(&h264_hdr, src);
	if (err) {
		info("h264_hdr_decode: %d\n", err);
		return err;
	}

	re_printf("avcodec: decode: %s %s type=%2d  \n",
		  marker ? "[M]" : "   ",
		  h264_is_keyframe(h264_hdr.type) ? "<KEY>" : "     ",
		  h264_hdr.type);

	if (h264_hdr.f) {
		info("avcodec: H264 forbidden bit set!\n");
		return EBADMSG;
	}

	if (st->frag && h264_hdr.type != H264_NAL_FU_A) {
		debug("avcodec: lost fragments; discarding previous NAL\n");
		fragment_rewind(st);
		st->frag = false;
		++st->stats.n_lost;
	}

	/* handle NAL types */
	if (1 <= h264_hdr.type && h264_hdr.type <= 23) {

		if (h264_is_keyframe(h264_hdr.type))
			*intra = true;

		--src->pos;

		if (h264_hdr.type == 5) {
			error_msg("This is I nalu\n");
		}

		/* prepend H.264 NAL start sequence */
		err  = mbuf_write_mem(st->mb, nal_seq, 3);

		err |= mbuf_write_mem(st->mb, mbuf_buf(src),
				      mbuf_get_left(src));
		if (err)
			goto out;
	}
	else if (H264_NAL_FU_A == h264_hdr.type) {
		struct h264_fu fu;

		err = h264_fu_hdr_decode(&fu, src);
		if (err) {
			info("h264_fu_hdr_decode return err, %d", err);
			return err;
		}
		h264_hdr.type = fu.type;

		info("H264_NAL_FU_A: %d %d %d, %d\n", fu.s, fu.e, fu.r, fu.type);

		if (fu.s) {
			if (st->frag) {
				debug("avcodec: lost fragments;"
				      " ignoring NAL\n");
				fragment_rewind(st);
				++st->stats.n_lost;
				return EPROTO;
			}

			st->frag_start = st->mb->pos;
			st->frag = true;

			if (h264_is_keyframe(fu.type))
				*intra = true;

			/* prepend H.264 NAL start sequence */
			mbuf_write_mem(st->mb, nal_seq, 3);

			/* encode NAL header back to buffer */
			err = h264_hdr_encode(&h264_hdr, st->mb);
		}
		else {
			if (!st->frag) {
				debug("avcodec: ignoring fragment\n");
				++st->stats.n_lost;
				return EPROTO;
			}

			if (seq_diff(st->frag_seq, seq) != 1) {
				debug("avcodec: lost fragments detected\n");
				fragment_rewind(st);
				st->frag = false;
				++st->stats.n_lost;
				return EPROTO;
			}
		}

		err = mbuf_write_mem(st->mb, mbuf_buf(src),
				     mbuf_get_left(src));
		if (err)
			goto out;

		if (fu.e)
			st->frag = false;

		st->frag_seq = seq;
	}
	else {
		warning("avcodec: unknown NAL type %u\n", h264_hdr.type);
		return EBADMSG;
	}

	if (*intra) {
		st->got_keyframe = true;
		++st->stats.n_key;
	}

	if (!marker) {

		if (st->mb->end > DECODE_MAXSZ) {
			warning("avcodec: decode buffer size exceeded\n");
			err = ENOMEM;
			goto out;
		}

		return 0;
	}

	if (st->frag) {
		err = EPROTO;
		goto out;
	}

	err = ffdecode(st, frame);
	if (err)
		goto out;

 out:
	mbuf_rewind(st->mb);
	st->frag = false;

	return err;
}
