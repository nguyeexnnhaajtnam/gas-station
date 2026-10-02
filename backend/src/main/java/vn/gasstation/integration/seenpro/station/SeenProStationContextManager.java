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
import vn.gasstation.infrastructure.logging.RequestLogContext;

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
    private final SeenProStationContextVerifier verifier;

    public SeenProStationContextManager(SeenProHttpClient client, SeenProSessionManager sessions,
                                        SeenProStationReferenceRegistry references,
                                        SeenProStationContextVerifier verifier) {
        this.client = client;
        this.sessions = sessions;
        this.references = references;
        this.verifier = verifier;
    }

    @Override
    public Optional<Station> activate(String stationId) {
        log.info("[BUSINESS] event=station.select.start stationId={} activeStationId={}",
            stationId, sessions.activeStationId().orElse("-"));
        var reference = references.resolve(stationId);
        if (reference.isEmpty()) {
            log.debug("[BUSINESS] event=station.select.skipped stationId={} reason=reference-not-found", stationId);
            return Optional.empty();
        }
        sessions.authenticatedAccount().orElseThrow(() ->
            new LegacySystemUnavailableException("Phiên đăng nhập nguồn dữ liệu đã hết hạn"));

        long started = System.nanoTime();
        log.debug("[SEENPRO] event=session.reused provider=seenpro stage=station-context cookieCount={}",
            sessions.cookieSnapshot().fingerprints().size());
        var response = client.getRedirect("view.php", Map.of(
            "gl", "2", "al", reference.get().legacyAccount(), "opt", "v"));
        if (response.statusCode() != 302 || !isMenuLocation(response.location())) {
            RequestLogContext.warning();
            log.warn("[BUSINESS] event=station.select.rejected stationId={} status={} durationMs={}", stationId,
                response.statusCode(), elapsedMs(started));
            throw new LegacySystemUnavailableException("Không thể kích hoạt ngữ cảnh trạm trên nguồn dữ liệu");
        }
        navigateRequired("menu.php", "menu");
        navigateAndVerifyStation("quanlycuahang.php", "store-info", reference.get().station());
        navigateAndVerifyStation("online.php", "online", reference.get().station());
        log.info("[BUSINESS] event=station.context.verified stationId={} status={} durationMs={}", stationId,
            response.statusCode(), elapsedMs(started));
        sessions.markStationContext(stationId);
        log.debug("[BUSINESS] event=station.context.persisted stationId={} activeStationId={}",
            stationId, sessions.activeStationId().orElse("-"));
        return Optional.of(reference.get().station());
    }

    private void navigateAndVerifyStation(String path, String stage, Station station) {
        var response = navigateRequired(path, stage);
        var verification = verifier.verify(response.body(), station.name());
        log.debug("[SEENPRO] event=parser.result provider=seenpro parser=station-context stage={} stationId={} matched={}",
            stage, station.id(), verification.matches());
        if (!verification.matches()) {
            throw new LegacySystemUnavailableException("SeenPro station context mismatch at " + path);
        }
    }

    private SeenProHttpClient.NavigationResponse navigateRequired(String path, String stage) {
        var before = sessions.cookieSnapshot();
        var response = client.navigate(path, Map.of());
        boolean cookieStoreChanged = !before.equals(sessions.cookieSnapshot());
        log.debug("[SEENPRO] event=navigation.result provider=seenpro stage={} path={} status={} cookieStoreChanged={}",
            stage, path, response.statusCode(), cookieStoreChanged);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new LegacySystemUnavailableException("SeenPro navigation failed at " + path
                + " with HTTP " + response.statusCode());
        }
        return response;
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
