package vn.gasstation.integration.seenpro.mapper;
import org.springframework.stereotype.Component;import vn.gasstation.integration.seenpro.model.SeenProPumpColumnRow;
import vn.gasstation.pumpcolumn.domain.PumpColumn;import java.math.BigDecimal;import java.util.Locale;
@Component public class SeenProPumpColumnMapper {
    public PumpColumn map(SeenProPumpColumnRow source){return new PumpColumn(clean(source.pumpCode()),clean(source.name()),clean(source.fuelLabel()),
        clean(source.deviceAddress()),configured(source.displayName()),configured(source.serialNumber()),connection(source.connectionStatus()),decimal(source.totalizerText()));}
    private String configured(String value){String clean=clean(value);return clean==null||clean.equalsIgnoreCase("Chưa cài đặt")?null:clean;}
    private String connection(String value){String clean=clean(value);if(clean==null)return "UNKNOWN";String upper=clean.toUpperCase(Locale.ROOT);
        return upper.equals("ONLINE")||upper.equals("OFFLINE")?upper:"UNKNOWN";}
    private BigDecimal decimal(String value){String clean=clean(value);if(clean==null)return null;clean=clean.replaceAll("[^0-9,.-]","");if(clean.isBlank())return null;
        int comma=clean.lastIndexOf(','),dot=clean.lastIndexOf('.');if(comma>dot)clean=clean.replace(".","").replace(',','.');
        else if(dot>=0&&comma<0&&clean.length()-dot-1==3)clean=clean.replace(".","");else clean=clean.replace(",","");return new BigDecimal(clean);}
    private String clean(String value){return value==null||value.isBlank()?null:value.trim().replaceAll("\\s+"," ");}
}
