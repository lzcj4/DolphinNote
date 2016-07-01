package com.tannuo.sdk.device.protocol;

import com.tannuo.sdk.device.TouchPoint;

import java.util.List;

/**
 * Created by Nick_PC on 2016/6/30.
 */
public class CVTProtocol implements IProtocol {
    private final byte HEADER_1 = 0x1F;
    private final byte HEADER_2 = (byte) 0xF7;
    private final byte FRAME_LEN = 0x2B;
    private final byte HEADER_RESERVED = 0x02;

    @Override
    public int parse(byte[] data) {
        return 0;
    }


    @Override
    public List<TouchPoint> getPoints() {
        return null;
    }

    @Override
    public byte[] getCmd() {
        return new byte[0];
    }
}
