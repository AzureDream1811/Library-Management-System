-- V2__data_example.sql
-- Library Management System - sample data for local/dev testing
-- Plaintext password for all seeded users: Password123!
-- (hash below is a real BCrypt hash of that password, cost factor 10)

INSERT INTO users (email, password, full_name, role, membership_status, membership_expires_at) VALUES
('admin@library.com',  '$2b$10$y/1TxqIRthbp5t1Kd44ROuTxwnuSNQTkOhDC3UTbCc9.Dsw8Kc./q', 'Admin User',  'ADMIN', 'ACTIVE', NULL),
('alice@example.com',  '$2b$10$y/1TxqIRthbp5t1Kd44ROuTxwnuSNQTkOhDC3UTbCc9.Dsw8Kc./q', 'Alice Nguyen', 'USER',  'ACTIVE', '2027-08-28'),
('bob@example.com',    '$2b$10$y/1TxqIRthbp5t1Kd44ROuTxwnuSNQTkOhDC3UTbCc9.Dsw8Kc./q', 'Bob Tran',     'USER',  'PENDING', NULL);

INSERT INTO books (title, author, isbn, total_copies, available_copies) VALUES
('Clean Code',                  'Robert C. Martin',  '9780132350884', 3, 2),
('Effective Java',              'Joshua Bloch',      '9780134685991', 2, 2),
('Designing Data-Intensive Applications', 'Martin Kleppmann', '9781449373320', 2, 1),
('Spring in Action',            'Craig Walls',       '9781617294945', 4, 4);

-- Alice currently borrowing "Clean Code", already returned "Designing Data-Intensive Applications" late (with fine)
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, return_date, status, fine_amount) VALUES
(2, 1, '2026-08-20', '2026-09-03', NULL,          'BORROWED', 0.00),
(2, 3, '2026-07-01', '2026-07-15', '2026-07-20',  'RETURNED', 25000.00);

-- Fine payment for the late return above (still pending)
INSERT INTO payments (user_id, type, related_borrow_record_id, amount, status, provider, transaction_ref, paid_at) VALUES
(2, 'FINE', 2, 25000.00, 'PENDING', NULL, NULL, NULL);

-- Membership fee payment for Bob (pending — his membership_status is PENDING until this succeeds)
INSERT INTO payments (user_id, type, related_borrow_record_id, amount, status, provider, transaction_ref, paid_at) VALUES
(3, 'MEMBERSHIP_FEE', NULL, 100000.00, 'PENDING', NULL, NULL, NULL);

-- Example of a successful membership payment for Alice (already active)
INSERT INTO payments (user_id, type, related_borrow_record_id, amount, status, provider, transaction_ref, paid_at) VALUES
(2, 'MEMBERSHIP_FEE', NULL, 100000.00, 'SUCCESS', 'VNPAY', 'VNP14231098231', '2026-08-01 09:15:00');
