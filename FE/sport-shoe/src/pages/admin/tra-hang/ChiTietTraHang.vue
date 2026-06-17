<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowLeft,
  Banknote,
  CheckCircle2,
  ClipboardCheck,
  Clock3,
  History,
  Image as ImageIcon,
  PackageCheck,
  RefreshCw,
  Truck,
  UploadCloud,
  UserRound,
  X,
  XCircle,
  Trash2,
} from "lucide-vue-next";
import Badge from "../../../components/ui/Badge.vue";
import Button from "../../../components/ui/Button.vue";
import Card from "../../../components/ui/Card.vue";
import Table from "../../../components/ui/Table.vue";
import {
  batDauKiemTraHang,
  capNhatKiemTraHang,
  danhDauHoanHangThatBai,
  duyetPhieuTraHang,
  hoanTienTraHang,
  huyPhieuTraHang,
  layChiTietTraHang,
  tuChoiTraHang,
  xacNhanGuiHangTra,
  xacNhanNhanHangTra,
} from "../../../services/tra-hang";
import { API_BASE_URL, uploadFileRequest } from "../../../services/api-client";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import { showConfirm, showError, showSuccess } from "../../../utils/alert";
import { layChiTietHoaDon } from "../../../services/hoa-don";
import { layDanhSachTaiKhoanNganHang } from "../../../services/client-profile";
import { ketNoiHoaDonRealtime } from "../../../services/hoa-don-realtime";
import logoGhn from "../../../assets/logo/Logo-GHN-Blue-Orange.webp";

const route = useRoute();
const router = useRouter();
const phieu = ref(null);
const dangTai = ref(false);
const dangXuLy = ref(false);
const loiTrang = ref("");
const modal = ref("");
const hienModalLichSu = ref(false);
const formDuyet = ref({ nhanHangTrucTiep: false, ghiChu: "" });
const formVanChuyen = ref({ donViVanChuyen: "", maVanDonHoan: "", ghiChu: "" });
const formTuChoi = ref({ lyDo: "" });
const formHoanTien = ref({
  hinhThucHoan: 2,
  maGiaoDich: "",
  ghiChu: "",
  taiKhoanNganHangId: null,
});
const formKiemTra = ref({ sanPhams: [], ghiChu: "" });

const trangThai = computed(() => Number(phieu.value?.trangThai || 0));
const coTheDuyet = computed(() => trangThai.value === 1);
const coTheXacNhanGui = computed(() => trangThai.value === 2);
const coTheXacNhanNhan = computed(() => trangThai.value === 3);
const coTheBaoHoanThatBai = computed(() => trangThai.value === 3);
const coTheBatDauKiemTra = computed(() => trangThai.value === 4);
const coTheKiemTra = computed(() => trangThai.value === 5);
const coTheHoanTien = computed(() => trangThai.value === 6);
const coTheHuy = computed(() => [1, 2, 10].includes(trangThai.value));

const nhanLyDo = {
  PRODUCT_DEFECT: "Sản phẩm lỗi hoặc hỏng do nhà sản xuất",
  WRONG_SIZE: "Giao sai kích cỡ hoặc màu sắc",
  NOT_AS_DESCRIBED: "Sản phẩm không đúng mô tả hoặc hình ảnh",
  UNSATISFIED: "Không còn nhu cầu hoặc đổi ý",
  KHAC: "Lý do khác",
  KHONG_VUA: "Sản phẩm không vừa",
  GIAO_SAI: "Giao sai sản phẩm",
  HANG_LOI: "Sản phẩm bị lỗi",
};

const apiOrigin = API_BASE_URL.replace(/\/api\/v1\/?$/, "");

function hienThiLyDo(maLyDo) {
  const ma = String(maLyDo || "").trim().toUpperCase();
  return nhanLyDo[ma] || maLyDo || "Lý do khác";
}

function resolveHinhAnh(url) {
  const value = String(url || "").trim();
  if (!value) return "";
  if (/^(https?:|data:|blob:)/i.test(value)) return value;
  if (value.startsWith("/uploads/")) return `${apiOrigin}${value}`;
  if (value.startsWith("uploads/")) return `${apiOrigin}/${value}`;
  return value.startsWith("/") ? `${apiOrigin}${value}` : `${apiOrigin}/${value}`;
}

const cacBuoc = [
  { id: 1, ten: "Chờ duyệt" },
  { id: 2, ten: "Gửi hàng" },
  { id: 4, ten: "Đã nhận hàng" },
  { id: 5, ten: "Kiểm tra" },
  { id: 6, ten: "Chờ hoàn tiền" },
  { id: 7, ten: "Đã hoàn tiền" },
];

const buocHienTai = computed(() => {
  const map = { 1: 0, 2: 1, 3: 1, 4: 2, 5: 3, 6: 4, 7: 5 };
  return map[trangThai.value] ?? 0;
});

function dinhDangTien(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(Number(value || 0));
}

function dinhDangNgay(value) {
  if (!value) return "Chưa cập nhật";
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(value));
}

function badgeVariant(value) {
  if (value === 7) return "success";
  if ([8, 9, 10].includes(value)) return "danger";
  return "warning";
}

const hoaDonGoc = ref(null);

const tongTienSanPhamHoan = computed(() => {
  return phieu.value?.chiTiet?.reduce((sum, item) => sum + (Number(item.soTienHoan) || 0), 0) || 0;
});

const isLoiShop = computed(() => {
  const lyDo = String(phieu.value?.lyDoMa || "").trim().toUpperCase();
  return ["PRODUCT_DEFECT", "WRONG_SIZE", "NOT_AS_DESCRIBED", "GIAO_SAI", "HANG_LOI"].includes(lyDo);
});

const hoanPhiVanChuyen = computed(() => {
  if (!phieu.value || !hoaDonGoc.value) return 0;
  return (isLoiShop.value && tongTienSanPhamHoan.value > 0) ? (Number(hoaDonGoc.value.phiVanChuyen) || 0) : 0;
});

const coPhieuGiamGia = computed(() => {
  return hoaDonGoc.value && hoaDonGoc.value.voucher && Number(hoaDonGoc.value.giamGia || 0) > 0;
});

async function taiChiTiet(amThang = false) {
  const silent = amThang === true;
  if (!silent) dangTai.value = true;
  loiTrang.value = "";
  try {
    phieu.value = await layChiTietTraHang(route.params.id);
    if (phieu.value?.hoaDonId) {
      try {
        hoaDonGoc.value = await layChiTietHoaDon(phieu.value.hoaDonId);
      } catch (hdError) {
        console.error("Không thể tải thông tin hóa đơn gốc", hdError);
      }
    }
  } catch (error) {
    if (!silent) {
      loiTrang.value = getDisplayErrorMessage(error, "Không thể tải phiếu trả hàng");
    }
  } finally {
    if (!silent) dangTai.value = false;
  }
}

async function thucHien(noiDungThanhCong, callback) {
  if (dangXuLy.value) return;
  dangXuLy.value = true;
  try {
    phieu.value = await callback();
    modal.value = "";
    showSuccess(noiDungThanhCong);
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể cập nhật phiếu trả hàng"));
  } finally {
    dangXuLy.value = false;
  }
}

function moModalDuyet() {
  formDuyet.value = { nhanHangTrucTiep: false, ghiChu: "" };
  modal.value = "duyet";
}

function moModalGuiHang() {
  formVanChuyen.value = {
    donViVanChuyen: phieu.value?.donViVanChuyen || "",
    maVanDonHoan: phieu.value?.maVanDonHoan || "",
    ghiChu: "",
  };
  modal.value = "gui-hang";
}

function moModalKiemTra() {
  formKiemTra.value = {
    sanPhams: (phieu.value?.chiTiet || []).map((item) => ({
      chiTietTraHangId: item.id,
      soLuongNhan: item.soLuongTra,
      soLuongChapNhan: item.soLuongTra,
      tinhTrangSanPham: "",
      nhapLaiTonKho: true,
      soLuongTra: item.soLuongTra,
      tenSanPham: item.tenSanPham,
      maBienThe: item.maBienThe,
      hinhAnhs: [],
    })),
    ghiChu: "",
  };
  modal.value = "kiem-tra";
}

const kiemTraFileInput = ref(null);
const dangUploadChoId = ref(null);
const dangTaiAnhKiemTra = ref(false);

const clickTaiAnhKiemTra = (chiTietTraHangId) => {
  dangUploadChoId.value = chiTietTraHangId;
  if (kiemTraFileInput.value) {
    kiemTraFileInput.value.click();
  }
};

const handleKiemTraFileUpload = async (event) => {
  const files = event.target.files;
  if (!files || files.length === 0 || !dangUploadChoId.value) return;

  const targetItem = formKiemTra.value.sanPhams.find(
    (sp) => sp.chiTietTraHangId === dangUploadChoId.value
  );
  if (!targetItem) return;

  dangTaiAnhKiemTra.value = true;
  try {
    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      const imageUrl = await uploadFileRequest(
        file,
        "Không thể tải ảnh bằng chứng lên lúc này."
      );
      if (!targetItem.hinhAnhs) {
        targetItem.hinhAnhs = [];
      }
      targetItem.hinhAnhs.push(imageUrl);
    }
    showSuccess("Tải lên hình ảnh thành công");
  } catch (error) {
    showError(error.message || "Không thể tải lên hình ảnh. Vui lòng thử lại.");
  } finally {
    dangTaiAnhKiemTra.value = false;
    dangUploadChoId.value = null;
    if (event.target) {
      event.target.value = ""; // Reset file input
    }
  }
};

const removeKiemTraImage = (item, index) => {
  if (item.hinhAnhs) {
    item.hinhAnhs.splice(index, 1);
  }
};

const dsTaiKhoanNganHangKhach = ref([]);
const dangTaiNganHangKhach = ref(false);
const taiKhoanNganHangChon = ref(null);

const qrHoanTienUrl = computed(() => {
  if (!taiKhoanNganHangChon.value) return "";
  const bank = taiKhoanNganHangChon.value.tenNganHang;
  const account = taiKhoanNganHangChon.value.soTaiKhoan;
  const name = encodeURIComponent(taiKhoanNganHangChon.value.tenChuTaiKhoan);
  const amount = Number(phieu.value?.tongTienThucTe) || 0;
  const desc = encodeURIComponent(`HOAN TIEN TRA HANG ${phieu.value?.ma || ""}`);
  return `https://img.vietqr.io/image/${bank}-${account}-compact2.png?amount=${amount}&addInfo=${desc}&accountName=${name}`;
});

async function taiTaiKhoanNganHangKhach() {
  if (!phieu.value?.hoaDonId) return;
  dangTaiNganHangKhach.value = true;
  try {
    const hd = await layChiTietHoaDon(phieu.value.hoaDonId);
    if (hd?.khachHangId) {
      const accounts = await layDanhSachTaiKhoanNganHang(hd.khachHangId);
      dsTaiKhoanNganHangKhach.value = accounts;
      const macDinh = accounts.find(a => a.laMacDinh);
      taiKhoanNganHangChon.value = macDinh || (accounts.length > 0 ? accounts[0] : null);
    } else {
      dsTaiKhoanNganHangKhach.value = [];
      taiKhoanNganHangChon.value = null;
    }
  } catch (e) {
    console.error("Không thể tải danh sách tài khoản ngân hàng của khách", e);
  } finally {
    dangTaiNganHangKhach.value = false;
  }
}

function moModalHoanTien() {
  formHoanTien.value = {
    hinhThucHoan: phieu.value?.hinhThucHoan || 2,
    maGiaoDich: "",
    ghiChu: "Hoàn tiền theo phiếu trả hàng",
    taiKhoanNganHangId: null,
  };
  taiTaiKhoanNganHangKhach();
  modal.value = "hoan-tien";
}

async function handleHoanTien() {
  if (Number(formHoanTien.value.hinhThucHoan) === 2 && !taiKhoanNganHangChon.value?.id) {
    showError("Vui lòng chọn tài khoản ngân hàng nhận tiền của khách hàng.");
    return;
  }

  await thucHien("Hoàn tiền trả hàng thành công", () => hoanTienTraHang(phieu.value.id, {
    ...formHoanTien.value,
    taiKhoanNganHangId: Number(formHoanTien.value.hinhThucHoan) === 2
      ? taiKhoanNganHangChon.value?.id
      : null,
  }));
}

function moModalTuChoi() {
  formTuChoi.value = { lyDo: "" };
  modal.value = "tu-choi";
}

async function xacNhanNhanHang() {
  const confirmed = await showConfirm(
    "Xác nhận cửa hàng đã nhận đủ kiện hàng trả?",
    "Xác nhận nhận hàng",
    "Đã nhận hàng",
  );
  if (!confirmed) return;
  await thucHien("Đã xác nhận nhận hàng trả", () =>
    xacNhanNhanHangTra(phieu.value.id, { ghiChu: "Cửa hàng đã nhận kiện hàng trả" }),
  );
}

async function baoHoanHangThatBai() {
  const confirmed = await showConfirm(
    "Xác nhận kiện hàng trả không giao được về cửa hàng?",
    "Hoàn hàng thất bại",
    "Xác nhận thất bại",
  );
  if (!confirmed) return;
  await thucHien("Đã ghi nhận hoàn hàng thất bại", () =>
    danhDauHoanHangThatBai(phieu.value.id, {
      ghiChu: "Đơn vị vận chuyển không giao được kiện hàng trả",
    }),
  );
}

async function batDauKiemTra() {
  const confirmed = await showConfirm(
    "Bắt đầu kiểm tra tình trạng các sản phẩm khách gửi trả?",
    "Kiểm tra hàng trả",
    "Bắt đầu",
  );
  if (!confirmed) return;
  await thucHien("Đã chuyển phiếu sang kiểm tra", () =>
    batDauKiemTraHang(phieu.value.id, { ghiChu: "Bắt đầu kiểm tra hàng trả" }),
  );
}

async function huyPhieu() {
  const confirmed = await showConfirm(
    "Phiếu đã hủy sẽ không tiếp tục được xử lý.",
    "Hủy phiếu trả hàng",
    "Xác nhận hủy",
  );
  if (!confirmed) return;
  await thucHien("Đã hủy phiếu trả hàng", () =>
    huyPhieuTraHang(phieu.value.id, { ghiChu: "Nhân viên hủy phiếu trả hàng" }),
  );
}

let ngatKetNoiRealtime = null;
let realtimeRefreshTimeout = null;

onMounted(() => {
  taiChiTiet();
  ngatKetNoiRealtime = ketNoiHoaDonRealtime({
    authScope: "admin",
    onHoaDonThayDoi: (event) => {
      if (event?.loaiSuKien !== "TRA_HANG") return;
      if (Number(event?.hoaDonId) !== Number(phieu.value?.hoaDonId)) return;
      if (realtimeRefreshTimeout) window.clearTimeout(realtimeRefreshTimeout);
      realtimeRefreshTimeout = window.setTimeout(() => taiChiTiet(true), 150);
    },
  });
});

onBeforeUnmount(() => {
  ngatKetNoiRealtime?.();
  if (realtimeRefreshTimeout) window.clearTimeout(realtimeRefreshTimeout);
});
</script>

<template>
  <div class="invoice-flat space-y-4 pb-10">
    <div class="flex flex-col justify-between gap-4 lg:flex-row lg:items-start">
      <div>
        <h1 class="text-[22px] font-bold leading-tight text-slate-800 md:text-[24px]">Chi Tiết Phiếu Trả Hàng</h1>
        <div v-if="phieu" class="mt-2 space-y-1 text-[13px] text-slate-500">
          <p>
            Mã Phiếu: <span class="font-semibold text-slate-700">{{ phieu.ma }}</span>
            <span class="mx-2 text-slate-300">|</span>
            Hóa Đơn: <span class="font-semibold text-slate-700">{{ phieu.maHoaDon }}</span>
          </p>
          <p>
            Khách Hàng:
            <span class="font-medium text-slate-700">{{ phieu.tenKhachHang || "Khách vãng lai" }}</span>
            <span class="mx-2 text-slate-300">|</span>
            Cập Nhật Gần Nhất:
            <span class="font-medium text-slate-700">{{ dinhDangNgay(phieu.ngayCapNhat) }}</span>
          </p>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <Button variant="soft" :loading="dangTai" class="h-10 px-3" title="Làm mới" @click="taiChiTiet">
          <RefreshCw class="h-4 w-4" />
        </Button>
        <Button variant="soft" class="h-10" @click="router.push({ name: 'admin-tra-hang' })">
          <template #prefix><ArrowLeft class="h-4 w-4" /></template>
          Quay Lại Danh Sách
        </Button>
      </div>
    </div>

    <Card v-if="dangTai">
      <div class="flex min-h-64 items-center justify-center text-sm text-slate-400">
        Đang tải chi tiết phiếu trả hàng...
      </div>
    </Card>

    <Card v-else-if="loiTrang || !phieu">
      <div class="flex min-h-64 flex-col items-center justify-center gap-4 text-center">
        <XCircle class="h-10 w-10 text-rose-400" />
        <p class="font-medium text-rose-600">{{ loiTrang || "Không tìm thấy phiếu trả hàng" }}</p>
        <Button variant="soft" @click="taiChiTiet">Thử lại</Button>
      </div>
    </Card>

    <template v-else>
      <section class="grid items-stretch gap-3 xl:grid-cols-[1fr_1fr_0.95fr]">
      <Card class="flex h-full flex-col px-4 py-4 md:px-5 xl:col-span-2">
        <template #header>
          <div class="flex w-full flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div class="flex items-center gap-3">
              <PackageCheck class="h-4.5 w-4.5 text-slate-500" />
              <h2 class="text-[15px] font-semibold text-slate-700">Trạng thái phiếu trả hàng</h2>
            </div>
            <Badge :variant="badgeVariant(phieu.trangThai)">{{ phieu.tenTrangThai }}</Badge>
          </div>
        </template>

        <div v-if="[8, 9, 10].includes(trangThai)" class="rounded-[6px] border border-rose-100 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {{ phieu.lyDoTuChoi || `Phiếu đang ở trạng thái ${phieu.tenTrangThai.toLowerCase()}.` }}
        </div>
        <div v-else class="relative mt-7 grid grid-cols-2 gap-4 px-2 pt-2 md:grid-cols-6">
          <div class="absolute left-[8%] right-[8%] top-9 hidden h-0.5 bg-slate-200 md:block"></div>
          <div
            v-for="(buoc, index) in cacBuoc"
            :key="buoc.id"
            class="relative z-10 flex flex-col items-center text-center"
          >
            <div
              class="flex h-[58px] w-[58px] items-center justify-center rounded-full border-[2.5px] transition"
              :class="index <= buocHienTai ? 'border-primary bg-primary text-white shadow-[0_8px_20px_-8px_rgba(220,38,38,0.65)]' : 'border-slate-200 bg-white text-slate-300'"
            >
              <CheckCircle2 v-if="index < buocHienTai" class="h-5 w-5" />
              <Clock3 v-else class="h-5 w-5" />
            </div>
            <p
              class="mt-3 whitespace-nowrap text-[12px] font-semibold"
              :class="index <= buocHienTai ? 'text-primary' : 'text-slate-400'"
            >
              {{ buoc.ten }}
            </p>
          </div>
        </div>

        <div class="mt-5 flex justify-end">
          <Button variant="primary" @click="hienModalLichSu = true">
            <template #prefix>
              <History class="h-4 w-4" />
            </template>
            Lịch Sử Thao Tác
          </Button>
        </div>
      </Card>

        <Card class="flex h-full flex-col px-5 py-4">
          <template #header>
            <div class="flex items-center gap-2">
              <Banknote class="h-4.5 w-4.5 text-slate-500" />
              <h2 class="text-[15px] font-semibold text-slate-700">Tổng Kết Hoàn Tiền</h2>
            </div>
          </template>
          <div class="mt-4 flex-1 space-y-4 text-sm">
            <div class="flex items-center justify-between">
              <span class="text-slate-500">Tiền Hoàn Dự Kiến</span>
              <span class="font-semibold text-slate-700">{{ dinhDangTien(phieu.tongTienDuKien) }}</span>
            </div>

            <!-- Chi tiết tính toán số tiền hoàn -->
            <div class="border-t border-slate-100 pt-3 space-y-2">
              <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Chi tiết tiền hoàn</p>
              
              <div class="flex items-center justify-between text-slate-600 pl-1">
                <span>Tiền sản phẩm hoàn trả</span>
                <span>{{ dinhDangTien(tongTienSanPhamHoan) }}</span>
              </div>

              <div v-if="hoaDonGoc && hoanPhiVanChuyen > 0" class="flex items-center justify-between text-slate-600 pl-1">
                <span class="flex items-center gap-1.5">
                  Phí vận chuyển gốc được hoàn
                  <img :src="logoGhn" alt="GHN" class="h-3.5 w-auto object-contain" />
                  <span class="relative group inline-flex items-center cursor-help text-slate-400 hover:text-slate-600 transition">
                    <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-3.5 w-3.5"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                    <span class="absolute bottom-full left-1/2 z-50 mb-2 w-56 -translate-x-1/2 rounded-xl bg-slate-800 p-2.5 text-center text-[10px] font-normal leading-normal text-white shadow-lg transition-all duration-75 opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto">
                      Hoàn phí ship GHN do lỗi của shop: {{ hienThiLyDo(phieu.lyDoMa) }}
                    </span>
                  </span>
                </span>
                <span class="text-slate-700 font-semibold">
                  +{{ dinhDangTien(hoanPhiVanChuyen) }}
                </span>
              </div>

              <div v-if="coPhieuGiamGia" class="bg-slate-50 rounded-xl p-2.5 mt-2 space-y-1">
                <div class="flex items-center justify-between text-[11px] text-slate-500">
                  <span>Mã giảm giá đã dùng</span>
                  <span class="font-bold text-slate-700">{{ hoaDonGoc.voucher }}</span>
                </div>
                <div class="flex items-center justify-between text-[11px] text-slate-500">
                  <span class="flex items-center gap-1">
                    Tiền voucher giảm
                    <span class="relative group inline-flex items-center cursor-help text-slate-400 hover:text-slate-600 transition">
                      <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-3.5 w-3.5"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                      <span class="absolute bottom-full left-1/2 z-50 mb-2 w-56 -translate-x-1/2 rounded-xl bg-slate-800 p-2.5 text-center text-[10px] font-normal leading-normal text-white shadow-lg transition-all duration-75 opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto">
                        Số tiền hoàn của sản phẩm đã được tự động khấu trừ theo tỷ lệ áp dụng voucher của đơn hàng gốc.
                      </span>
                    </span>
                  </span>
                  <span class="text-emerald-600 font-semibold">-{{ dinhDangTien(hoaDonGoc.giamGia) }}</span>
                </div>
              </div>
            </div>

            <div class="border-t border-slate-200 pt-4">
              <div class="flex items-center justify-between">
                <span class="text-[15px] font-bold text-slate-800">Tiền Hoàn Được Duyệt</span>
                <span class="text-lg font-bold text-emerald-600">{{ dinhDangTien(phieu.tongTienThucTe) }}</span>
              </div>
            </div>
            <div class="border-t border-slate-100 pt-3">
              <div class="flex items-center justify-between">
                <span class="font-semibold text-slate-700">Trạng Thái</span>
                <Badge :variant="badgeVariant(phieu.trangThai)">{{ phieu.tenTrangThai }}</Badge>
              </div>
            </div>
          </div>
        </Card>
      </section>

      <section class="grid items-start gap-3 xl:grid-cols-[1fr_1fr_0.95fr]">
        <Card class="px-5 py-4 xl:col-span-2">
          <template #header>
            <div class="flex items-center gap-2">
              <ClipboardCheck class="h-4.5 w-4.5 text-slate-500" />
              <h2 class="text-[15px] font-semibold text-slate-700">Thông Tin Yêu Cầu</h2>
            </div>
          </template>

          <dl class="grid gap-x-8 gap-y-5 text-sm sm:grid-cols-2">
            <div>
              <dt class="text-slate-400">Khách hàng</dt>
              <dd class="mt-1 font-semibold text-slate-700">{{ phieu.tenKhachHang || "Khách vãng lai" }}</dd>
            </div>
            <div>
              <dt class="text-slate-400">Số điện thoại</dt>
              <dd class="mt-1 font-semibold text-slate-700">{{ phieu.soDienThoaiKhachHang || "Không có" }}</dd>
            </div>
            <div>
              <dt class="text-slate-400">Lý do</dt>
              <dd class="mt-1 font-semibold text-slate-700">{{ hienThiLyDo(phieu.lyDoMa) }}</dd>
            </div>
            <div>
              <dt class="text-slate-400">Nhân viên xử lý</dt>
              <dd class="mt-1 font-semibold text-slate-700">{{ phieu.maNhanVien || "Chưa phân công" }}</dd>
            </div>
            <div v-if="phieu.donViVanChuyen">
              <dt class="text-slate-400">Vận chuyển hoàn</dt>
              <dd class="mt-1 font-semibold text-slate-700">
                {{ phieu.donViVanChuyen }} · {{ phieu.maVanDonHoan }}
              </dd>
            </div>
            <div class="sm:col-span-2">
              <dt class="text-slate-400">Mô tả</dt>
              <dd class="mt-1 leading-6 text-slate-700">{{ phieu.moTa || "Không có mô tả bổ sung" }}</dd>
            </div>
          </dl>

          <div class="mt-6 border-t border-slate-100 pt-5">
            <div class="mb-3 flex items-center gap-2">
              <ImageIcon class="h-4.5 w-4.5 text-primary" />
              <h3 class="text-sm font-semibold text-slate-700">Hình ảnh minh chứng</h3>
              <span
                v-if="phieu.hinhAnhs?.length"
                class="rounded-full bg-rose-50 px-2 py-0.5 text-[11px] font-semibold text-primary"
              >
                {{ phieu.hinhAnhs.length }} ảnh
              </span>
            </div>

            <div v-if="phieu.hinhAnhs?.length" class="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
              <a
                v-for="(url, index) in phieu.hinhAnhs"
                :key="`${url}-${index}`"
                :href="resolveHinhAnh(url)"
                target="_blank"
                rel="noopener noreferrer"
                class="group relative aspect-square overflow-hidden rounded-[6px] border border-slate-200 bg-slate-50"
                :title="`Xem ảnh minh chứng ${index + 1}`"
              >
                <img
                  :src="resolveHinhAnh(url)"
                  :alt="`Ảnh minh chứng trả hàng ${index + 1}`"
                  class="h-full w-full object-cover transition duration-300 group-hover:scale-105"
                />
                <span class="absolute bottom-2 left-2 rounded-md bg-slate-900/65 px-2 py-1 text-[10px] font-medium text-white">
                  Ảnh {{ index + 1 }}
                </span>
              </a>
            </div>

            <div
              v-else
              class="flex items-center gap-2 rounded-[6px] border border-dashed border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-400"
            >
              <ImageIcon class="h-4 w-4" />
              Khách hàng chưa cung cấp hình ảnh minh chứng.
            </div>
          </div>
        </Card>

        <Card class="flex flex-col px-5 py-4">
          <template #header>
            <div class="flex items-center gap-2">
              <UserRound class="h-4.5 w-4.5 text-slate-500" />
              <h2 class="text-[15px] font-semibold text-slate-700">Thao Tác Tiếp Theo</h2>
            </div>
          </template>
          <div class="grid flex-1 content-start gap-3">
            <template v-if="coTheDuyet">
              <Button full-width @click="moModalDuyet">Duyệt Phiếu Trả Hàng</Button>
              <Button variant="soft" full-width @click="moModalTuChoi">Từ Chối Yêu Cầu</Button>
            </template>
            <Button v-if="coTheXacNhanGui" full-width @click="moModalGuiHang">
              <template #prefix><Truck class="h-4 w-4" /></template>
              Xác Nhận Khách Đã Gửi Hàng
            </Button>
            <Button v-if="coTheXacNhanNhan" full-width @click="xacNhanNhanHang">
              Xác Nhận Đã Nhận Hàng
            </Button>
            <Button v-if="coTheBaoHoanThatBai" variant="soft" full-width @click="baoHoanHangThatBai">
              Báo Hoàn Hàng Thất Bại
            </Button>
            <Button v-if="coTheBatDauKiemTra" full-width @click="batDauKiemTra">
              Bắt Đầu Kiểm Tra
            </Button>
            <Button v-if="coTheKiemTra" full-width @click="moModalKiemTra">
              Nhập Kết Quả Kiểm Tra
            </Button>
            <Button v-if="coTheHoanTien" full-width @click="moModalHoanTien">
              <template #prefix><Banknote class="h-4 w-4" /></template>
              Xác Nhận Hoàn Tiền
            </Button>
            <Button v-if="coTheHuy" variant="outline" full-width @click="huyPhieu">
              Hủy Phiếu Trả Hàng
            </Button>
            <div
              v-if="![1, 2, 3, 4, 5, 6, 10].includes(trangThai)"
              class="rounded-[6px] bg-slate-50 px-4 py-5 text-center text-sm text-slate-500"
            >
              Phiếu đã kết thúc, không còn thao tác cần xử lý.
            </div>
          </div>
        </Card>
      </section>

      <Card class="px-5 py-4">
        <template #header>
          <div class="flex items-center gap-3">
            <PackageCheck class="h-4.5 w-4.5 text-slate-500" />
            <div>
              <h2 class="text-[15px] font-semibold text-slate-700">Danh Sách Sản Phẩm Trả</h2>
              <p class="mt-1 text-xs text-slate-400">{{ phieu.chiTiet?.length || 0 }} dòng sản phẩm</p>
            </div>
          </div>
        </template>
        <Table>
          <template #header>
            <th class="px-4 py-3 text-center">STT</th>
            <th class="px-4 py-3">Sản phẩm</th>
            <th class="px-4 py-3">Biến thể</th>
            <th class="px-4 py-3 text-center">Yêu cầu trả</th>
            <th class="px-4 py-3 text-center">Chấp nhận</th>
            <th class="px-4 py-3 text-right">Tiền hoàn</th>
            <th class="px-4 py-3">Tình trạng</th>
          </template>
          <template #body>
            <tr v-for="(item, index) in phieu.chiTiet" :key="item.id">
              <td class="px-4 py-4 text-center text-slate-500">{{ index + 1 }}</td>
              <td class="px-4 py-4">
                <p class="font-semibold text-slate-800">{{ item.tenSanPham || "Sản phẩm" }}</p>
                <p class="mt-1 text-xs text-slate-400">{{ item.maBienThe }}</p>
              </td>
              <td class="px-4 py-4 text-slate-600">{{ item.mauSac || "—" }} / {{ item.kichCo || "—" }}</td>
              <td class="px-4 py-4 text-center font-semibold">{{ item.soLuongTra }}</td>
              <td class="px-4 py-4 text-center font-semibold text-emerald-600">{{ item.soLuongChapNhan }}</td>
              <td class="px-4 py-4 text-right font-semibold text-primary">{{ dinhDangTien(item.soTienHoan) }}</td>
              <td class="px-4 py-4 text-slate-500">{{ item.tinhTrangSanPham || "Chưa kiểm tra" }}</td>
            </tr>
          </template>
        </Table>
      </Card>

    </template>

    <div
      v-if="hienModalLichSu"
      class="fixed inset-0 z-[90] flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
      @click.self="hienModalLichSu = false"
    >
      <div class="w-full max-w-2xl overflow-hidden rounded-[24px] bg-white shadow-2xl">
        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
          <div class="flex items-center gap-3">
            <History class="h-5 w-5 text-slate-500" />
            <h3 class="text-[17px] font-bold text-slate-800">Lịch sử thao tác</h3>
          </div>
          <button
            type="button"
            class="text-slate-400 transition hover:text-slate-600"
            @click="hienModalLichSu = false"
          >
            <X class="h-5 w-5" />
          </button>
        </div>

        <div class="max-h-[70vh] overflow-y-auto px-6 py-8">
          <div v-if="!phieu?.lichSu?.length" class="py-10 text-center text-sm text-slate-400">
            Chưa có lịch sử thao tác.
          </div>
          <div v-else class="relative pl-8">
            <div class="absolute bottom-0 left-[3.5px] top-0 w-[1.5px] bg-[#B82220]/20"></div>

            <div class="space-y-6">
              <div
                v-for="(item, index) in phieu.lichSu"
                :key="item.id || index"
                class="relative"
              >
                <div
                  class="absolute -left-[32px] top-4 h-2 w-2 rounded-full border-2 border-white bg-[#B82220] shadow-[0_0_0_2px_rgba(184,34,32,0.15)]"
                ></div>

                <div class="rounded-[6px] border border-slate-50 bg-slate-50/50 p-4 transition-colors hover:bg-slate-100/50">
                  <div class="text-[12px] font-medium text-slate-400">
                    {{ dinhDangNgay(item.ngayTao) }}
                  </div>
                  <div class="mt-1 text-[13px] font-semibold text-slate-400">
                    {{ item.maNhanVien || "Hệ thống" }} · {{ item.tenTrangThaiMoi }}
                  </div>
                  <p class="mt-2 text-[15px] font-bold text-slate-800">{{ item.hanhDong }}</p>
                  <p v-if="item.ghiChu" class="mt-2 text-[13px] italic text-slate-500">{{ item.ghiChu }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="modal"
      class="fixed inset-0 z-[80] flex items-center justify-center bg-slate-900/45 p-4 backdrop-blur-sm"
      @click.self="modal = ''"
    >
      <div class="max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-[6px] border border-rose-100 bg-white shadow-2xl">
        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
          <h3 class="text-lg font-bold text-slate-800">
            {{
              {
                duyet: "Duyệt phiếu trả hàng",
                "gui-hang": "Thông tin vận chuyển hoàn",
                "kiem-tra": "Kiểm tra sản phẩm trả",
                "hoan-tien": "Xác nhận hoàn tiền",
                "tu-choi": "Từ chối phiếu trả hàng",
              }[modal]
            }}
          </h3>
          <button class="rounded-full p-2 text-slate-400 hover:bg-slate-100" @click="modal = ''">
            <X class="h-5 w-5" />
          </button>
        </div>

        <div class="space-y-5 px-6 py-5">
          <template v-if="modal === 'duyet'">
            <div class="grid gap-3 sm:grid-cols-2">
              <button
                type="button"
                class="rounded-[6px] border p-4 text-left transition"
                :class="!formDuyet.nhanHangTrucTiep ? 'border-primary bg-rose-50' : 'border-slate-200'"
                @click="formDuyet.nhanHangTrucTiep = false"
              >
                <Truck class="h-5 w-5 text-primary" />
                <p class="mt-3 font-semibold text-slate-800">Khách gửi hàng về</p>
                <p class="mt-1 text-xs leading-5 text-slate-500">Chờ khách cung cấp kiện hàng và mã vận đơn.</p>
              </button>
              <button
                type="button"
                class="rounded-[6px] border p-4 text-left transition"
                :class="formDuyet.nhanHangTrucTiep ? 'border-primary bg-rose-50' : 'border-slate-200'"
                @click="formDuyet.nhanHangTrucTiep = true"
              >
                <PackageCheck class="h-5 w-5 text-primary" />
                <p class="mt-3 font-semibold text-slate-800">Đã nhận tại cửa hàng</p>
                <p class="mt-1 text-xs leading-5 text-slate-500">Bỏ qua bước vận chuyển và chuyển sang nhận hàng.</p>
              </button>
            </div>
            <textarea
              v-model="formDuyet.ghiChu"
              rows="3"
              class="w-full rounded-[6px] border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
              placeholder="Ghi chú duyệt phiếu..."
            ></textarea>
            <Button
              full-width
              :loading="dangXuLy"
              @click="thucHien('Duyệt phiếu trả hàng thành công', () => duyetPhieuTraHang(phieu.id, formDuyet))"
            >
              Xác nhận duyệt
            </Button>
          </template>

          <template v-else-if="modal === 'gui-hang'">
            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-600">Đơn vị vận chuyển</span>
              <input v-model="formVanChuyen.donViVanChuyen" class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300" placeholder="VD: GHN, GHTK..." />
            </label>
            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-600">Mã vận đơn hoàn</span>
              <input v-model="formVanChuyen.maVanDonHoan" class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300" placeholder="Nhập mã vận đơn..." />
            </label>
            <textarea v-model="formVanChuyen.ghiChu" rows="3" class="w-full rounded-[6px] border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300" placeholder="Ghi chú..."></textarea>
            <Button
              full-width
              :loading="dangXuLy"
              @click="thucHien('Đã xác nhận khách gửi hàng', () => xacNhanGuiHangTra(phieu.id, formVanChuyen))"
            >
              Xác nhận gửi hàng
            </Button>
          </template>

          <template v-else-if="modal === 'kiem-tra'">
            <div
              v-for="item in formKiemTra.sanPhams"
              :key="item.chiTietTraHangId"
              class="rounded-[6px] border border-slate-200 p-4"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="font-semibold text-slate-800">{{ item.tenSanPham || "Sản phẩm" }}</p>
                  <p class="mt-1 text-xs text-slate-400">{{ item.maBienThe }} · Yêu cầu trả {{ item.soLuongTra }}</p>
                </div>
                <label class="flex items-center gap-2 text-sm text-slate-600">
                  <input v-model="item.nhapLaiTonKho" type="checkbox" class="h-4 w-4 accent-red-600" />
                  Nhập lại tồn
                </label>
              </div>
              <div class="mt-4 grid gap-3 sm:grid-cols-2">
                <label class="space-y-2">
                  <span class="text-xs font-semibold text-slate-500">Số lượng đã nhận</span>
                  <input v-model.number="item.soLuongNhan" type="number" min="0" :max="item.soLuongTra" class="h-10 w-full rounded-[6px] border border-slate-200 px-3 outline-none focus:border-rose-300" />
                </label>
                <label class="space-y-2">
                  <span class="text-xs font-semibold text-slate-500">Số lượng chấp nhận</span>
                  <input v-model.number="item.soLuongChapNhan" type="number" min="0" :max="item.soLuongNhan" class="h-10 w-full rounded-[6px] border border-slate-200 px-3 outline-none focus:border-rose-300" />
                </label>
              </div>
              <input v-model="item.tinhTrangSanPham" class="mt-3 h-10 w-full rounded-[6px] border border-slate-200 px-3 text-sm outline-none focus:border-rose-300" placeholder="Tình trạng sản phẩm..." />
              
              <!-- Tải ảnh minh chứng trong bước kiểm tra -->
              <div class="mt-4 space-y-2">
                <span class="text-xs font-semibold text-slate-500 block text-left">Hình ảnh thực tế sản phẩm (Không bắt buộc)</span>
                <div class="flex flex-wrap gap-2 items-center">
                  <!-- Button tải ảnh -->
                  <button
                    type="button"
                    @click="clickTaiAnhKiemTra(item.chiTietTraHangId)"
                    :disabled="dangTaiAnhKiemTra"
                    class="w-16 h-16 rounded-[6px] border border-dashed border-slate-300 hover:border-rose-400 bg-slate-50 flex flex-col items-center justify-center text-slate-400 hover:text-rose-500 transition group"
                  >
                    <UploadCloud class="w-5 h-5 group-hover:scale-110 transition duration-300" />
                    <span class="text-[9px] font-semibold mt-1">{{ (dangTaiAnhKiemTra && dangUploadChoId === item.chiTietTraHangId) ? "..." : "Tải ảnh" }}</span>
                  </button>

                  <!-- Preview danh sách ảnh -->
                  <div
                    v-for="(url, idx) in item.hinhAnhs"
                    :key="idx"
                    class="relative w-16 h-16 rounded-[6px] border border-slate-200 overflow-hidden bg-slate-100 flex-shrink-0 group"
                  >
                    <img :src="resolveHinhAnh(url)" alt="Preview" class="w-full h-full object-cover" />
                    <button
                      type="button"
                      @click="removeKiemTraImage(item, idx)"
                      class="absolute inset-0 bg-black/40 flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition duration-300"
                    >
                      <Trash2 class="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            <textarea v-model="formKiemTra.ghiChu" rows="3" class="w-full rounded-[6px] border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300" placeholder="Kết luận kiểm tra..."></textarea>
            
            <input
              type="file"
              multiple
              ref="kiemTraFileInput"
              class="hidden"
              accept="image/*"
              @change="handleKiemTraFileUpload"
            />


            <Button
              full-width
              :loading="dangXuLy"
              @click="thucHien('Đã lưu kết quả kiểm tra', () => capNhatKiemTraHang(phieu.id, {
                sanPhams: formKiemTra.sanPhams.map(({ soLuongTra, tenSanPham, maBienThe, ...item }) => item),
                ghiChu: formKiemTra.ghiChu,
              }))"
            >
              Lưu kết quả kiểm tra
            </Button>
          </template>

          <template v-else-if="modal === 'hoan-tien'">
            <div class="rounded-[6px] bg-rose-50 px-5 py-4">
              <p class="text-sm text-rose-600 font-semibold">Số tiền cần hoàn</p>
              <p class="mt-1 text-2xl font-bold text-primary">{{ dinhDangTien(phieu.tongTienThucTe) }}</p>
            </div>

            <!-- Chi tiết phân tích số tiền hoàn trong modal -->
            <div class="rounded-2xl border border-slate-100 bg-slate-50/50 p-4 space-y-2 text-xs">
              <p class="font-bold text-slate-500 uppercase tracking-wider">Chi tiết cách tính tiền hoàn</p>
              
              <div class="flex items-center justify-between text-slate-600">
                <span>Tiền sản phẩm hoàn trả</span>
                <span class="font-semibold">{{ dinhDangTien(tongTienSanPhamHoan) }}</span>
              </div>

              <div v-if="hoaDonGoc && hoanPhiVanChuyen > 0" class="flex items-center justify-between text-slate-600">
                <span class="flex items-center gap-1.5">
                  Phí vận chuyển gốc được hoàn
                  <img :src="logoGhn" alt="GHN" class="h-3.5 w-auto object-contain" />
                  <span class="relative group inline-flex items-center cursor-help text-slate-400 hover:text-slate-600 transition">
                    <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-3.5 w-3.5"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                    <span class="absolute bottom-full left-1/2 z-50 mb-2 w-56 -translate-x-1/2 rounded-xl bg-slate-800 p-2.5 text-center text-[10px] font-normal leading-normal text-white shadow-lg transition-all duration-75 opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto">
                      Hoàn phí ship GHN do lỗi của shop: {{ hienThiLyDo(phieu.lyDoMa) }}
                    </span>
                  </span>
                </span>
                <span class="text-slate-700 font-semibold">
                  +{{ dinhDangTien(hoanPhiVanChuyen) }}
                </span>
              </div>

              <div v-if="coPhieuGiamGia" class="bg-white rounded-xl p-2.5 mt-2 space-y-1 border border-slate-100">
                <div class="flex items-center justify-between text-[11px] text-slate-500">
                  <span>Mã giảm giá đơn hàng</span>
                  <span class="font-bold text-slate-700">{{ hoaDonGoc.voucher }}</span>
                </div>
                <div class="flex items-center justify-between text-[11px] text-slate-500">
                  <span class="flex items-center gap-1">
                    Tiền giảm giá voucher gốc
                    <span class="relative group inline-flex items-center cursor-help text-slate-400 hover:text-slate-600 transition">
                      <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-3.5 w-3.5"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                      <span class="absolute bottom-full left-1/2 z-50 mb-2 w-56 -translate-x-1/2 rounded-xl bg-slate-800 p-2.5 text-center text-[10px] font-normal leading-normal text-white shadow-lg transition-all duration-75 opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto">
                        Số tiền hoàn của sản phẩm đã được tự động khấu trừ theo tỷ lệ áp dụng voucher của đơn hàng gốc.
                      </span>
                    </span>
                  </span>
                  <span class="text-emerald-600 font-semibold">-{{ dinhDangTien(hoaDonGoc.giamGia) }}</span>
                </div>
              </div>
            </div>
            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-600">Hình thức hoàn tiền</span>
              <select v-model.number="formHoanTien.hinhThucHoan" class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300">
                <option :value="1">Tiền mặt</option>
                <option :value="2">Chuyển khoản</option>
                <option :value="3">Ví điện tử</option>
              </select>
            </label>

            <!-- Customer Bank Account Selection & VietQR Code -->
            <div v-if="formHoanTien.hinhThucHoan === 2" class="space-y-4 pt-2">
              <div class="space-y-2">
                <span class="text-sm font-semibold text-slate-600">Tài khoản ngân hàng nhận tiền của khách</span>
                <div v-if="dangTaiNganHangKhach" class="text-xs text-slate-400">Đang tải danh sách tài khoản...</div>
                <select v-else-if="dsTaiKhoanNganHangKhach.length > 0" v-model="taiKhoanNganHangChon"
                  class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 outline-none transition focus:border-rose-300">
                  <option v-for="tk in dsTaiKhoanNganHangKhach" :key="tk.id" :value="tk">
                    {{ tk.tenNganHang }} - {{ tk.soTaiKhoan }} ({{ tk.tenChuTaiKhoan }}) {{ tk.laMacDinh ? '[Mặc định]' : '' }}
                  </option>
                </select>
                <div v-else class="rounded-[6px] border border-rose-100 bg-rose-50/50 px-4 py-3 text-xs font-semibold text-rose-700">
                  Khách hàng chưa liên kết tài khoản ngân hàng nào.
                </div>
              </div>

              <!-- VietQR Code Image -->
              <div v-if="taiKhoanNganHangChon" class="flex flex-col items-center justify-center border border-slate-100 rounded-[6px] p-5 bg-slate-50/80 gap-3">
                <span class="text-xs font-bold text-slate-500 uppercase tracking-wider">Quét mã VietQR để chuyển tiền</span>
                <div class="h-44 w-44 overflow-hidden rounded-[6px] border border-slate-200 bg-white p-2 flex items-center justify-center shadow-sm">
                  <img :src="qrHoanTienUrl" alt="VietQR Hoàn Tiền" class="h-full w-full object-contain" />
                </div>
                <div class="text-center space-y-0.5">
                  <p class="text-xs font-bold text-slate-700">Chủ TK: <span class="uppercase text-[#B82220]">{{ taiKhoanNganHangChon.tenChuTaiKhoan }}</span></p>
                  <p class="text-xs font-semibold text-slate-500">STK: {{ taiKhoanNganHangChon.soTaiKhoan }} ({{ taiKhoanNganHangChon.tenNganHang }})</p>
                </div>
              </div>
            </div>
            <label class="block space-y-2">
              <span class="text-sm font-semibold text-slate-600">Mã giao dịch</span>
              <input v-model="formHoanTien.maGiaoDich" class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300" placeholder="Để trống nếu hoàn tiền mặt" />
            </label>
            <textarea v-model="formHoanTien.ghiChu" rows="3" class="w-full rounded-[6px] border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300"></textarea>
            <Button
              full-width
              :loading="dangXuLy"
              :disabled="formHoanTien.hinhThucHoan === 2 && !taiKhoanNganHangChon"
              @click="handleHoanTien"
            >
              Xác nhận đã hoàn tiền
            </Button>
          </template>

          <template v-else-if="modal === 'tu-choi'">
            <textarea v-model="formTuChoi.lyDo" rows="5" class="w-full rounded-[6px] border border-slate-200 bg-slate-50 p-4 text-sm outline-none focus:border-rose-300" placeholder="Nhập lý do từ chối..."></textarea>
            <Button
              variant="danger"
              full-width
              :loading="dangXuLy"
              @click="thucHien('Đã từ chối phiếu trả hàng', () => tuChoiTraHang(phieu.id, formTuChoi))"
            >
              Xác nhận từ chối
            </Button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.invoice-flat :deep([class*="rounded-"]:not(.rounded-full)) {
  border-radius: 6px !important;
}
  </style>
