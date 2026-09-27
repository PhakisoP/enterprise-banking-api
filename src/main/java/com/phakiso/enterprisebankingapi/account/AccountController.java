package com.phakiso.enterprisebankingapi.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Account queries and financial operations")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details", description = "Returns the public details and current balance for an account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account details returned"),
            @ApiResponse(responseCode = "404", description = "Account does not exist",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public AccountResponse getAccount(
            @Parameter(name = "accountNumber", in = ParameterIn.PATH,
                    description = "Account number to retrieve", example = "1001")
            @PathVariable Integer accountNumber
    ) {
        return accountService.getAccountByAccountNumber(accountNumber);
    }

    /**
     * Deposits money into an account and records the resulting transaction.
     *
     * @param accountNumber the account receiving the deposit
     * @param request the validated deposit request
     */
    @PostMapping("/{accountNumber}/deposits")
    @Operation(summary = "Deposit funds", description = "Adds a positive amount to the account and records a transaction.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Deposit amount", required = true))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit completed"),
            @ApiResponse(responseCode = "400", description = "Request validation failed",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Account does not exist",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Account was concurrently modified",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void deposit(
            @Parameter(name = "accountNumber", in = ParameterIn.PATH,
                    description = "Account number receiving the deposit", example = "1001")
            @PathVariable Integer accountNumber,
            @Valid @RequestBody DepositRequest request
    ) {
        accountService.deposit(accountNumber, request.amount());
    }

    /**
     * Withdraws money from an account and records the resulting transaction.
     *
     * @param accountNumber the account receiving the withdrawal
     * @param request the validated withdrawal request
     */
    @PostMapping("/{accountNumber}/withdrawals")
    @Operation(summary = "Withdraw funds", description = "Subtracts a positive amount from the account and records a transaction.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Withdrawal amount", required = true))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal completed"),
            @ApiResponse(responseCode = "400", description = "Request validation failed",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Account does not exist",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Account was concurrently modified",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Account has insufficient funds",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void withdraw(
            @Parameter(name = "accountNumber", in = ParameterIn.PATH,
                    description = "Account number from which to withdraw", example = "1001")
            @PathVariable Integer accountNumber,
            @Valid @RequestBody WithdrawalRequest request
    ) {
        accountService.withdraw(accountNumber, request.amount());
    }

    /** Transfers funds from this account to another account atomically. */
    @PostMapping("/{accountNumber}/transfers")
    @Operation(summary = "Transfer funds", description = "Transfers a positive amount to another account and records both sides of the transaction atomically.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Destination account and transfer amount", required = true))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed"),
            @ApiResponse(responseCode = "400", description = "Request validation failed or transfer destination is invalid",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Source or destination account does not exist",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "An account was concurrently modified",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Source account has insufficient funds",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void transfer(
            @Parameter(name = "accountNumber", in = ParameterIn.PATH,
                    description = "Source account number", example = "1001")
            @PathVariable Integer accountNumber,
            @Valid @RequestBody TransferRequest request
    ) {
        accountService.transfer(
                accountNumber,
                request.destinationAccountNumber(),
                request.amount()
        );
    }
}
