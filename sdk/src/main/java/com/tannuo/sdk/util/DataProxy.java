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
public class DataProxy {

    private static final String TAG = "DataProxy";
    private static final String DATA_FOLDER = "BTData";
    private static final String DATA_IN_FILE = "In.txt";
    private static final String DATA_OUT_FILE = "Out.txt";

    private OutputStreamWriter mInWriter;
    private OutputStreamWriter mOutWriter;

    private static DataProxy instance;

    public DataProxy() throws IllegalAccessException {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new IllegalAccessException("External storage is not mounted");
        }
        File rootFolder = Environment.getExternalStorageDirectory();
        if (null != rootFolder && rootFolder.exists()) {
            File dataFolder = new File(rootFolder, String.format("/%s/", DATA_FOLDER));
            boolean isFolderExisted = dataFolder.exists();
            if (!dataFolder.exists()) {
                isFolderExisted = dataFolder.mkdirs();
            }
            if (isFolderExisted) {
                mInWriter = createDataFile(dataFolder, DATA_IN_FILE);
                mOutWriter = createDataFile(dataFolder, DATA_OUT_FILE);
            }
        }
        instance = this;
    }

    public static void clear() {
        if (instance != null) {
            try {
                if (null != instance.mInWriter) {
                    instance.mInWriter.close();
                }
                File rootFolder = Environment.getExternalStorageDirectory();
                if (null != rootFolder && rootFolder.exists()) {
                    File dataFolder = new File(rootFolder, String.format("/%s/", DATA_FOLDER));
                    boolean isFolderExisted = dataFolder.exists();
                    if (!dataFolder.exists()) {
                        isFolderExisted = dataFolder.mkdirs();
                    }
                    if (isFolderExisted) {
                        File file = new File(dataFolder, String.format("/%s", DATA_IN_FILE));
                        if (file.exists()) {
                            file.delete();
                        }
                        instance.mInWriter = createDataFile(dataFolder, DATA_IN_FILE);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static OutputStreamWriter createDataFile(File parentFolder, String fileName) {
        OutputStreamWriter result = null;
        File file = new File(parentFolder, String.format("/%s", fileName));
        if (file.exists()) {
            file.delete();
            Log.i(DataProxy.TAG, String.format("--- file :%s/%s deleted ---", parentFolder.getAbsolutePath(), fileName));
        }
        try {
            if (file.createNewFile()) {
                result = new OutputStreamWriter(new FileOutputStream(file));
                Log.i(DataProxy.TAG, String.format("+++ new file :%s/%s created +++", parentFolder.getAbsolutePath(), fileName));
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


