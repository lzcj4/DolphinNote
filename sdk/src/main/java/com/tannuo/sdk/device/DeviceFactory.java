package com.tannuo.sdk.device;

import com.tannuo.sdk.device.bluetooth.BTDeviceFactory;
import com.tannuo.sdk.device.bluetooth.IDeviceFactory;
import com.tannuo.sdk.device.usb.UsbDeviceFactory;

/**
 * Created by Nick_PC on 2016/7/13.
 */
public class DeviceFactory {
    public static final int DEVICE_BLUETOOTH = 0;
    public static final int DEVICE_USB = 1;

    private static class InstanceHolder {
        static DeviceFactory instance = new DeviceFactory();
    }

    public static DeviceFactory getInstance() {
        return InstanceHolder.instance;
    }

    public IDeviceFactory getDeviceFactory(int deviceType) {
        IDeviceFactory result = null;
        switch (deviceType) {
            case DEVICE_BLUETOOTH:
                result = new BTDeviceFactory();
                break;
            case DEVICE_USB:
                result = new UsbDeviceFactory();
                break;
        }
        return result;
    }
}
