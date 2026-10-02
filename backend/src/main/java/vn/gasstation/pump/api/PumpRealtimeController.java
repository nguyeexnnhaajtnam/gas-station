package vn.gasstation.pump.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.gasstation.pump.application.PumpMonitoringService;
import vn.gasstation.pump.domain.PumpRealtime;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pumps")
public class PumpRealtimeController {
    private final PumpMonitoringService service;

    public PumpRealtimeController(PumpMonitoringService service) {
        this.service = service;
    }

    @GetMapping("/realtime")
    public List<PumpRealtime> realtime() {
        return service.realtime();
    }
}
