package com.tannuo.sdk.bluetooth.connectservice;

/**
 * Created by nick on 2016/4/23.
 */
public interface ConnectService {
    int connect(String deviceName);

    int disconnect();

    void write(byte[] data);
}
