package com.phakiso.enterprisebankingapi.transaction;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getTransactionsByAccountNumber(Integer accountNumber) {
        return transactionRepository.findByAccountNumber(accountNumber);
    }
}
