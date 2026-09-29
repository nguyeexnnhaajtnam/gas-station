package vn.gasstation.station.api;

import org.junit.jupiter.api.Test;
import vn.gasstation.station.domain.Station;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class StationResponseSecurityTest {
    @Test
    void responseContractContainsNoLegacyOrPasswordFields() {
        var fields = Arrays.stream(StationResponse.class.getRecordComponents()).map(component -> component.getName()).toList();
        var response = StationResponse.from(new Station("id", "company-id", "code", "name", null, null));

        assertThat(fields).containsExactly("id", "companyId", "code", "name", "phone", "email");
        assertThat(response.toString()).doesNotContain("password", "matKhau", "view.php", "PHPSESSID");
    }
}
