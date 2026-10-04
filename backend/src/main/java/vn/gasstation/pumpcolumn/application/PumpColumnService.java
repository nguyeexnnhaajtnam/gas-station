package vn.gasstation.pumpcolumn.application;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.stereotype.Service;
import vn.gasstation.pumpcolumn.domain.PumpColumn;import java.util.List;
@Service public class PumpColumnService {
    private static final Logger log=LoggerFactory.getLogger(PumpColumnService.class);private final PumpColumnProvider provider;
    public PumpColumnService(PumpColumnProvider provider){this.provider=provider;}
    public List<PumpColumn> findAll(){var result=provider.findAll();log.info("[BUSINESS] event=pump-column.loaded count={}",result.size());return result;}
}
