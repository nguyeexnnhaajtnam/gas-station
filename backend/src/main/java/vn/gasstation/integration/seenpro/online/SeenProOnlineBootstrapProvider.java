package vn.gasstation.integration.seenpro.online;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
import vn.gasstation.infrastructure.logging.RequestLogContext;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "app.data-source", havingValue = "seenpro", matchIfMissing = true)
public class SeenProOnlineBootstrapProvider {
    private static final Logger log = LoggerFactory.getLogger(SeenProOnlineBootstrapProvider.class);
    private final SeenProHttpClient client;
    private final SeenProSessionManager sessions;
    private final SeenProOnlineParser parser;
    private final ConcurrentHashMap<String, List<SeenProPumpDescriptor>> byStation = new ConcurrentHashMap<>();

    public SeenProOnlineBootstrapProvider(SeenProHttpClient client, SeenProSessionManager sessions,
                                          SeenProOnlineParser parser) {
        this.client = client;
        this.sessions = sessions;
        this.parser = parser;
    }

    public List<SeenProPumpDescriptor> descriptors() {
        String stationId = sessions.activeStationId().orElseThrow(() ->
            new SeenProOnlineBootstrapIncompleteException(List.of("activeStationContext")));
        List<SeenProPumpDescriptor> cached = byStation.get(stationId);
        if (cached != null) {
            RequestLogContext.cacheHit();
            log.debug("[CACHE] event=cache.hit cache=pump-descriptor stationId={} count={}", stationId, cached.size());
            return cached;
        }
        RequestLogContext.cacheMiss();
        log.debug("[CACHE] event=cache.miss cache=pump-descriptor stationId={}", stationId);
        return byStation.computeIfAbsent(stationId, ignored -> load(stationId));
    }

    private List<SeenProPumpDescriptor> load(String stationId) {
        String html = client.get("online.php", java.util.Map.of());
        List<SeenProPumpDescriptor> descriptors = parser.parse(html);
        log.info("[CACHE] event=pump-descriptor.loaded cache=pump-descriptor stationId={} count={}",
            stationId, descriptors.size());
        return descriptors;
    }
}
