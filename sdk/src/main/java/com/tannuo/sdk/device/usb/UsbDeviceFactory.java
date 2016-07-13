package com.tannuo.sdk.device.usb;

import android.content.Context;

import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.bluetooth.IDevice;
import com.tannuo.sdk.device.bluetooth.IDeviceFactory;
import com.tannuo.sdk.device.protocol.CVTUsbProtocol;

/**
 * Created by Nick_PC on 2016/7/13.
 */
public class UsbDeviceFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchDeviceListener listener) {
        UsbDevice device = new UsbDevice(context, listener, new CVTUsbProtocol());
        return device;
    }
}
