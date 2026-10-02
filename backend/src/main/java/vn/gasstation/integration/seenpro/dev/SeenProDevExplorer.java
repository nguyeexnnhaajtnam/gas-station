package vn.gasstation.integration.seenpro.dev;

import org.jsoup.Jsoup;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Profile("dev")
public class SeenProDevExplorer {
    private static final Logger log = LoggerFactory.getLogger(SeenProDevExplorer.class);
    private static final List<String> AJAX_ENDPOINTS = List.of(
        "gettienhome", "getlithome", "getgiahome", "gettotal", "getconnectstate", "getpumpstate");
    private final SeenProSessionManager sessions;
    private final SeenProHttpClient client;

    public SeenProDevExplorer(SeenProSessionManager sessions, SeenProHttpClient client) {
        this.sessions = sessions;
        this.client = client;
    }

    public Exploration exploreOnlineJavaScript() {
        requireActiveStationSession();
        String html = client.get("online.php", Map.of());
        var document = Jsoup.parse(html);
        var externalSources = new LinkedHashMap<String, String>();
        var skippedExternal = new ArrayList<String>();
        document.select("script[src]").stream().map(script -> script.attr("src").strip())
            .filter(source -> !source.isBlank()).distinct().forEach(source -> {
                if (!client.isSeenProOrigin(source)) {
                    skippedExternal.add(source);
                    log.debug("[SEENPRO] event=script.skipped provider=seenpro reason=external-origin");
                    return;
                }
                externalSources.put(source, client.getResource(source));
            });

        List<JavaScriptAsset> assets = externalSources.entrySet().stream()
            .map(entry -> new JavaScriptAsset(entry.getKey(), entry.getValue().length(), sha256(entry.getValue())))
            .toList();
        var inlineSources = new ArrayList<String>();
        var inlineBlocks = new ArrayList<InlineScriptBlock>();
        document.select("script:not([src])").forEach(script -> {
            String javascript = script.data();
            int index = inlineSources.size();
            inlineSources.add(javascript);
            inlineBlocks.add(new InlineScriptBlock(index, javascript.length(), sha256(javascript)));
        });
        return new Exploration(document.title(), assets, List.copyOf(inlineBlocks), List.copyOf(skippedExternal),
            locateEndpoints(inlineSources, externalSources));
    }

    private void requireActiveStationSession() {
        var activeStation = sessions.activeStationId();
        log.debug("[SEENPRO] event=dev-explorer.session provider=seenpro authenticated={} stationContext={} cookieCount={}",
            sessions.authenticatedAccount().isPresent(), activeStation.isPresent(),
            sessions.cookieSnapshot().fingerprints().size());
        if (activeStation.isEmpty()) {
            throw new SeenProSessionNotEstablishedException();
        }
    }

    private static String sha256(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(content.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private static List<AjaxEndpointLocation> locateEndpoints(List<String> inlineSources,
                                                               Map<String, String> externalSources) {
        var result = new ArrayList<AjaxEndpointLocation>();
        for (String endpoint : AJAX_ENDPOINTS) {
            var locations = new ArrayList<String>();
            for (int index = 0; index < inlineSources.size(); index++) {
                if (containsIgnoreCase(inlineSources.get(index), endpoint)) locations.add("inline:" + index);
            }
            externalSources.forEach((source, javascript) -> {
                if (containsIgnoreCase(javascript, endpoint)) locations.add("external:" + source);
            });
            result.add(new AjaxEndpointLocation(endpoint, List.copyOf(locations)));
        }
        return List.copyOf(result);
    }

    private static boolean containsIgnoreCase(String source, String value) {
        return source.toLowerCase(java.util.Locale.ROOT).contains(value.toLowerCase(java.util.Locale.ROOT));
    }

    public record Exploration(String entryPageTitle, List<JavaScriptAsset> seenProScripts,
                              List<InlineScriptBlock> inlineScripts, List<String> skippedExternalScripts,
                              List<AjaxEndpointLocation> ajaxEndpoints) {}
    public record JavaScriptAsset(String source, int characterCount, String sha256) {}
    public record InlineScriptBlock(int index, int characterCount, String sha256) {}
    public record AjaxEndpointLocation(String endpoint, List<String> locations) {}
}
