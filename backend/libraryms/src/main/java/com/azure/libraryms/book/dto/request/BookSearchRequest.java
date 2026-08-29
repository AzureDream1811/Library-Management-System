package com.azure.libraryms.book.dto.request;

import org.springframework.data.domain.Pageable;

public record BookSearchRequest(
    String title, 
    String author, 
    String isbn, 
    Pageable pageable
) {
    
}
