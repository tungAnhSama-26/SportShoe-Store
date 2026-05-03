<script setup lang="ts">
import { computed, onMounted, ref, watch, markRaw } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowLeft,
  Banknote,
  CheckCircle2,
  CircleCheck,
  CircleX,
  ClipboardList,
  ClipboardCheck,
  Flag,
  History,
  Hourglass,
  MapPin,
  Package,
  Pencil,
  Printer,
  Search,
  Trash2,
  TriangleAlert,
  Truck,
  User,
  X,
} from "lucide-vue-next";
import { capNhatSanPhamHoaDon, capNhatTrangThaiHoaDon, layChiTietHoaDon, tinhPhiVanChuyenGhn } from "../../../services/hoa-don";
import { timSanPhamTaiQuay, type SanPhamTaiQuay } from "../../../services/ban-hang-tai-quay";
import { printInvoiceToPdf } from "../../../utils/invoice-pdf";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import logoGhn from "../../../constants/logoGhn";

const route = useRoute();
const router = useRouter();
const hoaDon = ref<any>(null);
const dangTai = ref(false);
const loiTrang = ref("");
const dangCapNhat = ref(false);
const toast = ref({
  hienThi: false,
  loai: "success" as "success" | "warning" | "error",
  tieuDe: "",
  noiDung: "",
});
let toastTimer: ReturnType<typeof setTimeout> | null = null;

const hienModalXacNhan = ref(false);
const hienModalLichSu = ref(false);
const hienModalSanPham = ref(false);
const hienModalXacNhanHuy = ref(false);

const hienModalThongTin = ref(false);
const tabHienTai = ref("donHang");
const formThongTin = ref({
  trangThai: "",
  tenKhachHang: "",
  soDienThoai: "",
  email: "",
  diaChi: "",
  loaiDon: ""
});
const formGhn = ref({
  serviceTypeId: 2,
  length: 30,
  width: 20,
  height: 12,
  weight: 500,
});
const dangTinhPhiGhn = ref(false);
const diaChiGhnDaDo = ref("");

const trangThaiMoiXacNhan = ref<string | null>(null);
const ghiChuXacNhan = ref("");

const tuKhoaSanPham = ref("");
const ketQuaTimKiem = ref<SanPhamTaiQuay[]>([]);
const dangTimKiem = ref(false);
const danhSachSanPhamUpdate = ref<
  Array<{ chiTietId: number; tenSanPham: string; soLuong: number; giaBan: number; maBienThe: string }>
>([]);

const cacBuocCoDinh = [
  { id: 1, key: "Chờ xác nhận", ten: "Chờ Xác Nhận", icon: markRaw(Hourglass) },
  { id: 2, key: "Đã xác nhận", ten: "Đã Xác Nhận", icon: markRaw(ClipboardCheck) },
  { id: 3, key: "Chờ lấy hàng", ten: "Chờ Lấy Hàng", icon: markRaw(Package) },
  { id: 4, key: "Chờ giao hàng", ten: "Chờ Giao Hàng", icon: markRaw(Truck) },
  { id: 5, key: "Đã giao hàng", ten: "Đã Giao Hàng", icon: markRaw(CircleCheck) },
  { id: 6, key: "Hoàn thành", ten: "Hoàn Thành", icon: markRaw(Flag) },
];

const cacBuocYeuCauHuy = [
  { id: 1, key: "Chờ xác nhận", ten: "Chờ Xác Nhận", icon: markRaw(Hourglass) },
  { id: 7, key: "Yêu cầu hủy", ten: "Yêu Cầu Hủy", icon: markRaw(TriangleAlert) },
];

const cacBuocDaHuy = [
  { id: 1, key: "Chờ xác nhận", ten: "Chờ Xác Nhận", icon: markRaw(Hourglass) },
  { id: 6, key: "Hủy", ten: "Đã Hủy", icon: markRaw(CircleX) },
];

const laDonTaiQuay = computed(() => {
  return hoaDon.value?.loaiDon === "Tại cửa hàng" || hoaDon.value?.kenhBan === 1;
});

const cacBuoc = computed(() => {
  if (laDonTaiQuay.value) {
    return [
      { id: 1, key: "Chờ xác nhận", ten: "Chờ Xác Nhận", icon: markRaw(Hourglass) },
      { id: 6, key: "Hoàn thành", ten: "Hoàn Thành", icon: markRaw(Flag) },
    ];
  }
  return cacBuocCoDinh;
});

function dinhDangTien(value: number) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value || 0);
}

function dinhDangNgay(ngay?: string) {
  if (!ngay) return "—";
  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(ngay));
}

function dinhDangGio(ngay?: string) {
  if (!ngay) return "--:--:--";
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  }).format(new Date(ngay));
}

function vietHoaChuCaiDau(text?: string) {
  if (!text) return "";
  return text
    .toLowerCase()
    .split(" ")
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}

const buocHienTai = computed(() => {
  if (!hoaDon.value) return 0;
  const stt = (hoaDon.value.trangThai || "").toLowerCase().trim();
  if (stt === "chờ xác nhận" || stt === "cho_xac_nhan") return 1;
  if (stt === "đã xác nhận" || stt === "da_xac_nhan") return 2;
  if (stt === "chờ lấy hàng" || stt === "cho_lay_hang" || stt === "cho_giao_hang") return 3;
  if (stt === "chờ giao hàng" || stt === "đang vận chuyển" || stt === "chờ vận chuyển" || stt === "dang_van_chuyen") return 4;
  if (stt === "đã giao hàng" || stt === "vận chuyển" || stt === "da_giao_hang") return 5;
  if (stt === "hoàn thành" || stt === "đã hoàn thành" || stt === "hoan_thanh") return 6;
  return 0;
});

const donDaHoanThanh = computed(() => {
  const stt = (hoaDon.value?.trangThai || "").toLowerCase().trim();
  return stt === "hoàn thành" || stt === "đã hoàn thành" || stt === "hoan_thanh";
});

const donYeuCauHuy = computed(() => {
  const stt = (hoaDon.value?.trangThai || "").toLowerCase().trim();
  return stt === "yêu cầu hủy" || stt === "yeu_cau_huy";
});

const donDaHuy = computed(() => {
  const stt = (hoaDon.value?.trangThai || "").toLowerCase().trim();
  return stt === "hủy" || stt === "huy" || stt === "đã hủy" || stt === "da_huy";
});

const toastClass = computed(() => {
  if (toast.value.loai === "success") return "border-emerald-100 bg-emerald-50 text-emerald-700";
  if (toast.value.loai === "warning") return "border-amber-100 bg-amber-50 text-amber-700";
  return "border-rose-100 bg-rose-50 text-rose-700";
});

const toastIconClass = computed(() => {
  if (toast.value.loai === "success") return "bg-emerald-100 text-emerald-600";
  if (toast.value.loai === "warning") return "bg-amber-100 text-amber-600";
  return "bg-rose-100 text-rose-600";
});

const toastAccentClass = computed(() => {
  if (toast.value.loai === "success") return "bg-emerald-500";
  if (toast.value.loai === "warning") return "bg-amber-500";
  return "bg-rose-500";
});

const ToastIcon = computed(() => {
  if (toast.value.loai === "success") return CheckCircle2;
  if (toast.value.loai === "warning") return CircleX;
  return CircleX;
});

function hienThiThongBao(loai: "success" | "warning" | "error", tieuDe: string, noiDung = "") {
  if (toastTimer) {
    clearTimeout(toastTimer);
  }
  toast.value = {
    hienThi: true,
    loai,
    tieuDe,
    noiDung,
  };
  toastTimer = setTimeout(() => {
    toast.value.hienThi = false;
  }, 3200);
}

function thongBaoDonDaHoanThanh() {
  hienThiThongBao("warning", "Đơn Hàng Đã Hoàn Thành", "Đơn hàng đã hoàn thành không thể thay đổi trạng thái");
}

function moModalThongTin() {
  if (donDaHoanThanh.value) {
    thongBaoDonDaHoanThanh();
    return;
  }
  hienModalThongTin.value = true;
}

const tongTienHang = computed(() => hoaDon.value?.sanPham?.reduce((tong: number, sp: any) => tong + sp.thanhTien, 0) ?? 0);
const tongKhachCanTra = computed(() =>
  hoaDon.value ? tongTienHang.value + (hoaDon.value.phiVanChuyen || 0) - (hoaDon.value.giamGia || 0) : 0,
);
const thanhToanGanNhat = computed(() => hoaDon.value?.lichSuThanhToan?.[0] ?? null);
const lichSuRutGon = computed(() => hoaDon.value?.lichSuHoaDon ?? []);
const thongTinBuoc = computed(() => {
  const lichSu = hoaDon.value?.lichSuHoaDon ?? [];
  const nguonBuoc = donDaHuy.value ? cacBuocDaHuy : donYeuCauHuy.value ? cacBuocYeuCauHuy : cacBuoc.value;
  return nguonBuoc.map((buoc) => {
    const banGhi = lichSu.find((item: any) => 
      (item.trangThai || "").toLowerCase() === buoc.key.toLowerCase() || 
      (item.trangThai || "").toLowerCase() === buoc.ten.toLowerCase()
    ) ?? null;
    
    let time = banGhi?.ngayTao;
    let staff = banGhi?.tenNhanVien;
    
    if (!banGhi && buoc.id === 1) {
      time = hoaDon.value?.ngayTao;
      staff = hoaDon.value?.tenNhanVien ?? "Admin";
    }

    return {
      ...buoc,
      thoiGian: time,
      nhanVien: staff,
    };
  });
});

const cacBuocHienThi = computed(() => {
  if (donDaHuy.value) {
    return thongTinBuoc.value;
  }
  if (donYeuCauHuy.value) {
    return thongTinBuoc.value;
  }
  if (laDonTaiQuay.value) {
    return thongTinBuoc.value;
  }
  return thongTinBuoc.value.filter(b => b.id <= buocHienTai.value + 1);
});

function lopVongTrangThai(buoc: any) {
  if (donDaHuy.value) {
    if (buoc.id === 1) {
      return "border-[#B82220] bg-[#B82220] text-white shadow-[0_10px_22px_rgba(184,34,32,0.18)]";
    }
    return "border-rose-500 bg-rose-500 text-white shadow-[0_10px_22px_rgba(244,63,94,0.2)]";
  }

  if (donYeuCauHuy.value) {
    if (buoc.id === 1) {
      return "border-[#B82220] bg-[#B82220] text-white shadow-[0_10px_22px_rgba(184,34,32,0.18)]";
    }
    return "border-amber-400 bg-white text-slate-500 shadow-[0_10px_20px_rgba(245,158,11,0.12)]";
  }

  return buoc.id <= buocHienTai.value
    ? "border-[#B82220] bg-[#B82220] text-white shadow-[0_10px_22px_rgba(184,34,32,0.18)]"
    : "border-[#B82220] bg-white text-[#B82220]";
}

function lopTenTrangThai(buoc: any) {
  if (donDaHuy.value && buoc.id === 6) {
    return "text-rose-500";
  }
  if (donYeuCauHuy.value && buoc.id === 7) {
    return "text-slate-500";
  }
  return "text-[#B82220]";
}

async function taiChiTiet() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    hoaDon.value = await layChiTietHoaDon(Number(route.params.id));
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể tải chi tiết hóa đơn");
  } finally {
    dangTai.value = false;
  }
}

function openModalXacNhan(trangThai: string) {
  if (donDaHoanThanh.value) {
    thongBaoDonDaHoanThanh();
    return;
  }
  trangThaiMoiXacNhan.value = trangThai;
  ghiChuXacNhan.value = "";
  hienModalXacNhan.value = true;
}

async function handleXacNhanTrangThai() {
  if (!hoaDon.value || !trangThaiMoiXacNhan.value || dangCapNhat.value) return;
  if (donDaHoanThanh.value) {
    thongBaoDonDaHoanThanh();
    return;
  }
  dangCapNhat.value = true;
  try {
    hoaDon.value = await capNhatTrangThaiHoaDon(hoaDon.value.id, {
      trangThai: trangThaiMoiXacNhan.value,
      ghiChu: ghiChuXacNhan.value,
    });
    hienThiThongBao("success", "Cập Nhật Thành Công", `Đơn hàng đã chuyển sang ${trangThaiMoiXacNhan.value}.`);
    hienModalXacNhan.value = false;
  } catch (error) {
    hienThiThongBao("error", "Lỗi Cập Nhật Trạng Thái", getDisplayErrorMessage(error, "Không thể cập nhật trạng thái đơn hàng."));
  } finally {
    dangCapNhat.value = false;
  }
}

async function handleXuLyYeuCauHuy(trangThai: "Hủy" | "Chờ xác nhận") {
  if (!hoaDon.value || dangCapNhat.value) return;
  dangCapNhat.value = true;
  try {
    hoaDon.value = await capNhatTrangThaiHoaDon(hoaDon.value.id, {
      trangThai,
      ghiChu: trangThai === "Hủy" ? "Xác nhận yêu cầu hủy của khách hàng" : "Từ chối yêu cầu hủy của khách hàng",
    });
    hienThiThongBao(
      "success",
      trangThai === "Hủy" ? "Đã Xác Nhận Hủy" : "Đã Từ Chối Hủy",
      trangThai === "Hủy" ? "Đơn hàng đã được chuyển sang trạng thái hủy." : "Đơn hàng đã quay lại trạng thái chờ xác nhận.",
    );
  } catch (error) {
    hienThiThongBao("error", "Lỗi Xử Lý Yêu Cầu Hủy", getDisplayErrorMessage(error, "Không thể xử lý yêu cầu hủy đơn hàng."));
  } finally {
    dangCapNhat.value = false;
  }
}

function moModalXacNhanHuy() {
  if (dangCapNhat.value) return;
  hienModalXacNhanHuy.value = true;
}

async function handleXacNhanHuyDon() {
  await handleXuLyYeuCauHuy("Hủy");
  if (!dangCapNhat.value) {
    hienModalXacNhanHuy.value = false;
  }
}

watch(hienModalSanPham, (val) => {
  if (!val || !hoaDon.value) return;
  if (donDaHoanThanh.value) {
    hienModalSanPham.value = false;
    thongBaoDonDaHoanThanh();
    return;
  }
  danhSachSanPhamUpdate.value = (hoaDon.value.sanPham || []).map((sp: any) => ({
    chiTietId: sp.id,
    tenSanPham: sp.tenSanPham,
    soLuong: sp.soLuong,
    giaBan: sp.donGia,
    maBienThe: sp.phanLoai || "",
  }));
});

async function timKiemSanPham() {
  if (!tuKhoaSanPham.value.trim()) {
    ketQuaTimKiem.value = [];
    return;
  }
  dangTimKiem.value = true;
  try {
    ketQuaTimKiem.value = await timSanPhamTaiQuay(tuKhoaSanPham.value);
  } finally {
    dangTimKiem.value = false;
  }
}

function themSanPham(sp: SanPhamTaiQuay) {
  const existing = danhSachSanPhamUpdate.value.find((i) => i.chiTietId === sp.chiTietId);
  if (existing) existing.soLuong++;
  else {
    danhSachSanPhamUpdate.value.push({
      chiTietId: sp.chiTietId,
      tenSanPham: sp.tenSanPham,
      soLuong: 1,
      giaBan: sp.giaBan,
      maBienThe: `${sp.mauSac} - ${sp.kichCo}`,
    });
  }
}

function removeSanPham(id: number) {
  danhSachSanPhamUpdate.value = danhSachSanPhamUpdate.value.filter((i) => i.chiTietId !== id);
}

async function handleSaveSanPham() {
  if (!hoaDon.value || dangCapNhat.value) return;
  if (donDaHoanThanh.value) {
    thongBaoDonDaHoanThanh();
    return;
  }
  dangCapNhat.value = true;
  try {
    hoaDon.value = await capNhatSanPhamHoaDon(hoaDon.value.id, {
      items: danhSachSanPhamUpdate.value.map((i) => ({ chiTietId: i.chiTietId, soLuong: i.soLuong })),
    });
    hienThiThongBao("success", "Cập Nhật Thành Công", "Sản phẩm hóa đơn đã được cập nhật.");
    hienModalSanPham.value = false;
  } catch (error) {
    window.alert(getDisplayErrorMessage(error, "Không thể cập nhật sản phẩm trong hóa đơn"));
  } finally {
    dangCapNhat.value = false;
  }
}

const danhSachTrangThaiHienThi = [
  { key: "Chờ xác nhận", label: "Chờ xác nhận" },
  { key: "Đã xác nhận", label: "Đã xác nhận" },
  { key: "Chờ lấy hàng", label: "Chờ lấy hàng" },
  { key: "Chờ giao hàng", label: "Chờ giao hàng" },
  { key: "Đã giao hàng", label: "Đã giao hàng" },
  { key: "Hoàn thành", label: "Hoàn thành" },
  { key: "Yêu cầu hủy", label: "Yêu cầu hủy" },
  { key: "Cần hoàn tiền", label: "Cần hoàn tiền" }
];

const indexTrangThaiHienTai = computed(() => {
  if (!hoaDon.value) return -1;
  const currentStt = (hoaDon.value.trangThai || "").toLowerCase().trim();
  let normalizedStt = hoaDon.value.trangThai;
  
  if (currentStt === 'chờ xác nhận' || currentStt === 'cho_xac_nhan') normalizedStt = "Chờ xác nhận";
  else if (currentStt === 'đã xác nhận' || currentStt === 'da_xac_nhan') normalizedStt = "Đã xác nhận";
  else if (currentStt === 'chờ lấy hàng' || currentStt === 'cho_lay_hang' || currentStt === 'cho_giao_hang') normalizedStt = "Chờ lấy hàng";
  else if (currentStt === 'chờ giao hàng' || currentStt === 'đang vận chuyển' || currentStt === 'chờ vận chuyển' || currentStt === 'dang_van_chuyen') normalizedStt = "Chờ giao hàng";
  else if (currentStt === 'đã giao hàng' || currentStt === 'vận chuyển' || currentStt === 'da_giao_hang') normalizedStt = "Đã giao hàng";
  else if (currentStt === 'hoàn thành' || currentStt === 'đã hoàn thành' || currentStt === 'hoan_thanh') normalizedStt = "Hoàn thành";
  else if (currentStt === 'yêu cầu hủy' || currentStt === 'yeu_cau_huy') normalizedStt = "Yêu cầu hủy";
  else if (currentStt === 'cần hoàn tiền' || currentStt === 'can_hoan_tien') normalizedStt = "Cần hoàn tiền";
  else if (currentStt === 'hủy' || currentStt === 'huy') normalizedStt = "Hủy";

  return danhSachTrangThaiHienThi.findIndex(x => x.key === normalizedStt);
});

function isOptionDisabled(stKey: string) {
  if (!hoaDon.value) return false;
  const targetIndex = danhSachTrangThaiHienThi.findIndex(x => x.key === stKey);
  if (indexTrangThaiHienTai.value < 0 || targetIndex < 0) return false;
  
  // Disable if it's the current one (no change) or already passed
  // Actually, we want them to be able to keep current or pick next.
  // User says "update forward", so picking current is just "staying".
  return targetIndex < indexTrangThaiHienTai.value;
}

function hienThiOptionTrangThai(index: number, key: string) {
  if (donDaHoanThanh.value) {
    return key === "Hoàn thành";
  }
  return laDonTaiQuay.value
    ? (index === 0 || key === "Hoàn thành")
    : (index === indexTrangThaiHienTai.value || index === indexTrangThaiHienTai.value + 1);
}

watch(hienModalThongTin, (val) => {
  if (!val || !hoaDon.value) return;
  if (donDaHoanThanh.value) {
    hienModalThongTin.value = false;
    thongBaoDonDaHoanThanh();
    return;
  }
  const stt = (hoaDon.value.trangThai || "").toLowerCase().trim();
  let defaultStt = hoaDon.value.trangThai;
  if (stt === 'chờ xác nhận' || stt === 'cho_xac_nhan') defaultStt = "Chờ xác nhận";
  else if (stt === 'đã xác nhận' || stt === 'da_xac_nhan') defaultStt = "Đã xác nhận";
  else if (stt === 'chờ lấy hàng' || stt === 'cho_lay_hang' || stt === 'cho_giao_hang') defaultStt = "Chờ lấy hàng";
  else if (stt === 'chờ giao hàng' || stt === 'đang vận chuyển' || stt === 'chờ vận chuyển' || stt === 'dang_van_chuyen') defaultStt = "Chờ giao hàng";
  else if (stt === 'đã giao hàng' || stt === 'vận chuyển' || stt === 'da_giao_hang') defaultStt = "Đã giao hàng";
  else if (stt === 'hoàn thành' || stt === 'đã hoàn thành' || stt === 'hoan_thanh') defaultStt = "Hoàn thành";
  else if (stt === 'yêu cầu hủy' || stt === 'yeu_cau_huy') defaultStt = "Yêu cầu hủy";
  else if (stt === 'cần hoàn tiền' || stt === 'can_hoan_tien') defaultStt = "Cần hoàn tiền";
  else if (stt === 'hủy' || stt === 'huy') defaultStt = "Hủy";

  formThongTin.value = {
    trangThai: defaultStt,
    tenKhachHang: hoaDon.value.tenKhachHang || "",
    soDienThoai: hoaDon.value.soDienThoai || "",
    email: hoaDon.value.email || "",
    diaChi: hoaDon.value.diaChi || "",
    loaiDon: hoaDon.value.loaiDon || ""
  };
  tabHienTai.value = "donHang";
});

watch(donDaHoanThanh, (locked) => {
  if (!locked) return;
  hienModalXacNhan.value = false;
  hienModalSanPham.value = false;
  hienModalThongTin.value = false;
});

async function handleLuuThongTin() {
  if (!hoaDon.value || dangCapNhat.value) return;
  if (donDaHoanThanh.value) {
    thongBaoDonDaHoanThanh();
    return;
  }
  dangCapNhat.value = true;
  try {
    if (formThongTin.value.trangThai !== hoaDon.value.trangThai) {
      hoaDon.value = await capNhatTrangThaiHoaDon(hoaDon.value.id, {
        trangThai: formThongTin.value.trangThai,
        ghiChu: ""
      });
    }
    hienThiThongBao("success", "Cập Nhật Thành Công", "Thông tin hóa đơn đã được cập nhật.");
    hienModalThongTin.value = false;
  } catch (error) {
    hienThiThongBao("error", "Lỗi Cập Nhật Thông Tin", getDisplayErrorMessage(error, "Không thể cập nhật thông tin hóa đơn."));
  } finally {
    dangCapNhat.value = false;
  }
}

async function handleTinhPhiGhn() {
  if (!hoaDon.value || dangTinhPhiGhn.value) return;
  if (donDaHoanThanh.value) {
    thongBaoDonDaHoanThanh();
    return;
  }
  if (!formThongTin.value.diaChi.trim()) {
    window.alert("Vui lòng nhập địa chỉ người nhận để tính phí GHN.");
    return;
  }

  dangTinhPhiGhn.value = true;
  try {
    const ketQua = await tinhPhiVanChuyenGhn(hoaDon.value.id, {
      toAddress: formThongTin.value.diaChi.trim(),
      serviceTypeId: Number(formGhn.value.serviceTypeId) || 2,
      length: Number(formGhn.value.length) || 30,
      width: Number(formGhn.value.width) || 20,
      height: Number(formGhn.value.height) || 12,
      weight: Number(formGhn.value.weight) || 500,
      insuranceValue: Math.min(Number(tongTienHang.value) || 0, 5000000),
    });
    diaChiGhnDaDo.value = [ketQua.matchedWardName, ketQua.matchedDistrictName, ketQua.matchedProvinceName]
      .filter(Boolean)
      .join(", ");
    hoaDon.value = await layChiTietHoaDon(hoaDon.value.id);
    hienThiThongBao("success", "Đã Tính Phí GHN", `Phí vận chuyển mới: ${dinhDangTien(ketQua.phiVanChuyen || ketQua.total || 0)}.`);
  } catch (error) {
    window.alert(getDisplayErrorMessage(error, "Không thể tính phí vận chuyển GHN"));
  } finally {
    dangTinhPhiGhn.value = false;
  }
}

function handlePrint() {
  if (!hoaDon.value) return;
  printInvoiceToPdf({
    invoice: hoaDon.value,
    filename: `hoa-don-${hoaDon.value.maHoaDon}`,
    formatCurrency: dinhDangTien,
    formatDate: dinhDangNgay,
  });
}

onMounted(taiChiTiet);
</script>

<template>
  <div class="space-y-4 pb-10">
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="translate-y-3 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-3 opacity-0"
    >
      <div
        v-if="toast.hienThi"
        class="fixed right-5 top-5 z-[70] w-[360px] max-w-[calc(100vw-2rem)] overflow-hidden rounded-2xl border bg-white shadow-[0_18px_60px_rgba(15,23,42,0.18)]"
        :class="toastClass"
      >
        <div class="flex gap-3 p-4">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full" :class="toastIconClass">
            <component :is="ToastIcon" class="h-5 w-5" />
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-sm font-bold text-slate-800">{{ toast.tieuDe }}</p>
            <p v-if="toast.noiDung" class="mt-1 text-sm leading-5 text-slate-600">{{ toast.noiDung }}</p>
          </div>
          <button type="button" class="rounded-full p-1 text-slate-400 transition hover:bg-white/70 hover:text-slate-600" @click="toast.hienThi = false">
            <X class="h-4 w-4" />
          </button>
        </div>
        <div class="h-1.5 w-full" :class="toastAccentClass"></div>
      </div>
    </Transition>

    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-[22px] font-bold leading-tight text-slate-800 md:text-[24px]">Chi Tiết Đơn Hàng</h1>
        <div v-if="hoaDon" class="mt-2 space-y-1 text-[13px] text-slate-500">
          <p>
            Mã Đơn Hàng: <span class="font-semibold text-slate-700">{{ hoaDon.maHoaDon }}</span>
            <span class="mx-2 text-slate-300">|</span>
            Ngày Tạo: {{ dinhDangGio(hoaDon.ngayTao) }} {{ dinhDangNgay(hoaDon.ngayTao) }}
          </p>
          <p>
            Tạo Bởi:
            <span class="font-medium text-slate-700">{{ hoaDon.tenNhanVien || "Hệ Thống" }}</span>
            <span class="mx-2 text-slate-300">|</span>
            Cập Nhật Gần Nhất:
            <span class="font-medium text-slate-700">
              {{
                hoaDon.lichSuHoaDon?.[0]
                  ? `${dinhDangGio(hoaDon.lichSuHoaDon[0].ngayTao)} ${dinhDangNgay(hoaDon.lichSuHoaDon[0].ngayTao)} - ${hoaDon.lichSuHoaDon[0].tenNhanVien}`
                  : "Chưa Có"
              }}
            </span>
          </p>
        </div>
      </div>

      <button
        type="button"
        @click="router.push({ name: 'admin-hoa-don' })"
        class="inline-flex h-10 items-center gap-2 rounded-full bg-slate-500 px-4 text-sm font-semibold text-white transition hover:bg-slate-600"
      >
        <ArrowLeft class="h-4 w-4" />
        Quay Lại Danh Sách
      </button>
    </div>

    <div v-if="dangTai" class="rounded-[28px] border border-slate-200 bg-white p-10 text-center text-sm text-slate-400 shadow-sm">
      Đang Tải Chi Tiết Hóa Đơn...
    </div>

    <div v-else-if="loiTrang || !hoaDon" class="rounded-[28px] border border-slate-200 bg-white p-10 text-center shadow-sm">
      <h2 class="text-2xl font-bold text-slate-800">Không Tìm Thấy Hóa Đơn</h2>
      <p class="mt-3 text-sm text-slate-400">{{ loiTrang || "Hóa Đơn Không Tồn Tại." }}</p>
    </div>

    <template v-else>
      <section class="grid items-stretch gap-3 xl:grid-cols-[1fr_1fr_0.95fr]">
        <div class="flex h-full flex-col rounded-[26px] border border-slate-200 bg-white px-4 py-4 shadow-sm md:px-5 xl:col-span-2">
          <div class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
            <ClipboardList class="h-4.5 w-4.5 text-slate-500" />
            Trạng Thái Đơn Hàng
          </div>

          <div class="relative mt-7 px-2 pt-2 flex justify-center">
            <div class="flex w-full items-start justify-around relative max-w-4xl">
              <!-- Đường ziczac động -->
              <div 
                class="absolute top-7 hidden h-[2px] bg-slate-200 md:block z-0" 
                :style="{ left: (100 / cacBuocHienThi.length / 2) + '%', right: (100 / cacBuocHienThi.length / 2) + '%' }"
              ></div>

              <div v-for="buoc in cacBuocHienThi" :key="buoc.id" class="relative z-10 flex w-32 flex-col items-center text-center">
                <div
                  class="flex h-[58px] w-[58px] items-center justify-center overflow-visible rounded-full border-[2.5px] transition-all"
                  :class="lopVongTrangThai(buoc)"
                >
                  <component :is="buoc.icon" class="h-[22px] w-[22px] block shrink-0" stroke-width="2.25" />
                </div>
                <p class="mt-3 whitespace-nowrap text-[12px] font-semibold" :class="lopTenTrangThai(buoc)">{{ buoc.ten }}</p>
                <div class="mt-1 min-h-[32px]">
                  <p v-if="buoc.thoiGian" class="text-[11px] leading-4 text-slate-400">
                    {{ dinhDangGio(buoc.thoiGian) }} {{ dinhDangNgay(buoc.thoiGian) }}
                  </p>
                  <p v-if="buoc.nhanVien" class="text-[11px] text-slate-400">{{ buoc.nhanVien }}</p>
                </div>
              </div>
            </div>
          </div>

          <div
            v-if="donYeuCauHuy"
            class="mt-5 flex items-center justify-center gap-2 rounded-lg border border-amber-200 bg-amber-50 px-4 py-2.5 text-sm font-semibold text-amber-700"
          >
            <TriangleAlert class="h-4 w-4" />
            Khách hàng yêu cầu hủy - đang chờ xác nhận
          </div>
          <div
            v-if="donDaHuy"
            class="mt-5 flex items-center justify-center gap-2 rounded-lg border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm font-semibold text-rose-700"
          >
            <CircleX class="h-4 w-4" />
            Đơn hàng đã bị hủy
          </div>

          <div class="mt-5 flex justify-end">
            <button
              type="button"
              @click="hienModalLichSu = true"
              class="inline-flex h-10 items-center gap-2 rounded-full bg-[#B82220] px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-[#B82220]/90"
            >
              <History class="h-4 w-4" />
              Lịch Sử Thao Tác
            </button>
          </div>
        </div>

        <aside class="flex h-full flex-col rounded-[26px] border border-slate-200 bg-white px-5 py-4 shadow-sm">
          <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
            <Banknote class="h-4.5 w-4.5 text-slate-500" />
            Tổng Kết Thanh Toán
          </h2>

          <div class="mt-4 flex-1 space-y-3 text-sm">
            <div class="flex items-center justify-between">
              <span class="text-slate-500">Tổng Tiền Hàng</span>
              <span class="font-semibold text-slate-700">{{ dinhDangTien(tongTienHang) }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-slate-500">Giảm Giá</span>
              <span class="font-semibold text-emerald-500">- {{ dinhDangTien(hoaDon.giamGia) }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="flex items-center gap-2 text-slate-500">
                Phí Vận Chuyển
                <img :src="logoGhn" alt="GHN" class="h-4 w-auto object-contain" />
              </span>
              <span class="font-semibold text-slate-700">+ {{ dinhDangTien(hoaDon.phiVanChuyen) }}</span>
            </div>

            <div class="border-t border-slate-200 pt-4">
              <div class="flex items-center justify-between">
                <span class="text-[15px] font-bold tracking-wide text-[#B82220]">Tổng Tiền</span>
                <span class="text-[18px] font-bold text-[#B82220]">{{ dinhDangTien(tongKhachCanTra) }}</span>
              </div>
            </div>
          </div>
        </aside>
      </section>

      <section class="grid gap-3 xl:grid-cols-[1fr_1fr_0.95fr]">
        <div class="rounded-[26px] border border-slate-200 bg-white px-5 py-4 shadow-sm">
          <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
            <User class="h-4.5 w-4.5 text-slate-500" />
            Thông Tin Khách Hàng
          </h2>

          <div class="mt-4 space-y-4 text-sm">
            <div class="flex items-center justify-between border-b border-slate-100 pb-3">
              <span class="text-slate-400">Tên Khách Hàng</span>
              <span class="font-semibold text-slate-700">{{ hoaDon.tenKhachHang }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-100 pb-3">
              <span class="text-slate-400">Số Điện Thoại</span>
              <span class="font-semibold text-slate-700">{{ hoaDon.soDienThoai }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-slate-400">Email</span>
              <span class="font-semibold text-slate-700">{{ hoaDon.email || "—" }}</span>
            </div>
          </div>
        </div>

        <div class="rounded-[26px] border border-slate-200 bg-white px-5 py-4 shadow-sm">
          <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
            <MapPin class="h-4.5 w-4.5 text-slate-500" />
            Thông Tin Giao Hàng
          </h2>

          <div class="mt-4 space-y-4 text-sm">
            <div class="flex items-center justify-between border-b border-slate-100 pb-3">
              <span class="text-slate-400">Địa Chỉ</span>
              <span class="max-w-[58%] text-right font-semibold text-slate-700">{{ hoaDon.diaChi || "—" }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-100 pb-3">
              <span class="text-slate-400">Loại Đơn</span>
              <span class="font-semibold text-slate-700">{{ hoaDon.loaiDon }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-slate-400">Ghi Chú</span>
              <span class="max-w-[58%] text-right font-semibold text-slate-700">{{ hoaDon.ghiChu || "—" }}</span>
            </div>
          </div>
        </div>

        <div class="space-y-3">
          <div class="rounded-[26px] border border-slate-200 bg-white px-5 py-4 shadow-sm">
            <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
              <History class="h-4.5 w-4.5 text-slate-500" />
              Lịch Sử Thanh Toán
            </h2>

            <div v-if="thanhToanGanNhat" class="mt-4 space-y-3 text-sm">
              <div class="flex items-center justify-between">
                <span class="font-semibold text-slate-700">{{ thanhToanGanNhat.phuongThucThanhToan }}</span>
                <span class="font-bold text-[#B82220]">{{ dinhDangTien(thanhToanGanNhat.tongTien) }}</span>
              </div>
              <div class="text-xs text-slate-400">
                {{ dinhDangGio(thanhToanGanNhat.thoiGian) }} {{ dinhDangNgay(thanhToanGanNhat.thoiGian) }}
                <span class="mx-1">-</span>
                {{ hoaDon.tenNhanVien || "Admin" }}
              </div>
            </div>
            <div v-else class="mt-4 text-sm text-slate-400">Chưa Có Lịch Sử Thanh Toán.</div>
          </div>

          <div
            v-if="donYeuCauHuy"
            class="rounded-[26px] border border-rose-100 bg-white px-5 py-4 shadow-sm"
          >
            <h2 class="flex items-center gap-2 text-[15px] font-semibold text-[#B82220]">
              <TriangleAlert class="h-4.5 w-4.5" />
              Khách Hàng Yêu Cầu Hủy Đơn
            </h2>
            <p class="mt-2 text-xs text-slate-500">Xem lịch sử thao tác để biết lý do yêu cầu hủy.</p>
            <div class="mt-4 grid gap-2 sm:grid-cols-2">
              <button
                type="button"
                @click="moModalXacNhanHuy"
                :disabled="dangCapNhat"
                class="inline-flex h-9 items-center justify-center gap-2 rounded-full bg-[#B82220] px-4 text-sm font-semibold text-white transition hover:bg-[#B82220]/90 disabled:cursor-not-allowed disabled:opacity-60"
              >
                <CircleCheck class="h-4 w-4" />
                Xác Nhận Hủy
              </button>
              <button
                type="button"
                @click="handleXuLyYeuCauHuy('Chờ xác nhận')"
                :disabled="dangCapNhat"
                class="inline-flex h-9 items-center justify-center gap-2 rounded-full border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
              >
                <CircleX class="h-4 w-4" />
                Từ Chối Hủy
              </button>
            </div>
          </div>

          <button
            type="button"
            @click="handlePrint"
            class="flex h-10 w-full items-center justify-center gap-2 rounded-full bg-sky-500 px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-sky-600"
          >
            <Printer class="h-4 w-4" />
            In Hóa Đơn
          </button>

          <button
            v-if="!donDaHoanThanh"
            type="button"
            @click="moModalThongTin"
            class="flex h-10 w-full items-center justify-center gap-2 rounded-full bg-amber-500 px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-amber-600"
          >
            <Pencil class="h-4 w-4" />
            Chỉnh Sửa Đơn Hàng
          </button>
        </div>
      </section>

      <section class="grid gap-3 xl:grid-cols-[1fr_1fr_0.95fr]">
        <div class="rounded-[26px] border border-slate-200 bg-white px-5 py-4 shadow-sm xl:col-span-2">
          <div class="mb-4 flex items-center justify-between gap-4">
            <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
              <Package class="h-4.5 w-4.5 text-slate-500" />
              Danh Sách Sản Phẩm ({{ hoaDon.sanPham?.length || 0 }})
            </h2>
          </div>

          <div class="overflow-x-auto">
            <table class="min-w-[980px] w-full text-sm">
              <thead>
                <tr class="bg-slate-100 text-left text-[11px] font-bold tracking-wide text-slate-950">
                  <th class="rounded-l-2xl px-4 py-3">STT</th>
                  <th class="px-4 py-3">Ảnh</th>
                  <th class="px-4 py-3">Sản Phẩm</th>
                  <th class="px-4 py-3">Màu Sắc</th>
                  <th class="px-4 py-3">Số Lượng</th>
                  <th class="px-4 py-3">Thời Gian</th>
                  <th class="px-4 py-3">Đơn Giá</th>
                  <th class="rounded-r-2xl px-4 py-3">Tổng Tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in hoaDon.sanPham" :key="item.id" class="border-b border-slate-100 last:border-b-0">
                  <td class="px-4 py-5 font-semibold text-slate-600">{{ index + 1 }}</td>
                  <td class="px-4 py-5">
                    <img :src="item.hinhAnh || 'https://via.placeholder.com/72x72?text=Shoe'" class="h-12 w-12 rounded-xl object-cover" />
                  </td>
                  <td class="px-4 py-5">
                    <p class="font-semibold text-slate-800">{{ vietHoaChuCaiDau(item.tenSanPham) }}</p>
                    <p class="mt-1 text-xs text-slate-400">{{ item.phanLoai }}</p>
                  </td>
                  <td class="px-4 py-5 text-slate-600">{{ item.mauSac }}</td>
                  <td class="px-4 py-5 font-semibold text-slate-700">{{ item.soLuong }}</td>
                  <td class="px-4 py-5">
                    <p class="text-xs font-semibold text-slate-700">{{ dinhDangGio(hoaDon.ngayTao) }}</p>
                    <p class="text-xs text-slate-400">{{ dinhDangNgay(hoaDon.ngayTao) }}</p>
                  </td>
                  <td class="px-4 py-5 font-semibold text-[#B82220]">{{ dinhDangTien(item.donGia) }}</td>
                  <td class="px-4 py-5 font-semibold text-[#B82220]">{{ dinhDangTien(item.thanhTien) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div></div>
      </section>
    </template>

    <div v-if="hienModalXacNhan" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-md overflow-hidden rounded-[28px] bg-white shadow-2xl">
        <div class="p-7">
          <h3 class="text-xl font-bold text-slate-800">Xác Nhận Cập Nhật Trạng Thái</h3>
          <p class="mt-2 text-sm text-slate-400">
            Cập Nhật Đơn Hàng Sang Trạng Thái
            <span class="font-semibold text-slate-700">{{ trangThaiMoiXacNhan }}</span>
          </p>
          <div class="mt-6">
            <label class="mb-2 block text-xs font-semibold uppercase tracking-wide text-slate-400">Ghi Chú</label>
            <textarea
              v-model="ghiChuXacNhan"
              rows="4"
              placeholder="Nhập Ghi Chú Cho Bước Này..."
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            ></textarea>
          </div>
          <div class="mt-7 flex gap-3">
            <button @click="hienModalXacNhan = false" class="flex-1 rounded-2xl bg-slate-100 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-200">
              Đóng
            </button>
            <button @click="handleXacNhanTrangThai" :disabled="dangCapNhat" class="flex-1 rounded-2xl bg-[#B82220] py-3 text-sm font-semibold text-white transition hover:bg-[#B82220]/90 disabled:opacity-50">
              {{ dangCapNhat ? "Đang Lưu..." : "Xác Nhận" }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalXacNhanHuy" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-md overflow-hidden rounded-[24px] bg-white shadow-2xl">
        <div class="p-6">
          <div class="flex items-start gap-4">
            <div class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-rose-50 text-[#B82220]">
              <TriangleAlert class="h-6 w-6" />
            </div>
            <div>
              <h3 class="text-[19px] font-bold text-slate-800">Xác Nhận Hủy Đơn Hàng?</h3>
              <p class="mt-2 text-sm leading-6 text-slate-500">
                Bạn có chắc chắn muốn xác nhận hủy đơn hàng này không? Sau khi xác nhận, đơn hàng sẽ chuyển sang trạng thái đã hủy.
              </p>
            </div>
          </div>

          <div class="mt-7 flex gap-3">
            <button
              type="button"
              @click="hienModalXacNhanHuy = false"
              :disabled="dangCapNhat"
              class="flex-1 rounded-2xl bg-slate-100 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-200 disabled:cursor-not-allowed disabled:opacity-60"
            >
              Quay Lại
            </button>
            <button
              type="button"
              @click="handleXacNhanHuyDon"
              :disabled="dangCapNhat"
              class="flex-1 rounded-2xl bg-[#B82220] py-3 text-sm font-semibold text-white transition hover:bg-[#B82220]/90 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {{ dangCapNhat ? "Đang Hủy..." : "Xác Nhận Hủy" }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalLichSu" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-2xl overflow-hidden rounded-[24px] bg-white shadow-2xl">
        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
          <div class="flex items-center gap-3">
            <History class="h-5 w-5 text-slate-500" />
            <h3 class="text-[17px] font-bold text-slate-800">Lịch sử thao tác</h3>
          </div>
          <button @click="hienModalLichSu = false" class="text-slate-400 transition hover:text-slate-600">
            <CircleX class="h-5 w-5" />
          </button>
        </div>
        
        <div class="max-h-[70vh] overflow-y-auto px-6 py-8">
          <div v-if="!lichSuRutGon.length" class="py-10 text-center text-sm text-slate-400">Chưa có lịch sử thao tác.</div>
          <div v-else class="relative pl-8">
            <!-- Trục timeline đỏ -->
            <div class="absolute bottom-0 left-[3.5px] top-0 w-[1.5px] bg-[#B82220]/20"></div>
            
            <div class="space-y-6">
              <div v-for="log in lichSuRutGon" :key="log.id" class="relative">
                <!-- Chấm tròn đỏ trên trục -->
                <div class="absolute -left-[32px] top-4 h-2 w-2 rounded-full border-2 border-white bg-[#B82220] shadow-[0_0_0_2px_rgba(184,34,32,0.15)]"></div>
                
                <div class="rounded-2xl border border-slate-50 bg-slate-50/50 p-4 transition-colors hover:bg-slate-100/50">
                  <div class="text-[12px] text-slate-400 font-medium">
                    {{ dinhDangGio(log.ngayTao) }} {{ dinhDangNgay(log.ngayTao) }}
                  </div>
                  <div class="mt-1 text-[13px] font-semibold text-slate-400">
                    {{ log.maNhanVien || 'Hệ thống' }} - {{ log.tenNhanVien || 'admin' }}
                  </div>
                  <p class="mt-2 text-[15px] font-bold text-slate-800">{{ log.trangThai }}</p>
                  <p v-if="log.ghiChu" class="mt-2 text-[13px] text-slate-500 italic">{{ log.ghiChu }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalSanPham" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-4xl overflow-hidden rounded-[32px] bg-white shadow-2xl">
        <div class="grid lg:grid-cols-2">
          <div class="border-r border-slate-100 p-7">
            <h3 class="text-xl font-bold text-slate-800">Tìm Sản Phẩm</h3>
            <div class="relative mt-6">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                v-model="tuKhoaSanPham"
                @input="timKiemSanPham"
                type="text"
                placeholder="Tên Sản Phẩm, SKU..."
                class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>
            <div class="mt-6 max-h-[420px] space-y-3 overflow-y-auto pr-2">
              <div v-if="dangTimKiem" class="py-10 text-center text-sm text-slate-400">Đang Tìm Kiếm...</div>
              <div v-else-if="!ketQuaTimKiem.length" class="py-10 text-center text-sm text-slate-400">Nhập Từ Khóa Để Tìm Sản Phẩm</div>
              <div v-for="sp in ketQuaTimKiem" :key="sp.chiTietId" @click="themSanPham(sp)" class="cursor-pointer rounded-2xl border border-slate-100 p-4 transition hover:border-rose-200 hover:bg-rose-50/40">
                <div class="flex items-center justify-between gap-4">
                  <div>
                    <p class="font-semibold text-slate-800">{{ sp.tenSanPham }}</p>
                    <p class="mt-1 text-xs text-slate-400">{{ sp.mauSac }} · {{ sp.kichCo }} · Tồn: {{ sp.soLuongTon }}</p>
                  </div>
                  <p class="font-semibold text-[#B82220]">{{ dinhDangTien(sp.giaBan) }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-slate-50/60 p-7">
            <h3 class="text-xl font-bold text-slate-800">Chỉnh Sửa Đơn Hàng</h3>
            <div class="mt-6 max-h-[420px] space-y-3 overflow-y-auto pr-2">
              <div v-if="!danhSachSanPhamUpdate.length" class="py-10 text-center text-sm text-slate-400">Chưa Có Sản Phẩm Nào</div>
              <div v-for="item in danhSachSanPhamUpdate" :key="item.chiTietId" class="flex items-center gap-4 rounded-2xl bg-white p-4 shadow-sm">
                <div class="flex-1">
                  <p class="font-semibold text-slate-800">{{ item.tenSanPham }}</p>
                  <p class="mt-1 text-xs text-slate-400">{{ item.maBienThe }}</p>
                </div>
                <input v-model.number="item.soLuong" type="number" min="1" class="h-10 w-16 rounded-xl border border-slate-200 bg-slate-50 text-center text-sm font-semibold outline-none focus:border-rose-300" />
                <button @click="removeSanPham(item.chiTietId)" class="rounded-xl bg-[#B82220]/10 p-2 text-[#B82220] transition hover:bg-[#B82220]/20">
                  <Trash2 class="h-4 w-4" />
                </button>
              </div>
            </div>
            <div class="mt-6 border-t border-slate-200 pt-5">
              <div class="flex items-center justify-between text-sm font-semibold">
                <span class="text-slate-700">Tổng Tiền Hàng</span>
                <span class="text-[#B82220]">{{ dinhDangTien(danhSachSanPhamUpdate.reduce((t, i) => t + i.giaBan * i.soLuong, 0)) }}</span>
              </div>
              <div class="mt-5 flex gap-3">
                <button @click="hienModalSanPham = false" class="flex-1 rounded-2xl bg-white py-3 text-sm font-semibold text-slate-600 ring-1 ring-slate-200 transition hover:bg-slate-50">
                  Đóng
                </button>
                <button @click="handleSaveSanPham" :disabled="dangCapNhat || donDaHoanThanh" class="flex-1 rounded-2xl bg-amber-500 py-3 text-sm font-semibold text-white transition hover:bg-amber-600 disabled:opacity-50">
                  {{ dangCapNhat ? "Đang Lưu..." : "Lưu Thay Đổi" }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalThongTin" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-[600px] overflow-hidden rounded-[16px] bg-white shadow-2xl">
        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
          <h3 class="text-[17px] font-semibold text-slate-800">Cập nhật thông tin đơn hàng</h3>
          <button @click="hienModalThongTin = false" class="text-slate-400 transition hover:text-slate-600">
            <CircleX class="h-5 w-5" />
          </button>
        </div>

        <div class="flex gap-6 border-b border-slate-100 px-6 pt-3 text-[14px]">
          <button
            :class="tabHienTai === 'donHang' ? 'border-b-2 border-slate-700 font-semibold text-slate-800' : 'text-slate-500 hover:text-slate-700'"
            class="pb-3 transition-colors"
            @click="tabHienTai = 'donHang'"
          >
            Thông tin đơn hàng
          </button>
          <button
            :class="tabHienTai === 'khachHang' ? 'border-b-2 border-blue-500 font-semibold text-blue-500' : 'text-blue-500 hover:text-blue-600'"
            class="pb-3 transition-colors"
            @click="tabHienTai = 'khachHang'"
          >
            Thông tin khách hàng
          </button>
          <button
            :class="tabHienTai === 'giaoHang' ? 'border-b-2 border-blue-500 font-semibold text-blue-500' : 'text-blue-500 hover:text-blue-600'"
            class="pb-3 transition-colors"
            @click="tabHienTai = 'giaoHang'"
          >
            Thông tin giao hàng
          </button>
        </div>

        <div class="p-6">
          <div v-if="tabHienTai === 'donHang'" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Mã đơn hàng</label>
                <input type="text" readonly :value="hoaDon.maHoaDon" class="w-full rounded-[8px] bg-slate-100 px-3 py-2.5 text-[14px] text-slate-600 outline-none" />
              </div>
              <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Ngày tạo</label>
                <input type="text" readonly :value="dinhDangGio(hoaDon.ngayTao) + ' ' + dinhDangNgay(hoaDon.ngayTao)" class="w-full rounded-[8px] bg-slate-100 px-3 py-2.5 text-[14px] text-slate-600 outline-none" />
              </div>
            </div>
            <div>
              <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Trạng thái</label>
              <select v-model="formThongTin.trangThai" class="w-full rounded-[8px] border border-blue-400 px-3 py-2.5 text-[14px] text-slate-800 outline-none ring-1 ring-blue-100 transition focus:border-blue-500 focus:ring-blue-300 disabled:cursor-not-allowed disabled:bg-slate-100">
                <template v-for="(st, index) in danhSachTrangThaiHienThi" :key="st.key">
                  <option 
                    v-if="hienThiOptionTrangThai(index, st.key)"
                    :value="st.key" 
                    :disabled="isOptionDisabled(st.key)"
                  >
                    {{ st.label }}
                  </option>
                </template>
              </select>
            </div>
          </div>

          <div v-if="tabHienTai === 'khachHang'" class="space-y-4">
             <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Tên khách hàng</label>
                <input type="text" v-model="formThongTin.tenKhachHang" class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition focus:border-blue-400" />
             </div>
             <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Số điện thoại</label>
                <input type="text" v-model="formThongTin.soDienThoai" class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition focus:border-blue-400" />
             </div>
             <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Email</label>
                <input type="text" v-model="formThongTin.email" class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition focus:border-blue-400" />
             </div>
          </div>

          <div v-if="tabHienTai === 'giaoHang'" class="space-y-4">
             <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Địa chỉ cụ thể</label>
                <textarea v-model="formThongTin.diaChi" rows="2" class="w-full rounded-[8px] border border-slate-200 px-3 py-2 text-[14px] text-slate-800 outline-none transition focus:border-blue-400"></textarea>
             </div>
             <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Loại đơn</label>
                <input type="text" readonly :value="formThongTin.loaiDon" class="w-full rounded-[8px] bg-slate-100 px-3 py-2.5 text-[14px] text-slate-600 outline-none" />
             </div>
             <div class="rounded-2xl border border-rose-100 bg-rose-50/40 p-4">
                <div class="mb-3 flex items-center justify-between gap-3">
                  <div>
                    <p class="flex items-center gap-2 text-[13px] font-semibold text-slate-700">
                      Tự Tính Phí Vận Chuyển
                      <img :src="logoGhn" alt="GHN" class="h-4 w-auto object-contain" />
                    </p>
                  </div>
                  <button
                    type="button"
                    @click="handleTinhPhiGhn"
                    :disabled="dangTinhPhiGhn || laDonTaiQuay || donDaHoanThanh"
                    class="rounded-full bg-rose-500 px-4 py-2 text-xs font-semibold text-white transition hover:bg-rose-600 disabled:cursor-not-allowed disabled:bg-slate-300"
                  >
                    {{ dangTinhPhiGhn ? "Đang Tính..." : "Tự Tính Phí GHN" }}
                  </button>
                </div>
                <div class="grid gap-3 md:grid-cols-2">
                  <div v-if="diaChiGhnDaDo" class="md:col-span-2 rounded-xl bg-white px-3 py-2 text-sm text-slate-600">
                    GHN Đã Dò: <span class="font-semibold text-slate-800">{{ diaChiGhnDaDo }}</span>
                  </div>
                  <label class="space-y-1.5">
                    <span class="text-xs font-medium text-slate-500">Loại Dịch Vụ</span>
                    <select v-model.number="formGhn.serviceTypeId" class="w-full rounded-[8px] border border-slate-200 bg-white px-3 py-2 text-[14px] outline-none transition focus:border-rose-300">
                      <option :value="2">Hàng Nhẹ</option>
                      <option :value="5">Hàng Nặng</option>
                    </select>
                  </label>
                  <label class="space-y-1.5">
                    <span class="text-xs font-medium text-slate-500">Cân Nặng (gram)</span>
                    <input v-model.number="formGhn.weight" type="number" class="w-full rounded-[8px] border border-slate-200 bg-white px-3 py-2 text-[14px] outline-none transition focus:border-rose-300" />
                  </label>
                  <label class="space-y-1.5">
                    <span class="text-xs font-medium text-slate-500">Dài (cm)</span>
                    <input v-model.number="formGhn.length" type="number" class="w-full rounded-[8px] border border-slate-200 bg-white px-3 py-2 text-[14px] outline-none transition focus:border-rose-300" />
                  </label>
                  <label class="space-y-1.5">
                    <span class="text-xs font-medium text-slate-500">Rộng (cm)</span>
                    <input v-model.number="formGhn.width" type="number" class="w-full rounded-[8px] border border-slate-200 bg-white px-3 py-2 text-[14px] outline-none transition focus:border-rose-300" />
                  </label>
                  <label class="space-y-1.5">
                    <span class="text-xs font-medium text-slate-500">Cao (cm)</span>
                    <input v-model.number="formGhn.height" type="number" class="w-full rounded-[8px] border border-slate-200 bg-white px-3 py-2 text-[14px] outline-none transition focus:border-rose-300" />
                  </label>
                  <div class="flex items-end justify-between rounded-xl bg-white px-3 py-2 text-sm">
                    <span class="text-slate-500">Phí Hiện Tại</span>
                    <span class="font-semibold text-rose-500">{{ dinhDangTien(hoaDon.phiVanChuyen) }}</span>
                  </div>
                </div>
             </div>
          </div>
        </div>

        <div class="flex justify-end gap-3 border-t border-slate-100 px-6 py-4">
          <button @click="hienModalThongTin = false" class="rounded-full bg-slate-500 px-6 py-2.5 text-[14px] font-medium text-white transition hover:bg-slate-600">
            Hủy
          </button>
          <button @click="handleLuuThongTin" :disabled="dangCapNhat || donDaHoanThanh" class="rounded-full bg-emerald-600 px-6 py-2.5 text-[14px] font-medium text-white transition hover:bg-emerald-700 disabled:opacity-50">
            {{ dangCapNhat ? 'Đang Lưu...' : 'Lưu' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
