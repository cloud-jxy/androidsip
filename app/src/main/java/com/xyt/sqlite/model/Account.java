package com.xyt.sqlite.model;

/**
 * Created by apple on 16/8/25.
 */
public class Account {
    private int DEFAULT_REGINT = 3600;
    public String user;
    public String password;
    public String auth_user;
    public String sip_server;
    public int regint = DEFAULT_REGINT;

    public int id;
    public static final String TABLE = "Accout";
    public static final String KEY_id = "id";
    public static final String KEY_user = "user";
    public static final String KEY_password = "password";
    public static final String KEY_auth_user = "auth_user";
    public static final String KEY_sip_server = "sip_server";
    public static final String KEY_regint = "regint";


    public String get_aor() {
        return user+'@'+sip_server;
    }

    public String get_reg_text() {
        String ret;

        ret = "<";
        ret += user + ':' + password + '@' + sip_server;
        ret += ">";

        if (regint != DEFAULT_REGINT) {
            ret += "regint=" + regint;
        }

        if (auth_user.isEmpty()) {
            ret += "auth_user=" + auth_user;
        }

        return ret;
    }
}
