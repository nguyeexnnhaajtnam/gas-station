package vn.gasstation.report.application;
import java.time.LocalDate;import vn.gasstation.report.domain.RevenueReport;
public interface RevenueReportProvider { RevenueReport get(LocalDate from,LocalDate to); }
