package vn.gasstation.integration.seenpro.parser;
import org.slf4j.*;import org.springframework.stereotype.Component;import vn.gasstation.integration.seenpro.model.SeenProFuelPriceDescriptor;
import java.util.*;import java.util.regex.Pattern;
@Component public class SeenProFuelPriceBootstrapParser {private static final Logger log=LoggerFactory.getLogger(SeenProFuelPriceBootstrapParser.class);
 private static final Pattern CALL=Pattern.compile("(?:waitTimeUpdate|giaUpdate)\\s*\\(\\s*(['\"])(.*?)\\1\\s*,\\s*(['\"])(.*?)\\3\\s*,",Pattern.CASE_INSENSITIVE);
 public List<SeenProFuelPriceDescriptor> parse(String html){var unique=new LinkedHashMap<String,SeenProFuelPriceDescriptor>();var matcher=CALL.matcher(html==null?"":html);
  while(matcher.find()){String user=matcher.group(2),fuel=matcher.group(4);unique.putIfAbsent(user+"\u0000"+fuel,new SeenProFuelPriceDescriptor(user,fuel));}
  if(unique.isEmpty()){log.warn("[SEENPRO] event=parser.failed parser=fuel-price-bootstrap failureStage=NO_POLLING_INVOCATIONS");return List.of();}
  log.info("[SEENPRO] event=parser.completed parser=fuel-price-bootstrap count={}",unique.size());return List.copyOf(unique.values());}}
