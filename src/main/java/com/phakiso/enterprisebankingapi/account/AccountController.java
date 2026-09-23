package com.phakiso.enterprisebankingapi.account;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse getAccount(@PathVariable Integer accountNumber) {
        return accountService.getAccountByAccountNumber(accountNumber);
    }

    /**
     * Deposits money into an account and records the resulting transaction.
     *
     * @param accountNumber the account receiving the deposit
     * @param request the validated deposit request
     */
    @PostMapping("/{accountNumber}/deposits")
    public void deposit(
            @PathVariable Integer accountNumber,
            @Valid @RequestBody DepositRequest request
    ) {
        accountService.deposit(accountNumber, request.amount());
    }
}