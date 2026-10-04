package vn.gasstation.pumpcolumn.domain;
import java.math.BigDecimal;
public record PumpColumn(String id, String name, String fuelType, String deviceAddress,
    String displayName, String serialNumber, String connectionStatus, BigDecimal totalizer) {}
