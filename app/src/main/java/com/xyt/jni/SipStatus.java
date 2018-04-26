package com.xyt.jni;

/**
 * Created by apple on 16/8/23.
 */
/*
定义app客户端的sip状态码
 */
public class SipStatus {
    public static  int STATE_IDLE = 0;
    public static  int STATE_INCOMING = 1;
    public static  int STATE_OUTGOING = 2;
    public static  int STATE_RINGING = 3;
    public static  int STATE_EARLY = 4;
    public static  int STATE_ESTABLISHED = 5;
    public static  int STATE_TERMINATED = 6;
}
