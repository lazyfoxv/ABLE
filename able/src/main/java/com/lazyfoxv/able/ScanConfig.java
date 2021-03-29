package com.lazyfoxv.able;


import java.util.UUID;

public final class ScanConfig {

    private final long timeout;
    private final boolean autoConnect;
    private final String[] filterBleNames;
    private final String[] filterBleAddress;
    private final UUID[] filterUuids;

    public long getTimeout() {
        return timeout;
    }

    public String[] getFilterBleNames() {
        return filterBleNames;
    }

    public String[] getFilterBleAddress() {
        return filterBleAddress;
    }

    public UUID[] getUuids() {
        return filterUuids;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    private ScanConfig(Builder builder) {
        this.timeout = builder.timeout;
        this.filterBleNames = builder.filterBleNames;
        this.filterUuids = builder.filterUuids;
        this.filterBleAddress = builder.filterBleAddress;
        this.autoConnect = builder.autoConnect;
    }

    public static class Builder {

        private long timeout;
        private boolean autoConnect;
        private String[] filterBleNames;
        private String[] filterBleAddress;
        private UUID[] filterUuids;

        public Builder setTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder filterBleNames(String... filterNames) {
            this.filterBleNames = filterNames;
            return this;
        }

        public Builder filterBleAddress(String... filterAddress) {
            this.filterBleAddress = filterAddress;
            return this;
        }

        public Builder filterUuids(UUID... filterUuids) {
            this.filterUuids = filterUuids;
            return this;
        }

        public Builder setAutoConnect(boolean autoConnect) {
            this.autoConnect = autoConnect;
            return this;
        }

        public ScanConfig build() {
            return new ScanConfig(this);
        }
    }
}
