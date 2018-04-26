#include <re.h>
#include <rem.h>
#include <baresip.h>
#include <sys/time.h>
#include "wels/codec_api.h"

struct videnc_state {
	ISVCEncoder *encoder;
	SSourcePicture pic;
	SFrameBSInfo info;
	
	struct videnc_param encprm;
	videnc_packet_h *pkth;
	void *arg;
	bool got_key_frame;
};

struct vtx {
	struct video *video;               /**< Parent                    */
	const struct vidcodec *vc;         /**< Current Video encoder     */
	struct videnc_state *enc;          /**< Video encoder state       */
	struct vidsrc_prm vsrc_prm;        /**< Video source parameters   */
	struct vidsz vsrc_size;            /**< Video source size         */
	struct vidsrc_st *vsrc;            /**< Video source              */
	struct lock *lock;                 /**< Lock for encoder          */
	struct vidframe *frame;            /**< Source frame              */
	struct vidframe *mute_frame;       /**< Frame with muted video    */
	struct lock *lock_tx;              /**< Protect the sendq         */
	struct list sendq;                 /**< Tx-Queue (struct vidqent) */
	struct tmr tmr_rtp;                /**< Timer for sending RTP     */
	unsigned skipc;                    /**< Number of frames skipped  */
	struct list filtl;                 /**< Filters in encoding order */
	char device[128];                  /**< Source device name        */
	int muted_frames;                  /**< # of muted frames sent    */
	uint32_t ts_offset;                /**< Random timestamp offset   */
	bool picup;                        /**< Send picture update       */
	bool muted;                        /**< Muted flag                */
	int frames;                        /**< Number of frames sent     */
	int efps;                          /**< Estimated frame-rate      */
	uint32_t ts_min;
	uint32_t ts_max;
};

static void destructor(void *arg) {
	struct videnc_state *vsp = (struct videnc_state *)arg;

	(*(vsp->encoder))->Uninitialize(vsp->encoder);
	WelsDestroySVCEncoder(vsp->encoder);
}

int openh264_encode_update(struct videnc_state **vesp, const struct vidcodec *vc,
		struct videnc_param *prm, const char *fmtp,
		videnc_packet_h *pkth, void *arg) {
	info("openh264_encode_update\n");

	struct vtx *vtx = (struct vtx *)arg;
	struct videnc_state *st;

	if (!vesp || !vc || !prm || !pkth) {
		info("params null");
		return EINVAL;
	}

	st = mem_zalloc(sizeof(*st), destructor);

	st->encprm = *prm;
	st->pkth = pkth;
	st->arg = arg;

	WelsCreateSVCEncoder(&(st->encoder));

	SEncParamBase param;
	memset(&param, 0, sizeof (SEncParamBase));

	param.iUsageType = CAMERA_VIDEO_REAL_TIME;
	param.fMaxFrameRate = prm->max_fs;
	param.iPicWidth = vtx->vsrc_size.w > 0 ? vtx->vsrc_size.w : 1280;
	param.iPicHeight = vtx->vsrc_size.h > 0 ? vtx->vsrc_size.h : 720;
	param.iTargetBitrate = 500000;

	int err = (*(st->encoder))->Initialize(st->encoder, &param);
	info("encode init: %d\n", err);

	struct videnc_state *old;
	old = *vesp;
	*vesp = st;

	if (old) {
		mem_deref(old);
	}

	info("openh264: video encoder %s: %d fps, %d bit/s, pktsize=%u\n",
		vc->name, prm->fps, prm->bitrate, prm->pktsize);
	return 0;
}


int openh264_encode(struct videnc_state *ves, bool update,
		const struct vidframe *frame) {
	info("openh264_encode\n");

	if (frame->fmt == VID_FMT_H264)
	{
		int w = frame->size.w > 0 ? frame->size.w : 1280;
		int h = frame->size.h > 0 ? frame->size.h : 720;

		uint8_t *buf = frame->data[0];
		int len = frame->linesize[0];

		struct timeval tv;
		gettimeofday(&tv, NULL);

		int err = h264_packetize((90000ULL * (1000000 * tv.tv_sec + tv.tv_usec)) / 1000000
			, buf, len, ves->encprm.pktsize, ves->pkth, ves->arg);

//        int err = h264_nal_send(true, true, true, buf[0]
//                , (90000ULL * (1000000 * tv.tv_sec + tv.tv_usec)) / 1000000
//                , buf + 1, len - 1
//                , ves->encprm.pktsize, ves->pkth, ves->arg);

        info("h264_packetize: %d; fmt=%d\n", err, frame->fmt);

	} else {
		int w = frame->size.w > 0 ? frame->size.w : 1280;
		int h = frame->size.h > 0 ? frame->size.h : 720;
		int pln = 2;

		switch (frame->fmt) {
		case VID_FMT_YUV420P:
			pln = 3;
			break;

		case VID_FMT_NV12:
			pln = 2;
			break;

		default:
			info("openh264_encode: pixel format not supported (%s)\n",
				vidfmt_name(frame->fmt));
			return ENOTSUP;
		}

		ves->pic.iPicWidth = w;
		ves->pic.iPicHeight = h;
		ves->pic.iColorFormat = videoFormatI420;
		for (int i=0; i<pln; i++) {
			ves->pic.iStride[i] = frame->linesize[i];
			ves->pic.pData[i]   = frame->data[i];
		}

		int err = (*(ves->encoder))->EncodeFrame(ves->encoder, &(ves->pic), &(ves->info));

		info("enocde: %d\n", err);

		for (int i = 0; i < ves->info.iLayerNum; ++i) {
			int type = ves->info.sLayerInfo[i].eFrameType;
			unsigned char * buf = ves->info.sLayerInfo[i].pBsBuf;
			int iNalCount = ves->info.sLayerInfo[i].iNalCount;
			int len = 0;

			for (int j = 0; j < iNalCount; j++) {
				len += ves->info.sLayerInfo[i].pNalLengthInByte[j];
			}

			if (type == videoFrameTypeSkip || len <= 0 || iNalCount <= 0) {
				continue;
			}

			if (type == videoFrameTypeIDR) {
				ves->got_key_frame = true;
				error_msg("encode, this is IDR\n");
			}

			if (type == videoFrameTypeI) {
				error_msg("encode, this is I\n");
			}

			if (ves->got_key_frame == false) {
				continue;
			}

			err |= h264_packetize(ves->info.uiTimeStamp, buf, len, 
			ves->encprm.pktsize, ves->pkth, ves->arg);

            info("h264_packetize: %d\n", err);
		}
	}
	return 0;
}
