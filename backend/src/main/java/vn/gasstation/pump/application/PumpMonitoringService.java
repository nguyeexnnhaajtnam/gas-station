package vn.gasstation.pump.application;

import org.springframework.stereotype.Service;
import vn.gasstation.pump.domain.PumpRealtime;

import java.util.List;

@Service
public class PumpMonitoringService {
    private final PumpRealtimeProvider provider;

    public PumpMonitoringService(PumpRealtimeProvider provider) {
        this.provider = provider;
    }

    public List<PumpRealtime> realtime() {
        return provider.currentSnapshot().pumps();
    }
}
