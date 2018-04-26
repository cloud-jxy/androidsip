package com.xyt.jni;

/**
 * Created by apple on 16/8/19.
 * 定义sip消息码
 */
public class SipEvent {
    public static int UA_EVENT_REGISTERING = 0;
    public static int UA_EVENT_OK = 1;
    public static int UA_EVENT_REGISTER_FAIL = 2;
    public static int UA_EVENT_UNREGISTERING =3;
    public static int UA_EVENT_SHUTDOWN = 4;
    public static int UA_EVENT_EXIT = 5;
    public static int UA_EVENT_CALL_INCOMING = 6;
    public static int UA_EVENT_CALL_RINGING = 7;
    public static int UA_EVENT_CALL_PROGRESS = 8;
    public static int UA_EVENT_CALL_ESTABLISHED = 9;
    public static int UA_EVENT_CALL_CLOSED = 10;
    public static int UA_EVENT_CALL_TRANSFER_FAILED = 11;
    public static int UA_EVENT_CALL_DTMF_START = 12;
    public static int UA_EVENT_CALL_DTMF_EN = 13;
}
