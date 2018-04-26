#include <re.h>
#include <rem.h>
#include <baresip.h>
#include "h264.h"

static struct h264_vidcodec h264 = {
	.vc = {
		.name      = "h264",
		.variant   = "packetization-mode=0",
		.encupdh   = h264_encode_update,
		.ench      = h264_encode,
		.decupdh   = h264_decode_update,
		.dech      = h264_decode,
		.fmtp_ench = h264_fmtp_enc,
	},
};


static int module_init(void)
{
	vidcodec_register(baresip_vidcodecl(), (struct vidcodec *)&h264);

	return 0;
}


static int module_close(void)
{
	vidcodec_unregister((struct vidcodec *)&h264);

	return 0;
}

EXPORT_SYM const struct mod_export DECL_EXPORTS(jdkcodec) = {
	"jdkcodec",
	"codec",
	module_init,
	module_close
};
