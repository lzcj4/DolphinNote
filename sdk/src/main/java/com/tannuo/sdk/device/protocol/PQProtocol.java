package com.tannuo.sdk.device.protocol;

import com.tannuo.sdk.device.TouchPoint;
import com.tannuo.sdk.util.DataLog;
import com.tannuo.sdk.util.DataUtil;
import com.tannuo.sdk.util.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Nick_PC on 2016/6/30.
 */
public class PQProtocol extends ProtocolBase {
    private final byte FRAME_HEADER1 = 0x55;
    private final byte FRAME_HEADER2 = 0x54;
    private final byte POINT_BYTES = 5;
    private final byte FRAME_BYTES = 10;

    private final byte ACTION_NONE = 0x0;
    private final byte ACTION_DOWN = (byte) 0x81;
    private final byte ACTION_MOVE = (byte) 0x82;
    private final byte ACTION_UP = (byte) 0x84;

    public PQProtocol() {
        TouchPoint.setActions(ACTION_DOWN, ACTION_MOVE, ACTION_UP);
    }

    @Override
    public int parse(byte[] data) {
        if (null == data || data.length == 0) {
            throw new IllegalArgumentException();
        }

        reset();
        byte[] totalData = combineBytes(lastUnhandledBytes, data);
        if (lastUnhandledBytes.length != 0) {
            lastUnhandledBytes = new byte[0];
        }
        int result = STATUS_GET_DATA;
        int len = totalData.length;
        for (int i = 0; i < len && len > 0; ) {
            int index = i;
            // For multi-frame or part-frame
            if ((len - index) < FRAME_BYTES) {
                lastUnhandledBytes = Arrays.copyOfRange(totalData, index, len);
                break;
            }

            //Check header
            if (!(totalData[index++] == FRAME_HEADER1 && totalData[index++] == FRAME_HEADER1)) {
                i++;
                continue;
            }


            TouchPoint point = new TouchPoint();
            byte action = totalData[index++];
            if (action == ACTION_NONE) {
                index += POINT_BYTES - 1;
                continue;
            }
            point.setActionByDevice(action); // byte:0
            //point.setId(totalData[index++]);// byte:1
            point.setX(DataUtil.bytesShortLittleEndian(totalData[index++], totalData[index++]));// byte:2,3
            point.setY(DataUtil.bytesShortLittleEndian(totalData[index++], totalData[index++]));// byte:4,5
//            point.setWidth(DataUtil.bytesShortLittleEndian(totalData[index++], totalData[index++]));// byte:6,7
//            point.setHeight(DataUtil.bytesShortLittleEndian(totalData[index++], totalData[index++]));// byte:8,9
            mPoints.add(point);
            index++;
            // Collections.sort(mPoints, (lhs, rhs) -> lhs.getId() - rhs.getId());
            byte checksum = totalData[index++];

            byte[] frameData = Arrays.copyOfRange(totalData, index - 10, index - 1);
            if (checkChecksum(frameData, checksum)) {
                Logger.i(TAG, "getFactory frame end ");
            }
            i = index;
        }
        return result;
    }

    private boolean checkChecksum(byte[] frameData, byte checksum) {
        byte sum = 0;
        for (byte b : frameData) {
            sum += b;
        }
        return sum == checksum;
    }

    @Override
    public List<TouchPoint> getPoints() {
        return this.mPoints;
    }

    @Override
    public byte[] getCmd() {
        return new byte[0];
    }
}
