MOD		:= h264
$(MOD)_SRCS	+= decode.c
$(MOD)_SRCS	+= encode.c
$(MOD)_SRCS	+= h264.c
$(MOD)_SRCS	+= sdp.c

include mk/mod.mk