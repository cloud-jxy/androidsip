package com.xyt.sipphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xyt.sipphone.R;
import com.xyt.sipphone.adapter.AccountAdapter;
import com.xyt.sipphone.adapter.ContactsAdapter;
import com.xyt.sipphone.adapter.SortAdapter;
import com.xyt.sipphone.model.CharacterParser;
import com.xyt.sipphone.model.PinyinComparator;
import com.xyt.sipphone.model.SortModel;
import com.xyt.sipphone.unit.ClearEditText;
import com.xyt.sipphone.unit.SideBar;
import com.xyt.sqlite.dao.ContactsRepo;
import com.xyt.sqlite.model.Contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ContactsActivity extends Activity {

    private ListView sortListView;
    private SideBar sideBar;
    /**
     * 显示字母的TextView
     */
    private TextView dialog;
    private ContactsAdapter adapter;
    private ClearEditText mClearEditText;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        initTitleBar();
    }

    private void initTitleBar() {
        TextView tv = (TextView)findViewById(R.id.text_title);
        tv.setText("联系人");

        Button btn = (Button)findViewById(R.id.button_backward);
        btn.setVisibility(View.INVISIBLE);

        btn = (Button)findViewById(R.id.button_forward);
        btn.setText("+");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddContactsActivity.class);
                intent.putExtra("operate", "add");  //0,add; 1,edit.
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        initViews();
    }

    private void initViews() {
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        pinyinComparator = new PinyinComparator();

        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);

        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if(position != -1){
                    sortListView.setSelection(position);
                }

            }
        });

        sortListView = (ListView) findViewById(R.id.country_lvcountry);
        sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//                LinearLayout layout = (LinearLayout) view.findViewById(R.id.item_contacts_operation);
//                layout.setVisibility(View.VISIBLE);
                //这里要利用adapter.getItem(position)来获取当前position所对应的对象
                Toast.makeText(getApplication(), ((SortModel)adapter.getItem(position)).getName(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), ContactsDetailActivity.class);

                SortModel model = (SortModel) adapter.getItem(position);
                intent.putExtra("name", model.getName());
                intent.putExtra("number", model.getNumber());
                intent.putExtra("domain", model.getDomain());
                intent.putExtra("keyId", model.getKeyId());

                startActivity(intent);
            }
        });

        ContactsRepo repo = new ContactsRepo(this);
        SourceDateList = filledData(repo.getList());

        // 根据a-z进行排序源数据
        Collections.sort(SourceDateList, pinyinComparator);
        adapter = new ContactsAdapter(this, SourceDateList);
        sortListView.setAdapter(adapter);


        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);

        //根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    /**
     * 为ListView填充数据
     * @param date
     * @return
     */
    private List<SortModel> filledData(ArrayList<HashMap<String, String>> date) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (HashMap<String, String> item : date) {
            SortModel sortModel = new SortModel();

            sortModel.setName(item.get(Contacts.KEY_name));
            sortModel.setNumber(item.get(Contacts.KEY_number));
            sortModel.setDomain(item.get(Contacts.KEY_domain));
            sortModel.setKeyId(item.get(Contacts.KEY_ID));

            //汉字转换成拼音
            String pinyin = characterParser.getSelling(sortModel.getName());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if(sortString.matches("[A-Z]")){
                sortModel.setSortLetters(sortString.toUpperCase());
            }else{
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }

        return mSortList;
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = SourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : SourceDateList) {
                String name = sortModel.getName();
                if (name.toUpperCase().indexOf(
                        filterStr.toString().toUpperCase()) != -1
                        || characterParser.getSelling(name).toUpperCase()
                        .startsWith(filterStr.toString().toUpperCase())) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
    }
}
