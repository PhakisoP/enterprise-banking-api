package com.phakiso.enterprisebankingapi.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Column(name = "pin")
    private String pin;

    @Column(name = "failed_attempts")
    private Integer failedAttempts;

    @Column(name = "is_locked")
    private Boolean locked;
}