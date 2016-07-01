package com.tannuo.sdk.device;

import android.util.Log;

public class TouchDeviceImpl implements TouchDeviceListener {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onTouchEvent(TouchEvent touchEvent) {
        Log.v(TAG, "onTouchEvent ");
    }

    @Override
    public void onConnected() {
        Log.v(TAG, "on device Connected ");
    }

    @Override
    public void onDisconnected() {
        Log.v(TAG, "on device disconnected ");
    }


    @Override
    public void onError(int errorCode) {
        Log.v(TAG, "onError " + errorCode);
    }

}
