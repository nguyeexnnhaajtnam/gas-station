package vn.gasstation.integration.seenpro.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.company.domain.Company;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.mapper.SeenProStationMapper;
import vn.gasstation.integration.seenpro.parser.SeenProStationParser;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
import vn.gasstation.integration.seenpro.station.SeenProStationReferenceRegistry;
import vn.gasstation.station.application.StationProvider;
import vn.gasstation.station.domain.Station;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.data-source", havingValue = "seenpro", matchIfMissing = true)
public class SeenProStationProvider implements StationProvider {
    private static final Logger log = LoggerFactory.getLogger(SeenProStationProvider.class);
    private final SeenProHttpClient client;
    private final SeenProSessionManager sessions;
    private final SeenProStationParser parser;
    private final SeenProStationMapper mapper;
    private final SeenProStationReferenceRegistry references;

    public SeenProStationProvider(SeenProHttpClient client, SeenProSessionManager sessions,
                                  SeenProStationParser parser, SeenProStationMapper mapper,
                                  SeenProStationReferenceRegistry references) {
        this.client = client;
        this.sessions = sessions;
        this.parser = parser;
        this.mapper = mapper;
        this.references = references;
    }

    @Override
    public List<Station> findByCompany(Company company) {
        log.debug("[BUSINESS] event=station.list.start provider=seenpro companyId={} sessionEstablished={}",
            company.id(), sessions.authenticatedAccount().isPresent());
        sessions.authenticatedAccount().orElseThrow(() ->
            new LegacySystemUnavailableException("Phiên đăng nhập chưa được xác nhận"));
        if (company.code() == null || company.code().isBlank()) {
            throw new LegacySystemUnavailableException("Công ty chưa có mã tham chiếu nguồn dữ liệu");
        }
        String html = client.get("view.php", Map.of("gl", "3", "al", company.code(), "opt", "v"));
        var stations = parser.parse(html).stream().map(source -> {
            Station station = mapper.map(source, company.id());
            references.register(station, source.account());
            return station;
        }).toList();
        log.info("[BUSINESS] event=station.list.loaded provider=seenpro companyId={} count={}",
            company.id(), stations.size());
        return stations;
    }
}
