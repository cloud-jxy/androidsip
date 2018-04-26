package com.xyt.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xyt.sqlite.DBHelper;
import com.xyt.sqlite.model.Cdr;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by apple on 16/8/23.
 */
public class CdrRepo {
    private DBHelper dbHelper;

    public CdrRepo(Context context) {
        dbHelper = new DBHelper(context);
    }


    public int insert(Cdr record) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(record.KEY_peer, record.peer_url);
        values.put(record.KEY_local, record.local_url);
        values.put(record.KEY_status, record.status);
        values.put(record.KEY_start, record.start_time);
        values.put(record.KEY_conn, record.conn_time);
        values.put(record.KEY_stop, record.stop_time);

        // Inserting Row
        long id = db.insert(record.TABLE, null, values);
        db.close(); // Closing database connection
        return (int) id;
    }

    public void delete(int id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(Cdr.TABLE, Cdr.KEY_ID + "= ?", new String[] { String.valueOf(id) });
        db.close(); // Closing database connection
    }

    public void deleteAll() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.execSQL("delete from Cdr");
        db.close(); // Closing database connection
    }

    public void update(Cdr record) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(record.KEY_peer, record.peer_url);
        values.put(record.KEY_local, record.local_url);
        values.put(record.KEY_status, record.status);
        values.put(record.KEY_start, record.start_time);
        values.put(record.KEY_conn, record.conn_time);
        values.put(record.KEY_stop, record.stop_time);

        // It's a good practice to use parameter ?, instead of concatenate string
        db.update(Cdr.TABLE, values, Cdr.KEY_ID + "= ?", new String[] { String.valueOf(record.id) });
        db.close(); // Closing database connection
    }


    public ArrayList<HashMap<String, String>> getList() {
        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Cdr.KEY_ID + "," +
                Cdr.KEY_peer + "," +
                Cdr.KEY_local + "," +
                Cdr.KEY_start + "," +
                Cdr.KEY_conn + "," +
                Cdr.KEY_stop + "," +
                Cdr.KEY_status +
                " FROM " + Cdr.TABLE;

        //Student student = new Student();
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> record = new HashMap<String, String>();
                record.put(Cdr.KEY_ID, cursor.getString(cursor.getColumnIndex(Cdr.KEY_ID)));
                record.put(Cdr.KEY_local, cursor.getString(cursor.getColumnIndex(Cdr.KEY_local)));
                record.put(Cdr.KEY_peer, cursor.getString(cursor.getColumnIndex(Cdr.KEY_peer)));
                record.put(Cdr.KEY_start, cursor.getString(cursor.getColumnIndex(Cdr.KEY_start)));
                record.put(Cdr.KEY_conn, cursor.getString(cursor.getColumnIndex(Cdr.KEY_conn)));
                record.put(Cdr.KEY_stop, cursor.getString(cursor.getColumnIndex(Cdr.KEY_stop)));
                record.put(Cdr.KEY_status, cursor.getString(cursor.getColumnIndex(Cdr.KEY_status)));
                list.add(record);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public Cdr getById(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Cdr.KEY_peer + "," +
                Cdr.KEY_local + "," +
                Cdr.KEY_start + "," +
                Cdr.KEY_conn + "," +
                Cdr.KEY_stop + "," +
                Cdr.KEY_status +
                " FROM " + Cdr.TABLE
                + " WHERE " +
                Cdr.KEY_ID + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        int iCount =0;
        Cdr record = new Cdr();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {
                record.id =cursor.getInt(cursor.getColumnIndex(Cdr.KEY_ID));
                record.local_url =cursor.getString(cursor.getColumnIndex(Cdr.KEY_peer));
                record.peer_url  =cursor.getString(cursor.getColumnIndex(Cdr.KEY_local));
                record.start_time =cursor.getInt(cursor.getColumnIndex(Cdr.KEY_start));
                record.conn_time =cursor.getInt(cursor.getColumnIndex(Cdr.KEY_conn));
                record.stop_time =cursor.getInt(cursor.getColumnIndex(Cdr.KEY_stop));
                record.status =cursor.getInt(cursor.getColumnIndex(Cdr.KEY_status));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return record;
    }
}
