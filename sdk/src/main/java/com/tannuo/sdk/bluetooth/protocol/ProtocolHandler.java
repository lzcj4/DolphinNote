package com.tannuo.sdk.bluetooth.protocol;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.TouchScreenListener;
import com.tannuo.sdk.bluetooth.connectservice.ConnectService;
import com.tannuo.sdk.util.DataLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


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
        mHandler.stop();
        DataLog.getInstance().close();
    }

    private static class ProtocolParseHandler extends Handler {
        private WeakReference<ProtocolHandler> wrProtocolHandler;
        private Thread mParseThread;
        private boolean mIsParsing = true;

        public ProtocolParseHandler(Looper looper, ProtocolHandler protocolHandler) {
            super(looper);
            wrProtocolHandler = new WeakReference<>(protocolHandler);

            mParseThread = new Thread(() -> {
                while (mIsParsing) {
                    byte[] data = getFromCache();
                    if (data.length > 0) {
                        parse(data);
                    }
                }
            });
            mParseThread.setName("protoco l_parse_thread");
            mParseThread.start();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PROTOCOL_PARSE:
                    byte[] buffer = (byte[]) msg.obj;
                    if (null == buffer) {
                        return;
                    }
                    addToCache(buffer);
                    break;
                default:
                    break;
            }
        }

        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        List<byte[]> dataCache = new ArrayList<>();

        private void addToCache(byte[] buffer) {
            try {
                lock.lock();
                dataCache.add(buffer);
                Log.e(this.getClass().getSimpleName(), String.format("/++ %s ++/", (dataCache.size())));
                condition.signal();
            } finally {
                lock.unlock();
            }
        }

        private byte[] getFromCache() {
            try {
                lock.lock();
                byte[] data = new byte[0];
                if (dataCache.size() > 0) {
                    data = dataCache.remove(0);
                    Log.e(this.getClass().getSimpleName(), String.format("/-- %s --/", (dataCache.size())));
                } else {
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return data;
            } finally {
                lock.unlock();
            }
        }

        private void parse(byte[] data) {
            if (null == data || data.length == 0 || wrProtocolHandler.get() == null) {
                return;
            }
            ProtocolHandler handler = wrProtocolHandler.get();
            Protocol protocol = handler.mProtocol;
            ConnectService service = handler.mService;
            TouchScreenListener touchListener = handler.mTouchListener;

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

        public void stop() {
            mIsParsing = false;
        }
    }
}
