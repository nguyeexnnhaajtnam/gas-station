package vn.gasstation.integration.seenpro.mapper;

import org.junit.jupiter.api.Test;
import vn.gasstation.transaction.application.TransactionQuery;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class SeenProTransactionQueryMapperTest {
    private final SeenProTransactionQueryMapper mapper = new SeenProTransactionQueryMapper();
    @Test void keepsLegacyNamesInsideAdapter() {
        var result=mapper.map(new TransactionQuery(LocalDate.of(2026,9,1), LocalDate.of(2026,9,29), "customer", "pump", "fuel", 0, 20));
        assertThat(result).containsEntry("t1","01/09/2026").containsEntry("t2","29/09/2026")
            .containsEntry("kh","customer").containsEntry("cb","pump").containsEntry("nl","fuel").containsEntry("start","0");
    }
    @Test void refusesToGuessPagination() {
        assertThatThrownBy(() -> mapper.map(new TransactionQuery(null,null,null,null,null,1,20)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

