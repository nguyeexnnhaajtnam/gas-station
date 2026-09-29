package vn.gasstation.station.api;

import vn.gasstation.station.domain.Station;

public record StationResponse(String id, String companyId, String code, String name, String phone, String email) {
    static StationResponse from(Station station) {
        return new StationResponse(station.id(), station.companyId(), station.code(), station.name(), station.phone(), station.email());
    }
}
