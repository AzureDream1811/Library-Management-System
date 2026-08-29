package com.azure.libraryms.book.mapper;

import org.mapstruct.Mapper;

import com.azure.libraryms.book.dto.response.BookResponse;
import com.azure.libraryms.book.model.Book;

@Mapper 
public interface BookMapper {

    public BookResponse mapToBookResponse(Book book);

    public Book mapToBook(BookResponse bookResponse);
    
}
