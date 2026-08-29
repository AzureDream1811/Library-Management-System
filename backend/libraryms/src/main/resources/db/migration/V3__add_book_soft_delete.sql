-- V3__add_book_soft_delete.sql
-- Add soft-delete support to books, replacing hard delete to avoid
-- FK violations against borrow_records for books with borrow history.

ALTER TABLE books
    ADD COLUMN deleted_at DATETIME NULL;

-- Drop the plain unique index on isbn — it would block re-adding a book
-- with the same ISBN after the original row is soft-deleted.
ALTER TABLE books
    DROP INDEX uk_books_isbn;

-- Partial unique index: isbn stays unique only among non-deleted rows.
-- (MySQL 8 functional index: expression evaluates to NULL for deleted rows,
-- and MySQL's unique index allows multiple NULLs.)
CREATE UNIQUE INDEX uk_books_isbn_active
    ON books (isbn, (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END));

CREATE INDEX idx_books_deleted_at ON books (deleted_at);
