package com.azure.libraryms.borrow.dto.request;

import java.time.LocalDate;

import com.azure.libraryms.borrow.model.BorrowStatus;

public record BorrowRecordsUserRequest(
        BorrowStatus status,

        LocalDate borrowDate,

        LocalDate dueDate,

        LocalDate returnDate) {

}
