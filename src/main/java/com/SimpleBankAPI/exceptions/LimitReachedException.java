package com.SimpleBankAPI.exceptions;

public class LimitReachedException extends RuntimeException{
    public LimitReachedException(String message) {
        super(message);
    }
}
