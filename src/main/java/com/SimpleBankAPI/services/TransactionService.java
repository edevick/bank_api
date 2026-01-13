package com.SimpleBankAPI.services;

import com.SimpleBankAPI.dtos.DepositRequest;
import com.SimpleBankAPI.dtos.TransferRequest;
import com.SimpleBankAPI.dtos.WithdrawalRequest;
import com.SimpleBankAPI.exceptions.*;
import com.SimpleBankAPI.models.Account;
import com.SimpleBankAPI.models.Transaction;
import com.SimpleBankAPI.repositories.AccountRepository;
import com.SimpleBankAPI.repositories.TransactionRepository;
import org.hibernate.PessimisticLockException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }
    @Transactional
    public Transaction deposit(UUID accountId, DepositRequest depositRequest){
        BigDecimal amount = depositRequest.getAmount();
        String transactionRef = depositRequest.getTransactionRef();
        if (amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
        List<Transaction>transactionList = transactionRepository.findByTransactionRef(transactionRef);
        if (!transactionList.isEmpty()){
            for (Transaction tr:transactionList) {
                if (!tr.getAccount().getId().equals(accountId)){
                    throw new TransactionRefDuplicationException("TransactionRef should be unique");
                }
                if (tr.getCredit() != null) return tr;
            }
        }

        Transaction transaction = new Transaction();
        if (accountRepository.existsById(accountId)) {
            Account account  = accountRepository.findById(accountId).get();
            transaction.setDate(LocalDateTime.now());
            transaction.setCredit(amount);
            transaction.setTransactionRef(transactionRef);
            transaction.setDebit(null);
            transaction.setAccount(account);
            account.setBalance(account.getBalance().add(amount));
            accountRepository.save(account);
            transactionRepository.save(transaction);
            return transaction;
        } else
            throw new AccountNotFoundException("Account does not exist");
    }
    @Transactional
    public Transaction withdrawal (UUID id, WithdrawalRequest withdrawalRequest){
        BigDecimal amount = withdrawalRequest.getAmount();
        String transactionRef = withdrawalRequest.getTransactionRef();
        if (amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
        List<Transaction> transactionList = transactionRepository.findByTransactionRef(transactionRef);
        if(!transactionList.isEmpty()){
            for (Transaction tr : transactionList) {
                if (!tr.getAccount().getId().equals(id)){
                    throw new TransactionRefDuplicationException("TransactionRef should be unique");
                }
                if (tr.getDebit() != null) return tr;
            }
        }
        Transaction transaction = new Transaction();
        if (accountRepository.existsById(id)) {
            Account account  = accountRepository.findById(id).get();
            if ((amount.compareTo(account.getBalance())>0)) {
                throw new NotEnoughMoneyException("Not enough balance");
            }
            transaction.setDate(LocalDateTime.now());
            transaction.setDebit(amount);
            transaction.setCredit(null);
            transaction.setTransactionRef(transactionRef);
            account.setBalance(account.getBalance().subtract(amount));
            transaction.setAccount(account);
            accountRepository.save(account);
            transactionRepository.save(transaction);
            return transaction;
        } else
            throw new AccountNotFoundException("Account does not exist");
    }
    public void transfer (TransferRequest transferRequest) {
        int maxRetries = 3;
        int attempt = 0;
        while (attempt < maxRetries){
            try{
                  executeTransfer(transferRequest);
            return;
            }catch (CannotAcquireLockException | PessimisticLockException e){
                attempt++;
                if (attempt >= maxRetries) {
                    throw new TransferFailedException("Transfer failed after retries");
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new TransferFailedException("Interrupted during retry", ie);
                }
            }
        }
    }
    @Transactional(isolation = Isolation.SERIALIZABLE)
    private void executeTransfer(TransferRequest transferRequest){
        BigDecimal amount = transferRequest.getAmount();
        UUID fromId = transferRequest.getFromId();
        UUID toId = transferRequest.getToId();
        String transactionRef = transferRequest.getTransactionRef();
        if (amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
        if (!accountRepository.existsById(fromId) || !accountRepository.existsById(toId)) {
            throw new AccountNotFoundException("Account does not exist");
        } else {
            Account accountFrom = new Account();
            Account accountTo = new Account();
            if(fromId.toString().compareTo(toId.toString())<0) {
                Optional<Account> accountFromOpt = accountRepository.findByIdForUpdate(fromId);
                if (accountFromOpt.isPresent()) accountFrom = accountFromOpt.get();
                Optional<Account> accountToOpt = accountRepository.findByIdForUpdate(toId);
                if (accountToOpt.isPresent())  accountTo = accountToOpt.get();

            } else{
                Optional<Account> accountToOpt = accountRepository.findByIdForUpdate(toId);
                if (accountToOpt.isPresent())  accountTo = accountToOpt.get();
                Optional<Account> accountFromOpt = accountRepository.findByIdForUpdate(fromId);
                if (accountFromOpt.isPresent()) accountFrom = accountFromOpt.get();
            }
            if (amount.compareTo(BigDecimal.valueOf(5000)) > 0){
                throw new LimitReachedException("Transfer can be up to 5000");
            }
            List<Transaction> transactionList = transactionRepository.findByTransactionRef(transactionRef);
            if(!transactionList.isEmpty()){
                for (Transaction tr : transactionList) {
                    if (!tr.getAccount().getId().equals(toId) && !tr.getAccount().getId().equals(fromId)) {
                        throw new TransactionRefDuplicationException("TransactionRef should be unique");
                    }
                }
                return;
            }
            List<Transaction> transactions = getTransactionsByIdAndDateBetween(fromId, LocalDate.now().atStartOfDay(),LocalDate.now().plusDays(1).atStartOfDay());
            if (accountFrom.getBalance().compareTo(amount) < 0) {
                throw new NotEnoughMoneyException("Not enough money on balance");
            }
            BigDecimal sum = transactions.stream().map(Transaction::getDebit).filter(Objects::nonNull).reduce(BigDecimal.ZERO,BigDecimal::add);
            if (sum.add(amount).compareTo(BigDecimal.valueOf(5000)) > 0){
                throw new LimitReachedException("Day limit reached 5000, transaction can not continue");
            }
            Transaction transaction1 = new Transaction();
            transaction1.setDate(LocalDateTime.now());
            accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
            transaction1.setAccount(accountFrom);
            transaction1.setTransactionRef(transactionRef);
            transaction1.setCredit(null);
            transaction1.setDebit(amount);
            transactionRepository.save(transaction1);

            Transaction transaction2 = new Transaction();
            transaction2.setDate(LocalDateTime.now());
            accountTo.setBalance(accountTo.getBalance().add(amount));
            transaction2.setAccount(accountTo);
            transaction2.setTransactionRef(transactionRef);
            transaction2.setDebit(null);
            transaction2.setCredit(amount);
            transactionRepository.save(transaction2);
        }
    }


    public List<Transaction> getTransactionsById(UUID id){
        return transactionRepository.findByAccountId(id);
    }

    public List<Transaction> getTransactionsByIdAndDateBetween(UUID id, LocalDateTime from, LocalDateTime to){
       return transactionRepository.findByAccountIdAndDateBetween(id,from,to);
    }

    public void recalculate (UUID id){
        List<Transaction> transactions = transactionRepository.findByAccountId(id);
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getCredit()!=null) {
                total = total.add(t.getCredit());
            } else {
                total = total.subtract(t.getDebit());
            }
        }

        if (accountRepository.findById(id).isPresent()){
            Account account = accountRepository.findById(id).get();
            if (!(total.compareTo(account.getBalance())==0)){
                account.setBalance(total);
                accountRepository.save(account);
            }

        }

    }
}
