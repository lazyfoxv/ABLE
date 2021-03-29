package com.lazyfoxv.able.util;

import android.util.Log;

public final class LogUtil {

    private static boolean enable = true;
    private static String TAG = "BluetoothX";

    public static void d(String msg) {
        if (enable && msg!=null ) {
            Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (enable && msg!=null ) {
            Log.i(TAG, msg);
        }
    }
    public static void w(String msg) {
        if (enable && msg!=null ) {
            Log.e(TAG, msg);
        }
    }
    public static void e(String msg) {
        if (enable && msg!=null ) {
            Log.e(TAG, msg);
        }
    }
    public static void v(String msg) {
        if (enable && msg!=null ) {
            Log.v(TAG, msg);
        }
    }

}
