package com.tannuo.sdk.device.bluetooth;

import android.content.Context;

import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.protocol.IProtocol;

/**
 * Created by Nick_PC on 2016/7/19.
 * 中易腾达
 */
public class ZYTDBLEDevice extends BLEDevice {
    private static final String UART_UUID = "0000e455-0000-1000-8000-00805f9b34fb";

    public ZYTDBLEDevice(Context context, TouchDeviceListener listener, IProtocol protocol) {
        super(context, listener, protocol);
        mUART_Uuid = UART_UUID;
    }
}