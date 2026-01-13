package com.SimpleBankAPI.dtos;

import java.math.BigDecimal;

public class DepositRequest {
    private String transactionRef;
    private BigDecimal amount;

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
