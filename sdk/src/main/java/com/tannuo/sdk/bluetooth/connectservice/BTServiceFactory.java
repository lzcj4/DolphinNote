package com.tannuo.sdk.bluetooth.connectservice;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.tannuo.sdk.bluetooth.TouchScreenListener;

/**
 * Created by nick on 2016/4/23.
 */
public class BTServiceFactory implements ServiceFactory {
    @Override
    public ConnectService get(Context context, TouchScreenListener touchListener) {
        ServiceFactory factory = getFactory();
        ConnectService result = factory.get(context, touchListener);
        return result;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private ServiceFactory getFactory() {
        ServiceFactory factory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            factory = new BLEFactory();
        } else
        {
            factory = new BLCFactory();
        }
        return factory;

    }
}
