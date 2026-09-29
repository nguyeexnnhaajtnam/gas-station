package vn.gasstation.dashboard.application;

import vn.gasstation.dashboard.domain.ReportSnapshot;

import java.time.LocalDate;
import java.util.Optional;

public interface ReportProvider {
    Optional<ReportSnapshot> summary(LocalDate from, LocalDate to);
}
