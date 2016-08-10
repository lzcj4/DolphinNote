package com.tannuo.jy;


import java.io.IOException;

//import android.util.Log;

/**
 * Created by Administrator on 2015/10/30.
 */
public class JYDZ_Comm_Protocol {
    public static final int COMM_STATUS_GET_HEADER = 0;
    public static final int COMM_STATUS_GET_LENGTH = 1;
    public static final int COMM_STATUS_GET_FEATURE = 2;
    public static final int COMM_STATUS_GET_DATA = 3;
    public static final int COMM_STATUS_GET_CHECKSUM = 4;
    public static final int COMM_STATUS_CHANGE_FORMAT = 5;
    public static final int COMM_STATUS_DATA_GET_OK = 6;
    public static final int COMM_STATUS_GESTURE_GET = 7;
    public static final int COMM_STATUS_SNAPSHOT_GET = 8;
    public static final int COMM_STATUS_IDENTI_GET = 9;
    public static final int COMM_STATUS_SCREENFEATURE_GET = 10;
    
    public static final int COMM_STATUS_ERROR = -1;
    public static final int COMM_STATUS_GET_LENGTH_ERROR = -2;
    public static final int COMM_STATUS_GET_FEATURE_ERROR = -3;
    public static final int COMM_STATUS_GET_CHECKSUM_ERROR = -4;

    public static final int DATAFEATURE_00 = 0;
    public static final int DATAFEATURE_01 = 1;
    public static final int DATAFEATURE_02 = 2;
    public static final int SCREENFEATURE = 0X60;
    public static final int GESTURE = 0X70;
    public static final int CONTROLCODE = 0X80;
    public static final int PACKAGE_TRANSCMD = 0X81;
    public static final int CONTROLCODE_USB =1;
    public static final int CONTROLCODE_NUSB =0;
    
    public static final int SNAPSHOT = 0X71;
    public static final int IDENTI   = 0X73;

    public static final byte[] PACKAGE_CONTROLCODE_USB={0x68,3,-128, 0,-21};
    public static final byte[] PACKAGE_CONTROLCODE_NUSB={0x68,3,-128, -86,-107}; 
    public static final byte[] PACKAGE_TRANSCMD_BYTE00={0x68,3,-127, 0,-20};
    public static final byte[] PACKAGE_TRANSCMD_BYTE01={0x68,3,-127, 1,-19};
    public static final byte[] PACKAGE_TRANSCMD_BYTE02={0x68,3,-127, 2,-18};

    public static final String TAG="JY_PROTOCOL";

    public static final int MESSAGE_UART_CMD_GET = 100;

    public static final int JYDZ_PROTOCOL_HEADER =0x68;
    public static final int JYDZ_PROTOCOL_MAX_LENGTH =40;

    public static final int COMM_CMD_OK =1;
    public static final int COMM_CMD_FALSE =0;

    private int commStatus;
    private int commLastStatus;
    private int commLength ;
    public int commCmdType ;
    private int commCmdState;
    private int commdataFeatrue;
    private int commControlCode;

    private int commDataCtr;
    private int commCheckSum; 
    public   int mPoints=1;
    public int[] dataBuffer;
    private TouchScreen JY_TouchScreen ;
    private DataFile mDataFile;




    public JYDZ_Comm_Protocol(TouchScreen TouchScreen){
        JY_TouchScreen = TouchScreen;
        commStatus = COMM_STATUS_GET_HEADER;
        commLength = 0;
        commCmdType = DATAFEATURE_00;
        commCmdState = COMM_CMD_FALSE;
        commDataCtr=0;
        dataBuffer = new int[400];
        mPoints=0;
        commLastStatus = COMM_STATUS_GET_HEADER;
        commdataFeatrue = DATAFEATURE_00;
        commControlCode = CONTROLCODE_NUSB;

    }

    private boolean CheckSum(){
        int mSum=0,Length = commLength-2,ii;
        mSum = commLength+commCmdType+JYDZ_PROTOCOL_HEADER;
        for(ii=0;ii<Length;ii++)
        {
            mSum+=dataBuffer[ii];
        }
        mSum &=0xFF;
        if(commCheckSum==mSum)
            return true;
        else
            return false;
    }

    private int CalcuCheckSum(int []buff,int size){
        int mSum=0,ii;
        for(ii=0;ii<size;ii++)
        {
            mSum+=buff[ii];
        }
        mSum &=0xFF;
        return mSum;
    }

    private boolean lengthCheck(){
        int mLength=0;
        switch (commCmdType)
        {
            case DATAFEATURE_00 :
                mLength=2+mPoints*5;
                break;
            case DATAFEATURE_01 :
                mLength=2+mPoints*6;
                break;
            case DATAFEATURE_02 :
                mLength=2+mPoints*10;
                break;
            case SCREENFEATURE :
                mLength=11;
                break;
            case GESTURE :
                mLength=3;
                break;
                
            case SNAPSHOT :
                mLength=3;
                break;
            case IDENTI :
                mLength=6;
                break;
            default:
                mLength =-1;
                break;
        }
        if(mLength==commLength)
            return true;
        else
            return false;
    }
    void resetProtocol(){
        commStatus = JYDZ_Comm_Protocol.COMM_STATUS_GET_HEADER;
        commLastStatus = JYDZ_Comm_Protocol.COMM_STATUS_GET_HEADER;
        commDataCtr = 0;
        commCmdState = JYDZ_Comm_Protocol.COMM_CMD_FALSE;
    }

    byte[] ChangeDataFeatrue() {
        if(commdataFeatrue==DATAFEATURE_00) {
            commdataFeatrue = DATAFEATURE_01;
            return PACKAGE_TRANSCMD_BYTE01;
        }
        else if(commdataFeatrue==DATAFEATURE_01){
            commdataFeatrue = DATAFEATURE_02;
            return PACKAGE_TRANSCMD_BYTE02;
        }
        else if(commdataFeatrue==DATAFEATURE_02){
            commdataFeatrue = DATAFEATURE_00;
            return PACKAGE_TRANSCMD_BYTE00;
        }
        else
            return null;
    }

    private byte[] GetControlCode() {
        if(commControlCode==CONTROLCODE_USB) {
            commControlCode = CONTROLCODE_NUSB;
            return PACKAGE_CONTROLCODE_NUSB;
        }
        else if(commControlCode==CONTROLCODE_NUSB){
            commControlCode = CONTROLCODE_USB;
            return PACKAGE_CONTROLCODE_USB;
        }
        else
            return null;
    }

    private int calcumPoints(){
        if(commCmdType==DATAFEATURE_00) {
            mPoints = (commLength-2)/5;
        }
        else if(commCmdType==DATAFEATURE_01){
            mPoints = (commLength-2)/6;
        }
        else if(commCmdType==DATAFEATURE_02){
            mPoints = (commLength-2)/10;
        }
        else mPoints = 0;
        JY_TouchScreen.setNumOfPoints(mPoints);
        return mPoints;
    }
    int ModeSelection(BLCommService.State mode, int NumOfBytes, byte []BT_DataBuf) throws IOException{
        if (mode == BLCommService.State.STARTWRITE){
            return handleAndStoreIncomeData( NumOfBytes, BT_DataBuf);
        }else {
            return handlerIncomeData(NumOfBytes, BT_DataBuf);
        }
    }

    int handleAndStoreIncomeData(int NumOfBytes,byte []BT_DataBuf) throws IOException{
        mDataFile = DataFile.getDataFile();
        mDataFile.WriteTxtFile(BT_DataBuf);
        return handlerIncomeData( NumOfBytes, BT_DataBuf);
    }
    int LoadAndHandleIncomeData (int NumOfBytes,byte []DataBuf){
        mDataFile = DataFile.getDataFile();
        return handlerIncomeData(NumOfBytes, DataBuf);
    }

    int handlerIncomeData(int NumOfBytes,byte []BT_DataBuf)  {
        int ii=0,errorcode=0;
        int[] readNum=new int[NumOfBytes];
        for (ii=0;ii<NumOfBytes;ii++) {
            readNum[ii] = BT_DataBuf[ii] & 0xFF;
            //Log.v(TAG, "Raw Data" + readNum[ii]);
                switch (commStatus) {
                    case JYDZ_Comm_Protocol.COMM_STATUS_GET_HEADER:
                        if (readNum[ii] == JYDZ_Comm_Protocol.JYDZ_PROTOCOL_HEADER)
                            commStatus = JYDZ_Comm_Protocol.COMM_STATUS_GET_LENGTH;
                        //Log.v(TAG, "head" + readNum[ii]);
                        break;

                    case JYDZ_Comm_Protocol.COMM_STATUS_GET_LENGTH:
                        commLength = readNum[ii];
                        if ((readNum[ii] >= 2)) {
                            commLastStatus = commStatus;
                            commStatus = JYDZ_Comm_Protocol.COMM_STATUS_GET_FEATURE;
                            //Log.v(TAG,"COMM_STATUS_GET_LENGTH"+readNum[ii]);
                        } else {
                            errorcode = COMM_STATUS_GET_LENGTH_ERROR;
                            commLastStatus = commStatus;
                            commStatus = JYDZ_Comm_Protocol.COMM_STATUS_ERROR;
                        }
                        break;


                    case JYDZ_Comm_Protocol.COMM_STATUS_GET_FEATURE:
                        commCmdType = readNum[ii];
                        calcumPoints();
                        if (lengthCheck()) {
                            if(commLength==2) {
                                commLastStatus = commStatus;
                                commStatus = JYDZ_Comm_Protocol.COMM_STATUS_GET_CHECKSUM;
                            }
                            else{
                                commLastStatus = commStatus;
                                commStatus = JYDZ_Comm_Protocol.COMM_STATUS_GET_DATA;
                                //Log.v(TAG,"COMM_STATUS_GET_FEATURE "+readNum[ii]);
                            }
                        }
                        else {
                            errorcode = COMM_STATUS_GET_FEATURE_ERROR;
                            commLastStatus = commStatus;
                            commStatus = JYDZ_Comm_Protocol.COMM_STATUS_ERROR;
                            //Log.v(TAG,"COMM_STATUS_GET_FEATURE"+readNum[ii]);
                        }
                        break;

                    case JYDZ_Comm_Protocol.COMM_STATUS_GET_DATA:

                        dataBuffer[commDataCtr++] = readNum[ii];

                        if (commDataCtr >= commLength - 2) {
                            commLastStatus = commStatus;
                            commStatus = JYDZ_Comm_Protocol.COMM_STATUS_GET_CHECKSUM;
                            //Log.v(TAG,"COMM_STATUS_GET_DATA   "+readNum[ii]);
                        }
                        break;

                    case JYDZ_Comm_Protocol.COMM_STATUS_GET_CHECKSUM:
                        commCheckSum = readNum[ii];
                        if (CheckSum()) {
                            commCmdState = JYDZ_Comm_Protocol.COMM_CMD_OK;
                            commLastStatus = commStatus;
                            commStatus = JYDZ_Comm_Protocol.COMM_STATUS_GET_HEADER;
                            //Log.v(TAG,"COMM_STATUS_GET_CHECKSUM" +readNum[ii]);
                        } else {
                            errorcode = COMM_STATUS_GET_CHECKSUM_ERROR;
                            commLastStatus = commStatus;
                            commStatus = JYDZ_Comm_Protocol.COMM_STATUS_ERROR;
                        }
                        break;
                    default:
                        break;
                }
                if (commStatus == JYDZ_Comm_Protocol.COMM_STATUS_ERROR) {
                    resetProtocol();
                }
                if (commCmdState == JYDZ_Comm_Protocol.COMM_CMD_OK) {
                    resetProtocol();
                    switch (commCmdType) {
                    case JYDZ_Comm_Protocol.DATAFEATURE_00:
                        errorcode = COMM_STATUS_CHANGE_FORMAT;
                        //Log.v(TAG, "DATA0");
                        break;
                    case JYDZ_Comm_Protocol.DATAFEATURE_01:
                        errorcode = COMM_STATUS_CHANGE_FORMAT;
                        //Log.v(TAG, "DATA1");
                        break;
                    case JYDZ_Comm_Protocol.DATAFEATURE_02:
                        JY_TouchScreen.SetPoint(mPoints, dataBuffer);
                        errorcode = COMM_STATUS_DATA_GET_OK;
                        //Log.v(TAG, "DATA2");
                        break;
                    case JYDZ_Comm_Protocol.SCREENFEATURE:
                        JY_TouchScreen.SetIrTouchFeature(dataBuffer);
                        errorcode = COMM_STATUS_SCREENFEATURE_GET;
                        //Log.v(TAG, "FEATRUE");
                        break;
                    case JYDZ_Comm_Protocol.GESTURE:
                        JY_TouchScreen.setmGuesture(dataBuffer[0]);
                        errorcode = COMM_STATUS_GESTURE_GET;
                        //Log.v(TAG, "ScreenGes");
                        break;
                    case JYDZ_Comm_Protocol.SNAPSHOT:
                    	JY_TouchScreen.setSnapShot(dataBuffer[0]);
                        errorcode = COMM_STATUS_SNAPSHOT_GET;
                        //Log.v(TAG, "SNAPSHOT");
                        break;
                    case JYDZ_Comm_Protocol.IDENTI:
                    	JY_TouchScreen.setID(dataBuffer[0]+dataBuffer[1]*256 +dataBuffer[2]*256*256 +dataBuffer[3]*256*256*256);
                        errorcode = COMM_STATUS_IDENTI_GET;
                        //Log.v(TAG, "IDENTI");
                        break;
                    default:
                        break;
                    }
                }

        }
        return  errorcode;
    }
    
    public boolean getCommStatusChanged(){
	return commLastStatus==commStatus;
}

}