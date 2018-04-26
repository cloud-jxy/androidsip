package com.xyt.sipphone.adapter;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xyt.sipphone.R;
import com.xyt.sipphone.activity.HistoryActivity;
import com.xyt.sipphone.model.HistoryItem;
import com.xyt.sipphone.unit.SlideView;
import com.xyt.sqlite.dao.CdrRepo;

import java.util.List;

/**
 * Created by apple on 16/9/5.
 */
public class HistoryAdapter extends BaseAdapter {

    private SlideView mLastSlideViewWithStatusOn;
    public List<HistoryItem> mMessageItems;
    protected Context mContext;


    public HistoryAdapter(Context mContext, List<HistoryItem> mMessageItems) {
        super();
        this.mMessageItems = mMessageItems;
        this.mContext = mContext;
    }



    @Override
    public int getCount() {
        return mMessageItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


//        HistoryItem item = mMessageItems.get(position);
//        SlideView slideView = item.slideView;
//        item.slideView = slideView;
//        item.slideView.shrink();

        HistoryItem item = mMessageItems.get(position);

        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_history, null);

        ((TextView)convertView.findViewById(R.id.history_name)).setText(item.name);
        ((TextView)convertView.findViewById(R.id.history_duration)).setText(item.duration);
        ((TextView)convertView.findViewById(R.id.history_time)).setText(item.start_time);
        ((Button)convertView.findViewById(R.id.item_history_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("test run!!");

                int pos = HistoryActivity.sortListView.getPositionForView(view);
                String text = "I 'm on " + pos;
                Log.e("test", text);

                HistoryItem item = (HistoryItem)getItem(pos);

                CdrRepo repo = new CdrRepo(mContext);
                repo.delete(item.id);
                mMessageItems.remove(pos);
                notifyDataSetChanged();
            }
        });

//        slideView.setOnSlideListener(new SlideView.OnSlideListener() {
//            @Override
//            public void onSlide(View view, int status) {
//                if (mLastSlideViewWithStatusOn != null && mLastSlideViewWithStatusOn != view) {
//                    mLastSlideViewWithStatusOn.shrink();
//                }
//
//                if (status == SLIDE_STATUS_ON) {
//                    mLastSlideViewWithStatusOn = (SlideView) view;
//                }
//            }
//        });

        return convertView;
    }
}
