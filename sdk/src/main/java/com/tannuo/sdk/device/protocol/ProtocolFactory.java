package com.tannuo.sdk.device.protocol;

import com.tannuo.sdk.device.DeviceFactory;

/**
 * Created by Nick_PC on 2016/7/18.
 */
public class ProtocolFactory {

    private static class InstanceHolder {
        private static final ProtocolFactory instance = new ProtocolFactory();
    }

    public static ProtocolFactory getInstance() {
        return InstanceHolder.instance;
    }

    public IProtocolFactory getFactory(int connType) {
        IProtocolFactory result = null;
        switch (connType) {
            case DeviceFactory.DEVICE_BLC:
            case DeviceFactory.DEVICE_BLE:
                result = new BTProtocolFactory();
                break;
            case DeviceFactory.DEVICE_USB:
                result = new UsbProtocolFactory();
                break;
        }
        return result;
    }

}
