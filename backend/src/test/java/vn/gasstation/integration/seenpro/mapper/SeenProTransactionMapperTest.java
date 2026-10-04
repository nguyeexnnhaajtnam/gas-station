package vn.gasstation.integration.seenpro.mapper;

import org.junit.jupiter.api.Test;
import vn.gasstation.integration.seenpro.model.SeenProTransactionModel;
import vn.gasstation.integration.seenpro.model.SeenProPumpCodeHistoryRow;
import vn.gasstation.pumpcode.domain.InvoiceStatus;
import static org.assertj.core.api.Assertions.*;

class SeenProPumpCodeHistoryMapperTest {
    @Test void normalizesLegacyValuesIntoDomainSemantics() {
        var source=new SeenProTransactionModel(" tx-1 "," B01 ","1"," RON 95-III ","23.500","12,50","293.750",
            "2026-09-29T08:15:00+07:00"," Khách lẻ ","đã xuất"," 001 ");
        var result=new SeenProPumpCodeHistoryMapper().map(source);
        assertThat(result.pumpCode()).isEqualTo("B01");
        assertThat(result.unitPrice()).isEqualByComparingTo("23500");
        assertThat(result.volume()).isEqualByComparingTo("12.50");
        assertThat(result.amount()).isEqualByComparingTo("293750");
        assertThat(result.invoiceStatus()).isEqualTo(InvoiceStatus.ISSUED);
    }

    @Test void parsesSeenProTimeBeforeDateFormat() {
        var source = new SeenProPumpCodeHistoryRow("CB01-1235-00002", "Cột 01", "RON95", "27.720",
            "1,08", "30.000", "21:31:59 03/10/2026", "Khách lẻ", "-", null);

        var result = new SeenProPumpCodeHistoryMapper().map(source);

        assertThat(result.finishedAt()).hasToString("2026-10-03T21:31:59+07:00");
    }
}

