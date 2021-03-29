package com.lazyfoxv.able.wrapper;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;


import com.lazyfoxv.able.callback.ScanDeviceCallback;

import java.util.List;

public final class ScanCallbackWrapper extends ScanCallback {

    private ScanDeviceCallback mScanDeviceCallback;

    private String[] filterNames;
    private String[] filterAddress;

    public void setScanDeviceCallback(ScanDeviceCallback scanDeviceCallback) {
        mScanDeviceCallback = scanDeviceCallback;
    }

    public String[] getFilterNames() {
        return filterNames;
    }

    public void setFilterNames(String[] filterNames) {
        this.filterNames = filterNames;
    }

    public void setFilterAddress(String[] filterAddress) {
        this.filterAddress = filterAddress;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        ScanRecord scanRecord = result.getScanRecord();
        BluetoothDevice bluetoothDevice = result.getDevice();
        byte[] advertising = new byte[0];
        if (scanRecord != null) { //scanRecord 可能为 null
            advertising = scanRecord.getBytes();
        }
        String deviceName = bluetoothDevice.getName();
        if (deviceName != null) {
            deviceName = deviceName.trim();
            if (filterAddress != null && filterAddress.length > 0) {
                if (filterBy(deviceName, filterAddress)) return;
            } else if (filterNames != null && filterNames.length > 0) {
                if (filterBy(deviceName, filterNames)) return;
            }
        }
        if (mScanDeviceCallback != null) {
            mScanDeviceCallback.onScanSuccess(bluetoothDevice, result.getRssi(), advertising);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        if (mScanDeviceCallback != null) {
            mScanDeviceCallback.onScanFailed(errorCode);
        }
    }


    private boolean filterBy(String deviceName, String[] filter) {
        boolean isFiltering = false;
        for (String filterName : filter) {
            if (deviceName.equals(filterName)) {
                isFiltering = true;
                break;
            }
        }
        return !isFiltering;
    }
}
