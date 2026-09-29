package vn.gasstation.integration.seenpro.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.gasstation.dashboard.application.PriceProvider;
import vn.gasstation.dashboard.domain.PriceSnapshot;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;

import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.data-source", havingValue = "seenpro", matchIfMissing = true)
public class SeenProPriceProvider implements PriceProvider {
    private final SeenProHttpClient client;
    private final SeenProSessionManager sessions;

    public SeenProPriceProvider(SeenProHttpClient client, SeenProSessionManager sessions) {
        this.client = client;
        this.sessions = sessions;
    }

    @Override
    public Optional<PriceSnapshot> current() {
        sessions.activeStationId().orElseThrow(() -> new LegacySystemUnavailableException("Chưa kích hoạt ngữ cảnh trạm"));
        client.get("quanlygia.php", Map.of());
        return Optional.of(new PriceSnapshot(true));
    }
}
