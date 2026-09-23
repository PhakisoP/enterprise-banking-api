package com.phakiso.enterprisebankingapi.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void getAccount_shouldReturnAccountDetails() throws Exception {
        when(accountService.getAccountByAccountNumber(888888))
                .thenReturn(
                        new AccountResponse(
                                888888,
                                1,
                                "Cheque",
                                new BigDecimal("31000.00")
                        )
                );

        mockMvc.perform(
                        get("/api/v1/accounts/888888")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(888888))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.accountType").value("Cheque"))
                .andExpect(jsonPath("$.balance").value(31000.00));
    }

    @Test
    void deposit_shouldCallAccountService() throws Exception {
        mockMvc.perform(
                        post("/api/v1/accounts/888888/deposits")
                                .contentType("application/json")
                                .content("""
                                    {
                                        "amount": 1000.00
                                    }
                                    """)
                )
                .andExpect(status().isOk());

        verify(accountService).deposit(
                888888,
                new BigDecimal("1000.00")
        );
    }

    @Test
    void withdraw_shouldCallAccountService() throws Exception {
        mockMvc.perform(
                        post("/api/v1/accounts/888888/withdrawals")
                                .contentType("application/json")
                                .content("""
                                    {
                                        "amount": 500.00
                                    }
                                    """)
                )
                .andExpect(status().isOk());

        verify(accountService).withdraw(
                888888,
                new BigDecimal("500.00")
        );
    }

    @Test
    void deposit_shouldRejectInvalidAmount() throws Exception {
        mockMvc.perform(
                        post("/api/v1/accounts/888888/deposits")
                                .contentType("application/json")
                                .content("""
                                    {
                                        "amount": 0
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.detail").value("Deposit amount must be greater than zero"))
                .andExpect(jsonPath("$.field").value("amount"));

        verify(accountService, never()).deposit(
                anyInt(),
                any(BigDecimal.class)
        );
    }

    @Test
    void withdraw_shouldRejectInvalidAmount() throws Exception {
        mockMvc.perform(
                        post("/api/v1/accounts/888888/withdrawals")
                                .contentType("application/json")
                                .content("""
                                    {
                                        "amount": 0
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.detail").value("Withdrawal amount must be greater than zero"))
                .andExpect(jsonPath("$.field").value("amount"));

        verify(accountService, never()).withdraw(
                anyInt(),
                any(BigDecimal.class)
        );
    }
}
