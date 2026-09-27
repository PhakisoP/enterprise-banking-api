package com.phakiso.enterprisebankingapi.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WithdrawalRequest(

        @Schema(description = "Withdrawal amount; must be at least 0.01", example = "25.50",
                minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Withdrawal amount is required")
        @DecimalMin(value = "0.01", message = "Withdrawal amount must be greater than zero")
        BigDecimal amount

) {
}
