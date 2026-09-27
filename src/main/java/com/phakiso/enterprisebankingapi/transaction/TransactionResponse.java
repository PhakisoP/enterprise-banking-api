package com.phakiso.enterprisebankingapi.transaction;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "A recorded account transaction.")
public record TransactionResponse(
        @Schema(description = "Transaction identifier", example = "501")
        Integer transactionId,
        @Schema(description = "Account number associated with the transaction", example = "1001")
        Integer accountNumber,
        @Schema(description = "Transaction type: Deposit, Withdrawal, Transfer Out, or Transfer In",
                example = "Deposit")
        String transactionType,
        @Schema(description = "Transaction amount", example = "25.50")
        BigDecimal amount,
        @Schema(description = "Account balance after this transaction", example = "125.50")
        BigDecimal balanceAfter,
        @Schema(description = "Date and time the transaction was recorded", example = "2026-09-27T12:30:00")
        LocalDateTime transactionDate
) {
}
