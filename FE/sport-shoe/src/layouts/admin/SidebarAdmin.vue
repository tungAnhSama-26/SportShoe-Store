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
  Tag
} from 'lucide-vue-next';

const route = useRoute();
const isActive = (path: string) => route.path.startsWith(path);

const openKhuyenMai = ref(isActive('/admin/phieu-giam-gia') || isActive('/admin/dot-giam-gia'));
watch(() => route.path, (newPath) => {
  if (newPath.startsWith('/admin/phieu-giam-gia') || newPath.startsWith('/admin/dot-giam-gia')) {
    openKhuyenMai.value = true;
  }
});
</script>

<template>
  <aside 
    class="w-[260px] bg-white border-r border-gray-100 flex flex-col h-screen fixed lg:sticky top-0 z-50 shrink-0 transition-all duration-300 ease-in-out"
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
      <router-link to="/admin/thong-ke" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/thong-ke') ? 'bg-red-50 text-red-600 font-semibold' : 'text-gray-700 hover:bg-gray-50'">
        <TrendingUp class="w-5 h-5 mr-3" :class="isActive('/admin/thong-ke') ? 'text-red-500' : 'text-gray-500 group-hover:text-gray-700'" />
        <span class="text-sm">Thống kê</span>
      </router-link>

      <router-link to="/admin/hoa-don" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/hoa-don') ? 'bg-red-50 text-red-600 font-semibold' : 'text-gray-700 hover:bg-gray-50'">
        <ReceiptText class="w-5 h-5 mr-3" :class="isActive('/admin/hoa-don') ? 'text-red-500' : 'text-gray-500 group-hover:text-gray-700'" />
        <span class="text-sm">Quản lý hóa đơn</span>
      </router-link>

      <router-link to="/admin/ban-hang" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/ban-hang') ? 'bg-red-50 text-red-600 font-semibold' : 'text-gray-700 hover:bg-gray-50'">
        <Store class="w-5 h-5 mr-3" :class="isActive('/admin/ban-hang') ? 'text-red-500' : 'text-gray-500 group-hover:text-gray-700'" />
        <span class="text-sm">Bán hàng tại quầy</span>
      </router-link>

      <button class="w-full flex items-center justify-between px-4 py-3 rounded-xl transition-colors group text-gray-700 hover:bg-gray-50">
        <div class="flex items-center">
          <Package class="w-5 h-5 mr-3 text-gray-500 group-hover:text-gray-700" />
          <span class="text-sm font-medium">Quản lý sản phẩm</span>
        </div>
        <ChevronDown class="w-4 h-4 text-gray-400"/>
      </button>

      <div class="space-y-1">
        <button @click="openKhuyenMai = !openKhuyenMai" class="w-full flex items-center justify-between px-4 py-3 rounded-xl transition-colors group" :class="(isActive('/admin/phieu-giam-gia') || isActive('/admin/dot-giam-gia') || openKhuyenMai) ? 'bg-red-50 text-red-600 font-semibold' : 'text-gray-700 hover:bg-gray-50'">
          <div class="flex items-center">
            <BadgePercent class="w-5 h-5 mr-3" :class="(isActive('/admin/phieu-giam-gia') || isActive('/admin/dot-giam-gia') || openKhuyenMai) ? 'text-red-500' : 'text-gray-500 group-hover:text-gray-700'" />
            <span class="text-sm font-medium">Quản lý khuyến mãi</span>
          </div>
          <ChevronDown class="w-4 h-4 transition-transform duration-200" :class="[openKhuyenMai ? 'rotate-180 text-red-500' : 'text-gray-400']"/>
        </button>
        <div class="pl-[36px] pr-4 space-y-1 overflow-hidden transition-all duration-300" v-show="openKhuyenMai">
          <router-link to="/admin/phieu-giam-gia" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/phieu-giam-gia') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Ticket class="w-[18px] h-[18px] mr-3" :class="isActive('/admin/phieu-giam-gia') ? 'text-red-500' : 'text-gray-400'" />
            Phiếu giảm giá
          </router-link>
          <router-link to="/admin/dot-giam-gia" class="flex items-center px-4 py-2.5 rounded-xl text-[13px] font-medium transition-colors border border-transparent" :class="isActive('/admin/dot-giam-gia') ? 'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
            <Tag class="w-[18px] h-[18px] mr-3" :class="isActive('/admin/dot-giam-gia') ? 'text-red-500' : 'text-gray-400'" />
            Đợt giảm giá
          </router-link>
        </div>
      </div>

      <router-link to="/admin/nhan-vien" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/nhan-vien') ? 'bg-red-50 text-red-600 font-semibold' : 'text-gray-700 hover:bg-gray-50'">
        <UserRoundCog class="w-5 h-5 mr-3" :class="isActive('/admin/nhan-vien') ? 'text-red-500' : 'text-gray-500 group-hover:text-gray-700'" />
        <span class="text-sm">Quản lý nhân viên</span>
      </router-link>

      <router-link to="/admin/khach-hang" class="flex items-center px-4 py-3 rounded-xl transition-colors group" :class="isActive('/admin/khach-hang') ? 'bg-red-50 text-red-600 font-semibold' : 'text-gray-700 hover:bg-gray-50'">
        <Users class="w-5 h-5 mr-3" :class="isActive('/admin/khach-hang') ? 'text-red-500' : 'text-gray-500 group-hover:text-gray-700'" />
        <span class="text-sm">Quản lý khách hàng</span>
      </router-link>
    </nav>
  </aside>
</template>
