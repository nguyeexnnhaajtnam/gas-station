package vn.gasstation.store.application;
import org.slf4j.*;import org.springframework.stereotype.Service;import vn.gasstation.store.domain.StoreDetails;
@Service public class StoreDetailsService {private static final Logger log=LoggerFactory.getLogger(StoreDetailsService.class);private final StoreDetailsProvider provider;
 public StoreDetailsService(StoreDetailsProvider provider){this.provider=provider;}public StoreDetails current(){var result=provider.current();log.info("[BUSINESS] event=store-info.loaded");return result;}}
