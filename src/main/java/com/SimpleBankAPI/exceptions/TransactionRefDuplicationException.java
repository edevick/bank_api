package com.SimpleBankAPI.exceptions;

public class TransactionRefDuplicationException extends RuntimeException{
    public TransactionRefDuplicationException(String message){
        super(message);
    }
}
