<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { layChiTietDonHang, guiDanhGiaSanPham } from '../services/don-hang';
import { uploadAnh } from '../services/client-profile';
import { resolveMediaUrl } from '../utils/media';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showWarning, showSuccess, showError } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import anhMacDinh from '../assets/login-shoe.png';

const MAX_MEDIA = 6;

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
      if (!sp.daDanhGia) f[sp.hoaDonChiTietId] = { soSao: 0, noiDung: '', media: [], dangTaiMedia: false };
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
  if (f.dangTaiMedia) return showWarning('Đang tải ảnh/video, vui lòng đợi.');
  dangGui.value = sp.hoaDonChiTietId;
  try {
    await guiDanhGiaSanPham(sp.hoaDonChiTietId, f.soSao, f.noiDung.trim() || null, f.media);
    await taiChiTiet();
    showSuccess('Cảm ơn bạn đã đánh giá sản phẩm!');
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không thể gửi đánh giá'));
  } finally {
    dangGui.value = null;
  }
}

// Chọn ảnh/video -> upload lên server -> lưu {url, loai} vào form.
async function chonMedia(event, id) {
  const f = forms.value[id];
  if (!f) return;
  const files = Array.from(event.target.files || []);
  event.target.value = ''; // reset để chọn lại cùng 1 file vẫn được
  if (!files.length) return;
  if (f.media.length + files.length > MAX_MEDIA) {
    return showWarning(`Tối đa ${MAX_MEDIA} ảnh/video cho mỗi đánh giá.`);
  }
  f.dangTaiMedia = true;
  try {
    for (const file of files) {
      const laVideo = file.type.startsWith('video/');
      const laAnh = file.type.startsWith('image/');
      if (!laVideo && !laAnh) {
        showWarning(`"${file.name}" không phải ảnh hoặc video.`);
        continue;
      }
      if (file.size > 50 * 1024 * 1024) {
        showWarning(`"${file.name}" vượt quá 50MB.`);
        continue;
      }
      const url = await uploadAnh(file, 'customer');
      if (url) f.media.push({ url, loai: laVideo ? 'video' : 'image' });
    }
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Tải ảnh/video lên thất bại'));
  } finally {
    f.dangTaiMedia = false;
  }
}

function xoaMedia(id, index) {
  forms.value[id]?.media.splice(index, 1);
}

function xuLyAnhLoi(event) {
  if (event.target.src !== anhMacDinh) event.target.src = anhMacDinh;
}
</script>

<template>
  <main class="bg-slate-50 min-h-screen pb-20">
    <div class="mx-auto max-w-3xl px-6 lg:px-10 pt-8">
      <button @click="router.push(`/khachhang/don-hang/${route.params.id}`)" class="mb-6 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-primary transition-colors">
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
              <router-link :to="`/khachhang/san-pham/${sp.giayId}`" class="h-20 w-20 shrink-0 overflow-hidden rounded-xl bg-slate-50">
                <img :src="sp.hinhAnh || anhMacDinh" :alt="sp.tenSanPham" class="h-full w-full object-cover" @error="xuLyAnhLoi" />
              </router-link>
              <div class="flex-1">
                <router-link :to="`/khachhang/san-pham/${sp.giayId}`" class="font-semibold text-slate-800 hover:text-primary">{{ sp.tenSanPham }}</router-link>
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
              <div v-if="sp.phanHoiDanhGia" class="mt-2 rounded-xl border-l-2 border-primary bg-white/70 p-3">
                <span class="text-xs font-bold text-primary">Phản hồi từ cửa hàng</span>
                <p class="mt-1 text-sm leading-6 text-slate-600">{{ sp.phanHoiDanhGia }}</p>
              </div>
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

              <!-- Ảnh / video -->
              <div class="mt-3">
                <div v-if="forms[sp.hoaDonChiTietId].media.length" class="mb-2 flex flex-wrap gap-2">
                  <div v-for="(m, idx) in forms[sp.hoaDonChiTietId].media" :key="idx" class="relative h-20 w-20 overflow-hidden rounded-lg bg-slate-100">
                    <video v-if="m.loai === 'video'" :src="resolveMediaUrl(m.url)" class="h-full w-full object-cover" />
                    <img v-else :src="resolveMediaUrl(m.url)" alt="" class="h-full w-full object-cover" />
                    <button type="button" @click="xoaMedia(sp.hoaDonChiTietId, idx)" class="absolute right-0.5 top-0.5 flex h-5 w-5 items-center justify-center rounded-full bg-black/60 text-xs leading-none text-white hover:bg-black/80">×</button>
                  </div>
                </div>
                <label class="inline-flex cursor-pointer items-center gap-2 rounded-xl border border-dashed border-slate-300 px-4 py-2 text-sm text-slate-500 transition hover:border-primary hover:text-primary" :class="{ 'pointer-events-none opacity-60': forms[sp.hoaDonChiTietId].dangTaiMedia }">
                  <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><path d="m17 8-5-5-5 5" /><path d="M12 3v12" /></svg>
                  <span>{{ forms[sp.hoaDonChiTietId].dangTaiMedia ? 'Đang tải...' : 'Thêm ảnh/video' }}</span>
                  <input type="file" accept="image/*,video/*" multiple class="hidden" :disabled="forms[sp.hoaDonChiTietId].dangTaiMedia" @change="chonMedia($event, sp.hoaDonChiTietId)" />
                </label>
                <p class="mt-1 text-xs text-slate-400">Tối đa {{ MAX_MEDIA }} ảnh/video, mỗi tệp ≤ 50MB.</p>
              </div>

              <div class="mt-3 flex justify-end">
                <button @click="gui(sp)" :disabled="dangGui === sp.hoaDonChiTietId || forms[sp.hoaDonChiTietId].dangTaiMedia" class="rounded-xl bg-primary px-6 py-2.5 text-sm font-bold text-white transition hover:bg-primary/90 disabled:opacity-60">
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
