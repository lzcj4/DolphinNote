package com.tannuo.sdk.bluetooth.blc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.tannuo.sdk.bluetooth.ConnectServiceBase;
import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.TouchScreenListener;
import com.tannuo.sdk.bluetooth.protocol.JYDZ_Comm_Protocol;
import com.tannuo.sdk.bluetooth.protocol.ProtocolHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Bluetooth classic connect service
 */
public class BLCConnectService extends ConnectServiceBase {

    // Unique UUID for this application
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BLCThread mThread;
    private int mState;

    public BLCConnectService(Context context, TouchScreenListener touchListener) {
        super(context, touchListener);
        mProtocol = new JYDZ_Comm_Protocol(new TouchScreen(600, 2000));
        mHandler = new ProtocolHandler(this, mProtocol, mTouchListener);
    }

    @Override
    public int connect(String devName, String devAddr, String pwd) {
        // Get the local Bluetooth adapter
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBTAdapter.isEnabled()) {
            mTouchListener.onError(BL_ERROR_NOT_ENABLE);
            return BL_STATE_BL_NOT_ENABLE;
        }

        mDeviceName = devName;
        mDeviceAddr = devAddr;
        mDevicePassword = pwd;
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, filter);
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
        }
        mBTAdapter.startDiscovery();
        return BL_STATE_READY;
    }

    @Override
    public int disconnect() {
        Log.d(TAG, "disconnect");
        stopBLCThread();
        mHandler.stop();
        setState(BL_STATE_LISTEN);
        return BL_STATE_READY;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }


    public void reset() {
        Log.d(TAG, "reset");

        stopBLCThread();
        setState(BL_STATE_LISTEN);
    }


    public void connect(BluetoothDevice device, boolean isSecure) {
        Log.d(TAG, "connect to: " + device);

        stopBLCThread();
        mThread = new BLCThread(device, isSecure);
        mThread.start();
        setState(BL_STATE_CONNECTING);
    }

    private void stopBLCThread() {
        if (mThread != null) {
            mThread.cancel();
            mThread = null;
        }
    }


    @Override
    public void write(byte[] data) {
        mThread.write(data);
    }


    private void connectFailed() {
        mTouchListener.onError(BL_ERROR_CONN_FAILED);
        this.reset();
    }

    private void connectSucceed() {
        setState(BL_STATE_CONNECTED);
        mTouchListener.onBLConnected();
    }

    private void connectionLost() {
        mTouchListener.onError(BL_ERROR_CONN_LOST);
        this.reset();
    }

    private class BLCThread extends Thread {
        private BluetoothSocket mSocket;
        private BluetoothDevice mDevice;
        private String mSocketType;

        private InputStream mInStream;
        private OutputStream mOutStream;
        private boolean isRunning = true;

        public BLCThread(BluetoothDevice device, boolean secure) {
            if (null == device) {
                throw new IllegalArgumentException("device");
            }
            mDevice = device;
            mSocketType = secure ? "Secure" : "Insecure";
            setName("BLCThread_" + mSocketType);
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);

            if (!connect()) {
                return;
            }

            connectSucceed();

            if (getStream()) {
                readInLoop();
            } else {
                connectionLost();
            }
        }

        private boolean connect() {
            boolean result = false;
            try {
                mBTAdapter.cancelDiscovery();
                mSocket = mDevice.createRfcommSocketToServiceRecord(SPP_UUID);
                Log.d(TAG, "Create Socket...");
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mBTAdapter.cancelDiscovery();

            try {
                mSocket.connect();
                result = true;
            } catch (IOException e) {
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectFailed();
            }
            return result;

        }

        private boolean getStream() {
            boolean result = true;
            // Get the BluetoothSocket input and output streams
            try {
                mInStream = mSocket.getInputStream();

            } catch (Exception e) {
                Log.e(TAG, "temp sockets not created", e);
                result = false;
            }

            try {
                mOutStream = mSocket.getOutputStream();
            } catch (Exception e) {
                Log.e(TAG, "temp sockets not created", e);
                result = false;
            }
            return result;
        }

        private void readInLoop() {
            byte[] buffer = new byte[1024];
            int readLen;

            // Keep listening to the InputStream while startIO
            while (isRunning) {
                try {
                    // Read from the InputStream
                    readLen = mInStream.read(buffer);
                    if (readLen == 0) {
                        continue;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    BLCConnectService.this.reset();
                    break;
                }

                byte[] data = Arrays.copyOfRange(buffer, 0, readLen);
                mHandler.sendMessage(ProtocolHandler.ACTION_PROTOCOL_PARSE, data);
            }
        }

        /**
         * Write to the startIO OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                if (mOutStream != null) {
                    mOutStream.write(buffer);
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                this.isRunning = false;
                if (mSocket != null) {
                    mSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (null == device || !device.getName().equalsIgnoreCase(mDeviceName)) {
                    return;
                }
                if (TextUtils.isEmpty(mDeviceAddr)) {
                    mDeviceAddr = device.getAddress();
                } else {
                    mDeviceName = device.getName();
                }

                mDevice = device;
                mBTAdapter.cancelDiscovery();
                connect(device, true);
                mContext.unregisterReceiver(mReceiver);
                Log.v(TAG, "Device Get  " + mDeviceAddr);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mDevice == null) {
                    Log.v(TAG, "No Device Named " + mDeviceName + " Found");
                    mTouchListener.onError(BL_ERROR_DEV_NOT_FOUND);
                }
                mDevice = null;
                mDeviceAddr = null;
                mContext.unregisterReceiver(mReceiver);
            }
        }
    };

}

