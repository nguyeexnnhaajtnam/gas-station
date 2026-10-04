package vn.gasstation.integration.seenpro.parser;
import org.jsoup.Jsoup;import org.jsoup.nodes.Element;import org.slf4j.*;import org.springframework.stereotype.Component;import vn.gasstation.store.domain.StoreDetails;
import java.text.Normalizer;import java.util.*;
@Component public class SeenProStoreDetailsHtmlParser {private static final Logger log=LoggerFactory.getLogger(SeenProStoreDetailsHtmlParser.class);
 public StoreDetails parse(String html){var document=Jsoup.parse(html==null?"":html);Element root=document.selectFirst("#thongTinCuaHang .noiDung");
  log.info("[SEENPRO] event=parser.document parser=store-info title={} rootFound={}",document.title(),root!=null);
  if(root==null)throw new IllegalArgumentException("SeenPro store information root was not found");var values=new HashMap<String,String>();
  for(Element row:root.select(".rowx")){Element label=row.selectFirst(".label"),value=row.selectFirst(".value");if(label!=null&&value!=null)values.put(normalize(label.text()),clean(value.text()));}
  log.info("[SEENPRO] event=parser.completed parser=store-info fieldCount={}",values.size());
  return new StoreDetails(values.get("ten cong ty"),values.get("dia chi cong ty"),values.get("ma so thue"),values.get("dien thoai cong ty"),
   values.get("fax cong ty"),values.get("email cong ty"),values.get("ten cua hang"),values.get("dia chi cua hang"),values.get("dien thoai cua hang"),values.get("fax cua hang"),values.get("email cua hang"));}
 private String clean(String value){return value==null||value.isBlank()||value.equalsIgnoreCase("Chưa cập nhật")?null:value.trim().replaceAll("\\s+"," ");}
 private String normalize(String value){return Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}+","").replace('đ','d').replace('Đ','D').toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim();}}
