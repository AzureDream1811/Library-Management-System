package com.azure.libraryms.book.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.azure.libraryms.book.model.Book;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    @Query("""
            SELECT b FROM Book b
            WHERE (:keyword IS NULL OR
                   LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   b.isbn = :keyword)
            """)
    Page<Book> search(@Param("keyword") String keyword, Pageable pageable);

}
