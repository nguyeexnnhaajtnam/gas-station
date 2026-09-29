package vn.gasstation.transaction.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Transaction(String id, String pumpId, String pumpCode, String fuelId, String fuelName,
                          BigDecimal unitPrice, BigDecimal liters, BigDecimal amount, String currency,
                          OffsetDateTime completedAt, String customerId, String customerName,
                          InvoiceState invoiceState, String invoiceNumber) {}

