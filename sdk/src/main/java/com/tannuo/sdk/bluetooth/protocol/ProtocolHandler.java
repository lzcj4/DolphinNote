package com.tannuo.sdk.bluetooth.protocol;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.tannuo.sdk.bluetooth.ConnectService;
import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.TouchScreenListener;


/**
 * Created by nick on 2016/4/24.
 */
public class ProtocolHandler {
    private final ConnectService mService;
    private final Protocol mProtocol;
    private final TouchScreenListener mTouchListener;

    private HandlerThread mProtocolThread;
    private CallbackHandler mHandler;
    public static final int ACTION_PROTOCOL_PARSE = 1;

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
        mHandler = new CallbackHandler(mProtocolThread.getLooper());
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

    private class CallbackHandler extends Handler {
        public CallbackHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_PROTOCOL_PARSE:
                    byte[] buffer = (byte[]) msg.obj;
                    if (null == buffer) {
                        return;
                    }
                    handlerProtocol(buffer);
                    break;
                default:
                    break;
            }
        }

        private void handlerProtocol(byte[] data) {
            if (null == data || data.length == 0) {
                return;
            }
            switch (mProtocol.parse(data)) {
                case JYDZ_Comm_Protocol.STATUS_CHANGE_DATA_FEATURE:
                    byte[] cmdChangeFeature = mProtocol.changeDataFeature();
                    if (cmdChangeFeature != null) {
                        mService.write(cmdChangeFeature);
                    }
                    break;
                case JYDZ_Comm_Protocol.STATUS_DATA_GET_OK:
                    TouchScreen touchScreen = mProtocol.getTouchScreen();
                    if (touchScreen.mTouchDownList.size() > 0) {
                        mTouchListener.onTouchDown(touchScreen.mTouchDownList);
                        touchScreen.mTouchDownList.clear();
                    }
                    if (touchScreen.mTouchUpList.size() > 0) {
                        mTouchListener.onTouchUp(touchScreen.mTouchUpList);
                        touchScreen.mTouchUpList.clear();
                    }
                    if (touchScreen.mTouchMoveList.size() > 0) {
                        mTouchListener.onTouchMove(touchScreen.mTouchMoveList);
                        touchScreen.mTouchMoveList.clear();
                    }
                    break;
            }
        }
    }
}
