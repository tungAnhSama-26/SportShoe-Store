const danhMucGiay = [
  {
    ten: "Gi\xE0y ch\u1EA1y b\u1ED9",
    moTa: "Ph\u1ED1i m\xE0u hi\u1EC7n \u0111\u1EA1i, d\u1EC5 mang v\xE0 h\u1EE3p nhi\u1EC1u ho\xE0n c\u1EA3nh t\u1EEB s\xE1ng \u0111\u1EBFn t\u1ED1i.",
    hinhAnh: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80"
  },
  {
    ten: "Gi\xE0y t\u1EADp gym",
    moTa: "Gi\xFAp b\u1EA1n gi\u1EEF \u0111\u1ED9 \u1ED5n \u0111\u1ECBnh khi v\u1EADn \u0111\u1ED9ng v\xE0 v\u1EABn nh\u1EB9 ch\xE2n su\u1ED1t bu\u1ED5i t\u1EADp.",
    hinhAnh: "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=900&q=80"
  },
  {
    ten: "Gi\xE0y du l\u1ECBch",
    moTa: "T\u1ED1i \u01B0u cho di chuy\u1EC3n d\xE0i, \xEAm \u0111\u1EBF v\xE0 d\u1EC5 ph\u1ED1i v\u1EDBi trang ph\u1EE5c \u0111\u01A1n gi\u1EA3n.",
    hinhAnh: "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=900&q=80&sat=-100"
  },
  {
    ten: "Gi\xE0y ph\u1ED1i \u0111\u1ED3",
    moTa: "Nhi\u1EC1u l\u1EF1a ch\u1ECDn d\xE1ng th\u1EA5p \u0111\u1EC3 d\u1EC5 k\u1EBFt h\u1EE3p v\u1EDBi phong c\xE1ch \u0111\u01B0\u1EDDng ph\u1ED1.",
    hinhAnh: "https://images.unsplash.com/photo-1520639888713-7851133b1ed0?auto=format&fit=crop&w=900&q=80"
  }
];
const sanPhamNoiBat = [
  {
    ten: "Air Max Ch\u1EA1y B\u1ED9",
    moTa: "M\u1EABu sneaker ch\u1EA1y b\u1ED9 n\u1ED5i b\u1EADt v\u1EDBi \u0111\u1EC7m \xEAm v\xE0 ph\u1EA7n th\xE2n gi\xE0y si\xEAu nh\u1EB9.",
    gia: 359e4,
    giaCu: 41e5,
    nhan: "Gi\u1EA3m gi\xE1",
    soMau: "4 m\xE0u c\xF3 s\u1EB5n",
    hinhAnh: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80"
  },
  {
    ten: "\xCAm Ph\u1ED1 Th\u1ECB",
    moTa: "Phom th\u1EA5p t\u1ED1i gi\u1EA3n cho \u0111i l\u1EA1i h\u1EB1ng ng\xE0y v\xE0 d\u1EC5 ph\u1ED1i \u0111\u1ED3.",
    gia: 289e4,
    nhan: "M\u1EDBi",
    soMau: "3 m\xE0u c\xF3 s\u1EB5n",
    hinhAnh: "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=900&q=80"
  },
  {
    ten: "Th\u1EC3 Thao Chuy\xEAn Nghi\u1EC7p",
    moTa: "L\u1EF1a ch\u1ECDn t\u1EADp luy\u1EC7n \u1ED5n \u0111\u1ECBnh v\u1EDBi \u0111\u1EBF b\xE1m v\xE0 h\u1ED7 tr\u1EE3 c\u1ED5 ch\xE2n t\u1ED1t.",
    gia: 429e4,
    nhan: "Ph\u1ED5 bi\u1EBFn",
    soMau: "5 m\xE0u c\xF3 s\u1EB5n",
    hinhAnh: "https://images.unsplash.com/photo-1552346154-21d32810aba3?auto=format&fit=crop&w=900&q=80"
  },
  {
    ten: "Sneaker C\u1ED5 \u0110i\u1EC3n",
    moTa: "Phong c\xE1ch c\u1ED5 \u0111i\u1EC3n, g\u1ECDn g\xE0ng v\xE0 d\u1EC5 d\xF9ng trong nhi\u1EC1u t\xECnh hu\u1ED1ng.",
    gia: 219e4,
    nhan: "",
    soMau: "6 m\xE0u c\xF3 s\u1EB5n",
    hinhAnh: "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=900&q=80&sat=-100"
  }
];
const giaTriNoiBat = [
  {
    tieuDe: "Mi\u1EC5n ph\xED v\u1EADn chuy\u1EC3n",
    noiDung: "Cho \u0111\u01A1n h\xE0ng tr\xEAn 2.500.000\u0111",
    bieuTuong: "giao-hang"
  },
  {
    tieuDe: "Thanh to\xE1n an to\xE0n",
    noiDung: "Quy tr\xECnh thanh to\xE1n \u0111\u01B0\u1EE3c b\u1EA3o v\u1EC7 100%",
    bieuTuong: "thanh-toan"
  },
  {
    tieuDe: "\u0110\u1ED5i tr\u1EA3 d\u1EC5 d\xE0ng",
    noiDung: "Ch\xEDnh s\xE1ch \u0111\u1ED5i tr\u1EA3 trong 30 ng\xE0y",
    bieuTuong: "doi-tra"
  },
  {
    tieuDe: "H\u1ED7 tr\u1EE3 24/7",
    noiDung: "Lu\xF4n s\u1EB5n s\xE0ng h\u1ED7 tr\u1EE3 b\u1EA1n",
    bieuTuong: "ho-tro"
  }
];
const thongKeTrangChu = [
  { so: "12k+", nhan: "Kh\xE1ch h\xE0ng h\xE0i l\xF2ng" },
  { so: "48h", nhan: "Giao nhanh to\xE0n qu\u1ED1c" },
  { so: "4.9/5", nhan: "\u0110\xE1nh gi\xE1 trung b\xECnh" }
];
export {
  danhMucGiay,
  giaTriNoiBat,
  sanPhamNoiBat,
  thongKeTrangChu
};
