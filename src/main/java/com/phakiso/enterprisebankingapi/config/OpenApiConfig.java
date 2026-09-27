package com.phakiso.enterprisebankingapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
        info = @Info(
                title = "Enterprise Banking API",
                version = "1.0.0",
                description = "REST API for the Enterprise Banking project. " +
                        "Provides account details, transaction history, deposits, " +
                        "withdrawals, and transfers."
        )
)
public class OpenApiConfig {
}
