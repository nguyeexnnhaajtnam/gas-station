package vn.gasstation.integration.seenpro.mapper;

import org.junit.jupiter.api.Test;
import vn.gasstation.integration.seenpro.model.SeenProStationModel;

import static org.assertj.core.api.Assertions.assertThat;

class SeenProStationMapperTest {
    private final SeenProStationMapper mapper = new SeenProStationMapper();

    @Test
    void mapsOnlyCleanDomainFieldsAndUsesStableOpaqueId() {
        var source = new SeenProStationModel(" station-code ", " Trạm  Miền Xanh ", null,
            " station@example.test ", "view.php?gl=2&al=station-code&opt=v", "daily-quanly.php?gl=2");

        var first = mapper.map(source, "company-id");
        var second = mapper.map(source, "company-id");

        assertThat(first.id()).isEqualTo(second.id()).isNotEqualTo("station-code");
        assertThat(first.companyId()).isEqualTo("company-id");
        assertThat(first.code()).isEqualTo("station-code");
        assertThat(first.name()).isEqualTo("Trạm Miền Xanh");
        assertThat(first.email()).isEqualTo("station@example.test");
        assertThat(first.toString()).doesNotContain("view.php", "daily-quanly.php", "gl=", "al=");
    }
}
