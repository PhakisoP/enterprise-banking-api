package com.phakiso.enterprisebankingapi.exception;

import com.phakiso.enterprisebankingapi.account.AccountNotFoundException;
import com.phakiso.enterprisebankingapi.account.InsufficientFundsException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Converts application and Spring MVC validation errors into
 * consistent HTTP ProblemDetail responses.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles requests for accounts that do not exist.
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problemDetail.setTitle("Account Not Found");
        problemDetail.setDetail(exception.getMessage());

        return problemDetail;
    }

    /**
     * Handles withdrawal requests where the account does not have
     * sufficient funds to complete the transaction.
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);

        problemDetail.setTitle("Insufficient Funds");
        problemDetail.setDetail(exception.getMessage());

        return problemDetail;
    }

    /**
     * Handles validation failures on incoming request bodies.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        var fieldError = exception.getBindingResult()
                .getFieldError();

        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle("Validation Failed");

        if (fieldError != null) {
            problemDetail.setDetail(fieldError.getDefaultMessage());
            problemDetail.setProperty("field", fieldError.getField());
        } else {
            problemDetail.setDetail("Request validation failed");
        }

        return ResponseEntity.status(status)
                .headers(headers)
                .body(problemDetail);
    }
}