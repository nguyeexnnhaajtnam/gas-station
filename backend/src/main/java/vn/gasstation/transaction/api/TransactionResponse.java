package vn.gasstation.transaction.api;

import vn.gasstation.transaction.domain.InvoiceState;
import vn.gasstation.transaction.domain.Transaction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionResponse(String id, String pumpId, String pumpCode, String fuelId, String fuelName,
    BigDecimal unitPrice, BigDecimal liters, BigDecimal amount, String currency, OffsetDateTime completedAt,
    String customerId, String customerName, InvoiceState invoiceState, String invoiceNumber) {
    static TransactionResponse from(Transaction t) { return new TransactionResponse(t.id(), t.pumpId(), t.pumpCode(), t.fuelId(), t.fuelName(), t.unitPrice(), t.liters(), t.amount(), t.currency(), t.completedAt(), t.customerId(), t.customerName(), t.invoiceState(), t.invoiceNumber()); }
}

