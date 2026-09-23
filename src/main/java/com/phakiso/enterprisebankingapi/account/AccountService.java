package com.phakiso.enterprisebankingapi.account;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.phakiso.enterprisebankingapi.transaction.TransactionService;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    public AccountService(
            AccountRepository accountRepository,
            TransactionService transactionService
    ) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
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

    @Transactional
    public void deposit(Integer accountNumber, BigDecimal amount) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        account.deposit(amount);

        accountRepository.save(account);

        transactionService.createTransaction(
                accountNumber,
                "Deposit",
                amount,
                account.getBalance()
        );
    }
}