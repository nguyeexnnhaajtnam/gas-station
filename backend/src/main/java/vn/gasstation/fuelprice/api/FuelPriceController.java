package vn.gasstation.fuelprice.api;
import org.springframework.web.bind.annotation.*;import vn.gasstation.fuelprice.application.FuelPriceService;import vn.gasstation.fuelprice.domain.FuelPrice;import java.util.List;
@RestController @RequestMapping("/api/v1/fuel-prices") public class FuelPriceController {private final FuelPriceService service;
 public FuelPriceController(FuelPriceService service){this.service=service;}@GetMapping public List<FuelPrice> current(){return service.current();}}
