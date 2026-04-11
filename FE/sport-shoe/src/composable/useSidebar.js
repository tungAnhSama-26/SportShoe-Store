import { ref } from "vue";
const DESKTOP_BREAKPOINT = 1024;
const isSidebarOpen = ref(false);
const isSidebarCollapsed = ref(false);
const isDesktopSidebar = ref(false);
function syncSidebarWithViewport() {
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
function toggleSidebar() {
  if (isDesktopSidebar.value) {
    isSidebarOpen.value = true;
    isSidebarCollapsed.value = !isSidebarCollapsed.value;
    return;
  }
  isSidebarOpen.value = !isSidebarOpen.value;
}
function closeSidebar() {
  if (isDesktopSidebar.value) {
    isSidebarOpen.value = true;
    isSidebarCollapsed.value = true;
    return;
  }
  isSidebarOpen.value = false;
}
export {
  closeSidebar,
  isDesktopSidebar,
  isSidebarCollapsed,
  isSidebarOpen,
  syncSidebarWithViewport,
  toggleSidebar
};
