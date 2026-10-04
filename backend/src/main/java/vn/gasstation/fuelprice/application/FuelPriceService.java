package vn.gasstation.fuelprice.application;
import org.slf4j.*;import org.springframework.stereotype.Service;import vn.gasstation.fuelprice.domain.FuelPrice;import java.util.List;
@Service public class FuelPriceService {private static final Logger log=LoggerFactory.getLogger(FuelPriceService.class);private final FuelPriceProvider provider;
 public FuelPriceService(FuelPriceProvider provider){this.provider=provider;}public List<FuelPrice> current(){var result=provider.current();log.info("[BUSINESS] event=fuel-price.loaded count={}",result.size());return result;}}
