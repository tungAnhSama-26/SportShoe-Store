const DANH_SACH_SHIPPER_GHN = [
  { ma: "GHN-0186", hoTen: "Nguyễn Minh Anh", soDienThoai: "0901234186" },
  { ma: "GHN-0247", hoTen: "Trần Quốc Bảo", soDienThoai: "0912746247" },
  { ma: "GHN-0315", hoTen: "Lê Hoàng Nam", soDienThoai: "0936158315" },
  { ma: "GHN-0428", hoTen: "Phạm Đức Long", soDienThoai: "0978421428" },
  { ma: "GHN-0539", hoTen: "Vũ Thành Công", soDienThoai: "0985392539" },
];

function bamChuoi(value) {
  return Array.from(String(value || "GHN")).reduce(
    (hash, char) => (hash * 31 + char.charCodeAt(0)) >>> 0,
    0,
  );
}

export function layShipperGhnTheoHoaDon(hoaDon) {
  if (!hoaDon) return null;

  const khoa = hoaDon.ma || hoaDon.maHoaDon || hoaDon.id || hoaDon.hoaDonId;
  const shipper = DANH_SACH_SHIPPER_GHN[bamChuoi(khoa) % DANH_SACH_SHIPPER_GHN.length];

  return {
    ...shipper,
    donVi: "GHN Express",
  };
}

export function laTrangThaiCoShipperGhn(value) {
  const trangThai = String(value || "")
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase()
    .replace(/đ/g, "d")
    .replace(/[_-]+/g, " ")
    .replace(/\s+/g, " ")
    .trim();

  return [
    "cho lay hang",
    "cho giao hang",
    "dang giao hang",
    "dang van chuyen",
    "cho van chuyen",
    "da giao hang",
    "giao hang that bai",
    "hoan thanh",
  ].includes(trangThai);
}

export function hoaDonDaCoShipperGhn(hoaDon) {
  if (!hoaDon) return false;

  const maTrangThai = Number(hoaDon.trangThai);
  if ([2, 3, 4, 5, 10].includes(maTrangThai)) return true;

  if (
    laTrangThaiCoShipperGhn(hoaDon.trangThai) ||
    laTrangThaiCoShipperGhn(hoaDon.trangThaiText)
  ) {
    return true;
  }

  const lichSu = [
    ...(Array.isArray(hoaDon.lichSuHoaDon) ? hoaDon.lichSuHoaDon : []),
    ...(Array.isArray(hoaDon.lichSuTrangThai) ? hoaDon.lichSuTrangThai : []),
  ];

  return lichSu.some((item) =>
    laTrangThaiCoShipperGhn(item?.trangThai ?? item?.trangThaiText),
  );
}
