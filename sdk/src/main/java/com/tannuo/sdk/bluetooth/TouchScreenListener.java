package com.tannuo.sdk.bluetooth;

import java.util.List;

/**
 * Created by nick on 2016/04/22.
 */
public interface TouchScreenListener {

    /**
     * Gesture triggered
     *
     * @param gestureNo
     */
    void onGestureGet(int gestureNo);

    /**
     * Touch up points
     *
     * @param upPoints
     */
    void onTouchUp(List<TouchScreen.TouchPoint> upPoints);

    /**
     * Touch down points
     *
     * @param downPoints
     */
    void onTouchDown(List<TouchScreen.TouchPoint> downPoints);

    /**
     * Touch move points
     *
     * @param movePoints
     */
    void onTouchMove(List<TouchScreen.TouchPoint> movePoints);

    /**
     * Snapshot trigged
     *
     * @param snapshot
     */
    void onSnapshot(int snapshot);

    /**
     * Get touch screen id
     *
     * @param touchScreenID
     */
    void onIdGet(long touchScreenID);

    /**
     * Disconnected or invalid operations
     *
     * @param errorCode
     */
    void onError(int errorCode);

    /**
     * Bluetooth device startIO
     */
    void onBLConnected();
}


