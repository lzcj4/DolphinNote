package com.tannuo.sdk.bluetooth.device;

import android.content.Context;

import com.tannuo.sdk.bluetooth.TouchScreenListener;

/**
 * Created by nick on 2016/4/23.
 */
public interface IDeviceFactory {
    /**
     * get bluetooth connect service
     *
     * @param context
     * @param touchListener
     * @return
     */
    IDevice get(Context context, TouchScreenListener touchListener);
}
