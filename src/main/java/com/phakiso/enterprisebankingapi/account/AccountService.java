package com.phakiso.enterprisebankingapi.account;

import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccountByAccountNumber(Integer accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElseThrow();
    }
}
