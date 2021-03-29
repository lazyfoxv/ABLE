package com.lazyfoxv.able.wrapper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;


import com.lazyfoxv.able.callback.BleCoreCallback;
import com.lazyfoxv.able.callback.ConnectCallback;
import com.lazyfoxv.able.util.Constants;
import com.lazyfoxv.able.util.LogUtil;

import java.util.HashMap;
import java.util.List;


/**
 * 方法回调在 Binder 线程，所以不建议直接在这个线程处理耗时的任务和更新UI
 */
public final class BluetoothGattCallbackWrapper extends BluetoothGattCallback {

    private final List<ConnectCallback> mConnectCallbackList;
    private final List<BleCoreCallback> mBleCoreCallbackList;
    private final Handler mHandler;

    // 保存所有的连接蓝牙设备Gatt(成功发现服务的Gatt)
    private final HashMap<String, BluetoothGatt> mBluetoothGattMap = new HashMap<>();

    public BluetoothGattCallbackWrapper(List<BleCoreCallback> bleCoreCallbackList, List<ConnectCallback> connectCallbackList, Handler handler) {
        this.mBleCoreCallbackList = bleCoreCallbackList;
        this.mConnectCallbackList = connectCallbackList;
        this.mHandler = handler;
    }

    /**
     * 连接状态改变
     *
     * @param gatt
     * @param status
     * @param newState
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
            gatt.discoverServices(); // 开始异步发现服务
        } else if ((status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_DISCONNECTED) || (status == 8 && newState == BluetoothGatt.STATE_DISCONNECTED)) {
            mBluetoothGattMap.remove(gatt.getDevice().getAddress());
            gatt.close();
            for (ConnectCallback connectCallback : mConnectCallbackList) {
                connectCallback.onDisconnect(gatt.getDevice().getAddress(), gatt.getDevice().getName());
            }
        } else { // ERROR------->  status == 133 || status == 257 || status == 22 || status == 19
            LogUtil.e("Connect Error: status="+status+"  newState="+newState +", Gatt is closed!");
            mHandler.sendEmptyMessage(Constants.MSG_WHAT_CONNECT_ERROR);
            gatt.close();
            for (ConnectCallback connectCallback : mConnectCallbackList) {
                connectCallback.onConnectError(gatt.getDevice().getAddress(), gatt.getDevice().getName(), status);
            }
        }
    }

    /**
     * 完成发现服务, 连接成功
     *
     * @param gatt
     * @param status
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        mBluetoothGattMap.put(gatt.getDevice().getAddress(), gatt);
        for (ConnectCallback connectCallback : mConnectCallbackList) {
            connectCallback.onConnectSuccess(gatt.getDevice().getAddress(), gatt.getDevice().getName(), gatt.getServices());
        }
        mHandler.sendEmptyMessage(Constants.MSG_WHAT_CONNECT_SUCCESS);
    }

    /**
     * //读取特征
     *
     * @param gatt
     * @param characteristic
     * @param status
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        for (BleCoreCallback bleCoreCallback : mBleCoreCallbackList) {
            bleCoreCallback.onReadData(gatt.getDevice().getAddress(), gatt.getDevice().getName(), characteristic, status);
        }
    }

    /**
     * //写入特征
     *
     * @param gatt
     * @param characteristic
     * @param status
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        for (BleCoreCallback bleCoreCallback : mBleCoreCallbackList) {
            bleCoreCallback.onWriteData(gatt.getDevice().getAddress(), gatt.getDevice().getName(), characteristic, status);
        }
    }

    /**
     * 特征改变
     *
     * @param gatt
     * @param characteristic
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        for (BleCoreCallback bleCoreCallback : mBleCoreCallbackList) {
            bleCoreCallback.onNotifyData(gatt.getDevice().getAddress(), gatt.getDevice().getName(), characteristic);
        }
    }

    /**
     * 读取描述符
     *
     * @param gatt
     * @param descriptor
     * @param status
     */
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
    }

    /**
     * 写入描述符
     *
     * @param gatt
     * @param descriptor
     * @param status
     */
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
    }

    /**
     * 读取信号强度
     *
     * @param gatt
     * @param rssi
     * @param status
     */
    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        for (BleCoreCallback bleCoreCallback : mBleCoreCallbackList) {
            bleCoreCallback.onReadRssi(gatt.getDevice().getAddress(), gatt.getDevice().getName(), rssi);
        }
    }

    /**
     * //蓝牙网卡变化回调
     *
     * @param gatt
     * @param mtu
     * @param status
     */
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
    }

    /**
     * 获取已连接Gatt
     *
     * @return
     */
    public HashMap<String, BluetoothGatt> getBluetoothGattMap() {
        return mBluetoothGattMap;
    }
}
