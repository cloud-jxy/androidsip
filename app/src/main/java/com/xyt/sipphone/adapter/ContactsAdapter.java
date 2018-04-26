package com.xyt.sipphone.adapter;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.xyt.sipphone.model.SortModel;

import java.util.List;

/**
 * Created by apple on 16/9/5.
 */
public class ContactsAdapter extends SortAdapter {

    public ContactsAdapter(Context mContext, List<SortModel> list) {
        super(mContext, list);
    }

//    public void changeImageVisable(View view,int position) {
//
//    }
}
