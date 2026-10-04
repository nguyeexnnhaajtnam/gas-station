package vn.gasstation.integration.seenpro.parser;
import org.jsoup.Jsoup;import org.jsoup.nodes.Element;import org.slf4j.*;import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProPumpColumnRow;
import java.net.URLDecoder;import java.nio.charset.StandardCharsets;import java.text.Normalizer;import java.util.*;import java.util.regex.Pattern;
@Component public class SeenProPumpColumnHtmlParser {
    private static final Logger log=LoggerFactory.getLogger(SeenProPumpColumnHtmlParser.class);
    private static final Pattern PUMP_CODE=Pattern.compile("(?:\\?|&)maCot=([^&]+)");
    public List<SeenProPumpColumnRow> parse(String html){
        var document=Jsoup.parse(html==null?"":html);var cards=document.select(".boxContent .cotBom");
        log.info("[SEENPRO] event=parser.document parser=pump-column title={} cardCount={}",document.title(),cards.size());
        if(cards.isEmpty()){log.warn("[SEENPRO] event=parser.failed parser=pump-column failureStage=NO_PUMP_COLUMN_CARDS");return List.of();}
        var result=new ArrayList<SeenProPumpColumnRow>();
        for(Element card:cards){var values=values(card);String code=pumpCode(card);
            if(code==null||code.isBlank()){log.warn("[SEENPRO] event=parser.row.skipped parser=pump-column reason=PUMP_CODE_MISSING");continue;}
            result.add(new SeenProPumpColumnRow(code,values.get("ten cot"),values.get("nhien lieu"),values.get("dia chi mac"),
                values.get("hien thi"),values.get("so serial"),values.get("ket noi"),values.get("total")));}
        log.info("[SEENPRO] event=parser.completed parser=pump-column count={}",result.size());return List.copyOf(result);
    }
    private Map<String,String> values(Element card){var result=new HashMap<String,String>();
        for(Element row:card.select(".cotBomRight .row1")){Element label=row.selectFirst(".rowLeft"),value=row.selectFirst(".rowRight");
            if(label!=null&&value!=null)result.put(normalize(label.text()),value.text().trim());}return result;}
    private String pumpCode(Element card){Element link=card.selectFirst("a[href*=maCot]");if(link==null)return null;
        var matcher=PUMP_CODE.matcher(link.attr("href"));return matcher.find()?URLDecoder.decode(matcher.group(1),StandardCharsets.UTF_8):null;}
    private String normalize(String value){return Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}+","")
        .replace('đ','d').replace('Đ','D').toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim();}
}
