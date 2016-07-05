package com.tannuo.sdk.device.bluetooth;

import android.content.Context;

import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.protocol.BTProtocolFactory;
import com.tannuo.sdk.device.protocol.IProtocol;
import com.tannuo.sdk.device.protocol.ProtocolType;

/**
 * Created by nick on 2016/4/23.
 */
public class BLCFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchDeviceListener listener) {
        // return new MockDevice(listener);

        IProtocol protocol = BTProtocolFactory.getInstance().get(ProtocolType.JY);
        // IProtocol protocol = BTProtocolFactory.getInstance().get(ProtocolType.CVT);
        return new BLCDevice(context, listener, protocol);
    }
}
