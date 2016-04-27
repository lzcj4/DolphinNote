package com.tannuo.sdk.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.tannuo.sdk.bluetooth.protocol.JYDZ_Comm_Protocol;
import com.tannuo.sdk.bluetooth.protocol.ProtocolHandler;

/**
 * Created by nlang on 4/26/2016.
 */
public abstract class ConnectServiceBase implements ConnectService {
    protected final String TAG = this.getClass().getSimpleName();

    // Constants that indicate the current connection state
    protected static final int BL_STATE_NONE = 0;       // we're doing nothing
    protected static final int BL_STATE_LISTEN = 1;     // now listening for incoming connections
    protected static final int BL_STATE_CONNECTING = 2; // now initiating an outgoing connection
    protected static final int BL_STATE_CONNECTED = 3;  // now startIO to a remote device
    protected static final int BL_STATE_READY = 4;  // now startIO to a remote device
    protected static final int BL_STATE_BL_NOT_ENABLE = -1;  // now startIO to a remote device

    protected static final int BL_ERROR_NONE = 0;
    protected static final int BL_ERROR_CONN_FAILED = 1;
    protected static final int BL_ERROR_CONN_LOST = 2;
    protected static final int BL_ERROR_DEV_NOT_FOUND = 3;
    protected static final int BL_ERROR_NOT_ENABLE = 4;

    protected String mDeviceName = null;
    protected String mDeviceAddr = null;
    protected String mDevicePassword = null;

    protected TouchScreenListener mTouchListener;
    protected Context mContext;
    protected BluetoothAdapter mBTAdapter;
    protected BluetoothDevice mDevice;

    protected JYDZ_Comm_Protocol mProtocol;
    protected ProtocolHandler mHandler;

    protected ConnectServiceBase(Context context, TouchScreenListener touchListener) {
        if (null == context || null == touchListener) {
            throw new IllegalArgumentException("ConnectServiceBase construct failed");
        }
        mContext = context;
        mTouchListener = touchListener;
    }

    public abstract int connect(String deviceName, String deviceAddr, String pwd);

    public abstract int disconnect();

    public abstract void write(byte[] data);
}
