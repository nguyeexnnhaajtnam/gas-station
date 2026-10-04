package vn.gasstation.dashboard.application;

import org.springframework.stereotype.Service;
import vn.gasstation.dashboard.domain.DashboardSummary;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class DashboardService {
    private final DashboardProvider provider;

    public DashboardService(DashboardProvider provider) {
        this.provider = provider;
    }

    public DashboardSummary summary(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        return provider.summary(from == null ? today : from, to == null ? today : to);
    }
}
