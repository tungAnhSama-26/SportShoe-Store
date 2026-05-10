<script setup>
import { computed } from "vue";
import { RouterLink, useRoute } from "vue-router";
import {
  Bell,
  BookOpen,
  Building,
  Building2,
  ChevronRight,
  FileText,
  Files,
  Globe,
  GraduationCap,
  Info,
  LayoutDashboard,
  LayoutGrid,
  Moon,
  Search,
  UserRound,
  Users,
  Wrench,
} from "lucide-vue-next";

const route = useRoute();

const sidebarGroups = computed(() => [
  {
    title: "",
    items: [
      { label: "Dashboards", icon: LayoutDashboard },
    ],
  },
  {
    title: "QUẢN LÝ TUYỂN SINH",
    items: [
      { label: "Thông tin cơ sở", icon: Info },
      { label: "Trung tâm liên kết", icon: Building2 },
      { label: "Kỳ tuyển sinh", icon: FileText },
      { label: "Tiêu chí/Trọng số đánh giá", icon: Files, hasArrow: true },
      { label: "Hồ sơ thí sinh", icon: FileText, hasArrow: true },
    ],
  },
  {
    title: "QUẢN LÝ HỌC VIÊN",
    items: [
      { label: "Dụng cụ", icon: Wrench },
      { label: "Loại hợp đồng", icon: FileText },
      { label: "Phiên bản hợp đồng", icon: Files },
      { label: "Hợp đồng đã ký", icon: FileText },
      { label: "Thông tin học viên", icon: Users },
    ],
  },
  {
    title: "QUẢN LÝ HỌC TẬP NGOẠI KHÓA",
    items: [
      { label: "Môn học ngoại khóa", icon: BookOpen },
      { label: "Phòng học ngoại khóa", icon: Building },
      { label: "Giáo viên phụ trách", icon: GraduationCap },
      { label: "Lớp học ngoại khóa", icon: Users, hasArrow: true },
    ],
  },
]);

const isLeaveRequestRoute = computed(() => route.path.includes("/don-xin-phep-nghi-hoc"));
</script>

<template>
  <div class="pvf-shell min-h-screen">
    <div class="mx-auto flex min-h-screen max-w-[1540px]">
      <aside class="hidden w-[156px] shrink-0 border-r border-[#eceef5] bg-white xl:flex xl:flex-col">
        <div class="flex items-center gap-2 border-b border-[#eceef5] px-4 py-4">
          <div class="flex h-10 w-10 items-center justify-center rounded-md bg-[#ef1f2f] text-sm font-extrabold text-white">
            PVF
          </div>
          <div class="min-w-0 leading-none">
            <p class="text-[8px] font-bold uppercase tracking-[0.08em] text-[#ef1f2f]">TRUNG TÂM</p>
            <p class="mt-1 text-[8px] font-bold uppercase tracking-[0.04em] text-[#ef1f2f]">ĐÀO TẠO BÓNG ĐÁ TRẺ PVF</p>
            <p class="mt-1 text-[7px] uppercase tracking-[0.3em] text-[#8d93a5]">Football Academy</p>
          </div>
          <button
            type="button"
            class="flex h-5 w-5 items-center justify-center rounded-full bg-[#ef1f2f] text-white"
          >
            <ChevronRight class="h-3 w-3" />
          </button>
        </div>

        <div class="border-b border-[#eceef5] px-3 py-3">
          <label class="relative block">
            <Search class="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[#a4abbc]" />
            <input
              type="text"
              placeholder="Tìm kiếm"
              class="h-8 w-full rounded-md border border-[#edf0f6] bg-[#fafbfe] pl-8 pr-3 text-[11px] text-[#5f6475] outline-none"
            />
          </label>
        </div>

        <nav class="flex-1 overflow-y-auto px-3 py-3">
          <div
            v-for="group in sidebarGroups"
            :key="group.title || 'main'"
            class="mb-5"
          >
            <p
              v-if="group.title"
              class="mb-2 px-2 text-[10px] font-semibold uppercase tracking-[0.05em] text-[#b0b5c3]"
            >
              {{ group.title }}
            </p>
            <div class="space-y-1">
              <div
                v-for="item in group.items"
                :key="item.label"
                class="flex items-center justify-between rounded-md px-2 py-2 text-[11px] text-[#5b6374]"
                :class="{
                  'bg-[#f5f7fb] text-[#404858]': item.label === 'Tiêu chí/Trọng số đánh giá' && isLeaveRequestRoute,
                }"
              >
                <div class="flex min-w-0 items-center gap-2">
                  <component :is="item.icon" class="h-3.5 w-3.5 shrink-0 text-[#7f889b]" />
                  <span class="truncate leading-[1.25]">{{ item.label }}</span>
                </div>
                <ChevronRight v-if="item.hasArrow" class="h-3.5 w-3.5 shrink-0 text-[#a8afc0]" />
              </div>
            </div>
          </div>
        </nav>
      </aside>

      <div class="flex min-w-0 flex-1 flex-col">
        <header class="border-b border-[#eceef5] bg-white">
          <div class="flex h-[62px] items-center justify-between gap-4 px-5 md:px-7">
            <div class="min-w-0">
              <h1 class="truncate text-[16px] font-bold text-[#ef3039]">Hệ Thống Quản Lý Đào Tạo PVF</h1>
            </div>
            <div class="flex shrink-0 items-center gap-3 text-[#5f6476]">
              <button type="button" class="pvf-header-icon">
                <Globe class="h-4 w-4" />
              </button>
              <button type="button" class="pvf-header-icon">
                <Moon class="h-4 w-4" />
              </button>
              <button type="button" class="pvf-header-icon">
                <LayoutGrid class="h-4 w-4" />
              </button>
              <button type="button" class="relative pvf-header-icon">
                <Bell class="h-4 w-4" />
                <span class="absolute right-[7px] top-[7px] h-1.5 w-1.5 rounded-full bg-[#ef3039]"></span>
              </button>
              <div class="relative flex h-8 w-8 items-center justify-center rounded-full bg-[linear-gradient(135deg,#6b7eff,#8f4fff)] text-white">
                <UserRound class="h-4 w-4" />
                <span class="absolute bottom-0 right-0 h-2.5 w-2.5 rounded-full border border-white bg-[#66cc55]"></span>
              </div>
            </div>
          </div>
        </header>

        <main class="flex-1 px-4 py-4 md:px-6">
          <router-view />
        </main>

        <footer class="mt-auto flex items-center justify-between px-6 pb-4 text-[10px] text-[#9aa1b3]">
          <span>2025 © PVF VN</span>
          <span>Design & Develop by FPT POLYTECHNIC</span>
        </footer>
      </div>
    </div>
  </div>
</template>
