package com.azure.libraryms.borrow.exceptions;

public class NoAvailableCopiesException extends RuntimeException {
    public NoAvailableCopiesException(String title) {
        super("No available copies for the book: " + title);
    }
    
}
