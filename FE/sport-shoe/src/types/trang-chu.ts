export interface DanhMucGiay {
  ten: string;
  moTa: string;
  hinhAnh: string;
}

export interface SanPhamNoiBat {
  ten: string;
  moTa: string;
  gia: number;
  nhan: string;
  hinhAnh: string;
  soMau: string;
  giaCu?: number;
}

export interface GiaTriNoiBat {
  tieuDe: string;
  noiDung: string;
  bieuTuong: "giao-hang" | "thanh-toan" | "doi-tra" | "ho-tro";
}

export interface ThongKeTrangChu {
  so: string;
  nhan: string;
}
