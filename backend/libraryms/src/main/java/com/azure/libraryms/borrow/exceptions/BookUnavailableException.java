package com.azure.libraryms.borrow.exceptions;

public class BookUnavailableException extends RuntimeException {
    public BookUnavailableException(Long bookId) {
        super("Book is no longer available (id: " + bookId + "), please try again");
    }
}