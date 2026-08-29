package com.azure.libraryms.book.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.azure.libraryms.book.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);
    
}
