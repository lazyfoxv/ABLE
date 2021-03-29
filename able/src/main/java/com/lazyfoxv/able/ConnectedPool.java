package com.lazyfoxv.able;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by lazyfoxv on 2021-02-01.
 * Ble设备连接池
 */
public final class ConnectedPool {


    private final LinkedHashMap<String, BaseDevice> devices = new LinkedHashMap<>();

    private ConnectedPool() {
    }

    private static volatile ConnectedPool connectedPool;

    public static ConnectedPool getInstance() {
        if (connectedPool == null) {
            synchronized (ConnectedPool.class) {
                if (connectedPool == null) {
                    connectedPool = new ConnectedPool();
                }
            }
        }
        return connectedPool;
    }

    /**
     * 通过Mac地址获取设备
     *
     * @param bleAddress Mac地址
     * @return 设备
     */
    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    public <D extends BaseDevice> D getDevice(String bleAddress) {
        return (D) devices.get(bleAddress);
    }

    /**
     * 获取第一个设备, 单连接建议使用
     *
     * @return 设备
     */
    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    public <D extends BaseDevice> D getHeadDevice() {
        return (D) devices.entrySet().iterator().next().getValue();
    }

    /**
     * 获取所有设备
     *
     * @return 设备集合(LinkedHashMap)
     */
    public LinkedHashMap<String, BaseDevice> getDeviceMap() {
        return devices;
    }

    /**
     * 获取所有设备
     *
     * @return 设备集合(List)
     */
    public List<BaseDevice> getDeviceList() {
        return (List<BaseDevice>) devices.values();
    }

    /**
     * 添加设备
     *
     * @param device 设备
     * @param <D>    extends BaseDevice
     */
    <D extends BaseDevice> void addDevice(D device) {
        devices.put(device.getBleAddress(), device);
    }

    /**
     * 添加设备
     *
     * @param device     设备
     * @param bleAddress 地址
     */
    <D extends BaseDevice> void addDevice(String bleAddress, D device) {
        devices.put(bleAddress, device);
    }

    /**
     * 获取设备个数
     *
     * @return 个数
     */
    public int getSize() {
        return devices.size();
    }

    /**
     * 移除所有设备
     */
//    public void removeAll() {
//        devices.clear();
//    }

    /**
     * 通过Mac地址移除设备
     *
     * @param bleAddress Mac地址
     */
    void removeDevice(String bleAddress) {
        devices.remove(bleAddress);
    }
}
