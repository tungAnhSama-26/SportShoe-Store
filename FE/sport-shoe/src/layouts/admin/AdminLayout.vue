<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import { CircleCheckBig, X } from "lucide-vue-next";
import SidebarAdmin from "./SidebarAdmin.vue";
import HeaderAdmin from "./HeaderAdmin.vue";
import { isDesktopSidebar, isSidebarOpen, syncSidebarWithViewport } from "../../composable/useSidebar";
import { useAdminSession } from "../../composable/useAdminSession";

const { adminSession, refreshAdminSession, shouldShowRoleNotice } = useAdminSession();
const hienThongBaoDangNhap = ref(false);
let thongBaoTimeout;

function dongThongBaoDangNhap() {
  hienThongBaoDangNhap.value = false;
  if (thongBaoTimeout) {
    window.clearTimeout(thongBaoTimeout);
    thongBaoTimeout = undefined;
  }
}

function xuLyDongBoPhien() {
  refreshAdminSession();
}

onMounted(() => {
  syncSidebarWithViewport();
  refreshAdminSession();

  if (shouldShowRoleNotice()) {
    hienThongBaoDangNhap.value = true;
    thongBaoTimeout = window.setTimeout(() => {
      hienThongBaoDangNhap.value = false;
      thongBaoTimeout = undefined;
    }, 4200);
  }

  window.addEventListener("resize", syncSidebarWithViewport);
  window.addEventListener("storage", xuLyDongBoPhien);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", syncSidebarWithViewport);
  window.removeEventListener("storage", xuLyDongBoPhien);
  dongThongBaoDangNhap();
});
</script>

<template>
  <div class="admin-shell min-h-screen flex font-sans text-gray-800 dark:text-slate-100 relative transition-colors duration-300">
    <!-- Mobile overlay -->
    <div 
      v-show="!isDesktopSidebar && isSidebarOpen" 
      @click="isSidebarOpen = false"
      class="fixed inset-0 bg-black/50 z-40 lg:hidden transition-opacity"
    ></div>

    <SidebarAdmin />
    <div class="flex-1 flex min-w-0 flex-col">
      <HeaderAdmin />
      <main class="admin-content flex-1 w-full min-w-0 overflow-x-hidden p-4 lg:p-6">
        <RouterView v-slot="{ Component, route }">
          <Transition name="page-fade">
            <component :is="Component" :key="route.fullPath" />
          </Transition>
        </RouterView>
      </main>
    </div>

    <div
      v-if="hienThongBaoDangNhap"
      class="fixed right-4 top-[88px] z-[70] w-[min(92vw,360px)] rounded-3xl border border-emerald-100 bg-white px-4 py-4 shadow-[0_20px_45px_rgba(15,23,42,0.12)] dark:border-emerald-900/50 dark:bg-slate-800"
    >
      <div class="flex items-start gap-3">
        <div class="mt-0.5 rounded-2xl bg-emerald-50 p-2 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-300">
          <CircleCheckBig class="h-5 w-5" />
        </div>

        <div class="min-w-0 flex-1">
          <p class="text-sm font-medium text-slate-800 dark:text-slate-100">Đăng nhập thành công</p>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-300">
            Bạn đang sử dụng quyền {{ adminSession.vaiTro }}.
          </p>
        </div>

        <button
          type="button"
          @click="dongThongBaoDangNhap"
          class="rounded-full p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-700 dark:hover:text-slate-100"
        >
          <X class="h-4 w-4" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-fade-enter-active {
  transition: opacity 0.35s ease, transform 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: absolute;
  width: 100%;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateX(40px);
}

.page-fade-enter-to {
  opacity: 1;
  transform: translateX(0);
}

.page-fade-leave-from {
  opacity: 1;
  transform: translateX(0);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateX(-40px);
}
</style>
