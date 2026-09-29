package vn.gasstation.transaction.application;

import java.time.LocalDate;

public record TransactionQuery(LocalDate from, LocalDate to, String customerId, String pumpId,
                               String fuelId, int page, int size) {
    public TransactionQuery {
        if (page < 0) throw new IllegalArgumentException("page must be non-negative");
        if (size < 1 || size > 200) throw new IllegalArgumentException("size must be between 1 and 200");
        if (from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException("from must not be after to");
    }
}

