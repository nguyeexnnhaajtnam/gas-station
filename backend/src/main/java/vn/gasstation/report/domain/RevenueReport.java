package vn.gasstation.report.domain;
import java.math.BigDecimal;import java.time.LocalDate;import java.util.List;
public record RevenueReport(LocalDate from,LocalDate to,BigDecimal todayRevenue,BigDecimal todayLiters,long todayPumpCodeCount,BigDecimal todayInvoiceRevenue,BigDecimal todayInvoiceLiters,long todayInvoiceCount,List<RevenueBreakdown> fuels,List<RevenueBreakdown> pumps,List<RevenueBreakdown> invoices) {}
