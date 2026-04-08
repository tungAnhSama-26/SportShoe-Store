<script setup lang="ts">
import SidebarAdmin from './SidebarAdmin.vue';
import HeaderAdmin from './HeaderAdmin.vue';
import { isSidebarOpen } from '../../composable/useSidebar';
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa] flex font-sans text-gray-800 relative">
    <!-- Mobile overlay -->
    <div 
      v-show="isSidebarOpen" 
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
