package vn.gasstation.station.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.gasstation.station.application.StationService;

@RestController
@RequestMapping("/api/v1/stations")
public class StationContextController {
    private final StationService service;

    public StationContextController(StationService service) {
        this.service = service;
    }

    public record StationSelectionResponse(StationResponse station) {}

    @PostMapping("/{stationId}/select")
    public ResponseEntity<StationSelectionResponse> select(@PathVariable String stationId) {
        return service.activate(stationId)
            .map(StationResponse::from)
            .map(StationSelectionResponse::new)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
