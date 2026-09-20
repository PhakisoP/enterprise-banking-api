package com.phakiso.enterprisebankingapi.account;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Integer accountNumber) {
        super("Account not found: " + accountNumber);
    }
}