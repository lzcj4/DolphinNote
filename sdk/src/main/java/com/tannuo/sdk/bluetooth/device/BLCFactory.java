package com.tannuo.sdk.bluetooth.device;

import android.content.Context;

import com.tannuo.sdk.bluetooth.TouchScreenListener;

/**
 * Created by nick on 2016/4/23.
 */
public class BLCFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchScreenListener touchListener) {
        return new BLCDevice(context, touchListener);
    }
}
