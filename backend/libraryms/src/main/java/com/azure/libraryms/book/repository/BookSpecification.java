package com.azure.libraryms.book.repository;

import org.springframework.data.jpa.domain.Specification;

import com.azure.libraryms.book.model.Book;

public class BookSpecification {
    
    public static Specification<Book> hasKeyword(String field, String value) {
        return (root, query, cb) -> value == null ? null
                : cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    public static Specification<Book> hasIsbn(String isbn) {
        return (root, query, cb) -> isbn == null ? null
                : cb.equal(root.get("isbn"), isbn);
    }

    public static Specification<Book> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }
}
