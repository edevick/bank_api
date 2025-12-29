package com.SimpleBankAPI.services;

import com.SimpleBankAPI.enums.TransactionName;
import com.SimpleBankAPI.exceptions.AccountNotFoundException;
import com.SimpleBankAPI.exceptions.InvalidAmountException;
import com.SimpleBankAPI.exceptions.LimitReachedException;
import com.SimpleBankAPI.exceptions.NotEnoughMoneyException;
import com.SimpleBankAPI.models.Account;
import com.SimpleBankAPI.models.Transaction;
import com.SimpleBankAPI.repositories.AccountRepository;
import com.SimpleBankAPI.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public Transaction deposit(Long id, BigDecimal amount){
        if (amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
        Transaction transaction = new Transaction();
        if (accountRepository.existsById(id)) {
            Account account  = accountRepository.findById(id).get();
            transaction.setDate(LocalDateTime.now());
            transaction.setType(TransactionName.DEPOSIT);
            transaction.setAmount(amount);
            transaction.setAccount(account);
            account.setBalance(account.getBalance().add(amount));
            transactionRepository.save(transaction);
            return transaction;
        } else
            throw new AccountNotFoundException("Account does not exist");
    }

    public Transaction withdrawal (Long id, BigDecimal amount){
        if (amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
        Transaction transaction = new Transaction();
        if (accountRepository.existsById(id)) {
            Account account  = accountRepository.findById(id).get();
            if ((amount.compareTo(account.getBalance())>0)) {
                throw new NotEnoughMoneyException("Not enough balance");
            }
            transaction.setDate(LocalDateTime.now());
            transaction.setType(TransactionName.WITHDRAWAL);
            transaction.setAmount(amount);
            account.setBalance(account.getBalance().subtract(amount));
            transaction.setAccount(account);
            transactionRepository.save(transaction);
            return transaction;
        } else
            throw new AccountNotFoundException("Account does not exist");
    }
    @Transactional
    public void transfer (Long fromId,Long toId, BigDecimal amount){
        if (amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
        if (!accountRepository.existsById(fromId)) {
            throw new AccountNotFoundException("Account with id " + fromId +" does not exist");
        } else {
            Account account1  = accountRepository.findById(fromId).get();
            if (amount.compareTo(BigDecimal.valueOf(5000)) > 0){
                throw new LimitReachedException("Transfer can be up to 5000");
            }
            List<Transaction> transactions = getTransactionsByIdAndDateBetween(fromId,TransactionName.WITHDRAWAL, LocalDate.now().atStartOfDay(),LocalDate.now().plusDays(1).atStartOfDay());
            BigDecimal sum = transactions.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
            if (sum.add(amount).compareTo(BigDecimal.valueOf(5000)) > 0){
                throw new LimitReachedException("Day limit reached 5000, transaction can not continue");
            }
            Transaction transaction1 = new Transaction();
            transaction1.setAmount(amount);
            transaction1.setDate(LocalDateTime.now());
            transaction1.setType(TransactionName.WITHDRAWAL);
            account1.setBalance(account1.getBalance().subtract(amount));
            transaction1.setAccount(account1);
            transactionRepository.save(transaction1);
        }
        if (!accountRepository.existsById(toId)) {
            throw new AccountNotFoundException("Account with id " + toId +" does not exist");
        }   else {
            Account account2  = accountRepository.findById(toId).get();
            account2.setBalance(account2.getBalance().add(amount));
            Transaction transaction2 = new Transaction();
            transaction2.setAmount(amount);
            transaction2.setDate(LocalDateTime.now());
            transaction2.setType(TransactionName.DEPOSIT);
            transaction2.setAccount(account2);
            transactionRepository.save(transaction2);
        }
    }

    public List<Transaction> getTransactionsById(Long id){
        return transactionRepository.findByAccountId(id);
    }

    public List<Transaction> getTransactionsByIdAndDateBetween(Long id, TransactionName type, LocalDateTime from, LocalDateTime to){
       return transactionRepository.findByAccountIdAndTypeAndDateBetween(id,type,from,to);
    }

    public void recalculate (Long id){
        List<Transaction> transactions = transactionRepository.findByAccountId(id);
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionName.DEPOSIT) {
                total = total.add(t.getAmount());
            } else {
                total = total.subtract(t.getAmount());
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
