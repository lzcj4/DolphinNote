package com.tannuo.sdk.device;

import com.tannuo.sdk.device.bluetooth.BLCFactory;
import com.tannuo.sdk.device.bluetooth.BLEFactory;
import com.tannuo.sdk.device.bluetooth.IDeviceFactory;
import com.tannuo.sdk.device.usb.UsbDeviceFactory;

/**
 * Created by Nick_PC on 2016/7/13.
 */
public class DeviceFactory {
    ///
    public static final int DEVICE_BLC = 0;
    public static final int DEVICE_BLE = 1;
    public static final int DEVICE_USB = 2;

    private static class InstanceHolder {
        static DeviceFactory instance = new DeviceFactory();
    }

    public static DeviceFactory getInstance() {
        return InstanceHolder.instance;
    }

    public IDeviceFactory getDeviceFactory(int deviceType) {
        IDeviceFactory result = null;
        switch (deviceType) {
            case DEVICE_BLC:
                result = new BLCFactory();
                break;
            case DEVICE_BLE:
                result = new BLEFactory();
                break;
            case DEVICE_USB:
                result = new UsbDeviceFactory();
                break;
        }
        return result;
    }
}
