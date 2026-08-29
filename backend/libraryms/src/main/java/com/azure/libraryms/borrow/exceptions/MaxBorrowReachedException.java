package com.azure.libraryms.borrow.exceptions;

public class MaxBorrowReachedException extends RuntimeException {
    public MaxBorrowReachedException(String userName) {
        super("User " + userName + " has reached the maximum borrow limit.");
    }
    
}
