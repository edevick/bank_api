package com.SimpleBankAPI.models;

import com.SimpleBankAPI.enums.TransactionName;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private TransactionName type;
    private BigDecimal amount;
    private LocalDateTime date;
    @ManyToOne
    private Account account;
    public Transaction(){

    }

    public TransactionName getType() {
        return type;
    }

    public void setType(TransactionName type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
