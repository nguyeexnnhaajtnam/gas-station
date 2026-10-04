package vn.gasstation.pumpcode.application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.gasstation.pumpcode.domain.PumpCodeHistory;
@Service
public class PumpCodeHistoryService {
    private static final Logger log=LoggerFactory.getLogger(PumpCodeHistoryService.class);
    private final PumpCodeHistoryProvider provider;
    public PumpCodeHistoryService(PumpCodeHistoryProvider provider){this.provider=provider;}
    public Page<PumpCodeHistory> search(PumpCodeHistorySearchRequest request){
        log.info("[BUSINESS] event=pump-code.history.search.start page={} size={}",request.page(),request.size());
        Page<PumpCodeHistory> result=provider.find(request);
        log.info("[BUSINESS] event=pump-code.history.search.completed count={} totalItems={} page={}",result.getNumberOfElements(),result.getTotalElements(),result.getNumber());
        return result;
    }
}
