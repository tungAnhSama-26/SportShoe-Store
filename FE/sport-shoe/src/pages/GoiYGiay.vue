<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, ArrowRight, Check, Crown, Loader2, RotateCcw, Sparkles } from 'lucide-vue-next';
import { layCauHoiGoiY, layGoiYGiay } from '../services/goi-y';
import { resolveMediaUrl } from '../utils/media';
import { dinhDangTienViet } from '../utils/dinhDangTien';
import { showError } from '../utils/alert';
import { getDisplayErrorMessage } from '../utils/error-message';
import anhMacDinh from '../assets/login-shoe.png';

const router = useRouter();

const dsCauHoi = ref([]);
const dangTaiCauHoi = ref(true);
const daChon = ref({});          // { [maCauHoi]: string[] } - mỗi câu chọn được NHIỀU đáp án

// Bước hiện tại: 0..(n-1), mỗi bước là một câu hỏi.
const buoc = ref(0);

const dangGoiY = ref(false);
const ketQua = ref(null);

const tongBuoc = computed(() => dsCauHoi.value.length);
const laBuocCuoi = computed(() => buoc.value >= dsCauHoi.value.length - 1);
const cauHoiHienTai = computed(() => dsCauHoi.value[buoc.value] || null);

// Câu hỏi kích cỡ dùng THANH TRƯỢT thay vì ô tick (mỗi người 1 size).
const MA_KICH_CO = 'kich-co';
const laCauHoiSize = computed(() => cauHoiHienTai.value?.ma === MA_KICH_CO);
const chiSoSize = ref(0);
const sizeDangChon = computed(() => {
  const c = dsCauHoi.value.find((x) => x.ma === MA_KICH_CO);
  return c ? c.luaChon[chiSoSize.value] : '';
});

// Kéo thanh trượt -> ghi lại size đang chọn (BE nhận dạng mảng nên bọc trong [ ]).
function doiSize() {
  if (sizeDangChon.value) daChon.value[MA_KICH_CO] = [sizeDangChon.value];
}

// Phải chọn ít nhất 1 đáp án mới cho qua câu tiếp theo.
const daTraLoiCauNay = computed(() => {
  const c = cauHoiHienTai.value;
  return !!c && (daChon.value[c.ma] || []).length > 0;
});

const phanTramTienDo = computed(() =>
  tongBuoc.value
    ? Math.round(((buoc.value + (daTraLoiCauNay.value ? 1 : 0)) / tongBuoc.value) * 100)
    : 0,
);

async function taiCauHoi() {
  dangTaiCauHoi.value = true;
  try {
    dsCauHoi.value = (await layCauHoiGoiY()) || [];
    const khoiTao = {};
    dsCauHoi.value.forEach((c) => { khoiTao[c.ma] = []; });
    daChon.value = khoiTao;
    // Thanh trượt size: đặt sẵn ở giữa dải để khách chỉ cần kéo chỉnh.
    const cauSize = dsCauHoi.value.find((c) => c.ma === MA_KICH_CO);
    if (cauSize && cauSize.luaChon.length) {
      chiSoSize.value = Math.floor((cauSize.luaChon.length - 1) / 2);
      daChon.value[MA_KICH_CO] = [cauSize.luaChon[chiSoSize.value]];
    }
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'Không tải được câu hỏi'));
  } finally {
    dangTaiCauHoi.value = false;
  }
}

onMounted(taiCauHoi);

function chon(maCauHoi, luaChon) {
  const hienTai = daChon.value[maCauHoi] || [];
  daChon.value[maCauHoi] = hienTai.includes(luaChon)
    ? hienTai.filter((x) => x !== luaChon)
    : [...hienTai, luaChon];
}

function daTick(maCauHoi, luaChon) {
  return (daChon.value[maCauHoi] || []).includes(luaChon);
}

function tiep() {
  if (!daTraLoiCauNay.value) return;
  if (!laBuocCuoi.value) buoc.value += 1;
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function lui() {
  if (buoc.value > 0) buoc.value -= 1;
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

async function guiGoiY() {
  if (dangGoiY.value) return;
  dangGoiY.value = true;
  ketQua.value = null;
  try {
    const traLoi = Object.entries(daChon.value)
      .filter(([, v]) => v && v.length)
      .map(([ma, v]) => ({ ma, daChon: v }));
    ketQua.value = await layGoiYGiay({ traLoi });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (e) {
    showError(getDisplayErrorMessage(e, 'AI chưa gợi ý được, thử lại nhé'));
  } finally {
    dangGoiY.value = false;
  }
}

function lamLai() {
  const khoiTao = {};
  dsCauHoi.value.forEach((c) => { khoiTao[c.ma] = []; });
  daChon.value = khoiTao;
  ketQua.value = null;
  buoc.value = 0;
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function xemSanPham(giayId) {
  router.push(`/khachhang/san-pham/${giayId}`);
}

// Đôi hợp nhất = phần tử đầu (BE yêu cầu AI xếp theo độ phù hợp giảm dần).
const hopNhat = computed(() => ketQua.value?.sanPhams?.[0] || null);
const conLai = computed(() => (ketQua.value?.sanPhams || []).slice(1));

function anhSP(url) {
  return resolveMediaUrl(url) || anhMacDinh;
}
function anhLoi(e) {
  if (e.target.src !== anhMacDinh) e.target.src = anhMacDinh;
}
</script>

<template>
  <div class="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
    <!-- Tiêu đề -->
    <div class="text-center">
      <h1 class="mt-4 text-2xl font-bold text-slate-900 sm:text-3xl">Gợi ý giày cho bạn</h1>
    </div>

    <div v-if="dangTaiCauHoi" class="py-20 text-center text-sm text-slate-400">Đang tải câu hỏi...</div>

    <template v-else>
      <!-- ===== Phần hỏi: mỗi lần chỉ 1 bước ===== -->
      <div v-if="!ketQua" class="mt-8">
        <!-- Thanh tiến độ -->
        <div class="mb-5">
          <div class="mb-2 flex items-center justify-between text-xs font-semibold text-slate-500">
            <span>Bước {{ Math.min(buoc + 1, tongBuoc) }} / {{ tongBuoc }}</span>
            <span>{{ phanTramTienDo }}%</span>
          </div>
          <div class="h-2 w-full overflow-hidden rounded-full bg-slate-100">
            <div
              class="h-full rounded-full bg-gradient-to-r from-rose-500 to-red-500 transition-all duration-300"
              :style="{ width: phanTramTienDo + '%' }"
            ></div>
          </div>
        </div>

        <!-- Một câu hỏi -->
        <section v-if="cauHoiHienTai" :key="cauHoiHienTai.ma" class="rounded-3xl border border-slate-100 bg-white p-5 shadow-sm sm:p-7">
          <h2 class="text-lg font-bold text-slate-800">{{ cauHoiHienTai.cauHoi }}</h2>

          <!-- Câu kích cỡ: THANH TRƯỢT -->
          <div v-if="laCauHoiSize" class="mt-8 pb-2">
            <div class="text-center">
              <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Size của bạn</span>
              <p class="mt-1 text-5xl font-extrabold text-primary">{{ sizeDangChon }}</p>
            </div>

            <input
              type="range"
              min="0"
              :max="cauHoiHienTai.luaChon.length - 1"
              step="1"
              v-model.number="chiSoSize"
              @input="doiSize"
              class="thanh-truot-size mt-6 w-full"
            />

            <div class="mt-2 flex justify-between px-0.5">
              <button
                v-for="(lc, i) in cauHoiHienTai.luaChon"
                :key="lc"
                type="button"
                @click="chiSoSize = i; doiSize()"
                :class="[
                  'w-9 rounded-lg py-1 text-xs font-bold transition',
                  chiSoSize === i ? 'bg-primary/10 text-primary' : 'text-slate-400 hover:text-primary',
                ]"
              >
                {{ lc }}
              </button>
            </div>
          </div>

          <!-- Các câu còn lại: ô tick chọn nhiều -->
          <div v-else class="mt-5 grid gap-2.5 sm:grid-cols-2">
            <button
              v-for="lc in cauHoiHienTai.luaChon"
              :key="lc"
              type="button"
              @click="chon(cauHoiHienTai.ma, lc)"
              :class="[
                'flex items-center gap-2.5 rounded-2xl border px-4 py-3 text-left text-sm font-medium transition',
                daTick(cauHoiHienTai.ma, lc)
                  ? 'border-primary bg-primary/5 text-primary'
                  : 'border-slate-200 text-slate-600 hover:border-primary/40 hover:bg-slate-50',
              ]"
            >
              <span
                :class="[
                  'flex h-5 w-5 shrink-0 items-center justify-center rounded-md border transition',
                  daTick(cauHoiHienTai.ma, lc) ? 'border-primary bg-primary text-white' : 'border-slate-300',
                ]"
              >
                <Check v-if="daTick(cauHoiHienTai.ma, lc)" class="h-3.5 w-3.5" />
              </span>
              <span>{{ lc }}</span>
            </button>
          </div>
        </section>

        <!-- Điều hướng -->
        <div class="mt-5 flex items-center gap-3">
          <button
            v-if="buoc > 0"
            type="button"
            @click="lui"
            class="flex items-center gap-2 rounded-2xl border border-slate-200 px-5 py-3.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
          >
            <ArrowLeft class="h-4 w-4" /> Quay lại
          </button>

          <button
            v-if="!laBuocCuoi"
            type="button"
            @click="tiep"
            :disabled="!daTraLoiCauNay"
            class="flex flex-1 items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3.5 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5 disabled:translate-y-0 disabled:opacity-50"
          >
            Tiếp tục <ArrowRight class="h-4 w-4" />
          </button>

          <button
            v-else
            type="button"
            @click="guiGoiY"
            :disabled="dangGoiY || !daTraLoiCauNay"
            class="flex flex-1 items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 py-3.5 text-sm font-bold text-white shadow-lg shadow-primary/25 transition hover:-translate-y-0.5 disabled:translate-y-0 disabled:opacity-60"
          >
            <Loader2 v-if="dangGoiY" class="h-4 w-4 animate-spin" />
            <Sparkles v-else class="h-4 w-4" />
            {{ dangGoiY ? 'Đang phân tích giày phù hợp với bạn...' : 'Xem giày phù hợp' }}
          </button>
        </div>
      </div>

      <!-- ===== Kết quả ===== -->
      <div v-else class="mt-8 space-y-6">
        <section class="rounded-3xl border border-primary/20 bg-primary/5 p-5 sm:p-6">
          <div class="flex items-start gap-3">
            <Sparkles class="mt-0.5 h-5 w-5 shrink-0 text-primary" />
            <div class="min-w-0 space-y-2">
              <p class="text-sm leading-relaxed text-slate-700">{{ ketQua.loiKhuyen }}</p>
            </div>
          </div>
        </section>

        <!-- Đôi HỢP NHẤT: hiển thị TO -->
        <section v-if="hopNhat">
          <div class="mb-3 flex items-center gap-2">
            <Crown class="h-4 w-4 text-amber-500" />
            <h2 class="text-sm font-bold uppercase tracking-wide text-slate-700">Phù hợp nhất với bạn</h2>
          </div>

          <button
            type="button"
            @click="xemSanPham(hopNhat.giayId)"
            class="group grid w-full overflow-hidden rounded-3xl border-2 border-primary/30 bg-white text-left shadow-md transition hover:-translate-y-0.5 hover:shadow-xl sm:grid-cols-2"
          >
            <div class="relative">
              <img
                :src="anhSP(hopNhat.hinhAnh)"
                :alt="hopNhat.ten"
                @error="anhLoi"
                class="h-72 w-full bg-slate-100 object-cover transition group-hover:scale-[1.02] sm:h-full sm:min-h-[23rem]"
              />
              <span class="absolute left-4 top-4 rounded-full bg-amber-400 px-3 py-1 text-[11px] font-bold text-amber-950 shadow">
                ⭐ Hợp nhất
              </span>
            </div>
            <div class="flex flex-col justify-center gap-3 p-6">
              <p class="text-xl font-bold leading-snug text-slate-900">{{ hopNhat.ten }}</p>
              <p class="text-2xl font-extrabold text-primary">{{ dinhDangTienViet(hopNhat.giaBan) }}</p>
              <p v-if="hopNhat.lyDo" class="text-sm leading-relaxed text-slate-600">{{ hopNhat.lyDo }}</p>
              <span class="mt-1 inline-flex items-center gap-1 text-sm font-bold text-primary">
                Xem chi tiết <ArrowRight class="h-4 w-4" />
              </span>
            </div>
          </button>
        </section>

        <!-- Các lựa chọn khác: hiển thị NHỎ -->
        <section v-if="conLai.length">
          <h2 class="mb-3 text-xs font-bold uppercase tracking-wide text-slate-400">
            Lựa chọn khác, ít phù hợp hơn
          </h2>
          <div class="grid grid-cols-2 gap-3 sm:grid-cols-3">
            <button
              v-for="sp in conLai"
              :key="sp.giayId"
              type="button"
              @click="xemSanPham(sp.giayId)"
              class="group overflow-hidden rounded-2xl border border-slate-100 bg-white text-left shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
            >
              <img
                :src="anhSP(sp.hinhAnh)"
                :alt="sp.ten"
                @error="anhLoi"
                class="h-28 w-full bg-slate-100 object-cover transition group-hover:scale-[1.02]"
              />
              <div class="space-y-1 p-3">
                <p class="line-clamp-2 text-xs font-semibold leading-snug text-slate-800">{{ sp.ten }}</p>
                <p class="text-sm font-bold text-primary">{{ dinhDangTienViet(sp.giaBan) }}</p>
                <p v-if="sp.lyDo" class="line-clamp-2 text-[11px] leading-relaxed text-slate-500">{{ sp.lyDo }}</p>
              </div>
            </button>
          </div>
        </section>

        <button
          type="button"
          @click="lamLai"
          class="flex w-full items-center justify-center gap-2 rounded-2xl border border-slate-200 px-6 py-3.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
        >
          <RotateCcw class="h-4 w-4" /> Làm lại từ đầu
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* Thanh trượt chọn size - tô màu thương hiệu cho cả Chrome lẫn Firefox */
.thanh-truot-size {
  -webkit-appearance: none;
  appearance: none;
  height: 8px;
  border-radius: 9999px;
  background: #e2e8f0;
  outline: none;
  cursor: pointer;
}
.thanh-truot-size::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 28px;
  height: 28px;
  border-radius: 9999px;
  background: linear-gradient(135deg, #f43f5e, #dc2626);
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(207, 16, 24, 0.4);
  cursor: pointer;
}
.thanh-truot-size::-moz-range-thumb {
  width: 22px;
  height: 22px;
  border-radius: 9999px;
  background: linear-gradient(135deg, #f43f5e, #dc2626);
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(207, 16, 24, 0.4);
  cursor: pointer;
}
</style>
