package vn.gasstation.pumpcode.api;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import vn.gasstation.pumpcode.application.*;
import vn.gasstation.pumpcode.domain.PumpCodeHistory;
import java.time.LocalDate;
import java.time.ZoneId;
@RestController
@RequestMapping("/api/v1/pump-codes/history")
public class PumpCodeHistoryController {
    private final PumpCodeHistoryService service;
    public PumpCodeHistoryController(PumpCodeHistoryService service){this.service=service;}
    @GetMapping public Page<PumpCodeHistory> find(
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required=false) String pumpId,@RequestParam(required=false) String fuelType,
        @RequestParam(required=false) String customer,@RequestParam(required=false) String amountFilter,
        @RequestParam(required=false) String volumeFilter,@RequestParam(required=false) String status,
        @RequestParam(required=false) String sort,@RequestParam(defaultValue="0") @Min(0) int page,
        @RequestParam(defaultValue="20") @Min(1) @Max(200) int size){
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        return service.search(new PumpCodeHistorySearchRequest(from==null?today:from,to==null?today:to,pumpId,fuelType,customer,amountFilter,volumeFilter,status,sort,page,size));
    }
}
