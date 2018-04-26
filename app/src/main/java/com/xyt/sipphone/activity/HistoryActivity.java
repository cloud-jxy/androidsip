package com.xyt.sipphone.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xyt.sipphone.R;
import com.xyt.sipphone.adapter.ContactsAdapter;
import com.xyt.sipphone.adapter.HistoryAdapter;
import com.xyt.sipphone.model.HistoryItem;
import com.xyt.sipphone.model.SortModel;
import com.xyt.sipphone.unit.SlideView;
import com.xyt.sqlite.dao.CdrRepo;
import com.xyt.sqlite.model.Cdr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

public class HistoryActivity extends Activity {

    private List<HistoryItem> SourceDateList;
    private HistoryAdapter adapter;
    public static ListView sortListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }

    @Override
    protected void onStart() {
        super.onStart();
        intTitleBar();
        initView();
        initEvent();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void intTitleBar() {
        ((TextView)findViewById(R.id.text_title)).setText("通话记录");
        ((Button)findViewById(R.id.button_backward)).setVisibility(View.INVISIBLE);
        ((Button)findViewById(R.id.button_forward)).setText("清空");

        ((Button)findViewById(R.id.button_forward)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CdrRepo repo = new CdrRepo(getApplicationContext());
                repo.deleteAll();
                adapter.mMessageItems.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void initEvent() {

    }

    private void initView() {
        SourceDateList = getData();

        sortListView = (ListView) findViewById(R.id.history_lv);
        adapter = new HistoryAdapter(this, SourceDateList);
        sortListView.setAdapter(adapter);
    }


    private List<HistoryItem> getData() {
        ArrayList<HistoryItem> ret = new ArrayList<HistoryItem>();
        CdrRepo rep = new CdrRepo(this);
        ArrayList<HashMap<String, String>> list = rep.getList();
        for(HashMap<String, String> data  : list) {
            HistoryItem item = new HistoryItem();

            int time1 = Integer.parseInt(data.get(Cdr.KEY_start));
            int time2 = Integer.parseInt(data.get(Cdr.KEY_conn));
            int time3 = Integer.parseInt(data.get(Cdr.KEY_stop));
            item.name = data.get(Cdr.KEY_peer);
            if (time1 == 0 || time3-time1 <= 2) {
                item.b_answer = false;
                item.duration = "未接电话";
            } else {
                item.b_answer = true;
                item.duration = (time3-time1)+"秒";
            }

            Log.e("test", "stime="+time1+" ctime="+time2+"stop_time="+time3);

            Date date;
            if (item.b_answer) {
                date = new Date(time1*1000L);
            } else {
                date = new Date(time3 * 1000L);
            }
            //注意format的格式要与日期String的格式相匹配
            DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            try {
                item.start_time = sdf.format(date);
                System.out.println(date.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }


//            LayoutInflater mInflater = getLayoutInflater();
//            View itemView = mInflater.inflate(R.layout.item_history, null);
//
//            SlideView slideView = new SlideView(this);
//            slideView.setContentView(itemView);


//            item.slideView = slideView;
            item.id = Integer.parseInt(data.get(Cdr.KEY_ID));

            ret.add(item);
        }

        return ret;
    }
}
