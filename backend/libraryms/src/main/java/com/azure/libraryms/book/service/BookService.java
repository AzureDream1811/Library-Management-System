package com.azure.libraryms.book.service;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azure.libraryms.book.dto.request.BookCreateRequest;
import com.azure.libraryms.book.dto.request.BookPatchRequest;
import com.azure.libraryms.book.dto.request.BookSearchRequest;
import com.azure.libraryms.book.dto.response.BookResponse;
import com.azure.libraryms.book.mapper.BookMapper;
import com.azure.libraryms.book.model.Book;
import com.azure.libraryms.book.repository.BookRepository;
import com.azure.libraryms.book.repository.BookSpecification;
import com.azure.libraryms.common.exception.EntityAlreadyExistsException;
import com.azure.libraryms.common.exception.EntityNotFoundException;
import com.azure.libraryms.common.exception.IllegalArgumentException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    // Create book
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse createBook(BookCreateRequest request) {
        if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
            throw new EntityNotFoundException("Book", "ISBN", request.isbn());
        }

        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .totalCopies(request.totalCopies())
                .availableCopies(request.totalCopies())
                .build();

        try {
            Book savedBook = bookRepository.save(book);

            return bookMapper.mapToBookResponse(savedBook);

        } catch (DataIntegrityViolationException e) {
            throw new EntityNotFoundException("Book", "ISBN", request.isbn());
        }
    }

    // Search book
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(BookSearchRequest request, Pageable pageable) {
        Specification<Book> spec = Specification
                .where(BookSpecification.notDeleted())
                .and(BookSpecification.hasKeyword("title", request.title()))
                .and(BookSpecification.hasKeyword("author", request.author()))
                .and(BookSpecification.hasIsbn(request.isbn()));

        return bookRepository.findAll(spec, pageable)
                .map(bookMapper::mapToBookResponse);
    }

    // Update book
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse patchUpdate(Long id, BookPatchRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book", "ID", id.toString()));

        if (request.title() != null) {
            book.setTitle(request.title());
        }

        if (request.author() != null) {
            book.setAuthor(request.author());
        }

        if (request.isbn() != null && !request.isbn().equals(book.getIsbn())) {
            if (bookRepository.existsByIsbn(request.isbn())) {
                throw new EntityAlreadyExistsException("Book", "ISBN", request.isbn());
            }
            book.setIsbn(request.isbn());
        }

        if (request.totalCopies() != null) {
            applyTotalCopiesChange(book, request.totalCopies());
        }

        Book savedBook = bookRepository.save(book);

        return bookMapper.mapToBookResponse(savedBook);
    }

    // Delete book
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book", "ID", id.toString()));

        book.setDeletedAt(LocalDateTime.now());
        bookRepository.save(book);
    }

    private void applyTotalCopiesChange(Book book, int newTotalCopies) {
        int borrowedCount = book.getTotalCopies() - book.getAvailableCopies();
        if (newTotalCopies < borrowedCount) {
            throw new IllegalArgumentException(
                    "totalCopies (%d) không thể nhỏ hơn số bản đang được mượn (%d)"
                            .formatted(newTotalCopies, borrowedCount));
        }

        book.setTotalCopies(newTotalCopies);
        book.setAvailableCopies(newTotalCopies - borrowedCount);
    }

}
