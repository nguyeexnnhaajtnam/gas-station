package vn.gasstation.integration.seenpro.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.gasstation.dashboard.application.ReportProvider;
import vn.gasstation.dashboard.domain.ReportSnapshot;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
import vn.gasstation.integration.seenpro.mapper.SeenProRevenueReportMapper;
import vn.gasstation.integration.seenpro.parser.SeenProRevenueReportHtmlParser;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.data-source", havingValue = "seenpro", matchIfMissing = true)
public class SeenProReportProvider implements ReportProvider {
    private final SeenProHttpClient client;
    private final SeenProSessionManager sessions;
    private final SeenProRevenueReportHtmlParser parser;
    private final SeenProRevenueReportMapper mapper;

    public SeenProReportProvider(SeenProHttpClient client, SeenProSessionManager sessions, SeenProRevenueReportHtmlParser parser, SeenProRevenueReportMapper mapper) {
        this.client = client;
        this.sessions = sessions;
        this.parser = parser;
        this.mapper = mapper;
    }

    @Override
    public Optional<ReportSnapshot> summary(LocalDate from, LocalDate to) {
        sessions.activeStationId().orElseThrow(() -> new LegacySystemUnavailableException("Chưa kích hoạt ngữ cảnh trạm"));
        var query = Map.of("t1", from.toString(), "g1", "", "t2", to.toString(), "g2", "");
        var report = mapper.map(parser.parse(client.get("baocao.php", query)), from, to);
        var revenue = report.fuels().stream().map(v -> v.revenue()).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        var liters = report.fuels().stream().map(v -> v.liters()).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        var count = report.fuels().stream().mapToLong(v -> v.count()).sum();
        return Optional.of(new ReportSnapshot(revenue, liters, count));
    }
}
