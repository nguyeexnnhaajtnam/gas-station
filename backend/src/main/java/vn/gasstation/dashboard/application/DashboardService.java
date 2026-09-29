package vn.gasstation.dashboard.application;

import org.springframework.stereotype.Service;
import vn.gasstation.dashboard.domain.DashboardSummary;

import java.time.LocalDate;

@Service
public class DashboardService {
    private final DashboardProvider provider;

    public DashboardService(DashboardProvider provider) {
        this.provider = provider;
    }

    public DashboardSummary summary(LocalDate from, LocalDate to) {
        return provider.summary(from, to);
    }
}
