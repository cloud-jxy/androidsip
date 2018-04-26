package com.xyt.sipphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xyt.sipphone.R;

public class AccountActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initTitle();
        initEvent();
    }

    private void initEvent() {
        ((Button)findViewById(R.id.btn1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AccountDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initTitle() {
        ((TextView)findViewById(R.id.text_title)).setText("帐号设置");
        ((Button)findViewById(R.id.button_forward)).setVisibility(View.INVISIBLE);
        ((Button)findViewById(R.id.button_backward)).setVisibility(View.INVISIBLE);
    }
}
