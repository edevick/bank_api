package com.SimpleBankAPI.services;

import com.SimpleBankAPI.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    TransactionRepository transactionRepository;
    @InjectMocks
    TransactionService transactionService;

    @Test
    void deposit_shouldAddDepositTransaction(){

    }
}
