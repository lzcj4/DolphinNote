package com.tannuo.sdk.device.bluetooth;

import android.content.Context;

import com.tannuo.sdk.device.TouchDeviceListener;

/**
 * Created by nick on 2016/4/23.
 */
public interface IDeviceFactory {
    /**
     * get bluetooth connect service
     *
     * @param context
     * @param listener
     * @return
     */
    IDevice get(Context context, TouchDeviceListener listener);
}
