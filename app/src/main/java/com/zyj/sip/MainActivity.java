package com.zyj.sip;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.xyt.sipphone.R;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    Thread mThread;
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_zyj);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Sip.run();
            }
        });
        mThread.setName("Sip Loop");
        mThread.start();

        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(this);

        findViewById(R.id.btn_dial).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Sip.call("3000");
            }
        });

//        try {
//            Thread.sleep(3000);
//            Sip.call("3000\n");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Surface surface = surfaceHolder.getSurface();
        Sip.initSurface(surface);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Sip.releaseSurface();
    }
}
