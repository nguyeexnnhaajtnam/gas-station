package vn.gasstation.dashboard.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.gasstation.dashboard.domain.DashboardSummary;
import vn.gasstation.dashboard.domain.PriceSnapshot;
import vn.gasstation.dashboard.domain.ReportSnapshot;
import vn.gasstation.dashboard.domain.StoreInfo;
import vn.gasstation.shared.application.DataProviderUnavailableException;
import vn.gasstation.tank.application.TankProvider;
import vn.gasstation.transaction.application.TransactionProvider;

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

    public CompositeDashboardProvider(StoreInfoProvider stores, ReportProvider reports, TankProvider tanks,
                                      PriceProvider prices, TransactionProvider transactions) {
        this.stores = stores;
        this.reports = reports;
        this.tanks = tanks;
        this.prices = prices;
        this.transactions = transactions;
    }

    @Override
    public DashboardSummary summary(LocalDate from, LocalDate to) {
        var unavailable = new ArrayList<String>();
        Optional<StoreInfo> store = safely("STORE_INFO", stores::current, unavailable).flatMap(value -> value);
        Optional<ReportSnapshot> report = safely("REPORT", () -> reports.summary(from, to), unavailable).flatMap(value -> value);
        Optional<Integer> tankCount = safely("TANKS", () -> tanks.findAll().size(), unavailable);
        Optional<PriceSnapshot> price = safely("PRICES", prices::current, unavailable).flatMap(value -> value);

        // TransactionProvider is intentionally not called until theodoibanhang.php has a confirmed parser fixture.
        // Keeping it as a composed dependency makes the future enrichment explicit without coupling current summary availability to it.
        if (transactions != null) unavailable.add("TRANSACTIONS");

        return new DashboardSummary(
            report.map(ReportSnapshot::revenue).orElse(null),
            report.map(ReportSnapshot::litersSold).orElse(null),
            report.map(ReportSnapshot::transactionCount).orElse(null),
            tankCount.orElse(null),
            store.map(StoreInfo::contextAvailable).orElse(false),
            price.map(PriceSnapshot::sourceAvailable).orElse(false),
            !unavailable.isEmpty(),
            unavailable
        );
    }

    private <T> Optional<T> safely(String source, Supplier<T> supplier, List<String> unavailable) {
        try {
            T value = supplier.get();
            if (value == null || (value instanceof Optional<?> optional && optional.isEmpty())) unavailable.add(source);
            return Optional.ofNullable(value);
        } catch (DataProviderUnavailableException error) {
            unavailable.add(source);
            log.warn("dashboard.source unavailable source={} errorType={}", source, error.getClass().getSimpleName());
            return Optional.empty();
        }
    }
}
