package com.tannuo.sdk.device.protocol;

import com.tannuo.sdk.device.TouchPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick_PC on 2016/7/1.
 */
public abstract class ProtocolBase implements IProtocol {
    protected final String TAG = this.getClass().getSimpleName();
    public static final int STATUS_CHANGE_DATA_FEATURE = 1;
    public static final int STATUS_GET_DATA = 2;
    public static final int STATUS_GET_GESTURE = 3;
    public static final int STATUS_GET_SNAPSHOT = 4;
    public static final int STATUS_GET_IDENTI = 5;
    public static final int STATUS_GET_SCREEN_FEATURE = 6;

    protected static final int ERROR_NONE = 0;
    protected static final int ERROR_HEADER = -1;
    protected static final int ERROR_DATA = -2;
    protected static final int ERROR_DATA_LENGTH = -3;
    protected static final int ERROR_DATA_FEATURE = -4;
    protected static final int ERROR_CHECKSUM = -5;

    public List<TouchPoint> mPoints = new ArrayList<>();

    protected byte[] lastUnhandledBytes = new byte[0];

    protected byte[] combineBytes(byte[] data1, byte[] data2) {
        byte[] result = new byte[0];
        if (null == data1 && null == data2) {
            return result;
        }
        if (null == data2) {
            result = data1;
        }

        if (null == data1) {
            result = data2;
        }

        if (null != data1 && null != data2) {
            result = new byte[data1.length + data2.length];
            System.arraycopy(data1, 0, result, 0, data1.length);
            System.arraycopy(data2, 0, result, data1.length, data2.length);
        }

        return result;
    }

    protected void reset() {
        mPoints.clear();
    }
}
