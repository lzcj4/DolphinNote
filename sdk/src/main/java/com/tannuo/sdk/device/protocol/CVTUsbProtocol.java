package com.tannuo.sdk.device.protocol;

import com.tannuo.sdk.device.TouchFrame;
import com.tannuo.sdk.device.TouchPath;
import com.tannuo.sdk.device.TouchPoint;
import com.tannuo.sdk.util.DataLog;
import com.tannuo.sdk.util.DataUtil;
import com.tannuo.sdk.util.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Nick_PC on 2016/7/13.
 */

public class CVTUsbProtocol extends ProtocolBase {
    private final byte FRAME_HEADER = 0x02;
    private final byte FRAME_BYTES = 0x3E;//62 bytes
    private final byte FRAME_POINTS = 6;
    private final byte POINT_BYTES = 10;

    private static final byte ACTION_NONE = 0x00;
    private final byte ACTION_DOWN = 0x04;
    private final byte ACTION_MOVE = 0x07;
    private final byte ACTION_UP = 0x04;

    public CVTUsbProtocol() {
        TouchPoint.setActions(ACTION_DOWN, ACTION_MOVE, ACTION_UP);
    }

    TouchFrame mTouchFrame = new TouchFrame();

    @Override
    public int parse(byte[] data) {
        if (null == data || data.length == 0) {
            throw new IllegalArgumentException();
        }

        DataLog.getInstance().writeInData(data);
        DataLog.getInstance().writeInLineData(data);
        reset();

//        02 07 00 90 52 8a 13 38 00 66 00 00 01 00 00 00
//        00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00
//        03 00 00 00 00 00 00 00 00 00 04 00 00 00 00 00
//        00 00 00 00 05 00 00 00 00 00 00 00 06

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
            if (!(totalData[index++] == FRAME_HEADER)) {
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
                point.setX(DataUtil.bytesShortLittleEndian(totalData[index++], totalData[index++]));// byte:2,3
                point.setY(DataUtil.bytesShortLittleEndian(totalData[index++], totalData[index++]));// byte:4,5
                point.setWidth(DataUtil.bytesShortLittleEndian(totalData[index++], totalData[index++]));// byte:6,7
                point.setHeight(DataUtil.bytesShortLittleEndian(totalData[index++], totalData[index++]));// byte:8,9
                mPoints.add(point);
                TouchPath path = mTouchFrame.get(point.getId());
                if (path != null && path.getLastPoint() != null &&
                        path.getLastPoint().getIsMove() && action == ACTION_UP) {
                    point.setAction(TouchPoint.ACTION_UP);
                    mTouchFrame.remove(point.getId());
                }
                mTouchFrame.put(point);
            }
            // Collections.sort(mPoints, (lhs, rhs) -> lhs.getId() - rhs.getId());
            int frameEnd = totalData[index++];
            Logger.i(TAG, "getFactory frame end ");

            i = index;
        }
        return result;
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
