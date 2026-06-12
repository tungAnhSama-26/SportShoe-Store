<script setup>
import { computed, ref } from "vue";
import { ChevronDown, Home, LogOut, Menu, Moon, Sun, UserCog, UserRound } from "lucide-vue-next";
import { useRoute, useRouter } from "vue-router";
import { toggleSidebar } from "../../composable/useSidebar";
import { useDarkMode } from "../../composable/useDarkMode";
import { useAdminSession } from "../../composable/useAdminSession";
import { isAdminRole, logoutAdmin } from "../../services/auth";

const route = useRoute();
const router = useRouter();
const { isDark, toggleDark } = useDarkMode();
const { adminSession, avatarUrl } = useAdminSession();
const FALLBACK_ADMIN_NAME = "Trần Vũ Tùng Anh";
const hienMenuTaiKhoan = ref(false);

const pageTitle = computed(() => {
  const titles = {
    'admin-tra-hang': 'Quản lý trả hàng',
    'admin-tra-hang-chi-tiet': 'Chi tiết trả hàng',
    'admin-thong-ke': 'Thống kê',
    'admin-phieu-giam-gia': 'Quản lý phiếu giảm giá',
    'admin-phieu-giam-gia-khach-hang': 'Phiếu giảm giá khách hàng',
    'admin-phieu-giam-gia-them': 'Thêm phiếu giảm giá',
    'admin-phieu-giam-gia-chi-tiet': 'Cập nhật phiếu giảm giá',
    'admin-hoa-don': 'Quản lý hóa đơn',
    'admin-hoa-don-chi-tiet': 'Chi tiết hóa đơn',
    'admin-ban-hang': 'Bán hàng tại quầy',
    'admin-san-pham': 'Quản lý sản phẩm',
    'admin-san-pham-them': 'Thêm sản phẩm',
    'admin-chi-tiet-san-pham': 'Cập nhật sản phẩm',
    'admin-chi-tiet-san-pham-new': 'Chi tiết sản phẩm',
    'admin-bien-the-san-pham': 'Quản lý biến thể sản phẩm',
    'admin-bien-the-san-pham-them': 'Thêm biến thể sản phẩm',
    'admin-loai-giay': 'Quản lý loại giày',
    'admin-co-giay': 'Quản lý cổ giày',
    'admin-de-giay': 'Quản lý đế giày',
    'admin-chat-lieu-giay': 'Quản lý chất liệu giày',
    'admin-thuong-hieu': 'Quản lý thương hiệu',
    'admin-cong-nghe-dem': 'Quản lý công nghệ đệm',
    'admin-mau-sac': 'Quản lý màu sắc',
    'admin-kich-co': 'Quản lý kích cỡ',
    'admin-trong-luong': 'Quản lý trọng lượng',
    'admin-dot-giam-gia': 'Quản lý đợt giảm giá',
    'admin-dot-giam-gia-them': 'Thêm đợt giảm giá',
    'admin-dot-giam-gia-chi-tiet': 'Chi tiết đợt giảm giá',
    'admin-nhan-vien': 'Quản lý nhân viên',
    'admin-nhan-vien-them': 'Thêm nhân viên',
    'admin-nhan-vien-lich-lam': 'Lịch làm việc',
    'admin-nhan-vien-chi-tiet': 'Cập nhật nhân viên',
    'admin-profile': 'Hồ sơ cá nhân',
    'nhanvien-profile': 'Hồ sơ cá nhân',
    'admin-khach-hang': 'Quản lý khách hàng',
    'admin-khach-hang-them': 'Thêm khách hàng',
    'admin-khach-hang-chi-tiet': 'Cập nhật khách hàng'
  };
  return titles[route.name] || 'Hệ thống Quản trị';
});

const profileName = computed(() => {
  const username = adminSession.value.tenTaiKhoan?.trim();
  const fullName = adminSession.value.hoTen?.trim();

  if (username && username !== "admin") {
    return fullName || username;
  }

  return FALLBACK_ADMIN_NAME;
});

const hasAvatar = computed(() => Boolean(adminSession.value.hinhAnh?.trim()));

function chuyenDenCapNhatThongTin() {
  hienMenuTaiKhoan.value = false;
  router.push(isAdminRole() ? "/admin/profile" : "/nhanvien/profile");
}

function chuyenDenTrangChu() {
  hienMenuTaiKhoan.value = false;
  router.push("/");
}

function dangXuat() {
  logoutAdmin();
  hienMenuTaiKhoan.value = false;
  router.push("/admin/login");
}
</script>

<template>
  <header class="sticky top-0 z-30 border-b border-slate-200/80 bg-white/95 backdrop-blur dark:border-slate-700 dark:bg-slate-800/95">
    <div class="flex h-[74px] items-center justify-between gap-3 px-4 lg:px-6">
      <div class="flex items-center gap-4">
        <button
          type="button"
          @click="toggleSidebar"
          class="inline-flex h-11 w-11 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 transition hover:border-slate-300 hover:text-slate-700 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:text-white"
        >
          <Menu class="h-5 w-5" />
        </button>
        <h1 class="hidden md:block text-[28px] whitespace-nowrap font-bold text-slate-800 dark:text-slate-100">
          {{ pageTitle }}
        </h1>
      </div>

      <div class="flex items-center gap-3">
        <button
          type="button"
          @click="toggleDark"
          class="inline-flex h-11 w-11 items-center justify-center rounded-2xl border border-slate-200 bg-slate-50 text-slate-500 transition hover:border-slate-300 hover:bg-white hover:text-slate-700 dark:border-slate-700 dark:bg-slate-700 dark:text-slate-200 dark:hover:bg-slate-600"
        >
          <Sun v-if="!isDark" class="h-5 w-5" />
          <Moon v-else class="h-5 w-5" />
        </button>

        <div class="hidden h-8 w-px bg-slate-200 dark:bg-slate-700 sm:block"></div>

        <div class="relative">
          <button
            type="button"
            class="flex items-center gap-2 rounded-full border border-slate-200 bg-white px-2.5 py-1.5 text-left shadow-sm transition hover:border-slate-300 hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-800 dark:hover:bg-slate-700"
            @click="hienMenuTaiKhoan = !hienMenuTaiKhoan"
          >
            <div class="flex h-9 w-9 items-center justify-center overflow-hidden rounded-full bg-slate-100 text-slate-500 dark:bg-slate-700 dark:text-slate-200">
              <img
                v-if="hasAvatar"
                :src="avatarUrl"
                :alt="profileName"
                class="h-full w-full object-cover"
              />
              <UserRound v-else class="h-[18px] w-[18px]" />
            </div>
            <span class="hidden max-w-[180px] truncate text-sm font-semibold text-slate-700 sm:block dark:text-slate-100">
              {{ profileName }}
            </span>
            <ChevronDown
              class="hidden h-4 w-4 text-slate-400 transition sm:block"
              :class="hienMenuTaiKhoan ? 'rotate-180' : ''"
            />
          </button>

          <div
            v-if="hienMenuTaiKhoan"
            class="absolute right-0 top-[calc(100%+10px)] z-50 w-56 overflow-hidden rounded-2xl border border-slate-200 bg-white p-2 shadow-[0_18px_45px_rgba(15,23,42,0.16)] dark:border-slate-700 dark:bg-slate-800"
          >
            <button
              type="button"
              class="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-slate-700 transition hover:bg-slate-50 dark:text-slate-100 dark:hover:bg-slate-700"
              @click="chuyenDenCapNhatThongTin"
            >
              <UserCog class="h-4 w-4 text-slate-500 dark:text-slate-300" />
              Cập nhật thông tin
            </button>
            <button
              type="button"
              class="mt-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-slate-700 transition hover:bg-slate-50 dark:text-slate-100 dark:hover:bg-slate-700"
              @click="chuyenDenTrangChu"
            >
              <Home class="h-4 w-4 text-slate-500 dark:text-slate-300" />
              Trang chủ
            </button>
            <button
              type="button"
              class="mt-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-primary transition hover:bg-primary/10 dark:text-primary dark:hover:bg-primary/20"
              @click="dangXuat"
            >
              <LogOut class="h-4 w-4" />
              Đăng xuất
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
