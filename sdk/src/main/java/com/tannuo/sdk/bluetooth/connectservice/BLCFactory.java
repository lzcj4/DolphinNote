package com.tannuo.sdk.bluetooth.connectservice;

import android.content.Context;

import com.tannuo.sdk.bluetooth.TouchScreenListener;

/**
 * Created by nick on 2016/4/23.
 */
public class BLCFactory implements ServiceFactory {
    @Override
    public ConnectService get(Context context, TouchScreenListener touchListener) {
        return new BLCConnectService(context, touchListener);
    }
}
