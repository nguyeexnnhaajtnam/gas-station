package vn.gasstation.integration.seenpro.provider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;import vn.gasstation.integration.seenpro.parser.SeenProStoreDetailsHtmlParser;
import vn.gasstation.store.application.StoreDetailsProvider;import vn.gasstation.store.domain.StoreDetails;import java.util.Map;
@Component @ConditionalOnProperty(name="app.data-source",havingValue="seenpro",matchIfMissing=true)
public class SeenProStoreDetailsAdapter implements StoreDetailsProvider {private final SeenProHttpClient client;private final SeenProStoreDetailsHtmlParser parser;
 public SeenProStoreDetailsAdapter(SeenProHttpClient client,SeenProStoreDetailsHtmlParser parser){this.client=client;this.parser=parser;}
 public StoreDetails current(){return parser.parse(client.get("thongtincuahang.php",Map.of()));}}
