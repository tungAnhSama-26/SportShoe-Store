<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import logoChinh from "../../assets/logo/delete-background-logo.png";
import { gioHangStore } from "../../stores/gio-hang";
import { layKhachId } from "../../services/gio-hang";
import { logoutCustomer } from "../../services/auth";

defineProps({
  thuongHieu: {
    type: String,
    required: true,
  },
});

const router = useRouter();
const dangCuon = ref(false);
const menuMo = ref(false);
const menuTaiKhoanMo = ref(false);
const daDangNhap = ref(Boolean(layKhachId()));

function toggleTaiKhoan() {
  menuTaiKhoanMo.value = !menuTaiKhoanMo.value;
}

function dangXuat() {
  logoutCustomer();
  daDangNhap.value = false;
  gioHangStore.datSoLuong(0);
  menuTaiKhoanMo.value = false;
  menuMo.value = false;
  document.body.style.overflow = "";
  router.replace("/login");
}

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
  daDangNhap.value = Boolean(layKhachId());
  capNhatTrangThaiCuon();
  window.addEventListener("scroll", capNhatTrangThaiCuon, { passive: true });
  gioHangStore.lamMoi();
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
        <router-link to="/gio-hang" class="relative text-slate-900 transition hover:text-primary" aria-label="Giỏ hàng">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="20" r="1" />
            <circle cx="18" cy="20" r="1" />
            <path d="M3 4h2l2.4 10.2a1 1 0 0 0 1 .8h9.7a1 1 0 0 0 1-.8L21 7H7" />
          </svg>
          <span v-if="gioHangStore.soLuong" class="absolute -right-2 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-bold text-white shadow-sm shadow-primary/30">
            {{ gioHangStore.soLuong }}
          </span>
        </router-link>
      </div>

      <!-- Desktop Nav -->
      <nav class="hidden min-w-0 flex-1 items-center justify-center gap-5 whitespace-nowrap text-[12px] font-semibold leading-[1.35] text-slate-800 lg:gap-7 md:flex">
        <router-link :to="{ path: '/' }" class="shrink-0 transition hover:text-primary">Trang chủ</router-link>
        <router-link :to="{ path: '/', hash: '#bo-suu-tap' }" class="shrink-0 transition hover:text-primary">Danh mục</router-link>
        <router-link :to="{ path: '/san-pham' }" class="shrink-0 transition hover:text-primary">Sản phẩm</router-link>
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
        <div class="relative">
          <button @click="toggleTaiKhoan" class="inline-flex shrink-0 items-center justify-center text-slate-900 transition hover:text-primary" aria-label="Tài khoản">
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21a8 8 0 1 0-16 0" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </button>
          <div v-if="menuTaiKhoanMo" @click="menuTaiKhoanMo = false" class="fixed inset-0 z-40"></div>
          <div v-if="menuTaiKhoanMo" class="absolute right-0 z-50 mt-3 w-52 overflow-hidden rounded-2xl border border-slate-100 bg-white py-2 shadow-xl">
            <template v-if="daDangNhap">
              <router-link to="/profile" @click="menuTaiKhoanMo = false" class="flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50">
                <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21a8 8 0 1 0-16 0" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
                Hồ sơ của bạn
              </router-link>
              <router-link to="/don-hang" @click="menuTaiKhoanMo = false" class="flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50">
                <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><path d="M3 6h18M16 10a4 4 0 0 1-8 0"/></svg>
                Đơn hàng của bạn
              </router-link>
              <button @click="dangXuat" class="flex w-full items-center gap-2 border-t border-slate-50 px-4 py-2.5 text-left text-sm font-medium text-rose-500 transition hover:bg-rose-50">
                <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/></svg>
                Đăng xuất
              </button>
            </template>
            <template v-else>
              <router-link to="/login" @click="menuTaiKhoanMo = false" class="block px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50">Đăng nhập</router-link>
              <router-link to="/register" @click="menuTaiKhoanMo = false" class="block border-t border-slate-50 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50">Đăng ký</router-link>
            </template>
          </div>
        </div>
        <router-link to="/gio-hang" class="relative inline-flex shrink-0 items-center justify-center text-slate-900 transition hover:text-primary" aria-label="Giỏ hàng">
          <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="20" r="1" />
            <circle cx="18" cy="20" r="1" />
            <path d="M3 4h2l2.4 10.2a1 1 0 0 0 1 .8h9.7a1 1 0 0 0 1-.8L21 7H7" />
          </svg>
          <span v-if="gioHangStore.soLuong" class="absolute -right-2 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-bold text-white shadow-sm shadow-primary/30">
            {{ gioHangStore.soLuong }}
          </span>
        </router-link>
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
        <router-link :to="{ path: '/san-pham' }" @click="toggleMenu" class="transition hover:text-primary">Sản phẩm</router-link>
        <router-link :to="{ path: '/', hash: '#noi-bat' }" @click="toggleMenu" class="transition hover:text-primary">Nổi bật</router-link>
        <router-link :to="{ path: '/', hash: '#gia-tri' }" @click="toggleMenu" class="transition hover:text-primary">Giới thiệu</router-link>
        <router-link to="/gio-hang" @click="toggleMenu" class="transition hover:text-primary">Giỏ hàng</router-link>
        <router-link v-if="daDangNhap" to="/profile" @click="toggleMenu" class="transition hover:text-primary">Hồ sơ của bạn</router-link>
        <router-link v-if="daDangNhap" to="/don-hang" @click="toggleMenu" class="transition hover:text-primary">Đơn hàng của bạn</router-link>
        <button v-if="daDangNhap" @click="dangXuat" class="text-left text-rose-500 transition hover:text-rose-600">Đăng xuất</button>
        <router-link v-else to="/login" @click="toggleMenu" class="transition hover:text-primary">Đăng nhập</router-link>
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
