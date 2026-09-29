package vn.gasstation.transaction.application;

import vn.gasstation.shared.domain.PageResult;
import vn.gasstation.transaction.domain.Transaction;
import java.util.Optional;

public interface TransactionProvider {
    PageResult<Transaction> find(TransactionQuery query);
    Optional<Transaction> findById(String id);
}

