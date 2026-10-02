package vn.gasstation.pump.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record PumpRealtimeSnapshot(List<PumpRealtime> pumps, boolean available, OffsetDateTime observedAt) {
    public PumpRealtimeSnapshot {
        pumps = pumps == null ? List.of() : List.copyOf(pumps);
    }

    public static PumpRealtimeSnapshot unavailable(OffsetDateTime observedAt) {
        return new PumpRealtimeSnapshot(List.of(), false, observedAt);
    }
}
