package com.SimpleBankAPI.services;

import com.SimpleBankAPI.dtos.DepositRequest;
import com.SimpleBankAPI.dtos.TransferRequest;
import com.SimpleBankAPI.dtos.WithdrawalRequest;
import com.SimpleBankAPI.exceptions.NotEnoughMoneyException;
import com.SimpleBankAPI.models.Account;
import com.SimpleBankAPI.models.Transaction;
import com.SimpleBankAPI.repositories.AccountRepository;
import com.SimpleBankAPI.repositories.TransactionRepository;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionServiceTest {
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TransactionService transactionService;


    @Test
    void depositCalledTwice_shouldBeIdempotent(){
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(1000));
        account.setOwnerAccount("Kateryna");
        account.setCreatedAt(LocalDateTime.now());
        account.setNumberAccount(1234L);
        Account savedAccount = accountRepository.save(account);

        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setTransactionRef("TEST-001");
        depositRequest.setAmount(BigDecimal.valueOf(100));
        transactionService.deposit( savedAccount.getId(),depositRequest);
        transactionService.deposit( savedAccount.getId(),depositRequest);
        List<Transaction>transactionList = transactionRepository.findByTransactionRef("TEST-001");

        assertEquals(1, transactionList.size());




    }

    @Test
    void withdrawalCalledTwice_shouldBeIdempotent(){
        Account account = new Account();
        account.setNumberAccount(234L);
        account.setBalance(BigDecimal.valueOf(2000));
        account.setOwnerAccount("Sergey");
        account.setCreatedAt(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);

        WithdrawalRequest withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setAmount(BigDecimal.valueOf(200));
        withdrawalRequest.setTransactionRef("WITH-001");
        transactionService.withdrawal(savedAccount.getId(),withdrawalRequest);
        transactionService.withdrawal(savedAccount.getId(),withdrawalRequest);

        List<Transaction> transactionList = transactionRepository.findByTransactionRef("WITH-001");
        assertEquals(1,transactionList.size());

    }

    @Test
    void withdrawal_NotEnoughMoney_ShouldThrowException(){
        Account account = new Account();
        account.setNumberAccount(1234L);
        account.setBalance(BigDecimal.ZERO);
        account.setOwnerAccount("Piter");
        account.setCreatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(account);
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setAmount(BigDecimal.valueOf(100));
        withdrawalRequest.setTransactionRef("WITH-003");
        assertThrows(NotEnoughMoneyException.class, () -> {
            transactionService.withdrawal(saved.getId(), withdrawalRequest);
        });
    }
    @Test
    void transfer_With_InsufficientFunds_shouldThrowException(){
        Account sender = new Account();
        sender.setCreatedAt(LocalDateTime.now());
        sender.setBalance(BigDecimal.valueOf(200));
        sender.setNumberAccount(5678L);
        sender.setOwnerAccount("Mehmet");
        Account receiver = new Account();
        receiver.setCreatedAt(LocalDateTime.now());
        receiver.setBalance(BigDecimal.ZERO);
        receiver.setNumberAccount(5679L);
        receiver.setOwnerAccount("Ahmet");
        Account savedSender = accountRepository.save(sender);
        Account savedReceiver = accountRepository.save(receiver);
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setAmount(BigDecimal.valueOf(500));
        transferRequest.setTransactionRef("TRANSFER-0020");
        transferRequest.setToId(savedReceiver.getId());
        transferRequest.setFromId(savedSender.getId());
        assertThrows(NotEnoughMoneyException.class,()->{
            transactionService.transfer(transferRequest);
        });
    }

    @Test
    void concurrentTransfers_shouldMaintainBalanceConsistency() throws InterruptedException{
        Account sender = new Account();
        sender.setCreatedAt(LocalDateTime.now());
        sender.setBalance(BigDecimal.valueOf(1000));
        sender.setOwnerAccount("Leo");
        sender.setNumberAccount(1234L);
        Account savedSender = accountRepository.save(sender);

        Account receiver = new Account();
        receiver.setCreatedAt(LocalDateTime.now());
        receiver.setBalance(BigDecimal.valueOf(0));
        receiver.setOwnerAccount("Teo");
        receiver.setNumberAccount(789L);
        Account savedReceiver = accountRepository.save(receiver);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            final int threadIndex = i; // Make effectively final for lambda capture
            executorService.submit(() -> {
                String transactionRef = "TRANSFER-" + threadIndex; // Create inside lambda
                TransferRequest transferRequest  = new TransferRequest();
                transferRequest.setAmount(BigDecimal.valueOf(100));
                transferRequest.setToId(savedReceiver.getId());
                transferRequest.setFromId(savedSender.getId());
                transferRequest.setTransactionRef(transactionRef);
                transactionService.transfer(transferRequest);
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // Use findById instead of getReferenceById to eagerly load the entity
        Account finalSender = accountRepository.findById(savedSender.getId()).orElseThrow();
        Account finalReceiver = accountRepository.findById(savedReceiver.getId()).orElseThrow();
        assertEquals(BigDecimal.ZERO,finalSender.getBalance());
        assertEquals(BigDecimal.valueOf(1000),finalReceiver.getBalance());

    }
}
