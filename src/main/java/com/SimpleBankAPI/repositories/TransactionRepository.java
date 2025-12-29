package com.SimpleBankAPI.repositories;

import com.SimpleBankAPI.enums.TransactionName;
import com.SimpleBankAPI.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    List<Transaction> findByAccountIdAndTypeAndDateBetween(Long accountId, TransactionName type, LocalDateTime from, LocalDateTime to);

    List<Transaction> findByAccountId(Long accountId);
}
