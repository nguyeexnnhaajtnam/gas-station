package vn.gasstation.integration.seenpro.provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.mapper.*;
import vn.gasstation.integration.seenpro.parser.SeenProPumpCodeHistoryHtmlParser;
import vn.gasstation.pumpcode.application.*;
import vn.gasstation.pumpcode.domain.PumpCodeHistory;
@Component
@ConditionalOnProperty(name="app.data-source",havingValue="seenpro",matchIfMissing=true)
public class SeenProPumpCodeAdapter implements PumpCodeHistoryProvider {
    private static final Logger log=LoggerFactory.getLogger(SeenProPumpCodeAdapter.class);
    private final SeenProHttpClient client;private final SeenProPumpCodeQueryMapper queryMapper;
    private final SeenProPumpCodeHistoryHtmlParser parser;private final SeenProPumpCodeHistoryMapper mapper;
    public SeenProPumpCodeAdapter(SeenProHttpClient client,SeenProPumpCodeQueryMapper queryMapper,
        SeenProPumpCodeHistoryHtmlParser parser,SeenProPumpCodeHistoryMapper mapper){
        this.client=client;this.queryMapper=queryMapper;this.parser=parser;this.mapper=mapper;
    }
    public Page<PumpCodeHistory> find(PumpCodeHistorySearchRequest request){
        String html=client.get("theodoibanhang.php",queryMapper.map(request));
        var items=parser.parse(html).stream().map(mapper::map).toList();
        boolean hasNext=items.size()>=request.size();
        long total=(long)request.page()*request.size()+items.size()+(hasNext?1:0);
        log.info("[SEENPRO] event=parser.completed provider=seenpro parser=pump-code-history count={} page={}",items.size(),request.page());
        return new PageImpl<>(items,PageRequest.of(request.page(),request.size()),total);
    }
}
