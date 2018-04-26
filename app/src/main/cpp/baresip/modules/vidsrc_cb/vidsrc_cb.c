/**
 * Copyright (C) 2018 Xueyun Jiang
*/

#include <re.h>
#include <rem.h>
#include <baresip.h>
#include <pthread.h>
#include <malloc.h>

static int alloc(struct vidsrc_st **stp, const struct vidsrc *vs,
		 struct media_ctx **ctx, struct vidsrc_prm *prm,
		 const struct vidsz *size, const char *fmt,
		 const char *dev, vidsrc_frame_h *frameh,
		 vidsrc_error_h *errorh, void *arg);

struct vidsrc_st {
	const struct vidsrc *vs;  /* inheritance */
	int fps;
	struct vidsrc_prm prm;
	struct vidsz size;
	bool run;
	bool update;
	pthread_t thread;
	struct vidframe *frame;
	vidsrc_frame_h *frameh;
	void *arg;
};


static struct vidsrc *vidsrc;
struct vidsrc_st *g_vidsrc_st = NULL;

// void my_vidsrc_update(void *buf, int w, int h) {
// 	struct vidframe f;
// 	struct vidsz size;

// 	if (!g_vidsrc_st || !g_vidsrc_st->frameh) {
// 		info("%s\n", "update null");

// 		return;
// 	}

// 	size.w = w;
// 	size.h = h;
// 	vidframe_init_buf(&f, VID_FMT_NV21, &size, (uint8_t *)buf);
//     g_vidsrc_st->frameh(&f, g_vidsrc_st->arg);

// 	info("%s\n", "update over");
// }

static void	destructor(void *arg)
{
	struct vidsrc_st *st = arg;

	(void)st;
	g_vidsrc_st = NULL;

//	if (st->run) {
//		st->run = false;
//		pthread_join(st->thread, NULL);
//	}

	mem_deref(st->frame);
}

static void process(struct vidsrc_st *st)
{
	struct vidframe f;
	int size = st->size.w * st->size.h * 4;
	char *buf = (char *)malloc(size);
	static int color = 0;
	
	color = (color + 4) % 255;
	memset(buf, color, size);

	vidframe_init_buf(&f, VID_FMT_NV21, &st->size, (uint8_t *)buf);
	st->frameh(&f, st->arg);

	info("vidsrc_cb: read");

	st->update = false;

	free(buf);
}

static void *read_thread(void *arg)
{
	struct vidsrc_st *st = arg;
	uint64_t ts = 0;

	while (st->run) {
		sys_msleep(1000 / st->prm.fps);

		process(st);

		ts += 1000/st->prm.fps;
	}

	return NULL;
}

static int alloc(struct vidsrc_st **stp, const struct vidsrc *vs,
		 struct media_ctx **ctx, struct vidsrc_prm *prm,
		 const struct vidsz *size, const char *fmt,
		 const char *dev, vidsrc_frame_h *frameh,
		 vidsrc_error_h *errorh, void *arg)
{
	struct vidsrc_st *st;
	int err = 0;

	info("vidsrc_cb alloc\n");

	(void)ctx;
	(void)fmt;
	(void)dev;
	(void)errorh;

	// if (!stp || !prm || !size || !frameh)
	// 	return EINVAL;

	st = mem_zalloc(sizeof(*st), destructor);
	if (!st)
		return ENOMEM;

    g_vidsrc_st = st;

	st->vs     = vs;
	st->size   = *size;
	st->fps    = prm->fps;
	st->frameh = frameh;
	st->arg    = arg;
	st->prm    = *prm;
	st->run    = true;
	st->update = false;

	err = vidframe_alloc(&st->frame, VID_FMT_RGB32, size);
	if (err)
		goto out;

//	err = pthread_create(&st->thread, NULL, read_thread, st);


 out:
	if (err)
		mem_deref(st);
	else
		*stp = st;

	return err;
}


static int module_init(void)
{
	info("vidsrc_cb init!\n");
	return vidsrc_register(&vidsrc, baresip_vidsrcl(), "vidsrc_cb", alloc, NULL);
}

int vidsrc_cb_init()
{
	info("vidsrc_cb_init init!\n");
	return vidsrc_register(&vidsrc, baresip_vidsrcl(), "vidsrc_cb", alloc, NULL);
}


static int module_close(void)
{
	vidsrc = mem_deref(vidsrc);
	return 0;
}


EXPORT_SYM const struct mod_export DECL_EXPORTS(vidsrc_cb) = {
	"vidsrc_cb",
	"vidsrc",
	module_init,
	module_close
};
