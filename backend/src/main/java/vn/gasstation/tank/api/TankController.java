package vn.gasstation.tank.api;
import org.springframework.web.bind.annotation.*;
import vn.gasstation.tank.application.TankService;
import vn.gasstation.tank.domain.Tank;
import java.util.List;
@RestController @RequestMapping("/api/v1/tanks")
public class TankController {
    private final TankService service;
    public TankController(TankService service) { this.service = service; }
    @GetMapping public List<Tank> all() { return service.findAll(); }
}
