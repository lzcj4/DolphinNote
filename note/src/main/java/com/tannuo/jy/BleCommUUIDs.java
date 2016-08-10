package com.tannuo.jy;

import java.util.HashMap;

/**
 * Created by Yuting Bao on 2016/4/27.
 */

public class BleCommUUIDs {
    private static HashMap<String, String> UUIDList = new HashMap();

    static {
        // Feasycom
        UUIDList.put("Feasycom", "0000fff0-0000-1000-8000-00805f9b34fb");
        UUIDList.put("FeasycomTX", "0000fff2-0000-1000-8000-00805f9b34fb");
        UUIDList.put("FeasycomRX", "0000fff1-0000-1000-8000-00805f9b34fb");

        // MTSeriBle4E
        UUIDList.put("MTSeriBle4E", "0000f1f0-0000-1000-8000-00805f9b34fb");
        UUIDList.put("MTSeriBle4ETX", "0000f1f1-0000-1000-8000-00805f9b34fb");
        UUIDList.put("MTSeriBle4ERX", "0000f1f2-0000-1000-8000-00805f9b34fb");


    }

    public static String[] lookup(String deviceName) {
        String[] deviceUUIDS;
        deviceUUIDS = new String[3];
        deviceUUIDS[0] = UUIDList.get(deviceName);
        deviceUUIDS[1] = UUIDList.get(deviceName+"TX");
        deviceUUIDS[2] = UUIDList.get(deviceName+"RX");
        return deviceUUIDS;
    }
}
