package com.tannuo.sdk.device.protocol;

import com.tannuo.sdk.device.TouchPoint;
import com.tannuo.sdk.util.DataLog;
import com.tannuo.sdk.util.DataUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Nick_PC on 2016/6/30.
 */
public class CVTProtocol extends ProtocolBase {
    private final byte HEADER_1 = 0x1F;
    private final byte HEADER_2 = (byte) 0xF7;
    private final byte FRAME_LEN = 0x2B;
    private final byte HEADER_RESERVED = 0x02;
    private final byte FRAME_BYTES = 43;
    private final byte FRAME_POINTS = 6;
    private final byte POINT_BYTES = 10;

    private final byte ACTION_DOWN = 0x02;
    private final byte ACTION_MOVE = 0x03;
    private final byte ACTION_UP = 0x02;
    private final byte ACTION_NONE = 0x00;

    public CVTProtocol() {
        TouchPoint.setActions(ACTION_DOWN, ACTION_MOVE, ACTION_UP);
    }

    TouchPoint lastPoint = null;

    @Override
    public int parse(byte[] data) {
        if (null == data || data.length == 0) {
            throw new IllegalArgumentException();
        }

        DataLog.getInstance().writeInData(data);
        DataLog.getInstance().writeInLineData(data);
        reset();
//        1F F7 2B 00 02
//        02 00 80 0F 26 72 00 01 00 00 00 00 00 02 00 00 00 00 00 03 00 00 00 00 00
//        04 00 00 00 00 00 05 00 00 00 00 01 7C
        byte[] totalData = combineBytes(lastUnhandledBytes, data);
        if (lastUnhandledBytes.length != 0) {
            lastUnhandledBytes = new byte[0];
        }
        int result = STATUS_GET_DATA;
        int len = totalData.length;
        for (int i = 0; i < len && len > 0; ) {
            int index = i;
            if ((len - index) < FRAME_BYTES) {
                lastUnhandledBytes = Arrays.copyOfRange(totalData, index, len);
                break;
            }

            //Check header
            if (!(totalData[index++] == HEADER_1 &&
                    totalData[index++] == HEADER_2 &&
                    totalData[index++] == FRAME_LEN &&
                    totalData[index++] == 0x00 &&
                    totalData[index++] == HEADER_RESERVED)) {
                i++;
                continue;
            }

            for (int j = 0; j < FRAME_POINTS && index < len; j++) {
                TouchPoint point = new TouchPoint();
                byte action = totalData[index++];
                if (action == ACTION_NONE) {
                    index += POINT_BYTES - 1;
                    continue;
                }
                point.setActionByDevice(action); // byte:0
                point.setId(totalData[index++]);// byte:1
                point.setX(DataUtil.bytesToIntLittleEndian(totalData[index++], totalData[index++]));// byte:2,3
                point.setY(DataUtil.bytesToIntLittleEndian(totalData[index++], totalData[index++]));// byte:4,5
                point.setWidth(DataUtil.bytesToIntLittleEndian(totalData[index++], totalData[index++]));// byte:6,7
                point.setHeight(DataUtil.bytesToIntLittleEndian(totalData[index++], totalData[index++]));// byte:8,9

                mPoints.add(point);
                if (lastPoint != null && lastPoint.getIsMove() &&
                        action == ACTION_UP) {
                    point.setAction(TouchPoint.ACTION_UP);
                }
                lastPoint = point;
            }
            Collections.sort(mPoints, (lhs, rhs) -> lhs.getId() - rhs.getId());
            if (index < len) {
                int pointLen = totalData[index++];
                byte checkSum = totalData[index++];
                if (!checkSum(totalData, i)) {
                    mPoints.clear();
                }
            }
            i = index;
        }
        return result;
    }

    private boolean checkSum(byte[] data, int index) {
        int len = data.length;
        if (index < 0 || index >= len) {
            return false;
        }
        byte sum = 0;
        for (int i = 0; i < FRAME_BYTES - 1; i++) {
            sum += data[index++];
        }
        byte checksum = data[index];

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
