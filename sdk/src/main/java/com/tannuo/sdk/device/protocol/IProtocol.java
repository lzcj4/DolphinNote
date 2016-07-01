package com.tannuo.sdk.device.protocol;


import com.tannuo.sdk.device.TouchPoint;

import java.util.List;

/**
 * Created by nick on 2016/4/23.
 */
public interface IProtocol {
    int parse(byte[] data);

    List<TouchPoint> getPoints();

    byte[] getCmd();
}
