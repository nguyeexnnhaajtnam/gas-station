package vn.gasstation.integration.seenpro.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.gasstation.dashboard.application.ReportProvider;
import vn.gasstation.dashboard.domain.ReportSnapshot;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.data-source", havingValue = "seenpro", matchIfMissing = true)
public class SeenProReportProvider implements ReportProvider {
    private final SeenProHttpClient client;
    private final SeenProSessionManager sessions;

    public SeenProReportProvider(SeenProHttpClient client, SeenProSessionManager sessions) {
        this.client = client;
        this.sessions = sessions;
    }

    @Override
    public Optional<ReportSnapshot> summary(LocalDate from, LocalDate to) {
        sessions.activeStationId().orElseThrow(() -> new LegacySystemUnavailableException("Chưa kích hoạt ngữ cảnh trạm"));
        client.get("baocao.php", Map.of());
        // TODO parse metrics after a sanitized baocao.php fixture confirms selectors and date query parameters.
        return Optional.empty();
    }
}
