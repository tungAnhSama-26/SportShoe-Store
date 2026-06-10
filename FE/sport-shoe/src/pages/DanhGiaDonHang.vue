<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { layChiTietDonHang, guiDanhGiaSanPham } from '../services/don-hang';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showWarning, showSuccess, showError } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import anhMacDinh from '../assets/login-shoe.png';

const route = useRoute();
const router = useRouter();

const don = ref(null);
const dangTai = ref(true);
const loi = ref('');
const forms = ref({});
const dangGui = ref(null);

onMounted(taiChiTiet);

async function taiChiTiet() {
  dangTai.value = true;
  loi.value = '';
  try {
    don.value = await layChiTietDonHang(route.params.id);
    // Khởi tạo form đánh giá cho từng sản phẩm chưa đánh giá.
    const f = {};
    (don.value.sanPhams || []).forEach((sp) => {
      if (!sp.daDanhGia) f[sp.hoaDonChiTietId] = { soSao: 0, noiDung: '' };
    });
    forms.value = f;
  } catch {
    don.value = null;
    loi.value = 'Không tải được đơn hàng này.';
  } finally {
    dangTai.value = false;
  }
}

async function gui(sp) {
  const f = forms.value[sp.hoaDonChiTietId];
  if (!f || !f.soSao) return showWarning('Vui lòng chọn số sao.');
  dangGui.value = sp.hoaDonChiTietId;
  try {
    await guiDanhGiaSanPham(sp.hoaDonChiTietId, f.soSao, f.noiDung.trim() || null);
    await taiChiTiet();
    showSuccess('Cảm ơn bạn đã đánh giá sản phẩm!');
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể gửi đánh giá'));
  } finally {
    dangGui.value = null;
  }
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) event.target.src = anhMacDinh;
}
</script>

<template>
  <main class="bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-3xl px-6 lg:px-10 pt-8">
      <button @click="router.push(`/don-hang/${route.params.id}`)" class="mb-6 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-primary transition-colors">
        <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6" /></svg>
        Quay lại đơn hàng
      </button>

      <div v-if="dangTai" class="py-28 text-center text-sm text-slate-400">Đang tải...</div>
      <div v-else-if="loi || !don" class="py-28 text-center text-sm text-rose-500">{{ loi || 'Không tìm thấy đơn hàng.' }}</div>

      <template v-else>
        <h1 class="text-2xl font-bold text-slate-900">Đánh giá đơn hàng #{{ don.ma }}</h1>
        <p class="mt-1 text-sm text-slate-400">Chia sẻ cảm nhận của bạn về các sản phẩm đã mua.</p>

        <div class="mt-6 space-y-5">
          <div v-for="sp in don.sanPhams" :key="sp.hoaDonChiTietId" class="rounded-3xl bg-white border border-slate-100 p-6 shadow-sm">
            <!-- Thông tin sản phẩm -->
            <div class="flex gap-4">
              <router-link :to="`/san-pham/${sp.giayId}`" class="h-20 w-20 shrink-0 overflow-hidden rounded-xl bg-slate-50">
                <img :src="sp.hinhAnh || anhMacDinh" :alt="sp.tenSanPham" class="h-full w-full object-cover" @error="xuLyAnhLoi" />
              </router-link>
              <div class="flex-1">
                <router-link :to="`/san-pham/${sp.giayId}`" class="font-semibold text-slate-800 hover:text-primary">{{ sp.tenSanPham }}</router-link>
                <p class="mt-1 text-xs text-slate-400">{{ sp.mauSac }} · {{ sp.kichCo }} · x{{ sp.soLuong }}</p>
                <p class="mt-1 text-sm font-semibold text-primary">{{ dinhDangTienViet(sp.giaDonVi) }}</p>
              </div>
            </div>

            <!-- Đã đánh giá -->
            <div v-if="sp.daDanhGia" class="mt-4 rounded-2xl bg-emerald-50/60 p-4">
              <div class="flex items-center gap-2">
                <div class="flex">
                  <span v-for="i in 5" :key="i" class="text-sm" :class="i <= sp.soSao ? 'text-amber-400' : 'text-slate-300'">★</span>
                </div>
                <span class="text-xs font-semibold text-emerald-600">Đã đánh giá</span>
              </div>
              <p v-if="sp.noiDungDanhGia" class="mt-1.5 text-sm text-slate-600">{{ sp.noiDungDanhGia }}</p>
            </div>

            <!-- Form đánh giá -->
            <div v-else-if="forms[sp.hoaDonChiTietId]" class="mt-4 border-t border-slate-100 pt-4">
              <p class="text-sm font-semibold text-slate-700 mb-2">Chọn số sao</p>
              <div class="flex items-center gap-1 mb-3">
                <button v-for="i in 5" :key="i" @click="forms[sp.hoaDonChiTietId].soSao = i" type="button" class="text-2xl transition" :class="i <= forms[sp.hoaDonChiTietId].soSao ? 'text-amber-400' : 'text-slate-300 hover:text-amber-300'">★</button>
              </div>
              <textarea
                v-model="forms[sp.hoaDonChiTietId].noiDung"
                rows="3"
                maxlength="1000"
                placeholder="Cảm nhận của bạn về sản phẩm..."
                class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 resize-none"
              ></textarea>
              <div class="mt-3 flex justify-end">
                <button @click="gui(sp)" :disabled="dangGui === sp.hoaDonChiTietId" class="rounded-xl bg-primary px-6 py-2.5 text-sm font-bold text-white transition hover:bg-primary/90 disabled:opacity-60">
                  {{ dangGui === sp.hoaDonChiTietId ? 'Đang gửi...' : 'Gửi đánh giá' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </main>
</template>
