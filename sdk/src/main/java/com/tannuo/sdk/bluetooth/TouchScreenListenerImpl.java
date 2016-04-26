package com.tannuo.sdk.bluetooth;

import android.util.Log;

import java.util.List;

public class TouchScreenListenerImpl implements TouchScreenListener {
    private final String TAG = "TouchManager";

    @Override
    public void onSnapshot(int snapshot) {
        Log.v(TAG, "snapshot " + snapshot);
    }

    @Override
    public void onError(int errorCode) {

        Log.v(TAG, "onError " + errorCode);
    }

    @Override
    public void onBLConnected() {
        Log.v(TAG, "Conn ");
    }

    @Override
    public void onGestureGet(int gestureNo) {
        Log.v(TAG, "gesture " + gestureNo);
    }

    @Override
    public void onTouchUp(List<TouchScreen.TouchPoint> upPoints) {
        for (TouchScreen.TouchPoint item : upPoints) {
            Log.v(TAG, "onTouchUp  " + item.pointId + ": " + item.pointColor);
        }
    }

    @Override
    public void onTouchDown(List<TouchScreen.TouchPoint> downPoints) {
        for (TouchScreen.TouchPoint item : downPoints) {
            Log.v(TAG, "onTouchDown  " + item.pointId + ":  " + item.pointStatus + " " + item.pointY);
        }
    }

    @Override
    public void onIdGet(long id) {
        Log.v(TAG, "Id" + id);
    }


    @Override
    public void onTouchMove(List<TouchScreen.TouchPoint> movePoints) {
        for (TouchScreen.TouchPoint item : movePoints) {
            Log.v(TAG, "onTouchMove  " + item.pointId + ":  " + item.pointStatus + " " + item.pointY);
        }
    }
}
