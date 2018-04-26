package com.xyt.sqlite.model;

/**
 * Created by apple on 16/8/23.
 */
public class Cdr {
    // Labels table name
    public static final String TABLE = "Cdr";

    // Labels Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_peer = "peer_url";
    public static final String KEY_local = "local_url";
    public static final String KEY_status = "status";
    public static final String KEY_start = "start_time";
    public static final String KEY_conn = "conn_time";
    public static final String KEY_stop = "stop_time";

    public int id;
    public String peer_url;
    public String local_url;
    public int status;
    public int start_time;
    public int conn_time;
    public int stop_time;
}

