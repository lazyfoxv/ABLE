package com.lazyfoxv.able;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;


import com.lazyfoxv.able.callback.BleCoreCallback;
import com.lazyfoxv.able.callback.ConnectCallback;

import java.util.List;

public class CallbackAdapter implements ConnectCallback, BleCoreCallback {

    @Override
    public void onStartConnect(String bleAddress, String bleName) {

    }

    @Override
    public void onConnectSuccess(String bleAddress, String bleName, List<BluetoothGattService> services) {

    }

    @Override
    public void onConnectError(String bleAddress, String bleName, int errorCode) {

    }

    @Override
    public void onDisconnect(String bleAddress, String bleName) {

    }

    @Override
    public void onNotifyData(String bleAddress, String bleName, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onWriteData(String bleAddress, String bleName, BluetoothGattCharacteristic characteristic, int errorCode) {

    }

    @Override
    public void onReadData(String bleAddress, String bleName, BluetoothGattCharacteristic characteristic, int errorCode) {

    }

    @Override
    public void onReadRssi(String bleAddress, String bleName, int rssi) {

    }
}
