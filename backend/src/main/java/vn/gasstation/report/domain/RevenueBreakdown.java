package vn.gasstation.report.domain;
import java.math.BigDecimal;
public record RevenueBreakdown(String name,BigDecimal liters,BigDecimal revenue,long count) {}
