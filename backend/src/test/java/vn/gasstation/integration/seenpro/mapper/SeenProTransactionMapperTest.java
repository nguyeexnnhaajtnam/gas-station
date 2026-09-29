package vn.gasstation.integration.seenpro.mapper;

import org.junit.jupiter.api.Test;
import vn.gasstation.integration.seenpro.model.SeenProTransactionModel;
import vn.gasstation.transaction.domain.InvoiceState;
import static org.assertj.core.api.Assertions.*;

class SeenProTransactionMapperTest {
    @Test void normalizesLegacyValuesIntoDomainSemantics() {
        var source=new SeenProTransactionModel(" tx-1 "," B01 ","1"," RON 95-III ","23.500","12,50","293.750",
            "2026-09-29T08:15:00+07:00"," Khách lẻ ","đã xuất"," 001 ");
        var result=new SeenProTransactionMapper().map(source);
        assertThat(result.id()).isEqualTo("tx-1");
        assertThat(result.unitPrice()).isEqualByComparingTo("23500");
        assertThat(result.liters()).isEqualByComparingTo("12.50");
        assertThat(result.amount()).isEqualByComparingTo("293750");
        assertThat(result.invoiceState()).isEqualTo(InvoiceState.ISSUED);
    }
}

