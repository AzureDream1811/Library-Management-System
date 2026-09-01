package com.azure.libraryms.borrow.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.azure.libraryms.borrow.dto.resposne.BorrowRecordResponse;
import com.azure.libraryms.borrow.model.BorrowRecord;

@Mapper(componentModel = "spring")
public interface BorrowRecordMapper {

    @Mapping(source = "borrowRecord.user.id", target = "userId")
    @Mapping(source = "borrowRecord.book.id", target = "bookId")
    public BorrowRecordResponse toBorrowRecordResponse(BorrowRecord borrowRecord);



}
