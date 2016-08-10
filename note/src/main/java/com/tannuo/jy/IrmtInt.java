package com.tannuo.jy;

import android.util.Log;

import java.util.List;

public class IrmtInt implements IrmtInterface {
	private final String TAG = "TouchManager";

	@Override
	public void onSnapshot(int mSnapShot) {
		// TODO Auto-generated method stub
		Log.v(TAG,"mSnapShot " + mSnapShot);
	}

	@Override
	public void onError(int ErrorCode) {
		// TODO Auto-generated method stub
		if (ErrorCode > 0)
			Log.v(TAG, "Bluetooth Connect Error " + ErrorCode);
		else
			Log.v(TAG,"Data Transmission Error " + ErrorCode);
	}

//	@Override
//	public void onErrorDetect(int ErrorNum) {
//		// TODO Auto-generated method stub
//		Log.v(TAG,"onErrorDetect " + ErrorNum);
//	}

	@Override
	public void onBLconnected() {
		// TODO Auto-generated method stub
		Log.v(TAG, "Conn ");
	}

	@Override
	public void onGestureGet(int GestureNo) {
		// TODO Auto-generated method stub
		Log.v(TAG, "gesture "+GestureNo);
	}

	@Override
	public void onTouchUp(List<TouchScreen.TouchPoint> mTouchList) {
		for (TouchScreen.TouchPoint mTP:mTouchList){
			Log.v(TAG,"onTouchUp  "+mTP.pointId +": "+mTP.pointColor);
		}
	}

	@Override
	public void onTouchDown(List<TouchScreen.TouchPoint> mTouchList) {
		// TODO Auto-generated method stub
		for (TouchScreen.TouchPoint mTP:mTouchList){
			Log.v(TAG,"onTouchDown  "+mTP.pointId +":  "+mTP.pointStatus +" "+mTP.pointY);
		}
	}

	@Override
	public void onIdGet(long Id) {
		// TODO Auto-generated method stub
		Log.v(TAG,"Id"+Id);
	}


	@Override
	public void onTouchMove(List<TouchScreen.TouchPoint> mTouchList) {
		// TODO Auto-generated method stub
		for (TouchScreen.TouchPoint mTP:mTouchList){
			Log.v(TAG,"onTouchMove  "+mTP.pointId +":  "+mTP.pointStatus +" "+mTP.pointY);
		}
	}
}
