package vn.gasstation.pumpcode.application;
import java.time.LocalDate;
public record PumpCodeHistorySearchRequest(LocalDate from, LocalDate to, String pumpId, String fuelType,
    String customer, String amountFilter, String volumeFilter, String status, String sort, int page, int size) {
    public PumpCodeHistorySearchRequest {
        if (page < 0) throw new IllegalArgumentException("page must be non-negative");
        if (size < 1 || size > 200) throw new IllegalArgumentException("size must be between 1 and 200");
        if (from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException("from must not be after to");
    }
}
