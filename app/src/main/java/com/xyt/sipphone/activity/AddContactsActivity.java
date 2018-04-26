package com.xyt.sipphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xyt.sipphone.R;
import com.xyt.sipphone.model.SortModel;
import com.xyt.sqlite.dao.ContactsRepo;
import com.xyt.sqlite.model.Contacts;

public class AddContactsActivity extends Activity {
    private boolean bEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        initTitleBar();
        initEvent();
        initView();
    }

    private void initTitleBar() {
        TextView tv = (TextView)findViewById(R.id.text_title);
        tv.setText("添加联系人");

        Button btn = (Button)findViewById(R.id.button_forward);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });

        btn = (Button)findViewById(R.id.button_backward);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void add() {
        Contacts record = new Contacts();
        record.name = ((EditText)findViewById(R.id.contacts_name)).getText().toString();
        record.number = ((EditText)findViewById(R.id.contacts_number)).getText().toString();
        record.domain = ((EditText)findViewById(R.id.contacts_domain)).getText().toString();

        if (record.name.isEmpty() || record.name.isEmpty() || record.domain.isEmpty()) {
            Toast.makeText(getApplicationContext(), "参数错误", 0).show();
            return;
        }

        ContactsRepo repo = new ContactsRepo(getApplicationContext());

        if (bEdit) {
            Intent intent = getIntent();
            String keyId = (String) intent.getSerializableExtra("keyId");
            record.id = Integer.parseInt(keyId);
            repo.update(record);


            intent = new Intent(getApplicationContext(), ContactsDetailActivity.class);

            intent.putExtra("name", record.name);
            intent.putExtra("number", record.number);
            intent.putExtra("domain", record.domain);
            intent.putExtra("keyId", keyId);

            startActivity(intent);
        } else {
            repo.insert(record);
            finish();
        }
    }

    private void initView() {
        Intent intent = getIntent();
        String operate = (String)intent.getSerializableExtra("operate");

        if (operate.equals("edit")) {
            EditText et = (EditText)findViewById(R.id.contacts_name);
            et.setText((CharSequence) intent.getSerializableExtra("name"));

            et = (EditText)findViewById(R.id.contacts_number);
            et.setText((CharSequence) intent.getSerializableExtra("number"));

            et = (EditText)findViewById(R.id.contacts_domain);
            et.setText((CharSequence) intent.getSerializableExtra("domain"));

            bEdit = true;
        } else {
            bEdit = false;
        }
    }

    private void initEvent() {

    }
}
