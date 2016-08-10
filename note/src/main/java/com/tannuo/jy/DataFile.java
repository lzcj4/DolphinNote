package com.tannuo.jy;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Allen on 16-07-01.
 */
public class DataFile {
    public String rootdir;
    public String filedir;
    public String filepath;
    public FileOutputStream fos;
    public FileInputStream fis;
    public File file;
    public String rootdirectory = "/精研电子/";
    public String defaultdirectory = "/精研电子/test.txt";
    public String loaddirectory = null;
    public byte[] buffer;
    private static DataFile myDataFile = new DataFile();
    public DataFile(){
        this.rootdir = Environment.getExternalStorageDirectory().toString();
    }
    public static DataFile getDataFile() {
        return myDataFile;
    }

    public void CreateTxtFile(String path) {
        filedir = path;
        filepath = rootdir + filedir;
        file = new File(filepath);
        if(file.exists()){
            Log.d("File Create", "File Exists");
            if(file.isFile()) {
                file.delete();
                file = new File(filepath);
            }
        }else{
            Log.d("File Create", "New File is Created at" + filepath);
        }
    }

    public void OpenTxtFile() throws IOException{
        fos = new FileOutputStream(file);
    }
    public void CloseTxtFile() throws IOException{
        fos.close();
    }
    public void WriteTxtFile(byte[] mbytes) throws IOException{
        fos.write(mbytes);
    }
    public void ReadTxtFile(String path) throws IOException {
        if (path == null){
            Log.d("ReadPath", " NULL pointer error");
        }
        try {
            fis = new FileInputStream(path);
            int length = fis.available();
            buffer = new byte[length];
            fis.read(buffer);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


