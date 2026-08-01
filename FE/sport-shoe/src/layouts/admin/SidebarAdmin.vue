<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { demDanhGiaChuaXem } from "../../services/admin-danh-gia";
import logoChinh from "../../assets/logo/delete-background-logo.png";
import {
  isDesktopSidebar,
  isSidebarCollapsed,
  isSidebarOpen,
  toggleSidebar
} from "../../composable/useSidebar";
import { useAdminSession } from "../../composable/useAdminSession";
import {
  ArrowRightLeft,
  Award,
  BadgePercent,
  Box,
  CalendarDays,
  ChevronDown,
  ChevronsLeft,
  ClipboardList,
  Clock,
  Feather,
  History,
  Footprints,
  CircleDollarSign,
  Home,
  LayoutDashboard,
  Layers,
  MoveVertical,
  Package,
  PackageCheck,
  Palette,
  ReceiptText,
  Ruler,
  Star,
  Store,
  Tag,
  Ticket,
  UserRoundCog,
  Users,
  Weight,
  MessageSquare
} from "lucide-vue-next";
const route = useRoute();
const { adminSession } = useAdminSession();
const laAdmin = computed(() => adminSession.value.vaiTro === "Quản trị viên");
const isActive = (path) => route.path.startsWith(path);

// Chuông thông báo: số đánh giá chưa xem.
const soDanhGiaChuaXem = ref(0);
let dongHoChuaXem = null;
async function taiSoDanhGiaChuaXem() {
  try {
    soDanhGiaChuaXem.value = (await demDanhGiaChuaXem()) || 0;
  } catch {
    // bỏ qua lỗi nền
  }
}
onMounted(() => {
  taiSoDanhGiaChuaXem();
  // Tự cập nhật mỗi 30s để chuông sáng khi có người đánh giá (không cần F5).
  dongHoChuaXem = setInterval(taiSoDanhGiaChuaXem, 30000);
  // Cập nhật tức thì khi admin vừa xem đánh giá của 1 sản phẩm.
  window.addEventListener("danh-gia-da-xem", taiSoDanhGiaChuaXem);
});
onUnmounted(() => {
  if (dongHoChuaXem) clearInterval(dongHoChuaXem);
  window.removeEventListener("danh-gia-da-xem", taiSoDanhGiaChuaXem);
});
const checkThuocTinhActive = (newPath) => {
  const routes = [
    "/admin/loai-giay",
    "/admin/co-giay",
    "/admin/de-giay",
    "/admin/chat-lieu-giay",
    "/admin/thuong-hieu",
    "/admin/cong-nghe-dem",
    "/admin/mau-sac",
    "/admin/kich-co",
    "/admin/trong-luong"
  ];
  return routes.some((currentRoute) => newPath.startsWith(currentRoute));
};
const checkSanPhamActive = (newPath) =>
  newPath.startsWith("/admin/san-pham")
  || newPath.startsWith("/admin/bien-the-san-pham")
  || newPath.startsWith("/admin/chi-tiet-san-pham");
const checkKhuyenMaiActive = (newPath) => {
  const routes = ["/admin/phieu-giam-gia", "/admin/dot-giam-gia"];
  return routes.some((currentRoute) => newPath.startsWith(currentRoute));
};
const checkLichLamActive = (newPath) => {
  const routes = ["/admin/lich-lam-viec", "/admin/lich-ca-lam", "/admin/ban-giao-ca", "/admin/mo-ca"];
  return routes.some((currentRoute) => newPath.startsWith(currentRoute));
};
const compactMode = computed(() => isDesktopSidebar.value && isSidebarCollapsed.value);
const isSanPhamActive = computed(() => checkSanPhamActive(route.path));
const isThuocTinhActive = computed(() => checkThuocTinhActive(route.path));
const isKhuyenMaiActive = computed(() => checkKhuyenMaiActive(route.path));
const isLichLamActive = computed(() => checkLichLamActive(route.path));
const openSanPham = ref(checkSanPhamActive(route.path));
const openThuocTinh = ref(checkThuocTinhActive(route.path));
const openKhuyenMai = ref(checkKhuyenMaiActive(route.path));
const openLichLam = ref(checkLichLamActive(route.path));
watch(
  () => route.path,
  (newPath) => {
    taiSoDanhGiaChuaXem(); // cập nhật badge chuông khi điều hướng
    if (checkSanPhamActive(newPath)) {
      openSanPham.value = true;
    }
    if (checkThuocTinhActive(newPath)) {
      openThuocTinh.value = true;
    }
    if (newPath.startsWith("/admin/phieu-giam-gia") || newPath.startsWith("/admin/dot-giam-gia")) {
      openKhuyenMai.value = true;
    }
    if (checkLichLamActive(newPath)) {
      openLichLam.value = true;
    }
  }
);
function toggleSanPham() {
  openSanPham.value = !openSanPham.value;
}
function toggleThuocTinh() {
  openThuocTinh.value = !openThuocTinh.value;
}
function toggleKhuyenMai() {
  openKhuyenMai.value = !openKhuyenMai.value;
}
function toggleLichLam() {
  openLichLam.value = !openLichLam.value;
}
function navItemClass(active) {
  return [
    "group flex min-w-0 items-center rounded-xl px-4 py-3 transition-colors",
    compactMode.value ? "justify-center px-3" : "",
    active ? "bg-primary/10 dark:bg-primary/20 text-primary dark:text-primary font-medium" : "text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50"
  ];
}
function navIconClass(active) {
  return [
    "h-5 w-5 shrink-0",
    compactMode.value ? "" : "mr-3",
    active ? "text-primary" : "text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300"
  ];
}
function subItemClass(active) {
  return [
    "flex min-w-0 items-center rounded-xl border border-transparent transition-colors",
    compactMode.value ? "justify-center px-3 py-2.5" : "px-4 py-2.5 text-[13px] font-normal",
    active
      ? "bg-primary/10 text-primary border-primary/20 shadow-sm"
      : "text-gray-500 hover:text-gray-800 hover:bg-gray-50 dark:text-gray-400 dark:hover:text-white dark:hover:bg-gray-700/50"
  ];
}
</script>

<template>
  <aside
    class="fixed top-0 z-50 flex h-screen shrink-0 flex-col border-r border-gray-100 bg-white transition-[width,transform] duration-200 ease-out dark:border-slate-600/50 dark:bg-slate-700 lg:sticky"
    :class="
      isDesktopSidebar
        ? [compactMode ? 'w-[92px] translate-x-0' : 'w-[260px] translate-x-0']
        : [isSidebarOpen ? 'w-[260px] translate-x-0' : 'w-[260px] -translate-x-full']
    "
  >
    <div
      class="relative mb-2 flex flex-col items-center pt-5"
      :class="compactMode ? 'px-3' : 'px-6'"
    >
      <img
        :src="logoChinh"
        alt="Logo"
        class="w-auto object-contain transition-[height,max-width] duration-200 ease-out"
        :class="compactMode ? 'h-8 max-w-[48px]' : 'h-16 max-w-[180px]'"
      />
      <button
        v-if="!compactMode"
        type="button"
        :title="compactMode ? '\u004d\u1edf r\u1ed9ng sidebar' : 'Thu g\u1ecdn sidebar'"
        class="absolute top-2 text-gray-400 transition-colors hover:text-gray-600 focus:outline-none"
        :class="compactMode ? 'right-1/2 translate-x-1/2' : 'right-4'"
        @click="toggleSidebar"
      >
        <ChevronsLeft class="h-5 w-5" :class="compactMode ? 'rotate-180' : ''" />
      </button>
    </div>

    <nav class="mt-6 flex-1 space-y-1 overflow-x-hidden overflow-y-auto" :class="compactMode ? 'px-2' : 'px-4'">


      <router-link v-if="laAdmin" to="/admin/thong-ke" :title="compactMode ? 'Tổng quan' : undefined" :class="navItemClass(isActive('/admin/thong-ke'))">
        <LayoutDashboard :class="navIconClass(isActive('/admin/thong-ke'))" />
        <span v-if="!compactMode" class="min-w-0 truncate text-sm leading-tight">Tổng quan</span>
      </router-link>

      <router-link to="/admin/ban-hang" :title="compactMode ? 'B\u00e1n h\u00e0ng t\u1ea1i qu\u1ea7y' : undefined" :class="navItemClass(isActive('/admin/ban-hang'))">
        <Store :class="navIconClass(isActive('/admin/ban-hang'))" />
        <span v-if="!compactMode" class="min-w-0 truncate text-sm leading-tight">B&#225;n h&#224;ng t&#7841;i qu&#7847;y</span>
      </router-link>

      <router-link to="/admin/hoa-don" :title="compactMode ? 'Qu\u1ea3n l\u00fd h\u00f3a \u0111\u01a1n' : undefined" :class="navItemClass(isActive('/admin/hoa-don'))">
        <ReceiptText :class="navIconClass(isActive('/admin/hoa-don'))" />
        <span v-if="!compactMode" class="min-w-0 truncate text-sm leading-tight">Qu&#7843;n l&#253; h&#243;a &#273;&#417;n</span>
      </router-link>

      <div v-if="laAdmin" class="space-y-1">
        <button
          type="button"
          :title="compactMode ? 'Qu\u1ea3n l\u00fd s\u1ea3n ph\u1ea9m' : undefined"
          class="group flex w-full min-w-0 items-center rounded-xl px-4 py-3 transition-colors focus:outline-none"
          :class="[
            compactMode ? 'justify-center px-3' : 'justify-between',
            isSanPhamActive
              ? 'bg-primary/10 dark:bg-primary/20 text-primary dark:text-primary font-medium'
              : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50',
          ]"
          @click="toggleSanPham"
        >
          <div class="flex items-center" :class="compactMode ? 'justify-center' : 'min-w-0 flex-1'">
            <Package :class="navIconClass(isSanPhamActive)" />
            <span v-if="!compactMode" class="truncate text-sm leading-tight">Qu&#7843;n l&#253; s&#7843;n ph&#7849;m</span>
          </div>
          <ChevronDown
            v-if="!compactMode"
            class="h-4 w-4 transition-transform duration-200"
            :class="[openSanPham ? 'rotate-180 text-primary' : 'text-gray-400']"
          />
        </button>
        <div v-show="openSanPham" class="space-y-1 overflow-hidden transition-all duration-300" :class="compactMode ? 'px-0 py-1' : 'pr-4 pl-[36px]'">
          <router-link to="/admin/san-pham" :title="compactMode ? 'S\u1ea3n ph\u1ea9m' : undefined" :class="subItemClass(isActive('/admin/san-pham'))">
            <Package class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/san-pham') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 truncate leading-tight">S&#7843;n ph&#7849;m</span>
          </router-link>
          <router-link to="/admin/bien-the-san-pham" :title="compactMode ? 'Bi\u1ebfn th\u1ec3 s\u1ea3n ph\u1ea9m' : undefined" :class="subItemClass(isActive('/admin/bien-the-san-pham'))">
            <Layers class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/bien-the-san-pham') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 whitespace-nowrap leading-tight">Bi&#7871;n th&#7875; s&#7843;n ph&#7849;m</span>
          </router-link>
        </div>
      </div>

      <div v-if="laAdmin" class="space-y-1">
        <button
          type="button"
          :title="compactMode ? 'Danh s\u00e1ch thu\u1ed9c t\u00ednh' : undefined"
          class="group flex w-full min-w-0 items-center rounded-xl px-4 py-3 transition-colors focus:outline-none"
          :class="[
            compactMode ? 'justify-center px-3' : 'justify-between',
            isThuocTinhActive
              ? 'bg-primary/10 dark:bg-primary/20 text-primary dark:text-primary font-medium'
              : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50',
          ]"
          @click="toggleThuocTinh"
        >
          <div class="flex items-center" :class="compactMode ? 'justify-center' : 'min-w-0 flex-1'">
            <Box :class="navIconClass(isThuocTinhActive)" />
            <span v-if="!compactMode" class="truncate text-sm leading-tight">Danh s&#225;ch thu&#7897;c t&#237;nh</span>
          </div>
          <ChevronDown
            v-if="!compactMode"
            class="h-4 w-4 transition-transform duration-200"
            :class="[openThuocTinh ? 'rotate-180 text-primary' : 'text-gray-400']"
          />
        </button>
        <div v-show="openThuocTinh" class="space-y-1 overflow-hidden transition-all duration-300" :class="compactMode ? 'px-0 py-1' : 'pr-4 pl-[36px]'">
          <router-link to="/admin/loai-giay" :title="compactMode ? 'Lo\u1ea1i gi\u00e0y' : undefined" :class="subItemClass(isActive('/admin/loai-giay'))">
            <Box class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/loai-giay') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 truncate leading-tight">Lo&#7841;i gi&#224;y</span>
          </router-link>
          <router-link to="/admin/thuong-hieu" :title="compactMode ? 'Th\u01b0\u01a1ng hi\u1ec7u' : undefined" :class="subItemClass(isActive('/admin/thuong-hieu'))">
            <Award class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/thuong-hieu') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 truncate leading-tight">Th&#432;&#417;ng hi&#7879;u</span>
          </router-link>
          <router-link to="/admin/chat-lieu-giay" :title="compactMode ? 'Ch\u1ea5t li\u1ec7u gi\u00e0y' : undefined" :class="subItemClass(isActive('/admin/chat-lieu-giay'))">
            <Layers class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/chat-lieu-giay') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 truncate leading-tight">Ch&#7845;t li&#7879;u gi&#224;y</span>
          </router-link>
          <router-link to="/admin/de-giay" :title="compactMode ? '\u0110\u1ebf gi\u00e0y' : undefined" :class="subItemClass(isActive('/admin/de-giay'))">
            <Footprints class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/de-giay') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 truncate leading-tight">&#272;&#7871; gi&#224;y</span>
          </router-link>
          <router-link to="/admin/co-giay" :title="compactMode ? 'C\u1ed5 gi\u00e0y' : undefined" :class="subItemClass(isActive('/admin/co-giay'))">
            <MoveVertical class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/co-giay') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 truncate leading-tight">C&#7893; gi&#224;y</span>
          </router-link>
          <router-link to="/admin/cong-nghe-dem" :title="compactMode ? 'C\u00f4ng ngh\u1ec7 \u0111\u1ec7m' : undefined" :class="subItemClass(isActive('/admin/cong-nghe-dem'))">
            <Feather class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/cong-nghe-dem') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 truncate leading-tight">C&#244;ng ngh&#7879; &#273;&#7879;m</span>
          </router-link>
          <router-link to="/admin/mau-sac" :title="compactMode ? 'M\u00e0u s\u1eafc' : undefined" :class="subItemClass(isActive('/admin/mau-sac'))">
            <Palette class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/mau-sac') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 truncate leading-tight">M&#224;u s&#7855;c</span>
          </router-link>
          <router-link to="/admin/kich-co" :title="compactMode ? 'K\u00edch c\u1ee1' : undefined" :class="subItemClass(isActive('/admin/kich-co'))">
            <Ruler class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/kich-co') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 truncate leading-tight">K&#237;ch c&#7905;</span>
          </router-link>
          <router-link to="/admin/trong-luong" :title="compactMode ? 'Tr\u1ecdng l\u01b0\u1ee3ng' : undefined" :class="subItemClass(isActive('/admin/trong-luong'))">
            <Weight class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/trong-luong') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="min-w-0 truncate leading-tight">Tr&#7885;ng l&#432;&#7907;ng</span>
          </router-link>
        </div>
      </div>

      <div v-if="laAdmin" class="space-y-1">
        <button
          type="button"
          :title="compactMode ? 'Qu\u1ea3n l\u00fd gi\u1ea3m gi\u00e1' : undefined"
          class="group flex w-full min-w-0 items-center rounded-xl px-4 py-3 transition-colors focus:outline-none"
          :class="[
            compactMode ? 'justify-center px-3' : 'justify-between',
            isKhuyenMaiActive
              ? 'bg-primary/10 dark:bg-primary/20 text-primary dark:text-primary font-medium'
              : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50',
          ]"
          @click="toggleKhuyenMai"
        >
          <div class="flex items-center" :class="compactMode ? 'justify-center' : 'min-w-0 flex-1'">
            <BadgePercent :class="navIconClass(isKhuyenMaiActive)" />
            <span v-if="!compactMode" class="truncate text-sm leading-tight">Qu&#7843;n l&#253; gi&#7843;m gi&#225;</span>
          </div>
          <ChevronDown
            v-if="!compactMode"
            class="h-4 w-4 transition-transform duration-200"
            :class="[openKhuyenMai ? 'rotate-180 text-primary' : 'text-gray-400']"
          />
        </button>
        <div v-show="openKhuyenMai" class="space-y-1 overflow-hidden transition-all duration-300" :class="compactMode ? 'px-0 py-1' : 'pr-4 pl-[36px]'">
          <router-link to="/admin/phieu-giam-gia" :title="compactMode ? 'Phi\u1ebfu gi\u1ea3m gi\u00e1' : undefined" :class="subItemClass(isActive('/admin/phieu-giam-gia'))">
            <Ticket class="h-[18px] w-[18px] shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/phieu-giam-gia') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="leading-tight">Phiếu giảm giá</span>
          </router-link>
          <router-link to="/admin/dot-giam-gia" :title="compactMode ? '\u0110\u1ee3t gi\u1ea3m gi\u00e1' : undefined" :class="subItemClass(isActive('/admin/dot-giam-gia'))">
            <Tag class="h-[18px] w-[18px] shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/dot-giam-gia') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="leading-tight">Đợt giảm giá</span>
          </router-link>
        </div>
      </div>

      <router-link to="/admin/khach-hang" :title="compactMode ? 'Qu\u1ea3n l\u00fd kh\u00e1ch h\u00e0ng' : undefined" :class="navItemClass(isActive('/admin/khach-hang'))">
        <Users :class="navIconClass(isActive('/admin/khach-hang'))" />
        <span v-if="!compactMode" class="min-w-0 truncate text-sm leading-tight">Qu&#7843;n l&#253; kh&#225;ch h&#224;ng</span>
      </router-link>

      <router-link to="/admin/chat" :title="compactMode ? 'Hỗ trợ trực tuyến' : undefined" :class="navItemClass(isActive('/admin/chat'))">
        <MessageSquare :class="navIconClass(isActive('/admin/chat'))" />
        <span v-if="!compactMode" class="min-w-0 truncate text-sm leading-tight">Hỗ trợ trực tuyến</span>
      </router-link>

      <router-link v-if="laAdmin" to="/admin/danh-gia" :title="compactMode ? 'Quản lý đánh giá' : undefined" :class="navItemClass(isActive('/admin/danh-gia'))">
        <div class="relative shrink-0" :class="compactMode ? '' : 'mr-3'">
          <Star
            class="h-5 w-5"
            :class="isActive('/admin/danh-gia') ? 'text-primary' : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'"
          />
          <span
            v-if="soDanhGiaChuaXem > 0"
            class="absolute -right-2 -top-2 flex h-4 min-w-[16px] items-center justify-center rounded-full bg-rose-500 px-1 text-[10px] font-bold leading-none text-white"
          >{{ soDanhGiaChuaXem > 99 ? '99+' : soDanhGiaChuaXem }}</span>
        </div>
        <span v-if="!compactMode" class="min-w-0 truncate text-sm leading-tight">Quản lý đánh giá</span>
      </router-link>


      <router-link v-if="laAdmin" to="/admin/nhan-vien" :title="compactMode ? 'Qu\u1ea3n l\u00fd nh\u00e2n vi\u00ean' : undefined" :class="navItemClass(isActive('/admin/nhan-vien') && !isLichLamActive)"
        >
        <UserRoundCog :class="navIconClass(isActive('/admin/nhan-vien') && !isLichLamActive)" />
        <span v-if="!compactMode" class="min-w-0 truncate text-sm leading-tight">Qu&#7843;n l&#253; nh&#226;n vi&#234;n</span>
      </router-link>

      <!-- Quản lý lịch làm (ADMIN) -->
      <div v-if="laAdmin" class="space-y-1">
        <button
          type="button"
          :title="compactMode ? 'Qu\u1ea3n l\u00fd l\u1ecbch l\u00e0m' : undefined"
          class="group flex w-full min-w-0 items-center rounded-xl px-4 py-3 transition-colors focus:outline-none"
          :class="[
            compactMode ? 'justify-center px-3' : 'justify-between',
            isLichLamActive
              ? 'bg-primary/10 dark:bg-primary/20 text-primary dark:text-primary font-medium'
              : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50',
          ]"
          @click="toggleLichLam"
        >
          <div class="flex items-center" :class="compactMode ? 'justify-center' : 'min-w-0 flex-1'">
            <CalendarDays :class="navIconClass(isLichLamActive)" />
            <span v-if="!compactMode" class="truncate text-sm leading-tight">Qu&#7843;n l&#253; l&#7883;ch l&#224;m</span>
          </div>
          <ChevronDown
            v-if="!compactMode"
            class="h-4 w-4 transition-transform duration-200"
            :class="[openLichLam ? 'rotate-180 text-primary' : 'text-gray-400']"
          />
        </button>
        <div v-show="openLichLam" class="space-y-1 overflow-hidden transition-all duration-300" :class="compactMode ? 'px-0 py-1' : 'pr-4 pl-[36px]'">
          <router-link to="/admin/lich-lam-viec" :title="compactMode ? 'L\u1ecbch l\u00e0m vi\u1ec7c' : undefined" :class="subItemClass(isActive('/admin/lich-lam-viec'))">
            <CalendarDays class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/lich-lam-viec') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="leading-tight">L&#7883;ch l&#224;m vi&#7879;c</span>
          </router-link>
          <router-link to="/admin/lich-ca-lam" :title="compactMode ? 'L\u1ecbch ca l\u00e0m' : undefined" :class="subItemClass(isActive('/admin/lich-ca-lam'))">
            <CalendarDays class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/lich-ca-lam') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="leading-tight">L&#7883;ch ca l&#224;m</span>
          </router-link>
          <router-link to="/admin/lich-su-hoat-dong" :title="compactMode ? 'L\u1ecbch s\u1eed ho\u1ea1t \u0111\u1ed9ng' : undefined" :class="subItemClass(isActive('/admin/lich-su-hoat-dong'))">
            <History class="h-4 w-4 shrink-0" :class="[compactMode ? '' : 'mr-3', isActive('/admin/lich-su-hoat-dong') ? 'text-primary' : 'text-gray-400 dark:text-gray-500']" />
            <span v-if="!compactMode" class="leading-tight">Lịch sử hoạt động</span>
          </router-link>
        </div>
      </div>

      <!-- Lịch làm và giao ca (NHÂN VIÊN) -->
      <template v-else>
        <router-link to="/admin/lich-lam-viec" :title="compactMode ? 'L\u1ecbch l\u00e0m vi\u1ec7c' : undefined" :class="navItemClass(isActive('/admin/lich-lam-viec'))">
          <CalendarDays :class="navIconClass(isActive('/admin/lich-lam-viec'))" />
          <span v-if="!compactMode" class="min-w-0 truncate text-sm leading-tight">L&#7883;ch l&#224;m vi&#7879;c</span>
        </router-link>
        <router-link to="/admin/ban-giao-ca" :title="compactMode ? 'Bàn giao ca' : undefined" :class="navItemClass(isActive('/admin/ban-giao-ca'))">
          <ArrowRightLeft :class="navIconClass(isActive('/admin/ban-giao-ca'))" />
          <span v-if="!compactMode" class="min-w-0 truncate text-sm leading-tight">Bàn giao ca</span>
        </router-link>
        <router-link to="/admin/mo-ca" :title="compactMode ? 'Mở ca' : undefined" :class="navItemClass(isActive('/admin/mo-ca'))">
          <Clock :class="navIconClass(isActive('/admin/mo-ca'))" />
          <span v-if="!compactMode" class="min-w-0 truncate text-sm leading-tight">Mở ca</span>
        </router-link>
      </template>
    </nav>
  </aside>
</template>
