package com.tannuo.sdk.device.bluetooth;

import com.tannuo.sdk.device.TouchEvent;
import com.tannuo.sdk.device.protocol.IProtocol;
import com.tannuo.sdk.device.protocol.JYTouchScreen;
import com.tannuo.sdk.device.protocol.JYProtocol;
import com.tannuo.sdk.device.protocol.ProtocolHandler;
import com.tannuo.sdk.device.TouchDeviceListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Nick_PC on 2016/5/20.
 */
public class MockDevice implements IDevice {
    private final ProtocolHandler mHandler;
    private final IProtocol mProtocol;
    private final TouchDeviceListener mListener;

    private final JYTouchScreen mTouchScreen;
    private ProtocolHandler.DataListener mDataListener;

    public MockDevice(TouchDeviceListener listener) {
        mTouchScreen = new JYTouchScreen(600, 2000);
        mProtocol = new JYProtocol(mTouchScreen);
        mListener = listener;
        if (listener instanceof ProtocolHandler.DataListener) {
            mDataListener = (ProtocolHandler.DataListener) listener;
        }
        mHandler = new ProtocolHandler(this, mProtocol, mListener);
    }

    private boolean isRunning = true;

    private void mockReader() {
        BufferedReader reader = null;
        File file = new File("/sdcard/point_data/data.txt");
        if (file.exists()) {
            try {
                reader = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        while (isRunning) {
            try {
                String line = reader.readLine();
                if (null == line) {
                    break;
                }

                String[] items = line.split(" ");
                int len = items.length;
                byte[] data = new byte[len];
                for (int i = 0; i < len; i++) {
                    String hexValue = "0x" + items[i];
                    int intValue = Integer.decode(hexValue);
                    data[i] = (byte) (intValue);
                }
                if (mDataListener != null) {
                    mDataListener.onReceive(data);
                }
                int code = mProtocol.parse(data);
                switch (code) {
                    case JYProtocol.STATUS_GET_DATA:
                        if (!mTouchScreen.mPoints.isEmpty()) {
                            TouchEvent event = new TouchEvent();
                            event.setAction(TouchEvent.ACTION_TOUCH);
                            event.setPoints(mTouchScreen.mPoints);
                            mListener.onTouchEvent(event);
                        }
                        break;
                    default:
                        break;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int connect(String name) {
        isRunning = true;
        Observable.create((s) -> mockReader())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> {
                });
        mListener.onConnected();
        return 0;
    }

    @Override
    public int disconnect() {
        isRunning = false;
        mListener.onDisconnected();
        return 0;
    }

    @Override
    public void write(byte[] data) {

    }
}
