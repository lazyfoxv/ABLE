package com.lazyfoxv.ble;

import androidx.annotation.NonNull;

import com.lazyfoxv.able.BaseDevice;
import com.lazyfoxv.able.callback.DataReceivedCallback;
import com.lazyfoxv.able.util.HexUtil;

public class TestDevice extends BaseDevice {

    public TestDevice(String bleAddress, String bleName) {
        super(bleAddress, bleName);
    }

    @Override
    public String getServiceUUID() {
        return null;
    }

    @Override
    public String getWriteUUID() {
        return null;
    }

    @Override
    public String getNotifyUUID() {
        this.setSpeed().execute();
        return null;
    }

    public TestDevice setSpeed() {
        return this.enqueue(HexUtil.hexStrToBytes("999"));
    }

    public TestDevice setBrightness() {
        return enqueue(HexUtil.hexStrToBytes("10101010"));
    }

    public TestDevice setMode(int mode, DataReceivedCallback dataReceivedCallback){
        String m = HexUtil.deciToHexStrOfOneByte(mode);
        return enqueue(HexUtil.hexStrToBytes(m), "F1", dataReceivedCallback);
    }
}
