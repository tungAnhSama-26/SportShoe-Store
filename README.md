<div align="center">

# 👟 SPORTSHOE STORE - HỆ THỐNG QUẢN LÝ BÁN GIÀY THỂ THAO & BÁN HÀNG TẠI QUẦY (POS)

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)](https://vuejs.org/)
[![React Native](https://img.shields.io/badge/React_Native-Expo-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactnative.dev/)
[![SQL Server](https://img.shields.io/badge/Microsoft%20SQL%20Server-2022-CC292B?style=for-the-badge&logo=microsoft-sql-server&logoColor=white)](https://www.microsoft.com/sql-server)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.0-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

*Dự án tốt nghiệp / Hệ thống thương mại điện tử chuyên biệt cho giày thể thao, tích hợp toàn diện giữa cửa hàng trực tuyến (E-Commerce Web), hệ thống quản trị chuyên sâu (Admin Dashboard), hệ thống Bán hàng tại quầy đa năng (Web POS) và ứng dụng di động/máy tính bảng (PosMobile) đồng bộ thời gian thực.*

[🌐 Trải nghiệm Trực tiếp (Demo Online)](https://sportshoestore.duckdns.org) • [📱 Màn hình POS Mobile](https://sportshoestore.duckdns.org/pos-mobile) • [💻 Màn hình POS Tablet](https://sportshoestore.duckdns.org/pos) • [📖 Báo cáo Lỗi & Đóng góp](https://github.com/tungAnhSama-26/SportShoe-Store/issues)

</div>

---

## 📑 MỤC LỤC
- [1. Giới thiệu tổng quan](#-1-giới-thiệu-tổng-quan)
- [2. Kiến trúc hệ thống & Công nghệ sử dụng](#-2-kiến-trúc-hệ-thống--công-nghệ-sử-dụng)
- [3. Tính năng nổi bật](#-3-tính-năng-nổi-bật)
  - [3.1. Phân hệ Bán hàng tại quầy (POS & PosMobile)](#31-phân-hệ-bán-hàng-tại-quầy-pos--posmobile)
  - [3.2. Phân hệ Khách hàng (E-Commerce Storefront)](#32-phân-hệ-khách-hàng-e-commerce-storefront)
  - [3.3. Phân hệ Quản trị (Admin Management)](#33-phân-hệ-quản-trị-admin-management)
  - [3.4. Trợ lý ảo AI & Tích hợp bên thứ ba](#34-trợ-lý-ảo-ai--tích-hợp-bên-thứ-ba)
- [4. Hướng dẫn cài đặt & Chạy cục bộ (Local Development)](#-4-hướng-dẫn-cài-đặt--chạy-cục-bộ-local-development)
  - [4.1. Yêu cầu môi trường](#41-yêu-cầu-môi-trường)
  - [4.2. Khởi tạo Cơ sở dữ liệu](#42-khởi-tạo-cơ-sở-dữ-liệu)
  - [4.3. Chạy Backend (Spring Boot)](#43-chạy-backend-spring-boot)
  - [4.4. Chạy Frontend Web (Vue 3)](#44-chạy-frontend-web-vue-3)
  - [4.5. Chạy PosMobile (Expo / React Native)](#45-chạy-posmobile-expo--react-native)
- [5. Triển khai bằng Docker (Docker Deployment)](#-5-triển-khai-bằng-docker-docker-deployment)
- [6. Tài khoản dùng thử (Demo Accounts)](#-6-tài-khoản-dùng-thử-demo-accounts)

---

## 🌟 1. Giới thiệu tổng quan

**SportShoe Store** là giải pháp phần mềm quản lý bán hàng đa kênh (Omnichannel) toàn diện, được thiết kế để giải quyết bài toán vận hành thực tế cho các chuỗi cửa hàng giày dép thể thao:
* **Đa nền tảng:** Kết hợp mượt mà giữa Web Khách hàng, Web Admin, Màn hình POS Tablet và Ứng dụng POS Mobile.
* **Thời gian thực (Real-time Full Sync):** Sử dụng giao thức WebSocket STOMP giúp giỏ hàng, hóa đơn chờ, trạng thái thanh toán và tồn kho được đồng bộ tức thì giữa nhân viên thu ngân và khách hàng/thiết bị phụ trợ.
* **Thông minh & Tự động hóa:** Tự động đề xuất Voucher tối ưu, tính phí vận chuyển GHN theo địa giới hành chính chuẩn 2 cấp, tạo mã VietQR động theo số tiền thực thu, và hỗ trợ Chatbot AI tư vấn sản phẩm.

---

## 🏗️ 2. Kiến trúc hệ thống & Công nghệ sử dụng

### ⚙️ Backend Layer
* **Ngôn ngữ & Framework:** Java 17, Spring Boot 3.2.5 (RESTful API, Spring Security, Spring Data JPA, WebSocket STOMP, Spring Validation).
* **Database & Caching:** Microsoft SQL Server 2022 / Azure SQL Edge, HikariCP.
* **Xác thực & Phân quyền:** JWT (JSON Web Tokens) với RBAC (`ROLE_ADMIN`, `ROLE_STAFF`, `ROLE_CUSTOMER`).
* **Thư viện tích hợp:** Lombok, Hibernate ORM, Apache POI (Xuất Excel), iText/HTML2PDF (In hóa đơn).

### 🖥️ Frontend Web Layer
* **Framework:** Vue 3 (Composition API & `<script setup>`), Vite, Vue Router 4.
* **Styling & UI:** Tailwind CSS v4, Lucide Icons, SweetAlert2.
* **Biểu đồ & Tiện ích:** Chart.js, Vue-Chartjs, ZXing (Quét mã vạch/QR qua Camera), Axios, StompJS.

### 📱 Mobile/Tablet POS Layer
* **Framework:** React Native, Expo SDK 56 (hỗ trợ Android, iOS & Expo Web).
* **Quản trị trạng thái & Kết nối:** React Context API, StompJS, Axios.

### 🔌 Dịch vụ & Cổng tích hợp bên thứ ba
* **Cổng thanh toán:** VNPay Gateway, VietQR (Mã QR động NAPAS 247).
* **Giao hàng & Địa chỉ:** API Giao Hàng Nhanh (GHN), Bản đồ địa giới hành chính Việt Nam chuẩn 2 cấp (Tỉnh/Thành - Phường/Xã).
* **AI Chatbot & LLM:** Google Gemini API, OpenAI GPT, DeepSeek, Groq Cloud, và fallback máy chủ cục bộ Ollama (Qwen2.5 / Qwen3).
* **Email tự động:** JavaMailSender (Gửi hóa đơn điện tử & thông báo đơn hàng bất đồng bộ).

---

## ✨ 3. Tính năng nổi bật

### 3.1. Phân hệ Bán hàng tại quầy (POS & PosMobile)
* **Quản lý đa hóa đơn chờ:** Tạo và lưu trữ đồng thời tối đa 5 hóa đơn chờ, chuyển đổi tức thì không giật lag.
* **Tìm kiếm & Quét mã nhanh:** Tìm kiếm biến thể sản phẩm theo Tên, Mã biến thể, Màu sắc, Kích cỡ, hoặc quét trực tiếp Barcode/QR qua camera thiết bị.
* **Hệ thống Gợi ý Voucher thông minh (Smart Voucher Suggestion):**
  - Tự động phát hiện và áp dụng Voucher mang lại giá trị giảm giá cao nhất (*Voucher tốt nhất*).
  - Hiển thị thanh gợi ý hạn mức tiếp theo (*"Mua thêm X để được giảm thêm Y"*).
* **Phương thức thanh toán đa dạng & Linh hoạt:**
  - Tiền mặt (tự động tính tiền thừa trả khách).
  - Chuyển khoản ngân hàng (tự động bật Modal VietQR kèm số tiền và đồng hồ đếm ngược 5 phút).
  - **Thanh toán kết hợp (Tiền mặt + Chuyển khoản):** Tách linh hoạt số tiền trả mặt và chuyển khoản cho cùng một đơn hàng.
* **Đồng bộ hóa 2 chiều Realtime:** Mọi thay đổi tại màn hình Admin được phản ánh tức thì lên màn hình PosMobile/Tablet của khách hàng hoặc nhân viên đứng quầy.
* **In hóa đơn & Xuất PDF:** Xuất hóa đơn bán lẻ chuẩn định dạng POS nhiệt (80mm) và tải file PDF.

### 3.2. Phân hệ Khách hàng (E-Commerce Storefront)
* **Trang chủ hiện đại:** Banner động, danh mục sản phẩm nổi bật, sản phẩm bán chạy, sản phẩm giảm giá.
* **Bộ lọc sản phẩm đa tiêu chí:** Lọc đồng thời theo Thương hiệu, Danh mục, Khoảng giá, Kích cỡ, Màu sắc, Chất liệu, v.v.
* **Giỏ hàng & Đặt hàng trực tuyến:** Tính phí vận chuyển tự động qua GHN, nhập voucher khuyến mãi, thanh toán COD hoặc VNPay.
* **Tra cứu & Theo dõi đơn hàng:** Xem dòng thời gian trạng thái đơn hàng (Chờ xác nhận -> Đã đóng gói -> Đang giao -> Đã giao -> Hoàn tất).
* **Đánh giá sản phẩm:** Đánh giá sao, tải ảnh/video phản hồi thực tế.

### 3.3. Phân hệ Quản trị (Admin Management)
* **Quản lý Sản phẩm & Biến thể:** Quản lý thuộc tính động (Thương hiệu, Loại giày, Cổ giày, Đế giày, Chất liệu, Màu sắc, Kích cỡ, Trọng lượng, Đệm). Upload và quản lý thư viện ảnh biến thể.
* **Quản lý Hóa đơn:** Chi tiết lịch sử đơn hàng, cập nhật thông tin vận chuyển, in phiếu giao hàng, xử lý đổi trả/hủy đơn.
* **Quản lý Khuyến mãi:** Tạo phiếu giảm giá (Voucher) theo phần trăm/tiền mặt, đợt giảm giá theo khung giờ với bộ đếm ngược.
* **Quản lý Giao ca & Ca làm việc:** Mở ca, đóng ca, kiểm kê tiền mặt đầu ca/cuối ca, tính tiền chênh lệch, bàn giao ca an toàn.
* **Quản lý Lịch làm việc & Phân ca:** Xếp lịch làm việc cho nhân viên, quản trị chấm công.
* **Thống kê & Báo cáo:** Biểu đồ doanh thu theo ngày/tháng/năm, top sản phẩm bán chạy, tỷ lệ phương thức thanh toán, xuất báo cáo Excel.

### 3.4. Trợ lý ảo AI & Tích hợp bên thứ ba
* **Chatbot AI tư vấn thông minh:** Hỗ trợ giải đáp thắc mắc về size giày, gợi ý mẫu giày phù hợp theo nhu cầu (chạy bộ, bóng rổ, thời trang) và tra cứu tình trạng đơn hàng tự động.
* **Kiến trúc Multi-Provider:** Tự động chuyển đổi giữa các Cloud AI (Gemini, OpenAI, DeepSeek, Groq) và Local LLM (Ollama) khi mất kết nối mạng.

---

## 🚀 4. Hướng dẫn cài đặt & Chạy cục bộ (Local Development)

### 4.1. Yêu cầu môi trường
* **Java Development Kit (JDK):** Phiên bản 17 trở lên.
* **Node.js:** Phiên bản 18.x hoặc 20.x (kèm npm).
* **Database:** Microsoft SQL Server 2019/2022 hoặc Docker Azure SQL Edge.
* **Build tool:** Maven 3.8+.

---

### 4.2. Khởi tạo Cơ sở dữ liệu
1. Mở **SQL Server Management Studio (SSMS)** hoặc **Azure Data Studio**.
2. Tạo cơ sở dữ liệu mới:
```sql
CREATE DATABASE giay;
GO
```
3. Chạy lần lượt các script trong thư mục `database/`:
   - `01_schema.sql`
   - `02_data.sql`

---

### 4.3. Chạy Backend (Spring Boot)
1. Di chuyển vào thư mục backend:
```bash
cd BE/server
```
2. Cấu hình thông tin kết nối CSDL trong file `src/main/resources/application.properties` (hoặc tạo file `application-local.properties`):
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=giay;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourPassword123!
```
3. Biên dịch và khởi chạy server:
```bash
mvn clean spring-boot:run
```
*Backend sẽ khởi chạy tại:* `http://localhost:8080`

---

### 4.4. Chạy Frontend Web (Vue 3)
1. Di chuyển vào thư mục frontend:
```bash
cd FE/sport-shoe
```
2. Cài đặt các gói phụ thuộc:
```bash
npm install
```
3. Khởi chạy môi trường phát triển (Dev Server):
```bash
npm run dev
```
*Giao diện Web sẽ khởi chạy tại:* `http://localhost:5173`

---

### 4.5. Chạy PosMobile (Expo / React Native)
1. Di chuyển vào thư mục PosMobile:
```bash
cd PosMobile
```
2. Cài đặt thư viện:
```bash
npm install
```
3. Chạy trên nền tảng Web hoặc thiết bị di động:
   - **Chạy bản Web:** `npm run web` (Mở tại `http://localhost:8081`)
   - **Chạy trên điện thoại (Expo Go):** `npm start` rồi quét mã QR bằng ứng dụng Expo Go trên Android/iOS.

---

## 🐳 5. Triển khai bằng Docker (Docker Deployment)

Hệ thống đã được đóng gói hoàn chỉnh bằng **Docker Compose** sẵn sàng triển khai trên VPS / Cloud Server (AWS, DigitalOcean, Azure):

1. Tạo file cấu hình môi trường `.env` từ `.env.example`:
```bash
cp .env.example .env
```
2. Khởi chạy toàn bộ hệ thống (Database, Ollama AI, Backend, Frontend) chỉ với 1 câu lệnh:
```bash
docker compose up -d --build
```
3. Kiểm tra trạng thái các container:
```bash
docker compose ps
```

---

## 🔑 6. Tài khoản dùng thử (Demo Accounts)

| Phân quyền | Tên đăng nhập / Email | Mật khẩu mặc định | Ghi chú |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin@sportshoe.com` / `admin` | `Admin@123` | Toàn quyền quản trị hệ thống |
| **Nhân viên thu ngân (Staff)** | `staff@sportshoe.com` / `nhanvien` | `Staff@123` | Bán hàng tại quầy, giao ca, hóa đơn |
| **Khách hàng (Customer)** | `customer@gmail.com` / `khachhang` | `Customer@123` | Mua sắm, tra cứu đơn hàng |

---

<div align="center">
  <sub>⭐️ Đừng quên để lại một ngôi sao (Star) nếu bạn thấy dự án hữu ích! Made with ❤️ by SportShoe Team.</sub>
</div>
