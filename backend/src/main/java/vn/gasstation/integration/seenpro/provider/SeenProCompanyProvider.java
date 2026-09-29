package vn.gasstation.integration.seenpro.provider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.gasstation.company.application.CompanyProvider;
import vn.gasstation.company.domain.Company;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.mapper.SeenProCompanyMapper;
import vn.gasstation.integration.seenpro.parser.SeenProCompanyParser;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
import java.util.List;
import java.util.Map;
@Component @ConditionalOnProperty(name="app.data-source",havingValue="seenpro",matchIfMissing=true)
public class SeenProCompanyProvider implements CompanyProvider {
    private final SeenProHttpClient client; private final SeenProSessionManager sessions;
    private final SeenProCompanyParser parser; private final SeenProCompanyMapper mapper;
    public SeenProCompanyProvider(SeenProHttpClient client, SeenProSessionManager sessions, SeenProCompanyParser parser, SeenProCompanyMapper mapper) {
        this.client=client;this.sessions=sessions;this.parser=parser;this.mapper=mapper;
    }
    @Override public List<Company> findAll() {
        String account=sessions.authenticatedAccount().orElseThrow(() -> new LegacySystemUnavailableException("Phiên đăng nhập chưa được xác nhận"));
        String html=client.get("view.php", Map.of("gl","4","al",account,"opt","v"));
        return parser.parse(html).stream().map(mapper::map).toList();
    }
}
