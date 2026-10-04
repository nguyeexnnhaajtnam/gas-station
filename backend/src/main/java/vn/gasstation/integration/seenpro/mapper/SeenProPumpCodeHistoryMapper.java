package vn.gasstation.integration.seenpro.mapper;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProPumpCodeHistoryRow;
import vn.gasstation.integration.seenpro.model.SeenProTransactionModel;
import vn.gasstation.pumpcode.domain.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
@Component
public class SeenProPumpCodeHistoryMapper {
    public PumpCodeHistory map(SeenProTransactionModel source){
        return map(new SeenProPumpCodeHistoryRow(source.pumpCode(),source.dispenser(),source.fuelLabel(),
            source.unitPriceText(),source.litersText(),source.amountText(),source.completedAtText(),
            source.customerText(),source.invoiceStateText(),source.invoiceNumberText()));
    }
    public PumpCodeHistory map(SeenProPumpCodeHistoryRow source){
        return new PumpCodeHistory(normalize(source.pumpCode()),normalize(source.dispenser()),normalize(source.fuelLabel()),
            decimal(source.unitPriceText()),decimal(source.litersText()),decimal(source.amountText()),dateTime(source.completedAtText()),
            normalize(source.customerText()),invoiceStatus(source.invoiceStateText()),normalize(source.invoiceNumberText()));
    }
    private String normalize(String value){return value==null||value.isBlank()?null:value.trim().replaceAll("\\s+"," ");}
    private BigDecimal decimal(String value){
        String clean=normalize(value);if(clean==null)return null;clean=clean.replaceAll("[^0-9,.-]","");
        int comma=clean.lastIndexOf(','),dot=clean.lastIndexOf('.');
        if(comma>dot)clean=clean.replace(".","").replace(',','.');
        else if(dot>=0&&comma<0&&clean.length()-dot-1==3)clean=clean.replace(".","");
        else clean=clean.replace(",","");
        return new BigDecimal(clean);
    }
    private OffsetDateTime dateTime(String value){
        String clean=normalize(value);if(clean==null)return null;
        try{return OffsetDateTime.parse(clean,DateTimeFormatter.ISO_OFFSET_DATE_TIME);}
        catch(java.time.format.DateTimeParseException ignored){
            for(String pattern:List.of("HH:mm:ss dd/MM/yyyy","HH:mm dd/MM/yyyy","dd/MM/yyyy HH:mm:ss","dd/MM/yyyy HH:mm")){
                try{return LocalDateTime.parse(clean,DateTimeFormatter.ofPattern(pattern)).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toOffsetDateTime();}
                catch(java.time.format.DateTimeParseException ignoredPattern){}
            }
            throw new java.time.format.DateTimeParseException("Unsupported SeenPro completion time",clean,0);
        }
    }
    private InvoiceStatus invoiceStatus(String value){
        String clean=normalize(value);if(clean==null)return InvoiceStatus.UNKNOWN;
        String lower=clean.toLowerCase(Locale.forLanguageTag("vi"));
        if(lower.contains("đã xuất")||lower.contains("đã phát hành"))return InvoiceStatus.ISSUED;
        if(lower.contains("chưa xuất")||lower.contains("chưa phát hành"))return InvoiceStatus.NOT_ISSUED;
        return InvoiceStatus.UNKNOWN;
    }
}
