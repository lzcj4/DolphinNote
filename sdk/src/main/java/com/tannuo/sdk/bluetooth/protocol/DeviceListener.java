package com.tannuo.sdk.bluetooth.protocol;

/**
 * Created by Nick_PC on 2016/6/20.
 */
public interface DeviceListener {
    int ERROR_FAILED = 0;

    /**
     * Disconnected or invalid operations
     *
     * @param errorCode
     */
    void onError(int errorCode);

    /**
     * Bluetooth device connected
     */
    void onConnected();

    /**
     * Bluetooth device disconnected
     */
    void onDisconnected();
}
