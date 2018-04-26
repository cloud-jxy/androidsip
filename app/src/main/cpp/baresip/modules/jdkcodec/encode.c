#include <re.h>
#include <rem.h>
#include <baresip.h>
#include <sys/time.h>

struct videnc_state {
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

}

int h264_encode_update(struct videnc_state **vesp, const struct vidcodec *vc,
		struct videnc_param *prm, const char *fmtp,
		videnc_packet_h *pkth, void *arg) {
	info("h264_encode_update\n");

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

	struct videnc_state *old;
	old = *vesp;
	*vesp = st;

	if (old) {
		mem_deref(old);
	}

	info("h264: video encoder %s: %d fps, %d bit/s, pktsize=%u\n",
		vc->name, prm->fps, prm->bitrate, prm->pktsize);
	return 0;
}


int h264_encode(struct videnc_state *ves, bool update,
		const struct vidframe *frame) {
	info("h264_encode\n");

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

        info("h264_packetize: %d; fmt=%d\n", err, frame->fmt);

	}
	return 0;
}
