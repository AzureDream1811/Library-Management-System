package com.azure.libraryms.borrow.dto.request;

import jakarta.validation.constraints.NotNull;

public record BorrowRecordCreateRequest(

        @NotNull Long bookId,
        @NotNull Long userId

) {

}
