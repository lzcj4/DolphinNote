package com.tannuo.sdk.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Nick_PC on 2016/5/5.
 */
public class DataLog {

    private static final String TAG = "DataLog";
    private static final String DATA_FOLDER = "BTData";
    private static final String DATA_IN_FILE = "In.txt";
    private static final String DATA_OUT_FILE = "Out.txt";

    private OutputStreamWriter mInWriter;
    private OutputStreamWriter mOutWriter;

    private static DataLog instance;

    public DataLog()  {
        createFiles();
        instance = this;
    }

    private void createFiles(){
        try {
            File dataFolder = getDataFolder();
            if (null != dataFolder) {
                mInWriter = createDataFile(dataFolder, DATA_IN_FILE);
                mOutWriter = createDataFile(dataFolder, DATA_OUT_FILE);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    private static File getDataFolder() throws  IllegalAccessException{
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new IllegalAccessException("External storage is not mounted");
        }
        File result = null;
        File rootFolder = Environment.getExternalStorageDirectory();
        if (null != rootFolder && rootFolder.exists()) {
            File dataFolder = new File(rootFolder, String.format("/%s/", DATA_FOLDER));
            if (!dataFolder.exists() && dataFolder.mkdirs())
                result = dataFolder;
        }
        return result;
    }


    public static void clear() {
        if (instance == null) {
            return;
        }

        instance.close();
        instance.createFiles();
    }

    private static OutputStreamWriter createDataFile(File parentFolder, String fileName) {
        OutputStreamWriter result = null;
        File file = new File(parentFolder, String.format("/%s", fileName));
        if (file.exists()) {
            file.delete();
            Log.i(DataLog.TAG, String.format("--- file :%s/%s deleted ---", parentFolder.getAbsolutePath(), fileName));
        }
        try {
            if (file.createNewFile()) {
                result = new OutputStreamWriter(new FileOutputStream(file));
                Log.i(DataLog.TAG, String.format("+++ new file :%s/%s created +++", parentFolder.getAbsolutePath(), fileName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void write(OutputStreamWriter writer, String data) {
        if (writer != null) {
            try {
                writer.append(data);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void write(OutputStreamWriter writer, byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte item : data) {
            sb.append(String.format("%02X ", item));
        }
        //sb.append("\r\n");
        write(writer, sb.toString());
    }

    public void writeInData(String data) {
        write(mInWriter, data);
    }

    public void writeInData(byte[] data) {
        write(mInWriter, data);
    }

    public void writeOutData(String data) {
        write(mOutWriter, data);
    }

    public void writeOutData(byte[] data) {
        write(mOutWriter, data);
    }

    public void close() {
        if (null != mInWriter) {
            try {
                mInWriter.close();
                mInWriter = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (null != mOutWriter) {
            try {
                mOutWriter.close();
                mOutWriter = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


