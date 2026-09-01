package com.azure.libraryms.common.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.azure.libraryms.borrow.exceptions.BookUnavailableException;
import com.azure.libraryms.borrow.exceptions.MaxBorrowReachedException;
import com.azure.libraryms.borrow.exceptions.NoAvailableCopiesException;
import com.azure.libraryms.borrow.exceptions.InvalidBorrowStatusException;
import com.azure.libraryms.common.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Not Found");
    }

    @ExceptionHandler({
        EntityAlreadyExistsException.class,
        BookUnavailableException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictExceptions(RuntimeException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, "Conflict");
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        MaxBorrowReachedException.class,
        NoAvailableCopiesException.class,
        InvalidBorrowStatusException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Bad Request");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception ex, HttpStatus status, String error) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(), status.value(), error, ex.getMessage());
        return new ResponseEntity<>(response, status);
    }
}