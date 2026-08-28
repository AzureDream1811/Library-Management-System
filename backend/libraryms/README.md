# Library Management System — Functional Requirements Document

## Danh sách chức năng

| Mã    | Chức năng                        | Actor                  |
| ----- | -------------------------------- | ---------------------- |
| FR-01 | Đăng ký tài khoản                | USER                   |
| FR-02 | Đăng nhập                        | USER, ADMIN            |
| FR-03 | Quản lý sách (CRUD)              | ADMIN                  |
| FR-04 | Tìm kiếm sách                    | USER, ADMIN            |
| FR-05 | Mượn sách                        | USER                   |
| FR-06 | Trả sách                         | USER                   |
| FR-07 | Tự động phát hiện quá hạn        | System (scheduled job) |
| FR-08 | Xem lịch sử mượn/trả             | USER, ADMIN            |
| FR-09 | Thanh toán phí phạt              | USER                   |
| FR-10 | Đăng ký / gia hạn thành viên     | USER                   |
| FR-11 | Xử lý callback thanh toán (IPN)  | System                 |
| FR-12 | Quản lý thành viên               | ADMIN                  |
| FR-13 | Cấu hình phí phạt                | ADMIN                  |
| FR-14 | Xem báo cáo giao dịch thanh toán | ADMIN                  |

---

### FR-01: Đăng ký tài khoản

- **Actor:** USER (chưa đăng nhập)
- **Mô tả:** Người dùng tạo tài khoản mới với email, mật khẩu, họ tên.
- **Điều kiện tiên quyết:** Email chưa tồn tại trong hệ thống.
- **Luồng chính:**
  1. User nhập email, password, fullName
  2. Hệ thống validate định dạng email, độ mạnh password
  3. Hệ thống hash password (BCrypt), tạo `User` với `role=USER`, `membershipStatus=PENDING`
  4. Trả về thông báo thành công
- **Luồng ngoại lệ:** Email đã tồn tại → trả lỗi 409 Conflict
- **Điều kiện sau:** Tài khoản được tạo, chưa thể mượn sách cho đến khi thanh toán phí thành viên (FR-10)

---

### FR-02: Đăng nhập

- **Actor:** USER, ADMIN
- **Mô tả:** Xác thực bằng email/password, trả về JWT.
- **Luồng chính:**
  1. User nhập email, password
  2. Hệ thống kiểm tra password bằng BCrypt
  3. Sinh JWT chứa `userId`, `role`
- **Luồng ngoại lệ:** Sai email/password → 401 Unauthorized

---

### FR-03: Quản lý sách (CRUD)

- **Actor:** ADMIN
- **Mô tả:** Thêm, sửa, xoá, xem thông tin sách.
- **Điều kiện tiên quyết:** Đăng nhập với role `ADMIN`
- **Luồng chính:**
  1. ADMIN gửi request tạo/sửa/xoá `Book` (title, author, isbn, totalCopies)
  2. Hệ thống validate dữ liệu, đồng bộ `availableCopies` khi tạo mới (= totalCopies)
- **Luồng ngoại lệ:** Xoá sách đang có `BorrowRecord` ở trạng thái `BORROWED` → chặn xoá, trả lỗi 400
- **Điều kiện sau:** Danh sách sách được cập nhật

---

### FR-04: Tìm kiếm sách

- **Actor:** USER, ADMIN
- **Mô tả:** Tìm sách theo tên, tác giả, ISBN.
- **Luồng chính:**
  1. User nhập từ khoá tìm kiếm
  2. Hệ thống trả về danh sách sách khớp kèm `availableCopies`

---

### FR-05: Mượn sách

- **Actor:** USER
- **Mô tả:** User mượn một cuốn sách còn sẵn.
- **Điều kiện tiên quyết:**
  - `membershipStatus = ACTIVE`
  - Không có `Payment(FINE, status=PENDING)` chưa thanh toán
  - Số sách đang mượn < giới hạn cho phép (mặc định 5)
  - `book.availableCopies > 0`
- **Luồng chính:**
  1. User chọn sách để mượn
  2. Hệ thống kiểm tra các điều kiện tiên quyết
  3. Tạo `BorrowRecord(status=BORROWED, dueDate=+14 ngày)`
  4. Giảm `availableCopies` của sách đi 1
- **Luồng ngoại lệ:**
  - Không đủ điều kiện tiên quyết → trả lỗi kèm lý do cụ thể (chưa là member / còn nợ phí / hết sách / vượt giới hạn
    mượn)

---

### FR-06: Trả sách

- **Actor:** USER
- **Mô tả:** User trả sách đã mượn.
- **Luồng chính:**
  1. User chọn `BorrowRecord` cần trả
  2. Hệ thống ghi nhận `returnDate = now`
  3. Nếu `returnDate > dueDate` → tính `fineAmount = số ngày trễ × finePerDay`, tạo
     `Payment(type=FINE, status=PENDING)` liên kết `BorrowRecord`
  4. Cập nhật `status = RETURNED`, tăng `availableCopies` của sách lên 1
- **Điều kiện sau:** Nếu có phí phạt, user không thể mượn sách mới cho đến khi thanh toán (FR-09)

---

### FR-07: Tự động phát hiện quá hạn

- **Actor:** System (scheduled job, chạy mỗi ngày 00:00)
- **Mô tả:** Quét các `BorrowRecord` có `dueDate < today` và `status = BORROWED`, cập nhật `status = OVERDUE`.
- **Luồng chính:**
  1. Job chạy định kỳ (`@Scheduled(cron=...)`)
  2. Truy vấn các bản ghi quá hạn chưa trả
  3. Cập nhật status, có thể gửi email nhắc nhở (optional, ngoài phạm vi bản v1)

---

### FR-08: Xem lịch sử mượn/trả

- **Actor:** USER (chỉ của mình), ADMIN (tất cả)
- **Mô tả:** Xem danh sách các lần mượn/trả kèm trạng thái, phí phạt (nếu có).

---

### FR-09: Thanh toán phí phạt

- **Actor:** USER
- **Mô tả:** User thanh toán khoản `Payment(type=FINE, status=PENDING)` qua cổng VNPay.
- **Điều kiện tiên quyết:** Tồn tại `Payment` ở trạng thái `PENDING` thuộc về user
- **Luồng chính:**
  1. User chọn khoản phí cần thanh toán, gọi checkout
  2. Hệ thống sinh request ký (HMAC SHA512), trả về `paymentUrl` của VNPay
  3. User thanh toán trên trang VNPay
  4. Hệ thống nhận callback (FR-11), cập nhật `status = SUCCESS`
- **Luồng ngoại lệ:** Thanh toán thất bại/hủy → `status = FAILED`, user có thể thử lại

---

### FR-10: Đăng ký / gia hạn thành viên

- **Actor:** USER
- **Mô tả:** User thanh toán phí thành viên để kích hoạt/gia hạn quyền mượn sách.
- **Luồng chính:**
  1. Hệ thống tạo `Payment(type=MEMBERSHIP_FEE, status=PENDING)`
  2. User checkout qua VNPay (tương tự FR-09)
  3. Sau khi `status=SUCCESS` → cập nhật `membershipStatus=ACTIVE`, `membershipExpiresAt = now + 1 năm`

---

### FR-11: Xử lý callback thanh toán (IPN)

- **Actor:** System (webhook từ VNPay)
- **Mô tả:** Nhận và xác thực kết quả thanh toán từ VNPay.
- **Luồng chính:**
  1. VNPay gọi `POST /api/payments/vnpay-ipn` với các tham số kết quả
  2. Hệ thống verify chữ ký (HMAC SHA512) để đảm bảo request hợp lệ, không giả mạo
  3. Nếu hợp lệ và `vnp_ResponseCode = "00"` → cập nhật `Payment.status = SUCCESS`, trigger side-effect tương ứng
     (FR-09/FR-10)
  4. Nếu không hợp lệ hoặc thất bại → `status = FAILED`
- **Luồng ngoại lệ:** Chữ ký không khớp → từ chối, log cảnh báo, không cập nhật trạng thái

---

### FR-12: Quản lý thành viên

- **Actor:** ADMIN
- **Mô tả:** Xem danh sách user, trạng thái membership, khoá/mở tài khoản.

---

### FR-13: Cấu hình phí phạt

- **Actor:** ADMIN
- **Mô tả:** Thiết lập `finePerDay`, giới hạn số sách mượn tối đa, số ngày mượn mặc định — không hardcode trong code.

---

### FR-14: Xem báo cáo giao dịch thanh toán

- **Actor:** ADMIN
- **Mô tả:** Xem danh sách tất cả `Payment` (lọc theo type, status, khoảng thời gian) phục vụ đối soát.
