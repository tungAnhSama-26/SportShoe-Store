<script setup lang="ts">
import type { ThongKeTrangChu } from "../../types/trang-chu";

defineProps<{
  thongKe: ThongKeTrangChu[];
}>();
</script>

<template>
  <section class="bg-[#fff8f4]">
    <div class="w-full overflow-hidden bg-[#A00417]">
      <div class="relative min-h-[500px] lg:min-h-[620px]">
        <img
          src="https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1800&q=80"
          alt="Giày thể thao"
          class="absolute inset-0 h-full w-full object-cover object-[62%_center]"
        />
        <div class="absolute inset-0 bg-[linear-gradient(90deg,rgba(18,6,8,0.82)_0%,rgba(18,6,8,0.68)_26%,rgba(18,6,8,0.22)_56%,rgba(18,6,8,0.34)_100%)]"></div>

        <div class="relative mx-auto flex min-h-[500px] max-w-7xl items-center px-6 pb-14 pt-5 lg:min-h-[620px] lg:px-10 lg:pb-20">
          <div class="max-w-[17rem] text-white sm:max-w-[19rem] lg:max-w-[21rem]">
            <span class="inline-flex rounded-full border border-white/20 bg-white/10 px-4 py-2 text-[11px] font-medium backdrop-blur-sm">
              Bộ sưu tập mới 2026
            </span>
            <p class="mt-9 text-sm font-medium text-orange-200">Bước vào phong cách riêng</p>
            <h1 class="mt-6 max-w-[9ch] text-[1.7rem] font-medium leading-[1.12] sm:max-w-[10ch] sm:text-[1.95rem] lg:max-w-[11ch] lg:text-[2.45rem]">
              Bứt phá phong cách với Sneaker mới
            </h1>
            <div class="mt-6 flex flex-wrap gap-3">
              <a
                id="mua-ngay"
                href="#bo-suu-tap"
                class="inline-flex items-center gap-2 rounded-md bg-[#cf1018] px-3.5 py-2 text-[13px] font-semibold text-white transition hover:bg-[#b30d14]"
              >
                Mua ngay
                <span aria-hidden="true">→</span>
              </a>
              <a
                href="#noi-bat"
                class="inline-flex items-center rounded-md border border-white/70 px-3.5 py-2 text-[13px] font-semibold text-white transition hover:bg-white/10"
              >
                Khám phá bộ sưu tập
              </a>
            </div>

            <div class="mt-8 grid max-w-xl grid-cols-3 gap-7 sm:gap-9">
              <div v-for="chiSo in thongKe" :key="chiSo.nhan">
                <p class="text-base font-semibold text-[#ffb27d] sm:text-lg">{{ chiSo.so }}</p>
                <p class="mt-2 whitespace-nowrap text-[11px] leading-5 text-white/80 sm:text-xs">{{ chiSo.nhan }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
