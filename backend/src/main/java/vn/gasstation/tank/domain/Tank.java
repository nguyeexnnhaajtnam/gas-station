package vn.gasstation.tank.domain;

import java.math.BigDecimal;
import java.util.List;

public record Tank(String id, String name, String fuelId, String fuelName, BigDecimal estimatedVolumeLiters,
                   List<String> pumpIds) {
    public Tank { pumpIds = pumpIds == null ? List.of() : List.copyOf(pumpIds); }
}

