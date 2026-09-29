package vn.gasstation.company.api;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

class CompanyResponseSecurityTest {
    @Test void publicCompanyContractCannotExposeAnyPasswordField() {
        assertThat(Arrays.stream(CompanyResponse.class.getRecordComponents())
            .map(component -> component.getName().toLowerCase()).toList())
            .noneMatch(name -> name.contains("password") || name.contains("matkhau"));
    }
}
