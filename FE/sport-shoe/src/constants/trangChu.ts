import type {
  DanhMucGiay,
  GiaTriNoiBat,
  SanPhamNoiBat,
  ThongKeTrangChu,
} from "../types/trang-chu";

export const danhMucGiay: DanhMucGiay[] = [
  {
    ten: "Giày chạy bộ",
    moTa: "Phối màu hiện đại, dễ mang và hợp nhiều hoàn cảnh từ sáng đến tối.",
    hinhAnh:
      "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80",
  },
  {
    ten: "Giày tập gym",
    moTa: "Giúp bạn giữ độ ổn định khi vận động và vẫn nhẹ chân suốt buổi tập.",
    hinhAnh:
      "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=900&q=80",
  },
  {
    ten: "Giày du lịch",
    moTa: "Tối ưu cho di chuyển dài, êm đế và dễ phối với trang phục đơn giản.",
    hinhAnh:
      "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=900&q=80&sat=-100",
  },
  {
    ten: "Giày phối đồ",
    moTa: "Nhiều lựa chọn dáng thấp để dễ kết hợp với phong cách đường phố.",
    hinhAnh:
      "https://images.unsplash.com/photo-1520639888713-7851133b1ed0?auto=format&fit=crop&w=900&q=80",
  },
];

export const sanPhamNoiBat: SanPhamNoiBat[] = [
  {
    ten: "Air Max Chạy Bộ",
    moTa: "Mẫu sneaker chạy bộ nổi bật với đệm êm và phần thân giày siêu nhẹ.",
    gia: 3590000,
    giaCu: 4100000,
    nhan: "Giảm giá",
    soMau: "4 màu có sẵn",
    hinhAnh:
      "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80",
  },
  {
    ten: "Êm Phố Thị",
    moTa: "Phom thấp tối giản cho đi lại hằng ngày và dễ phối đồ.",
    gia: 2890000,
    nhan: "Mới",
    soMau: "3 màu có sẵn",
    hinhAnh:
      "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=900&q=80",
  },
  {
    ten: "Thể Thao Chuyên Nghiệp",
    moTa: "Lựa chọn tập luyện ổn định với đế bám và hỗ trợ cổ chân tốt.",
    gia: 4290000,
    nhan: "Phổ biến",
    soMau: "5 màu có sẵn",
    hinhAnh:
      "https://images.unsplash.com/photo-1552346154-21d32810aba3?auto=format&fit=crop&w=900&q=80",
  },
  {
    ten: "Sneaker Cổ Điển",
    moTa: "Phong cách cổ điển, gọn gàng và dễ dùng trong nhiều tình huống.",
    gia: 2190000,
    nhan: "",
    soMau: "6 màu có sẵn",
    hinhAnh:
      "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=900&q=80&sat=-100",
  },
];

export const giaTriNoiBat: GiaTriNoiBat[] = [
  {
    tieuDe: "Miễn phí vận chuyển",
    noiDung: "Cho đơn hàng trên 2.500.000đ",
    bieuTuong: "giao-hang",
  },
  {
    tieuDe: "Thanh toán an toàn",
    noiDung: "Quy trình thanh toán được bảo vệ 100%",
    bieuTuong: "thanh-toan",
  },
  {
    tieuDe: "Đổi trả dễ dàng",
    noiDung: "Chính sách đổi trả trong 30 ngày",
    bieuTuong: "doi-tra",
  },
  {
    tieuDe: "Hỗ trợ 24/7",
    noiDung: "Luôn sẵn sàng hỗ trợ bạn",
    bieuTuong: "ho-tro",
  },
];

export const thongKeTrangChu: ThongKeTrangChu[] = [
  { so: "12k+", nhan: "Khách hàng hài lòng" },
  { so: "48h", nhan: "Giao nhanh toàn quốc" },
  { so: "4.9/5", nhan: "Đánh giá trung bình" },
];
