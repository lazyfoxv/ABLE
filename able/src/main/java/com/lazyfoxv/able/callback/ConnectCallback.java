package com.lazyfoxv.able.callback;

import android.bluetooth.BluetoothGattService;

import java.util.List;

public interface ConnectCallback {
    /**
     * 开始连接
     */
    void onStartConnect(String bleAddress, String bleName);

    /**
     * 连接成功
     */
    void onConnectSuccess(String bleAddress, String bleName, List<BluetoothGattService> services);

    /**
     * 连接错误
     *
     * @param bleAddress BluetoothGatt
     */
    void onConnectError(String bleAddress, String bleName, int errorCode);

    /**
     * 断开连接
     */
    void onDisconnect(String bleAddress, String bleName);
}
