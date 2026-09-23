package com.phakiso.enterprisebankingapi.exception;

import com.phakiso.enterprisebankingapi.account.AccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts application and request validation errors into
 * consistent HTTP ProblemDetail responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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
     * Handles validation failures on incoming request bodies.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception
    ) {
        var fieldError = exception.getBindingResult()
                .getFieldError();

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problemDetail.setTitle("Validation Failed");

        if (fieldError != null) {
            problemDetail.setDetail(fieldError.getDefaultMessage());
            problemDetail.setProperty("field", fieldError.getField());
        } else {
            problemDetail.setDetail("Request validation failed");
        }

        return problemDetail;
    }
}