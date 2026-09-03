<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  capNhatThongTinGiaoHang,
  layChiTietDonHang,
  xacNhanDaNhanHang,
  yeuCauHuyDonHang,
} from '../services/don-hang';
import {
  layDanhSachDiaChiProfile,
  layDanhSachTaiKhoanNganHang,
} from '../services/client-profile';
import { layKhachId, layThongTinKhach } from '../services/gio-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showSuccess, showError, showConfirm } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import {
  CAC_BUOC_DON_HANG,
  layCauHinhTrangThaiDonHang,
  layViTriTienTrinhDonHang,
} from '../utils/order-status';
import anhMacDinh from '../assets/login-shoe.png';
import ChinhSuaGiaoHangModal from '../components/common/ChinhSuaGiaoHangModal.vue';
import {
  ketNoiHoaDonRealtime,
  langNgheHoaDonThayDoiNoiBo,
} from '../services/hoa-don-realtime';
import logoGhn from '../assets/logo/Logo-GHN-Blue-Orange.webp';
import { API_BASE_URL } from '../services/api-client';
import { dinhDangDiaChi } from '../utils/dia-chi';
import {
  hoaDonDaCoShipperGhn,
  layShipperGhnTheoHoaDon,
} from '../utils/ghn-shipper';

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
  if (/^(data:|blob:)/i.test(value)) return value;
  const idx = value.indexOf("/uploads/");
  if (idx >= 0) return value.slice(idx);
  if (value.startsWith("uploads/")) return `/${value}`;
  if (/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i.test(value)) {
    return value.replace(/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i, "");
  }
  if (/^https?:/i.test(value)) return value;
  if (value.startsWith("/")) return value;
  return `/${value}`;
}

const route = useRoute();
const router = useRouter();
const REALTIME_POLL_INTERVAL_MS = 3000;

const don = ref(null);
const dangTai = ref(true);
const loi = ref('');
const hienModalGiaoHang = ref(false);
const dangLuuGiaoHang = ref(false);
const diaChiDaLuu = ref([]);
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

const viTriHienTai = computed(() => layViTriTienTrinhDonHang(don.value?.trangThai));
const cauHinhTrangThai = computed(() => layCauHinhTrangThaiDonHang(don.value?.trangThai));

const thongTinShipper = computed(() => {
  if (!don.value) return null;
  return hoaDonDaCoShipperGhn(don.value)
    ? layShipperGhnTheoHoaDon(don.value)
    : null;
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

const lichSuGiaoLai = computed(() => {
  const lichSu = Array.isArray(don.value?.lichSuTrangThai) ? don.value.lichSuTrangThai : [];
  return lichSu
    .filter((item) => chuanHoaTrangThai(item?.trangThai) === 'tao luot giao lai')
    .sort((a, b) => new Date(b?.ngayTao || 0) - new Date(a?.ngayTao || 0));
});

const giaoLaiMoiNhat = computed(() => lichSuGiaoLai.value[0] || null);

const thongTinCacBuoc = computed(() => {
  const lichSu = Array.isArray(don.value?.lichSuTrangThai) ? don.value.lichSuTrangThai : [];

  return CAC_BUOC_DON_HANG.map((buoc, index) => {
    if (chuanHoaTrangThai(buoc.ten) === 'cho lay hang' && giaoLaiMoiNhat.value) {
      return {
        ...buoc,
        thoiGian: giaoLaiMoiNhat.value.ngayTao,
      };
    }

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

const lyDoHuyDon = computed(() => {
  if (!don.value || !Array.isArray(don.value.lichSuTrangThai)) return '';
  // Tìm sự kiện liên quan đến hủy trong lịch sử có ghi chú
  const itemHuy = don.value.lichSuTrangThai.find((item) => {
    const tt = chuanHoaTrangThai(item?.trangThai);
    return (
      tt === 'huy' ||
      tt === 'da huy' ||
      tt === 'chap nhan yeu cau huy' ||
      tt === 'yeu cau huy' ||
      tt === 'hoan tien' ||
      tt === 'giao hang that bai'
    ) && item?.ghiChu && item.ghiChu.trim() !== '';
  });
  if (itemHuy?.ghiChu) {
    return itemHuy.ghiChu.trim();
  }
  // Nếu đơn ở trạng thái đã hủy hoặc đặc biệt, lấy ghi chú gần nhất
  if ([6, 7, 8, 10].includes(don.value.trangThai)) {
    const firstWithNote = don.value.lichSuTrangThai.find((item) => item?.ghiChu && item.ghiChu.trim() !== '');
    if (firstWithNote?.ghiChu) return firstWithNote.ghiChu.trim();
    if (don.value.ghiChu && don.value.ghiChu.trim() !== '') return don.value.ghiChu.trim();
  }
  return '';
});

function nhanHinhThucThanhToan(hinhThuc) {
  return hinhThuc === 'CHUYEN_KHOAN'
    ? 'Chuyển khoản (VietQR/VNPAY)'
    : 'Thanh toán khi nhận hàng (COD)';
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
const coTheNhanHang = computed(() => {
  if (!don.value) return false;
  if (typeof don.value.coTheNhanHang === 'boolean') {
    return don.value.coTheNhanHang;
  }
  const daThanhToan = don.value.hinhThucThanhToan === 'CHUYEN_KHOAN' || don.value.daThanhToan === true;
  return don.value.trangThai === 4 && daThanhToan;
});
const emailKhachHang = ref('');

const lichSuGiaoHang = computed(() => {
  if (!don.value || !Array.isArray(don.value.lichSuTrangThai)) return [];
  return don.value.lichSuTrangThai
    .filter((item) => {
      const trangThai = chuanHoaTrangThai(item?.trangThai);
      return trangThai === 'cap nhat thong tin giao hang' || trangThai === 'tao luot giao lai';
    })
    .map((item) => ({
      ...item,
      laGiaoLai: chuanHoaTrangThai(item?.trangThai) === 'tao luot giao lai',
    }))
    .sort((a, b) => new Date(b?.ngayTao || 0) - new Date(a?.ngayTao || 0));
});

async function moModalSuaThongTinGiaoHang() {
  const khachHangId = layKhachId();
  emailKhachHang.value = layThongTinKhach()?.email || '';
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
  const daThanhToanCK =
    don.value?.daThanhToan === true ||
    don.value?.hinhThucThanhToan === 'CHUYEN_KHOAN';

  if (daThanhToanCK) {
    const khId = layKhachId();
    if (khId) {
      try {
        const accounts = await layDanhSachTaiKhoanNganHang(khId);
        if (!accounts || accounts.length === 0) {
          const res = await showConfirm(
            'Sau khi hủy, số tiền đã thanh toán sẽ được chuyển trả vào tài khoản ngân hàng của bạn. Vui lòng thêm thông tin ngân hàng nhận tiền để tiếp tục.',
            'Thông tin hoàn tiền',
            'Thêm tài khoản ngay',
            'Đóng',
          );
          if (res) {
            router.push('/khachhang/profile?tab=nganHang');
          }
          return;
        }
      } catch (err) {
        console.error('Lỗi kiểm tra tài khoản ngân hàng:', err);
      }
    }
  }

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
            <span class="rounded-full px-3.5 py-1.5 text-sm font-semibold" :class="lopBadge(don.trangThai)">
              {{ don.trangThaiText }}
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
            Trạng Thái Đơn Hàng
          </div>

          <div
            v-if="!cauHinhTrangThai.hienStepper"
            class="mt-5 flex items-start gap-3.5 rounded-2xl px-5 py-4"
            :class="cauHinhTrangThai.lopMau"
          >
            <svg class="h-6 w-6 shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6M9 9l6 6"/></svg>
            <div class="space-y-1">
              <p class="font-bold text-base">{{ cauHinhTrangThai.tieuDe }}</p>
              <p class="text-sm opacity-90">{{ cauHinhTrangThai.moTa }}</p>
              <div v-if="lyDoHuyDon" class="mt-2.5 pt-2 border-t border-rose-200/60 dark:border-rose-800/60 text-sm">
                <span class="font-semibold text-rose-950 dark:text-rose-100">Lý do hủy: </span>
                <span class="font-medium text-rose-800 dark:text-rose-200">{{ lyDoHuyDon }}</span>
              </div>
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

        <!-- Hành động khi đơn hoàn thành hoặc đã giao hàng và thanh toán -->
        <section v-if="don && (coTheNhanHang || don.trangThai === 5)" class="mt-6 flex flex-wrap gap-3 items-center">
          <button v-if="coTheNhanHang" @click="xacNhanNhan" :disabled="dangXuLy" class="inline-flex items-center justify-center rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5 disabled:opacity-60">
            {{ dangXuLy ? 'Đang xử lý...' : 'Đã nhận hàng' }}
          </button>
          <button v-if="don.trangThai === 5" @click="diDanhGia" class="inline-flex items-center gap-2 rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5">
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l2.9 6.3 6.9.6-5.2 4.6 1.6 6.8L12 17.3 5.8 20.9l1.6-6.8L2.2 8.9l6.9-.6z"/></svg>
            Đánh giá sản phẩm
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
            <p class="mt-1">{{ dinhDangDiaChi(don.diaChiGiaoHang) || '—' }}</p>
            <p class="mt-2 text-xs text-slate-500">
              Phương thức thanh toán:
              <span class="font-medium text-slate-700">{{ nhanHinhThucThanhToan(don.hinhThucThanhToan) }}</span>
            </p>
            <div
              v-if="thongTinShipper"
              class="mt-4 flex items-center gap-3 border-t border-slate-200 pt-4"
            >
              <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-orange-500 text-white shadow-sm">
                <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="8" r="4" />
                  <path d="M4.5 21a7.5 7.5 0 0 1 15 0" />
                </svg>
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center gap-x-2 gap-y-1">
                  <p class="font-bold text-slate-800">{{ thongTinShipper.hoTen }}</p>
                  <span class="rounded-full bg-orange-100 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide text-orange-700">
                    {{ thongTinShipper.donVi }}
                  </span>
                </div>
                <p class="mt-1 text-xs text-slate-500">Mã shipper: {{ thongTinShipper.ma }}</p>
              </div>
              <a
                :href="`tel:${thongTinShipper.soDienThoai}`"
                class="inline-flex shrink-0 items-center gap-2 rounded-xl border border-orange-200 bg-white px-3 py-2 text-xs font-bold text-orange-600 shadow-sm transition hover:bg-orange-50"
              >
                <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.79 19.79 0 0 1 2.12 4.18 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.12.9.33 1.78.62 2.63a2 2 0 0 1-.45 2.11L8 9.73a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.85.29 1.73.5 2.63.62A2 2 0 0 1 22 16.92z" />
                </svg>
                {{ thongTinShipper.soDienThoai }}
              </a>
            </div>
          </div>
          
          <p v-if="don && don.soLanSuaDiaChi >= 1 && don.trangThai === 1 && don.hinhThucThanhToan === 'COD'" class="text-xs text-rose-500 font-medium mt-3 flex items-center gap-1.5 bg-rose-50 border border-rose-100 px-3 py-2.5 rounded-xl">
            <svg class="h-4 w-4 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            Bạn đã chỉnh sửa thông tin giao hàng 1 lần. Theo quy định, thông tin giao hàng chỉ được thay đổi tối đa 1 lần.
          </p>

          <!-- Lịch sử thay đổi thông tin giao hàng -->
          <div v-if="lichSuGiaoHang.length > 0" class="mt-4 border-t border-slate-100 pt-4">
            <h3 class="text-xs font-bold text-slate-700 mb-2 flex items-center gap-2">
              <svg class="h-4.5 w-4.5 text-rose-500" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Lịch sử thay đổi thông tin giao hàng
            </h3>
            <div class="space-y-3">
              <div
                v-for="(log, idx) in lichSuGiaoHang"
                :key="`${log.trangThai}-${log.ngayTao}-${idx}`"
                class="rounded-xl border border-slate-100 bg-white p-3.5 text-xs shadow-sm"
              >
                <div class="flex items-center justify-between mb-2 pb-1.5 border-b border-slate-100">
                  <span class="font-semibold text-slate-700">
                    Người thực hiện: {{ log.maNhanVien || (log.laGiaoLai ? 'Cửa hàng' : 'Khách hàng') }}
                  </span>
                  <span class="text-slate-400 text-[10px]">{{ formatNgay(log.ngayTao) }}</span>
                </div>
                <div class="text-slate-600 whitespace-pre-line leading-relaxed text-[11px]">
                  {{ log.laGiaoLai ? 'Cửa hàng đã tạo lượt giao lại đơn hàng.' : log.ghiChu }}
                </div>
              </div>
            </div>
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


        <!-- Phân tích giá -->
        <section class="mt-6 rounded-3xl bg-white border border-slate-100 p-6 lg:p-7 shadow-sm">
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

    <ChinhSuaGiaoHangModal
      v-model="hienModalGiaoHang"
      title="Chỉnh sửa thông tin nhận hàng"
      :initial-data="{
        tenNguoiNhan: don?.tenNguoiNhan,
        sdtNguoiNhan: don?.sdtNguoiNhan,
        email: emailKhachHang,
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
