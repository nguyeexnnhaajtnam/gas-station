package vn.gasstation.integration.seenpro.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.mapper.SeenProTransactionMapper;
import vn.gasstation.integration.seenpro.mapper.SeenProTransactionQueryMapper;
import vn.gasstation.integration.seenpro.parser.SeenProTransactionParser;
import vn.gasstation.shared.domain.PageResult;
import vn.gasstation.transaction.application.TransactionProvider;
import vn.gasstation.transaction.application.TransactionQuery;
import vn.gasstation.transaction.domain.Transaction;
import java.util.Optional;

@Component
@ConditionalOnProperty(name="app.data-source", havingValue="seenpro", matchIfMissing=true)
public class SeenProTransactionProvider implements TransactionProvider {
    private final SeenProHttpClient client; private final SeenProTransactionQueryMapper queryMapper;
    private final SeenProTransactionParser parser; private final SeenProTransactionMapper mapper;
    public SeenProTransactionProvider(SeenProHttpClient client, SeenProTransactionQueryMapper queryMapper, SeenProTransactionParser parser, SeenProTransactionMapper mapper) {
        this.client=client; this.queryMapper=queryMapper; this.parser=parser; this.mapper=mapper;
    }
    public PageResult<Transaction> find(TransactionQuery query) {
        var models=parser.parse(client.get("theodoibanhang.php", queryMapper.map(query)));
        var items=models.stream().map(mapper::map).toList();
        return new PageResult<>(items, query.page(), query.size(), items.size(), false); // TODO derive totals/next page from confirmed HTML.
    }
    public Optional<Transaction> findById(String id) { return Optional.empty(); }
}

