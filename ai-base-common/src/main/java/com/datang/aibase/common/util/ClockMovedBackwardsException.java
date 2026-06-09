package com.datang.aibase.common.util;

public class ClockMovedBackwardsException extends RuntimeException {

    private final long lastTimestamp;
    private final long currentTimestamp;

    public ClockMovedBackwardsException(long lastTimestamp, long currentTimestamp) {
        super(String.format("Clock moved backwards: last=%d, current=%d, diff=%dms",
                lastTimestamp, currentTimestamp, currentTimestamp - lastTimestamp));
        this.lastTimestamp = lastTimestamp;
        this.currentTimestamp = currentTimestamp;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public long getCurrentTimestamp() {
        return currentTimestamp;
    }
}
