package com.phakiso.enterprisebankingapi.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @Schema(description = "Required positive destination account number", example = "2002",
                minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Destination account number is required")
        @Positive(message = "Destination account number must be positive")
        Integer destinationAccountNumber,

        @Schema(description = "Transfer amount; must be at least 0.01", example = "25.50",
                minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Transfer amount is required")
        @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
        @Digits(integer = 17, fraction = 2, message = "Transfer amount must have no more than 2 decimal places")
        BigDecimal amount
) {
}
