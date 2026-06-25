import { TOI_DA_CHU_SO_THANH_TOAN } from "./HangSo";

export function dinhDangTien(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
  }).format(value || 0);
}

export function dinhDangSo(value) {
  return new Intl.NumberFormat("vi-VN", {
    maximumFractionDigits: 0
  }).format(value || 0);
}

export function layChuSoTien(value) {
  return String(value ?? "").replace(/[^\d]/g, "").slice(0, TOI_DA_CHU_SO_THANH_TOAN);
}

export function dinhDangTienNhap(value) {
  const digits = layChuSoTien(value);
  return digits ? dinhDangSo(Number(digits)) : "";
}
