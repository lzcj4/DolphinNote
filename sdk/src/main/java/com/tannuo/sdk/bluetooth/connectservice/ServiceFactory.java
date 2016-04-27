package com.tannuo.sdk.bluetooth.connectservice;

import android.content.Context;

import com.tannuo.sdk.bluetooth.TouchScreenListener;

/**
 * Created by nick on 2016/4/23.
 */
public interface ServiceFactory {
    /**
     * get bluetooth connect service
     *
     * @param context
     * @param touchListener
     * @return
     */
    ConnectService get(Context context, TouchScreenListener touchListener);
}
