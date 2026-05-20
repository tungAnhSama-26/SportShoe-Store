import { MAX_PAYMENT_DIGITS } from "./constants";

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
  return String(value ?? "").replace(/[^\d]/g, "").slice(0, MAX_PAYMENT_DIGITS);
}

export function dinhDangTienNhap(value) {
  const digits = layChuSoTien(value);
  return digits ? dinhDangSo(Number(digits)) : "";
}
