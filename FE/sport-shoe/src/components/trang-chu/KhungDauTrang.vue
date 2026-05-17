<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import logoChinh from "../../assets/logo/delete-background-logo.png";

defineProps({
  thuongHieu: {
    type: String,
    required: true,
  },
});

const dangCuon = ref(false);
const menuMo = ref(false);

function capNhatTrangThaiCuon() {
  dangCuon.value = window.scrollY > 12;
}

function toggleMenu() {
  menuMo.value = !menuMo.value;
  if (menuMo.value) {
    document.body.style.overflow = "hidden";
  } else {
    document.body.style.overflow = "";
  }
}

onMounted(() => {
  capNhatTrangThaiCuon();
  window.addEventListener("scroll", capNhatTrangThaiCuon, { passive: true });
});

onUnmounted(() => {
  window.removeEventListener("scroll", capNhatTrangThaiCuon);
  document.body.style.overflow = "";
});
</script>

<template>
  <header
    :class="[
      'fixed inset-x-0 top-0 z-50 border-b transition-all duration-300',
      dangCuon
        ? 'border-primary/20 bg-white/95 shadow-primary/10 shadow-[0_10px_30px_0_var(--tw-shadow-color)] backdrop-blur'
        : 'border-slate-200/80 bg-white/88 backdrop-blur',
    ]"
  >
    <div class="mx-auto flex max-w-7xl items-center gap-4 px-6 py-3 lg:px-10">
      <!-- Mobile Menu Button -->
      <button @click="toggleMenu" class="flex text-slate-900 transition md:hidden" aria-label="Menu">
        <svg v-if="!menuMo" class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
        <svg v-else class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <!-- Logo -->
      <router-link to="/" class="flex shrink-0 items-center gap-3">
        <img :src="logoChinh" :alt="thuongHieu" class="h-10 w-auto object-contain md:h-12" />
      </router-link>

      <!-- Mobile Cart -->
      <div class="flex items-center md:hidden">
        <button class="relative text-slate-900 transition hover:text-primary" aria-label="Giỏ hàng">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="20" r="1" />
            <circle cx="18" cy="20" r="1" />
            <path d="M3 4h2l2.4 10.2a1 1 0 0 0 1 .8h9.7a1 1 0 0 0 1-.8L21 7H7" />
          </svg>
          <span class="absolute -right-2 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-bold text-white shadow-sm shadow-primary/30">
            2
          </span>
        </button>
      </div>

      <!-- Desktop Nav -->
      <nav class="hidden min-w-0 flex-1 items-center justify-center gap-5 whitespace-nowrap text-[12px] font-semibold leading-[1.35] text-slate-800 lg:gap-7 md:flex">
        <router-link :to="{ path: '/' }" class="shrink-0 transition hover:text-primary">Trang chủ</router-link>
        <router-link :to="{ path: '/', hash: '#bo-suu-tap' }" class="shrink-0 transition hover:text-primary">Danh mục</router-link>
        <router-link :to="{ path: '/', hash: '#noi-bat' }" class="shrink-0 transition hover:text-primary">Nổi bật</router-link>
        <router-link :to="{ path: '/', hash: '#gia-tri' }" class="shrink-0 transition hover:text-primary">Giới thiệu</router-link>
      </nav>

      <!-- Desktop Actions -->
      <div class="ml-auto hidden shrink-0 items-center gap-4 md:flex">
        <button class="inline-flex shrink-0 items-center justify-center text-slate-900 transition hover:text-primary" aria-label="Tìm kiếm">
          <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" />
            <path d="m20 20-3.5-3.5" />
          </svg>
        </button>
        <button class="inline-flex shrink-0 items-center justify-center text-slate-900 transition hover:text-primary" aria-label="Tài khoản">
          <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21a8 8 0 1 0-16 0" />
            <circle cx="12" cy="7" r="4" />
          </svg>
        </button>
        <button class="relative inline-flex shrink-0 items-center justify-center text-slate-900 transition hover:text-primary" aria-label="Giỏ hàng">
          <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="20" r="1" />
            <circle cx="18" cy="20" r="1" />
            <path d="M3 4h2l2.4 10.2a1 1 0 0 0 1 .8h9.7a1 1 0 0 0 1-.8L21 7H7" />
          </svg>
          <span class="absolute -right-2 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-bold text-white shadow-sm shadow-primary/30">
            2
          </span>
        </button>
      </div>
    </div>

    <!-- Mobile Menu -->
    <div
      v-show="menuMo"
      class="absolute left-0 top-full z-50 flex h-[calc(100vh-60px)] w-full flex-col overflow-y-auto bg-white shadow-xl md:hidden"
    >
      <nav class="flex flex-col gap-6 p-6 text-base font-semibold text-slate-800">
        <router-link :to="{ path: '/' }" @click="toggleMenu" class="transition hover:text-primary">Trang chủ</router-link>
        <router-link :to="{ path: '/', hash: '#bo-suu-tap' }" @click="toggleMenu" class="transition hover:text-primary">Danh mục</router-link>
        <router-link :to="{ path: '/', hash: '#noi-bat' }" @click="toggleMenu" class="transition hover:text-primary">Nổi bật</router-link>
        <router-link :to="{ path: '/', hash: '#gia-tri' }" @click="toggleMenu" class="transition hover:text-primary">Giới thiệu</router-link>
      </nav>
      <div class="mb-8 flex items-center gap-6 border-t border-slate-100 px-6 pt-6">
        <button class="flex items-center gap-2 text-slate-900 transition hover:text-primary" aria-label="Tìm kiếm">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" />
            <path d="m20 20-3.5-3.5" />
          </svg>
          <span class="text-sm font-semibold">Tìm kiếm</span>
        </button>
        <button class="flex items-center gap-2 text-slate-900 transition hover:text-primary" aria-label="Tài khoản">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21a8 8 0 1 0-16 0" />
            <circle cx="12" cy="7" r="4" />
          </svg>
          <span class="text-sm font-semibold">Tài khoản</span>
        </button>
      </div>
    </div>
  </header>
</template>
