package vn.gasstation.dashboard.application;

import vn.gasstation.dashboard.domain.DashboardSummary;

import java.time.LocalDate;

public interface DashboardProvider {
    DashboardSummary summary(LocalDate from, LocalDate to);
}
