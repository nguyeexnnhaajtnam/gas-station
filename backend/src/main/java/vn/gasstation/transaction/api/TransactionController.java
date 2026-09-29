package vn.gasstation.transaction.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import vn.gasstation.shared.domain.PageResult;
import vn.gasstation.transaction.application.TransactionQuery;
import vn.gasstation.transaction.application.TransactionService;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService service;
    public TransactionController(TransactionService service) { this.service = service; }
    @GetMapping
    public PageResult<TransactionResponse> find(
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required=false) String customerId, @RequestParam(required=false) String pumpId,
        @RequestParam(required=false) String fuelId, @RequestParam(defaultValue="0") @Min(0) int page,
        @RequestParam(defaultValue="20") @Min(1) @Max(200) int size) {
        var result = service.find(new TransactionQuery(from, to, customerId, pumpId, fuelId, page, size));
        return new PageResult<>(result.items().stream().map(TransactionResponse::from).toList(), result.page(), result.size(), result.totalItems(), result.hasNext());
    }
}

