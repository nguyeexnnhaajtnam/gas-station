package vn.gasstation.integration.seenpro.station;

import org.springframework.stereotype.Component;
import vn.gasstation.station.domain.Station;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SeenProStationReferenceRegistry {
    private final ConcurrentHashMap<String, Reference> references = new ConcurrentHashMap<>();

    public void register(Station station, String legacyAccount) {
        if (station.id() != null && legacyAccount != null && !legacyAccount.isBlank()) {
            references.put(station.id(), new Reference(station, legacyAccount));
        }
    }

    public Optional<Reference> resolve(String stationId) {
        return Optional.ofNullable(references.get(stationId));
    }

    public record Reference(Station station, String legacyAccount) {}
}
