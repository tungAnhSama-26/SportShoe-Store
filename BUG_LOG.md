# 📋 NHẬT KÝ THEO DÕI VÀ XỬ LÝ LỖI (BUG LOG)
**Dự án:** SportShoe-Store (Hệ thống Bán giày thể thao)  
**Cập nhật lần cuối:** 16/08/2026  

---

## 📊 1. BẢNG TỔNG HỢP DANH SÁCH LỖI (BUG TRACKING TABLE)

| Mã Bug | Tiêu đề Lỗi | Phân hệ | Mức độ | Trạng thái | Ngày xử lý | Người xử lý |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **BUG-001** | Ca làm vẫn hiển thị khi đã ngừng hoạt động / Cho phép ngừng ca khi đang có lịch | Quản lý Ca làm (Admin) | 🔴 Cao | ✅ Đã đóng | 15/08/2026 | AI & Dev |
| **BUG-002** | Bán hàng tại quầy bị ẩn số lượng tồn kho do cơ chế giữ hàng Online | Bán hàng tại quầy (POS) | 🔴 Nghiêm trọng | ✅ Đã đóng | 15/08/2026 | AI & Dev |
| **BUG-003** | Khách hàng không xem được Lý do hủy đơn hàng trên giao diện tra cứu/chi tiết | Client (Khách hàng) | 🟡 Trung bình | ✅ Đã đóng | 15/08/2026 | AI & Dev |
| **BUG-004** | Lỗi hiển thị thông tin đợt giảm giá (DGG) trong chi tiết hóa đơn quản trị | Quản lý Hóa đơn (Admin) | 🟡 Trung bình | ✅ Đã đóng | 14/08/2026 | AI & Dev |
| **BUG-005** | Xung đột tồn kho khả dụng (Soft-hold) làm giảm số lượng ảo của khách Online | Đặt hàng & Tồn kho | 🔴 Cao | ✅ Đã đóng | 16/08/2026 | AI & Dev |
| **BUG-006** | Lỗi định dạng hiển thị ký tự mã hóa Unicode trên Sidebar Admin | Giao diện Admin (FE) | 🟢 Thấp | ✅ Đã đóng | 16/08/2026 | AI & Dev |
| **BUG-007** | In hóa đơn PDF hiển thị `[object Object]` ở trường địa chỉ khách hàng | In hóa đơn / PDF (FE) | 🟡 Trung bình | ✅ Đã đóng | 17/08/2026 | AI & Dev |

---

## 📝 2. CHI TIẾT CÁC LỖI ĐÃ XỬ LÝ (RESOLVED BUGS)

### 🔹 BUG-001: Lỗi kiểm soát trạng thái Ca làm việc
- **Phân hệ:** Backend & Frontend Quản lý Ca làm (`CaLamServiceImpl.java`, `QuanLyCaLam.vue`)
- **Mô tả:** Khi một ca làm việc đang có nhân viên được xếp lịch trong tương lai, hệ thống vẫn cho phép chuyển trạng thái ca sang "Ngừng hoạt động", dẫn đến sai lệch lịch làm việc.
- **Nguyên nhân:** Thiếu kiểm tra ràng buộc sự tồn tại của các bản ghi `LichLamViec` trong ca trước khi cập nhật `trangThai = 0`.
- **Giải pháp xử lý:**
  - Bổ sung kiểm tra `lichLamViecRepository.existsByCaLamIdAndNgayLamViecAfter(...)`.
  - Nếu có lịch làm việc, chặn cập nhật và ném ra thông báo: *"Ca làm việc đang có lịch làm của nhân viên, vui lòng xóa hoặc chuyển lịch trước khi ngừng hoạt động"*.
- **Kết quả:** Đã test ngăn chặn thành công, bảo toàn tính toàn vẹn dữ liệu lịch làm việc.

---

### 🔹 BUG-002: Bán hàng tại quầy bị ảnh hưởng bởi đơn Online chờ xác nhận
- **Phân hệ:** POS (`TonKhoTaiQuayService.java`, `LogicBanHangTaiQuay.js`)
- **Mô tả:** Khách hàng Online đặt đơn hoặc quét QR làm giảm `soLuongKhaDung`, khiến nhân viên bán hàng tại quầy không nhìn thấy biến thể giày (ví dụ: Size 36) hoặc bị báo hết hàng dù trên kệ cửa hàng vẫn còn giày thực tế.
- **Nguyên nhân:** Logic POS gọi chung hàm kiểm tra tồn kho khả dụng của kênh Online thay vì lấy trực tiếp tồn kho thực tế (`GiayChiTiet.soLuong`).
- **Giải pháp xử lý:**
  - Tách riêng `TonKhoTaiQuayService` để lấy trực tiếp tồn kho vật lý.
  - Thiết lập chính sách: Khách mua tại quầy luôn có quyền ưu tiên cao nhất so với đơn online đang chờ duyệt.
- **Kết quả:** Nhân viên tại quầy luôn xem và bán được đúng 100% số lượng giày thực tế trong kho.

---

### 🔹 BUG-003: Không hiển thị Lý do hủy đơn hàng cho Khách hàng
- **Phân hệ:** Frontend Client (`ChiTietDonHang.vue`, `TraCuuDonHang.vue`)
- **Mô tả:** Khi đơn hàng bị Admin hủy (ví dụ do hết hàng hoặc khách yêu cầu), giao diện khách chỉ hiện thông báo chung "Đơn hàng đã hủy" mà không biết nguyên nhân cụ thể.
- **Nguyên nhân:** Giao diện chi tiết đơn chưa bóc tách trường `ghiChu` từ mảng `lichSuTrangThai` của hóa đơn.
- **Giải pháp xử lý:**
  - Thêm computed `lyDoHuyDon` tự động trích xuất ghi chú từ bản ghi sự kiện hủy đơn (`HUY`, `CHAP_NHAN_YEU_CAU_HUY`).
  - Hiển thị trực tiếp dòng: `Lý do hủy: [Nội dung ghi chú]` ngay trong banner trạng thái màu đỏ.
- **Kết quả:** Khách hàng đăng nhập và khách tra cứu vãng lai đều xem được lý do hủy đơn minh bạch.

---

### 🔹 BUG-004: Lỗi hiển thị Đợt giảm giá (DGG) trong chi tiết Hóa đơn
- **Phân hệ:** Backend Quản lý Hóa đơn (`QuanLyHoaDonServiceImpl.java`)
- **Mô tả:** Một số hóa đơn có sản phẩm được áp dụng đợt giảm giá nhưng khi mở chi tiết hóa đơn thì tên đợt giảm giá hoặc giá trị giảm hiển thị không chính xác.
- **Nguyên nhân:** Hàm `selectProductDiscount` chưa so khớp chính xác khoảng thời gian hiệu lực (`ngayBatDau`, `ngayKetThuc`) với ngày tạo hóa đơn và loại giảm (phần trăm / tiền mặt).
- **Giải pháp xử lý:**
  - Viết lại hàm `selectProductDiscount` và `calculateProductDiscountPrice` chuẩn hóa việc so khớp giá bán sau giảm với giá ghi nhận trên hóa đơn.
- **Kết quả:** Đã viết bộ Unit Test `QuanLyHoaDonServiceImplDiscountTest` và vượt qua toàn bộ 3/3 test cases.

---

### 🔹 BUG-005: Xung đột và giữ hàng ảo (Soft-hold) ở luồng Đặt hàng Online
- **Phân hệ:** Backend & Frontend Đặt hàng (`ClientDatHangService.java`, `ClientSanPhamController.java`)
- **Mô tả:** Khách quét QR giữ hàng 5 phút gây khó hiểu và làm ẩn số lượng sản phẩm trên website đối với các khách hàng khác, dẫn đến tình trạng "báo hết hàng ảo".
- **Nguyên nhân:** Áp dụng cơ chế `TonKhoKhaDungService` (soft-hold) phức tạp trước khi trừ kho thực tế.
- **Giải pháp xử lý:**
  - Gỡ bỏ hoàn toàn cơ chế giữ hàng ảo.
  - Chuyển về cơ chế chuẩn: Website hiển thị theo tồn thực tế; tồn kho chỉ trừ thật khi Admin bấm **"Xác nhận đơn"**.
- **Kết quả:** Toàn bộ 18/18 Unit Test chạy qua thành công (`BUILD SUCCESS`).

---

### 🔹 BUG-006: Lỗi hiển thị mã hóa Unicode trên Sidebar Admin
- **Phân hệ:** Frontend Layout Admin (`SidebarAdmin.vue`)
- **Mô tả:** Tên menu mục Lịch làm việc bị hiển thị chuỗi escape unicode `L\u1ecbch l\u00e0m vi\u1ec7c` và bị xuống dòng khi co giãn màn hình.
- **Nguyên nhân:** Ký tự unicode bị encode thô và thiếu class chống ngắt dòng.
- **Giải pháp xử lý:**
  - Sửa lại text tiếng Việt trực tiếp và thêm class `whitespace-nowrap`.
- **Kết quả:** Menu hiển thị đẹp mắt, chuẩn font tiếng Việt trên mọi kích thước màn hình.

---

### 🔹 BUG-007: In hóa đơn PDF hiển thị `[object Object]` ở trường địa chỉ khách hàng
- **Phân hệ:** Frontend In hóa đơn & Xuất PDF (`invoice-pdf.js`)
- **Mô tả:** Khi nhân viên bấm In hóa đơn PDF trong Quản lý hóa đơn, trường Địa chỉ người nhận hiển thị chuỗi `[object Object]` thay vì chuỗi địa chỉ đầy đủ (Số nhà, Phường/Xã, Quận/Huyện, Tỉnh/Thành).
- **Nguyên nhân:** Dữ liệu hóa đơn `invoice.diaChi` trả về là một Object chứa các trường địa chỉ chi tiết (`{ diaChiCuThe, phuongXa, tinhThanh }`). Hàm in hóa đơn chuyển ép kiểu thô `String(invoice.diaChi)` thành `[object Object]`.
- **Giải pháp xử lý:**
  - Import hàm `dinhDangDiaChi` từ `dia-chi.js`.
  - Viết hàm `getInvoiceAddress(invoice)` kiểm tra nếu là Object thì tự động ghép chuỗi địa chỉ đầy đủ.
- **Kết quả:** Địa chỉ khách hàng khi in hóa đơn bán hàng hiển thị chính xác, rõ ràng và đầy đủ.

---

## 📌 3. BIỂU MẪU GHI NHẬN LỖI MỚI (BUG REPORT TEMPLATE)

*Dùng biểu mẫu này khi phát hiện lỗi mới để ghi vào file:*

```markdown
### 🔹 [BUG-XXX]: [Tiêu đề ngắn gọn mô tả lỗi]
- **Phân hệ:** [Backend / Frontend / POS / Mobile / Database]
- **Người báo cáo:** [Tên người phát hiện]
- **Mức độ nghiêm trọng:** [🔴 Nghiêm trọng / 🟡 Trung bình / 🟢 Thấp]
- **Các bước tái hiện (Steps to Reproduce):**
  1. Bước 1...
  2. Bước 2...
  3. Bước 3...
- **Kết quả thực tế (Actual Result):** [Hệ thống bị lỗi gì / báo lỗi gì]
- **Kết quả mong muốn (Expected Result):** [Hành vi đúng cần đạt được]
- **Nguyên nhân gốc rễ (Root Cause):** [Điền sau khi điều tra]
- **Giải pháp xử lý (Resolution):** [Cách khắc phục]
- **Trạng thái:** [Mới mở / Đang sửa / Đã đóng]
```
