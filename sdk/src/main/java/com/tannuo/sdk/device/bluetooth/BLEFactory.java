package com.tannuo.sdk.device.bluetooth;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.protocol.IProtocol;

/**
 * Created by nick on 2016/4/23.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchDeviceListener listener, IProtocol protocol) {
        return new BLEDevice(context, listener, protocol);
    }
}
