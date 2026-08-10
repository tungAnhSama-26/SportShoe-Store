export const DIA_CHI_RONG = Object.freeze({
  tinhThanhCode: "",
  tinhThanh: "",
  phuongXaCode: "",
  phuongXa: "",
  diaChiCuThe: "",
});

export function chuanHoaDiaChi(value) {
  const source = value?.diaChi ?? value?.diaChiGiaoHang ?? value ?? {};
  return {
    tinhThanhCode: String(source.tinhThanhCode ?? ""),
    tinhThanh: source.tinhThanh ?? "",
    phuongXaCode: String(source.phuongXaCode ?? ""),
    phuongXa: source.phuongXa ?? "",
    diaChiCuThe: source.diaChiCuThe ?? "",
  };
}

export function taoPayloadDiaChi(value) {
  const address = chuanHoaDiaChi(value);
  return Object.fromEntries(Object.entries(address).map(([key, item]) => [key, String(item).trim()]));
}

export function dinhDangDiaChi(value) {
  const address = chuanHoaDiaChi(value);
  return [address.diaChiCuThe, address.phuongXa, address.tinhThanh].filter(Boolean).join(", ");
}

export function diaChiHopLe(value) {
  const address = chuanHoaDiaChi(value);
  return Boolean(address.tinhThanh && address.phuongXa && address.diaChiCuThe);
}

export function layMaDonViDiaChi(value) {
  return String(value?.code ?? value?.id ?? value?.value ?? "").trim();
}

export function chuanHoaTenDonViDiaChi(value, boTienTo = false) {
  const normalized = String(value ?? "").trim().toLocaleLowerCase("vi-VN")
    .normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/đ/g, "d")
    .replace(/[^a-z0-9\s]/g, " ").replace(/\s+/g, " ").trim();
  if (!boTienTo) return normalized;
  return normalized.replace(/^(?:thanh pho|thi xa|thi tran|dac khu|tinh|phuong|xa|tp)\s+/, "").trim();
}

export function timDonViDiaChi(danhSach, code, ten) {
  const items = Array.isArray(danhSach) ? danhSach : [];
  const normalizedCode = String(code ?? "").trim();
  if (normalizedCode) {
    const byCode = items.find((item) => layMaDonViDiaChi(item) === normalizedCode);
    if (byCode) return byCode;
  }
  const normalizedName = chuanHoaTenDonViDiaChi(ten);
  if (!normalizedName) return null;
  const exact = items.filter((item) => chuanHoaTenDonViDiaChi(item?.ten ?? item?.label) === normalizedName);
  if (exact.length === 1) return exact[0];
  const withoutPrefix = chuanHoaTenDonViDiaChi(ten, true);
  const matches = items.filter((item) => chuanHoaTenDonViDiaChi(item?.ten ?? item?.label, true) === withoutPrefix);
  return matches.length === 1 ? matches[0] : null;
}

export async function doiChieuDiaChiHaiCap(value, danhSachTinh, layDanhSachPhuongXa) {
  const diaChi = chuanHoaDiaChi(value);
  const tinh = timDonViDiaChi(danhSachTinh, diaChi.tinhThanhCode, diaChi.tinhThanh);
  if (!tinh) {
    return { diaChi: { ...diaChi, tinhThanhCode: "", phuongXaCode: "" }, tinh: null, phuongXa: null, danhSachPhuongXa: [] };
  }
  const tinhThanhCode = layMaDonViDiaChi(tinh);
  const loaded = await layDanhSachPhuongXa(tinhThanhCode);
  const danhSachPhuongXa = Array.isArray(loaded) ? loaded : [];
  const phuongXa = timDonViDiaChi(danhSachPhuongXa, diaChi.phuongXaCode, diaChi.phuongXa);
  return {
    diaChi: {
      ...diaChi,
      tinhThanhCode,
      tinhThanh: tinh.ten ?? tinh.label ?? diaChi.tinhThanh,
      phuongXaCode: phuongXa ? layMaDonViDiaChi(phuongXa) : "",
      phuongXa: phuongXa?.ten ?? phuongXa?.label ?? diaChi.phuongXa,
    },
    tinh,
    phuongXa,
    danhSachPhuongXa,
  };
}
