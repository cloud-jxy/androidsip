package com.xyt.sipphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xyt.jni.JniTools;
import com.xyt.sipphone.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by apple on 16/8/25.
 */
public class DialActivity extends Activity {
    public static String title;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 3:
                    ((TextView)findViewById(R.id.text_title)).setText(title);
            }
        }
    };
    public static DialActivity activity;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial);

        intTitleBar();
        init();
        activity = this;
        //定时器刷新帐号状态
        initTimer();
    }

    private void initTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(3);// 向Handler发送消息
            }
        }, 2000, 1000*5);
    }

    private void intTitleBar() {
        ((TextView)findViewById(R.id.text_title)).setText("拨号");
        ((Button)findViewById(R.id.button_forward)).setVisibility(View.INVISIBLE);
        ((Button)findViewById(R.id.button_backward)).setVisibility(View.INVISIBLE);
    }

    private void init() {
        Button btn;

        btn = (Button)findViewById(R.id.btn_0);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("0");
            }
        });

        btn = (Button)findViewById(R.id.btn_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("1");
            }
        });

        btn = (Button)findViewById(R.id.btn_2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("2");
            }
        });

        btn = (Button)findViewById(R.id.btn_3);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("3");
            }
        });

        btn = (Button)findViewById(R.id.btn_4);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("4");
            }
        });

        btn = (Button)findViewById(R.id.btn_5);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("5");
            }
        });

        btn = (Button)findViewById(R.id.btn_6);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("6");
            }
        });

        btn = (Button)findViewById(R.id.btn_7);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("7");
            }
        });

        btn = (Button)findViewById(R.id.btn_8);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("8");
            }
        });

        btn = (Button)findViewById(R.id.btn_9);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("9");
            }
        });

        btn = (Button)findViewById(R.id.btn_10);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("*");
            }
        });

        btn = (Button)findViewById(R.id.btn_11);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dial_number("#");
            }
        });

        btn = (Button)findViewById(R.id.btn_del);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = (EditText)findViewById(R.id.edit_number);
                String text = et.getText().toString();
                if (text.length() > 0) {
                    et.setText(text.substring(0, text.length()-1));
                }
            }
        });
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                EditText et = (EditText)findViewById(R.id.edit_number);
                et.setText("");
                return false;
            }
        });

        btn = (Button)findViewById(R.id.btn_call);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = (EditText)findViewById(R.id.edit_number);
                String number = et.getText().toString();

//                JniTools jni = new JniTools();
//                jni.call(number);

                Intent intent = new Intent(getApplicationContext(), SessionActivity.class);
                intent.putExtra("number", number);
                intent.putExtra("type", 1);
                startActivity(intent);
            }
        });

        btn = (Button)findViewById(R.id.btn_hangup);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JniTools jni = new JniTools();
                jni.hangup();
            }
        });

        EditText ed = (EditText)findViewById(R.id.edit_number);
        ed.setInputType(InputType.TYPE_NULL);
    }

    private void dial_number(String s) {
        EditText et = (EditText)findViewById(R.id.edit_number);
        String text = et.getText().toString();
        text += s;
        et.setText(text);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
    }

    private void refresh() {
        JniTools jni = new JniTools();
        String aor = jni.get_current_aor();

        title = aor;
        ((TextView)findViewById(R.id.text_title)).setText(aor);
    }
}
