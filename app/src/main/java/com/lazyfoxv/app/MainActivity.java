package com.lazyfoxv.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;

import com.lazyfoxv.able.BleManager;
import com.lazyfoxv.able.ConnectedPool;
import com.lazyfoxv.able.R;
import com.lazyfoxv.able.callback.ConnectCallback;
import com.lazyfoxv.able.callback.DataReceivedCallback;
import com.lazyfoxv.able.callback.ScanDeviceCallback;
import com.lazyfoxv.ble.TestDevice;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BleManager.getInstance().scan(new ScanDeviceCallback() {
            @Override
            public void onStartScan() {

            }

            @Override
            public void onScanSuccess(BluetoothDevice device, int rssi, byte[] advertising) {

            }

            @Override
            public void onScanFailed(int errorCode) {

            }

            @Override
            public void onStopScan() {

            }
        });

        BleManager.getInstance().connect("555");

        BleManager.getInstance().addBleConnectCallback(new ConnectCallback() {
            @Override
            public void onStartConnect(String bleAddress, String bleName) {

            }

            @Override
            public void onConnectSuccess(String bleAddress, String bleName, List<BluetoothGattService> services) {
                TestDevice testDevice = new TestDevice(bleAddress, bleName);
            }

            @Override
            public void onConnectError(String bleAddress, String bleName, int errorCode) {

            }

            @Override
            public void onDisconnect(String bleAddress, String bleName) {

            }
        });

        TestDevice testDevice = ConnectedPool.getInstance().getHeadDevice();
        testDevice.setSpeed().setBrightness().execute();

        testDevice.setMode(1, new DataReceivedCallback() {
            @Override
            public void onDataReceived(@NonNull String data) {

            }
        }).execute();
    }


}