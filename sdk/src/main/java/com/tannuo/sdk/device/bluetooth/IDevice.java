package com.tannuo.sdk.device.bluetooth;

/**
 * Created by nick on 2016/4/23.
 */
public interface IDevice {
    int connect(String deviceName);

    int disconnect();

    void write(byte[] data);
}
