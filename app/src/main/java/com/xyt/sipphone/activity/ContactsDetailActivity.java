package com.xyt.sipphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xyt.jni.JniTools;
import com.xyt.sipphone.R;
import com.xyt.sqlite.dao.ContactsRepo;

public class ContactsDetailActivity extends Activity {
    private String keyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_detail);

        initTitleBar();
        initView();
        initEvent();
    }

    private void initEvent() {
        Button btn;

        btn = (Button)findViewById(R.id.contacts_detail_del);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });

        btn = (Button)findViewById(R.id.contacts_detail_call);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call();
            }
        });
    }

    private void initView() {
        Intent intent = getIntent();
        System.out.println(intent.getSerializableExtra("name"));
        System.out.println(intent.getSerializableExtra("number"));
        System.out.println(intent.getSerializableExtra("domain"));
        System.out.println(intent.getSerializableExtra("keyId"));

        keyId = (String) intent.getSerializableExtra("keyId");
        TextView tv;
        tv= (TextView)findViewById(R.id.contacts_detail_name);
        tv.setText((String)intent.getSerializableExtra("name"));
        tv= (TextView)findViewById(R.id.contacts_detail_number);
        tv.setText((String)intent.getSerializableExtra("number"));
        tv= (TextView)findViewById(R.id.contacts_detail_domain);
        tv.setText((String)intent.getSerializableExtra("domain"));
    }

    private void initTitleBar() {
        TextView tv = (TextView)findViewById(R.id.text_title);
        tv.setText("联系人详情");

        Button btn = (Button)findViewById(R.id.button_backward);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn = (Button)findViewById(R.id.button_forward);
        btn.setText("编辑");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit();
            }
        });
    }

    private void edit() {
        Intent intent = new Intent(getApplicationContext(), AddContactsActivity.class);

        TextView tv;
        tv= (TextView)findViewById(R.id.contacts_detail_name);
        intent.putExtra("name", tv.getText().toString());
        tv= (TextView)findViewById(R.id.contacts_detail_number);
        intent.putExtra("number", tv.getText().toString());
        tv= (TextView)findViewById(R.id.contacts_detail_domain);
        intent.putExtra("domain", tv.getText().toString());
        intent.putExtra("keyId", keyId);
        intent.putExtra("operate", "edit");

        startActivity(intent);
    }


    private void delete() {
        ContactsRepo repo = new ContactsRepo(getApplicationContext());
        repo.delete(Integer.parseInt(keyId));
        finish();
    }


    private void call() {
        String number = ((TextView)findViewById(R.id.contacts_detail_number)).getText().toString();
        String domain = ((TextView)findViewById(R.id.contacts_detail_domain)).getText().toString();

        String aor = number + '@' + domain;
        JniTools jni = new JniTools();
//        jni.call(aor);

        Intent intent = new Intent(getApplicationContext(), SessionActivity.class);
        intent.putExtra("number", number);
        intent.putExtra("type", 1);
        startActivity(intent);
    }
}
