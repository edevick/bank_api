package com.SimpleBankAPI.controllers;

import com.SimpleBankAPI.enums.TransactionName;
import com.SimpleBankAPI.models.Account;
import com.SimpleBankAPI.models.Transaction;
import com.SimpleBankAPI.services.AccountService;
import com.SimpleBankAPI.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

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
  public List<Transaction> getTransactions(@PathVariable Long id){
      return transactionService.getTransactionsById(id);
  }

    //deposit money
  @PostMapping("/{id}/deposit")
  public ResponseEntity<Void> deposit( @PathVariable Long id,  @RequestParam BigDecimal amount){
      transactionService.deposit(id,amount);
      return ResponseEntity.ok().build();
  }

    //withdrawal money
  @PostMapping("/{id}/withdrawal")
  public ResponseEntity<Void> withdrawal( @PathVariable Long id,  @RequestParam BigDecimal amount){
      transactionService.withdrawal(id,amount);
      return ResponseEntity.ok().build();
  }

    //transfer money between accounts
  @PostMapping("/transfer")
  public ResponseEntity<Void> transfer(@RequestParam Long fromId ,@RequestParam Long toId, @RequestParam BigDecimal amount){
        transactionService.transfer(fromId,toId,amount);
        return ResponseEntity.ok().build();
  }


    //transactions history with filters by type and day
  @GetMapping("/{id}/history")
  public List<Transaction> history(@PathVariable Long id, @RequestParam(required = false) TransactionName type,
                                      @RequestParam(required = false) LocalDateTime startDate,
                                      @RequestParam(required = false) LocalDateTime finishDate ){
      return transactionService.getTransactionsByIdAndDateBetween(id,type,startDate,finishDate);
    }

    //recalculate balance
  @PostMapping("/{id}/recalculation")
  public ResponseEntity<Void> recalculate(@PathVariable Long id){
      transactionService.recalculate(id);
      return ResponseEntity.ok().build();
  }

}
