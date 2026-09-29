package vn.gasstation.integration.seenpro.mapper;

import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProStationModel;
import vn.gasstation.station.domain.Station;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class SeenProStationMapper {
    public Station map(SeenProStationModel source, String companyId) {
        String account = clean(source.account());
        String id = account == null ? null : UUID.nameUUIDFromBytes(
            ("seenpro-station:" + account).getBytes(StandardCharsets.UTF_8)).toString();
        return new Station(id, companyId, account, clean(source.name()), clean(source.phone()), clean(source.email()));
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim().replaceAll("\\s+", " ");
    }
}
