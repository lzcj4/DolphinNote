package com.tannuo.sdk.device.usb;

import android.content.Context;

import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.bluetooth.IDevice;
import com.tannuo.sdk.device.bluetooth.IDeviceFactory;
import com.tannuo.sdk.device.protocol.IProtocol;

/**
 * Created by Nick_PC on 2016/7/13.
 */
public class UsbDeviceFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchDeviceListener listener, IProtocol protocol, int vendorId) {
//        switch (vendorId) {
//            case DeviceFactory.VENDOR_Mock:
//                return new MockDevice(listener, protocol);
//            default:
//                return new BLEDevice(context, listener, protocol);
//        }
        switch (vendorId) {
            default:
                return new UsbDevice(context, listener, protocol);
        }
    }
}
