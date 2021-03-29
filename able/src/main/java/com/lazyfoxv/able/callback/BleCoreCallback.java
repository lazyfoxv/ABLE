package com.lazyfoxv.able.callback;

import android.bluetooth.BluetoothGattCharacteristic;

public interface BleCoreCallback {

    /**
     * 接收到设备通知数据
     */
    void onNotifyData(String bleAddress, String bleName, BluetoothGattCharacteristic characteristic);

    /**
     * 写入
     */
    void onWriteData(String bleAddress, String bleName, BluetoothGattCharacteristic characteristic, int errorCode);

    /**
     * 读取
     */
    void onReadData(String bleAddress, String bleName, BluetoothGattCharacteristic characteristic, int errorCode);

    /**
     * 读取信号
     */
    void onReadRssi(String bleAddress, String bleName, int rssi);

}
