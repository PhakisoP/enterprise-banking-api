package com.phakiso.enterprisebankingapi.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountOptimisticLockingHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void optimisticLockingFailureIsReturnedAsHttpConflict() throws Exception {
        doThrow(new ObjectOptimisticLockingFailureException(Account.class, 888888))
                .when(accountService).withdraw(888888, new BigDecimal("1.00"));

        mockMvc.perform(post("/api/v1/accounts/888888/withdrawals")
                        .contentType("application/json")
                        .content("{\"amount\":1.00}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Concurrent Update Conflict"))
                .andExpect(jsonPath("$.status").value(409));
    }
}
