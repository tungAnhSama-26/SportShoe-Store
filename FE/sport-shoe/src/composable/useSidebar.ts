import { ref } from "vue";

const DESKTOP_BREAKPOINT = 1024;

export const isSidebarOpen = ref(false);
export const isSidebarCollapsed = ref(false);
export const isDesktopSidebar = ref(false);

export function syncSidebarWithViewport() {
  if (typeof window === "undefined") {
    return;
  }

  isDesktopSidebar.value = window.innerWidth >= DESKTOP_BREAKPOINT;

  if (isDesktopSidebar.value) {
    isSidebarOpen.value = true;
    return;
  }

  isSidebarCollapsed.value = false;
  isSidebarOpen.value = false;
}

export function toggleSidebar() {
  if (isDesktopSidebar.value) {
    isSidebarOpen.value = true;
    isSidebarCollapsed.value = !isSidebarCollapsed.value;
    return;
  }

  isSidebarOpen.value = !isSidebarOpen.value;
}

export function closeSidebar() {
  if (isDesktopSidebar.value) {
    isSidebarOpen.value = true;
    isSidebarCollapsed.value = true;
    return;
  }

  isSidebarOpen.value = false;
}
