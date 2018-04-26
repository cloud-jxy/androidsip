package com.xyt.sipphone.activity;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.RadioGroup;
import android.widget.TabHost;

import com.tencent.bugly.crashreport.CrashReport;
import com.xyt.jni.JniTools;
import com.xyt.sipphone.EventThread;
import com.xyt.sipphone.R;
import com.xyt.sipphone.Util;

import java.io.File;

public class MainActivity extends ActivityGroup implements RadioGroup.OnCheckedChangeListener{
    private RadioGroup radioderGroup;
    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Util.isTvPlatform(this)) {
            // TODO: start tv Activity
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        EventThread.mMain = this;
        new EventThread().start();

        CrashReport.initCrashReport(getApplicationContext(), "2c9f1dedde", true);

        ensureBaresipConfig();

       JniTools jni = new JniTools();
       jni.init(Util.getBaresipConfigPath(this));
       jni.set_play_path("/data/local/tmp/music");

//       sip:607:xyt607@xswitch.cn:7060
       SharedPreferences preferences = getSharedPreferences("uag", Context.MODE_PRIVATE);
       String number = preferences.getString("number", "607");
       String password = preferences.getString("password", "xyt607");
       String domain = preferences.getString("domain", "xswitch.cn:7060");
       String aor = "sip:" + number + ":" + password + "@" + domain;

       if (true) {
           SharedPreferences.Editor editor = preferences.edit();
           editor.putString("number", number);
           editor.putString("password", password);
           editor.putString("domain", domain);
           editor.commit();
       }

//        String aor = "sip:607:xyt607@xswitch.cn:7060";
       new Thread(new Runnable() {
           @Override
           public void run() {
               JniTools jni = new JniTools();
               jni.run();
           }
       }).start();
       if(!aor.isEmpty() && !jni.is_reg(aor)) {
           jni.reg("sip:1122:test1234@xswitch.cn:7060");
       }


        tabHost=(TabHost)findViewById(R.id.MainTab);
        tabHost.setup(this.getLocalActivityManager());
        LayoutInflater inflater=LayoutInflater.from(this);

        Intent intent1 = new Intent().setClass(this, DialActivity.class);
        tabHost.addTab(tabHost.newTabSpec("1").setIndicator("拨号").setContent(intent1));

        Intent intent2 = new Intent().setClass(this, ContactsActivity.class);
        tabHost.addTab(tabHost.newTabSpec("2").setIndicator("通信录").setContent(intent2));

        Intent intent3 = new Intent().setClass(this, AccountActivity.class);
        tabHost.addTab(tabHost.newTabSpec("3").setIndicator("帐号").setContent(intent3));

        Intent intent4 = new Intent().setClass(this, HistoryActivity.class);
        tabHost.addTab(tabHost.newTabSpec("4").setIndicator("通话记录").setContent(intent4));

        radioderGroup=(RadioGroup)findViewById(R.id.main_radio);
        radioderGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.r1:
                tabHost.setCurrentTabByTag("1");
                break;
            case R.id.r2:
                tabHost.setCurrentTabByTag("2");
                break;
            case R.id.r3:
                tabHost.setCurrentTabByTag("3");
                break;
            case R.id.r4:
                tabHost.setCurrentTabByTag("4");
                break;
        }
    }

    private void ensureBaresipConfig() {
        String configPath = Util.getBaresipConfigPath(this);
        if (true || false == new File(configPath).exists())
            Util.copyAssets(this, "baresip_config", configPath);
    }

    static {
        System.loadLibrary("native-lib");
    }
}
