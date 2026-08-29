package com.azure.libraryms.book.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BookGetBtIsbnRequest(
    @NotBlank
    String isbn
) {
    
}
