#include <re.h>
#include <rem.h>
#include <baresip.h>
#include "openh264.h"

static struct openh264_vidcodec openh264 = {
	.vc = {
		.name      = "h264",
		.variant   = "packetization-mode=0",
		.encupdh   = openh264_encode_update,
		.ench      = openh264_encode,
		.decupdh   = openh264_decode_update,
		.dech      = openh264_decode,
		.fmtp_ench = openh264_fmtp_enc,
	},
};


static int module_init(void)
{
	vidcodec_register(baresip_vidcodecl(), (struct vidcodec *)&openh264);

	return 0;
}


static int module_close(void)
{
	vidcodec_unregister((struct vidcodec *)&openh264);

	return 0;
}

EXPORT_SYM const struct mod_export DECL_EXPORTS(openh264) = {
	"openh264",
	"codec",
	module_init,
	module_close
};
