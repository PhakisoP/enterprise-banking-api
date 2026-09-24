package com.phakiso.enterprisebankingapi.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "account_number")
    private Integer accountNumber;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "balance")
    private BigDecimal balance;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "pin")
    private String pin;

    @Column(name = "failed_attempts")
    private Integer failedAttempts;

    @Column(name = "is_locked")
    private Boolean locked;

    public Integer getAccountNumber() {
        return accountNumber;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public String getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(this.accountNumber);
        }

        this.balance = this.balance.subtract(amount);
    }
}
