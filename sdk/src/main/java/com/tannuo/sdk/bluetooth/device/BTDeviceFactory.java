package com.tannuo.sdk.bluetooth.device;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.tannuo.sdk.bluetooth.TouchScreenListener;

/**
 * Created by nick on 2016/4/23.
 */
public class BTDeviceFactory implements IDeviceFactory {
    @Override
    public IDevice get(Context context, TouchScreenListener touchListener) {
        IDeviceFactory factory = getFactory();
        IDevice result = factory.get(context, touchListener);
        return result;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private IDeviceFactory getFactory() {
        IDeviceFactory factory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            factory = new BLEFactory();
        } else
        {
            factory = new BLCFactory();
        }
        return factory;

    }
}
