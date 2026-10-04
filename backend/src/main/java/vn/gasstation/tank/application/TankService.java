package vn.gasstation.tank.application;
import org.springframework.stereotype.Service;
import vn.gasstation.tank.domain.Tank;
import java.util.List;
@Service
public class TankService {
    private final TankProvider provider;
    public TankService(TankProvider provider) { this.provider = provider; }
    public List<Tank> findAll() { return provider.findAll(); }
}

