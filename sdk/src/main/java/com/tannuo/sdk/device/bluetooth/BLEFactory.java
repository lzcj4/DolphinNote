package com.tannuo.sdk.device.bluetooth;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.tannuo.sdk.device.TouchDeviceListener;

/**
 * Created by nick on 2016/4/23.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchDeviceListener listener) {
        return new MockDevice(listener);
        //IProtocol protocol = new JYProtocol(new JYTouchScreen(8000, 20000));
        //return new BLEDevice(context, listener,protocol);
    }
}
