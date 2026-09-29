package vn.gasstation.pump.domain;

import java.time.OffsetDateTime;

public record PumpLiveState(String pumpId, Status status, OffsetDateTime observedAt) {
    public enum Status { AVAILABLE, FUELING, OFFLINE, UNKNOWN }
}

