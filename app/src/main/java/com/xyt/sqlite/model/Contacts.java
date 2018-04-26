package com.xyt.sqlite.model;

/**
 * Created by apple on 16/9/7.
 */
public class Contacts {
    // Labels table name
    public static final String TABLE = "contacts";

    public static final String KEY_ID = "id";
    public static final String KEY_name = "name";
    public static final String KEY_number = "number";
    public static final String KEY_domain = "domain";

    public int id;
    public String name;
    public String number;
    public String domain;
}
