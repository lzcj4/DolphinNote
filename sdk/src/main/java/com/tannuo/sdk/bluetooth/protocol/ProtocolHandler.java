package com.tannuo.sdk.bluetooth.protocol;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.tannuo.sdk.bluetooth.connectservice.ConnectService;
import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.TouchScreenListener;

import java.lang.ref.WeakReference;


/**
 * Created by nick on 2016/4/24.
 */
public class ProtocolHandler {
    private final ConnectService mService;
    private final Protocol mProtocol;
    private final TouchScreenListener mTouchListener;

    private HandlerThread mProtocolThread;
    private ProtocolParseHandler mHandler;
    public static final int MESSAGE_PROTOCOL_PARSE = 1;

    public ProtocolHandler(ConnectService service, Protocol protocol,
                           TouchScreenListener touchListener) {
        if (null == service || null == protocol || null == touchListener) {
            throw new IllegalArgumentException();
        }
        mService = service;
        mProtocol = protocol;
        mTouchListener = touchListener;

        start();
    }

    private void start() {
        mProtocolThread = new HandlerThread("protocol_handler_thread");
        mProtocolThread.start();
        mHandler = new ProtocolParseHandler(mProtocolThread.getLooper(), this);
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

            Protocol protocol = wrProtocolHanlder.get().mProtocol;
            ConnectService service = wrProtocolHanlder.get().mService;
            TouchScreenListener touchListener = wrProtocolHanlder.get().mTouchListener;

            switch (protocol.parse(data)) {
                case JYDZ_Comm_Protocol.STATUS_CHANGE_DATA_FEATURE:
                    byte[] cmdChangeFeature = protocol.changeDataFeature();
                    if (cmdChangeFeature != null) {
                        service.write(cmdChangeFeature);
                    }
                    break;
                case JYDZ_Comm_Protocol.STATUS_DATA_GET_OK:
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
