package com.xyt.sipphone.activity;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xyt.codec.AvcEncoder;
import com.xyt.codec.Decoder4Jni;
import com.xyt.jni.JniTools;
import com.xyt.sipphone.EventThread;
import com.xyt.sipphone.R;
import com.xyt.sipphone.unit.CameraPreview;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class SessionActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceView mRemoteSurfaceView;
    private SurfaceView mLocalSurfaceView;
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private byte[] mVidbuf = null;
    private int mFrameWidth = 0;
    private int mFrameHeight = 0;
    private AvcEncoder mEncoder = null;
    private byte[] mOutputData = null;

    int seconds = 0;
    Timer mTimer = new Timer();
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    seconds++;
                    TextView tv = (TextView)findViewById(R.id.status);
                    tv.setText(getStrTime(seconds));
            }
        }
    };

    private String getStrTime(int seconds) {
        String re_StrTime;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        long lcc_time = Long.valueOf(seconds);
        re_StrTime = sdf.format(new Date(lcc_time * 1000L));

        return re_StrTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        setRemoteSurfaceView();
        setLocalSurfaceView();

        Log.e("androidsip", "surface view init over!");

        EventThread.mSess = this;
        String number = (String)getIntent().getSerializableExtra("number");
        int type = (int)getIntent().getSerializableExtra("type");

        if (!number.isEmpty()) {
            TextView tv = (TextView)findViewById(R.id.number);
            tv.setText(number);
        }

        TextView tv = (TextView)findViewById(R.id.status);
        tv.setText("呼叫中……");

        if (type == 1) {
            Button btn = (Button)findViewById(R.id.hangup1);
            btn.setVisibility(View.VISIBLE);
            btn = (Button)findViewById(R.id.hangup2);
            btn.setVisibility(View.INVISIBLE);
            btn = (Button)findViewById(R.id.answer);
            btn.setVisibility(View.INVISIBLE);
        } else {
            Button btn = (Button)findViewById(R.id.hangup1);
            btn.setVisibility(View.INVISIBLE);
            btn = (Button)findViewById(R.id.hangup2);
            btn.setVisibility(View.VISIBLE);
            btn = (Button)findViewById(R.id.answer);
            btn.setVisibility(View.VISIBLE);
        }

        Button btn = (Button)findViewById(R.id.hangup1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hangup();
            }
        });

        btn = (Button)findViewById(R.id.hangup2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hangup();
            }
        });

        btn = (Button)findViewById(R.id.answer);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answer();
            }
        });
    }

    private void setLocalSurfaceView() {
        // Create an instance of Camera
        if (mCamera == null) {
            mCamera = getCameraInstance();
        }

        // Create our Preview view and set it as the content of our activity.
        mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.video_local);
        preview.addView(mCameraPreview);

        Camera.Parameters cameraParam = mCamera.getParameters();

//        cameraParam.setPreviewFormat(ImageFormat.YV12);
        cameraParam.setPreviewFormat(ImageFormat.YV12);
        cameraParam.setPreviewFrameRate(25);

        mCamera.setParameters(cameraParam);

        mCamera.setPreviewCallbackWithBuffer(this);
        int frameHeight = mCamera.getParameters().getPreviewSize().height;
        int frameWidth = mCamera.getParameters().getPreviewSize().width;
        mVidbuf = new byte[frameHeight * frameWidth * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8];

        mCamera.addCallbackBuffer(mVidbuf);
    }

    private void setRemoteSurfaceView() {
        mRemoteSurfaceView = findViewById(R.id.video_remote);
        mRemoteSurfaceView.getHolder().addCallback(this);
        Decoder4Jni.mSurface = mRemoteSurfaceView.getHolder().getSurface();
    }

    public void established() {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(1);// 向Handler发送消息
            }
        }, 0, 1000);
    }

    public void hangup() {
        JniTools jni = new JniTools();
        jni.hangup();

        mTimer.cancel();
        finish();
    }

    public void answer() {
        JniTools jni = new JniTools();
        jni.answer();

        Button btn = (Button)findViewById(R.id.hangup1);
        btn.setVisibility(View.VISIBLE);
        btn = (Button)findViewById(R.id.hangup2);
        btn.setVisibility(View.INVISIBLE);
        btn = (Button)findViewById(R.id.answer);
        btn.setVisibility(View.INVISIBLE);
    }

    public void test(View view) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        String number = (String)getIntent().getSerializableExtra("number");
        int type = (int)getIntent().getSerializableExtra("type");

        Surface surface = surfaceHolder.getSurface();
        JniTools.initSurface(surface);

        Log.e("androidsip", String.format("number=%s, type=%d\n", number, type));
        if (type == 1) {
            JniTools jni = new JniTools();
            Log.e("androidsip", String.format("call number=%s\n", number));
            int ret = jni.call(number);
            Log.e("androidsip", String.format("call number=%s, return %d\n", number, ret));
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        JniTools.releaseSurface();
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = Camera.open(0);//open the camera for the application
            mCamera.startPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.e("androidsip", "onPreviewFrame\n");

        if (camera == null) {
            return;
        }

        if (mFrameWidth == 0 || mFrameWidth == 0) {
            mFrameWidth = camera.getParameters().getPreviewSize().width;
            mFrameHeight = camera.getParameters().getPreviewSize().height;
        }

        if (mEncoder == null) {
            mEncoder = new AvcEncoder();
            try {
                mEncoder.init(mFrameWidth, mFrameHeight, 25, 2500000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mOutputData == null) {
            mOutputData = new byte[mFrameWidth * mFrameHeight * 3 / 2];
        }

        int len = mEncoder.offerEncoder(data, mOutputData);

//        Log.e("androidsip", String.format("%dx%d", mFrameWidth, mFrameHeight));
        JniTools.updateH264(mOutputData, mFrameWidth, mFrameHeight, len);
        camera.addCallbackBuffer(data);
    }
}
