package com.tannuo.sdk.device.bluetooth;

import android.content.Context;

import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.protocol.IProtocol;

/**
 * Created by nick on 2016/4/23.
 */
public interface IDeviceFactory {
    /**
     * getFactory bluetooth connect service
     *
     * @param context
     * @param listener
     * @param protocol
     * @param vendorId
     * @return
     */
    IDevice get(Context context, TouchDeviceListener listener, IProtocol protocol, int vendorId);
}
