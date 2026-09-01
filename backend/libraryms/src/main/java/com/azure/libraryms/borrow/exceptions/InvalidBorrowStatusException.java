package com.azure.libraryms.borrow.exceptions;

import com.azure.libraryms.borrow.model.BorrowStatus;

public class InvalidBorrowStatusException extends RuntimeException {
    public InvalidBorrowStatusException(Long borrowRecordId, BorrowStatus currentStatus) {
        super("BorrowRecord %d cannot be returned, current status: %s"
                .formatted(borrowRecordId, currentStatus));
    }
}
