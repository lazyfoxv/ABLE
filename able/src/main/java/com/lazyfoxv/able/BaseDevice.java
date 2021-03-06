package com.lazyfoxv.able;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.lazyfoxv.able.callback.DataReceivedCallback;
import com.lazyfoxv.able.callback.ReadRssiCallback;
import com.lazyfoxv.able.util.Constants;
import com.lazyfoxv.able.util.HexUtil;
import com.lazyfoxv.able.util.LogUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public abstract class BaseDevice {

    public static final String TAG = "ABLE";

    private String mBleAddress;
    private String mBleName;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private BluetoothGatt mBluetoothGatt;
    private ReadRssiCallback mReadRssiCallback;
    private final Queue<byte[]> mDataQueue = new LinkedList<>();


    private final Map<String, DataReceivedCallback> mReturnDataCallbackMap = new HashMap<>();

    private final CallbackAdapter mBleCallbackAdapter = new CallbackAdapter() {
        @Override
        public void onReadRssi(String bleAddress, String bleName, int rssi) {
            super.onReadRssi(bleAddress, bleName, rssi);
            if (mReadRssiCallback != null) {
                mReadRssiCallback.onReadRssiSuccess(rssi);
            }
        }

        @Override
        public void onNotifyData(String bleAddress, String bleName, BluetoothGattCharacteristic characteristic) {
            super.onNotifyData(bleAddress, bleName, characteristic);
            byte[] data = characteristic.getValue();
            parseData(bleAddress, bleName, data);
            dispatchRespData(HexUtil.bytesToHexStr(data));
        }


        @Override
        public void onWriteData(String bleAddress, String bleName, BluetoothGattCharacteristic characteristic, int errorCode) {
            super.onWriteData(bleAddress, bleName, characteristic, errorCode);

            Message message = mHandler.obtainMessage(Constants.MSG_WHAT_WRITE_NEXT);
            mHandler.sendMessageDelayed(message, 100);
        }

    };


    /**
     * ???????????????????????????uuid
     *
     * @return ServiceUUID
     */
    public abstract String getServiceUUID();

    /**
     * ??????????????????uuid
     *
     * @return WriteUUID
     */
    public abstract String getWriteUUID();

    /**
     * ?????????????????????uuid
     *
     * @return NotifyUUID
     */
    public abstract String getNotifyUUID();

    /**
     * ?????????BLE???????????????, ???????????????
     *
     * @param bleAddress bleAddress
     * @param bleName    bleName
     * @param data       ????????????
     */
    protected void parseData(String bleAddress, String bleName, byte[] data) {

    }


    public BaseDevice(String bleAddress, String bleName) {
        this.mBleAddress = bleAddress;
        this.mBleName = bleName;
        this.mBluetoothGatt = BleManager.getInstance().getConnectedGatt(bleAddress);
        if (this.mBluetoothGatt != null) {
            BleManager.getInstance().addBleCoreCallback(mBleCallbackAdapter);
            ConnectedPool.getInstance().addDevice(this);
            initHandler();
        } else {
            throw new IllegalArgumentException("BluetoothGatt is null!");
        }
    }


    @Nullable
    public String getBleAddress() {
        return this.mBleAddress;
    }

    @Nullable
    public String getBleName() {
        return this.mBleName;
    }


    /**
     * ??????????????????/??????
     *
     * @param enabled ??????????????????
     */
    public void setNotification(boolean enabled) {
        if (mBluetoothGatt != null) {
            BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(getServiceUUID()));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(getNotifyUUID()));
                if (characteristic != null) {
                    // ??????/?????? Android ???????????????
                    mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
                    //??? Characteristic ??? Descriptor ?????????????????????????????????????????????????????????????????????????????????????????????????????????
                    List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                    for (BluetoothGattDescriptor descriptor : descriptors) {
                        UUID uuid = descriptor.getUuid();
                        if (uuid != null) {
                            if (enabled) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            } else {
                                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                            }
                            // ????????????, ????????????
                            boolean success = mBluetoothGatt.writeDescriptor(descriptor);
                            LogUtil.i("setNotification: " + success);
                        }
                    }
                }
            } else {
                Log.e(TAG, "setNotification: ??????????????????");
            }
        } else {
            Log.e(TAG, "setNotification: Gatt Null");
        }
    }

    /**
     * ??????????????????
     *
     * @param readRssiCallback ????????????
     */
    public void readRssi(@NonNull ReadRssiCallback readRssiCallback) {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.readRemoteRssi();
            this.mReadRssiCallback = readRssiCallback;
        }
    }

    /**
     * ????????????
     */
    public void execute() {
        if (mDataQueue.size() == 1) {
            writeData();
        } else if (mDataQueue.size() > 1) {
            writeQueue();
        } else {
            throw new IllegalArgumentException("Data queue error!");
        }
    }

    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    protected <T> T enqueue(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data is null!");
        }
        if (mDataQueue.size() >= 1 && data.length > 20) {
            throw new IllegalArgumentException("Please split data!");
        }
        this.mDataQueue.offer(data);
        return (T) this;
    }

    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    protected <T> T enqueue(byte[] data, String filterTag, DataReceivedCallback dataReceivedCallback) {
        this.enqueue(data);
        boolean containsKey = mReturnDataCallbackMap.containsKey(filterTag);
        if (containsKey) {
            LogUtil.w("Enqueue: returnDataCallback will be replaced");
        }
        mReturnDataCallbackMap.put(filterTag, dataReceivedCallback);
        return (T) this;
    }

    private void writeData() {
        if (mDataQueue.peek() != null) {
            byte[] data = mDataQueue.poll();
            if (data != null && mDataQueue.size() == 0) {
                if (data.length >= 20) {
                    mDataQueue.addAll(splitByte(data, 20));
                    writeQueue();
                } else {
                    if (mBluetoothGatt != null) {
                        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(getServiceUUID()));
                        if (service != null) {
                            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(getWriteUUID()));
                            if (characteristic != null) {
                                characteristic.setValue(data);
                                boolean result = mBluetoothGatt.writeCharacteristic(characteristic);
                            }
                        }
                    }
                }
            }
        }
    }

    private void writeQueue() {
        if (mDataQueue.peek() != null) {
            byte[] data = mDataQueue.poll();
            if (mBluetoothGatt != null) {
                BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(getServiceUUID()));
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(getWriteUUID()));
                    if (characteristic != null) {
                        characteristic.setValue(data);
                        boolean result = mBluetoothGatt.writeCharacteristic(characteristic);
                    }
                }
            }
        }
    }

    private void initHandler() {
        mHandlerThread = new HandlerThread("splitWriter");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == Constants.MSG_WHAT_WRITE_NEXT) {
                    writeQueue();
                }
            }
        };
    }

    /**
     * ???????????????
     *
     * @param data  ??????
     * @param count ??????
     * @return ???????????????
     */
    private static Queue<byte[]> splitByte(byte[] data, int count) {
        if (count > 20) {
            LogUtil.w("Be careful: split count beyond 20! Ensure MTU higher than 23!");
        }
        Queue<byte[]> byteQueue = new LinkedList<>();
        int pkgCount;
        if (data.length % count == 0) {
            pkgCount = data.length / count;
        } else {
            pkgCount = Math.round(data.length / (float) count + 1);
        }
        if (pkgCount > 0) {
            for (int i = 0; i < pkgCount; i++) {
                byte[] dataPkg;
                int j;
                if (pkgCount == 1 || i == pkgCount - 1) {
                    j = data.length % count == 0 ? count : data.length % count;
                    System.arraycopy(data, i * count, dataPkg = new byte[j], 0, j);
                } else {
                    System.arraycopy(data, i * count, dataPkg = new byte[count], 0, count);
                }
                byteQueue.offer(dataPkg);
            }
        }
        return byteQueue;
    }

    /**
     * ??????????????????
     *
     * @param data data
     *             TODO: ??????byte[]
     */
    private void dispatchRespData(String data) {
        for (Map.Entry<String, DataReceivedCallback> returnDataCallbackEntry : mReturnDataCallbackMap.entrySet()) {
            String filterTag = returnDataCallbackEntry.getKey();
            String header = data.substring(0, filterTag.length());
            if (header.equalsIgnoreCase(filterTag)) {
                returnDataCallbackEntry.getValue().onDataReceived(data);
            }
        }
    }

    /**
     * ???????????????????????????
     */
    void release() {
//        this.mBluetoothGatt.disconnect();
        this.mBluetoothGatt.close();
        BleManager.getInstance().onDisconnect(this.mBleAddress, this.mBleName);
        this.mBluetoothGatt = null;
        BleManager.getInstance().removeBleCoreCallback(mBleCallbackAdapter);
    }
}
