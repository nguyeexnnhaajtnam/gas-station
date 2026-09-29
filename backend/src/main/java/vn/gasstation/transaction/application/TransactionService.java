package vn.gasstation.transaction.application;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.gasstation.shared.domain.PageResult;
import vn.gasstation.transaction.domain.Transaction;

@Service
public class TransactionService {
    private final TransactionProvider provider;
    public TransactionService(TransactionProvider provider) { this.provider = provider; }
    @Cacheable(value = "transactions", key = "#query") public PageResult<Transaction> find(TransactionQuery query) { return provider.find(query); }
}

