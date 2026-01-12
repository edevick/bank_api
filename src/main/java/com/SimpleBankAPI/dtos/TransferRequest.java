package com.SimpleBankAPI.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferRequest {
    public UUID fromId;
    public UUID toId;
    public String transactionRef;
    public BigDecimal amount;
}
