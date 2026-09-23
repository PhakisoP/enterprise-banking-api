package com.phakiso.enterprisebankingapi.account;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Integer accountNumber) {
        super("Insufficient funds for account: " + accountNumber);
    }
}
