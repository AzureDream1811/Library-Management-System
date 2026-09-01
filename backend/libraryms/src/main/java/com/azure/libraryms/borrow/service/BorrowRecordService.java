package com.azure.libraryms.borrow.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azure.libraryms.auth.model.User;
import com.azure.libraryms.auth.repository.UserRepository;
import com.azure.libraryms.book.model.Book;
import com.azure.libraryms.book.repository.BookRepository;
import com.azure.libraryms.borrow.dto.request.BorrowRecordCreateRequest;
import com.azure.libraryms.borrow.dto.request.BorrowRecordsUserRequest;
import com.azure.libraryms.borrow.dto.resposne.BorrowRecordResponse;
import com.azure.libraryms.borrow.exceptions.BookUnavailableException;
import com.azure.libraryms.borrow.exceptions.InvalidBorrowStatusException;
import com.azure.libraryms.borrow.exceptions.MaxBorrowReachedException;
import com.azure.libraryms.borrow.exceptions.NoAvailableCopiesException;
import com.azure.libraryms.borrow.mapper.BorrowRecordMapper;
import com.azure.libraryms.borrow.model.BorrowRecord;
import com.azure.libraryms.borrow.model.BorrowStatus;
import com.azure.libraryms.borrow.repository.BorrowRecordRepository;
import com.azure.libraryms.borrow.repository.BorrowRecordSpecification;
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

    private final BigDecimal finePerDay = new BigDecimal("1.00");

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

    public BorrowRecordResponse returnBook(Long borrowRecordId) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new EntityNotFoundException("BorrowRecord", "ID", borrowRecordId.toString()));

        if (borrowRecord.getStatus() != BorrowStatus.BORROWED && borrowRecord.getStatus() != BorrowStatus.OVERDUE) {
            throw new InvalidBorrowStatusException(borrowRecord.getBook().getId(), borrowRecord.getStatus());
        }

        LocalDate returnDate = LocalDate.now();
        borrowRecord.setReturnDate(returnDate);
        borrowRecord.setStatus(BorrowStatus.RETURNED);

        if (returnDate.isAfter(borrowRecord.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(borrowRecord.getDueDate(), returnDate);
            BigDecimal fineAmount = finePerDay.multiply(BigDecimal.valueOf(overdueDays));
            borrowRecord.setFineAmount(fineAmount);

            // TODO: create a fine payment with payment repository and set the fine payment
            // status to unpaid
        }

        Book book = borrowRecord.getBook();
        try {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        } catch (OptimisticLockException e) {
            throw new BookUnavailableException(book.getId());
        }

        BorrowRecord updatedBorrowRecord = borrowRecordRepository.save(borrowRecord);

        return borrowRecordMapper.toBorrowRecordResponse(updatedBorrowRecord);
    }

    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> getUserBorrowRecords(Long userId,
            BorrowRecordsUserRequest request,
            Pageable pageable) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", "ID", userId.toString()));

        Specification<BorrowRecord> spec = Specification
                .where(BorrowRecordSpecification.hasUser(user))
                .and(BorrowRecordSpecification.hasStatus(request.status()));

        return borrowRecordRepository.findAll(spec, pageable).map(borrowRecordMapper::toBorrowRecordResponse);
    }

    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> getAllBorrowRecords(BorrowRecordsUserRequest request, Pageable pageable) {
        Specification<BorrowRecord> spec = Specification
                .where(BorrowRecordSpecification.notDeleted())
                .and(BorrowRecordSpecification.hasStatus(request.status()));

        return borrowRecordRepository.findAll(spec, pageable).map(borrowRecordMapper::toBorrowRecordResponse);
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
