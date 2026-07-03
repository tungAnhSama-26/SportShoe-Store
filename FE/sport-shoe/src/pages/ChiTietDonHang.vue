<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  capNhatThongTinGiaoHang,
  layChiTietDonHang,
  xacNhanDaNhanHang,
  yeuCauHuyDonHang,
} from '../services/don-hang';
import { layDanhSachDiaChiProfile } from '../services/client-profile';
import { layKhachId } from '../services/gio-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showSuccess, showError, showConfirm } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import {
  CAC_BUOC_DON_HANG,
  layCauHinhTrangThaiDonHang,
  layViTriTienTrinhDonHang,
} from '../utils/order-status';
import anhMacDinh from '../assets/login-shoe.png';
import YeuCauTraHangModal from '../components/common/YeuCauTraHangModal.vue';
import ChinhSuaGiaoHangModal from '../components/common/ChinhSuaGiaoHangModal.vue';
import {
  ketNoiHoaDonRealtime,
  langNgheHoaDonThayDoiNoiBo,
} from '../services/hoa-don-realtime';
import logoGhn from '../assets/logo/Logo-GHN-Blue-Orange.webp';
import { API_BASE_URL } from '../services/api-client';

const apiOrigin = API_BASE_URL.replace(/\/api\/v1\/?$/, "");

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

const route = useRoute();
const router = useRouter();
const REALTIME_POLL_INTERVAL_MS = 3000;

const don = ref(null);
const dangTai = ref(true);
const loi = ref('');
const hienModalTraHang = ref(false);
const hienModalGiaoHang = ref(false);
const dangLuuGiaoHang = ref(false);
const diaChiDaLuu = ref([]);
const SO_NGAY_DUOC_GUI_YEU_CAU_TRA_HANG = 3;

const daQuaHanTraHang = computed(() => {
  if (!don.value) return true;
  const mocTime = don.value.ngayGiao ? new Date(don.value.ngayGiao).getTime() : (don.value.ngayCapNhat ? new Date(don.value.ngayCapNhat).getTime() : new Date().getTime());
  const bayGio = new Date().getTime();
  const thoiHanTraHangMs = SO_NGAY_DUOC_GUI_YEU_CAU_TRA_HANG * 24 * 60 * 60 * 1000;
  return (bayGio - mocTime) > thoiHanTraHangMs;
});

function lopBadgeTraHang(tt) {
  switch (tt) {
    case 1: return 'bg-amber-50 text-amber-600 border border-amber-100';
    case 2: return 'bg-blue-50 text-blue-600 border border-blue-100';
    case 3: return 'bg-violet-50 text-violet-600 border border-violet-100';
    case 4: return 'bg-cyan-50 text-cyan-600 border border-cyan-100';
    case 5: return 'bg-purple-50 text-purple-600 border border-purple-100';
    case 6: return 'bg-rose-50 text-rose-600 border border-rose-100';
    case 7: return 'bg-emerald-50 text-emerald-600 border border-emerald-100';
    case 8: return 'bg-stone-100 text-stone-600 border border-stone-200';
    case 9: return 'bg-slate-100 text-slate-500 border border-slate-200';
    case 10: return 'bg-rose-50 text-rose-600 border border-rose-100';
    default: return 'bg-slate-100 text-slate-600';
  }
}

let ngatKetNoiRealtime = null;
let ngatKetNoiRealtimeNoiBo = null;
let realtimeRefreshTimeout = null;
let realtimePollingInterval = null;

function laSuKienCuaDonHienTai(event) {
  const idHienTai = [route.params.id, don.value?.id, don.value?.hoaDonId]
    .filter((value) => value !== undefined && value !== null && value !== "")
    .map((value) => Number(value));
  const idSuKien = [event?.hoaDonId, event?.id]
    .filter((value) => value !== undefined && value !== null && value !== "")
    .map((value) => Number(value));

  if (idSuKien.some((id) => idHienTai.includes(id))) return true;

  const maHienTai = [don.value?.ma, don.value?.maHoaDon]
    .filter(Boolean)
    .map((value) => String(value).trim().toLowerCase());
  const maSuKien = [event?.maHoaDon, event?.ma]
    .filter(Boolean)
    .map((value) => String(value).trim().toLowerCase());

  return maSuKien.some((ma) => maHienTai.includes(ma));
}

function lenLichTaiLaiChiTiet() {
  if (realtimeRefreshTimeout) clearTimeout(realtimeRefreshTimeout);
  realtimeRefreshTimeout = setTimeout(() => taiChiTiet(true), 150);
}

function dongBoKhiQuayLaiTrang() {
  if (document.visibilityState === 'visible') {
    lenLichTaiLaiChiTiet();
  }
}

function batDauDongBoDinhKy() {
  if (realtimePollingInterval) return;
  realtimePollingInterval = window.setInterval(() => {
    taiChiTiet(true);
  }, REALTIME_POLL_INTERVAL_MS);
}

onMounted(() => {
  taiChiTiet();
  batDauDongBoDinhKy();
  window.addEventListener('focus', lenLichTaiLaiChiTiet);
  window.addEventListener('pageshow', lenLichTaiLaiChiTiet);
  document.addEventListener('visibilitychange', dongBoKhiQuayLaiTrang);
  ngatKetNoiRealtimeNoiBo = langNgheHoaDonThayDoiNoiBo((event) => {
    if (!laSuKienCuaDonHienTai(event)) return;
    lenLichTaiLaiChiTiet();
  });
  ngatKetNoiRealtime = ketNoiHoaDonRealtime({
    authScope: 'customer',
    onHoaDonThayDoi: (event) => {
      if (!laSuKienCuaDonHienTai(event)) return;
      lenLichTaiLaiChiTiet();
    },
    onConnectionChange: (status) => {
      if (status === 'connected') {
        lenLichTaiLaiChiTiet();
      }
    },
  });
});

onBeforeUnmount(() => {
  ngatKetNoiRealtime?.();
  ngatKetNoiRealtimeNoiBo?.();
  window.removeEventListener('focus', lenLichTaiLaiChiTiet);
  window.removeEventListener('pageshow', lenLichTaiLaiChiTiet);
  document.removeEventListener('visibilitychange', dongBoKhiQuayLaiTrang);
  if (realtimeRefreshTimeout) clearTimeout(realtimeRefreshTimeout);
  if (realtimePollingInterval) clearInterval(realtimePollingInterval);
});

watch(() => route.params.id, () => taiChiTiet());

async function taiChiTiet(amThang = false) {
  if (!amThang) dangTai.value = true;
  loi.value = '';
  try {
    don.value = await layChiTietDonHang(route.params.id);
  } catch {
    if (!amThang) {
      don.value = null;
      loi.value = 'Không tải được đơn hàng này.';
    }
  } finally {
    if (!amThang) dangTai.value = false;
  }
}

const CAC_BUOC_TRA_HANG = Object.freeze([
  {
    id: 1,
    ten: 'Chờ duyệt',
    icon: 'M12 8v4l3 3 M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z',
  },
  {
    id: 2,
    ten: 'Chờ gửi hàng',
    icon: 'M3 6h18 M7 3v6 M17 3v6 M5 10h14v10H5z',
  },
  {
    id: 3,
    ten: 'Đang hoàn hàng',
    icon: 'M3 12h15 M14 7l5 5-5 5 M5 5v14',
  },
  {
    id: 4,
    ten: 'Đã nhận hàng',
    icon: 'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M3.3 7 12 12l8.7-5 M12 22V12',
  },
  {
    id: 5,
    ten: 'Đang kiểm tra',
    icon: 'M9 11l3 3L22 4 M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11',
  },
  {
    id: 6,
    ten: 'Chờ hoàn tiền',
    icon: 'M12 2v20 M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6',
  },
  {
    id: 7,
    ten: 'Đã hoàn tiền',
    icon: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z M8 12l3 3 5-6',
  },
]);

const TRANG_THAI_TRA_HANG_DAC_BIET = Object.freeze({
  8: {
    tieuDe: 'Yêu cầu trả hàng bị từ chối',
    moTa: 'Yêu cầu trả hàng/hoàn tiền không được chấp nhận.',
    lopMau: 'bg-rose-50 text-rose-600',
  },
  9: {
    tieuDe: 'Yêu cầu trả hàng đã hủy',
    moTa: 'Phiếu trả hàng/hoàn tiền này đã được hủy.',
    lopMau: 'bg-slate-100 text-slate-600',
  },
  10: {
    tieuDe: 'Hoàn hàng thất bại',
    moTa: 'Hàng trả chưa được giao thành công. Vui lòng liên hệ cửa hàng để được hỗ trợ.',
    lopMau: 'bg-rose-50 text-rose-600',
  },
});

const coPhieuTraHang = computed(() => don.value?.phieuTraHangId != null);

const tongTienSanPhamHoan = computed(() => {
  return don.value?.chiTietTraHang?.reduce((sum, item) => sum + (Number(item.soTienHoan) || 0), 0) || 0;
});

const isLoiShop = computed(() => {
  const lyDo = String(don.value?.lyDoTraHangMa || "").trim().toUpperCase();
  return ["PRODUCT_DEFECT", "WRONG_SIZE", "NOT_AS_DESCRIBED", "GIAO_SAI", "HANG_LOI"].includes(lyDo);
});

const hoanPhiVanChuyen = computed(() => {
  if (!don.value) return 0;
  return (isLoiShop.value && tongTienSanPhamHoan.value > 0) ? (Number(don.value.phiVanChuyen) || 0) : 0;
});

const coPhieuGiamGia = computed(() => {
  return don.value && don.value.maPhieuGiamGia && Number(don.value.giamVoucher || 0) > 0;
});

const viTriHienTai = computed(() => (
  coPhieuTraHang.value
    ? Math.min(Math.max(Number(don.value?.trangThaiTraHang) || 0, 0), CAC_BUOC_TRA_HANG.length)
    : layViTriTienTrinhDonHang(don.value?.trangThai)
));

const cauHinhTrangThai = computed(() => {
  if (!coPhieuTraHang.value) {
    return layCauHinhTrangThaiDonHang(don.value?.trangThai);
  }

  const trangThai = Number(don.value?.trangThaiTraHang);
  const dacBiet = TRANG_THAI_TRA_HANG_DAC_BIET[trangThai];
  if (dacBiet) {
    return { ...dacBiet, hienStepper: false };
  }

  return {
    hienStepper: trangThai >= 1 && trangThai <= 7,
    tieuDe: 'Trạng thái trả hàng chưa xác định',
    moTa: 'Vui lòng tải lại trang hoặc liên hệ cửa hàng để được hỗ trợ.',
    lopMau: 'bg-slate-100 text-slate-600',
  };
});

function chuanHoaTrangThai(value) {
  const trangThai = String(value || '')
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .replace(/\s+/g, ' ')
    .trim();
  return trangThai === 'cho giao hang' ? 'dang giao hang' : trangThai;
}

const thongTinCacBuoc = computed(() => {
  if (coPhieuTraHang.value) {
    const lichSuTraHang = Array.isArray(don.value?.lichSuTraHang) ? don.value.lichSuTraHang : [];
    return CAC_BUOC_TRA_HANG.map((buoc) => {
      const banGhi = lichSuTraHang.find(
        (item) => Number(item?.trangThai) === buoc.id,
      );
      return {
        ...buoc,
        thoiGian: banGhi?.ngayTao || null,
      };
    });
  }

  const lichSu = Array.isArray(don.value?.lichSuTrangThai) ? don.value.lichSuTrangThai : [];

  return CAC_BUOC_DON_HANG.map((buoc, index) => {
    const banGhi = lichSu.find(
      (item) => chuanHoaTrangThai(item?.trangThai) === chuanHoaTrangThai(buoc.ten),
    );

    if (banGhi) {
      return {
        ...buoc,
        thoiGian: banGhi.ngayTao,
      };
    }

    if (index === 0) {
      return {
        ...buoc,
        thoiGian: don.value?.ngayLap,
      };
    }

    if (index + 1 === viTriHienTai.value) {
      return {
        ...buoc,
        thoiGian: don.value?.ngayCapNhat,
      };
    }

    return { ...buoc, thoiGian: null };
  });
});

function formatNgay(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString('vi-VN');
  } catch {
    return '';
  }
}

function formatGioBuoc(iso) {
  if (!iso) return '';
  return new Intl.DateTimeFormat('vi-VN', {
    timeZone: 'Asia/Bangkok',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(new Date(iso));
}

function formatNgayBuoc(iso) {
  if (!iso) return '';
  return new Intl.DateTimeFormat('vi-VN', {
    timeZone: 'Asia/Bangkok',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(new Date(iso));
}

function lopBadge(tt) {
  switch (tt) {
    case 1: return 'bg-amber-50 text-amber-600';
    case 9: return 'bg-sky-50 text-sky-600';
    case 2: return 'bg-violet-50 text-violet-600';
    case 3: return 'bg-indigo-50 text-indigo-600';
    case 4: return 'bg-cyan-50 text-cyan-600';
    case 5: return 'bg-emerald-50 text-emerald-600';
    case 6: return 'bg-rose-50 text-rose-600';
    case 7: return 'bg-amber-50 text-amber-700';
    case 8: return 'bg-rose-50 text-rose-600 border border-rose-100';
    case 10: return 'bg-rose-50 text-rose-600';
    default: return 'bg-slate-100 text-slate-600';
  }
}

const dangXuLy = ref(false);
const daHoanThanh = computed(() => don.value?.trangThai === 5);
// Quyền hủy/sửa do backend quyết định theo hình thức thanh toán + trạng thái.
const coTheYeuCauHuy = computed(() => don.value?.coTheHuy === true);
const coTheSuaThongTinGiaoHang = computed(() => don.value?.coTheCapNhatGiaoHang === true);

async function moModalSuaThongTinGiaoHang() {
  const khachHangId = layKhachId();
  diaChiDaLuu.value = [];
  if (khachHangId) {
    try {
      const data = await layDanhSachDiaChiProfile(khachHangId);
      diaChiDaLuu.value = Array.isArray(data) ? data : [];
    } catch {
      diaChiDaLuu.value = [];
    }
  }
  hienModalGiaoHang.value = true;
}

async function luuThongTinGiaoHang(payload) {
  if (dangLuuGiaoHang.value) return;
  dangLuuGiaoHang.value = true;
  try {
    don.value = await capNhatThongTinGiaoHang(route.params.id, payload);
    hienModalGiaoHang.value = false;
    showSuccess('Thông tin nhận hàng đã được cập nhật.');
  } catch (error) {
    showError(getDisplayErrorMessage(error, 'Không thể cập nhật thông tin nhận hàng'));
  } finally {
    dangLuuGiaoHang.value = false;
  }
}

async function guiYeuCauHuy() {
  const daThanhToanCK = don.value?.hinhThucThanhToan === 'CHUYEN_KHOAN';
  const daXacNhan = await showConfirm(
    daThanhToanCK
      ? 'Bạn chắc chắn muốn hủy đơn? Đơn đã thanh toán sẽ được cửa hàng hoàn tiền lại cho bạn.'
      : 'Bạn chắc chắn muốn hủy đơn hàng này? Thao tác không thể hoàn tác.',
    'Hủy đơn hàng',
    'Hủy đơn',
    'Quay lại',
  );
  if (!daXacNhan) return;

  dangXuLy.value = true;
  try {
    await yeuCauHuyDonHang(route.params.id);
    await taiChiTiet(true);
    showSuccess(daThanhToanCK
      ? 'Đơn hàng đã được hủy. Cửa hàng sẽ hoàn tiền cho bạn.'
      : 'Đơn hàng đã được hủy.');
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể hủy đơn hàng'));
  } finally {
    dangXuLy.value = false;
  }
}

async function xacNhanNhan() {
  dangXuLy.value = true;
  try {
    await xacNhanDaNhanHang(route.params.id);
    await taiChiTiet(true);
    showSuccess('Đã xác nhận nhận hàng. Bạn có thể đánh giá sản phẩm.');
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể xác nhận nhận hàng'));
  } finally {
    dangXuLy.value = false;
  }
}

function diDanhGia() {
  router.push(`/khachhang/don-hang/${route.params.id}/danh-gia`);
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) event.target.src = anhMacDinh;
}
</script>

<template>
  <main class="invoice-flat bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-4xl px-6 lg:px-10 pt-8">
      <button @click="router.push('/khachhang/don-hang')" class="mb-6 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-primary transition-colors">
        <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6" /></svg>
        Đơn hàng của bạn
      </button>

      <div v-if="dangTai" class="py-28 text-center text-sm text-slate-400">Đang tải đơn hàng...</div>
      <div v-else-if="loi || !don" class="py-28 text-center text-sm text-rose-500">{{ loi || 'Không tìm thấy đơn hàng.' }}</div>

      <template v-else>
        <!-- Header -->
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">Đơn hàng #{{ don.ma }}</h1>
            <p class="mt-1 text-sm text-slate-400">Đặt lúc {{ formatNgay(don.ngayLap) }}</p>
          </div>
          <div class="flex items-center gap-2">
            <span
              v-if="don.phieuTraHangId == null"
              class="rounded-full px-3.5 py-1.5 text-sm font-semibold"
              :class="lopBadge(don.trangThai)"
            >
              {{ don.trangThaiText }}
            </span>
            <span v-if="don.phieuTraHangId != null" class="rounded-full px-3.5 py-1.5 text-sm font-semibold" :class="lopBadgeTraHang(don.trangThaiTraHang)">
              {{ don.trangThaiTraHangText }}
            </span>
          </div>
        </div>

        <!-- Trục trạng thái -->
        <section class="mt-6 overflow-hidden rounded-3xl border border-slate-100 bg-white px-5 py-5 shadow-sm lg:px-7">
          <div class="flex items-center gap-2 border-b border-slate-100 pb-5 text-[15px] font-semibold text-slate-700">
            <svg class="h-[18px] w-[18px] text-slate-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <rect x="7" y="4" width="10" height="16" rx="2" />
              <path d="M9 4.5V3.8A1.8 1.8 0 0 1 10.8 2h2.4A1.8 1.8 0 0 1 15 3.8v.7M10 9h4M10 13h4M10 17h2" />
            </svg>
            {{ coPhieuTraHang ? 'Trạng Thái Trả Hàng/Hoàn Tiền' : 'Trạng Thái Đơn Hàng' }}
          </div>

          <div
            v-if="!cauHinhTrangThai.hienStepper"
            class="mt-5 flex items-center gap-3 rounded-2xl px-5 py-4"
            :class="cauHinhTrangThai.lopMau"
          >
            <svg class="h-6 w-6 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6M9 9l6 6"/></svg>
            <div>
              <p class="font-bold">{{ cauHinhTrangThai.tieuDe }}</p>
              <p class="text-sm opacity-75">{{ cauHinhTrangThai.moTa }}</p>
            </div>
          </div>

          <div v-else class="relative mx-auto mt-7 w-full max-w-5xl px-2 pt-2">
            <div
              class="absolute top-[35px] z-0 h-[2px] bg-slate-200"
              :style="{ left: `${100 / thongTinCacBuoc.length / 2}%`, right: `${100 / thongTinCacBuoc.length / 2}%` }"
            ></div>

            <div
              class="relative z-10 grid w-full items-start"
              :style="{ gridTemplateColumns: `repeat(${thongTinCacBuoc.length}, minmax(0, 1fr))` }"
            >
              <div
                v-for="(buoc, i) in thongTinCacBuoc"
                :key="buoc.ten"
                class="flex min-w-0 flex-col items-center text-center"
              >
                <div
                  class="flex h-14 w-14 items-center justify-center rounded-full border-[2.5px] transition"
                  :class="(i + 1) <= viTriHienTai
                    ? 'border-[#c52220] bg-[#c52220] text-white shadow-[0_10px_22px_rgba(197,34,32,0.18)]'
                    : 'border-slate-200 bg-white text-slate-300'"
                >
                  <svg class="h-[22px] w-[22px]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                    <path :d="buoc.icon" />
                  </svg>
                </div>
                <p
                  class="mt-3 whitespace-nowrap text-xs font-semibold"
                  :class="(i + 1) <= viTriHienTai ? 'text-[#c52220]' : 'text-slate-400'"
                >
                  {{ buoc.ten }}
                </p>
                <div class="mt-1 min-h-[36px] text-[11px] leading-4 text-slate-400">
                  <p v-if="buoc.thoiGian">
                    <span class="block">{{ formatGioBuoc(buoc.thoiGian) }}</span>
                    <span class="block">{{ formatNgayBuoc(buoc.thoiGian) }}</span>
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section v-if="coTheYeuCauHuy" class="mt-6 flex flex-wrap gap-3 items-center">
          <button
            @click="guiYeuCauHuy"
            :disabled="dangXuLy"
            class="inline-flex items-center justify-center gap-2 rounded-2xl border border-rose-200 bg-rose-50 px-6 py-3 text-sm font-bold text-rose-600 shadow-sm transition hover:-translate-y-0.5 hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {{ dangXuLy ? 'Đang xử lý...' : 'Hủy đơn' }}
          </button>
        </section>

        <!-- Hành động khi đơn hoàn thành hoặc đã giao hàng -->
        <section v-if="don && [4, 5].includes(don.trangThai)" class="mt-6 flex flex-wrap gap-3 items-center">
          <button v-if="don.trangThai === 4" @click="xacNhanNhan" :disabled="dangXuLy" class="inline-flex items-center justify-center rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5 disabled:opacity-60">
            {{ dangXuLy ? 'Đang xử lý...' : 'Đã nhận hàng' }}
          </button>
          <button v-if="don.trangThai === 5" @click="diDanhGia" class="inline-flex items-center gap-2 rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5">
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l2.9 6.3 6.9.6-5.2 4.6 1.6 6.8L12 17.3 5.8 20.9l1.6-6.8L2.2 8.9l6.9-.6z"/></svg>
            Đánh giá sản phẩm
          </button>

          <!-- Yêu cầu trả hàng: chỉ hiện khi còn trong thời hạn chính sách 3 ngày. -->
          <button
            v-if="(don.phieuTraHangId == null || [8, 9].includes(don.trangThaiTraHang)) && !daQuaHanTraHang"
            @click="hienModalTraHang = true"
            class="inline-flex items-center justify-center gap-2 rounded-2xl border border-rose-200 bg-rose-50 px-6 py-3 text-sm font-bold text-rose-600 shadow-sm transition hover:-translate-y-0.5 hover:bg-rose-100"
          >
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 14 4 9l5-5" />
              <path d="M4 9h10a6 6 0 0 1 6 6v5" />
            </svg>
            Yêu cầu trả hàng
          </button>
        </section>

        <!-- Thông tin nhận hàng -->
        <section class="mt-6 rounded-3xl bg-white border border-slate-100 p-6 lg:p-7 shadow-sm">
          <div class="mb-3 flex flex-wrap items-center justify-between gap-3">
            <h2 class="text-base font-bold text-slate-800">Thông tin nhận hàng</h2>
            <button
              v-if="coTheSuaThongTinGiaoHang"
              type="button"
              class="inline-flex items-center gap-2 rounded-xl border border-rose-200 bg-white px-4 py-2 text-xs font-bold text-rose-600 transition hover:bg-rose-50"
              @click="moModalSuaThongTinGiaoHang"
            >
              <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 20h9" />
                <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L8 18l-4 1 1-4Z" />
              </svg>
              Chỉnh sửa thông tin
            </button>
          </div>
          <div class="rounded-2xl bg-slate-50 px-5 py-4 text-sm text-slate-600">
            <p class="font-semibold text-slate-800">{{ don.tenNguoiNhan }} · {{ don.sdtNguoiNhan }}</p>
            <p class="mt-1">{{ don.diaChiGiaoHang }}</p>
          </div>
        </section>

        <!-- Sản phẩm -->
        <section class="mt-6 rounded-3xl bg-white border border-slate-100 p-6 lg:p-7 shadow-sm">
          <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
            <h2 class="text-base font-bold text-slate-800">Sản phẩm</h2>
          </div>
          <div class="space-y-4">
            <div
              v-for="(sp, i) in don.sanPhams"
              :key="sp.hoaDonChiTietId ?? i"
              class="flex gap-4"
            >
              <img :src="resolveHinhAnh(sp.hinhAnh) || anhMacDinh" :alt="sp.tenSanPham" class="h-16 w-16 shrink-0 rounded-xl object-cover bg-slate-50" @error="xuLyAnhLoi" />
              <div class="flex-1 text-sm">
                <p class="font-medium text-slate-800">{{ sp.tenSanPham }}</p>
                <p class="text-xs text-slate-400">{{ sp.mauSac }} · {{ sp.kichCo }} · x{{ sp.soLuong }}</p>
                <div class="mt-1 flex items-center gap-2">
                  <span class="font-semibold text-primary">{{ dinhDangTienViet(sp.giaDonVi) }}</span>
                  <span v-if="Number(sp.giaNiemYet) > Number(sp.giaDonVi)" class="text-xs text-slate-400 line-through">{{ dinhDangTienViet(sp.giaNiemYet) }}</span>
                </div>
              </div>
              <p class="text-sm font-semibold text-slate-700">{{ dinhDangTienViet(sp.thanhTien) }}</p>
            </div>
          </div>
        </section>

        <!-- Chi tiết trả hàng / hoàn tiền -->
        <section v-if="coPhieuTraHang" class="mt-6 rounded-3xl bg-white border border-slate-100 p-6 lg:p-7 shadow-sm">
          <div class="border-b border-slate-100 pb-4 mb-4">
            <h2 class="text-base font-bold text-slate-800 flex items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-rose-500"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M12 7v5l4 2"/></svg>
              Chi tiết yêu cầu trả hàng
            </h2>
          </div>

          <div class="grid gap-6 md:grid-cols-2">
            <!-- Cột trái: Lý do & Hình ảnh -->
            <div class="space-y-4">
              <div>
                <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">Lý do trả hàng</span>
                <p class="text-sm font-semibold text-slate-800">{{ hienThiLyDo(don.lyDoTraHangMa) }}</p>
                <p v-if="don.lyDoTraHangMoTa" class="mt-1.5 text-sm text-slate-600 bg-slate-50 rounded-xl p-3 border border-slate-100 whitespace-pre-wrap">
                  {{ don.lyDoTraHangMoTa }}
                </p>
              </div>

              <div>
                <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-2">Hình ảnh minh chứng</span>
                <div v-if="don.hinhAnhTraHang?.length" class="grid grid-cols-3 gap-2">
                  <a
                    v-for="(url, index) in don.hinhAnhTraHang"
                    :key="`${url}-${index}`"
                    :href="resolveHinhAnh(url)"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="group relative aspect-square overflow-hidden rounded-xl border border-slate-200 bg-slate-50"
                  >
                    <img
                      :src="resolveHinhAnh(url)"
                      :alt="`Ảnh minh chứng trả hàng ${index + 1}`"
                      class="h-full w-full object-cover transition duration-300 group-hover:scale-105"
                    />
                  </a>
                </div>
                <div v-else class="flex items-center gap-2 rounded-xl border border-dashed border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-400">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                  Chưa cung cấp hình ảnh minh chứng.
                </div>
              </div>
            </div>

            <!-- Cột phải: Tổng kết hoàn tiền -->
            <div class="border-t md:border-t-0 md:border-l border-slate-100 pt-4 md:pt-0 md:pl-6 flex flex-col justify-between">
              <div>
                <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-3">Tổng kết hoàn tiền</span>
                <div class="space-y-3 text-sm">
                  <div class="flex items-center justify-between text-slate-500">
                    <span>Tiền hoàn dự kiến</span>
                    <span class="font-semibold text-slate-700">{{ dinhDangTienViet(don.tongTienDuKienTra) }}</span>
                  </div>

                  <div class="border-t border-slate-100 pt-3 space-y-2">
                    <div class="flex items-center justify-between text-slate-600">
                      <span>Tiền sản phẩm hoàn trả</span>
                      <span>{{ dinhDangTienViet(tongTienSanPhamHoan) }}</span>
                    </div>

                    <div v-if="hoanPhiVanChuyen > 0" class="flex items-center justify-between text-slate-600">
                      <span class="flex items-center gap-1.5">
                        Phí vận chuyển gốc được hoàn
                        <img :src="logoGhn" alt="GHN" class="h-3.5 w-auto object-contain" />
                        <span class="relative group inline-flex items-center cursor-help text-slate-400 hover:text-slate-600 transition">
                          <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-3.5 w-3.5"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                          <span class="absolute bottom-full left-1/2 z-50 mb-2 w-56 -translate-x-1/2 rounded-xl bg-slate-800 p-2.5 text-center text-[10px] font-normal leading-normal text-white shadow-lg transition-all duration-0 opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto">
                            Hoàn phí ship GHN do lỗi của shop: {{ hienThiLyDo(don.lyDoTraHangMa) }}
                          </span>
                        </span>
                      </span>
                      <span class="text-slate-700 font-semibold">
                        +{{ dinhDangTienViet(hoanPhiVanChuyen) }}
                      </span>
                    </div>

                    <div v-if="coPhieuGiamGia" class="bg-slate-50 rounded-xl p-2.5 mt-2 space-y-1">
                      <div class="flex items-center justify-between text-[11px] text-slate-500">
                        <span>Mã giảm giá đã dùng</span>
                        <span class="font-bold text-slate-700">{{ don.maPhieuGiamGia }}</span>
                      </div>
                      <div class="flex items-center justify-between text-[11px] text-slate-500">
                        <span class="flex items-center gap-1">
                          Tiền voucher giảm
                          <span class="relative group inline-flex items-center cursor-help text-slate-400 hover:text-slate-600 transition">
                            <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-3.5 w-3.5"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                            <span class="absolute bottom-full left-1/2 z-50 mb-2 w-56 -translate-x-1/2 rounded-xl bg-slate-800 p-2.5 text-center text-[10px] font-normal leading-normal text-white shadow-lg transition-all duration-0 opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto">
                              Số tiền hoàn của sản phẩm đã được tự động khấu trừ theo tỷ lệ áp dụng voucher của đơn hàng gốc.
                            </span>
                          </span>
                        </span>
                        <span class="text-emerald-600 font-semibold">-{{ dinhDangTienViet(don.giamVoucher) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="border-t border-slate-200 pt-4 mt-4">
                <div class="flex items-center justify-between">
                  <span class="text-base font-bold text-slate-800">Tiền hoàn được duyệt</span>
                  <span class="text-xl font-bold text-emerald-600">{{ dinhDangTienViet(don.tongTienThucTeTra) }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Phân tích giá -->
        <section v-if="!coPhieuTraHang" class="mt-6 rounded-3xl bg-white border border-slate-100 p-6 lg:p-7 shadow-sm">
          <h2 class="text-base font-bold text-slate-800 mb-4">Thông tin thanh toán</h2>
          <div class="space-y-2.5 text-sm">
            <div class="flex items-center justify-between text-slate-600">
              <span>Tạm tính (giá gốc)</span>
              <span>{{ dinhDangTienViet(don.tamTinh) }}</span>
            </div>
            <div v-if="Number(don.giamDotGiamGia) > 0" class="flex items-center justify-between text-emerald-600">
              <span>Giảm từ đợt giảm giá</span>
              <span>-{{ dinhDangTienViet(don.giamDotGiamGia) }}</span>
            </div>
            <div v-if="Number(don.giamVoucher) > 0" class="flex items-center justify-between text-emerald-600">
              <span>Giảm từ voucher<span v-if="don.maPhieuGiamGia"> ({{ don.maPhieuGiamGia }})</span></span>
              <span>-{{ dinhDangTienViet(don.giamVoucher) }}</span>
            </div>
            <div v-if="Number(don.phiVanChuyen) > 0" class="flex items-center justify-between text-slate-600">
              <span class="flex items-center gap-1.5">
                Phí vận chuyển
                <img :src="logoGhn" alt="GHN" class="h-4 w-auto object-contain" />
              </span>
              <span>+{{ dinhDangTienViet(don.phiVanChuyen) }}</span>
            </div>
            <div class="flex items-center justify-between border-t border-slate-100 pt-3">
              <span class="text-base font-bold text-slate-800">Tổng thanh toán</span>
              <span class="text-2xl font-bold text-primary">{{ dinhDangTienViet(don.tongThanhToan) }}</span>
            </div>
          </div>
        </section>
      </template>
    </div>

    <!-- Return Request Modal -->
    <YeuCauTraHangModal
      :isOpen="hienModalTraHang"
      :don="don"
      @close="hienModalTraHang = false"
      @success="taiChiTiet"
    />
    <ChinhSuaGiaoHangModal
      v-model="hienModalGiaoHang"
      title="Chỉnh sửa thông tin nhận hàng"
      :initial-data="{
        tenNguoiNhan: don?.tenNguoiNhan,
        sdtNguoiNhan: don?.sdtNguoiNhan,
        diaChiGiaoHang: don?.diaChiGiaoHang,
      }"
      :saved-addresses="diaChiDaLuu"
      :saving="dangLuuGiaoHang"
      @save="luuThongTinGiaoHang"
    />
  </main>
</template>
<style scoped>
.invoice-flat :deep([class*="rounded-"]:not(.rounded-full)) {
  border-radius: 6px !important;
}
  </style>
