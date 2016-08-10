package com.tannuo.jy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/11/3.
 */

public class TouchScreen {
    public static final int PAINTMODE_DOT_1 = 0;
    public static final int PAINTMODE_DOT_2 = 1;
    public static final int PAINTMODE_LINE_1 = 2;
    public static final int PAINTMODE_LINE_2 = 3;

    public static final int PATH_BUF_MAX_LEN = 100;

    public static final int POINT_BLACK = 1;
    public static final int POINT_RED = 2;
    public static final int POINT_WHITE = 3;
    
    public static final int IR_TOUCH_X_MAX = 32767;
    public static final int IR_TOUCH_Y_MAX = 32767;
    
    public static final int POINT_STATUS_UP  = 4;
    public static final int POINT_STATUS_DOWN  = 7;

    public IrTouch mIrTouch=new IrTouch();

    //瑙︽懜灞忓疄闄呭弬鏁
    public List<TouchPoint> mTouchUpList   = new ArrayList<TouchPoint>();
    public List<TouchPoint> mTouchDownList = new ArrayList<TouchPoint>();
    public List<TouchPoint> mTouchMoveList = new ArrayList<TouchPoint>();
    
    private static HashMap<Integer, TouchPoint> mTouchUpMap = new HashMap<Integer, TouchPoint>();
    private static HashMap<Integer, TouchPoint> mTouchDownMap = new HashMap<Integer, TouchPoint>();
    
    //屏幕的属性
    public int mPaintMode = PAINTMODE_DOT_1;
	public int mGuesture;
	public long TouchScreenID;
	public int mNumOfPoints;
	public int mSnapShot;

    public  TouchScreen() {}
    //
    void SetPoint(int Points,int []dataBuffer){
        float area =0;
        TouchPoint mTouchPoint ;
        for (int kk = 0; kk <Points; kk++) {
        	mTouchPoint = new TouchPoint();
        	mTouchPoint.pointStatus = (byte)dataBuffer[kk * 10];
            mTouchPoint.pointId     = (byte)dataBuffer[kk * 10 + 1];
            mTouchPoint.pointX      = dataBuffer[kk * 10 + 2] + dataBuffer[kk * 10 + 3] * 256;
            mTouchPoint.pointY      = dataBuffer[kk * 10 + 4] + dataBuffer[kk * 10 + 5] * 256;
            mTouchPoint.pointWidth  = dataBuffer[kk * 10 + 6] + dataBuffer[kk * 10 + 7] * 256;
            mTouchPoint.pointHeight = dataBuffer[kk * 10 + 8] + dataBuffer[kk * 10 + 9] * 256;
            mTouchPoint.pointArea   = mTouchPoint.pointWidth*mTouchPoint.pointHeight;
            
            area = mTouchPoint.pointArea;
            if((area>8000)&&(area<20000)) {
            	mTouchPoint.pointColor = POINT_RED;
            }else if(area>=20000) {
            	mTouchPoint.pointColor = POINT_WHITE;
            }else
            	mTouchPoint.pointColor = POINT_BLACK;
            
            if(mTouchPoint.pointStatus==POINT_STATUS_DOWN)
            	mTouchDownList.add(mTouchPoint);
            else
            	mTouchUpList.add(mTouchPoint);
           }
    }
    //
    void SetIrTouchFeature(int []dataBuffer){
    	mIrTouch.mScreenXLED = dataBuffer[0]+dataBuffer[1]*256;
    	mIrTouch.mScreenYLED = dataBuffer[2]+dataBuffer[3]*256;
    	mIrTouch.mScreenLedInsert = dataBuffer[4];
    	mIrTouch.mScreenLedDistance = dataBuffer[5]+dataBuffer[6]*256;
    	mIrTouch.mScreenMaxPoint =dataBuffer[7];
    	mIrTouch.mScreenFrameRate =dataBuffer[8];
    }
    //
    void setPaintMode(int DrawMode){
        if(DrawMode == PAINTMODE_DOT_1)
            mPaintMode = PAINTMODE_DOT_1;
        else if(DrawMode == PAINTMODE_LINE_1)
            mPaintMode = PAINTMODE_LINE_1;
    }
    //
    int getPaintMode(){
        return  mPaintMode;
    }
    //
    void setmScreenWidth(float Width){
        //mScreenWidth = Width;
    }
    float getmScreenWidth(){
        return 0;
    }
    void setmScreenHeight(float Height){
        //mScreenHeight = Height;
    }
    public float getmScreenHeight(){
        return 0;
    }
    void setNumOfPoints(int NewNumOfPoints){
        mNumOfPoints = NewNumOfPoints;
    }
    void setmGuesture(int newGuesture){
        mGuesture = newGuesture;
    }
    void setID(long NewID){
    	TouchScreenID = NewID;
    }
    void setSnapShot(int Shot){
        mSnapShot = Shot;
    }
    
    public class TouchPoint {
    	public byte pointId; 
    	public byte pointStatus; 
    	public int pointX; 
    	public int pointY; 
    	public int pointWidth; 
    	public int pointHeight; 
    	public int pointArea;
    	public byte pointColor;
    }
    
    public class IrTouch {
    	public int mScreenXLED;
    	public int mScreenYLED ;
    	public int mScreenLedInsert;
    	public int mScreenLedDistance;
    	public int mScreenMaxPoint ;
    	public int mScreenFrameRate ;
    }
}
