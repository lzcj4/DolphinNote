package com.tannuo.note;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.tencent.bugly.Bugly;

/**
 * Created by Nick_PC on 2016/6/2.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        Stetho.initializeWithDefaults(this);
        Bugly.init(getApplicationContext(), "900036739", false);
        super.onCreate();
    }
}
