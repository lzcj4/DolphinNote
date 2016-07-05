package com.tannuo.sdk.device.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.protocol.IProtocol;
import com.tannuo.sdk.device.protocol.ProtocolHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bluetooth low energy connect service
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEDevice extends DeviceBase {

    private static final String UART_UUID = "0000f1f0-0000-1000-8000-00805f9b34fb";
    private static final String UART_UUID_TX = "0000f1f1-0000-1000-8000-00805f9b34fb";
    private static final String UART_UUID_RX = "0000f1f2-0000-1000-8000-00805f9b34fb";

    /**
     * Unit: ms
     */
    private static final int TIMER_INTERVAL = 10 * 1000;

    private String mUART_Uuid = UART_UUID;
    private String mUART_TX_Uuid = UART_UUID_TX;
    private String mUART_RX_Uuid = UART_UUID_RX;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mGattService;
    /**
     * Write only characteristic
     */
    private BluetoothGattCharacteristic mGattTXChara;
    /**
     * Read only characteristic
     */
    private BluetoothGattCharacteristic mGattRXChara;

    private boolean mIsGattConnected = false;

    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private BluetoothGattCallback mGattCallback;

    public BLEDevice(Context context, TouchDeviceListener listener, IProtocol protocol) {
        super(context, listener, protocol);
        mLeScanCallback = new LeScanCallbackImpl();
        mGattCallback = new BluetoothGattCallbackImpl();
    }

    @Override
    public int connect(String devName) {
        mDeviceName = devName;
        getBluetoothAdapter();
        if (null == mBTAdapter || !mBTAdapter.isEnabled()) {
            mDeviceListener.onError(BL_ERROR_NOT_ENABLE);
            return BL_STATE_BL_NOT_ENABLE;
        }

        mHandler.postDelayed(() -> {
            stopLeScan(mLeScanCallback);
            if (null == mDevice) {
                mDeviceListener.onError(BL_ERROR_DEV_NOT_FOUND);
            }
        }, TIMER_INTERVAL);

        startLeScan(mLeScanCallback);
        return BL_STATE_READY;
    }

    @Override
    public int disconnect() {
        disconnectGatt();
        mHandler.stop();
        return BL_STATE_READY;
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (null != mBTAdapter) {
            return mBTAdapter;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (null == bluetoothManager) {
            return null;
        }
        mBTAdapter = bluetoothManager.getAdapter();
        return mBTAdapter;
    }

    private void startLeScan(BluetoothAdapter.LeScanCallback callback) {
        if (null != mBTAdapter) {
            mBTAdapter.startLeScan(callback);
        }
    }

    private void stopLeScan(BluetoothAdapter.LeScanCallback callback) {
        if (null != mBTAdapter) {
            //mBTAdapter.stopLeScan(callback);
        }
    }

    private boolean connectGatt(BluetoothDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("device");
        }
        disconnectGatt();
//        BluetoothDevice device_tmp = mBTAdapter.getRemoteDevice(device.getAddress());
//        if (device_tmp == null) {
//            return false;
//        }

        mBluetoothGatt = device.connectGatt(mContext.getApplicationContext(), true, mGattCallback);
        // mIsGattConnected = true;
        return true;
    }

    private void disconnectGatt() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        mIsGattConnected = false;
    }

    @Override
    public void write(byte[] data) {
        if (null != mGattTXChara && null != mBluetoothGatt &&
                null != data) {
            mGattTXChara.setValue(data);
            mBluetoothGatt.writeCharacteristic(mGattTXChara);
        } else {
            //Log.v(TAG,"mRFCommTX_flag NOT SET");
        }
    }

    private class LeScanCallbackImpl implements BluetoothAdapter.LeScanCallback {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, String.format("BLE device scanned, name:%s , addr:%s", device.getName(), device.getAddress()));
            String name = device.getName();
            String addr = device.getAddress();
            if (!TextUtils.isEmpty(mDeviceName) && !TextUtils.isEmpty(name) && name.equalsIgnoreCase(mDeviceName)) {
                mDevice = device;
                stopLeScan(mLeScanCallback);
                connectGatt(device);
            }
        }
    }

    private class BluetoothGattCallbackImpl extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, String.format("++ BLE BluetoothGattCallback connection connected"));
                mIsGattConnected = true;
                mDeviceListener.onConnected();
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, String.format("-- BLE BluetoothGattCallback connection disconnected"));
                mIsGattConnected = false;
                mDeviceListener.onError(BL_ERROR_CONN_LOST);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                String uuid = service.getUuid().toString();
//                if (!uuid.equalsIgnoreCase(UART_UUID)) {
//                    continue;
//                }
                if (!uuid.equalsIgnoreCase("0000ffe0-0000-1000-8000-00805f9b34fb")) {
                    continue;
                }
                Log.d(TAG, String.format("-- BLE BluetoothGattCallback service discovered :%s", UART_UUID));
                mGattService = service;
                Map<String, String> grounp = new HashMap<String, String>();
                grounp.put("name", BLEGattAttributes.lookup(uuid, "unknow"));
                grounp.put("Uuid", uuid);
                List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();

                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if (uuid.equalsIgnoreCase(UART_UUID_TX)) {
                        mGattTXChara = gattCharacteristic;
                        Log.d(TAG, String.format("-- BLE BluetoothGattCallback write only characteristic found :%s", UART_UUID_TX));
                        // } else if (uuid.equalsIgnoreCase(UART_UUID_RX)) {
                    } else if (uuid.equalsIgnoreCase("0000ffe1-0000-1000-8000-00805f9b34fb")) {
                        int proper;
                        mGattRXChara = gattCharacteristic;
                        proper = mGattRXChara.getProperties();
                        Log.d(TAG, String.format("-- BLE BluetoothGattCallback read only characteristic found :%s", UART_UUID_RX));
                        if (0 != (proper &
                                (BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_INDICATE))) {
                            mBluetoothGatt.setCharacteristicNotification(mGattRXChara, true);
                            BluetoothGattDescriptor descriptor = mGattRXChara.getDescriptor(
                                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(descriptor);
                        }
                        //Log.v(TAG,"RX GET");
                    }
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] data = characteristic.getValue();
            mHandler.sendMessage(ProtocolHandler.MESSAGE_PROTOCOL_PARSE, data);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    }

}
