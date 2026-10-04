package vn.gasstation.integration.seenpro.mapper;
import org.springframework.stereotype.Component;import vn.gasstation.integration.seenpro.model.SeenProTankRow;import vn.gasstation.tank.domain.Tank;import java.math.BigDecimal;import java.util.List;
@Component public class SeenProTankMapper {public Tank map(SeenProTankRow source){return new Tank(source.tankId(),source.tankName(),null,source.fuelName(),decimal(source.estimatedVolumeText()),List.of());}
 private BigDecimal decimal(String value){if(value==null||value.isBlank())return null;String clean=value.trim();int comma=clean.lastIndexOf(','),dot=clean.lastIndexOf('.');
  if(comma>dot)clean=clean.replace(".","").replace(',','.');else clean=clean.replace(",","");return new BigDecimal(clean);}}
