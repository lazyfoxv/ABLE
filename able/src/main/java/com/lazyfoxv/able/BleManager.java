package com.lazyfoxv.able;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;


import com.lazyfoxv.able.callback.BleCoreCallback;
import com.lazyfoxv.able.callback.ConnectCallback;
import com.lazyfoxv.able.callback.ScanDeviceCallback;
import com.lazyfoxv.able.util.Constants;
import com.lazyfoxv.able.util.LogUtil;
import com.lazyfoxv.able.wrapper.BluetoothGattCallbackWrapper;
import com.lazyfoxv.able.wrapper.ScanCallbackWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;


/**
 * Created by lazyfoxv on 2021-01-03.
 * BleManager
 */
public final class BleManager {

    //TODO: isConnected
    //TODO: Rssi change
    //TODO: LiveData
    //TODO: ScanAndConnect

    //TODO: Enable
    //TODO: Release
    //TODO: startup

    //TODO: 扫描是否要清空

    public static final String TAG = "蓝牙::";

    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    // 多连接的连接索引
    private int mConnectIndex = -1;

    // 需要多连接的List
    private List<String> mBleAddressList;

    // 是否跳过错误
    private boolean mIgnoreError;

    // 扫描配置
    private final ScanConfig mScanConfig;

    // 主要回调
    private final List<BleCoreCallback> mBleCoreCallbackList;
    private final List<ConnectCallback> mConnectCallbackList;

    // 扫描回调
    private ScanDeviceCallback mScanDeviceCallback;

    // 包装系统类
    private final BluetoothGattCallbackWrapper mBluetoothGattCallbackWrapper;

    // 包装系统类
    private ScanCallbackWrapper mScanCallbackWrapper;

    // 是否正在扫描
    private volatile boolean isScanning;

    private final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.MSG_WHAT_SCAN_STOP:
                    LogUtil.i("Stop scan");
                    stopScan();
                    break;
                case Constants.MSG_WHAT_CONNECT_SUCCESS:
                    LogUtil.i("Connect success");
                    nextConnect();
                    break;
                case Constants.MSG_WHAT_CONNECT_ERROR:
                    LogUtil.e("Connect error");
                    if (mIgnoreError) {
                        nextConnect();
                    }
                    break;
            }
        }
    };

    @SuppressLint("StaticFieldLeak")
    private static volatile BleManager bleManager;

    private BleManager() {
        mIgnoreError = true;
        mBleCoreCallbackList = new ArrayList<>();
        mConnectCallbackList = new ArrayList<>();
        mBluetoothGattCallbackWrapper = new BluetoothGattCallbackWrapper(mBleCoreCallbackList, mConnectCallbackList, mHandler);
        mScanConfig = new ScanConfig.Builder().setTimeout(30_000).build();
    }

    public static BleManager getInstance() {
        if (bleManager == null) {
            synchronized (BleManager.class) {
                if (bleManager == null) {
                    bleManager = new BleManager();
                }
            }
        }
        return bleManager;
    }

    public void init(Context context) {
        this.mContext = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mScanCallbackWrapper = new ScanCallbackWrapper();
    }

    public boolean isBluetoothEnable() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public boolean isSupportBle() {
        return mContext.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public void enableBluetooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
    }

    public void disableBluetooth() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled())
                mBluetoothAdapter.disable();
        }
    }

    /**
     * 扫描ble蓝牙
     *
     * @param scanDeviceCallback 扫描回调
     */
    public void scan(ScanDeviceCallback scanDeviceCallback) {
        scan(mScanConfig, scanDeviceCallback);
    }

    /**
     * 扫描ble蓝牙
     *
     * @param scanConfig         扫描配置
     * @param scanDeviceCallback 扫描回调
     */
    public void scan(ScanConfig scanConfig, ScanDeviceCallback scanDeviceCallback) {
        if (mBluetoothAdapter == null) {
            return;
        }
        if (isScanning) {
            stopScan();
        }
        isScanning = true;
        mScanDeviceCallback = scanDeviceCallback;
        mScanCallbackWrapper.setScanDeviceCallback(scanDeviceCallback);
        mHandler.removeMessages(Constants.MSG_WHAT_SCAN_STOP);
        if (scanConfig.getTimeout() > 0) {
            mHandler.sendEmptyMessageDelayed(Constants.MSG_WHAT_SCAN_STOP, scanConfig.getTimeout());
        }

        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        UUID[] uuids = scanConfig.getUuids();
        String[] filterBleAddress = scanConfig.getFilterBleAddress();
        String[] filterBleNames = scanConfig.getFilterBleNames();

        if (uuids != null && uuids.length > 0) { // 使用UUID扫描
            List<ScanFilter> scanFilterList = new ArrayList<>();
            for (final UUID uuid : uuids) {
                scanFilterList.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(uuid)).build());
            }
            scanner.startScan(scanFilterList, new ScanSettings.Builder().build(), mScanCallbackWrapper);
        } else if (filterBleAddress != null && filterBleAddress.length > 0) { // 使用Address扫描
            mScanCallbackWrapper.setFilterAddress(filterBleAddress);
            scanner.startScan(mScanCallbackWrapper);
        } else {  // 默认使用Name扫描
            mScanCallbackWrapper.setFilterNames(filterBleNames);
            scanner.startScan(mScanCallbackWrapper);
        }
        if (mScanDeviceCallback != null) {
            mScanDeviceCallback.onStartScan();
        }
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        if (mBluetoothAdapter == null) {
            return;
        }
        isScanning = false;
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.stopScan(mScanCallbackWrapper);
        if (mScanDeviceCallback != null) {
            mScanDeviceCallback.onStopScan();
        }
    }

    /**
     * 是否在扫描
     *
     * @return
     */
    public boolean isScanning() {
        return isScanning;
    }

    /**
     * 连接多个设备
     *
     * @param bleAddressList Mac地址
     * @param ignoreError    当List中设备发生连接错误时, 是否忽略错误继续连接下一个, 默认为true
     */
    public void connect(List<String> bleAddressList, boolean ignoreError) {
        if (bleAddressList.size() > 0) {
            mConnectIndex = 0;
            this.mBleAddressList = bleAddressList;
            connect(bleAddressList.get(0));
            mIgnoreError = ignoreError;
        }
    }

    /**
     * 连接多个设备
     * @param bleAddressList Mac地址
     */
    public void connect(List<String> bleAddressList) {
        connect(bleAddressList, true);
    }

    /**
     * 连接单个设备
     *
     * @param bleAddress Mac地址
     */
    public void connect(String bleAddress) {
        if (mBluetoothAdapter == null || bleAddress == null) return;
        BluetoothDevice device = getSystemConnectedDevice(bleAddress);
        if (device == null) {
            device = mBluetoothAdapter.getRemoteDevice(bleAddress);
        }
        if (device == null) return;
        BluetoothGatt gatt;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gatt = device.connectGatt(mContext, false, mBluetoothGattCallbackWrapper, TRANSPORT_LE);
        } else {
            gatt = device.connectGatt(mContext, false, mBluetoothGattCallbackWrapper);
        }
        // gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        Log.d("BleGattCallbackAdapter:", "开始连接: " + bleAddress);
        if (gatt != null) {
            for (ConnectCallback connectCallback : mConnectCallbackList) {
                connectCallback.onStartConnect(gatt.getDevice().getAddress(), gatt.getDevice().getName());
            }
        } else {
//            clear();
            for (ConnectCallback connectCallback : mConnectCallbackList) {
                connectCallback.onConnectError(gatt.getDevice().getAddress(), gatt.getDevice().getName(), -1);
            }
        }
    }


    /**
     * 传入Mac是否已连接
     *
     * @param bleAddress Mac
     * @return boolean
     */
    public boolean isConnected(String bleAddress) {
        return getSystemConnectedDevice(bleAddress) != null || getConnectedGatt(bleAddress) != null;
    }


    public void addBleCoreCallback(BleCoreCallback bleCoreCallback) {
        mBleCoreCallbackList.add(bleCoreCallback);
    }

    public void removeBleCoreCallback(BleCoreCallback bleCoreCallback) {
        mBleCoreCallbackList.remove(bleCoreCallback);
    }

    public void addBleConnectCallback(ConnectCallback connectCallback) {
        mConnectCallbackList.add(connectCallback);
    }

    public void removeBleConnectCallback(ConnectCallback connectCallback) {
        mConnectCallbackList.remove(connectCallback);
    }

    /**
     * 断开连接
     *
     * @param bleAddress Mac
     */
    public void disconnect(String bleAddress) {
        BaseDevice device = ConnectedPool.getInstance().getDevice(bleAddress);
        if (device != null) {
            device.release();
            ConnectedPool.getInstance().removeDevice(bleAddress);
        }
    }

    /**
     * 断开连接
     *
     * @param device 设备
     * @param <D>    T extends BaseDevice
     */
    public <D extends BaseDevice> void disconnect(D device) {
        this.disconnect(device.getBleAddress());
    }

    /**
     * 断开所有已连接的设备
     */
    public void disconnectAll() {
        LinkedHashMap<String, BaseDevice> deviceMap = ConnectedPool.getInstance().getDeviceMap();
        Iterator<Map.Entry<String, BaseDevice>> iterator = deviceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next().getValue().release();
            iterator.remove();
        }
    }

    void onDisconnect(String bleAddress, String bleName) {
        for (ConnectCallback connectCallback : mConnectCallbackList) {
            connectCallback.onDisconnect(bleAddress, bleName);
        }
    }

    /**
     * 通过反射刷新 GattCache
     *
     * @param gatt BluetoothGatt
     * @return boolean
     */
    public boolean refreshGattCache(BluetoothGatt gatt) {
        boolean result = false;
        if (gatt != null) {
            try {
                Method refresh = BluetoothGatt.class.getMethod("refresh");
                if (refresh != null) {
                    refresh.setAccessible(true);
                    result = (boolean) refresh.invoke(gatt, new Object[0]);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取已连接的 BluetoothGatt
     *
     * @param bleAddress Mac
     * @return BluetoothGatt
     */
    BluetoothGatt getConnectedGatt(String bleAddress) {
        return mBluetoothGattCallbackWrapper.getBluetoothGattMap().get(bleAddress);
    }

    /**
     * 获取系统已连上的
     *
     * @param bleAddress Mac
     * @return BluetoothDevice
     */
    private BluetoothDevice getSystemConnectedDevice(String bleAddress) {
        List<BluetoothDevice> connectedDevices = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        BluetoothDevice bluetoothDevice = null;
        for (BluetoothDevice connectedDevice : connectedDevices) {
            if (bleAddress.equalsIgnoreCase(connectedDevice.getAddress())) {
                bluetoothDevice = connectedDevice;
                break;
            }
        }
        return bluetoothDevice;
    }

    /**
     * 多设备排队连接
     */
    private void nextConnect() {
        if (mBleAddressList.size() > 1) {
            mConnectIndex++;
            for (int i = 0; i < mBleAddressList.size(); i++) {
                if (mConnectIndex == i) {
                    connect(mBleAddressList.get(i));
                }
            }
        }
    }
}
