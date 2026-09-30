package com.phakiso.enterprisebankingapi.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositRequest(

        @Schema(description = "Deposit amount; must be at least 0.01", example = "25.50",
                minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Deposit amount is required")
        @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
        @Digits(integer = 17, fraction = 2, message = "Deposit amount must have no more than 2 decimal places")
        BigDecimal amount

) {
}
