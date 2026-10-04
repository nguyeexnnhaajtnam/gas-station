package vn.gasstation.integration.seenpro.mapper;

import org.junit.jupiter.api.Test;
import vn.gasstation.pumpcode.application.PumpCodeHistorySearchRequest;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class SeenProPumpCodeQueryMapperTest {
    private final SeenProPumpCodeQueryMapper mapper = new SeenProPumpCodeQueryMapper();
    @Test void keepsLegacyNamesInsideAdapter() {
        var result=mapper.map(new PumpCodeHistorySearchRequest(LocalDate.of(2026,9,1),LocalDate.of(2026,9,29),"pump","fuel","customer",null,null,null,null,0,20));
        assertThat(result).containsEntry("t1","2026-09-01").containsEntry("t2","2026-09-29")
            .containsEntry("kh","customer").containsEntry("cb","pump").containsEntry("nl","fuel").containsEntry("start","0");
    }
    @Test void mapsPageToLegacyOffset() {
        var result=mapper.map(new PumpCodeHistorySearchRequest(null,null,null,null,null,null,null,null,null,1,20));
        assertThat(result).containsEntry("start","20").containsEntry("t1","").containsEntry("t2","")
            .containsEntry("dkt","").containsEntry("dkl","");
    }

    @Test void usesActualSeenProFilterParameterNames() {
        var result=mapper.map(new PumpCodeHistorySearchRequest(null,null,null,null,null,"30000","1,08","3","5",0,20));
        assertThat(result).containsEntry("ts","30000").containsEntry("ls","1,08")
            .containsEntry("tt","3").containsEntry("sx","5")
            .doesNotContainKeys("tien","lit","trangthai","sapxep");
    }
}

