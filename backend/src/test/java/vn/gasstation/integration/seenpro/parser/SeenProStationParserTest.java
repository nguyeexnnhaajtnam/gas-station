package vn.gasstation.integration.seenpro.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SeenProStationParserTest {
    private final SeenProStationParser parser = new SeenProStationParser();

    @Test
    void parsesStationsAndIgnoresHeaderMalformedRowsAndPasswords() throws IOException {
        var stations = parser.parse(fixture());

        assertThat(stations).hasSize(2);
        assertThat(stations.get(0).account()).isEqualTo("tram_hoa_binh");
        assertThat(stations.get(0).name()).isEqualTo("CỬA HÀNG XĂNG DẦU HÒA BÌNH");
        assertThat(stations.get(0).phone()).isEqualTo("0900 000 001");
        assertThat(stations.get(0).email()).isEqualTo("tram1@example.test");
        assertThat(stations.get(0).viewUrl()).contains("view.php?gl=2");
        assertThat(stations.get(0).manageUrl()).contains("daily-quanly.php?gl=2");
        assertThat(stations.get(1).phone()).isNull();
        assertThat(stations.get(1).email()).isNull();
        assertThat(stations.get(1).manageUrl()).isNull();
        assertThat(stations.toString()).doesNotContain("synthetic-secret", "another-synthetic-secret");
    }

    private String fixture() throws IOException {
        try (var input = getClass().getResourceAsStream("/seenpro/station-list-success.html")) {
            if (input == null) throw new IOException("Missing sanitized station fixture");
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
