package com.lazyfoxv.able.util;

public final class CrcUtils {

    public static int CRC8(byte[] source, int offset, int length) {
        int wCRCin = 0x00;
        int wCPoly = 0x07;
        for (int i = offset, cnt = offset + length; i < cnt; i++) {
            for (int j = 0; j < 8; j++) {
                boolean bit = ((source[i] >> (7 - j) & 1) == 1);
                boolean c07 = ((wCRCin >> 7 & 1) == 1);
                wCRCin <<= 1;
                if (c07 ^ bit)
                    wCRCin ^= wCPoly;
            }
        }
        wCRCin &= 0xFF;
        return wCRCin ^= 0x00;
    }

    public static int CRC8_ITU(byte[] source, int offset, int length) {
        int wCRCin = 0x00;
        int wCPoly = 0x07;
        for (int i = offset, cnt = offset + length; i < cnt; i++) {
            for (int j = 0; j < 8; j++) {
                boolean bit = ((source[i] >> (7 - j) & 1) == 1);
                boolean c07 = ((wCRCin >> 7 & 1) == 1);
                wCRCin <<= 1;
                if (c07 ^ bit)
                    wCRCin ^= wCPoly;
            }
        }
        wCRCin &= 0xFF;
        return wCRCin ^= 0x55;
    }

    public static int CRC16_MAXIM(byte[] source, int offset, int length) {
        int wCRCin = 0x0000;
        // Integer.reverse(0x8005) >>> 16
        int wCPoly = 0xA001;
        for (int i = offset, cnt = offset + length; i < cnt; i++) {
            wCRCin ^= ((int) source[i] & 0x00FF);
            for (int j = 0; j < 8; j++) {
                if ((wCRCin & 0x0001) != 0) {
                    wCRCin >>= 1;
                    wCRCin ^= wCPoly;
                } else {
                    wCRCin >>= 1;
                }
            }
        }
        return wCRCin ^= 0xFFFF;
    }

    public static int CRC16_IBM(byte[] source, int offset, int length) {
        int wCRCin = 0x0000;
        // Integer.reverse(0x8005) >>> 16
        int wCPoly = 0xA001;
        for (int i = offset, cnt = offset + length; i < cnt; i++) {
            wCRCin ^= ((int) source[i] & 0x00FF);
            for (int j = 0; j < 8; j++) {
                if ((wCRCin & 0x0001) != 0) {
                    wCRCin >>= 1;
                    wCRCin ^= wCPoly;
                } else {
                    wCRCin >>= 1;
                }
            }
        }
        return wCRCin ^= 0x0000;
    }

    public static int CRC16_MODBUS(byte[] source, int offset, int length) {
        int wCRCin = 0xFFFF;
        // Integer.reverse(0x8005) >>> 16
        int wCPoly = 0xA001;
        for (int i = offset, cnt = offset + length; i < cnt; i++) {
            wCRCin ^= ((int) source[i] & 0x00FF);
            for (int j = 0; j < 8; j++) {
                if ((wCRCin & 0x0001) != 0) {
                    wCRCin >>= 1;
                    wCRCin ^= wCPoly;
                } else {
                    wCRCin >>= 1;
                }
            }
        }
        return wCRCin ^= 0x0000;
    }

    public static int CRC16_USB(byte[] source, int offset, int length) {
        int wCRCin = 0xFFFF;
        // Integer.reverse(0x8005) >>> 16
        int wCPoly = 0xA001;
        for (int i = offset, cnt = offset + length; i < cnt; i++) {
            wCRCin ^= ((int) source[i] & 0x00FF);
            for (int j = 0; j < 8; j++) {
                if ((wCRCin & 0x0001) != 0) {
                    wCRCin >>= 1;
                    wCRCin ^= wCPoly;
                } else {
                    wCRCin >>= 1;
                }
            }
        }
        return wCRCin ^= 0xFFFF;
    }

    public static int CRC8_MAXIM(byte[] source, int offset, int length) {
        int wCRCin = 0x00;
        // Integer.reverse(0x31) >>> 24
        int wCPoly = 0x8C;
        for (int i = offset, cnt = offset + length; i < cnt; i++) {
            wCRCin ^= ((long) source[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((wCRCin & 0x01) != 0) {
                    wCRCin >>= 1;
                    wCRCin ^= wCPoly;
                } else {
                    wCRCin >>= 1;
                }
            }
        }
        return wCRCin ^= 0x00;
    }

    public static long CRC32(byte[] source, int offset, int length) {
        long wCRCin = 0xFFFFFFFFL;
        // Long.reverse(0x04C11DB7L) >>> 32
        long wCPoly = 0xEDB88320L;
        for (int i = offset, cnt = offset + length; i < cnt; i++) {
            wCRCin ^= ((long) source[i] & 0x000000FFL);
            for (int j = 0; j < 8; j++) {
                if ((wCRCin & 0x00000001L) != 0) {
                    wCRCin >>= 1;
                    wCRCin ^= wCPoly;
                } else {
                    wCRCin >>= 1;
                }
            }
        }
        return wCRCin ^= 0xFFFFFFFFL;
    }
}
