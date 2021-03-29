package com.lazyfoxv.able.callback;

import android.bluetooth.BluetoothDevice;

public interface ScanDeviceCallback {
    /**
     * 开始扫描
     */
    void onStartScan();

    /**
     * 发现设备
     *
     * @param device      设备
     * @param rssi        信号强度
     * @param advertising 广播数据
     */
    void onScanSuccess(BluetoothDevice device, int rssi, byte[] advertising);

    void onScanFailed(final int errorCode);

    /**
     * 停止扫描
     */
    void onStopScan();
}
