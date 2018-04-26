package com.xyt.sipphone.unit;

import android.hardware.Camera;
import android.util.Log;

/**
 * Created by jxy on 2018/3/20.
 */

public class CameraTest {
    public static void test() {
        if (hasCamera()) {
            Log.e("andrdoidsip", "has camera");
        } else {
            Log.e("andrdoidsip", "no camera");
        }

        if (hasFrontFacingCamera()) {
            Log.e("andrdoidsip", "hasFrontFacingCamera");
        } else {
            Log.e("andrdoidsip", "noFrontFacingCamera");
        }

        if (hasBackFacingCamera()) {
            Log.e("andrdoidsip", "hasBackFacingCamera");
        } else {
            Log.e("andrdoidsip", "noBackFacingCamera");
        }
    }

    public static boolean checkCameraFacing(final int facing) {
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查设备是否有摄像头
     * @return
     */
    public static boolean hasCamera() {
        return hasBackFacingCamera() || hasFrontFacingCamera();
    }

    /**检查设备是否有后置摄像头
     * @return
     */
    public static boolean hasBackFacingCamera() {
        final int CAMERA_FACING_BACK = 0;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    /**检查设备是否有前置摄像头
     * @return
     */
    public static boolean hasFrontFacingCamera() {
        final int CAMERA_FACING_BACK = 1;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    public static int getSdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }
}
