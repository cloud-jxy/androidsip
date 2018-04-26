/**
 * @file fakevideo.c Fake video source and video display
 *
 * Copyright (C) 2010 Creytiv.com
 */
#define _DEFAULT_SOURCE 1
#define _BSD_SOURCE 1
#include <unistd.h>
#include <pthread.h>
#include <re.h>
#include <rem.h>
#include <baresip.h>


/**
 * @defgroup fakevideo fakevideo
 *
 * Fake video source and display module
 *
 * This module can be used to generate fake video input frames, and to
 * send output video frames to a fake non-existant display.
 *
 * Example config:
 \verbatim
  video_source    fakevideo,nil
  video_display   fakevideo,nil
 \endverbatim
 */


struct vidsrc_st {
	const struct vidsrc *vs;  /* inheritance */
	struct vidframe *frame;
	pthread_t thread;
	bool run;
	int fps;
	vidsrc_frame_h *frameh;
	void *arg;
	struct vidsz size;
	pthread_mutex_t mutex;
};

struct vidisp_st {
	const struct vidisp *vd;  /* inheritance */
};


static struct vidsrc *vidsrc;
static struct vidisp *vidisp;

static struct vidsrc_st *g_vidsrc_st;

void my_vidsrc_update(void *buf, int w, int h) {
	struct vidframe f;
	struct vidsz size;

	if (!g_vidsrc_st) {
		return;
	}

	pthread_mutex_lock(&(g_vidsrc_st->mutex));
	if (!g_vidsrc_st || !g_vidsrc_st->frameh) {
		info("%s\n", "fakevideo update null");
		pthread_mutex_unlock(&(g_vidsrc_st->mutex));
		return;
	}

	size.w = w;
	size.h = h;
	vidframe_init_buf(&f, VID_FMT_NV21, &size, (uint8_t *)buf);

	if (g_vidsrc_st->frameh) {
		g_vidsrc_st->frameh(&f, g_vidsrc_st->arg);
	}

	pthread_mutex_unlock(&(g_vidsrc_st->mutex));
	info("%s\n", "fakevideo update over");
}

void my_vidsrc_update_h264(void *buf, int w, int h, int len)
{
	struct vidframe f;
	struct vidsz size;

	if (!g_vidsrc_st)
	{
		return;
	}

	pthread_mutex_lock(&(g_vidsrc_st->mutex));
	if (!g_vidsrc_st || !g_vidsrc_st->frameh)
	{
		info("%s\n", "fakevideo update null");
		pthread_mutex_unlock(&(g_vidsrc_st->mutex));
		return;
	}

	size.w = w;
	size.h = h;
	vidframe_init_buf_h264(&f, VID_FMT_H264, &size, (uint8_t *)buf, len);

	if (g_vidsrc_st->frameh)
	{
		g_vidsrc_st->frameh(&f, g_vidsrc_st->arg);
	}

	pthread_mutex_unlock(&(g_vidsrc_st->mutex));
	info("%s\n", "fakevideo update over");
}

static void process(struct vidsrc_st *st)
{
	struct vidframe f;
	int size = st->size.w * st->size.h * 4;
	char *buf = (char *)malloc(size);
	static int color = 0;

	info("fakevideo: process\n");
	
	color = (color + 4) % 255;
	memset(buf, color, size);

	vidframe_init_buf(&f, VID_FMT_NV21, &st->size, buf);
	st->frameh(&f, st->arg);

	free(buf);
}

static void *read_thread(void *arg)
{
	struct vidsrc_st *st = arg;
	uint64_t ts = tmr_jiffies();

	info("fakevideo: read_thread\n");

	while (st->run) {
		sys_msleep(1000 / st->fps);

		process(st);

		ts += 1000/st->fps;
	}

	return NULL;
}


static void src_destructor(void *arg)
{
	struct vidsrc_st *st = arg;

	pthread_mutex_lock(&(st->mutex));

	info("fakevideo: src_destructor\n");

	g_vidsrc_st = NULL;

	if (st->run) {
		st->run = false;
		pthread_join(st->thread, NULL);
	}

	mem_deref(st->frame);

	pthread_mutex_unlock(&(st->mutex));
	pthread_mutex_destroy(&(st->mutex));
}


static void disp_destructor(void *arg)
{
	struct vidisp_st *st = arg;
	(void)st;
}


static int src_alloc(struct vidsrc_st **stp, const struct vidsrc *vs,
		     struct media_ctx **ctx, struct vidsrc_prm *prm,
		     const struct vidsz *size, const char *fmt,
		     const char *dev, vidsrc_frame_h *frameh,
		     vidsrc_error_h *errorh, void *arg)
{
	struct vidsrc_st *st;
	int err;

	(void)ctx;
	(void)fmt;
	(void)dev;
	(void)errorh;

	info("fakevideo src_alloc!\n");

	if (!stp || !prm || !size || !frameh)
		return EINVAL;

	st = mem_zalloc(sizeof(*st), src_destructor);
	if (!st)
		return ENOMEM;

	g_vidsrc_st = st;

	st->vs     = vs;
	st->fps    = prm->fps;
	st->frameh = frameh;
	st->arg    = arg;
	st->size   = *size;

	pthread_mutex_init(&st->mutex, NULL);

	err = vidframe_alloc(&st->frame, VID_FMT_YUV420P, size);
	if (err)
		goto out;

	st->run = true;
	// err = pthread_create(&st->thread, NULL, read_thread, st);
	if (err) {
		st->run = false;
		goto out;
	}

 out:
	if (err)
		mem_deref(st);
	else
		*stp = st;

	return err;
}


static int disp_alloc(struct vidisp_st **stp, const struct vidisp *vd,
		      struct vidisp_prm *prm, const char *dev,
		      vidisp_resize_h *resizeh, void *arg)
{
	struct vidisp_st *st;
	(void)prm;
	(void)dev;
	(void)resizeh;
	(void)arg;

	if (!stp || !vd)
		return EINVAL;

	st = mem_zalloc(sizeof(*st), disp_destructor);
	if (!st)
		return ENOMEM;

	st->vd = vd;

	*stp = st;

	return 0;
}


static int display(struct vidisp_st *st, const char *title,
		   const struct vidframe *frame)
{
	(void)st;
	(void)title;
	(void)frame;

	return 0;
}


static int module_init(void)
{
	int err = 0;
	info("falevideo init!\n");
	err |= vidsrc_register(&vidsrc, baresip_vidsrcl(),
			       "fakevideo", src_alloc, NULL);
	err |= vidisp_register(&vidisp, baresip_vidispl(),
			       "fakevideo", disp_alloc, NULL,
			       display, NULL);
	return err;
}


static int module_close(void)
{
	vidsrc = mem_deref(vidsrc);
	vidisp = mem_deref(vidisp);
	return 0;
}


EXPORT_SYM const struct mod_export DECL_EXPORTS(fakevideo) = {
	"fakevideo",
	"fakevideo",
	module_init,
	module_close
};
