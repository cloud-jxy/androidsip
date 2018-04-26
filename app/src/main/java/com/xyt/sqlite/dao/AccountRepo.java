package com.xyt.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xyt.sqlite.DBHelper;
import com.xyt.sqlite.model.Account;
import com.xyt.sqlite.model.Cdr;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by apple on 16/9/1.
 */
public class AccountRepo {
    private DBHelper dbHelper;

    public AccountRepo(Context context) {
        dbHelper = new DBHelper(context);
    }

    public int insert(Account record) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(record.KEY_user, record.user);
        values.put(record.KEY_password, record.password);
        values.put(record.KEY_auth_user, record.auth_user);
        values.put(record.KEY_sip_server, record.sip_server);

        // Inserting Row
        long id = db.insert(record.TABLE, null, values);
        db.close(); // Closing database connection
        return (int) id;
    }

    public void delete(int id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(Cdr.TABLE, Account.KEY_id + "= ?", new String[] { String.valueOf(id) });
        db.close(); // Closing database connection
    }

    public void update(Account record) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(record.KEY_user, record.user);
        values.put(record.KEY_password, record.password);
        values.put(record.KEY_auth_user, record.auth_user);
        values.put(record.KEY_sip_server, record.sip_server);

        // It's a good practice to use parameter ?, instead of concatenate string
        db.update(Account.TABLE, values, Account.KEY_id + "= ?", new String[] { String.valueOf(record.id) });
        db.close(); // Closing database connection
    }


    public ArrayList<HashMap<String, String>> getList() {
        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Account.KEY_user + "," +
                Account.KEY_password + "," +
                Account.KEY_auth_user + "," +
                Account.KEY_sip_server + "," +
                Account.KEY_regint +
                " FROM " + Account.TABLE
                + " WHERE " +
                Account.KEY_id + "=?";

        //Student student = new Student();
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> record = new HashMap<String, String>();
                record.put(Account.KEY_user, cursor.getString(cursor.getColumnIndex(Account.KEY_user)));
                record.put(Account.KEY_password, cursor.getString(cursor.getColumnIndex(Account.KEY_password)));
                record.put(Account.KEY_auth_user, cursor.getString(cursor.getColumnIndex(Account.KEY_auth_user)));
                record.put(Account.KEY_sip_server, cursor.getString(cursor.getColumnIndex(Account.KEY_sip_server)));
                record.put(Account.KEY_regint, cursor.getString(cursor.getColumnIndex(Account.KEY_regint)));

                list.add(record);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }


    public Account getById(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Account.KEY_user + "," +
                Account.KEY_password + "," +
                Account.KEY_auth_user + "," +
                Account.KEY_sip_server + "," +
                Account.KEY_regint +
                " FROM " + Account.TABLE
                + " WHERE " +
                Account.KEY_id + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        int iCount =0;
        Account record = new Account();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {
                record.id =cursor.getInt(cursor.getColumnIndex(Cdr.KEY_ID));
                record.user =cursor.getString(cursor.getColumnIndex(Account.KEY_user));
                record.password  =cursor.getString(cursor.getColumnIndex(Account.KEY_password));
                record.auth_user =cursor.getString(cursor.getColumnIndex(Account.KEY_auth_user));
                record.sip_server =cursor.getString(cursor.getColumnIndex(Account.KEY_sip_server));
                record.regint =cursor.getInt(cursor.getColumnIndex(Account.KEY_regint));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return record;
    }
}
