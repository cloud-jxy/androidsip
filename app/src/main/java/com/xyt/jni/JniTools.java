package com.xyt.jni;

import android.view.Surface;

import com.xyt.sipphone.EventThread;

/**
 * Created by apple on 16/7/27.
 */
public class JniTools {
    public static int SIP_EVENT = 0;

    public native static void initSurface(Surface surface);
    public native static void releaseSurface();

    public native int init(String configPath);
    public native int close();
    public native int run();

    public native int reg(String text);
    public native void unreg(String aor);
    public native void unreg_all();
    public native int call(String number);
    public native int answer();
    public native int hangup();
    public native int set_current_account(String aor);

    public native boolean is_reg(String aor);

    /*
    set/get语音文件路径.
     */
    public native void set_play_path(String path);
    public native String get_play_path();

    public native String get_current_aor();

    public native int digit_handler(byte key);

    public void handle(Call call) {
        System.out.println("Msg: " + call.event
                + " status=" + call.status
                + " start-time=" + call.start_time
                + " conn-time=" + call.conn_time
                + " stop-time=" + call.stop_time);

        EventThread.mHandler.obtainMessage(SIP_EVENT, call).sendToTarget();
    }



//    public native int test();
    public static native void update(byte[] data, int w, int h);
    public static native int test();

    public static native void updateH264(byte[] data, int mFrameWidth, int mFrameHeight, int len);
}
