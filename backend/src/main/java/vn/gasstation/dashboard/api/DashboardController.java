package vn.gasstation.dashboard.api;

import org.springframework.web.bind.annotation.*;
import vn.gasstation.dashboard.application.DashboardService;
import vn.gasstation.dashboard.domain.DashboardSummary;
import java.time.LocalDate;

@RestController @RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }

    @GetMapping("/summary")
    public DashboardSummary summary(@RequestParam(required=false) LocalDate from, @RequestParam(required=false) LocalDate to) {
        return service.summary(from, to);
    }
}
