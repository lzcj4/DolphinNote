package com.tannuo.sdk;

import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.protocol.BTProtocol;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Nick_PC on 2016/5/20.
 */
public class ProtocolParseTest {
    private BTProtocol mProtocol;
    private BufferedReader mReader;

    @Before
    public void initial() {

        File file = new File("d:/In_Line.txt");
        if (file.exists()) {
            try {
                mReader = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        TouchScreen mScreen = new TouchScreen(0, 0);
        mProtocol = new BTProtocol(mScreen);
    }

    @Test
    public void testParse() {
//        for (int i = 0; i < data.length; i++) {
//            mProtocol.handlerIncomeData(data[i].length, data[i]);
//        }

        //   mProtocol.createFileWriter();
        while (true) {
            try {
                String line = mReader.readLine();
                if (null == line) {
                    break;
                }

                String[] items = line.split(" ");
                int len = items.length;
                byte[] data = new byte[len];
                for (int i = 0; i < len; i++) {
                    String hexValue = "0x" + items[i];
                    int intValue = Integer.decode(hexValue);
                    data[i] = (byte) (intValue);
                }

                mProtocol.parse(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //  mProtocol.closeWriter();
    }
}

