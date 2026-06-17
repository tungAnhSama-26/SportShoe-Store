<script setup>
import { onMounted, onUnmounted, ref } from 'vue';

defineProps({
  thongKe: {
    type: Array,
    required: true,
  },
});

// Bộ ảnh nền banner - tự chuyển sau 5 giây, có nút mũi tên + chấm đổi qua lại.
const ANH_BANNER = [
  'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1800&q=80',
  'https://images.unsplash.com/photo-1552346154-21d32810aba3?auto=format&fit=crop&w=1800&q=80',
  'https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?auto=format&fit=crop&w=1800&q=80',
  'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=1800&q=80',
];

const anhHienTai = ref(0);
let tuDongTimer = null;

function chuyenAnh(huong) {
  anhHienTai.value = (anhHienTai.value + huong + ANH_BANNER.length) % ANH_BANNER.length;
  datLaiTuDong();
}

function chonAnh(i) {
  anhHienTai.value = i;
  datLaiTuDong();
}

// Bấm tay thì đếm lại 5 giây từ đầu để ảnh không nhảy ngay sau đó.
function datLaiTuDong() {
  if (tuDongTimer) clearInterval(tuDongTimer);
  tuDongTimer = setInterval(() => {
    anhHienTai.value = (anhHienTai.value + 1) % ANH_BANNER.length;
  }, 5000);
}

onMounted(datLaiTuDong);
onUnmounted(() => {
  if (tuDongTimer) clearInterval(tuDongTimer);
});
</script>

<template>
  <section class="bg-background">
    <div class="w-full overflow-hidden bg-black">
      <div class="relative min-h-[500px] lg:min-h-[620px]">
        <!-- Các ảnh nền chồng lên nhau, ảnh đang chọn hiện rõ (fade) -->
        <img
          v-for="(anh, i) in ANH_BANNER"
          :key="anh"
          :src="anh"
          alt="Giày thể thao SportShoe"
          class="absolute inset-0 h-full w-full object-cover object-[62%_center] transition-opacity duration-700"
          :class="i === anhHienTai ? 'opacity-100' : 'opacity-0'"
        />
        <div class="absolute inset-0 bg-[linear-gradient(90deg,rgba(18,6,8,0.82)_0%,rgba(18,6,8,0.68)_26%,rgba(18,6,8,0.22)_56%,rgba(18,6,8,0.34)_100%)]"></div>

        <!-- Nút chuyển ảnh trái / phải -->
        <button
          @click="chuyenAnh(-1)"
          aria-label="Ảnh trước"
          class="absolute left-4 top-1/2 z-10 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border border-white/25 bg-white/10 text-white backdrop-blur-sm transition hover:bg-white/25"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6" /></svg>
        </button>
        <button
          @click="chuyenAnh(1)"
          aria-label="Ảnh sau"
          class="absolute right-4 top-1/2 z-10 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border border-white/25 bg-white/10 text-white backdrop-blur-sm transition hover:bg-white/25"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m9 18 6-6-6-6" /></svg>
        </button>

        <!-- Chấm điều hướng -->
        <div class="absolute bottom-5 left-1/2 z-10 flex -translate-x-1/2 gap-2">
          <button
            v-for="(anh, i) in ANH_BANNER"
            :key="'cham-' + i"
            @click="chonAnh(i)"
            :aria-label="`Ảnh ${i + 1}`"
            class="h-2 rounded-full transition-all"
            :class="i === anhHienTai ? 'w-6 bg-white' : 'w-2 bg-white/40 hover:bg-white/70'"
          ></button>
        </div>

        <div class="relative mx-auto flex min-h-[500px] max-w-7xl items-center px-6 pb-14 pt-5 lg:min-h-[620px] lg:px-10 lg:pb-20">
          <div class="max-w-[17rem] text-white sm:max-w-[19rem] lg:max-w-[23rem]">
            <span class="inline-flex rounded-full border border-white/20 bg-white/10 px-4 py-2 text-[11px] font-medium backdrop-blur-sm">
              Bộ sưu tập mới 2026
            </span>
            <p class="mt-9 text-sm font-medium text-secondary">Giày thể thao chính hãng</p>
            <h1 class="mt-6 text-[1.7rem] font-medium leading-[1.12] sm:text-[1.95rem] lg:text-[2.45rem]">
              Bứt phá mọi bước chân cùng SportShoe
            </h1>
            <p class="mt-4 text-sm leading-6 text-slate-300">
              Sneaker chính hãng từ các thương hiệu hàng đầu — giao nhanh toàn quốc, đổi trả dễ dàng.
            </p>
            <div class="mt-6 flex flex-wrap gap-3">
              <router-link
                id="mua-ngay"
                to="/khachhang/san-pham"
                class="inline-flex items-center gap-2 rounded-xl bg-primary px-5 py-2.5 text-[13px] font-semibold text-white shadow-lg shadow-primary/30 transition-all hover:-translate-y-0.5 hover:shadow-primary/50"
              >
                Mua ngay
                <span aria-hidden="true">→</span>
              </router-link>
              <a
                href="#bo-suu-tap"
                class="inline-flex items-center rounded-xl border border-white/40 bg-white/5 px-5 py-2.5 text-[13px] font-semibold text-white backdrop-blur-sm transition-all hover:bg-white/20 hover:border-white/60"
              >
                Khám phá theo hãng
              </a>
            </div>

            <div class="mt-8 grid max-w-xl grid-cols-3 gap-7 sm:gap-9">
              <div v-for="chiSo in thongKe" :key="chiSo.nhan">
                <p class="text-base font-semibold text-secondary sm:text-xl">{{ chiSo.so }}</p>
                <p class="mt-2 whitespace-nowrap text-[11px] leading-5 text-slate-300 sm:text-xs">{{ chiSo.nhan }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
