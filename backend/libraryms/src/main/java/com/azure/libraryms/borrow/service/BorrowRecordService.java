package com.azure.libraryms.borrow.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azure.libraryms.auth.model.User;
import com.azure.libraryms.auth.repository.UserRepository;
import com.azure.libraryms.book.model.Book;
import com.azure.libraryms.book.repository.BookRepository;
import com.azure.libraryms.borrow.dto.request.BorrowRecordCreateRequest;
import com.azure.libraryms.borrow.dto.resposne.BorrowRecordResponse;
import com.azure.libraryms.borrow.exceptions.BookUnavailableException;
import com.azure.libraryms.borrow.exceptions.MaxBorrowReachedException;
import com.azure.libraryms.borrow.exceptions.NoAvailableCopiesException;
import com.azure.libraryms.borrow.mapper.BorrowRecordMapper;
import com.azure.libraryms.borrow.model.BorrowRecord;
import com.azure.libraryms.borrow.model.BorrowStatus;
import com.azure.libraryms.borrow.repository.BorrowRecordRepository;
import com.azure.libraryms.common.exception.EntityNotFoundException;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BorrowRecordService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordMapper borrowRecordMapper;

    public BorrowRecordResponse createBorrowRecord(BorrowRecordCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User", "ID", request.userId().toString()));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new EntityNotFoundException("Book", "ID", request.bookId().toString()));

        validateBorrowEligibility(user, book);

        BorrowRecord borrowRecord = BorrowRecord.builder()
                .user(user)
                .book(book)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14)) // Assuming a 2-week borrowing period
                .status(BorrowStatus.BORROWED)
                .build();

        try {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookRepository.save(book);
        } catch (OptimisticLockException e) {
            throw new BookUnavailableException(book.getId());
        }
        
        BorrowRecord savedBorrowRecord = borrowRecordRepository.save(borrowRecord);

        return borrowRecordMapper.toBorrowRecordResponse(savedBorrowRecord);
    }

    private void validateBorrowEligibility(User user, Book book) {
        if (book.getAvailableCopies() <= 0) {
            throw new NoAvailableCopiesException(book.getTitle());
        }

        long activeBorrows = borrowRecordRepository.countByUserAndStatus(user, BorrowStatus.BORROWED);
        if (activeBorrows >= 5) { // Assuming a maximum of 5 active borrows per user
            throw new MaxBorrowReachedException(user.getUsername());
        }
    }

}
