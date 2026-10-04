package vn.gasstation.integration.seenpro.parser;

import java.util.*;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProRevenueReport;

@Component public class SeenProRevenueReportHtmlParser {
 private static final Logger log=LoggerFactory.getLogger(SeenProRevenueReportHtmlParser.class);
 private static final Pattern LABELS=Pattern.compile("(?i)\\blabels\\s*:\\s*");
 private static final Pattern DATASETS=Pattern.compile("(?i)\\bdatasets\\s*:\\s*");
 private static final Pattern LABEL=Pattern.compile("(?i)\\blabel\\s*:\\s*(['\"])(.*?)\\1",Pattern.DOTALL);
 private static final Pattern DATA=Pattern.compile("(?i)\\bdata\\s*:\\s*");
 private static final Pattern ITEM=Pattern.compile("(['\"])(.*?)\\1|(-?[0-9]+(?:\\.[0-9]+)?)",Pattern.DOTALL);

 public SeenProRevenueReport parse(String html){
  var document=Jsoup.parse(html==null?"":html);var today=document.select(".today-left .info .content").eachText();var groups=document.select(".baocaochitiet .boxGas");
  log.info("[SEENPRO] event=parser.document parser=revenue-report title={} todayMetrics={} groupCount={}",document.title(),today.size(),groups.size());
  if(today.size()<6||groups.size()<3)throw new IllegalStateException("SeenPro revenue report structure is incomplete");
  var fuelRows=rows(groups.get(0),false);var pumpRows=rows(groups.get(1),false);var invoiceRows=rows(groups.get(2),true);
  var charts=new ArrayList<List<SeenProRevenueReport.SeenProChartSeries>>();for(Element script:document.select("script:not([src])"))charts.addAll(charts(script.data()));
  var fuelChart=bestChart(charts,fuelRows);var pumpChart=bestChart(charts,pumpRows);
  var result=new SeenProRevenueReport(List.copyOf(today),fuelRows,pumpRows,invoiceRows,fuelChart,pumpChart);
  log.info("[SEENPRO] event=parser.completed parser=revenue-report fuelRows={} pumpRows={} invoiceRows={} chartCount={}",result.fuels().size(),result.pumps().size(),result.invoices().size(),charts.size());return result;
 }
 private List<SeenProRevenueReport.SeenProRevenueRow> rows(Element group,boolean invoiceOrder){var result=new ArrayList<SeenProRevenueReport.SeenProRevenueRow>();for(Element row:group.select(".boxData > .rowx.color4")){var cells=row.children().stream().filter(e->e.hasClass("colx")).map(Element::text).toList();if(cells.size()<4||cells.get(0).equalsIgnoreCase("Tất cả"))continue;result.add(invoiceOrder?new SeenProRevenueReport.SeenProRevenueRow(cells.get(0),cells.get(2),cells.get(1),cells.get(3)):new SeenProRevenueReport.SeenProRevenueRow(cells.get(0),cells.get(1),cells.get(2),cells.get(3)));}return List.copyOf(result);}
 private List<SeenProRevenueReport.SeenProChartSeries> bestChart(List<List<SeenProRevenueReport.SeenProChartSeries>> charts,List<SeenProRevenueReport.SeenProRevenueRow> rows){var names=rows.stream().map(r->r.name().toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toSet());return charts.stream().max(Comparator.comparingLong(chart->chart.stream().filter(series->names.contains(series.name().toLowerCase(Locale.ROOT))).count())).filter(chart->chart.stream().anyMatch(series->names.contains(series.name().toLowerCase(Locale.ROOT)))).orElse(List.of());}
 private List<List<SeenProRevenueReport.SeenProChartSeries>> charts(String source){var result=new ArrayList<List<SeenProRevenueReport.SeenProChartSeries>>();var lm=LABELS.matcher(source);while(lm.find()){int ls=source.indexOf('[',lm.end()),le=matching(source,ls,'[',']');if(ls<0||le<0)continue;var labels=values(source.substring(ls+1,le));var dm=DATASETS.matcher(source);if(!dm.find(le))continue;int ds=source.indexOf('[',dm.end()),de=matching(source,ds,'[',']');if(ds<0||de<0)continue;var series=datasets(source.substring(ds+1,de),labels);if(!labels.isEmpty()&&!series.isEmpty())result.add(series);lm.region(Math.max(le+1,de+1),source.length());}return result;}
 private List<SeenProRevenueReport.SeenProChartSeries> datasets(String source,List<String> labels){var result=new ArrayList<SeenProRevenueReport.SeenProChartSeries>();int cursor=0;while((cursor=source.indexOf('{',cursor))>=0){int end=matching(source,cursor,'{','}');if(end<0)break;String object=source.substring(cursor+1,end);var label=LABEL.matcher(object);var data=DATA.matcher(object);if(label.find()&&data.find()){int start=object.indexOf('[',data.end()),finish=matching(object,start,'[',']');if(start>=0&&finish>=0)result.add(new SeenProRevenueReport.SeenProChartSeries(unescape(label.group(2)),labels,values(object.substring(start+1,finish))));}cursor=end+1;}return List.copyOf(result);}
 private List<String> values(String source){var result=new ArrayList<String>();var matcher=ITEM.matcher(source);while(matcher.find())result.add(unescape(matcher.group(2)!=null?matcher.group(2):matcher.group(3)));return List.copyOf(result);}
 private int matching(String source,int start,char open,char close){if(start<0)return-1;int depth=0;char quote=0;for(int i=start;i<source.length();i++){char c=source.charAt(i);if(quote!=0){if(c==quote&&source.charAt(i-1)!='\\')quote=0;continue;}if(c=='\''||c=='"'){quote=c;continue;}if(c==open)depth++;else if(c==close&&--depth==0)return i;}return-1;}
 private String unescape(String value){return value==null?"":value.replace("\\'","'").replace("\\\"","\"").trim();}
}
