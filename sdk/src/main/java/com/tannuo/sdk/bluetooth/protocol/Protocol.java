package com.tannuo.sdk.bluetooth.protocol;


import com.tannuo.sdk.bluetooth.TouchScreen;

/**
 * Created by nick on 2016/4/23.
 */
public interface Protocol {
    int parse(byte[] data);

    TouchScreen getTouchScreen();

    byte[] changeDataFeature();
}
