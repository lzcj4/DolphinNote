package com.tannuo.sdk.device.bluetooth;

import android.content.Context;

import com.tannuo.sdk.device.DeviceFactory;
import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.protocol.IProtocol;

/**
 * Created by nick on 2016/4/23.
 */
public class BLCFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchDeviceListener listener, IProtocol protocol, int vendorId) {
        switch (vendorId) {
            case DeviceFactory.VENDOR_Mock:
                return new MockDevice(listener, protocol);
            default:
                return new BLCDevice(context, listener, protocol);
        }
    }
}
