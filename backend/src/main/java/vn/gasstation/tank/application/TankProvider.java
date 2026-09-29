package vn.gasstation.tank.application;
import vn.gasstation.tank.domain.Tank;
import java.util.List;
import java.util.Optional;
public interface TankProvider { List<Tank> findAll(); Optional<Tank> findById(String id); }

