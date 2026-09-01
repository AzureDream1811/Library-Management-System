package com.azure.libraryms.borrow.repository;

import org.springframework.data.jpa.domain.Specification;

import com.azure.libraryms.auth.model.User;
import com.azure.libraryms.borrow.model.BorrowRecord;
import com.azure.libraryms.borrow.model.BorrowStatus;

public class BorrowRecordSpecification {

    public static Specification<BorrowRecord> hasKeyword(String field, String value) {
        return (root, query, cb) -> value == null ? null
                : cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    public static Specification<BorrowRecord> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<BorrowRecord> hasUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    public static Specification<BorrowRecord> hasStatus(BorrowStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
