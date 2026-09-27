package com.phakiso.enterprisebankingapi.transaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts/{accountNumber}/transactions")
@Tag(name = "Transactions", description = "Account transaction history")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Get transaction history", description = "Returns transactions for the account, ordered from newest to oldest. An account with no transactions returns an empty array.")
    @ApiResponse(responseCode = "200", description = "Transaction history returned")
    public List<TransactionResponse> getTransactions(
            @Parameter(name = "accountNumber", in = ParameterIn.PATH,
                    description = "Account number whose transaction history is requested", example = "1001")
            @PathVariable Integer accountNumber) {
        return transactionService.getTransactionsByAccountNumber(accountNumber);
    }
}
