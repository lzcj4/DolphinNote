package com.tannuo.sdk.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by nick on 2016/04/22.
 */
public class MTBeacon {
    private BluetoothDevice device;
    private int averagerssi = 0;
    private int rssi;
    private byte[] scanRecord;
    private int search_count = 0;

    private int major;
    private int minor;
    private int Txpower;
    private String UUID;

    private int Ibeancon_Offset = 0;
    private int MT_inf_offset = 0;

    public MTBeacon(BluetoothDevice device, int rssi, byte[] scanRecord) {
        this.device = device;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
        this.averagerssi = rssi;

//		getOffset(this.scanRecord);
    }


    public BluetoothDevice getDevice() {
        return device;
    }


    public int checkSearchcount() {
        search_count++;
        return search_count;
    }


    public boolean reflashInf(BluetoothDevice device, int rssi,
                              byte[] scanRecord) {
        this.device = device;
        this.rssi = rssi;
        this.scanRecord = scanRecord;

        averagerssi = (averagerssi + rssi) / 2;

        search_count = 0; // ����

//		getOffset(this.scanRecord);

        return true;
    }


    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getTxpower() {
        return Txpower;
    }

    public String getUUID() {
        return UUID;
    }

    public int getEnergy() {
        return scanRecord[MT_inf_offset + 3];
    }


    public int getCurrentRssi() {
        return rssi;
    }

    public int getAveragerssi() {
        return averagerssi;
    }


    public double getCurrentDistance() {
        return calcDistance(rssi);
    }


    public double getAveragerDistance() {
        return calcDistance(averagerssi);
    }


    //TODO what's this?
    private double calcDistance(int rssi) {
        double distance = 0;
        double ratio = rssi * 1.0 / Txpower;
        if (ratio < 1.0) {
            distance = Math.pow(ratio, 10);
        } else {
            distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
        }

        return distance;
    }


    //TODO what's this?
    private void getOffset(byte[] scanRecord) {
        for (int i = 0; i < scanRecord.length; ) {
            if ((scanRecord[i] == 26) && (scanRecord[i + 1] == -1)
                    && (scanRecord[i + 2] == 76) && (scanRecord[i + 3] == 0)
                    && (scanRecord[i + 4] == 2) && (scanRecord[i + 5] == 21)) {
                Ibeancon_Offset = i;

                major = ((0xFF & scanRecord[i + 22]) * 256 + (0xFF & scanRecord[i + 23]));
                minor = ((0xFF & scanRecord[i + 24]) * 256 + (0xFF & scanRecord[i + 25]));
                Txpower = scanRecord[i + 26];

                UUID = "";
                for (int j = i + 6; j < i + 22; j++) { // uuid
                    String hex = Integer.toHexString(scanRecord[j] & 0xFF);
                    if (hex.length() == 1) {
                        hex = '0' + hex;
                    }
                    if ((j == (i + 10)) || (j == (i + 12)) || (j == (i + 14))
                            || (j == (i + 16)))
                        UUID += '-';
                    UUID += hex;
                }
                UUID = UUID.toUpperCase();

            }

            if ((scanRecord[i] == 3) && (scanRecord[i + 1] == -86)) {
                MT_inf_offset = i;
            }

            i += (scanRecord[i] + 1);
            if ((i >= (scanRecord.length)) || (0x00 == scanRecord[i])) {
                break;
            }
        }

    }

}
