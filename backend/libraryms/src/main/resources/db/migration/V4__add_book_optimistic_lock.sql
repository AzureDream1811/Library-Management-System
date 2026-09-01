-- V4__add_book_optimistic_lock.sql
-- Add optimistic locking support to books, to prevent lost updates when multiple users
-- attempt to update the same book concurrently. This is done by adding a version column
ALTER TABLE books ADD COLUMN version BIGINT NOT NULL DEFAULT 0;