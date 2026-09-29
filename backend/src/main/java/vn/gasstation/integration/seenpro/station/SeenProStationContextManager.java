package vn.gasstation.integration.seenpro.station;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
import vn.gasstation.station.application.StationContextActivator;
import vn.gasstation.station.domain.Station;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.data-source", havingValue = "seenpro", matchIfMissing = true)
public class SeenProStationContextManager implements StationContextActivator {
    private static final Logger log = LoggerFactory.getLogger(SeenProStationContextManager.class);
    private final SeenProHttpClient client;
    private final SeenProSessionManager sessions;
    private final SeenProStationReferenceRegistry references;

    public SeenProStationContextManager(SeenProHttpClient client, SeenProSessionManager sessions,
                                        SeenProStationReferenceRegistry references) {
        this.client = client;
        this.sessions = sessions;
        this.references = references;
    }

    @Override
    public Optional<Station> activate(String stationId) {
        var reference = references.resolve(stationId);
        if (reference.isEmpty()) return Optional.empty();
        sessions.authenticatedAccount().orElseThrow(() ->
            new LegacySystemUnavailableException("Phiên đăng nhập nguồn dữ liệu đã hết hạn"));

        long started = System.nanoTime();
        log.info("seenpro.station-context started stationId={}", stationId);
        var response = client.getRedirect("view.php", Map.of(
            "gl", "2", "al", reference.get().legacyAccount(), "opt", "v"));
        if (response.statusCode() != 302 || !isMenuLocation(response.location())) {
            log.warn("seenpro.station-context rejected stationId={} status={} durationMs={}", stationId,
                response.statusCode(), elapsedMs(started));
            throw new LegacySystemUnavailableException("Không thể kích hoạt ngữ cảnh trạm trên nguồn dữ liệu");
        }
        log.info("seenpro.station-context activated stationId={} status={} durationMs={}", stationId,
            response.statusCode(), elapsedMs(started));
        sessions.markStationContext(stationId);
        return Optional.of(reference.get().station());
    }

    private static boolean isMenuLocation(String location) {
        if (location == null || location.isBlank()) return false;
        try {
            String path = URI.create(location.trim()).getPath();
            return path != null && (path.equals("menu.php") || path.equals("/menu.php"));
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static long elapsedMs(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }
}
