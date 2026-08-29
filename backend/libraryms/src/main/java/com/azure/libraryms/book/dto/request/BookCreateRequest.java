package com.azure.libraryms.book.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BookCreateRequest(
        @Size(min = 1, max = 50, message = "Title must be between 1 and 50 characters") String title,
        @Size(min = 1, max = 50, message = "Author must be between 1 and 50 characters") String author,
        @Size(min = 1, max = 20, message = "ISBN must be between 1 and 20 characters") String isbn,
        @Positive Integer totalCopies) {

}
