package com.azure.libraryms.borrow.dto.resposne;

import java.time.LocalDate;

import com.azure.libraryms.borrow.model.BorrowStatus;

public record BorrowRecordResponse(
    Long id,
    Long bookId,
    Long userId,
    LocalDate borrowDate,
    LocalDate dueDate,
    BorrowStatus status
) {
    
}
