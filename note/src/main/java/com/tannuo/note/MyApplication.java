package com.tannuo.note;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Nick_PC on 2016/6/2.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        Stetho.initializeWithDefaults(this);
        super.onCreate();
    }
}
