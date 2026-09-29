package vn.gasstation.dashboard.application;

import vn.gasstation.dashboard.domain.StoreInfo;

import java.util.Optional;

public interface StoreInfoProvider {
    Optional<StoreInfo> current();
}
