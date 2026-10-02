package vn.gasstation.integration.seenpro.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.integration.seenpro.client.SeenProPollingClient;
import vn.gasstation.integration.seenpro.mapper.SeenProPumpRealtimeMapper;
import vn.gasstation.integration.seenpro.online.SeenProOnlineBootstrapProvider;
import vn.gasstation.pump.application.PumpRealtimeProvider;
import vn.gasstation.pump.domain.PumpRealtimeSnapshot;

@Component
@ConditionalOnProperty(name = "app.data-source", havingValue = "seenpro", matchIfMissing = true)
public class SeenProPumpRealtimeProvider implements PumpRealtimeProvider {
    private static final Logger log = LoggerFactory.getLogger(SeenProPumpRealtimeProvider.class);
    private final SeenProPollingClient client;
    private final SeenProPumpRealtimeMapper mapper;
    private final SeenProOnlineBootstrapProvider bootstrap;

    public SeenProPumpRealtimeProvider(SeenProPollingClient client, SeenProPumpRealtimeMapper mapper,
                                       SeenProOnlineBootstrapProvider bootstrap) {
        this.client = client;
        this.mapper = mapper;
        this.bootstrap = bootstrap;
    }

    @Override
    public PumpRealtimeSnapshot currentSnapshot() {
        var descriptors = bootstrap.descriptors();
        log.info("[SEENPRO] event=parser.result provider=seenpro parser=online-bootstrap pumpDescriptors={}",
            descriptors.size());
        var payloads = descriptors.stream().map(client::fetchSnapshotInputs).toList();
        var snapshot = mapper.map(payloads);
        log.info("[BUSINESS] event=pump-realtime.generated count={} available={}",
            snapshot.pumps().size(), snapshot.available());
        return snapshot;
    }
}
