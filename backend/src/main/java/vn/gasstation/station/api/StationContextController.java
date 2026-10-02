package vn.gasstation.station.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.station.application.StationService;

@RestController
@RequestMapping("/api/v1/stations")
public class StationContextController {
    private static final Logger log = LoggerFactory.getLogger(StationContextController.class);
    private final StationService service;

    public StationContextController(StationService service) {
        this.service = service;
    }

    public record StationSelectionResponse(StationResponse station) {}

    @PostMapping("/{stationId}/select")
    public ResponseEntity<StationSelectionResponse> select(@PathVariable String stationId) {
        log.debug("[BUSINESS] event=station.select.requested stationId={}", stationId);
        var response = service.activate(stationId)
            .map(StationResponse::from)
            .map(StationSelectionResponse::new)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
        log.debug("[BUSINESS] event=station.select.responded stationId={} status={}", stationId, response.getStatusCode().value());
        return response;
    }
}
