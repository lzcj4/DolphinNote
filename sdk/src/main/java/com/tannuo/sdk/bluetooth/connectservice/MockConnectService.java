package com.tannuo.sdk.bluetooth.connectservice;

import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.TouchScreenListener;
import com.tannuo.sdk.bluetooth.TouchScreenListenerImpl;
import com.tannuo.sdk.bluetooth.protocol.BTProtocol;
import com.tannuo.sdk.bluetooth.protocol.Protocol;
import com.tannuo.sdk.bluetooth.protocol.ProtocolHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Nick_PC on 2016/5/20.
 */
public class MockConnectService implements ConnectService {
    ProtocolHandler mHandler;
    Protocol mProtocol;
    private TouchScreenListener mListener = new TouchScreenListenerImpl() {
        @Override
        public void onTouchDown(List<TouchScreen.TouchPoint> downPoints) {
            super.onTouchDown(downPoints);
        }

        @Override
        public void onTouchUp(List<TouchScreen.TouchPoint> upPoints) {
            super.onTouchUp(upPoints);
        }
    };

    private final TouchScreen mTouchScreen;
    private final TouchListener mTouchListener;

    public MockConnectService(TouchListener listener) {
        mTouchScreen = new TouchScreen(600, 2000);
        mProtocol = new BTProtocol(mTouchScreen);
        mHandler = new ProtocolHandler(this, mProtocol, mListener);
        mTouchListener = listener;
    }

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

        while (true) {
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

                int code = mProtocol.parse(data);
                switch (code) {
                    case BTProtocol.STATUS_GET_DATA:
                        TouchEvent touchEvent = null;
                        if (!mTouchScreen.mTouchDownList.isEmpty()) {
                            touchEvent = getTouchEvent(TouchEvent.DOWN, mTouchScreen.mTouchDownList);
                        } else if (!mTouchScreen.mTouchMoveList.isEmpty()) {
                            touchEvent = getTouchEvent(TouchEvent.MOVE, mTouchScreen.mTouchMoveList);
                        } else if (!mTouchScreen.mTouchUpList.isEmpty()) {
                            touchEvent = getTouchEvent(TouchEvent.UP, mTouchScreen.mTouchUpList);
                        }
                        if (touchEvent != null) {
                            mTouchListener.onTouch(touchEvent);
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

    private TouchEvent getTouchEvent(int touchMode, List<TouchScreen.TouchPoint> points) {
        TouchEvent result = new TouchEvent();
        result.Mode = touchMode;
        List<TouchPoint> list = new ArrayList<>();
        for (TouchScreen.TouchPoint item : mTouchScreen.mTouchDownList) {
            TouchPoint p = new TouchPoint(item);
            list.add(p);
        }
        result.Points = list;
        return result;

    }

    @Override
    public int connect(String deviceName) {
        Observable.create((s) -> mockReader())
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> {
                });
        return 0;
    }

    @Override
    public int disconnect() {
        return 0;
    }

    @Override
    public void write(byte[] data) {

    }
}
