package com.phakiso.enterprisebankingapi.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Integer transactionId,
        Integer accountNumber,
        String transactionType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        LocalDateTime transactionDate
) {
}
