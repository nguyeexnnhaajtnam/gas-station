package vn.gasstation.integration.seenpro.parser;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

class SeenProCompanyParserTest {
    private final SeenProCompanyParser parser = new SeenProCompanyParser();

    @Test void parsesConfirmedDivRowsAndExcludesHeader() throws IOException {
        var companies = parser.parse(fixture());
        assertThat(companies).hasSize(2);
        assertThat(companies.get(0).name()).isEqualTo("CÔNG TY TNHH DEMO MIỀN NAM");
        assertThat(companies.get(0).legacyAccount()).isEqualTo("demo_south");
        assertThat(companies.get(0).phone()).isEqualTo("0900000001");
        assertThat(companies.get(0).email()).isEqualTo("south@example.invalid");
        assertThat(companies.get(0).navigationUrl()).isEqualTo("view.php?gl=3&al=demo_south&opt=v");
    }

    @Test void normalizesMissingValuesAndExtractsOnclickNavigation() throws IOException {
        var company = parser.parse(fixture()).get(1);
        assertThat(company.phone()).isNull();
        assertThat(company.email()).isNull();
        assertThat(company.navigationUrl()).isEqualTo("view.php?gl=3&al=demo_north&opt=v");
    }

    @Test void parsedModelHasNoPasswordFieldAndSecretNeverAppears() throws IOException {
        assertThat(Arrays.stream(vn.gasstation.integration.seenpro.model.SeenProCompanyModel.class.getRecordComponents())
            .map(component -> component.getName().toLowerCase()).toList()).noneMatch(name -> name.contains("password") || name.contains("matkhau"));
        assertThat(parser.parse(fixture()).toString()).doesNotContain("SYNTHETIC_SECRET_ONE", "SYNTHETIC_SECRET_TWO");
    }

    private String fixture() throws IOException {
        try (var input = getClass().getResourceAsStream("/seenpro/company-list-success.html")) {
            if (input == null) throw new IOException("fixture missing");
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
