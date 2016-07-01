package com.tannuo.sdk.device.bluetooth;

import android.content.Context;

import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.protocol.JYProtocol;
import com.tannuo.sdk.device.protocol.JYTouchScreen;
import com.tannuo.sdk.device.protocol.IProtocol;

/**
 * Created by nick on 2016/4/23.
 */
public class BLCFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchDeviceListener listener) {
        IProtocol protocol = new JYProtocol(new JYTouchScreen(600, 2000));
        return new BLCDevice(context, listener, protocol);
    }
}
