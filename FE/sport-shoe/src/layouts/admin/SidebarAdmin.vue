<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import logoChinh from '../../assets/logo/delete-background-logo.png';
import {
  isDesktopSidebar,
  isSidebarCollapsed,
  isSidebarOpen,
  toggleSidebar,
} from '../../composable/useSidebar';
import {
  Award,
  BadgePercent,
  Box,
  ChevronDown,
  ChevronsLeft,
  ClipboardList,
  Feather,
  Footprints,
  Layers,
  MoveVertical,
  Package,
  Palette,
  ReceiptText,
  Ruler,
  Store,
  Tag,
  Ticket,
  TrendingUp,
  UserRoundCog,
  Users,
  Weight,
} from 'lucide-vue-next';

const route = useRoute();

const isActive = (path: string) => route.path.startsWith(path);

const checkDanhMucActive = (newPath: string) => {
  const routes = [
    '/admin/loai-giay',
    '/admin/co-giay',
    '/admin/de-giay',
    '/admin/thuong-hieu',
    '/admin/cong-nghe-dem',
    '/admin/mau-sac',
    '/admin/kich-co',
    '/admin/trong-luong',
  ];
  return routes.some((currentRoute) => newPath.startsWith(currentRoute));
};

const compactMode = computed(() => isDesktopSidebar.value && isSidebarCollapsed.value);
const openDanhMuc = ref(checkDanhMucActive(route.path));
const openKhuyenMai = ref(isActive('/admin/phieu-giam-gia') || isActive('/admin/dot-giam-gia'));

watch(
  () => route.path,
  (newPath) => {
    if (checkDanhMucActive(newPath)) {
      openDanhMuc.value = true;
    }
    if (newPath.startsWith('/admin/phieu-giam-gia') || newPath.startsWith('/admin/dot-giam-gia')) {
      openKhuyenMai.value = true;
    }
  },
);

function toggleDanhMuc() {
  if (compactMode.value) {
    isSidebarCollapsed.value = false;
    openDanhMuc.value = true;
    return;
  }

  openDanhMuc.value = !openDanhMuc.value;
}

function toggleKhuyenMai() {
  if (compactMode.value) {
    isSidebarCollapsed.value = false;
    openKhuyenMai.value = true;
    return;
  }

  openKhuyenMai.value = !openKhuyenMai.value;
}

function navItemClass(active: boolean) {
  return [
    'flex items-center rounded-xl px-4 py-3 transition-colors group',
    compactMode.value ? 'justify-center px-3' : '',
    active
      ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold'
      : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50',
  ];
}

function navIconClass(active: boolean) {
  return [
    'h-5 w-5 shrink-0',
    compactMode.value ? '' : 'mr-3',
    active
      ? 'text-red-500'
      : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300',
  ];
}

function subItemClass(active: boolean) {
  return [
    'flex items-center rounded-xl border border-transparent px-4 py-2.5 text-[13px] font-medium transition-colors',
    active
      ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm'
      : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50',
  ];
}
</script>

<template>
  <aside
    class="fixed top-0 z-50 flex h-screen shrink-0 flex-col border-r border-gray-100 bg-white transition-all duration-300 ease-in-out dark:border-slate-600/50 dark:bg-slate-700 lg:sticky"
    :class="
      isDesktopSidebar
        ? [compactMode ? 'w-[92px] translate-x-0' : 'w-[260px] translate-x-0']
        : [isSidebarOpen ? 'w-[260px] translate-x-0' : 'w-[260px] -translate-x-full']
    "
  >
    <div
      class="relative mt-6 mb-2 flex flex-col items-center"
      :class="compactMode ? 'px-3' : 'px-6'"
    >
      <img
        :src="logoChinh"
        alt="Logo"
        class="w-auto object-contain transition-all duration-300"
        :class="compactMode ? 'h-10' : 'h-16'"
      />
      <span v-if="!compactMode" class="mt-2 text-xs font-medium text-gray-400">Qu&#7843;n tr&#7883; vi&#234;n</span>
      <button
        type="button"
        :title="compactMode ? '\u004d\u1edf r\u1ed9ng sidebar' : 'Thu g\u1ecdn sidebar'"
        class="absolute top-2 text-gray-400 transition-colors hover:text-gray-600"
        :class="compactMode ? 'right-1/2 translate-x-1/2' : 'right-4'"
        @click="toggleSidebar"
      >
        <ChevronsLeft class="h-5 w-5" :class="compactMode ? 'rotate-180' : ''" />
      </button>
    </div>

    <nav class="mt-6 flex-1 space-y-1 overflow-y-auto" :class="compactMode ? 'px-2' : 'px-4'">
      <router-link to="/admin/thong-ke" :title="compactMode ? 'Th\u1ed1ng k\u00ea' : undefined" :class="navItemClass(isActive('/admin/thong-ke'))">
        <TrendingUp :class="navIconClass(isActive('/admin/thong-ke'))" />
        <span v-if="!compactMode" class="text-sm">Th&#7889;ng k&#234;</span>
      </router-link>

      <router-link to="/admin/hoa-don" :title="compactMode ? 'Qu\u1ea3n l\u00fd h\u00f3a \u0111\u01a1n' : undefined" :class="navItemClass(isActive('/admin/hoa-don'))">
        <ReceiptText :class="navIconClass(isActive('/admin/hoa-don'))" />
        <span v-if="!compactMode" class="text-sm">Qu&#7843;n l&#253; h&#243;a &#273;&#417;n</span>
      </router-link>

      <router-link to="/admin/don-hang" :title="compactMode ? 'Qu\u1ea3n l\u00fd \u0111\u01a1n h\u00e0ng' : undefined" :class="navItemClass(isActive('/admin/don-hang'))">
        <ClipboardList :class="navIconClass(isActive('/admin/don-hang'))" />
        <span v-if="!compactMode" class="text-sm">Qu&#7843;n l&#253; &#273;&#417;n h&#224;ng</span>
      </router-link>

      <router-link to="/admin/ban-hang" :title="compactMode ? 'B\u00e1n h\u00e0ng t\u1ea1i qu\u1ea7y' : undefined" :class="navItemClass(isActive('/admin/ban-hang'))">
        <Store :class="navIconClass(isActive('/admin/ban-hang'))" />
        <span v-if="!compactMode" class="text-sm">B&#225;n h&#224;ng t&#7841;i qu&#7847;y</span>
      </router-link>

      <router-link to="/admin/san-pham" :title="compactMode ? 'Qu\u1ea3n l\u00fd s\u1ea3n ph\u1ea9m' : undefined" :class="navItemClass(isActive('/admin/san-pham'))">
        <Package :class="navIconClass(isActive('/admin/san-pham'))" />
        <span v-if="!compactMode" class="text-sm">Qu&#7843;n l&#253; s&#7843;n ph&#7849;m</span>
      </router-link>

      <div class="space-y-1">
        <button
          type="button"
          :title="compactMode ? 'Qu\u1ea3n l\u00fd danh m\u1ee5c' : undefined"
          class="group flex w-full items-center rounded-xl px-4 py-3 transition-colors"
          :class="[
            compactMode ? 'justify-center px-3' : 'justify-between',
            checkDanhMucActive(route.path) || openDanhMuc
              ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold'
              : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50',
          ]"
          @click="toggleDanhMuc"
        >
          <div class="flex items-center">
            <Layers :class="navIconClass(checkDanhMucActive(route.path) || openDanhMuc)" />
            <span v-if="!compactMode" class="text-sm font-medium">Qu&#7843;n l&#253; danh m&#7909;c</span>
          </div>
          <ChevronDown
            v-if="!compactMode"
            class="h-4 w-4 transition-transform duration-200"
            :class="[openDanhMuc ? 'rotate-180 text-red-500' : 'text-gray-400']"
          />
        </button>
        <div v-show="openDanhMuc && !compactMode" class="space-y-1 overflow-hidden pr-4 pl-[36px] transition-all duration-300">
          <router-link to="/admin/loai-giay" :class="subItemClass(isActive('/admin/loai-giay'))">
            <Box class="mr-3 h-4 w-4 shrink-0" :class="isActive('/admin/loai-giay') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Lo&#7841;i gi&#224;y
          </router-link>
          <router-link to="/admin/thuong-hieu" :class="subItemClass(isActive('/admin/thuong-hieu'))">
            <Award class="mr-3 h-4 w-4 shrink-0" :class="isActive('/admin/thuong-hieu') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Th&#432;&#417;ng hi&#7879;u
          </router-link>
          <router-link to="/admin/de-giay" :class="subItemClass(isActive('/admin/de-giay'))">
            <Footprints class="mr-3 h-4 w-4 shrink-0" :class="isActive('/admin/de-giay') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            &#272;&#7871; gi&#224;y
          </router-link>
          <router-link to="/admin/co-giay" :class="subItemClass(isActive('/admin/co-giay'))">
            <MoveVertical class="mr-3 h-4 w-4 shrink-0" :class="isActive('/admin/co-giay') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            C&#7893; gi&#224;y
          </router-link>
          <router-link to="/admin/cong-nghe-dem" :class="subItemClass(isActive('/admin/cong-nghe-dem'))">
            <Feather class="mr-3 h-4 w-4 shrink-0" :class="isActive('/admin/cong-nghe-dem') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            C&#244;ng ngh&#7879; &#273;&#7879;m
          </router-link>
          <router-link to="/admin/mau-sac" :class="subItemClass(isActive('/admin/mau-sac'))">
            <Palette class="mr-3 h-4 w-4 shrink-0" :class="isActive('/admin/mau-sac') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            M&#224;u s&#7855;c
          </router-link>
          <router-link to="/admin/kich-co" :class="subItemClass(isActive('/admin/kich-co'))">
            <Ruler class="mr-3 h-4 w-4 shrink-0" :class="isActive('/admin/kich-co') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            K&#237;ch c&#7905;
          </router-link>
          <router-link to="/admin/trong-luong" :class="subItemClass(isActive('/admin/trong-luong'))">
            <Weight class="mr-3 h-4 w-4 shrink-0" :class="isActive('/admin/trong-luong') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Tr&#7885;ng l&#432;&#7907;ng
          </router-link>
        </div>
      </div>

      <div class="space-y-1">
        <button
          type="button"
          :title="compactMode ? 'Qu\u1ea3n l\u00fd khuy\u1ebfn m\u00e3i' : undefined"
          class="group flex w-full items-center rounded-xl px-4 py-3 transition-colors"
          :class="[
            compactMode ? 'justify-center px-3' : 'justify-between',
            isActive('/admin/phieu-giam-gia') || isActive('/admin/dot-giam-gia') || openKhuyenMai
              ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold'
              : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50',
          ]"
          @click="toggleKhuyenMai"
        >
          <div class="flex items-center">
            <BadgePercent :class="navIconClass(isActive('/admin/phieu-giam-gia') || isActive('/admin/dot-giam-gia') || openKhuyenMai)" />
            <span v-if="!compactMode" class="text-sm font-medium">Qu&#7843;n l&#253; khuy&#7871;n m&#227;i</span>
          </div>
          <ChevronDown
            v-if="!compactMode"
            class="h-4 w-4 transition-transform duration-200"
            :class="[openKhuyenMai ? 'rotate-180 text-red-500' : 'text-gray-400']"
          />
        </button>
        <div v-show="openKhuyenMai && !compactMode" class="space-y-1 overflow-hidden pr-4 pl-[36px] transition-all duration-300">
          <router-link to="/admin/phieu-giam-gia" :class="subItemClass(isActive('/admin/phieu-giam-gia'))">
            <Ticket class="mr-3 h-[18px] w-[18px] shrink-0" :class="isActive('/admin/phieu-giam-gia') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Phi&#7871;u gi&#7843;m gi&#225;
          </router-link>
          <router-link to="/admin/dot-giam-gia" :class="subItemClass(isActive('/admin/dot-giam-gia'))">
            <Tag class="mr-3 h-[18px] w-[18px] shrink-0" :class="isActive('/admin/dot-giam-gia') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            &#272;&#7907;t gi&#7843;m gi&#225;
          </router-link>
        </div>
      </div>

      <router-link to="/admin/nhan-vien" :title="compactMode ? 'Qu\u1ea3n l\u00fd nh\u00e2n vi\u00ean' : undefined" :class="navItemClass(isActive('/admin/nhan-vien'))">
        <UserRoundCog :class="navIconClass(isActive('/admin/nhan-vien'))" />
        <span v-if="!compactMode" class="text-sm">Qu&#7843;n l&#253; nh&#226;n vi&#234;n</span>
      </router-link>

      <router-link to="/admin/khach-hang" :title="compactMode ? 'Qu\u1ea3n l\u00fd kh\u00e1ch h\u00e0ng' : undefined" :class="navItemClass(isActive('/admin/khach-hang'))">
        <Users :class="navIconClass(isActive('/admin/khach-hang'))" />
        <span v-if="!compactMode" class="text-sm">Qu&#7843;n l&#253; kh&#225;ch h&#224;ng</span>
      </router-link>
    </nav>
  </aside>
</template>
