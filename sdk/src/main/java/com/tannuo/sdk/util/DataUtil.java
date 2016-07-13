package com.tannuo.sdk.util;

/**
 * Created by nick on 2016/4/23.
 */
public class DataUtil {
    public static int byteToUnsignedByte(byte b) {
        int result = b & 0xFF;
        return result;
    }

    /**
     * @param bytes
     * @return
     */
    public static int bytesToIntLittleEndian(byte... bytes) {
        if (null == bytes || bytes.length == 0) {
            return 0;
        }
        if (!(bytes.length == 4 || bytes.length == 2)) {
            throw new IllegalArgumentException("invalid bytes'len");
        }

        int len = bytes.length - 1;
        int result = 0;
        for (int i = len; i >= 0; i--) {
            int b = byteToUnsignedByte(bytes[i]);
            result += b << 8 * i;
        }
        return result;
    }
}
