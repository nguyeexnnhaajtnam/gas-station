package vn.gasstation.station.application;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.gasstation.company.application.CompanyService;
import vn.gasstation.station.domain.Station;
import java.util.List;
import java.util.Optional;

@Service
public class StationService {
    private final StationProvider provider;
    private final CompanyService companies;
    private final StationContextActivator contextActivator;

    public StationService(StationProvider provider, CompanyService companies, StationContextActivator contextActivator) {
        this.provider = provider;
        this.companies = companies;
        this.contextActivator = contextActivator;
    }

    @Cacheable(value = "stations-by-company", key = "#companyId")
    public Optional<List<Station>> findByCompanyId(String companyId) {
        return companies.findById(companyId).map(provider::findByCompany);
    }

    public Optional<Station> activate(String stationId) {
        return contextActivator.activate(stationId);
    }
}
