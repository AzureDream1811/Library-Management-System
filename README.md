# Library Management System — Software Requirements Specification

## 1. Tổng quan

Hệ thống quản lý thư viện cho phép độc giả (`USER`) mượn/trả sách, thanh toán phí phạt trễ hạn và phí thành viên; quản
trị viên (`ADMIN`) quản lý sách, thành viên và theo dõi giao dịch. Backend xây bằng **Spring Boot** (Modular Monolith),
có phân quyền theo role và tích hợp cổng thanh toán.

**Phạm vi bổ sung so với bản gốc:** thêm module `Payment` để xử lý phí phạt trễ hạn và phí đăng ký thành viên, tích hợp
cổng thanh toán (đề xuất VNPay sandbox — phổ biến cho portfolio dev VN, có docs rõ ràng, dễ demo).

---

## 2. Actors / Roles

| Role    | Quyền hạn                                                                                     |
|---------|-----------------------------------------------------------------------------------------------|
| `ADMIN` | CRUD sách, quản lý thành viên, xem tất cả giao dịch mượn/trả và thanh toán, cấu hình phí phạt |
| `USER`  | Tìm kiếm sách, mượn/trả sách, xem lịch sử của mình, thanh toán phí phạt/phí thành viên        |

---

## 3. Entities

### 3.1 `User`

| Field               | Type                                 | Ghi chú           |
|---------------------|--------------------------------------|-------------------|
| id                  | Long                                 | PK                |
| fullName            | String                               |                   |
| email               | String                               | unique            |
| passwordHash        | String                               | BCrypt            |
| role                | Enum(`ADMIN`, `USER`)                |                   |
| membershipStatus    | Enum(`ACTIVE`, `EXPIRED`, `PENDING`) | phụ thuộc Payment |
| membershipExpiresAt | LocalDate                            |                   |

### 3.2 `Book`

| Field               | Type   | Ghi chú                     |
|---------------------|--------|-----------------------------|
| id                  | Long   | PK                          |
| title, author, isbn | String |                             |
| totalCopies         | int    |                             |
| availableCopies     | int    | giảm khi mượn, tăng khi trả |

### 3.3 `BorrowRecord`

| Field      | Type                                    | Ghi chú                                          |
|------------|-----------------------------------------|--------------------------------------------------|
| id         | Long                                    | PK                                               |
| user       | FK → User                               |                                                  |
| book       | FK → Book                               |                                                  |
| borrowDate | LocalDate                               |                                                  |
| dueDate    | LocalDate                               | mặc định +14 ngày                                |
| returnDate | LocalDate                               | null nếu chưa trả                                |
| status     | Enum(`BORROWED`, `RETURNED`, `OVERDUE`) |                                                  |
| fineAmount | BigDecimal                              | tính khi trả trễ hoặc job scheduled quét overdue |

### 3.4 `Payment` (mới)

| Field               | Type                                 | Ghi chú                            |
|---------------------|--------------------------------------|------------------------------------|
| id                  | Long                                 | PK                                 |
| user                | FK → User                            |                                    |
| type                | Enum(`FINE`, `MEMBERSHIP_FEE`)       |                                    |
| relatedBorrowRecord | FK → BorrowRecord                    | nullable, chỉ dùng khi type = FINE |
| amount              | BigDecimal                           |                                    |
| status              | Enum(`PENDING`, `SUCCESS`, `FAILED`) |                                    |
| provider            | String                               | vd: "VNPAY"                        |
| transactionRef      | String                               | mã giao dịch trả về từ gateway     |
| paidAt              | LocalDateTime                        | null nếu chưa thanh toán           |

---

## 4. Functional Requirements

### 4.1 Authentication & Authorization

- Đăng ký / đăng nhập (BCrypt + JWT hoặc session)
- `@PreAuthorize` theo role; `USER` chỉ thao tác trên dữ liệu của chính mình (ownership check)

### 4.2 Quản lý sách (ADMIN)

- CRUD sách, cập nhật số lượng bản copy

### 4.3 Mượn / trả sách (USER)

- Mượn sách: kiểm tra `availableCopies > 0`, kiểm tra `membershipStatus = ACTIVE`, giới hạn số sách mượn cùng lúc (vd:
  tối đa 5)
- Trả sách: tính `fineAmount` nếu `returnDate > dueDate` (vd: 5,000đ/ngày trễ)
- Scheduled job (`@Scheduled`) quét các bản ghi quá hạn mỗi ngày để cập nhật status `OVERDUE`

### 4.4 Thanh toán (mới)

- Tạo `Payment` khi:
    - User trả sách trễ → tự động tạo `Payment(type=FINE)` ở trạng thái `PENDING`
    - User đăng ký/gia hạn thành viên → tạo `Payment(type=MEMBERSHIP_FEE)`
- User thanh toán qua cổng VNPay sandbox → redirect sang trang thanh toán → callback (`IPN URL`) cập nhật
  `status = SUCCESS/FAILED` và `transactionRef`
- Khi `Payment(type=FINE)` thành công → không chặn mượn sách mới nữa
- Khi `Payment(type=MEMBERSHIP_FEE)` thành công → cập nhật `membershipStatus = ACTIVE`, `membershipExpiresAt`

---

## 5. API Endpoints (tóm tắt)

```
Auth
POST   /api/auth/register
POST   /api/auth/login

Book (ADMIN CRUD, public GET)
GET    /api/books
POST   /api/books                [ADMIN]
PUT    /api/books/{id}           [ADMIN]
DELETE /api/books/{id}           [ADMIN]

Borrow
POST   /api/borrow/{bookId}      [USER]
PUT    /api/borrow/{id}/return   [USER]
GET    /api/borrow/my-history    [USER]
GET    /api/borrow                [ADMIN] - xem tất cả

Payment
GET    /api/payments/my-pending  [USER]
POST   /api/payments/{id}/checkout   [USER] -> trả về payment URL (VNPay)
POST   /api/payments/vnpay-ipn       [PUBLIC/webhook] -> callback từ VNPay
GET    /api/payments                 [ADMIN] - xem tất cả giao dịch
```

---

## 6. Business Rules

- Không cho mượn sách mới nếu còn `Payment(FINE, status=PENDING)` chưa thanh toán
- Không cho mượn sách nếu `membershipStatus != ACTIVE`
- Phí phạt: cấu hình được (ADMIN set `finePerDay`), không hardcode
- Một `BorrowRecord` chỉ sinh tối đa 1 `Payment(FINE)` liên kết

---

## 7. Tech Stack đề xuất

- Spring Boot 3.x, Spring Security (JWT), Spring Data JPA, MySQL/PostgreSQL
- Flyway cho migration
- `@Scheduled` cho job quét overdue
- VNPay Merchant Sandbox cho payment integration
- JUnit 5 + Mockito cho unit test (đặc biệt logic tính fine và verify chữ ký IPN)