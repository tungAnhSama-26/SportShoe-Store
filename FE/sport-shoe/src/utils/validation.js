// Các hàm/biểu thức kiểm tra dữ liệu dùng chung cho toàn ứng dụng.

// Số điện thoại di động Việt Nam: bắt đầu bằng 0 hoặc +84,
// đầu số 3/5/7/8/9, tổng cộng 10 chữ số (sau +84 là 9 chữ số).
export const VN_PHONE_REGEX = /^(0|\+84)[35789]\d{8}$/;

// Email cơ bản: có ký tự trước @, sau @ và phần đuôi .xxx
export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function isValidVnPhone(value) {
  return VN_PHONE_REGEX.test(String(value ?? "").trim());
}

export function isValidEmail(value) {
  return EMAIL_REGEX.test(String(value ?? "").trim());
}

/**
 * Kiểm tra ngày sinh hợp lệ.
 * @returns {string} chuỗi lỗi (rỗng nếu hợp lệ).
 */
export function validateNgaySinh(value) {
  const raw = String(value ?? "").trim();
  if (!raw) return "";
  const ngay = new Date(raw);
  if (Number.isNaN(ngay.getTime())) return "Ngày sinh không hợp lệ.";

  const homNay = new Date();
  homNay.setHours(0, 0, 0, 0);
  if (ngay.getTime() > homNay.getTime()) return "Ngày sinh không được ở tương lai.";

  const gioiHanTuoi = new Date();
  gioiHanTuoi.setFullYear(gioiHanTuoi.getFullYear() - 120);
  if (ngay.getTime() < gioiHanTuoi.getTime()) return "Ngày sinh không hợp lệ.";

  return "";
}
