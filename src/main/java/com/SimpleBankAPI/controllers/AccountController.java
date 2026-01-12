package com.SimpleBankAPI.controllers;

import com.SimpleBankAPI.dtos.DepositRequest;
import com.SimpleBankAPI.dtos.TransferRequest;
import com.SimpleBankAPI.dtos.WithdrawalRequest;
import com.SimpleBankAPI.models.Account;
import com.SimpleBankAPI.models.Transaction;
import com.SimpleBankAPI.services.AccountService;
import com.SimpleBankAPI.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    //create account
  @PostMapping("/new")
  public ResponseEntity<Void> createAccount(@RequestBody Account account){
      accountService.createAccount(account);
      return ResponseEntity.ok().build();
  }
    //check account
  @GetMapping("/{id}/transactions")
  public List<Transaction> getTransactions(@PathVariable UUID id){
      return transactionService.getTransactionsById(id);
  }

    //deposit money
  @PostMapping("/{id}/deposit")
  public ResponseEntity<Void> deposit(@PathVariable UUID id,@RequestBody DepositRequest depositRequest){
      transactionService.deposit(id,depositRequest);
      return ResponseEntity.ok().build();
  }

    //withdrawal money
  @PostMapping("/{id}/withdrawal")
  public ResponseEntity<Void> withdrawal( @PathVariable("id") UUID accountId,  @RequestBody WithdrawalRequest withdrawalRequest){
      transactionService.withdrawal(accountId,withdrawalRequest);
      return ResponseEntity.ok().build();
  }

    //transfer money between accounts
  @PostMapping("/transfer")
  public ResponseEntity<Void> transfer(@RequestBody TransferRequest transferRequest){
        transactionService.transfer(transferRequest);
        return ResponseEntity.ok().build();
  }


    //transactions history with filters by type and day
  @GetMapping("/{id}/history")
  public List<Transaction> history(@PathVariable UUID id,
                                      @RequestParam(required = false) LocalDateTime startDate,
                                      @RequestParam(required = false) LocalDateTime finishDate ){
      return transactionService.getTransactionsByIdAndDateBetween(id,startDate,finishDate);
    }

    //recalculate balance
  @PostMapping("/{id}/recalculation")
  public ResponseEntity<Void> recalculate(@PathVariable UUID id){
      transactionService.recalculate(id);
      return ResponseEntity.ok().build();
  }

}
