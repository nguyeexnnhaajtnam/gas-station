package vn.gasstation.fuelprice.domain;
import java.math.BigDecimal;import java.time.OffsetDateTime;
public record FuelPrice(String id,String fuelName,BigDecimal unitPrice,OffsetDateTime effectiveAt,String updateStatus) {}
