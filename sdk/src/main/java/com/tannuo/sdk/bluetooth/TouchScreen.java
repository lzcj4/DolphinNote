package com.tannuo.sdk.bluetooth;

import android.util.SparseArray;

import com.tannuo.sdk.util.DataUtil;
import com.tannuo.sdk.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 2016/04/22.
 */

public class TouchScreen {
    private final String TAG = this.getClass().getSimpleName();
    public static final int PAINTMODE_DOT_1 = 0;
    public static final int PAINTMODE_DOT_2 = 1;
    public static final int PAINTMODE_LINE_1 = 2;
    public static final int PAINTMODE_LINE_2 = 3;

    public static final int PATH_BUF_MAX_LEN = 100;

    public static final int POINT_BLACK = 1;
    public static final int POINT_RED = 2;
    public static final int POINT_WHITE = 3;

    public static final int IR_TOUCH_X_MAX = 32767;
    public static final int IR_TOUCH_Y_MAX = 32767;

    public static final int POINT_STATUS_UP = 4;
    public static final int POINT_STATUS_DOWN = 7;

    public IrTouch mIrTouch;

    public List<TouchPoint> mTouchUpList = new ArrayList<>();
    public List<TouchPoint> mTouchDownList = new ArrayList<>();
    public List<TouchPoint> mTouchMoveList = new ArrayList<>();

    //屏幕的属性
    public int mPaintMode = PAINTMODE_DOT_1;
    public int mGesture;
    public long mTouchScreenID;
    public int mNumOfPoints;
    public int mSnapshot;

    private int mRedMin;
    private int mRedMax;

    public TouchScreen(int redMin, int redMax) {
        mRedMin = redMin;
        mRedMax = redMax;
    }

    public void reset() {
        mPaintMode = PAINTMODE_DOT_1;
        mIrTouch = null;
        mTouchUpList.clear();
        mTouchDownList.clear();
        mTouchMoveList.clear();
    }

    public void setPoint(int pointLen, int[] buffer) {
        if (pointLen <= 0 || buffer == null || (buffer.length % 10) != 0) {
            throw new IllegalArgumentException("point length or data invalid");
        }
        SparseArray<TouchPoint> downMap = new SparseArray<>();
        SparseArray<TouchPoint> upMap = new SparseArray<>();

        for (int i = 0; i < pointLen; i++) {
            int pos = i * 10;
            TouchPoint point = new TouchPoint();
            point.pointStatus = buffer[pos];
            point.pointId = buffer[pos + 1];
            point.pointX = DataUtil.bytesToIntLittleEndian(buffer[pos + 2], buffer[pos + 3]);
            point.pointY = DataUtil.bytesToIntLittleEndian(buffer[pos + 4], buffer[pos + 5]);
            point.pointWidth = DataUtil.bytesToIntLittleEndian(buffer[pos + 6], buffer[pos + 7]);
            point.pointHeight = DataUtil.bytesToIntLittleEndian(buffer[pos + 8], buffer[pos + 9]);
            point.pointArea = point.pointWidth * point.pointHeight;
            float area = point.pointArea;
            if ((area > mRedMin) && (area < mRedMax)) {
                point.pointColor = POINT_RED;
            } else if (area >= mRedMax) {
                point.pointColor = POINT_WHITE;
            } else {
                point.pointColor = POINT_BLACK;
            }

            Logger.i(TAG, point.toString());
            if (point.pointStatus == POINT_STATUS_DOWN) {
                downMap.put(point.pointId, point);
            } else {
                upMap.put(point.pointId, point);
            }
        }

        if (downMap.size() > 0) {
            for (int i = 0; i < downMap.size(); i++) {
                mTouchDownList.add(downMap.valueAt(i));
            }
        }

        if (upMap.size() > 0) {
            for (int i = 0; i < upMap.size(); i++) {
                mTouchUpList.add(upMap.valueAt(i));
            }
        }
    }

    //
    public void setIrTouchFeature(int[] dataBuffer) {
        if (null == dataBuffer || dataBuffer.length != 9) {
            throw new IllegalArgumentException("Invalid data");
        }
        mIrTouch = new IrTouch();
        mIrTouch.mScreenXLED = DataUtil.bytesToIntLittleEndian(dataBuffer[0], dataBuffer[1]);
        mIrTouch.mScreenYLED = DataUtil.bytesToIntLittleEndian(dataBuffer[2], dataBuffer[3]);
        mIrTouch.mScreenLedInsert = dataBuffer[4];
        mIrTouch.mScreenLedDistance = DataUtil.bytesToIntLittleEndian(dataBuffer[5], dataBuffer[6]);
        mIrTouch.mScreenMaxPoint = dataBuffer[7];
        mIrTouch.mScreenFrameRate = dataBuffer[8];
    }

    //
    public void setPaintMode(int drawMode) {
        if (drawMode == PAINTMODE_DOT_1)
            mPaintMode = PAINTMODE_DOT_1;
        else if (drawMode == PAINTMODE_LINE_1)
            mPaintMode = PAINTMODE_LINE_1;
    }

    //
    public int getPaintMode() {
        return mPaintMode;
    }

    //
    public void setScreenWidth(float width) {
        //mScreenWidth = Width;
    }

    float getScreenWidth() {
        return 0;
    }

    void setScreenHeight(float height) {
        //mScreenHeight = Height;
    }

    public float getScreenHeight() {
        return 0;
    }

    public void setNumOfPoints(int pointNum) {
        mNumOfPoints = pointNum;
    }

    public void setGesture(int newGesture) {
        mGesture = newGesture;
    }

    public void setID(long newID) {
        mTouchScreenID = newID;
    }

    public void setSnapshot(int shot) {
        mSnapshot = shot;
    }

    public class TouchPoint {
        public int pointId;
        public int pointStatus;
        public int pointX;
        public int pointY;
        public int pointWidth;
        public int pointHeight;
        public int pointArea;
        public byte pointColor;


        @Override
        public String toString() {
            return String.format("id:%s, x:%s, y:%s,  width:%s ,height:%s",
                    pointId, pointX, pointY, pointWidth, pointHeight);
        }
    }


    public class IrTouch {
        public int mScreenXLED;
        public int mScreenYLED;
        public int mScreenLedInsert;
        public int mScreenLedDistance;
        public int mScreenMaxPoint;
        public int mScreenFrameRate;
    }
}
