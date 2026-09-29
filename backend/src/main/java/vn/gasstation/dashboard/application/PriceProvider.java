package vn.gasstation.dashboard.application;

import vn.gasstation.dashboard.domain.PriceSnapshot;

import java.util.Optional;

public interface PriceProvider {
    Optional<PriceSnapshot> current();
}
