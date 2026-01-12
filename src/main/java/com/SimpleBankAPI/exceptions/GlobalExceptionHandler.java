package com.SimpleBankAPI.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
   public ResponseEntity<String> handleAccountNotFound (AccountNotFoundException e){
        return ResponseEntity.status(404).body(e.getMessage());
}
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<String> handleInvalidAmount (InvalidAmountException e){
        return ResponseEntity.status(400).body(e.getMessage());
    }
    @ExceptionHandler(LimitReachedException.class)
    public ResponseEntity<String> handleLimitReached (LimitReachedException e){
        return ResponseEntity.status(400).body(e.getMessage());
    }
    @ExceptionHandler(NotEnoughMoneyException.class)
    public ResponseEntity<String> handleNotEnoughMoney(NotEnoughMoneyException e){
        return ResponseEntity.status(400).body(e.getMessage());
    }
    @ExceptionHandler(TransactionRefDuplicationException.class)
    public ResponseEntity<String> handleTransactionRefDuplication(TransactionRefDuplicationException e){
        return ResponseEntity.status(400).body(e.getMessage());
    }

}
