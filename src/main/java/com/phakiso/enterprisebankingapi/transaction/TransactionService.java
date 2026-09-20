package com.phakiso.enterprisebankingapi.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionResponse> getTransactionsByAccountNumber(Integer accountNumber) {
        return transactionRepository.findByAccountNumber(accountNumber)
                .stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getTransactionId(),
                        transaction.getAccountNumber(),
                        transaction.getTransactionType(),
                        transaction.getAmount(),
                        transaction.getBalanceAfter(),
                        transaction.getTransactionDate()
                ))
                .toList();
    }

    public Transaction createTransaction(
            Integer accountNumber,
            String transactionType,
            BigDecimal amount,
            BigDecimal balanceAfter
    ) {
        Transaction transaction = new Transaction(
                accountNumber,
                transactionType,
                amount,
                balanceAfter,
                LocalDateTime.now()
        );

        return transactionRepository.save(transaction);
    }
}
