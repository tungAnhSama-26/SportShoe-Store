<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import logoChinh from "../../assets/logo/delete-background-logo.png";
import { isSidebarOpen } from "../../composable/useSidebar";
import {
  TrendingUp,
  ReceiptText,
  Store,
  Package,
  BadgePercent,
  Users,
  UserRoundCog,
  ChevronDown,
  ChevronsLeft,
  Ticket,
  Tag,
  ClipboardList,
  Circle,
  Box,
  Layers,
  Award,
  Footprints,
  MoveVertical,
  Feather,
  Palette,
  Ruler,
  Weight
} from 'lucide-vue-next';

const route = useRoute();
const isActive = (path: string) => route.path.startsWith(path);

const checkDanhMucActive = (newPath: string) => {
  const routes = ['/admin/loai-giay', '/admin/co-giay', '/admin/de-giay', '/admin/thuong-hieu', '/admin/cong-nghe-dem', '/admin/mau-sac', '/admin/kich-co', '/admin/trong-luong'];
  return routes.some(r => newPath.startsWith(r));
};

const openDanhMuc = ref(checkDanhMucActive(route.path));
const openKhuyenMai = ref(isActive('/admin/phieu-giam-gia') || isActive('/admin/dot-giam-gia'));

watch(() => route.path, (newPath) => {
  if (checkDanhMucActive(newPath)) openDanhMuc.value = true;
  if (newPath.startsWith('/admin/phieu-giam-gia') || newPath.startsWith('/admin/dot-giam-gia')) {
    openKhuyenMai.value = true;
  }
});
</script>

<template>
  <aside 
    class="w-[260px] bg-white dark:bg-slate-700 border-r border-gray-100 dark:border-slate-600/50 flex flex-col h-screen fixed lg:sticky top-0 z-50 shrink-0 transition-all duration-300 ease-in-out"
    :class="[isSidebarOpen ? 'translate-x-0 ml-0' : '-translate-x-full lg:ml-[-260px]']"
  >
    <div class="px-6 flex flex-col items-center mt-6 mb-2 relative">
      <img :src="logoChinh" alt="Logo" class="h-16 w-auto object-contain" />
      <span class="text-xs text-gray-400 mt-2 font-medium">Quản trị viên</span>
      <button @click="isSidebarOpen = false" class="text-gray-400 hover:text-gray-600 absolute right-4 top-2 transition-opacity" :class="isSidebarOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'">
        <ChevronsLeft class="w-5 h-5"/>
      </button>
    </div>

    <nav class="flex-1 px-4 mt-6 space-y-1 overflow-y-auto">
      <router-link to="/admin/thong-ke" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/thong-ke') ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50'">
        <TrendingUp class="w-5 h-5 mr-3" :class="isActive('/admin/thong-ke') ? 'text-red-500' : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'" />
        <span class="text-sm">Thống kê</span>
      </router-link>

      <router-link to="/admin/hoa-don" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/hoa-don') ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50'">
        <ReceiptText class="w-5 h-5 mr-3" :class="isActive('/admin/hoa-don') ? 'text-red-500' : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'" />
        <span class="text-sm">Quản lý hóa đơn</span>
      </router-link>

      <router-link to="/admin/don-hang" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/don-hang') ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50'">
        <ClipboardList class="w-5 h-5 mr-3" :class="isActive('/admin/don-hang') ? 'text-red-500' : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'" />
        <span class="text-sm">Quản lý đơn hàng</span>
      </router-link>

      <router-link to="/admin/ban-hang" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/ban-hang') ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50'">
        <Store class="w-5 h-5 mr-3" :class="isActive('/admin/ban-hang') ? 'text-red-500' : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'" />
        <span class="text-sm">Bán hàng tại quầy</span>
      </router-link>

      <router-link to="/admin/san-pham" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/san-pham') ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50'">
        <Package class="w-5 h-5 mr-3" :class="isActive('/admin/san-pham') ? 'text-red-500' : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'" />
        <span class="text-sm">Quản lý sản phẩm</span>
      </router-link>

      <div class="space-y-1">
        <button @click="openDanhMuc = !openDanhMuc" class="w-full flex items-center justify-between px-4 py-3 rounded-xl transition-colors group" :class="(checkDanhMucActive(route.path) || openDanhMuc) ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50'">
          <div class="flex items-center">
            <Layers class="w-5 h-5 mr-3" :class="(checkDanhMucActive(route.path) || openDanhMuc) ? 'text-red-500' : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'" />
            <span class="text-sm font-medium">Quản lý danh mục</span>
          </div>
          <ChevronDown class="w-4 h-4 transition-transform duration-200" :class="[openDanhMuc ? 'rotate-180 text-red-500' : 'text-gray-400']"/>
        </button>
        <div class="pl-[36px] pr-4 space-y-1 overflow-hidden transition-all duration-300" v-show="openDanhMuc">
          <router-link to="/admin/loai-giay" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/loai-giay') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Box class="w-4 h-4 mr-3" :class="isActive('/admin/loai-giay') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Loại giày
          </router-link>
          <router-link to="/admin/thuong-hieu" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/thuong-hieu') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Award class="w-4 h-4 mr-3" :class="isActive('/admin/thuong-hieu') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Thương hiệu
          </router-link>
          <router-link to="/admin/de-giay" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/de-giay') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Footprints class="w-4 h-4 mr-3" :class="isActive('/admin/de-giay') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Đế giày
          </router-link>
          <router-link to="/admin/co-giay" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/co-giay') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <MoveVertical class="w-4 h-4 mr-3" :class="isActive('/admin/co-giay') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Cổ giày
          </router-link>
          <router-link to="/admin/cong-nghe-dem" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/cong-nghe-dem') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Feather class="w-4 h-4 mr-3" :class="isActive('/admin/cong-nghe-dem') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Công nghệ đệm
          </router-link>
          <router-link to="/admin/mau-sac" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/mau-sac') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Palette class="w-4 h-4 mr-3" :class="isActive('/admin/mau-sac') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Màu sắc
          </router-link>
          <router-link to="/admin/kich-co" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/kich-co') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Ruler class="w-4 h-4 mr-3" :class="isActive('/admin/kich-co') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Kích cỡ
          </router-link>
          <router-link to="/admin/trong-luong" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/trong-luong') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Weight class="w-4 h-4 mr-3" :class="isActive('/admin/trong-luong') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Trọng lượng
          </router-link>
        </div>
      </div>

      <div class="space-y-1">
        <button @click="openKhuyenMai = !openKhuyenMai" class="w-full flex items-center justify-between px-4 py-3 rounded-xl transition-colors group" :class="(isActive('/admin/phieu-giam-gia') || isActive('/admin/dot-giam-gia') || openKhuyenMai) ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50'">
          <div class="flex items-center">
            <BadgePercent class="w-5 h-5 mr-3" :class="(isActive('/admin/phieu-giam-gia') || isActive('/admin/dot-giam-gia') || openKhuyenMai) ? 'text-red-500' : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'" />
            <span class="text-sm font-medium">Quản lý khuyến mãi</span>
          </div>
          <ChevronDown class="w-4 h-4 transition-transform duration-200" :class="[openKhuyenMai ? 'rotate-180 text-red-500' : 'text-gray-400']"/>
        </button>
        <div class="pl-[36px] pr-4 space-y-1 overflow-hidden transition-all duration-300" v-show="openKhuyenMai">
          <router-link to="/admin/phieu-giam-gia" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/phieu-giam-gia') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Ticket class="w-[18px] h-[18px] mr-3" :class="isActive('/admin/phieu-giam-gia') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Phiếu giảm giá
          </router-link>
          <router-link to="/admin/dot-giam-gia" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/dot-giam-gia') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Tag class="w-[18px] h-[18px] mr-3" :class="isActive('/admin/dot-giam-gia') ? 'text-red-500' : 'text-gray-400 dark:text-gray-500'" />
            Đợt giảm giá
          </router-link>
        </div>
      </div>

      <router-link to="/admin/nhan-vien" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/nhan-vien') ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50'">
        <UserRoundCog class="w-5 h-5 mr-3" :class="isActive('/admin/nhan-vien') ? 'text-red-500' : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'" />
        <span class="text-sm">Quản lý nhân viên</span>
      </router-link>

      <router-link to="/admin/khach-hang" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/khach-hang') ? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50'">
        <Users class="w-5 h-5 mr-3" :class="isActive('/admin/khach-hang') ? 'text-red-500' : 'text-gray-500 dark:text-gray-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'" />
        <span class="text-sm">Quản lý khách hàng</span>
      </router-link>
    </nav>
  </aside>
</template>
