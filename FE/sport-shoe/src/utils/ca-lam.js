export function chuyenGioThanhPhut(gio) {
  if (!gio || !String(gio).includes(':')) return null;
  const [hourRaw, minuteRaw] = String(gio).split(':');
  const hour = Number(hourRaw);
  const minute = Number(minuteRaw);
  if (!Number.isInteger(hour) || !Number.isInteger(minute)) return null;
  if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;
  return hour * 60 + minute;
}

export function chuyenPhutThanhGio(phut) {
  if (!Number.isInteger(phut)) return '';
  const phutTrongNgay = ((phut % 1440) + 1440) % 1440;
  const hour = Math.floor(phutTrongNgay / 60);
  const minute = phutTrongNgay % 60;
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
}

export function tinhThoiLuongCa(gioBatDau, gioKetThuc) {
  const batDau = chuyenGioThanhPhut(gioBatDau);
  const ketThuc = chuyenGioThanhPhut(gioKetThuc);
  if (batDau === null || ketThuc === null || batDau === ketThuc) return null;
  return ketThuc > batDau ? ketThuc - batDau : 1440 - batDau + ketThuc;
}

function tachKhoangGioTheoNgay(gioBatDau, gioKetThuc) {
  const batDau = chuyenGioThanhPhut(gioBatDau);
  const ketThuc = chuyenGioThanhPhut(gioKetThuc);
  if (batDau === null || ketThuc === null || batDau === ketThuc) return [];
  if (batDau < ketThuc) return [{ batDau, ketThuc }];
  return [
    { batDau, ketThuc: 1440 },
    { batDau: 0, ketThuc }
  ];
}

export function khoangGioGiaoNhau(gioBatDauA, gioKetThucA, gioBatDauB, gioKetThucB) {
  const khoangA = tachKhoangGioTheoNgay(gioBatDauA, gioKetThucA);
  const khoangB = tachKhoangGioTheoNgay(gioBatDauB, gioKetThucB);
  return khoangA.some(a => khoangB.some(b => a.batDau < b.ketThuc && b.batDau < a.ketThuc));
}

const GIO_BAT_DAU_MAC_DINH = '08:00';
const THOI_LUONG_MAC_DINH_PHUT = 4 * 60;

function taoGoiYMacDinh() {
  const batDauMacDinh = chuyenGioThanhPhut(GIO_BAT_DAU_MAC_DINH);
  return {
    tuCa: null,
    gioBatDau: GIO_BAT_DAU_MAC_DINH,
    gioKetThuc: chuyenPhutThanhGio(batDauMacDinh + THOI_LUONG_MAC_DINH_PHUT)
  };
}

export function taoGoiYCaTiepTheo(danhSachCaLam) {
  const cacCaHopLe = (Array.isArray(danhSachCaLam) ? danhSachCaLam : [])
    .filter(ca => ca.trangThai && ca.gioBatDau && ca.gioKetThuc)
    .map(ca => {
      const batDau = chuyenGioThanhPhut(ca.gioBatDau);
      const ketThuc = chuyenGioThanhPhut(ca.gioKetThuc);
      const thoiLuong = tinhThoiLuongCa(ca.gioBatDau, ca.gioKetThuc);
      if (batDau === null || ketThuc === null || thoiLuong === null) return null;
      const ketThucTuyenTinh = ketThuc > batDau ? ketThuc : ketThuc + 1440;
      return { ...ca, batDau, ketThuc, ketThucTuyenTinh, thoiLuong };
    })
    .filter(Boolean)
    .sort((a, b) => b.ketThucTuyenTinh - a.ketThucTuyenTinh || b.batDau - a.batDau);

  const caTruocDo = cacCaHopLe[0];
  if (!caTruocDo) return taoGoiYMacDinh();

  const batDauGoiY = caTruocDo.ketThucTuyenTinh;
  const ketThucGoiY = batDauGoiY + caTruocDo.thoiLuong;
  if (ketThucGoiY <= batDauGoiY) return taoGoiYMacDinh();

  return {
    tuCa: caTruocDo,
    gioBatDau: chuyenPhutThanhGio(batDauGoiY),
    gioKetThuc: chuyenPhutThanhGio(ketThucGoiY)
  };
}
