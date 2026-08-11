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

function cheSoDienThoai(value) {
  const soDienThoai = String(value || "");
  if (soDienThoai.length < 7) return soDienThoai;
  return `${soDienThoai.slice(0, 4)} *** ${soDienThoai.slice(-3)}`;
}

export function layShipperGhnTheoHoaDon(hoaDon) {
  if (!hoaDon) return null;

  const khoa = hoaDon.ma || hoaDon.maHoaDon || hoaDon.id || hoaDon.hoaDonId;
  const shipper = DANH_SACH_SHIPPER_GHN[bamChuoi(khoa) % DANH_SACH_SHIPPER_GHN.length];

  return {
    ...shipper,
    soDienThoaiHienThi: cheSoDienThoai(shipper.soDienThoai),
    donVi: "GHN Express",
  };
}

export function laTrangThaiCoShipperGhn(value) {
  const trangThai = String(value || "")
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase()
    .replace(/[_-]+/g, " ")
    .replace(/\s+/g, " ")
    .trim();

  return [
    "cho lay hang",
    "cho giao hang",
    "dang giao hang",
    "dang van chuyen",
    "cho van chuyen",
  ].includes(trangThai);
}
