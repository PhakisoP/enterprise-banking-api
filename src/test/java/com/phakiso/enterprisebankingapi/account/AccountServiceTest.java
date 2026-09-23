package com.phakiso.enterprisebankingapi.account;

import com.phakiso.enterprisebankingapi.transaction.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private AccountRepository accountRepository;
    private TransactionService transactionService;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionService = mock(TransactionService.class);

        accountService = new AccountService(
                accountRepository,
                transactionService
        );
    }

    @Test
    void withdraw_shouldReduceBalanceAndCreateTransaction() throws Exception {
        Account account = createAccount();

        accountService.withdraw(
                888888,
                new BigDecimal("500.00")
        );

        assertEquals(
                new BigDecimal("30500.00"),
                account.getBalance()
        );

        verify(accountRepository).save(account);

        verify(transactionService).createTransaction(
                888888,
                "Withdrawal",
                new BigDecimal("500.00"),
                new BigDecimal("30500.00")
        );
    }

    @Test
    void withdraw_shouldRejectWhenFundsAreInsufficient() throws Exception {
        Account account = createAccount();

        assertThrows(
                InsufficientFundsException.class,
                () -> accountService.withdraw(
                        888888,
                        new BigDecimal("50000.00")
                )
        );

        assertEquals(
                new BigDecimal("31000.00"),
                account.getBalance()
        );

        verify(accountRepository, never()).save(any(Account.class));

        verify(transactionService, never()).createTransaction(
                anyInt(),
                anyString(),
                any(BigDecimal.class),
                any(BigDecimal.class)
        );
    }

    @Test
    void withdraw_shouldAllowWithdrawalWhenAmountEqualsBalance() throws Exception {
        Account account = createAccount();

        accountService.withdraw(
                888888,
                new BigDecimal("31000.00")
        );

        assertEquals(
                new BigDecimal("0.00"),
                account.getBalance()
        );

        verify(accountRepository).save(account);

        verify(transactionService).createTransaction(
                888888,
                "Withdrawal",
                new BigDecimal("31000.00"),
                new BigDecimal("0.00")
        );
    }

    @Test
    void deposit_shouldIncreaseBalanceAndCreateTransaction() throws Exception {
        Account account = createAccount();

        accountService.deposit(
                888888,
                new BigDecimal("1000.00")
        );

        assertEquals(
                new BigDecimal("32000.00"),
                account.getBalance()
        );

        verify(accountRepository).save(account);

        verify(transactionService).createTransaction(
                888888,
                "Deposit",
                new BigDecimal("1000.00"),
                new BigDecimal("32000.00")
        );
    }


    private Account createAccount() throws Exception {
        Account account = new Account();

        setField(account, "accountNumber", 888888);
        setField(account, "balance", new BigDecimal("31000.00"));

        when(accountRepository.findById(888888))
                .thenReturn(java.util.Optional.of(account));

        return account;
    }

    private static void setField(
            Object target,
            String fieldName,
            Object value
    ) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}