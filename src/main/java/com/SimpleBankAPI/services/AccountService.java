package com.SimpleBankAPI.services;

import com.SimpleBankAPI.models.Account;
import com.SimpleBankAPI.repositories.AccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Account account){
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

}
