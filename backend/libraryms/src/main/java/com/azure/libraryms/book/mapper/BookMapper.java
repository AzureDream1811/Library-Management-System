package com.azure.libraryms.book.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.azure.libraryms.book.dto.response.BookResponse;
import com.azure.libraryms.book.model.Book;

@Mapper(componentModel = "spring")
public interface BookMapper {

    public BookResponse mapToBookResponse(Book book);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public Book mapToBook(BookResponse bookResponse);

}
