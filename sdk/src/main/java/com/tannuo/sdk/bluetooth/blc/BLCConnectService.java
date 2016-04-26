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

    private ConnectThread mConnectThread;
    private IOThread mIOThread;
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
        cancelAllThreads();
        mHandler.stop();
        setState(BL_STATE_LISTEN);
        return BL_STATE_READY;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    /**
     * Start the chat service. Specifically reset AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void reset() {
        Log.d(TAG, "reset");

        cancelAllThreads();
        setState(BL_STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == BL_STATE_CONNECTING) {
            cancelConnectThread();
        }

        cancelIOThread();

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(BL_STATE_CONNECTING);
    }

    /**
     * Start the IOThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been startIO
     */
    public void startIO(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "startIO, Socket Type:" + socketType);

        cancelAllThreads();

        // Start the thread to manage the connection and perform transmissions
        mIOThread = new IOThread(socket, socketType);
        mIOThread.start();
        mTouchListener.onBLConnected();
        setState(BL_STATE_CONNECTED);
    }


    public void stop() {
        Log.d(TAG, "stop");

        cancelAllThreads();
        setState(BL_STATE_NONE);
    }

    private void cancelConnectThread() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    private void cancelIOThread() {
        if (mIOThread != null) {
            mIOThread.cancel();
            mIOThread = null;
        }
    }

    private void cancelAllThreads() {
        cancelConnectThread();
        cancelIOThread();
    }

    /**
     * Write to the IOThread in an unsynchronized manner
     *
     * @param data The bytes to write
     * @see IOThread#write(byte[])
     */
    @Override
    public void write(byte[] data) {
        // Create temporary object
        IOThread r;
        // Synchronize a copy of the IOThread
        synchronized (this) {
            if (mState != BL_STATE_CONNECTED) return;
            r = mIOThread;
        }
        // Perform the write unsynchronized
        r.write(data);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        mTouchListener.onError(BL_ERROR_CONN_FAILED);
        this.reset();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        mTouchListener.onError(BL_ERROR_CONN_LOST);
        this.reset();
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mDevice = device;
            BluetoothSocket blSocket = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                blSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                Log.d(TAG, "Create Socket...");
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mSocket = blSocket;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            mBTAdapter.cancelDiscovery();

            try {
                mSocket.connect();
            } catch (IOException e) {
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BLCConnectService.this) {
                mConnectThread = null;
            }

            // Start the startIO thread
            startIO(mSocket, mDevice, mSocketType);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class IOThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean isRunning = true;

        public IOThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create IOThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mIOThread");

            byte[] buffer = new byte[1024];
            int readLen;

            // Keep listening to the InputStream while startIO
            while (isRunning) {
                try {
                    // Read from the InputStream
                    readLen = mmInStream.read(buffer);
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
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                this.isRunning = false;
                mmSocket.close();
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

