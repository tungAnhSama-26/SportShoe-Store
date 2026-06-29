<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { traCuuDonHangTheoMa } from '../services/don-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { getDisplayErrorMessage } from '../utils/error-message';
import { resolveMediaUrl } from '../utils/media';
import {
  CAC_BUOC_DON_HANG,
  layCauHinhTrangThaiDonHang,
  layViTriTienTrinhDonHang,
} from '../utils/order-status';
import anhMacDinh from '../assets/login-shoe.png';

const route = useRoute();
const maTimKiem = ref('');
const don = ref(null);
const dangTai = ref(false);
const loi = ref('');
const laDonMoiDat = ref(false); // hiện banner cảm ơn khi vừa đặt xong

// ===== Thanh tiến trình trạng thái đơn (giống màn chi tiết đơn của khách đăng nhập) =====
const viTriHienTai = computed(() => layViTriTienTrinhDonHang(don.value?.trangThai));

const cauHinhTrangThai = computed(() =>
  don.value
    ? layCauHinhTrangThaiDonHang(don.value?.trangThai)
    : { hienStepper: false, tieuDe: '', moTa: '', lopMau: '' },
);

function chuanHoaTrangThai(value) {
  const tt = String(value || '')
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .replace(/\s+/g, ' ')
    .trim();
  return tt === 'cho giao hang' ? 'dang giao hang' : tt;
}

const thongTinCacBuoc = computed(() => {
  const lichSu = Array.isArray(don.value?.lichSuTrangThai) ? don.value.lichSuTrangThai : [];
  return CAC_BUOC_DON_HANG.map((buoc, index) => {
    const banGhi = lichSu.find(
      (item) => chuanHoaTrangThai(item?.trangThai) === chuanHoaTrangThai(buoc.ten),
    );
    if (banGhi) return { ...buoc, thoiGian: banGhi.ngayTao };
    if (index === 0) return { ...buoc, thoiGian: don.value?.ngayLap };
    if (index + 1 === viTriHienTai.value) return { ...buoc, thoiGian: don.value?.ngayCapNhat };
    return { ...buoc, thoiGian: null };
  });
});

function formatGioBuoc(iso) {
  if (!iso) return '';
  return new Intl.DateTimeFormat('vi-VN', {
    timeZone: 'Asia/Bangkok', hour: '2-digit', minute: '2-digit', hour12: false,
  }).format(new Date(iso));
}
function formatNgayBuoc(iso) {
  if (!iso) return '';
  return new Intl.DateTimeFormat('vi-VN', {
    timeZone: 'Asia/Bangkok', day: '2-digit', month: '2-digit', year: 'numeric',
  }).format(new Date(iso));
}

function nhanHinhThuc(ht) {
  return ht === 'CHUYEN_KHOAN' ? 'Chuyển khoản (VietQR/VNPAY)' : 'Thanh toán khi nhận hàng (COD)';
}

function formatNgay(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString('vi-VN');
  } catch {
    return '';
  }
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) event.target.src = anhMacDinh;
}

async function traCuu(maInput) {
  const ma = String(maInput ?? maTimKiem.value).trim();
  if (!ma) {
    loi.value = 'Vui lòng nhập mã hóa đơn.';
    return;
  }
  dangTai.value = true;
  loi.value = '';
  try {
    don.value = await traCuuDonHangTheoMa(ma);
    maTimKiem.value = ma;
  } catch (e) {
    don.value = null;
    loi.value = getDisplayErrorMessage(e, 'Không tìm thấy đơn hàng với mã này.');
  } finally {
    dangTai.value = false;
  }
}

onMounted(() => {
  const ma = route.query.ma;
  if (ma) {
    laDonMoiDat.value = route.query.moi === '1';
    traCuu(ma);
  }
});
</script>

<template>
  <main class="orders-six-radius bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-3xl px-6 lg:px-10 pt-10">
      <h1 class="text-3xl font-bold text-slate-900 mb-2">Theo dõi đơn hàng</h1>
      <p class="text-sm text-slate-400 mb-8">Nhập mã hóa đơn để tra cứu thông tin đơn hàng của bạn (không cần đăng nhập).</p>

      <!-- Banner cảm ơn sau khi đặt hàng -->
      <div v-if="laDonMoiDat && don" class="mb-6 rounded-2xl border border-emerald-100 bg-emerald-50 px-6 py-5 text-center">
        <div class="mx-auto mb-2 flex h-12 w-12 items-center justify-center rounded-full bg-emerald-100">
          <svg class="h-6 w-6 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5" /></svg>
        </div>
        <p class="text-lg font-bold text-emerald-700">Cảm ơn quý khách đã mua hàng!</p>
        <p class="mt-1 text-sm text-slate-600">Mã hóa đơn của bạn là <span class="font-bold text-slate-900">{{ don.ma }}</span></p>
        <p class="mt-1 text-xs text-slate-500">Vui lòng lưu lại mã này để tra cứu đơn hàng sau.</p>
      </div>

      <!-- Ô tra cứu -->
      <div class="mb-6 flex flex-col gap-2 sm:flex-row">
        <input
          v-model="maTimKiem"
          type="text"
          placeholder="Nhập mã hóa đơn, ví dụ: HD..."
          class="h-12 flex-1 rounded-2xl border border-slate-200 bg-white px-4 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
          @keyup.enter="traCuu()"
        />
        <button
          :disabled="dangTai"
          class="h-12 rounded-2xl bg-primary px-6 text-sm font-bold text-white transition hover:bg-primary/90 disabled:opacity-60"
          @click="traCuu()"
        >
          {{ dangTai ? 'Đang tra...' : 'Tra cứu' }}
        </button>
      </div>

      <div v-if="loi" class="rounded-2xl border border-rose-100 bg-rose-50 px-4 py-3 text-sm text-rose-600">
        {{ loi }}
      </div>

      <div v-if="dangTai" class="py-12 text-center text-sm text-slate-400">Đang tải đơn hàng...</div>

      <!-- Hóa đơn read-only -->
      <div v-else-if="don" class="space-y-6">
        <!-- Trạng thái: thanh tiến trình (đỏ dần theo bước đơn đã đạt) -->
        <section class="overflow-hidden rounded-3xl border border-slate-100 bg-white px-5 py-5 shadow-sm lg:px-7">
          <div class="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 pb-4">
            <div>
              <p class="text-xs text-slate-400">Mã hóa đơn</p>
              <p class="text-lg font-bold text-slate-900">{{ don.ma }}</p>
            </div>
            <p class="text-xs text-slate-400">Đặt lúc: {{ formatNgay(don.ngayLap) }}</p>
          </div>

          <!-- Trạng thái đặc biệt (đã hủy / cần hoàn tiền...) -->
          <div
            v-if="!cauHinhTrangThai.hienStepper"
            class="mt-5 flex items-center gap-3 rounded-2xl px-5 py-4"
            :class="cauHinhTrangThai.lopMau"
          >
            <svg class="h-6 w-6 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6M9 9l6 6"/></svg>
            <div>
              <p class="font-bold">{{ cauHinhTrangThai.tieuDe || don.trangThaiText }}</p>
              <p v-if="cauHinhTrangThai.moTa" class="text-sm opacity-75">{{ cauHinhTrangThai.moTa }}</p>
            </div>
          </div>

          <!-- Thanh tiến trình -->
          <div v-else class="relative mx-auto mt-7 w-full max-w-5xl overflow-x-auto px-2 pt-2">
            <div class="relative min-w-[560px]">
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
                  <div class="mt-1 min-h-[32px] text-[11px] leading-4 text-slate-400">
                    <p v-if="buoc.thoiGian">{{ formatGioBuoc(buoc.thoiGian) }} {{ formatNgayBuoc(buoc.thoiGian) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Thông tin nhận hàng -->
        <section class="rounded-3xl bg-white border border-slate-100 p-6 shadow-sm">
          <h2 class="mb-3 text-base font-bold text-slate-800">Thông tin nhận hàng</h2>
          <div class="rounded-2xl bg-slate-50 px-5 py-4 text-sm text-slate-600">
            <p class="font-semibold text-slate-800">{{ don.tenNguoiNhan }} · {{ don.sdtNguoiNhan }}</p>
            <p class="mt-1">{{ don.diaChiGiaoHang }}</p>
            <p class="mt-2 text-xs text-slate-500">Phương thức thanh toán: <span class="font-medium text-slate-700">{{ nhanHinhThuc(don.hinhThucThanhToan) }}</span></p>
          </div>
        </section>

        <!-- Sản phẩm -->
        <section class="rounded-3xl bg-white border border-slate-100 p-6 shadow-sm">
          <h2 class="mb-4 text-base font-bold text-slate-800">Sản phẩm</h2>
          <div class="space-y-4">
            <div v-for="(sp, i) in don.sanPhams" :key="sp.hoaDonChiTietId ?? i" class="flex gap-4">
              <img :src="resolveMediaUrl(sp.hinhAnh) || anhMacDinh" :alt="sp.tenSanPham" class="h-16 w-16 shrink-0 rounded-xl object-cover bg-slate-50" @error="xuLyAnhLoi" />
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

          <!-- Phân tích giá -->
          <div class="mt-5 space-y-2 border-t border-slate-100 pt-4 text-sm">
            <div class="flex items-center justify-between text-slate-500">
              <span>Tạm tính</span>
              <span>{{ dinhDangTienViet(don.tamTinh) }}</span>
            </div>
            <div v-if="Number(don.giamDotGiamGia) > 0" class="flex items-center justify-between text-emerald-600">
              <span>Giảm theo đợt giảm giá</span>
              <span>-{{ dinhDangTienViet(don.giamDotGiamGia) }}</span>
            </div>
            <div v-if="Number(don.giamVoucher) > 0" class="flex items-center justify-between text-emerald-600">
              <span>Giảm voucher{{ don.maPhieuGiamGia ? ' (' + don.maPhieuGiamGia + ')' : '' }}</span>
              <span>-{{ dinhDangTienViet(don.giamVoucher) }}</span>
            </div>
            <div class="flex items-center justify-between text-slate-500">
              <span>Phí vận chuyển</span>
              <span>{{ dinhDangTienViet(don.phiVanChuyen) }}</span>
            </div>
            <div class="flex items-center justify-between border-t border-slate-100 pt-2">
              <span class="text-sm font-semibold text-slate-700">Tổng thanh toán</span>
              <span class="text-xl font-bold text-primary">{{ dinhDangTienViet(don.tongThanhToan) }}</span>
            </div>
          </div>
        </section>

        <!-- Lịch sử trạng thái -->
        <section v-if="don.lichSuTrangThai && don.lichSuTrangThai.length" class="rounded-3xl bg-white border border-slate-100 p-6 shadow-sm">
          <h2 class="mb-4 text-base font-bold text-slate-800">Lịch sử đơn hàng</h2>
          <ul class="space-y-3">
            <li v-for="(ls, i) in don.lichSuTrangThai" :key="i" class="flex items-start gap-3 text-sm">
              <span class="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-primary"></span>
              <div>
                <p class="font-medium text-slate-700">{{ ls.trangThai }}</p>
                <p class="text-xs text-slate-400">{{ formatNgay(ls.ngayTao) }}<span v-if="ls.maNhanVien"> · {{ ls.maNhanVien }}</span></p>
              </div>
            </li>
          </ul>
        </section>
      </div>
    </div>
  </main>
</template>
