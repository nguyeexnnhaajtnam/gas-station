package vn.gasstation.integration.seenpro.mapper;

import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProOnlinePayload;
import vn.gasstation.pump.domain.PumpRealtime;
import vn.gasstation.pump.domain.PumpRealtimeSnapshot;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Component
public class SeenProPumpRealtimeMapper {
    public PumpRealtimeSnapshot map(List<SeenProOnlinePayload> payloads) {
        OffsetDateTime observedAt = OffsetDateTime.now();
        var pumps = payloads.stream().map(payload -> map(payload, observedAt)).toList();
        return new PumpRealtimeSnapshot(pumps, true, observedAt);
    }

    private static PumpRealtime map(SeenProOnlinePayload payload, OffsetDateTime observedAt) {
        String state = normalized(payload.pumpStates());
        var operational = switch (state) {
            case "1", "active", "fueling", "dispensing", "dang bom", "images/pump-red.png" -> PumpRealtime.OperationalStatus.FUELING;
            case "0", "idle", "hung", "ready", "ranh", "images/pump-black.png" -> PumpRealtime.OperationalStatus.IDLE;
            case "maintenance", "error", "fault" -> PumpRealtime.OperationalStatus.MAINTENANCE;
            default -> PumpRealtime.OperationalStatus.UNKNOWN;
        };
        var nozzle = operational == PumpRealtime.OperationalStatus.FUELING
            ? PumpRealtime.NozzleStatus.DISPENSING
            : operational == PumpRealtime.OperationalStatus.IDLE
                ? PumpRealtime.NozzleStatus.HUNG : PumpRealtime.NozzleStatus.UNKNOWN;
        return new PumpRealtime(
            payload.descriptor().pumpCode(), payload.descriptor().pumpName(), "UNKNOWN",
            currency(payload.money()), decimal(payload.liters()), currency(payload.prices()), decimal(payload.totals()),
            connection(payload.connectionStates()), operational, nozzle, observedAt);
    }

    private static PumpRealtime.ConnectionStatus connection(String raw) {
        return switch (normalized(raw)) {
            case "1", "true", "online", "connected" -> PumpRealtime.ConnectionStatus.ONLINE;
            case "0", "false", "offline", "disconnected" -> PumpRealtime.ConnectionStatus.OFFLINE;
            default -> PumpRealtime.ConnectionStatus.UNKNOWN;
        };
    }

    // VND amounts use '.' as a thousands separator ("27.720" = 27720), unlike liters.
    private static BigDecimal currency(String raw) {
        if (raw == null) return null;
        String value = raw.trim().replaceAll("<[^>]+>", "").replace(" ", "").replace(" ", "");
        if (value.matches("-?\\d{1,3}(\\.\\d{3})+")) return new BigDecimal(value.replace(".", ""));
        return decimal(value);
    }

    private static BigDecimal decimal(String raw) {
        if (raw == null) return null;
        String value = raw.trim().replaceAll("<[^>]+>", "").replace("\u00a0", "").replace(" ", "");
        if (value.isEmpty()) return null;
        if (value.contains(",") && value.contains(".")) value = value.replace(".", "").replace(',', '.');
        else if (value.contains(",")) value = value.replace(',', '.');
        value = value.replaceAll("[^0-9+\\-.]", "");
        if (value.isEmpty() || value.equals("-") || value.equals(".")) return null;
        try { return new BigDecimal(value); }
        catch (NumberFormatException ignored) { return null; }
    }

    private static String normalized(String raw) {
        if (raw == null) return "";
        return java.text.Normalizer.normalize(raw.replaceAll("<[^>]+>", "").trim(), java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
