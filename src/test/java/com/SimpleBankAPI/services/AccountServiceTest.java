package com.SimpleBankAPI.services;

import com.SimpleBankAPI.models.Account;
import com.SimpleBankAPI.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private AccountService accountService;
    @Test
    void createAccount_shouldSetCreatedAt(){
        Account account = new Account();
        account.setOwnerAccount("Alice");
        account.setBalance(BigDecimal.valueOf(1000));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        Account result = accountService.createAccount(account);
        assertNotNull(result.getCreatedAt());
    }
}
