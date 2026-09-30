package com.phakiso.enterprisebankingapi.account;

import com.phakiso.enterprisebankingapi.transaction.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountApiIntegrationTest {

    private static final int SOURCE_ACCOUNT = 1001;
    private static final int DESTINATION_ACCOUNT = 2002;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private TransactionService transactionService;

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.update("DELETE FROM transactions");
        jdbcTemplate.update("DELETE FROM accounts");
        insertAccount(SOURCE_ACCOUNT, "100.00");
        insertAccount(DESTINATION_ACCOUNT, "50.00");
    }

    @Test
    void accountRetrievalUsesThePersistedAccount() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/{accountNumber}", SOURCE_ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(SOURCE_ACCOUNT))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.accountType").value("Cheque"))
                .andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    void transactionHistoryReturnsPersistedRowsNewestFirst() throws Exception {
        insertTransaction(SOURCE_ACCOUNT, "Deposit", "10.00", "110.00", "2026-09-01 09:00:00");
        insertTransaction(SOURCE_ACCOUNT, "Withdrawal", "5.00", "105.00", "2026-09-02 10:00:00");

        mockMvc.perform(get("/api/v1/accounts/{accountNumber}/transactions", SOURCE_ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].transactionType").value("Withdrawal"))
                .andExpect(jsonPath("$[0].balanceAfter").value(105.0))
                .andExpect(jsonPath("$[1].transactionType").value("Deposit"));
    }

    @Test
    void depositUpdatesBalanceAndPersistsLedgerEntry() throws Exception {
        performPost("/api/v1/accounts/{accountNumber}/deposits", SOURCE_ACCOUNT,
                "{\"amount\":25.50}").andExpect(status().isOk());

        assertBalance(SOURCE_ACCOUNT, "125.50");
        assertEquals(1L, transactionCount());
        assertEquals("Deposit", transactionType(0));
        assertEquals(0, new BigDecimal("125.50").compareTo(transactionBalanceAfter(0)));
    }

    @Test
    void withdrawalUpdatesBalanceAndPersistsLedgerEntry() throws Exception {
        performPost("/api/v1/accounts/{accountNumber}/withdrawals", SOURCE_ACCOUNT,
                "{\"amount\":25.00}").andExpect(status().isOk());

        assertBalance(SOURCE_ACCOUNT, "75.00");
        assertEquals(1L, transactionCount());
        assertEquals("Withdrawal", transactionType(0));
        assertEquals(0, new BigDecimal("75.00").compareTo(transactionBalanceAfter(0)));
    }

    @Test
    void transferUpdatesBothBalancesAndPersistsBothLedgerEntries() throws Exception {
        performTransfer("{\"destinationAccountNumber\":2002,\"amount\":30.00}")
                .andExpect(status().isOk());

        assertBalance(SOURCE_ACCOUNT, "70.00");
        assertBalance(DESTINATION_ACCOUNT, "80.00");
        assertEquals(2L, transactionCount());
        assertEquals("Transfer Out", transactionType(0));
        assertEquals("Transfer In", transactionType(1));
    }

    @Test
    void transferToMissingAccountReturnsNotFoundWithoutChangingSource() throws Exception {
        performTransfer("{\"destinationAccountNumber\":9999,\"amount\":30.00}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Account Not Found"));

        assertBalance(SOURCE_ACCOUNT, "100.00");
        assertEquals(0L, transactionCount());
    }

    @Test
    void transferWithInsufficientFundsReturns422WithoutChangingEitherAccount() throws Exception {
        performTransfer("{\"destinationAccountNumber\":2002,\"amount\":101.00}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Insufficient Funds"));

        assertBalance(SOURCE_ACCOUNT, "100.00");
        assertBalance(DESTINATION_ACCOUNT, "50.00");
        assertEquals(0L, transactionCount());
    }

    @Test
    void transferRollsBackBalancesAndFirstLedgerEntryWhenSecondEntryFails() throws Exception {
        doAnswer(invocation -> {
            if ("Transfer In".equals(invocation.getArgument(1))) {
                throw new IllegalStateException("Injected ledger failure");
            }
            return invocation.callRealMethod();
        }).when(transactionService).createTransaction(
                anyInt(), anyString(), any(BigDecimal.class), any(BigDecimal.class)
        );

        assertThrows(jakarta.servlet.ServletException.class,
                () -> performTransfer("{\"destinationAccountNumber\":2002,\"amount\":30.00}"));

        assertBalance(SOURCE_ACCOUNT, "100.00");
        assertBalance(DESTINATION_ACCOUNT, "50.00");
        assertEquals(0L, transactionCount());
    }

    @Test
    void validationFailuresAreRejectedBeforeDatabaseChanges() throws Exception {
        performPost("/api/v1/accounts/{accountNumber}/deposits", SOURCE_ACCOUNT,
                "{\"amount\":0}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));

        performTransfer("{\"destinationAccountNumber\":0,\"amount\":10.00}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("destinationAccountNumber"));

        performTransfer("{\"destinationAccountNumber\":2002,\"amount\":-1.00}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("amount"));

        performPost("/api/v1/accounts/{accountNumber}/deposits", SOURCE_ACCOUNT,
                "{\"amount\":1.001}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("amount"));

        assertBalance(SOURCE_ACCOUNT, "100.00");
        assertEquals(0L, transactionCount());
    }

    @Test
    void accountNotFoundIsReturnedForReadsAndMutations() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Account Not Found"));

        performPost("/api/v1/accounts/{accountNumber}/deposits", 9999,
                "{\"amount\":10.00}").andExpect(status().isNotFound());

        assertEquals(0L, transactionCount());
    }

    @Test
    void withdrawalWithInsufficientFundsDoesNotPersistChanges() throws Exception {
        performPost("/api/v1/accounts/{accountNumber}/withdrawals", SOURCE_ACCOUNT,
                "{\"amount\":101.00}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Insufficient Funds"));

        assertBalance(SOURCE_ACCOUNT, "100.00");
        assertEquals(0L, transactionCount());
    }

    private ResultActions performPost(String path, int accountNumber, String body) throws Exception {
        return mockMvc.perform(post(path, accountNumber)
                .contentType(APPLICATION_JSON)
                .content(body));
    }

    private ResultActions performTransfer(String body) throws Exception {
        return performPost("/api/v1/accounts/{accountNumber}/transfers", SOURCE_ACCOUNT, body);
    }

    private void insertAccount(int accountNumber, String balance) {
        jdbcTemplate.update("""
                INSERT INTO accounts
                    (account_number, customer_id, account_type, balance, version,
                     pin, failed_attempts, is_locked)
                VALUES (?, 1, 'Cheque', ?, 0, 'test-pin', 0, FALSE)
                """, accountNumber, new BigDecimal(balance));
    }

    private void insertTransaction(
            int accountNumber,
            String type,
            String amount,
            String balanceAfter,
            String dateTime
    ) {
        jdbcTemplate.update("""
                INSERT INTO transactions
                    (account_number, transaction_type, amount, balance_after, transaction_date)
                VALUES (?, ?, ?, ?, ?)
                """, accountNumber, type, new BigDecimal(amount), new BigDecimal(balanceAfter),
                Timestamp.valueOf(dateTime));
    }

    private void assertBalance(int accountNumber, String expected) {
        BigDecimal actual = jdbcTemplate.queryForObject(
                "SELECT balance FROM accounts WHERE account_number = ?",
                BigDecimal.class,
                accountNumber);
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private long transactionCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions", Long.class);
    }

    private String transactionType(int index) {
        return jdbcTemplate.queryForObject(
                "SELECT transaction_type FROM transactions ORDER BY transaction_id LIMIT 1 OFFSET ?",
                String.class,
                index);
    }

    private BigDecimal transactionBalanceAfter(int index) {
        return jdbcTemplate.queryForObject(
                "SELECT balance_after FROM transactions ORDER BY transaction_id LIMIT 1 OFFSET ?",
                BigDecimal.class,
                index);
    }
}
