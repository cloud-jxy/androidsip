/**
 * @file opengles.c Video driver for OpenGLES
 *
 * Copyright (C) 2010 - 2015 Creytiv.com
 */

#include <pthread.h>
#include <re.h>
#include <rem.h>
#include <baresip.h>

extern void render_h264(void* buf, int length);

struct vidisp_st
{
    const struct vidisp *vd; /* pointer to base-class (inheritance) */
    struct vidframe *vf;

    
};

static struct vidisp *vid;

static void destructor(void *arg)
{
    struct vidisp_st *st = arg;
    context_destroy(st);
    mem_deref(st->vf);
}

static int my_init(struct vidisp_st *st) {
    (void)st;

    return 0;
}

static int h264Render_alloc(struct vidisp_st **stp, const struct vidisp *vd,
                            struct vidisp_prm *prm, const char *dev,
                            vidisp_resize_h *resizeh,
                            void *arg)
{
    info("h264Render_alloc");
    struct vidisp_st *st;
    int err = 0;

    (void)prm;
    (void)dev;
    (void)resizeh;
    (void)arg;

    st = mem_zalloc(sizeof(*st), destructor);
    if (!st)
        return ENOMEM;

    st->vd = vd;

    err = my_init(st);
    if (err)
        goto out;

out:
    if (err)
        mem_deref(st);
    else
        *stp = st;

    return err;
}

static int h264Render_display(struct vidisp_st *st, const char *title,
                              const struct vidframe *frame)
{
    (void)title;

    return render_h264(frame->data[0], frame->LineSize[0]);    
}

static int module_init(void)
{
    int err = 0;
    err |= vidisp_register(&vid, baresip_vidispl(),
                           "h264Render", h264Render_alloc, NULL,
                           h264Render_display, NULL);
    return err;
}

static int module_close(void)
{
    vid = mem_deref(vid);

    return 0;
}

EXPORT_SYM const struct mod_export DECL_EXPORTS(h264Render) = {
    "h264Render",
    "vidisp",
    module_init,
    module_close,
};
