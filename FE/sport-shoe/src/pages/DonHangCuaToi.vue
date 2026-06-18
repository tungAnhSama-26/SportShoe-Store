<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { layDonHangCuaToi, yeuCauHuyDonHang } from '../services/don-hang';
import { layKhachId, themVaoGio } from '../services/gio-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showSuccess, showError, showConfirm } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import YeuCauTraHangModal from '../components/common/YeuCauTraHangModal.vue';
import { ketNoiHoaDonRealtime } from '../services/hoa-don-realtime';

const router = useRouter();
const danhSach = ref([]);
const dangTai = ref(true);
const daDangNhap = computed(() => Boolean(layKhachId()));

const dangChonDeTra = ref(null);
const laMoTraHangModal = ref(false);
const donDangGuiYeuCauHuy = ref(null);

let ngatKetNoiRealtime = null;
let realtimeRefreshTimeout = null;

function lenLichTaiLaiDanhSach() {
  if (realtimeRefreshTimeout) clearTimeout(realtimeRefreshTimeout);
  realtimeRefreshTimeout = setTimeout(() => taiDanhSach(true), 150);
}

function dongBoKhiQuayLaiTrang() {
  if (document.visibilityState === 'visible') {
    lenLichTaiLaiDanhSach();
  }
}

onMounted(() => {
  taiDanhSach();
  if (!daDangNhap.value) return;

  window.addEventListener('focus', lenLichTaiLaiDanhSach);
  document.addEventListener('visibilitychange', dongBoKhiQuayLaiTrang);
  ngatKetNoiRealtime = ketNoiHoaDonRealtime({
    authScope: 'customer',
    onHoaDonThayDoi: lenLichTaiLaiDanhSach,
    onConnectionChange: (status) => {
      if (status === 'connected') {
        lenLichTaiLaiDanhSach();
      }
    },
  });
});

onBeforeUnmount(() => {
  ngatKetNoiRealtime?.();
  window.removeEventListener('focus', lenLichTaiLaiDanhSach);
  document.removeEventListener('visibilitychange', dongBoKhiQuayLaiTrang);
  if (realtimeRefreshTimeout) clearTimeout(realtimeRefreshTimeout);
});

async function taiDanhSach(amThang = false) {
  if (!amThang) dangTai.value = true;
  try {
    danhSach.value = await layDonHangCuaToi();
  } catch {
    if (!amThang) {
      danhSach.value = [];
    }
  } finally {
    if (!amThang) dangTai.value = false;
  }
}

function formatNgay(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString('vi-VN');
  } catch {
    return '';
  }
}

function lopTrangThai(tt) {
  switch (tt) {
    case 1: return 'bg-amber-50 text-amber-600';
    case 9: return 'bg-orange-50 text-orange-600';
    case 2: return 'bg-blue-50 text-blue-600';
    case 3: return 'bg-violet-50 text-violet-600';
    case 4: return 'bg-cyan-50 text-cyan-600';
    case 5: return 'bg-emerald-50 text-emerald-600';
    case 6: return 'bg-stone-100 text-stone-600';
    case 7: return 'bg-primary/5 text-primary';
    case 8: return 'bg-rose-50 text-rose-600 border border-rose-100';
    case 10: return 'bg-rose-50 text-rose-600';
    default: return 'bg-slate-100 text-slate-600';
  }
}

function lopTrangThaiTraHang(tt) {
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

const dsTrangThai = [
  { value: "", label: "Tất cả" },
  { value: 1, label: "Chờ xác nhận" },
  { value: 9, label: "Đã xác nhận" },
  { value: 2, label: "Chờ lấy hàng" },
  { value: 3, label: "Đang giao hàng" },
  { value: 5, label: "Hoàn thành" },
  { value: 6, label: "Đã hủy" },
  { value: "TRA_HANG", label: "Trả hàng/Hoàn tiền" },
];

const trangThaiDangChon = ref("");

const danhSachHopLe = computed(() => {
  return danhSach.value.filter((d) => [1, 9, 2, 3, 4, 5, 6, 7, 8, 10].includes(d.trangThai));
});

const danhSachHienThi = computed(() => {
  if (trangThaiDangChon.value === "") {
    return danhSachHopLe.value;
  }
  if (trangThaiDangChon.value === "TRA_HANG") {
    return danhSachHopLe.value.filter((d) => d.phieuTraHangId != null || d.trangThai === 8);
  }
  return danhSachHopLe.value.filter(
    (d) => d.phieuTraHangId == null && d.trangThai === trangThaiDangChon.value,
  );
});

async function muaLai(don) {
  if (!don.sanPhams || don.sanPhams.length === 0) return;
  try {
    await Promise.all(don.sanPhams.map(item => themVaoGio(item.giayChiTietId, item.soLuong, {
      tenSanPham: item.ten,
      mauSac: item.mauSac,
      kichCo: item.kichCo,
      hinhAnh: item.hinhAnh,
      giaBan: item.giaDonVi,
      tonKho: Math.max(Number(item.soLuong), 10),
    })));
    showSuccess("Đã thêm các sản phẩm vào giỏ hàng!");
    router.push('/khachhang/gio-hang');
  } catch (error) {
    showError(error.message || "Không thể mua lại sản phẩm");
  }
}

const SO_NGAY_DUOC_GUI_YEU_CAU_TRA_HANG = 3;

function laQuaHanTraHang(don) {
  if (!don || !don.ngayCapNhat) return true;
  const thoiGianHoanThanh = new Date(don.ngayCapNhat).getTime();
  const bayGio = new Date().getTime();
  const thoiHanTraHangMs = SO_NGAY_DUOC_GUI_YEU_CAU_TRA_HANG * 24 * 60 * 60 * 1000;
  return (bayGio - thoiGianHoanThanh) > thoiHanTraHangMs;
}

function moYeuCauTraHang(don) {
  dangChonDeTra.value = don;
  laMoTraHangModal.value = true;
}

async function xuLyTaoPhieuTraHangThanhCong() {
  laMoTraHangModal.value = false;
  trangThaiDangChon.value = "TRA_HANG";
  await taiDanhSach(true);
}

async function guiYeuCauHuy(don) {
  const daXacNhan = await showConfirm(
    'Sau khi gửi yêu cầu, cửa hàng sẽ xem xét và xác nhận hủy đơn.',
    'Yêu cầu hủy đơn hàng',
    'Gửi yêu cầu',
    'Quay lại',
  );
  if (!daXacNhan) return;

  donDangGuiYeuCauHuy.value = don.id;
  try {
    await yeuCauHuyDonHang(don.id);
    await taiDanhSach(true);
    const donSauKhiHuy = danhSach.value.find((item) => item.id === don.id);
    if (Number(donSauKhiHuy?.trangThai) === 8 || donSauKhiHuy?.phieuTraHangId != null) {
      trangThaiDangChon.value = "TRA_HANG";
    } else if (Number(donSauKhiHuy?.trangThai) === 6) {
      trangThaiDangChon.value = 6;
    }
    showSuccess('Yêu cầu hủy đơn hàng đã được gửi.');
  } catch (error) {
    showError(getDisplayErrorMessage(error, 'Không thể gửi yêu cầu hủy đơn hàng'));
  } finally {
    donDangGuiYeuCauHuy.value = null;
  }
}
</script>

<template>
  <main class="orders-six-radius bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-5xl px-6 lg:px-10 pt-10">
      <h1 class="text-3xl font-bold text-slate-900 mb-8">Đơn hàng của bạn</h1>

      <div v-if="!daDangNhap" class="py-24 text-center">
        <p class="text-sm text-slate-500 mb-4">Vui lòng đăng nhập để xem đơn hàng.</p>
        <router-link to="/login" class="inline-flex rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white hover:bg-primary/90">Đăng nhập</router-link>
      </div>

      <div v-else-if="dangTai" class="py-24 text-center text-sm text-slate-400">Đang tải đơn hàng...</div>

      <div v-else>
        <!-- Horizontal Status Tabs -->
        <div class="mb-6 overflow-hidden border-b border-slate-200">
          <div class="flex w-full items-stretch justify-between gap-1">
            <button
              v-for="tab in dsTrangThai"
              :key="tab.value"
              @click="trangThaiDangChon = tab.value"
              class="relative min-w-0 whitespace-nowrap px-1.5 py-3 text-center text-[13px] font-medium leading-5 transition-colors"
              :class="trangThaiDangChon === tab.value ? 'text-primary font-bold' : 'text-slate-500 hover:text-slate-800'"
            >
              <span>{{ tab.label }}</span>
              <div
                v-if="trangThaiDangChon === tab.value"
                class="absolute bottom-0 left-0 right-0 h-0.5 bg-primary rounded-full"
              ></div>
            </button>
          </div>
        </div>

        <div v-if="!danhSachHienThi.length" class="py-24 text-center">
          <p class="text-sm text-slate-500 mb-4">Không có đơn hàng nào.</p>
          <router-link to="/khachhang/san-pham" class="inline-flex rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white hover:bg-primary/90">Mua sắm ngay</router-link>
        </div>

        <!-- Orders List -->
        <div v-else class="space-y-6">
          <div
            v-for="don in danhSachHienThi"
            :key="don.id"
            class="rounded-2xl bg-white border border-slate-100 shadow-sm p-6 space-y-4 hover:shadow-md transition duration-300"
          >
            <!-- Top section: Order Code, Status, Date -->
            <div class="flex flex-wrap items-center justify-between border-b border-slate-100 pb-3 gap-2">
              <div class="flex items-center gap-3">
                <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Mã đơn hàng</span>
                <p class="font-bold text-slate-900">#{{ don.ma }}</p>
                <span class="text-xs text-slate-400 font-medium">| {{ formatNgay(don.ngayLap) }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span
                  v-if="don.phieuTraHangId == null"
                  class="rounded-full px-3 py-1 text-xs font-semibold"
                  :class="lopTrangThai(don.trangThai)"
                >
                  {{ don.trangThaiText }}
                </span>
                <span v-if="don.phieuTraHangId != null" class="rounded-full px-3 py-1 text-xs font-semibold" :class="lopTrangThaiTraHang(don.trangThaiTraHang)">
                  {{ don.trangThaiTraHangText }}
                </span>
              </div>
            </div>

            <!-- Product list section -->
            <div class="space-y-4 py-1 divide-y divide-slate-100/60">
              <div
                v-for="sp in don.sanPhams"
                :key="sp.giayChiTietId"
                class="flex items-center gap-4 py-3 first:pt-0 last:pb-0"
              >
                <!-- Product Image -->
                <img
                  :src="sp.hinhAnh"
                  alt="Product Image"
                  class="w-16 h-16 object-cover rounded-xl border border-slate-100 bg-slate-50 flex-shrink-0"
                />

                <!-- Product Details -->
                <div class="flex-1 min-w-0">
                  <h4 class="font-semibold text-slate-900 truncate text-sm md:text-base mb-1" :title="sp.ten">
                    {{ sp.ten }}
                  </h4>
                  <p class="text-xs text-slate-400 mb-1">
                    Phân loại hàng: Màu {{ sp.mauSac }}, Size {{ sp.kichCo }}
                  </p>
                  <p class="text-xs text-slate-500 font-semibold">
                    x{{ sp.soLuong }}
                  </p>
                </div>

                <!-- Product Pricing -->
                <div class="text-right">
                  <div v-if="sp.giaNiemYet > sp.giaDonVi" class="text-xs text-slate-400 line-through mr-1">
                    {{ dinhDangTienViet(sp.giaNiemYet) }}
                  </div>
                  <div class="font-bold text-slate-800 text-sm md:text-base">
                    {{ dinhDangTienViet(sp.giaDonVi) }}
                  </div>
                </div>
              </div>
            </div>

            <!-- Bottom section: Totals and Actions -->
            <div class="flex flex-wrap items-center justify-between gap-4 border-t border-slate-100 pt-4">
              <div>
                <span class="text-xs text-slate-500 font-medium mr-1">Thành tiền:</span>
                <span class="text-xl font-bold text-primary">{{ dinhDangTienViet(don.tongThanhToan) }}</span>
              </div>
              <div class="flex items-center gap-2">
                <button
                  v-if="Number(don.trangThai) === 1"
                  @click="guiYeuCauHuy(don)"
                  :disabled="donDangGuiYeuCauHuy === don.id"
                  class="px-5 py-2 text-xs md:text-sm font-semibold text-rose-600 bg-rose-50 border border-rose-200 rounded-xl hover:bg-rose-100 transition shadow-sm disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {{ donDangGuiYeuCauHuy === don.id ? 'Đang gửi...' : 'Yêu cầu hủy' }}
                </button>
                <!-- Cho phép gửi lại sau khi phiếu trước bị từ chối/hủy và vẫn còn trong thời hạn trả hàng. -->
                <button
                  v-if="don.trangThai === 5 && (don.phieuTraHangId == null || [8, 9].includes(don.trangThaiTraHang)) && !laQuaHanTraHang(don)"
                  @click="moYeuCauTraHang(don)"
                  class="px-5 py-2 text-xs md:text-sm font-semibold text-rose-600 bg-rose-50 border border-rose-200 rounded-xl hover:bg-rose-100 transition shadow-sm"
                >
                  Yêu cầu trả hàng
                </button>
                <!-- Mua Lại (Buy Again) button for Completed (5) or Cancelled (6) -->
                <button
                  v-if="don.trangThai === 5 || don.trangThai === 6"
                  @click="muaLai(don)"
                  class="px-5 py-2 text-xs md:text-sm font-semibold text-white bg-primary rounded-xl hover:bg-primary/95 transition shadow-sm"
                >
                  Mua Lại
                </button>
                <button
                  @click="router.push(`/khachhang/don-hang/${don.id}`)"
                  class="px-5 py-2 text-xs md:text-sm font-semibold text-slate-600 bg-slate-50 border border-slate-200 rounded-xl hover:bg-slate-100 transition"
                >
                  Xem chi tiết
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Return Request Modal -->
    <YeuCauTraHangModal
      :isOpen="laMoTraHangModal"
      :don="dangChonDeTra"
      @close="laMoTraHangModal = false"
      @success="xuLyTaoPhieuTraHangThanhCong"
    />
  </main>
</template>

<style scoped>
.orders-six-radius :deep([class*="rounded-"]:not(.rounded-full)) {
  border-radius: 6px !important;
}
</style>
