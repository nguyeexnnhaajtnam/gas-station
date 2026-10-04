package vn.gasstation.integration.seenpro.parser;
import org.jsoup.Jsoup;import org.jsoup.nodes.Element;import org.slf4j.*;import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProTankRow;import java.net.*;import java.nio.charset.StandardCharsets;import java.util.*;import java.util.regex.Pattern;
@Component public class SeenProTankHtmlParser {private static final Logger log=LoggerFactory.getLogger(SeenProTankHtmlParser.class);
 private static final Pattern ID=Pattern.compile("(?:\\?|&)mb=([^&]+)");private static final Pattern VOLUME=Pattern.compile("(-?[0-9.,]+)\\s*lít",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
 public List<SeenProTankRow> parse(String html){var document=Jsoup.parse(html==null?"":html);var cards=document.select(".noiDung2 .boxBon");
  log.info("[SEENPRO] event=parser.document parser=tank title={} cardCount={}",document.title(),cards.size());
  if(cards.isEmpty()){log.warn("[SEENPRO] event=parser.failed parser=tank failureStage=NO_TANK_CARDS");return List.of();}
  var result=new ArrayList<SeenProTankRow>();for(Element card:cards){String id=id(card),heading=text(card,".tenBon"),volume=volume(text(card,".tonKho"));
   if(id==null){log.warn("[SEENPRO] event=parser.row.skipped parser=tank reason=TANK_ID_MISSING");continue;}
   String[] names=heading==null?new String[0]:heading.split("\\s+-\\s+",2);String tankName=names.length>0?names[0].trim():null;String fuel=names.length>1?names[1].trim():null;
   result.add(new SeenProTankRow(id,tankName,fuel,volume));}log.info("[SEENPRO] event=parser.completed parser=tank count={}",result.size());return List.copyOf(result);}
 private String id(Element card){Element link=card.selectFirst("a[href*=mb]");if(link==null)return null;var matcher=ID.matcher(link.attr("href"));return matcher.find()?URLDecoder.decode(matcher.group(1),StandardCharsets.UTF_8):null;}
 private String volume(String text){if(text==null)return null;var matcher=VOLUME.matcher(text);return matcher.find()?matcher.group(1):null;}
 private String text(Element card,String selector){Element element=card.selectFirst(selector);return element==null?null:element.text().trim();}
}
