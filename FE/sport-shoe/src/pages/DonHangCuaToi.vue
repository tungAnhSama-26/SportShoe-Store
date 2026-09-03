<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { layDonHangCuaToi, yeuCauHuyDonHang } from '../services/don-hang';
import { layKhachId, themVaoGio } from '../services/gio-hang';
import { layDanhSachTaiKhoanNganHang } from '../services/client-profile';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showSuccess, showError, showConfirm } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import { resolveMediaUrl } from '../utils/media';
import { ketNoiHoaDonRealtime } from '../services/hoa-don-realtime';
import { Star } from 'lucide-vue-next';
import anhMacDinh from '../assets/login-shoe.png';

const router = useRouter();
const danhSach = ref([]);
const dangTai = ref(true);
const daDangNhap = computed(() => Boolean(layKhachId()));

function xuLyAnhLoi(e) {
  if (e.target.src !== anhMacDinh) e.target.src = anhMacDinh;
}

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

const dsTrangThai = [
  { value: "", label: "Tất cả" },
  { value: 1, label: "Chờ xác nhận" },
  { value: 9, label: "Đã xác nhận" },
  { value: 2, label: "Chờ lấy hàng" },
  { value: 3, label: "Đang giao hàng" },
  { value: 5, label: "Hoàn thành" },
  { value: 6, label: "Đã hủy" },
  { value: 8, label: "Cần hoàn tiền" },
];

const trangThaiDangChon = ref("");

const danhSachHopLe = computed(() => {
  return danhSach.value.filter((d) => [1, 9, 2, 3, 4, 5, 6, 7, 8, 10].includes(d.trangThai));
});

const danhSachHienThi = computed(() => {
  if (trangThaiDangChon.value === "") {
    return danhSachHopLe.value;
  }
  return danhSachHopLe.value.filter(
    (d) => d.trangThai === trangThaiDangChon.value,
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

async function guiYeuCauHuy(don) {
  const laDonDaThanhToan =
    don.daThanhToan === true ||
    don.hinhThucThanhToan === 'CHUYEN_KHOAN';

  if (laDonDaThanhToan) {
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
    laDonDaThanhToan
      ? 'Bạn chắc chắn muốn hủy đơn? Đơn đã thanh toán sẽ được cửa hàng hoàn tiền lại cho bạn.'
      : 'Bạn chắc chắn muốn hủy đơn hàng này? Thao tác không thể hoàn tác.',
    'Hủy đơn hàng',
    'Hủy đơn',
    'Quay lại',
  );
  if (!daXacNhan) return;

  donDangGuiYeuCauHuy.value = don.id;
  try {
    await yeuCauHuyDonHang(don.id);
    await taiDanhSach(true);
    // Sau khi hủy, nhảy sang đúng tab chứa đơn để khách thấy kết quả.
    const donSauKhiHuy = danhSach.value.find((item) => item.id === don.id);
    if (Number(donSauKhiHuy?.trangThai) === 8) {
      // Đơn chuyển khoản đã thanh toán -> cần hoàn tiền.
      trangThaiDangChon.value = 8;
      showSuccess('Đơn hàng đã được hủy. Cửa hàng sẽ hoàn tiền cho bạn.');
    } else {
      if (Number(donSauKhiHuy?.trangThai) === 6) {
        trangThaiDangChon.value = 6;
      }
      showSuccess('Đơn hàng đã được hủy.');
    }
  } catch (error) {
    showError(getDisplayErrorMessage(error, 'Không thể hủy đơn hàng'));
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
                <span class="rounded-full px-3 py-1 text-xs font-semibold" :class="lopTrangThai(don.trangThai)">
                  {{ don.trangThaiText }}
                </span>
              </div>
            </div>

            <!-- Product list section (Click vào chuyển sang chi tiết đơn) -->
            <div
              class="space-y-4 py-1 divide-y divide-slate-100/60 cursor-pointer rounded-xl hover:bg-slate-50/80 p-2 -mx-2 transition"
              @click="router.push(`/khachhang/don-hang/${don.id}`)"
              title="Bấm để xem chi tiết đơn hàng"
            >
              <div
                v-for="sp in don.sanPhams"
                :key="sp.giayChiTietId"
                class="flex items-center gap-4 py-3 first:pt-0 last:pb-0"
              >
                <!-- Product Image -->
                <img
                  :src="resolveMediaUrl(sp.hinhAnh) || anhMacDinh"
                  :alt="sp.ten || 'Sản phẩm'"
                  class="w-16 h-16 object-cover rounded-xl border border-slate-100 bg-slate-50 flex-shrink-0"
                  @error="xuLyAnhLoi"
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
                  {{ donDangGuiYeuCauHuy === don.id ? 'Đang xử lý...' : 'Hủy đơn' }}
                </button>
                <!-- Đánh giá button for Completed (5) -->
                <button
                  v-if="don.trangThai === 5"
                  @click="router.push(`/khachhang/don-hang/${don.id}/danh-gia`)"
                  class="px-5 py-2 text-xs md:text-sm font-semibold text-white bg-gradient-to-r from-rose-500 to-red-500 rounded-xl hover:opacity-95 transition shadow-sm inline-flex items-center gap-1.5"
                >
                  <Star class="h-3.5 w-3.5 fill-white" />
                  Đánh giá
                </button>
                <!-- Mua Lại (Buy Again) button for Completed (5) or Cancelled (6) -->
                <button
                  v-if="don.trangThai === 5 || don.trangThai === 6"
                  @click="muaLai(don)"
                  class="px-5 py-2 text-xs md:text-sm font-semibold text-white bg-primary rounded-xl hover:bg-primary/95 transition shadow-sm"
                >
                  Mua Lại
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

  </main>
</template>

<style scoped>
.orders-six-radius :deep([class*="rounded-"]:not(.rounded-full)) {
  border-radius: 6px !important;
}
</style>
