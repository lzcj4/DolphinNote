package com.tannuo.note.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Nick_PC on 2016/7/18.
 */
public class SettingPref {
    private final String PREF_CONNECTION = "pref_connection";
    private final String PREF_PROTOCOL = "pref_protocol";
    private Context mContext;

    private static class InstanceHolder {
        private static final SettingPref instance = new SettingPref();
    }

    public static SettingPref getInstance() {
        return InstanceHolder.instance;
    }

    public void initial(Context context) {
        mContext = context;
    }

    private SharedPreferences getPref() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref;
    }

    public int getConnection() {
        return getInt(PREF_CONNECTION);
    }

    public void setConnection(int value) {
        setInt(PREF_CONNECTION, value);
    }

    public int getProtocol() {
        return getInt(PREF_PROTOCOL);
    }

    public void setProtocol(int value) {
        setInt(PREF_PROTOCOL, value);

    }

    private int getInt(String key) {
        String strValue = getPref().getString(key, "0");
        return Integer.parseInt(strValue);
    }

    private void setInt(String key, int value) {
        getPref().edit().putString(key, String.valueOf(value)).apply();
    }

}
