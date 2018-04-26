package com.xyt.sipphone.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.xyt.jni.JniTools;
import com.xyt.sipphone.R;

public class AccountDetailActivity extends Activity {
    private String old_aor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        initTitleBar();
        initView();
    }

    private void initView() {
        SharedPreferences preferences=getSharedPreferences("uag", Context.MODE_PRIVATE);

        String number = preferences.getString("number", "");
        String password = preferences.getString("password", "");
        String domain = preferences.getString("domain", "");

        old_aor = "sip:" + number + ":" + password + "@" + domain;

        ((EditText)findViewById(R.id.number)).setText(number);
        ((EditText)findViewById(R.id.password)).setText(password);
        ((EditText)findViewById(R.id.domain)).setText(domain);
    }

    private void initTitleBar() {
        ((Button)findViewById(R.id.button_backward)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ((Button)findViewById(R.id.button_forward)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = ((EditText)findViewById(R.id.number)).getText().toString();
                String password = ((EditText)findViewById(R.id.password)).getText().toString();
                String domain = ((EditText)findViewById(R.id.domain)).getText().toString();

                SharedPreferences sharedPref = getSharedPreferences("uag", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPref.edit();

                editor.putString("number", number);
                editor.putString("password", password);
                editor.putString("domain", domain);

                editor.commit();

                String aor = "sip:" + number + ":" + password + "@" + domain;
                JniTools jni = new JniTools();
//                if(!jni.is_reg(aor)) {
//                    jni.reg(aor);
//                }
//
//                if (!aor.equals(old_aor)) {
//                    jni.unreg(old_aor);
//                }

                finish();
            }
        });
    }
}
