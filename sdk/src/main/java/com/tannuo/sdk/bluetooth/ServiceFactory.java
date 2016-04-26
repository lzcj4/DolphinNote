package com.tannuo.sdk.bluetooth;

import android.content.Context;

/**
 * Created by nick on 2016/4/23.
 */
public interface ServiceFactory {
    /**
     * get bluetooth connect service
     * @param context
     * @param touchListener
     * @return
     */
    ConnectService get(Context context, TouchScreenListener touchListener);
}
