package com.tannuo.sdk.device.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLCommService {
    public enum State {
        WRITEREADY, IDLE, STARTWRITE, STARTDRAW, STARTLOAD
    }
	public static final int REQUEST_ENABLE_BT = 3;
	public  BluetoothManager mBluetoothManager;
	public  BluetoothAdapter mBluetoothAdapter;
	public  BluetoothGatt mBluetoothGatt;
	private  BluetoothGattService mRFCommService;
	private  BluetoothGattCharacteristic mRFCommTXCharacteristic;
	private  BluetoothGattCharacteristic mRFCommRXCharacteristic;
    private  Activity inerActivity = null;

    private  String DeviceName = null;
    private  String DeviceAddr = null;
    private  String DevicePassword = null;
    private String[] DeviceUuid;
    
	private boolean connect_flag = false;
	private boolean device_found_flag = false;
	private boolean discover_flag = false;
	private boolean mRFComm_flag = false;
	private boolean mRFCommTX_flag = false;
	private boolean mRFCommRX_flag = false;

	private boolean read_name_flag = false;
	private boolean bind_flag = false;
	private boolean exit_activity = false;
	DataProcessThread mDataProcessThread = new DataProcessThread();


	public int errorDetect = 0;
	public int errorNum = 0;
    public static State mState = State.IDLE;


    class LeScanTimerTask extends TimerTask{
		@Override
		public void run() {
			if(discover_flag==true){
				if(mBluetoothAdapter!=null){
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}
			discover_flag = false;
			device_found_flag = false;
		}
	}
	Timer leScanTimer = null;  
	LeScanTimerTask leScanTask = null;
	
	public BLCommService(){
    	
    	mDataProcessThread.start();
        
        initBle();
	}

    public int userConnect( String newDevName, String DevAddr,String DevPassWord, String[] uuidStr){
        DeviceName = newDevName;
        DeviceUuid = uuidStr;
        Log.v(TAG, newDevName+"");
        DeviceAddr = DevAddr;
        DevicePassword = DevPassWord;
        if(mBluetoothAdapter.isEnabled()){
            if(connect_flag == true){
                disConnectBle();
                connect_flag = false ;
            }
            setUpTimer();
            if(discover_flag == true){
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            return BL_STATE_READY;
        }else{
            return BL_STATE_BL_NOT_ENABLE;
        }
    }
    
	private void setUpTimer(){
		if(leScanTimer != null){
			leScanTimer.cancel();
			leScanTimer.purge();
		}
		if(leScanTask != null){
			leScanTask.cancel();
		}
		leScanTimer = new Timer();
		leScanTask = new LeScanTimerTask();
		leScanTimer.schedule(leScanTask, 10000);
		discover_flag  = true;
	}
	
    public void userEnableBL(){
    	if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            inerActivity.startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
    }

	public int UserDisconn(){
		disConnectBle();
		connect_flag = false;
		return BL_STATE_READY;
	}


	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			Log.v(TAG, "DEVICE GET!" + device.getName() );
			if(device.getName() != null) {
				if (device.getName().equalsIgnoreCase(DeviceName)) {
					if (discover_flag == true) {
						device_found_flag = true;
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
						discover_flag = false;
						connectBle(device);
					}
				}
			}
		}
	};



	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			super.onConnectionStateChange(gatt, status, newState);
			if (newState == BluetoothProfile.STATE_CONNECTED) { // ���ӳɹ�
				connect_flag = true;
				mBluetoothGatt.discoverServices();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) { // �Ͽ�����
				connect_flag = false;
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.v(TAG, "onServicesDiscovered");
				List<BluetoothGattService> services = gatt.getServices();
				Log.v(TAG,"services.size-->"+services.size());
				String uuid;
				
				for (BluetoothGattService service : services) {
					uuid = service.getUuid().toString();
					
					if(!uuid.equalsIgnoreCase(DeviceUuid[0])){
						continue;
					}
					mRFCommService = service;
					mRFComm_flag = true;
					List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
		
					for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
						uuid = gattCharacteristic.getUuid().toString();
						if(uuid.equalsIgnoreCase(DeviceUuid[1])){
							int proper;
							mRFCommTXCharacteristic = gattCharacteristic;
							proper = mRFCommTXCharacteristic.getProperties();
							mRFCommTX_flag = true;
							Log.v(TAG,"TX GET"+proper);
						}else if(uuid.equalsIgnoreCase(DeviceUuid[2])){
							int proper = 0; // ͨ��Ȩ��
							mRFCommRXCharacteristic = gattCharacteristic;
							proper = mRFCommRXCharacteristic.getProperties();
							Log.v(TAG,"RX GET"+proper);

							if ((0 != (proper & BluetoothGattCharacteristic.PROPERTY_NOTIFY))
									|| (0 != (proper & BluetoothGattCharacteristic.PROPERTY_INDICATE))) { //
								mBluetoothGatt.setCharacteristicNotification(mRFCommRXCharacteristic, true);

								BluetoothGattDescriptor descriptor = mRFCommRXCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
								descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
								mBluetoothGatt.writeDescriptor(descriptor);

							}
//							try {
//								Thread.sleep(200);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
							Log.v(TAG,"RX GET");
							mRFCommRX_flag = true;

						}
					}
				}
			}
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			super.onDescriptorRead(gatt, descriptor, status);
			read_name_flag = true;
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

            mHandler.obtainMessage(MSG_CharacteristicChanged, characteristic.getValue()).sendToTarget();//������Ϣ��CustomThreadʵ��
		}
		
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);

		}
	};


	// ��ʼ��BLE
	public boolean initBle() {
		mBluetoothManager = (BluetoothManager) inerActivity.getSystemService(Context.BLUETOOTH_SERVICE);

		if (null == mBluetoothManager) {
			return false;
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (null == mBluetoothAdapter) {
			return false;
		}
		return true;
	}

	// ɨ��
	public void scanBle(BluetoothAdapter.LeScanCallback callback) {
		mBluetoothAdapter.startLeScan(callback);
	}

	// ֹͣɨ��
	public void stopscanBle(BluetoothAdapter.LeScanCallback callback) {
		mBluetoothAdapter.stopLeScan(callback);
	}

	// ��������
	public boolean connectBle(BluetoothDevice mBluetoothDevice) {
		disConnectBle();
		connect_flag = false;
		BluetoothDevice device_tmp = mBluetoothAdapter.getRemoteDevice(mBluetoothDevice.getAddress());
		if(device_tmp == null){
			//Log.v(TAG,"device ������");
			return false;
		}
		mBluetoothGatt = device_tmp.connectGatt(inerActivity.getApplicationContext(), true, mGattCallback);
		connect_flag = true;
		return true;
	}

	// �ر�����
	public void disConnectBle(){
		if(mBluetoothGatt != null){
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			mBluetoothGatt = null;
			connect_flag = false;
			mRFCommTX_flag = false;
			mRFCommRX_flag = false;
			mRFComm_flag = false;			
		}
	}
	
	// ����Ƿ�����
	public boolean isConnected()
	{
		return connect_flag;
	}
	
	public void bleWrite(byte[] sendmsg){
		if(mRFCommTX_flag){
			mRFCommTXCharacteristic.setValue(sendmsg);
			mBluetoothGatt.writeCharacteristic(mRFCommTXCharacteristic);
		}else {
			//Log.v(TAG,"mRFCommTX_flag NOT SET");
		}
	}
	
	private Handler mHandler;
	class DataProcessThread extends Thread {
		@Override
	    public void run() {

			Looper.prepare();// start Looper
	        mHandler = new Handler(){
	        public void handleMessage (Message msg) {

	        	switch(msg.what) {
	        		case MSG_CharacteristicChanged:	        			
	        			byte [] buffer = (byte [])msg.obj;
	                    int bytes = buffer.length;
						break;
	        		default :
	        		break;
	        	}
	        }
	    };
	    Looper.loop();//4��������Ϣѭ��
	    }
	}
	
    private static final String TAG = "BLService";
	public final static String ACTION_DATA_CHANGE = "com.example.bluetooth.le.ACTION_DATA_CHANGE";
	public final static String ACTION_RSSI_READ = "com.example.bluetooth.le.ACTION_RSSI_READ";
	public final static String ACTION_STATE_CONNECTED = "com.example.bluetooth.le.ACTION_STATE_CONNECTED";
	public final static String ACTION_STATE_DISCONNECTED = "com.example.bluetooth.le.ACTION_STATE_DISCONNECTED";
	public final static String ACTION_WRITE_OVER = "com.example.bluetooth.le.ACTION_WRITE_OVER";
	public final static String ACTION_READ_OVER = "com.example.bluetooth.le.ACTION_READ_OVER";
	public final static String ACTION_READ_Descriptor_OVER = "com.example.bluetooth.le.ACTION_READ_Descriptor_OVER";
	public final static String ACTION_WRITE_Descriptor_OVER = "com.example.bluetooth.le.ACTION_WRITE_Descriptor_OVER";
	public final static String ACTION_ServicesDiscovered_OVER = "com.example.bluetooth.le.ACTION_ServicesDiscovered_OVER";

	protected static final int MSG_CharacteristicChanged = 1;
	
    public static final int BL_STATE_NONE = 0;       // we're doing nothing
    public static final int BL_STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int BL_STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int BL_STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int BL_STATE_READY = 4;  // now connected to a remote device
    public static final int BL_STATE_BL_NOT_ENABLE = -1;  // now connected to a remote device
    
    public static final int BL_ERROR_NONE = 0;
    public static final int BL_ERROR_CONN_FAILED = 1;
    public static final int BL_ERROR_CONN_LOST = 2;
    public static final int BL_ERROR_DEV_NOT_FOUND = 3;
    public static final int BL_ERROR_NOT_ENABLE = 4;

	// MTSeriBle4E
    public static final String UART_UUID = "0000f1f0-0000-1000-8000-00805f9b34fb";
    public static final String UART_UUID_TX = "0000f1f1-0000-1000-8000-00805f9b34fb";
    public static final String UART_UUID_RX = "0000f1f2-0000-1000-8000-00805f9b34fb";
	// Feasycom
//	public static final String UART_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
//	public static final String UART_UUID_TX = "0000fff2-0000-1000-8000-00805f9b34fb";
//	public static final String UART_UUID_RX = "0000fff1-0000-1000-8000-00805f9b34fb";
}
