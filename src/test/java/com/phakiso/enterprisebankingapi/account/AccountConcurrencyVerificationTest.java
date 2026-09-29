package com.phakiso.enterprisebankingapi.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class AccountConcurrencyVerificationTest {

    private static final int ACCOUNT_NUMBER = 777777;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void seedAccount() {
        jdbcTemplate.update("DELETE FROM transactions");
        jdbcTemplate.update("DELETE FROM accounts");

        jdbcTemplate.update("""
                INSERT INTO accounts
                    (account_number, customer_id, account_type, balance, version,
                     pin, failed_attempts, is_locked)
                VALUES (?, 1, 'Cheque', 31000.00, 0, 'test-pin', 0, FALSE)
                """, ACCOUNT_NUMBER);
    }

    @Test
    void concurrentUpdates_shouldAllowOneTransactionAndRejectTheOther()
            throws Exception {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        CountDownLatch accountsLoaded = new CountDownLatch(2);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<UpdateResult> first =
                    executor.submit(() ->
                            executeConcurrentUpdate(
                                    transactionTemplate,
                                    accountsLoaded
                            )
                    );

            Future<UpdateResult> second =
                    executor.submit(() ->
                            executeConcurrentUpdate(
                                    transactionTemplate,
                                    accountsLoaded
                            )
                    );

            UpdateResult firstResult =
                    first.get(10, TimeUnit.SECONDS);

            UpdateResult secondResult =
                    second.get(10, TimeUnit.SECONDS);

            List<UpdateResult> results =
                    List.of(firstResult, secondResult);

            long successfulUpdates = results.stream()
                    .filter(UpdateResult::successful)
                    .count();

            long optimisticLockFailures = results.stream()
                    .filter(result ->
                            result.failure()
                                    instanceof OptimisticLockingFailureException)
                    .count();

            assertEquals(
                    1,
                    successfulUpdates,
                    "Exactly one concurrent transaction should succeed"
            );

            assertEquals(
                    1,
                    optimisticLockFailures,
                    "Exactly one concurrent transaction should fail with optimistic locking"
            );

            BigDecimal finalBalance = jdbcTemplate.queryForObject(
                    """
                    SELECT balance
                    FROM accounts
                    WHERE account_number = ?
                    """,
                    BigDecimal.class,
                    ACCOUNT_NUMBER
            );

            assertEquals(
                    new BigDecimal("31001.00"),
                    finalBalance,
                    "Only one concurrent deposit should be committed"
            );

        } finally {
            executor.shutdownNow();
        }
    }

    private UpdateResult executeConcurrentUpdate(
            TransactionTemplate transactionTemplate,
            CountDownLatch accountsLoaded
    ) {
        try {
            transactionTemplate.executeWithoutResult(status -> {

                Account account = accountRepository
                        .findById(ACCOUNT_NUMBER)
                        .orElseThrow();

                accountsLoaded.countDown();

                awaitOtherTransaction(accountsLoaded);

                account.deposit(new BigDecimal("1.00"));

                accountRepository.saveAndFlush(account);
            });

            return UpdateResult.success();

        } catch (RuntimeException exception) {
            return UpdateResult.failure(exception);
        }
    }

    private void awaitOtherTransaction(CountDownLatch accountsLoaded) {
        try {
            if (!accountsLoaded.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Timed out waiting for concurrent transaction"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Concurrency test was interrupted",
                    exception
            );
        }
    }

    private record UpdateResult(
            boolean successful,
            RuntimeException failure
    ) {

        static UpdateResult success() {
            return new UpdateResult(true, null);
        }

        static UpdateResult failure(RuntimeException exception) {
            return new UpdateResult(false, exception);
        }
    }
}