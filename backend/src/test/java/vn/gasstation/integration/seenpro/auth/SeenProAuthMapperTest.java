package vn.gasstation.integration.seenpro.auth;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SeenProAuthMapperTest {
    @Test void mapsCredentialsToConfirmedLegacyFormContract() {
        var body = new SeenProAuthMapper().toFormBody("account name", "p&ss=word");
        assertThat(body).isEqualTo("taikhoan=account+name&matkhau=p%26ss%3Dword&btn-submit=");
    }
}

