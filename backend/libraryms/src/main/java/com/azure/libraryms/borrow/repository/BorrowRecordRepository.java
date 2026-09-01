package com.azure.libraryms.borrow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.azure.libraryms.auth.model.User;
import com.azure.libraryms.borrow.model.BorrowRecord;
import com.azure.libraryms.borrow.model.BorrowStatus;

public interface BorrowRecordRepository
        extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {

    long countByUserAndStatus(User user, BorrowStatus borrowed);

    Page<BorrowRecord> findByUser(User user, Pageable pageable);



}
