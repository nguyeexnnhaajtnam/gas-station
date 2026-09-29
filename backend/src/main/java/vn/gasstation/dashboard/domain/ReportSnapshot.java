package vn.gasstation.dashboard.domain;

import java.math.BigDecimal;

public record ReportSnapshot(BigDecimal revenue, BigDecimal litersSold, Long transactionCount) {}
