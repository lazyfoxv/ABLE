package com.lazyfoxv.able.util;

public final class HexUtil {


    /**
     * 16进制String转Byte数组
     *
     * @param hexStr 16进制String
     * @return Byte数组
     */
    public static byte[] hexStrToBytes(String hexStr) {
        if (hexStr == null || hexStr.equals("")) {
            return null;
        }
        hexStr = hexStr.trim();
        hexStr = hexStr.toUpperCase();
        int length = hexStr.length() / 2;
        char[] hexChars = hexStr.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 字节数组转16进制字符串
     *
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    public static String bytesToHexStr(byte[] bytes) {
        StringBuilder hexStr = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hexStr.append(hex.toUpperCase());
        }
        return hexStr.toString();
    }


    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static final String BIN_SEPARATOR = " ";

    /**
     * 字符串转换为2进制字符串
     *
     * @param str 普通字符串
     * @return String 2进制字符串
     */
    public static String strToBinStr(String str) {

        if (str == null) return null;

        StringBuilder sb = new StringBuilder();

        byte[] bytes = str.getBytes();
        for (byte aByte : bytes) {
            sb.append(Integer.toBinaryString(aByte)).append(BIN_SEPARATOR);
        }
        return sb.toString();
    }

    /**
     * 2进制字符串转换为普通字符串
     *
     * @param binStr 2进制字符串
     * @return String 普通字符串
     */
    public static String binStrToStr(String binStr) {
        if (binStr == null) return null;
        String[] binArrays = binStr.split(BIN_SEPARATOR);

        StringBuilder sb = new StringBuilder();
        for (String binString : binArrays) {
            char c = binStrToChar(binString);
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 2进制字符串转换为16进制字符串
     *
     * @param binStr 2进制字符串
     * @return String 16进制字符串
     */
    public static String binStrToHexStr(String binStr) {
        if (binStr == null || binStr.equals("") || binStr.length() % 8 != 0)
            return null;
        StringBuilder tmp = new StringBuilder();
        int iTmp;
        for (int i = 0; i < binStr.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(binStr.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }


    /**
     * 10进制整数进制转16进制字符串
     *
     * @param ten 10进制整数
     * @return String
     */
    public static String deciToHexStr(int ten) {
        return Integer.toHexString(ten);
    }

    /**
     * 10进制整数进制转16进制字符串, 返回1个字节表示
     *
     * @param ten 10进制整数
     * @return 返回1个字节表示
     */
    public static String deciToHexStrOfOneByte(int ten) {
        String hex = Integer.toHexString(ten & 0xFF);
        if (hex.length() == 1) {
            hex = "0" + hex;
        }
        return hex;
    }


    /**
     * 10进制整数进制转16进制字符串, 返回2个字节表示
     *
     * @param ten 10进制整数
     * @return 返回2个字节表示
     */
    public static String deciToHexStrOfTwoByte(int ten) {
        String hex = Integer.toHexString(ten & 0xFFFF);
        if (hex.length() == 1) {
            hex = "000" + hex;
        } else if (hex.length() == 2) {
            hex = "00" + hex;
        } else if (hex.length() == 3) {
            hex = "0" + hex;
        }
        return hex;
    }

    /**
     * 将2进制转换成字符
     *
     * @param binStr 2进制字符串
     * @return char
     */
    private static char binStrToChar(String binStr) {
        int[] temp = binStrToIntArray(binStr);
        int sum = 0;
        for (int i = 0; i < temp.length; i++) {
            sum += temp[temp.length - 1 - i] << i;
        }
        return (char) sum;
    }

    /**
     * 2进制字符转换为int数组
     *
     * @param binStr 2进制字符串
     * @return int[]
     */
    private static int[] binStrToIntArray(String binStr) {
        char[] temp = binStr.toCharArray();
        int[] result = new int[temp.length];
        for (int i = 0; i < temp.length; i++) {
            result[i] = temp[i] - 48;
        }
        return result;
    }

}
