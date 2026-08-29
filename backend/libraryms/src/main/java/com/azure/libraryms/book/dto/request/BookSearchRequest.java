package com.azure.libraryms.book.dto.request;

public record BookSearchRequest(
    String title, 
    String author, 
    String isbn
) {
    
}
