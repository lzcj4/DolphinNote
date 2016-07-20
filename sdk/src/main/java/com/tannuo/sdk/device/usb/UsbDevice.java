package com.tannuo.sdk.device.usb;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.bluetooth.DeviceBase;
import com.tannuo.sdk.device.protocol.IProtocol;
import com.tannuo.sdk.device.protocol.ProtocolHandler;
import com.tannuo.sdk.util.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Nick_PC on 2016/7/8.
 */
public class UsbDevice extends DeviceBase {

    private final int VID = 1423;
    private final int PID = 25479;

    //HID\VID_23C9&PID_5666&REV_0200&MI_02&Col01

    private UsbManager mUsbManager;
    private android.hardware.usb.UsbDevice mDevice;
    private DeviceThread thread;

    public UsbDevice(Context context, TouchDeviceListener listener, IProtocol protocol) {
        super(context, listener, protocol);
    }

    private void start() {
        this.mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, android.hardware.usb.UsbDevice> deviceMap = this.mUsbManager.getDeviceList();
        Iterator<android.hardware.usb.UsbDevice> deviceIterator = deviceMap.values().iterator();
        while (deviceIterator.hasNext()) {
            android.hardware.usb.UsbDevice item = deviceIterator.next();
            //if (item.getVendorId() == VID && item.getProductId() == PID)
            {
                mDevice = item;
                Logger.i(TAG, item.getDeviceName());
                break;
            }
        }

        if (mDevice != null) {
            if (mUsbManager.hasPermission(mDevice)) {
                startIO();
            } else {
                PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                mContext.registerReceiver(mUsbReceiver, filter);
                mUsbManager.requestPermission(mDevice, pi);
            }
        } else {
            mDeviceListener.onError(BL_ERROR_DEV_NOT_FOUND);
        }
    }

    private void startIO() {
        thread = new DeviceThread();
        thread.start();
    }

    private class DeviceThread extends Thread {
        private boolean isRunning = true;

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            int interfCount = mDevice.getInterfaceCount();
            UsbInterface inter = mDevice.getInterface(0);
            int endCount = inter.getEndpointCount();
            UsbEndpoint endPoint = null;
            for (int i = 0; i < endCount; i++) {
                UsbEndpoint item = inter.getEndpoint(i);
                if (item.getType() == UsbConstants.USB_ENDPOINT_XFER_INT &&
                        item.getDirection() == UsbConstants.USB_DIR_IN) {
                    endPoint = item;
                    break;
                }
            }
            if (null == endPoint) {
                Logger.e(TAG, "Can't get USB in endpoint ,usb device connect failed");
                return;
            }

            UsbDeviceConnection usbConn = mUsbManager.openDevice(mDevice);
            boolean isClaimInterf = usbConn.claimInterface(inter, true);
            if (!isClaimInterf) {
                Logger.e(TAG, "UsbDeviceConnection.claimInterface failed");
            }

            int len = endPoint.getMaxPacketSize();
            byte[] buffer = new byte[len];

            while (isClaimInterf && isRunning) {
                int readLen = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    readLen = usbConn.bulkTransfer(endPoint, buffer, 0, len, 5);
                } else {
                    readLen = usbConn.bulkTransfer(endPoint, buffer, len, 5);
                }
                if (readLen > 0) {
                    byte[] data = Arrays.copyOfRange(buffer, 0, readLen);
                    mHandler.sendMessage(ProtocolHandler.MESSAGE_PROTOCOL_PARSE, data);
                } else {
                    SystemClock.sleep(10);
                }
                Logger.i(TAG, String.format("read len:%s", readLen));
            }
        }

        public void cancel() {
            this.isRunning = false;
        }

    }

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    android.hardware.usb.UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            startIO();
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    public int connect(String name) {
        start();
        if (null != mDeviceListener) {
            mDeviceListener.onConnected();
        }
        return 0;
    }

    @Override
    public int disconnect() {
        if (null != thread) {
            thread.cancel();
            thread = null;
        }
        if (null != mDeviceListener) {
            //mDeviceListener.onDisconnected();
            mDeviceListener = null;
        }
        return 0;
    }

    @Override
    public void write(byte[] data) {

    }
}
