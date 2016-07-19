package com.tannuo.sdk.device.bluetooth;

import android.content.Context;

import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.protocol.IProtocol;

/**
 * Created by Nick_PC on 2016/7/19.
 * 馒头科技
 */
public class MTBLEDevice extends BLEDevice {
    private static final String UART_UUID = "0000f1f0-0000-1000-8000-00805f9b34fb";

    public MTBLEDevice(Context context, TouchDeviceListener listener, IProtocol protocol) {
        super(context, listener, protocol);
        mUART_Uuid = UART_UUID;
    }
}