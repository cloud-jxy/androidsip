struct openh264_vidcodec {
	struct vidcodec vc;
};

/* Encode */
int openh264_encode_update(struct videnc_state **vesp, const struct vidcodec *vc,
		struct videnc_param *prm, const char *fmtp,
		videnc_packet_h *pkth, void *arg);
int openh264_encode(struct videnc_state *ves, bool update,
		const struct vidframe *frame);


/* Decode */
int openh264_decode_update(struct viddec_state **vdsp, const struct vidcodec *vc,
		const char *fmtp);
int openh264_decode(struct viddec_state *vds, struct vidframe *frame,
		bool *intra, bool marker, uint16_t seq, struct mbuf *mb);


/* SDP */
bool openh264_fmtp_cmp(const char *fmtp1, const char *fmtp2, void *data);
int openh264_fmtp_enc(struct mbuf *mb, const struct sdp_format *fmt,
		bool offer, void *arg);
