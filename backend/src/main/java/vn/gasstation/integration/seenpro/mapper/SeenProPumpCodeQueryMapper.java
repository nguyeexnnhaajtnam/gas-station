package vn.gasstation.integration.seenpro.mapper;
import org.springframework.stereotype.Component;
import vn.gasstation.pumpcode.application.PumpCodeHistorySearchRequest;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
@Component
public class SeenProPumpCodeQueryMapper {
    private static final DateTimeFormatter LEGACY_DATE=DateTimeFormatter.ISO_LOCAL_DATE;
    public Map<String,String> map(PumpCodeHistorySearchRequest request){
        var result=new LinkedHashMap<String,String>();
        result.put("t1",request.from()==null?"":LEGACY_DATE.format(request.from()));
        result.put("t2",request.to()==null?"":LEGACY_DATE.format(request.to()));
        result.put("kh",value(request.customer()));
        result.put("cb",value(request.pumpId()));
        result.put("nl",value(request.fuelType()));
        result.put("dkt","");
        result.put("ts",value(request.amountFilter()));
        result.put("dkl","");
        result.put("ls",value(request.volumeFilter()));
        result.put("tt",value(request.status()));
        result.put("sx",value(request.sort()));
        result.put("start",Integer.toString(Math.multiplyExact(request.page(),request.size())));
        return Map.copyOf(result);
    }
    private String value(String value){return value==null?"":value;}
}
