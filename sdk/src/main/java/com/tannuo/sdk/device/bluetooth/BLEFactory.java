package com.tannuo.sdk.device.bluetooth;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.tannuo.sdk.device.DeviceFactory;
import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.protocol.IProtocol;

/**
 * Created by nick on 2016/4/23.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchDeviceListener listener, IProtocol protocol, int vendorId) {
        switch (vendorId) {
            case DeviceFactory.VENDOR_Mock:
                return new MockDevice(listener, protocol);
            case DeviceFactory.VENDOR_FYT:
                return new FYTBLEDevice(context, listener, protocol);
            case DeviceFactory.VENDOR_SHSD:
                return new SHSDBLEDevice(context, listener, protocol);
            case DeviceFactory.VENDOR_MT:
                return new MTBLEDevice(context, listener, protocol);
            case DeviceFactory.VENDOR_BT:
                return new BTBLEDevice(context, listener, protocol);
            case DeviceFactory.VENDOR_ZYTD:
                return new ZYTDBLEDevice(context, listener, protocol);
            default:
                return new BLEDevice(context, listener, protocol);
        }
    }
}
