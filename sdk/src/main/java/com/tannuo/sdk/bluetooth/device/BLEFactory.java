package com.tannuo.sdk.bluetooth.device;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.tannuo.sdk.bluetooth.TouchScreenListener;

/**
 * Created by nick on 2016/4/23.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchScreenListener touchListener) {
        return new MockDevice(touchListener);
        // return new BLEDevice(context, touchListener);
    }
}
