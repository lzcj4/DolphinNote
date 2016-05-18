package com.tannuo.sdk.bluetooth.protocol;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.TouchScreenListener;
import com.tannuo.sdk.bluetooth.connectservice.ConnectService;
import com.tannuo.sdk.util.DataLog;

import java.lang.ref.WeakReference;


/**
 * Created by nick on 2016/4/24.
 */
public class ProtocolHandler {
    public interface OnDataReceive {
        void onReceive(byte[] data);
    }

    private final ConnectService mService;
    private final Protocol mProtocol;
    private final TouchScreenListener mTouchListener;
    private OnDataReceive mDataReceive;

    private HandlerThread mProtocolThread;
    private ProtocolParseHandler mHandler;
    public static final int MESSAGE_PROTOCOL_PARSE = 1;
    private DataLog mDataLog;

    public ProtocolHandler(ConnectService service, Protocol protocol,
                           TouchScreenListener touchListener) {
        if (null == service || null == protocol || null == touchListener) {
            throw new IllegalArgumentException();
        }
        mService = service;
        mProtocol = protocol;
        mTouchListener = touchListener;
        if (touchListener instanceof OnDataReceive) {
            mDataReceive = (OnDataReceive) touchListener;
        }

        start();
    }

    private void start() {
        mProtocolThread = new HandlerThread("protocol_handler_thread");
        mProtocolThread.start();
        mHandler = new ProtocolParseHandler(mProtocolThread.getLooper(), this);
        mDataLog =DataLog.getInstance();
    }

    public void sendMessage(int what, Object obj) {
        mHandler.obtainMessage(what, obj).sendToTarget();
    }

    public void sendMessage(Message msg) {
        mHandler.sendMessage(msg);
    }

    public void post(Runnable runnable) {
        mHandler.post(runnable);
    }

    public void postDelayed(Runnable runnable, long delayMS) {
        mHandler.postDelayed(runnable, delayMS);
    }

    public void stop() {
        mProtocolThread.quit();
        mDataLog.close();
    }

    private static class ProtocolParseHandler extends Handler {
        private WeakReference<ProtocolHandler> wrProtocolHanlder;

        public ProtocolParseHandler(Looper looper, ProtocolHandler protocolHandler) {
            super(looper);
            wrProtocolHanlder = new WeakReference<>(protocolHandler);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PROTOCOL_PARSE:
                    byte[] buffer = (byte[]) msg.obj;
                    if (null == buffer) {
                        return;
                    }
                    parse(buffer);
                    break;
                default:
                    break;
            }
        }

        private void parse(byte[] data) {
            if (null == data || data.length == 0 || wrProtocolHanlder.get() == null) {
                return;
            }
            ProtocolHandler handler = wrProtocolHanlder.get();
            Protocol protocol = handler.mProtocol;
            ConnectService service = handler.mService;
            TouchScreenListener touchListener = handler.mTouchListener;

            handler.mDataLog.writeInData(data);
            if (handler.mDataReceive != null) {
                handler.mDataReceive.onReceive(data);
            }

            switch (protocol.parse(data)) {
                case BTProtocol.STATUS_CHANGE_DATA_FEATURE:
                    byte[] cmdChangeFeature = protocol.changeDataFeature();
                    if (cmdChangeFeature != null) {
                        service.write(cmdChangeFeature);
                    }
                    break;
                case BTProtocol.STATUS_GET_DATA:
                    handler.mDataLog.writeOutData(data);
                    TouchScreen touchScreen = protocol.getTouchScreen();
                    if (touchScreen.mTouchDownList.size() > 0) {
                        touchListener.onTouchDown(touchScreen.mTouchDownList);
                        touchScreen.mTouchDownList.clear();
                    }
                    if (touchScreen.mTouchUpList.size() > 0) {
                        touchListener.onTouchUp(touchScreen.mTouchUpList);
                        touchScreen.mTouchUpList.clear();
                    }
                    if (touchScreen.mTouchMoveList.size() > 0) {
                        touchListener.onTouchMove(touchScreen.mTouchMoveList);
                        touchScreen.mTouchMoveList.clear();
                    }
                    break;
            }
        }
    }
}
