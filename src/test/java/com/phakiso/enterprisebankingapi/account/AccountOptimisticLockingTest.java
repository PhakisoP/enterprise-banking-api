package com.phakiso.enterprisebankingapi.account;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
class AccountOptimisticLockingTest {

    @Autowired
    private EntityManager entityManager;

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