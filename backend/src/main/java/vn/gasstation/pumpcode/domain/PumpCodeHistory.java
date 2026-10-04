package vn.gasstation.pumpcode.domain;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
public record PumpCodeHistory(String pumpCode, String dispenser, String fuelType, BigDecimal unitPrice,
    BigDecimal volume, BigDecimal amount, OffsetDateTime finishedAt, String customer,
    InvoiceStatus invoiceStatus, String invoiceNumber) {}
