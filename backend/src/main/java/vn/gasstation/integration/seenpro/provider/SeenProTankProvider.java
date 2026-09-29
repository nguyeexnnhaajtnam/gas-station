package vn.gasstation.integration.seenpro.provider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
import vn.gasstation.tank.application.TankProvider;
import vn.gasstation.tank.domain.Tank;
import java.util.List; import java.util.Map; import java.util.Optional;
@Component @ConditionalOnProperty(name="app.data-source", havingValue="seenpro", matchIfMissing=true)
public class SeenProTankProvider implements TankProvider {
    private final SeenProHttpClient client;
    private final SeenProSessionManager sessions;
    public SeenProTankProvider(SeenProHttpClient client, SeenProSessionManager sessions) { this.client = client; this.sessions = sessions; }
    public List<Tank> findAll() {
        sessions.activeStationId().orElseThrow(() -> new LegacySystemUnavailableException("Chưa kích hoạt ngữ cảnh trạm"));
        client.get("khohang.php", Map.of());
        throw new LegacySystemUnavailableException("Cấu trúc trang kho/bồn chưa được xác nhận");
    }
    public Optional<Tank> findById(String id) { return Optional.empty(); }
}
