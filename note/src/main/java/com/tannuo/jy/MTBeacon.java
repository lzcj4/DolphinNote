package com.tannuo.jy;

import android.bluetooth.BluetoothDevice;

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
	private int MT_inf_offset = 0; // ��ͷ��Ϣƫ��

	public MTBeacon(BluetoothDevice device, int rssi, byte[] scanRecord) {
		this.device = device;
		this.rssi = rssi;
		this.scanRecord = scanRecord;
		this.averagerssi = rssi;

//		GetOffset(this.scanRecord); // ��ȡƫ����Ϣ
	}

	// ��ȡ�豸
	public BluetoothDevice GetDevice() {
		return device;
	}

	// ����
	public int CheckSearchcount() {
		search_count++;
		return search_count;
	}

	// ������Ϣ
	public boolean ReflashInf(BluetoothDevice device, int rssi,
			byte[] scanRecord) {
		this.device = device;
		this.rssi = rssi;
		this.scanRecord = scanRecord;

		averagerssi = (averagerssi + rssi) / 2;

		search_count = 0; // ����

//		GetOffset(this.scanRecord);

		return true;
	}

	// major��minor��TXpower��UUID��������Ϣ
	public int GetMajor() {
		return major;
	}

	public int GetMinor() {
		return minor;
	}

	public int GetTxpower() {
		return Txpower;
	}

	public String GetUUID() {
		return UUID;
	}

	public int GetEnergy(){
		return scanRecord[MT_inf_offset+3];
	}
	
	// ��ȡrssiֵ
	public int GetCurrentRssi() {
		return rssi;
	}

	// ��ȡrssi����ֵ(ƽ��ֵ)
	public int GetAveragerssi() {
		return averagerssi;
	}

	// ��ȡ��ǰ����
	public double GetCurrentDistance() {
		return CalculateDistance(rssi);
	}

	// ��ȡƽ�����
	public double GetAveragerDistance() {
		return CalculateDistance(averagerssi);
	}

	/************************* ������ *******************************/
	// �������
	private double CalculateDistance(int rssi) {
		double distance = 0;
		double ratio = rssi * 1.0 / Txpower; // �������
		if (ratio < 1.0) {
			distance = Math.pow(ratio, 10);
		} else {
			distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
		}

		return distance;
	}

	// ��ȡ��׼ƫ����
	private void GetOffset(byte[] scanRecord) {
		for (int i = 0; i < scanRecord.length;) {
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
