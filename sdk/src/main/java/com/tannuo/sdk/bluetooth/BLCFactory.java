package com.tannuo.sdk.bluetooth;

import android.content.Context;

import com.tannuo.sdk.bluetooth.blc.BLCConnectService;

/**
 * Created by nick on 2016/4/23.
 */
public class BLCFactory implements ServiceFactory {
    @Override
    public ConnectService get(Context context, TouchScreenListener touchListener) {
        return new BLCConnectService(context, touchListener);
    }
}
