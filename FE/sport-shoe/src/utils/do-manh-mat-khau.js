// Đánh giá độ mạnh mật khẩu -> { level, nhan, textClass, barClass, phanTram } hoặc null nếu rỗng.
// Đỏ = yếu, Vàng = trung bình, Xanh lá = mạnh.
export function danhGiaMatKhau(matKhau) {
  const pw = String(matKhau || '');
  if (!pw) return null;

  let diem = 0;
  if (pw.length >= 6) diem += 1;
  if (pw.length >= 10) diem += 1;
  if (/[a-z]/.test(pw)) diem += 1;
  if (/[A-Z]/.test(pw)) diem += 1;
  if (/\d/.test(pw)) diem += 1;
  if (/[^A-Za-z0-9]/.test(pw)) diem += 1;

  // Mật khẩu dưới 6 ký tự hoặc quá đơn giản -> yếu.
  if (pw.length < 6 || diem <= 2) {
    return { level: 'yeu', nhan: 'Yếu', textClass: 'text-red-500', barClass: 'bg-red-500', phanTram: 33 };
  }
  if (diem <= 4) {
    return { level: 'trung-binh', nhan: 'Trung bình', textClass: 'text-yellow-500', barClass: 'bg-yellow-400', phanTram: 66 };
  }
  return { level: 'manh', nhan: 'Mạnh', textClass: 'text-green-600', barClass: 'bg-green-500', phanTram: 100 };
}
