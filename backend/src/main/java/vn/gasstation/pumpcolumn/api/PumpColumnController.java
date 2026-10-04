package vn.gasstation.pumpcolumn.api;
import org.springframework.web.bind.annotation.*;import vn.gasstation.pumpcolumn.application.PumpColumnService;
import vn.gasstation.pumpcolumn.domain.PumpColumn;import java.util.List;
@RestController @RequestMapping("/api/v1/pump-columns") public class PumpColumnController {
    private final PumpColumnService service;public PumpColumnController(PumpColumnService service){this.service=service;}
    @GetMapping public List<PumpColumn> findAll(){return service.findAll();}
}
