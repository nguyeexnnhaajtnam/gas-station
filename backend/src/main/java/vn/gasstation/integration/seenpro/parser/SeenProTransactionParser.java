package vn.gasstation.integration.seenpro.parser;

import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.model.SeenProTransactionModel;
import java.util.List;

@Component
public class SeenProTransactionParser {
    public List<SeenProTransactionModel> parse(String html) {
        // TODO: implement selectors after a sanitized theodoibanhang.php response fixture is supplied.
        throw new LegacySystemUnavailableException("Cấu trúc trang giao dịch kế thừa chưa được xác nhận");
    }
}

