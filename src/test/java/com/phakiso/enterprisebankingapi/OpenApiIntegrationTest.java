package com.phakiso.enterprisebankingapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocumentExposesMetadataAndAccountOperations() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Enterprise Banking API"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.info.description").value(
                        "REST API for the Enterprise Banking project. " +
                                "Provides account details, transaction history, deposits, " +
                                "withdrawals, and transfers."))
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountNumber}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountNumber}/transactions'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountNumber}/deposits'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountNumber}/withdrawals'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountNumber}/transfers'].post").exists())
                .andExpect(jsonPath("$.components.securitySchemes").doesNotExist());
    }
}
