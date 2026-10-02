package vn.gasstation.integration.seenpro.client;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.infrastructure.logging.RequestLogContext;
import vn.gasstation.integration.seenpro.model.SeenProOnlinePayload;
import vn.gasstation.integration.seenpro.online.SeenProOnlineBootstrapIncompleteException;
import vn.gasstation.integration.seenpro.online.SeenProPumpDescriptor;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** SeenPro polling transport. Each endpoint owns its confirmed legacy form contract. */
@Component
public class SeenProPollingClient {
    private static final Logger log = LoggerFactory.getLogger(SeenProPollingClient.class);
    private final SeenProHttpClient http;
    private final SeenProSessionManager sessions;

    public SeenProPollingClient(SeenProHttpClient http, SeenProSessionManager sessions) {
        this.http = http;
        this.sessions = sessions;
    }

    public SeenProOnlinePayload fetchSnapshotInputs(SeenProPumpDescriptor descriptor) {
        requireContext(descriptor);
        return new SeenProOnlinePayload(descriptor,
            poll("money", descriptor, () -> getMoney(descriptor)),
            poll("liters", descriptor, () -> getLiters(descriptor)),
            poll("unit-price", descriptor, () -> getUnitPrice(descriptor)),
            poll("totalizer", descriptor, () -> getTotalizer(descriptor)),
            poll("connection-state", descriptor, () -> getConnectionState(descriptor)),
            poll("pump-state", descriptor, () -> getPumpState(descriptor)));
    }

    public String getConnectionState(SeenProPumpDescriptor descriptor) {
        return http.postForm("getconnectstate.php", Map.of(
            "master", descriptor.master(),
            "maCot", descriptor.pumpCode(),
            "standardizedMAC", descriptor.standardizedMAC()));
    }

    public String getMoney(SeenProPumpDescriptor descriptor) {
        return http.postForm("gettienhome.php", Map.of(
            "standardizedMAC", descriptor.standardizedMAC()));
    }

    public String getLiters(SeenProPumpDescriptor descriptor) {
        return http.postForm("getlithome.php", Map.of(
            "standardizedMAC", descriptor.standardizedMAC()));
    }

    public String getUnitPrice(SeenProPumpDescriptor descriptor) {
        return http.postForm("getgiahome.php", Map.of(
            "user", descriptor.user(),
            "maNhienLieu", descriptor.fuelId()));
    }

    public String getTotalizer(SeenProPumpDescriptor descriptor) {
        return http.postForm("gettotal.php", Map.of(
            "master", descriptor.master(),
            "slave", descriptor.slave()));
    }

    public String getPumpState(SeenProPumpDescriptor descriptor) {
        return http.postForm("getpumpstate.php", Map.of(
            "standardizedMAC", descriptor.standardizedMAC()));
    }

    private void requireContext(SeenProPumpDescriptor descriptor) {
        sessions.activeStationId().orElseThrow(() ->
            new LegacySystemUnavailableException("Chưa kích hoạt ngữ cảnh trạm"));
        if (descriptor == null) {
            throw new SeenProOnlineBootstrapIncompleteException(List.of("seenProPumpDescriptor"));
        }
    }

    private String poll(String metric, SeenProPumpDescriptor descriptor, Supplier<String> request) {
        try {
            return request.get();
        } catch (LegacySystemUnavailableException error) {
            RequestLogContext.warning();
            log.warn("[SEENPRO] event=polling.unavailable provider=seenpro metric={} pumpCode={} rootCause={}",
                metric, descriptor.pumpCode(), error.getClass().getSimpleName());
            return null;
        }
    }
}
