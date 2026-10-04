package vn.gasstation.integration.seenpro.provider;
import org.jsoup.Jsoup;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.stereotype.Component;
import vn.gasstation.fuelprice.application.FuelPriceProvider;import vn.gasstation.fuelprice.domain.FuelPrice;import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.model.SeenProFuelPriceDescriptor;import vn.gasstation.integration.seenpro.parser.SeenProFuelPriceBootstrapParser;
import java.math.BigDecimal;import java.time.*;import java.time.format.DateTimeFormatter;import java.util.*;
@Component @ConditionalOnProperty(name="app.data-source",havingValue="seenpro",matchIfMissing=true)
public class SeenProFuelPriceAdapter implements FuelPriceProvider {private final SeenProHttpClient client;private final SeenProFuelPriceBootstrapParser parser;
 public SeenProFuelPriceAdapter(SeenProHttpClient client,SeenProFuelPriceBootstrapParser parser){this.client=client;this.parser=parser;}
 public List<FuelPrice> current(){String html=client.get("quanlygia.php",Map.of());return parser.parse(html).stream().map(this::poll).toList();}
 private FuelPrice poll(SeenProFuelPriceDescriptor descriptor){Map<String,String> form=Map.of("user",descriptor.user(),"tenNhienLieu",descriptor.fuelName());
  String time=client.postForm("waittimeupdate.php",form),price=client.postForm("giaupdate.php",form);
  return new FuelPrice(id(descriptor.fuelName()),descriptor.fuelName(),decimal(price),effectiveAt(time),status(time));}
 private String id(String value){return value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+","-").replaceAll("(^-|-$)","");}
 private BigDecimal decimal(String value){String clean=value==null?"":value.trim().replaceAll("[^0-9,.-]","");if(clean.isBlank())return null;
  if(clean.matches("-?\\d{1,3}(\\.\\d{3})+"))return new BigDecimal(clean.replace(".",""));
  int comma=clean.lastIndexOf(','),dot=clean.lastIndexOf('.');if(comma>dot)clean=clean.replace(".","").replace(',','.');else clean=clean.replace(",","");return new BigDecimal(clean);}
 private OffsetDateTime effectiveAt(String html){String text=Jsoup.parse(html==null?"":html).text();var matcher=java.util.regex.Pattern.compile("(\\d{2}:\\d{2}:\\d{2})\\s*-\\s*(\\d{2}/\\d{2}/\\d{4})").matcher(text);
  if(!matcher.find())return null;var local=LocalDateTime.parse(matcher.group(1)+" "+matcher.group(2),DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));return local.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toOffsetDateTime();}
 private String status(String html){String text=Jsoup.parse(html==null?"":html).text().toLowerCase(Locale.forLanguageTag("vi"));if(text.contains("đã cập nhật"))return "UPDATED";if(text.contains("chờ")||text.contains("đặt lịch"))return "SCHEDULED";return "UNKNOWN";}
}
