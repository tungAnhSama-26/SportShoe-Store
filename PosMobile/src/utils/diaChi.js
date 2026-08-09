export const DIA_CHI_RONG = { tinhThanhCode: '', tinhThanh: '', phuongXaCode: '', phuongXa: '', diaChiCuThe: '' };

export function chuanHoaDiaChi(value) {
  const source = value?.diaChi ?? value?.diaChiGiaoHang ?? value ?? {};
  if (typeof source === 'string') return { ...DIA_CHI_RONG, diaChiCuThe: source };
  return {
    tinhThanhCode: String(source.tinhThanhCode ?? ''), tinhThanh: source.tinhThanh ?? '',
    phuongXaCode: String(source.phuongXaCode ?? ''), phuongXa: source.phuongXa ?? '',
    diaChiCuThe: source.diaChiCuThe ?? '',
  };
}

export const dinhDangDiaChi = (value) => {
  const item = chuanHoaDiaChi(value);
  return [item.diaChiCuThe, item.phuongXa, item.tinhThanh].filter(Boolean).join(', ');
};

export const diaChiHopLe = (value) => {
  const item = chuanHoaDiaChi(value);
  return Boolean(item.tinhThanh && item.phuongXa && item.diaChiCuThe);
};

export const layMaDonViDiaChi = (value) => String(value?.code ?? value?.id ?? value?.value ?? '').trim();

function chuanHoaTenDonVi(value, boTienTo = false) {
  const normalized = String(value ?? '').trim().toLocaleLowerCase('vi-VN')
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd')
    .replace(/[^a-z0-9\s]/g, ' ').replace(/\s+/g, ' ').trim();
  if (!boTienTo) return normalized;
  return normalized.replace(/^(?:thanh pho|thi xa|thi tran|dac khu|tinh|phuong|xa|tp)\s+/, '').trim();
}

export function timDonViDiaChi(danhSach, code, ten) {
  const items = Array.isArray(danhSach) ? danhSach : [];
  const normalizedCode = String(code ?? '').trim();
  if (normalizedCode) {
    const byCode = items.find((item) => layMaDonViDiaChi(item) === normalizedCode);
    if (byCode) return byCode;
  }
  const normalizedName = chuanHoaTenDonVi(ten);
  if (!normalizedName) return null;
  const exact = items.filter((item) => chuanHoaTenDonVi(item?.ten ?? item?.label) === normalizedName);
  if (exact.length === 1) return exact[0];
  const withoutPrefix = chuanHoaTenDonVi(ten, true);
  const matches = items.filter((item) => chuanHoaTenDonVi(item?.ten ?? item?.label, true) === withoutPrefix);
  return matches.length === 1 ? matches[0] : null;
}
