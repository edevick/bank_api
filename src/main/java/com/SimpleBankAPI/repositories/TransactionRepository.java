package com.SimpleBankAPI.repositories;

import com.SimpleBankAPI.enums.TransactionName;
import com.SimpleBankAPI.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByAccountIdAndDateBetween(UUID accountId, LocalDateTime from, LocalDateTime to);

    List<Transaction> findByAccountId(UUID accountId);
    List<Transaction> findByTransactionRef(String transactionRef);

}
