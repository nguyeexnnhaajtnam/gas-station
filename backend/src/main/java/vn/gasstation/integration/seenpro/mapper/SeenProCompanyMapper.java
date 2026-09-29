package vn.gasstation.integration.seenpro.mapper;
import org.springframework.stereotype.Component;
import vn.gasstation.company.domain.Company;
import vn.gasstation.company.domain.CompanyStatus;
import vn.gasstation.integration.seenpro.model.SeenProCompanyModel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
@Component
public class SeenProCompanyMapper {
    public Company map(SeenProCompanyModel source) {
        String account = clean(source.legacyAccount());
        String id = account == null ? null : UUID.nameUUIDFromBytes(("seenpro-company:" + account).getBytes(StandardCharsets.UTF_8)).toString();
        return new Company(id, clean(source.name()), account, clean(source.phone()), clean(source.email()), CompanyStatus.UNKNOWN);
    }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim().replaceAll("\\s+", " "); }
}
