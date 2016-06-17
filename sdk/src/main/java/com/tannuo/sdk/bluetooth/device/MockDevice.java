package com.tannuo.sdk.bluetooth.device;

import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.TouchScreenListener;
import com.tannuo.sdk.bluetooth.protocol.BTProtocol;
import com.tannuo.sdk.bluetooth.protocol.Protocol;
import com.tannuo.sdk.bluetooth.protocol.ProtocolHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Nick_PC on 2016/5/20.
 */
public class MockDevice implements IDevice {
    private final ProtocolHandler mHandler;
    private final Protocol mProtocol;
    private final TouchScreenListener mTouchListener;

    private final TouchScreen mTouchScreen;
    private ProtocolHandler.OnDataReceive mDataListener;

    public MockDevice(TouchScreenListener listener) {
        mTouchScreen = new TouchScreen(600, 2000);
        mProtocol = new BTProtocol(mTouchScreen);
        mTouchListener = listener;
        if (listener instanceof ProtocolHandler.OnDataReceive) {
            mDataListener = (ProtocolHandler.OnDataReceive) listener;
        }
        mHandler = new ProtocolHandler(this, mProtocol, mTouchListener);
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
                if (mDataListener != null) {
                    mDataListener.onReceive(data);
                }
                int code = mProtocol.parse(data);
                switch (code) {
                    case BTProtocol.STATUS_GET_DATA:
                        if (!mTouchScreen.mTouchDownList.isEmpty()) {
                            mTouchListener.onTouchDown(mTouchScreen.mTouchDownList);
                            mTouchScreen.mTouchDownList.clear();
                        } else if (!mTouchScreen.mTouchMoveList.isEmpty()) {
                            mTouchListener.onTouchMove(mTouchScreen.mTouchMoveList);
                            mTouchScreen.mTouchMoveList.clear();
                        } else if (!mTouchScreen.mTouchUpList.isEmpty()) {
                            mTouchListener.onTouchUp(mTouchScreen.mTouchUpList);
                            mTouchScreen.mTouchUpList.clear();
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
    public int connect(String deviceName) {
        Observable.create((s) -> mockReader())
                .observeOn(Schedulers.io())
                .subscribe((s) -> {
                });
        mTouchListener.onBLConnected();
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
