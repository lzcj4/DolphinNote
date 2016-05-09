package com.tannuo.sdk.bluetooth.protocol;

import android.util.Log;

import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.util.DataUtil;

import java.util.Arrays;

/**
 * Created by nick on 2016/4/23.
 */
public class BTProtocol implements Protocol {
    private final String TAG = this.getClass().getSimpleName();

    public static final int STATUS_CHANGE_DATA_FEATURE = 1;
    public static final int STATUS_GET_DATA = 2;
    public static final int STATUS_GET_GESTURE = 3;
    public static final int STATUS_GET_SNAPSHOT = 4;
    public static final int STATUS_GET_IDENTI = 5;
    public static final int STATUS_GET_SCREEN_FEATURE = 6;

    private static final int ERROR_NONE = 0;
    private static final int ERROR_HEADER = -1;
    private static final int ERROR_DATA = -2;
    private static final int ERROR_DATA_LENGTH = -3;
    private static final int ERROR_DATA_FEATURE = -4;
    private static final int ERROR_CHECKSUM = -5;

    /**
     * Data feature with 5 bytes data
     */
    private static final int FEATURE_DATA_5 = 0;
    /**
     * Data feature with 6 bytes data
     */
    private static final int FEATURE_DATA_6 = 1;
    /**
     * Data feature with 10 bytes data
     */
    private static final int FEATURE_DATA_10 = 2;

    private static final int FEATURE_DATA_5_LEN = 5;
    private static final int FEATURE_DATA_6_LEN = 6;
    private static final int FEATURE_DATA_10_LEN = 10;

    private static final byte FEATURE_SCREEN = 0x60;
    private static final byte FEATURE_GESTURE = 0x70;
    private static final byte FEATURE_SNAPSHOT = 0x71;
    private static final byte FEATURE_IDENTI = 0x73;
    private static final byte FEATURE_USB_CONTROL = (byte) 0x80;
    private static final byte FEATURE_CHANGE_DATA_FORMAT = (byte) 0x81;

    private static final int USB_ENABLED = 1;
    private static final int USB_DISABLED = 0;

    private static final int MESSAGE_UART_CMD_GET = 100;

    private static final int PROTOCOL_HEADER = 0x68;
    private static final int PROTOCOL_MAX_LENGTH = 40;
    private static final int FEATURE_CHECKSUM_LEN = 2;

    private static final byte[] PACKAGE_ENABLE_USB = {0x68, 0x03, (byte) FEATURE_USB_CONTROL, 0x00, (byte) 0xEB};
    private static final byte[] PACKAGE_DISABLE_USB = {0x68, 0x03, (byte) FEATURE_USB_CONTROL, (byte) 0xAA, (byte) 0x95};

    private static final byte[] PACKAGE_FEATURE_DATA_5 = {0x68, 0x03, (byte) FEATURE_CHANGE_DATA_FORMAT, 0x00, (byte) 0xEC};
    private static final byte[] PACKAGE_FEATURE_DATA_6 = {0x68, 0x03, (byte) FEATURE_CHANGE_DATA_FORMAT, 0x01, (byte) 0xED};
    private static final byte[] PACKAGE_FEATURE_DATA_10 = {0x68, 0x03, (byte) FEATURE_CHANGE_DATA_FORMAT, 0x02, (byte) 0xEE};

//    名称	         含义	                        长度	      备注
//    帧头	         0x68	                        1字节
//    长度	         不包含帧头和长度域	            1字节
//    数据包特征码	 表示后续数据包含义	            1字节	   不同类型的包，特征码不一样
//    数据域		                                    x字节	   不同数据域长度不同,具体格式见第3节
//    校验	         8位累加和校验	                1字节	   从帧头开始

//    a)	截屏键
//    数据包特征码	数据域格式	      数据域长度	  备注
//    0x71	        按键按下/弹起	  1字节
//
//    示例(Hex)：   68 03 71 11 ED    按下截屏键
//                  68 03 71 10 EC    释放截屏键

//    b)	标识码
//    数据包特征码	数据域格式	数据域长度	备注
//    0x73	        编号	        4字节
//    示例(Hex)： 68  06  73  00000001  E2  标识码帧

    private byte mLen;
    private byte mDataFeature;
    private byte mChangeDataFeature;
    private byte mUSBCode;

    private byte mChecksum;
    private int mPoints = 1;
    private TouchScreen mTouchScreen;

    public byte[] mDataBuffer;

    public BTProtocol(TouchScreen touchScreen) {
        mTouchScreen = touchScreen;
        mDataFeature = FEATURE_DATA_5;
        mChangeDataFeature = FEATURE_DATA_5;
        mUSBCode = USB_DISABLED;

        mDataBuffer = new byte[256];
        mLen = 0;
        mPoints = 0;
    }

    private int getDataLen() {
        if (mLen < FEATURE_CHECKSUM_LEN) {
            throw new IllegalArgumentException("Protocol len must bigger than feature and checksum len");
        }
        int result = mLen - FEATURE_CHECKSUM_LEN;
        return result;
    }

    private int calcPoints() {
        mPoints = 0;
        if (mDataFeature == FEATURE_DATA_5) {
            mPoints = getDataLen() / FEATURE_DATA_5_LEN;
        } else if (mDataFeature == FEATURE_DATA_6) {
            mPoints = getDataLen() / FEATURE_DATA_6_LEN;
        } else if (mDataFeature == FEATURE_DATA_10) {
            mPoints = getDataLen() / FEATURE_DATA_10_LEN;
        } else {
            // throw new IllegalArgumentException("Invalid data feature");
        }
        mTouchScreen.setNumOfPoints(mPoints);
        return mPoints;
    }

    private boolean lengthCheck() {
        int len;
        switch (mDataFeature) {
            case FEATURE_DATA_5:
                len = FEATURE_CHECKSUM_LEN + mPoints * FEATURE_DATA_5_LEN;
                break;
            case FEATURE_DATA_6:
                len = FEATURE_CHECKSUM_LEN + mPoints * FEATURE_DATA_6_LEN;
                break;
            case FEATURE_DATA_10:
                len = FEATURE_CHECKSUM_LEN + mPoints * FEATURE_DATA_10_LEN;
                break;
            case FEATURE_SCREEN:
                len = FEATURE_CHECKSUM_LEN + 9;
                break;
            case FEATURE_GESTURE:
                len = FEATURE_CHECKSUM_LEN + 1;
                break;
            case FEATURE_SNAPSHOT:
                len = FEATURE_CHECKSUM_LEN + 1;
                break;
            case FEATURE_IDENTI:
                len =FEATURE_CHECKSUM_LEN + 4;
                break;
            default:
                len = -1;
                break;
        }
        boolean result = len == mLen;
        return result;
    }

    private boolean checkSum() {
        int len = getDataLen();
        byte sum = (byte) (PROTOCOL_HEADER + mDataFeature + mLen);
        for (int i = 0; i < len; i++) {
            sum += mDataBuffer[i];
        }
        return mChecksum == sum;
    }

    void reset() {
        mLen = 0;
        mDataFeature = 0;
        mChecksum = 0;
        mPoints = 0;

        Arrays.fill(mDataBuffer, (byte) 0);
        mTouchScreen.reset();
    }

    @Override
    public byte[] changeDataFeature() {
        if (mChangeDataFeature == FEATURE_DATA_5) {
            mChangeDataFeature = FEATURE_DATA_6;
            return PACKAGE_FEATURE_DATA_6;
        } else if (mChangeDataFeature == FEATURE_DATA_6) {
            mChangeDataFeature = FEATURE_DATA_10;
            return PACKAGE_FEATURE_DATA_10;
        } else if (mChangeDataFeature == FEATURE_DATA_10) {
            mChangeDataFeature = FEATURE_DATA_5;
            return PACKAGE_FEATURE_DATA_5;
        } else
            return null;
    }

    private byte[] changeUsbStatus() {
        if (mUSBCode == USB_ENABLED) {
            mUSBCode = USB_DISABLED;
            return PACKAGE_DISABLE_USB;
        } else if (mUSBCode == USB_DISABLED) {
            mUSBCode = USB_ENABLED;
            return PACKAGE_ENABLE_USB;
        } else
            return null;
    }


    @Override
    public int parse(byte[] data) {
        if (null == data || data.length == 0) {
            throw new IllegalArgumentException();
        }
        printData(data);
        this.reset();
        int errorCode = ERROR_NONE;
        //  68 03 71 11 ED    按下截屏键

        byte header = data[0];
        if (header != PROTOCOL_HEADER) {
            errorCode = ERROR_HEADER;
            Log.e(TAG, "get invalid protocol header ");
        }

        if (errorCode == ERROR_NONE) {
            mLen = data.length > 1 ? data[1] : 0;
            if (mLen < FEATURE_CHECKSUM_LEN) {
                errorCode = ERROR_DATA_LENGTH;
                Log.e(TAG, "get invalid protocol  data len ");
            }
        }

        if (errorCode == ERROR_NONE) {
            mDataFeature = data.length > 2 ? data[2] : 0;
            calcPoints();
            if (!lengthCheck()) {
                errorCode = ERROR_DATA_FEATURE;
                Log.e(TAG, "get data feature and calc point len failed ");
            }
        }

        if (errorCode == ERROR_NONE) {
            int dataLen = getDataLen();
            int startIndex = 3;// header+feature+len
            int endIndex = startIndex + dataLen;
            if (dataLen > 0 && endIndex < data.length) {
                mDataBuffer = Arrays.copyOfRange(data, startIndex, endIndex);
            } else {
                errorCode = ERROR_DATA;
                Log.e(TAG, "get real data failed ");
            }
        }
        if (errorCode == ERROR_NONE) {
            mChecksum = data[data.length - 1];
            if (!checkSum()) {
                errorCode = ERROR_CHECKSUM;
                Log.d(TAG, "checksum invalid");
            }
        }

        int result = ERROR_NONE;
        if (errorCode == ERROR_NONE) {
            result = setScreenData();
        }

        return result;
    }

    private void printData(byte[] data) {
        if (null == data || data.length == 0) {
            return;
        }

        //if (BuildConfig.DEBUG) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        Log.d(TAG, String.format("received data:%s", sb.toString()));
        // }
    }

    private int setScreenData() {
        int result = 0;
        switch (mDataFeature) {
            case BTProtocol.FEATURE_DATA_5:
            case BTProtocol.FEATURE_DATA_6:
                result = STATUS_CHANGE_DATA_FEATURE;
                break;
            case BTProtocol.FEATURE_DATA_10:
                mTouchScreen.setPoint(mPoints, this.mDataBuffer);
                result = STATUS_GET_DATA;
                break;
            case BTProtocol.FEATURE_SCREEN:
                mTouchScreen.setIrTouchFeature(this.mDataBuffer);
                result = STATUS_GET_SCREEN_FEATURE;
                break;
            case BTProtocol.FEATURE_GESTURE:
                mTouchScreen.setGesture(this.mDataBuffer[0]);
                result = STATUS_GET_GESTURE;
                break;
            case BTProtocol.FEATURE_SNAPSHOT:
                mTouchScreen.setSnapshot(this.mDataBuffer[0]);
                result = STATUS_GET_SNAPSHOT;
                break;
            case BTProtocol.FEATURE_IDENTI:
                mTouchScreen.setID(DataUtil.bytesToIntLittleEndian(mDataBuffer[0], mDataBuffer[1], mDataBuffer[2], mDataBuffer[3]));
                result = STATUS_GET_IDENTI;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public TouchScreen getTouchScreen() {
        return mTouchScreen;
    }

}