package vn.gasstation.station.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.gasstation.station.application.StationService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/stations")
public class StationController {
    private final StationService service;
    public StationController(StationService service) { this.service = service; }

    public record StationListResponse(List<StationResponse> items) {}

    @GetMapping
    public ResponseEntity<StationListResponse> byCompany(@PathVariable String companyId) {
        return service.findByCompanyId(companyId)
            .map(stations -> new StationListResponse(stations.stream().map(StationResponse::from).toList()))
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
