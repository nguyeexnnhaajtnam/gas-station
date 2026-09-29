package vn.gasstation.company.application;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.gasstation.company.domain.Company;
import java.util.List;
import java.util.Optional;
@Service
public class CompanyService {
    private final CompanyProvider provider;
    public CompanyService(CompanyProvider provider) { this.provider = provider; }
    @Cacheable("companies") public List<Company> findAll() { return provider.findAll(); }
    public Optional<Company> findById(String id) {
        return findAll().stream().filter(company -> company.id().equals(id)).findFirst();
    }
}
