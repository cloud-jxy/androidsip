package com.xyt.sqlite;

/**
 * Created by apple on 16/8/23.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xyt.sqlite.model.Account;
import com.xyt.sqlite.model.Cdr;
import com.xyt.sqlite.model.Contacts;

public class DBHelper  extends SQLiteOpenHelper {
    //version number to upgrade database version
    //each time if you Add, Edit table, you need to change the
    //version number.
    private static final int DATABASE_VERSION = 4;

    // Database Name
    private static final String DATABASE_NAME = "sipphone.db";

    public DBHelper(Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here
        db.execSQL("DROP TABLE IF EXISTS " + Cdr.TABLE);
        String CREATE_TABLE_CDR = "CREATE TABLE " + Cdr.TABLE  + "("
                + Cdr.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Cdr.KEY_peer + " TEXT, "
                + Cdr.KEY_local + " TEXT, "
                + Cdr.KEY_status + " INTEGER, "
                + Cdr.KEY_start + " INTEGER, "
                + Cdr.KEY_conn + " INTEGER, "
                + Cdr.KEY_stop + " INTEGER )";

        db.execSQL(CREATE_TABLE_CDR);

        db.execSQL("DROP TABLE IF EXISTS " + Account.TABLE);
        String CREATE_TABLE_ACCOUNT = "CREATE TABLE " + Account.TABLE + "("
                + Account.KEY_id + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Account.KEY_user + " TEXT, "
                + Account.KEY_password + " TEXT, "
                + Account.KEY_auth_user + " TEXT, "
                + Account.KEY_sip_server + " TEXT, "
                + Account.KEY_regint + " INTEGER)";

        db.execSQL(CREATE_TABLE_ACCOUNT);

        db.execSQL("DROP TABLE IF EXISTS " + Contacts.TABLE);
        String CREATE_TABLE_CONTACTS = "CREATE TABLE " + Contacts.TABLE + "("
                + Contacts.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Contacts.KEY_name + " TEXT, "
                + Contacts.KEY_number + " TEXT, "
                + Contacts.KEY_domain + " TEXT)";
        db.execSQL(CREATE_TABLE_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed, all data will be gone!!!
//        db.execSQL("DROP TABLE IF EXISTS " + Cdr.TABLE);

        // Create tables again
        onCreate(db);
    }
}