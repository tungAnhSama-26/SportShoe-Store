// Các hàm/biểu thức kiểm tra dữ liệu dùng chung cho toàn ứng dụng.

// Số điện thoại di động Việt Nam: bắt đầu bằng 03/05/07/08/09 và có đúng 10 chữ số.
export const VN_PHONE_REGEX = /^0[35789]\d{8}$/;

// Email cơ bản: có ký tự trước @, sau @ và phần đuôi .xxx
export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const FULL_NAME_REGEX = /^\p{L}+(?: \p{L}+)*$/u;
export const ADDRESS_REGEX = /^[\p{L}0-9]+(?:[ \-.,/]+[\p{L}0-9]+)*$/u;

export function isValidVnPhone(value) {
  return VN_PHONE_REGEX.test(String(value ?? "").trim());
}

export function isValidEmail(value) {
  return EMAIL_REGEX.test(String(value ?? "").trim());
}

export function validateFullName(value, label = "Họ tên") {
  const raw = String(value ?? "");
  const trimmed = raw.trim();

  if (!trimmed) return `${label} không được để trống.`;
  if (raw !== trimmed) return `${label} không được có khoảng trắng ở đầu hoặc cuối.`;
  if (trimmed.length <= 3) return `${label} phải lớn hơn 3 ký tự.`;
  if (trimmed.length >= 100) return `${label} phải nhỏ hơn 100 ký tự.`;
  if (!FULL_NAME_REGEX.test(trimmed)) {
    return `${label} chỉ được chứa chữ cái và khoảng trắng, không chứa ký tự đặc biệt.`;
  }

  return "";
}

export function validateAddress(value, label = "Địa chỉ") {
  const raw = String(value ?? "");
  const trimmed = raw.trim();

  if (!trimmed) return "";
  if (raw !== trimmed) return `${label} không được có khoảng trắng ở đầu hoặc cuối.`;
  if (trimmed.length < 2 || trimmed.length > 70) return `${label} phải từ 2 đến 70 ký tự.`;
  if (!ADDRESS_REGEX.test(trimmed)) {
    return `${label} chỉ được chứa chữ, số, khoảng trắng và các ký tự hợp lệ như -, ., ,, /.`;
  }

  return "";
}

/**
 * Kiểm tra ngày sinh hợp lệ: không ở tương lai và tuổi phải từ 18 đến 100.
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

  const tuoi = tinhTuoi(ngay, homNay);
  if (tuoi < 18) return "Khách hàng phải đủ 18 tuổi.";
  if (tuoi > 100) return "Tuổi không hợp lệ (tối đa 100 tuổi).";

  return "";
}

/** Tuổi tròn tính đến hôm nay (trừ 1 nếu chưa tới sinh nhật năm nay). */
function tinhTuoi(ngaySinh, homNay) {
  let tuoi = homNay.getFullYear() - ngaySinh.getFullYear();
  const chuaToiSinhNhat =
    homNay.getMonth() < ngaySinh.getMonth() ||
    (homNay.getMonth() === ngaySinh.getMonth() && homNay.getDate() < ngaySinh.getDate());
  if (chuaToiSinhNhat) tuoi -= 1;
  return tuoi;
}
