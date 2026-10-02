package vn.gasstation.pump.application;

import vn.gasstation.pump.domain.PumpRealtimeSnapshot;

public interface PumpRealtimeProvider {
    PumpRealtimeSnapshot currentSnapshot();
}
