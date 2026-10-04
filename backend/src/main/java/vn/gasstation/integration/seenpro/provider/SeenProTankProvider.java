package vn.gasstation.integration.seenpro.provider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.mapper.SeenProTankMapper;
import vn.gasstation.integration.seenpro.parser.SeenProTankHtmlParser;
import vn.gasstation.tank.application.TankProvider;
import vn.gasstation.tank.domain.Tank;
import java.util.List; import java.util.Map; import java.util.Optional;
@Component @ConditionalOnProperty(name="app.data-source", havingValue="seenpro", matchIfMissing=true)
public class SeenProTankProvider implements TankProvider {
    private final SeenProHttpClient client;
    private final SeenProTankHtmlParser parser;
    private final SeenProTankMapper mapper;
    public SeenProTankProvider(SeenProHttpClient client, SeenProTankHtmlParser parser, SeenProTankMapper mapper) {
        this.client = client; this.parser = parser; this.mapper = mapper;
    }
    public List<Tank> findAll() {
        return parser.parse(client.get("khohang.php", Map.of())).stream().map(mapper::map).toList();
    }
    public Optional<Tank> findById(String id) { return findAll().stream().filter(tank -> tank.id().equals(id)).findFirst(); }
}
