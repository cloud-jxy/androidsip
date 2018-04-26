package com.zyj.sip;

import android.view.Surface;

/**
 * Created by zhangyangjing on 23/01/2018.
 */

public class Sip {

    public native static void run();
    public native static void call(String number);
    public native static void initSurface(Surface surface);
    public native static void releaseSurface();
}
