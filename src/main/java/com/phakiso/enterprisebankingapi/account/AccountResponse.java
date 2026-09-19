package com.phakiso.enterprisebankingapi.account;

import java.math.BigDecimal;

public record AccountResponse(
        Integer accountNumber,
        Integer customerId,
        String accountType,
        BigDecimal balance
) {
}
