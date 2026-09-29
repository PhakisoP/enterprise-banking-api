package com.phakiso.enterprisebankingapi.account;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.phakiso.enterprisebankingapi.transaction.TransactionService;

@Service
public class AccountService {

    private static final Logger log =
            LoggerFactory.getLogger(AccountService.class);

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
        log.debug("Retrieving account details");

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
        log.info("Starting deposit operation");

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

        log.info("Deposit operation completed");
    }

    @Transactional
    public void withdraw(Integer accountNumber, BigDecimal amount) {
        log.info("Starting withdrawal operation");

        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        account.withdraw(amount);

        accountRepository.save(account);

        transactionService.createTransaction(
                accountNumber,
                "Withdrawal",
                amount,
                account.getBalance()
        );

        log.info("Withdrawal operation completed");
    }

    @Transactional
    public void transfer(
            Integer sourceAccountNumber,
            Integer destinationAccountNumber,
            BigDecimal amount
    ) {
        log.info("Starting account transfer operation");

        if (sourceAccountNumber.equals(destinationAccountNumber)) {
            throw new InvalidTransferException(
                    "Choose a different destination account."
            );
        }

        Account source = accountRepository.findById(sourceAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(sourceAccountNumber));

        Account destination = accountRepository.findById(destinationAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(destinationAccountNumber));

        source.withdraw(amount);
        destination.deposit(amount);

        accountRepository.save(source);
        accountRepository.save(destination);

        transactionService.createTransaction(
                sourceAccountNumber,
                "Transfer Out",
                amount,
                source.getBalance()
        );

        transactionService.createTransaction(
                destinationAccountNumber,
                "Transfer In",
                amount,
                destination.getBalance()
        );

        log.info("Account transfer operation completed");
    }
}