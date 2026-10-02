package vn.gasstation.integration.seenpro.client;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import vn.gasstation.integration.seenpro.SeenProProperties;
import vn.gasstation.integration.seenpro.session.InMemorySeenProSessionManager;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SeenProHttpClientFormTest {
    @Test
    void postsUtf8FormBodyWithoutMovingParametersIntoQueryString() throws Exception {
        var method = new AtomicReference<String>();
        var contentType = new AtomicReference<String>();
        var rawQuery = new AtomicReference<String>();
        var body = new AtomicReference<String>();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/gettienhome.php", exchange -> {
            method.set(exchange.getRequestMethod());
            contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            rawQuery.set(exchange.getRequestURI().getRawQuery());
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "500.000".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            URI baseUrl = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/");
            var properties = new SeenProProperties(baseUrl, Duration.ofSeconds(1),
                Duration.ofSeconds(2), Duration.ofSeconds(5));
            var sessions = new InMemorySeenProSessionManager(properties);
            var client = new SeenProHttpClient(properties, sessions, new SeenProNavigationDiagnostics());
            Map<String, String> form = Map.of(
                "maCot", "CB 01",
                "standardizedMAC", "AA:BB",
                "master", "1",
                "slave", "2",
                "maNhienLieu", "E10-III",
                "user", "nguoi dung");

            assertThat(client.postForm("gettienhome.php", form)).isEqualTo("500.000");
            assertThat(method.get()).isEqualTo("POST");
            assertThat(contentType.get()).isEqualTo("application/x-www-form-urlencoded");
            assertThat(rawQuery.get()).isNull();
            assertThat(decodeForm(body.get())).containsExactlyInAnyOrderEntriesOf(form);
        } finally {
            server.stop(0);
        }
    }

    private static Map<String, String> decodeForm(String body) {
        return Arrays.stream(body.split("&"))
            .map(pair -> pair.split("=", 2))
            .collect(Collectors.toMap(
                pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)));
    }
}
