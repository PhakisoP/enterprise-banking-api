package com.phakiso.enterprisebankingapi.account;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@ActiveProfiles("test")
class AccountOptimisticLockingTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedAccount() {
        jdbcTemplate.update("DELETE FROM transactions");
        jdbcTemplate.update("DELETE FROM accounts");
        jdbcTemplate.update("""
                INSERT INTO accounts
                    (account_number, customer_id, account_type, balance, version,
                     pin, failed_attempts, is_locked)
                VALUES (888888, 1, 'Cheque', 31000.00, 0, 'test-pin', 0, FALSE)
                """);
    }

    @Test
    @Transactional
    void shouldRejectStaleAccountUpdate() {

        Account staleAccount =
                entityManager.find(Account.class, 888888);

        entityManager.detach(staleAccount);

        Account currentAccount =
                entityManager.find(Account.class, 888888);

        currentAccount.deposit(new BigDecimal("1.00"));

        entityManager.flush();

        staleAccount.deposit(new BigDecimal("1.00"));

        assertThrows(
                OptimisticLockException.class,
                () -> entityManager.merge(staleAccount)
        );
    }
}
