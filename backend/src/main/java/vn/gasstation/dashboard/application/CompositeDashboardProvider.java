package vn.gasstation.dashboard.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.gasstation.dashboard.domain.DashboardSummary;
import vn.gasstation.dashboard.domain.PriceSnapshot;
import vn.gasstation.dashboard.domain.ReportSnapshot;
import vn.gasstation.dashboard.domain.StoreInfo;
import vn.gasstation.shared.application.DataProviderUnavailableException;
import vn.gasstation.pump.application.OnlineAggregator;
import vn.gasstation.pump.domain.PumpRealtime;
import vn.gasstation.pump.domain.PumpRealtimeSnapshot;
import vn.gasstation.tank.application.TankProvider;
import vn.gasstation.transaction.application.TransactionProvider;
import vn.gasstation.infrastructure.logging.RequestLogContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class CompositeDashboardProvider implements DashboardProvider {
    private static final Logger log = LoggerFactory.getLogger(CompositeDashboardProvider.class);
    private final StoreInfoProvider stores;
    private final ReportProvider reports;
    private final TankProvider tanks;
    private final PriceProvider prices;
    private final TransactionProvider transactions;
    private final OnlineAggregator online;

    public CompositeDashboardProvider(StoreInfoProvider stores, ReportProvider reports, TankProvider tanks,
                                      PriceProvider prices, TransactionProvider transactions, OnlineAggregator online) {
        this.stores = stores;
        this.reports = reports;
        this.tanks = tanks;
        this.prices = prices;
        this.transactions = transactions;
        this.online = online;
    }

    @Override
    public DashboardSummary summary(LocalDate from, LocalDate to) {
        var unavailable = new ArrayList<String>();
        Optional<StoreInfo> store = safely("STORE_INFO", stores::current, unavailable).flatMap(value -> value);
        Optional<ReportSnapshot> report = safely("REPORT", () -> reports.summary(from, to), unavailable).flatMap(value -> value);
        Optional<Integer> tankCount = safely("TANKS", () -> tanks.findAll().size(), unavailable);
        Optional<PriceSnapshot> price = safely("PRICES", prices::current, unavailable).flatMap(value -> value);
        Optional<PumpRealtimeSnapshot> realtime = safely("ONLINE", online::currentSnapshot, unavailable);
        if (realtime.isPresent() && !realtime.get().available()) unavailable.add("ONLINE");

        // TransactionProvider is intentionally not called until theodoibanhang.php has a confirmed parser fixture.
        // Keeping it as a composed dependency makes the future enrichment explicit without coupling current summary availability to it.
        if (transactions != null) unavailable.add("TRANSACTIONS");

        var summary = new DashboardSummary(
            report.map(ReportSnapshot::revenue).orElse(null),
            report.map(ReportSnapshot::litersSold).orElse(null),
            report.map(ReportSnapshot::transactionCount).orElse(null),
            tankCount.orElse(null),
            realtime.filter(PumpRealtimeSnapshot::available)
                .map(snapshot -> (int) snapshot.pumps().stream()
                    .filter(pump -> pump.connectionStatus() == PumpRealtime.ConnectionStatus.ONLINE).count())
                .orElse(null),
            realtime.filter(PumpRealtimeSnapshot::available).map(PumpRealtimeSnapshot::pumps).orElse(List.of()),
            store.map(StoreInfo::contextAvailable).orElse(false),
            price.map(PriceSnapshot::sourceAvailable).orElse(false),
            !unavailable.isEmpty(),
            unavailable
        );
        log.info("[BUSINESS] event=dashboard.summary.generated degraded={} unavailableSources={} pumpCount={}",
            summary.partial(), unavailable.size(), summary.pumpOverview().size());
        return summary;
    }

    private <T> Optional<T> safely(String source, Supplier<T> supplier, List<String> unavailable) {
        try {
            T value = supplier.get();
            if (value == null || (value instanceof Optional<?> optional && optional.isEmpty())) unavailable.add(source);
            return Optional.ofNullable(value);
        } catch (DataProviderUnavailableException error) {
            unavailable.add(source);
            RequestLogContext.warning();
            log.warn("[BUSINESS] event=dashboard.source.unavailable source={} rootCause={}", source, error.getClass().getSimpleName());
            return Optional.empty();
        }
    }
}
