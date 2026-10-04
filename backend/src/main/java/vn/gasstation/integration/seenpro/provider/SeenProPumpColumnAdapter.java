package vn.gasstation.integration.seenpro.provider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;import vn.gasstation.integration.seenpro.mapper.SeenProPumpColumnMapper;
import vn.gasstation.integration.seenpro.parser.SeenProPumpColumnHtmlParser;import vn.gasstation.pumpcolumn.application.PumpColumnProvider;
import vn.gasstation.pumpcolumn.domain.PumpColumn;import java.util.*;
@Component @ConditionalOnProperty(name="app.data-source",havingValue="seenpro",matchIfMissing=true)
public class SeenProPumpColumnAdapter implements PumpColumnProvider {
    private final SeenProHttpClient client;private final SeenProPumpColumnHtmlParser parser;private final SeenProPumpColumnMapper mapper;
    public SeenProPumpColumnAdapter(SeenProHttpClient client,SeenProPumpColumnHtmlParser parser,SeenProPumpColumnMapper mapper){this.client=client;this.parser=parser;this.mapper=mapper;}
    public List<PumpColumn> findAll(){return parser.parse(client.get("danhsachcotbom.php",Map.of())).stream().map(mapper::map).toList();}
}
