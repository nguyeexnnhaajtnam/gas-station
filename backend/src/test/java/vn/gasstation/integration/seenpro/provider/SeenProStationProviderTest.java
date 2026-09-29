package vn.gasstation.integration.seenpro.provider;

import org.junit.jupiter.api.Test;
import vn.gasstation.company.domain.Company;
import vn.gasstation.company.domain.CompanyStatus;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.mapper.SeenProStationMapper;
import vn.gasstation.integration.seenpro.model.SeenProStationModel;
import vn.gasstation.integration.seenpro.parser.SeenProStationParser;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
import vn.gasstation.integration.seenpro.station.SeenProStationReferenceRegistry;
import vn.gasstation.station.domain.Station;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SeenProStationProviderTest {
    @Test
    void loadsStationsWithCompanyReferenceAndMapsToDomain() {
        var client = mock(SeenProHttpClient.class);
        var sessions = mock(SeenProSessionManager.class);
        var parser = mock(SeenProStationParser.class);
        var mapper = mock(SeenProStationMapper.class);
        var references = new SeenProStationReferenceRegistry();
        var provider = new SeenProStationProvider(client, sessions, parser, mapper, references);
        var company = new Company("company-id", "Công ty mẫu", "company_ref", null, null, CompanyStatus.UNKNOWN);
        var legacy = new SeenProStationModel("station_ref", "Trạm mẫu", null, null, null, null);
        var station = new Station("station-id", "company-id", "station_ref", "Trạm mẫu", null, null);

        when(sessions.authenticatedAccount()).thenReturn(Optional.of("authenticated-root"));
        when(client.get("view.php", Map.of("gl", "3", "al", "company_ref", "opt", "v"))).thenReturn("sanitized-html");
        when(parser.parse("sanitized-html")).thenReturn(List.of(legacy));
        when(mapper.map(legacy, "company-id")).thenReturn(station);

        assertThat(provider.findByCompany(company)).containsExactly(station);
        assertThat(references.resolve("station-id")).get().extracting(SeenProStationReferenceRegistry.Reference::legacyAccount).isEqualTo("station_ref");
        verify(client).get("view.php", Map.of("gl", "3", "al", "company_ref", "opt", "v"));
        verifyNoMoreInteractions(client);
    }
}
