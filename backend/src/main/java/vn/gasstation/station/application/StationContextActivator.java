package vn.gasstation.station.application;

import vn.gasstation.station.domain.Station;

import java.util.Optional;

public interface StationContextActivator {
    Optional<Station> activate(String stationId);
}
