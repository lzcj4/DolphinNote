package com.tannuo.sdk.bluetooth;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.tannuo.sdk.bluetooth.ble.BLEConnectService;

/**
 * Created by nick on 2016/4/23.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEFactory implements ServiceFactory {
    @Override
    public ConnectService get(Context context, TouchScreenListener touchListener) {
        return new BLEConnectService(context, touchListener);
    }
}
