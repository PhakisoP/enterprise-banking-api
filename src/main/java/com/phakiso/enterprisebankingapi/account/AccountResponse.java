package com.phakiso.enterprisebankingapi.account;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Public account details returned by account queries.")
public record AccountResponse(
        @Schema(description = "Account number", example = "1001")
        Integer accountNumber,
        @Schema(description = "Identifier of the account's customer", example = "1")
        Integer customerId,
        @Schema(description = "Account type", example = "Cheque")
        String accountType,
        @Schema(description = "Current account balance", example = "125.50")
        BigDecimal balance
) {
}
