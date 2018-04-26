package com.xyt.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xyt.sqlite.DBHelper;
import com.xyt.sqlite.model.Cdr;
import com.xyt.sqlite.model.Contacts;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by apple on 16/9/7.
 */
public class ContactsRepo {
    private DBHelper dbHelper;

    public ContactsRepo(Context context) {
        dbHelper = new DBHelper(context);
    }

    public int insert(Contacts record) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(record.KEY_name, record.name);
        values.put(record.KEY_number, record.number);
        values.put(record.KEY_domain, record.domain);

        // Inserting Row
        long id = db.insert(Contacts.TABLE, null, values);
        db.close(); // Closing database connection
        return (int) id;
    }


    public void delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(Contacts.TABLE, Contacts.KEY_ID + "= ?", new String[] { String.valueOf(id) });
        db.close(); // Closing database connection
    }


    public void update(Contacts record) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(record.KEY_name, record.name);
        values.put(record.KEY_number, record.number);
        values.put(record.KEY_domain, record.domain);

        // It's a good practice to use parameter ?, instead of concatenate string
        db.update(Contacts.TABLE, values, Contacts.KEY_ID + "= ?", new String[] { String.valueOf(record.id) });
        db.close(); // Closing database connection
    }


    public ArrayList<HashMap<String, String>> getList() {
        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
///         String selectQuery =  "SELECT  " +
//                Contacts.KEY_ID + "," +
//                Contacts.KEY_name + "," +
//                Contacts.KEY_number + "," +
//                Contacts.KEY_domain +
//                " FROM " + Contacts.TABLE;

        String selectQuery =  "SELECT * FROM " + Contacts.TABLE;

        //Student student = new Student();
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> record = new HashMap<String, String>();
                record.put(Contacts.KEY_ID, cursor.getString(cursor.getColumnIndex(Contacts.KEY_ID)));
                record.put(Contacts.KEY_name, cursor.getString(cursor.getColumnIndex(Contacts.KEY_name)));
                record.put(Contacts.KEY_number, cursor.getString(cursor.getColumnIndex(Contacts.KEY_number)));
                record.put(Contacts.KEY_domain, cursor.getString(cursor.getColumnIndex(Contacts.KEY_domain)));
                list.add(record);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }


    public Contacts getById(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Contacts.KEY_name + "," +
                Contacts.KEY_number + "," +
                Contacts.KEY_domain +
                " FROM " + Contacts.TABLE
                + " WHERE " +
                Contacts.KEY_ID + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        int iCount =0;
        Contacts record = new Contacts();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {
                record.id =cursor.getInt(cursor.getColumnIndex(Contacts.KEY_ID));
                record.name =cursor.getString(cursor.getColumnIndex(Contacts.KEY_name));
                record.number  =cursor.getString(cursor.getColumnIndex(Contacts.KEY_number));
                record.domain =cursor.getString(cursor.getColumnIndex(Contacts.KEY_domain));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return record;
    }
}
