-- V1__schema.sql
-- Library Management System - initial schema

CREATE TABLE users (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    email                   VARCHAR(255)    NOT NULL,
    password                VARCHAR(255)    NOT NULL,
    full_name               VARCHAR(255)    NOT NULL,
    role                    VARCHAR(20)     NOT NULL,
    membership_status       VARCHAR(20)     NOT NULL,
    membership_expires_at   DATE,
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'USER')),
    CONSTRAINT chk_users_membership_status CHECK (membership_status IN ('ACTIVE', 'EXPIRED', 'PENDING'))
);

CREATE TABLE books (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(255)    NOT NULL,
    author              VARCHAR(255)    NOT NULL,
    isbn                VARCHAR(20)     NOT NULL,
    total_copies        INT             NOT NULL,
    available_copies    INT             NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_books_isbn UNIQUE (isbn),
    CONSTRAINT chk_books_total_copies CHECK (total_copies >= 0),
    CONSTRAINT chk_books_available_copies CHECK (available_copies >= 0 AND available_copies <= total_copies)
);

CREATE TABLE borrow_records (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    book_id         BIGINT          NOT NULL,
    borrow_date     DATE            NOT NULL,
    due_date        DATE            NOT NULL,
    return_date     DATE,
    status          VARCHAR(20)     NOT NULL,
    fine_amount     DECIMAL(10, 2)  NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_borrow_records_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_borrow_records_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT chk_borrow_records_status CHECK (status IN ('BORROWED', 'RETURNED', 'OVERDUE')),
    CONSTRAINT chk_borrow_records_fine_amount CHECK (fine_amount >= 0)
);

CREATE INDEX idx_borrow_records_user_id ON borrow_records (user_id);
CREATE INDEX idx_borrow_records_book_id ON borrow_records (book_id);
CREATE INDEX idx_borrow_records_status ON borrow_records (status);

CREATE TABLE payments (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                     BIGINT          NOT NULL,
    type                        VARCHAR(20)     NOT NULL,
    related_borrow_record_id    BIGINT,
    amount                      DECIMAL(10, 2)  NOT NULL,
    status                      VARCHAR(20)     NOT NULL,
    provider                    VARCHAR(50),
    transaction_ref             VARCHAR(100),
    paid_at                     DATETIME,
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_payments_borrow_record FOREIGN KEY (related_borrow_record_id) REFERENCES borrow_records (id),
    CONSTRAINT chk_payments_type CHECK (type IN ('FINE', 'MEMBERSHIP_FEE')),
    CONSTRAINT chk_payments_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    CONSTRAINT chk_payments_amount CHECK (amount > 0),
    CONSTRAINT chk_payments_fine_has_record CHECK (
    (type = 'FINE' AND related_borrow_record_id IS NOT NULL) OR (type = 'MEMBERSHIP_FEE')
)
);

CREATE INDEX idx_payments_user_id ON payments (user_id);
CREATE INDEX idx_payments_status ON payments (status);
