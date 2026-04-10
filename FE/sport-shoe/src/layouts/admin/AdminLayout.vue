<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue';
import SidebarAdmin from './SidebarAdmin.vue';
import HeaderAdmin from './HeaderAdmin.vue';
import { isDesktopSidebar, isSidebarOpen, syncSidebarWithViewport } from '../../composable/useSidebar';

onMounted(() => {
  syncSidebarWithViewport();
  window.addEventListener('resize', syncSidebarWithViewport);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncSidebarWithViewport);
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa] dark:bg-slate-800 flex font-sans text-gray-800 dark:text-slate-100 relative transition-colors duration-300">
    <!-- Mobile overlay -->
    <div 
      v-show="!isDesktopSidebar && isSidebarOpen" 
      @click="isSidebarOpen = false"
      class="fixed inset-0 bg-black/50 z-40 lg:hidden transition-opacity"
    ></div>

    <SidebarAdmin />
    <div class="flex-1 flex flex-col min-w-0">
      <HeaderAdmin />
      <main class="flex-1 p-4 lg:p-6 overflow-x-hidden">
        <router-view />
      </main>
    </div>
  </div>
</template>
