package vn.gasstation.integration.seenpro.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.gasstation.dashboard.application.StoreInfoProvider;
import vn.gasstation.dashboard.domain.StoreInfo;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;

import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.data-source", havingValue = "seenpro", matchIfMissing = true)
public class SeenProStoreInfoProvider implements StoreInfoProvider {
    private final SeenProHttpClient client;
    private final SeenProSessionManager sessions;

    public SeenProStoreInfoProvider(SeenProHttpClient client, SeenProSessionManager sessions) {
        this.client = client;
        this.sessions = sessions;
    }

    @Override
    public Optional<StoreInfo> current() {
        requireStationContext();
        client.get("quanlycuahang.php", Map.of());
        return Optional.of(new StoreInfo(true));
    }

    private void requireStationContext() {
        sessions.activeStationId().orElseThrow(() ->
            new LegacySystemUnavailableException("Chưa kích hoạt ngữ cảnh trạm"));
    }
}
