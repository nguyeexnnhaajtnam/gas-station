package vn.gasstation.store.api;
import org.springframework.web.bind.annotation.*;import vn.gasstation.store.application.StoreDetailsService;import vn.gasstation.store.domain.StoreDetails;
@RestController @RequestMapping("/api/v1/store-info") public class StoreDetailsController {private final StoreDetailsService service;
 public StoreDetailsController(StoreDetailsService service){this.service=service;}@GetMapping public StoreDetails current(){return service.current();}}
