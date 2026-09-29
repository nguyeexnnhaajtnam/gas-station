package vn.gasstation.station.application;

import vn.gasstation.company.domain.Company;
import vn.gasstation.station.domain.Station;
import java.util.List;

public interface StationProvider {
    List<Station> findByCompany(Company company);
}
