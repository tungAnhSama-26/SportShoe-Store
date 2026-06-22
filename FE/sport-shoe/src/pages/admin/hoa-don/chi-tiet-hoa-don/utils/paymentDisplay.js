function chuanHoaThanhToanText(value) {
  return String(value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase()
    .trim();
}

function laGiaoDichHoanTien(thanhToan) {
  const loaiGiaoDich = chuanHoaThanhToanText(thanhToan?.loaiGiaoDich);
  const ghiChu = chuanHoaThanhToanText(thanhToan?.ghiChu);
  return (
    loaiGiaoDich.includes("hoan tien") ||
    (ghiChu.startsWith("da hoan tien cho khach hang") &&
      !ghiChu.includes("khach hang da thanh toan"))
  );
}

export function nhanTrangThaiThanhToan(thanhToan) {
  const trangThai = chuanHoaThanhToanText(thanhToan?.trangThaiThanhToan);
  if (
    (trangThai === "can hoan tien" || trangThai === "da hoan tien") &&
    !laGiaoDichHoanTien(thanhToan)
  ) {
    return "Đã thanh toán";
  }
  return thanhToan?.trangThaiThanhToan || "Chờ thanh toán";
}

export function lopTrangThaiThanhToan(thanhToan) {
  const trangThai = chuanHoaThanhToanText(nhanTrangThaiThanhToan(thanhToan));
  if (trangThai === "da thanh toan") {
    return "bg-emerald-50 text-emerald-600";
  }
  if (trangThai === "can hoan tien") {
    return "bg-amber-50 text-amber-600";
  }
  if (trangThai === "da hoan tien") {
    return "bg-violet-50 text-violet-600";
  }
  if (trangThai === "da huy" || trangThai === "thanh toan that bai") {
    return "bg-rose-50 text-rose-600";
  }
  return "bg-slate-100 text-slate-500";
}
