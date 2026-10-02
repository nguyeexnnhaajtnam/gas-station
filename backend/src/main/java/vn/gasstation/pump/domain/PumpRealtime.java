package vn.gasstation.pump.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PumpRealtime(
    String id,
    String number,
    String fuelType,
    BigDecimal money,
    BigDecimal liters,
    BigDecimal unitPrice,
    BigDecimal totalizer,
    ConnectionStatus connectionStatus,
    OperationalStatus operationalStatus,
    NozzleStatus nozzleStatus,
    OffsetDateTime observedAt
) {
    public enum ConnectionStatus { ONLINE, OFFLINE, UNKNOWN }
    public enum OperationalStatus { IDLE, FUELING, MAINTENANCE, UNKNOWN }
    public enum NozzleStatus { HUNG, LIFTED, DISPENSING, UNKNOWN }
}
