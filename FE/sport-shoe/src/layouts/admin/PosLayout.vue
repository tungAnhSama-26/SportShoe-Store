<script setup>
import { onMounted, onBeforeUnmount } from "vue";
import { useAdminSession } from "../../composable/useAdminSession";

const { refreshAdminSession } = useAdminSession();

function xuLyDongBoPhien() {
  refreshAdminSession();
}

onMounted(() => {
  refreshAdminSession();
  window.addEventListener("storage", xuLyDongBoPhien);
});

onBeforeUnmount(() => {
  window.removeEventListener("storage", xuLyDongBoPhien);
});
</script>

<template>
  <div class="pos-shell h-screen w-full flex items-center justify-center font-sans text-gray-800 bg-slate-200 dark:bg-slate-950 overflow-hidden p-0 sm:p-4 lg:p-8">
    
    <!-- iPad Device Frame (Responsive) -->
    <div class="relative w-full h-full max-w-[1180px] max-h-[820px] sm:rounded-[36px] lg:rounded-[40px] sm:p-[12px] lg:p-[16px] sm:bg-black sm:shadow-[0_20px_60px_-15px_rgba(0,0,0,0.5)] flex flex-col transition-all duration-300 mx-auto overflow-hidden">
      
      <!-- iPad Camera Hole (Landscape Left) -->
      <div class="hidden sm:block absolute left-[6px] lg:left-[8px] top-1/2 -translate-y-1/2 w-2 h-2 lg:w-2.5 lg:h-2.5 rounded-full bg-slate-800 border border-black z-50 shadow-inner"></div>

      <!-- Screen Area -->
      <div class="flex-1 w-full h-full bg-[#f4f4f9] dark:bg-slate-900 sm:rounded-[24px] lg:rounded-[26px] overflow-hidden flex flex-col relative ring-1 ring-slate-800/10" id="pos-tablet-screen">
        <main class="flex-1 w-full min-w-0 overflow-hidden">
          <RouterView v-slot="{ Component, route }">
            <Transition name="page-fade" mode="out-in">
              <component :is="Component" :key="route.fullPath" />
            </Transition>
          </RouterView>
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-fade-enter-active {
  transition: opacity 0.15s ease-out, transform 0.15s ease-out;
}
.page-fade-leave-active {
  transition: opacity 0.1s ease-in;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(4px);
}
.page-fade-leave-to {
  opacity: 0;
}
</style>
