package com.phakiso.enterprisebankingapi.account;

import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse getAccountByAccountNumber(Integer accountNumber) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        return new AccountResponse(
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getAccountType(),
                account.getBalance()
        );
    }
}
