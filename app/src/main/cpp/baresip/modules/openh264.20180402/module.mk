MOD		:= openh264
$(MOD)_SRCS	+= decode.c
$(MOD)_SRCS	+= encode.c
$(MOD)_SRCS	+= openh264.c
$(MOD)_SRCS	+= sdp.c
$(MOD)_LFLAGS	+= -lopenh264

include mk/mod.mk