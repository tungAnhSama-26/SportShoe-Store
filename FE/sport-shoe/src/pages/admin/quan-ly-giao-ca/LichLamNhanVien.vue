<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import {
  CalendarClock,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Clock,
  Sun,
  Sunset,
  Moon,
} from "lucide-vue-next";
import { layLichLamViec } from "../../../services/lich-lam.js";
import { useAdminSession } from "../../../composable/useAdminSession.js";

const router = useRouter();
const { adminSession } = useAdminSession();

// ────────── Ca làm ──────────
const DS_CA = [
  {
    id: "sang",
    nhan: "Sáng",
    gio: "08:00 – 12:00",
    gioVao: "08:00",
    gioRa: "12:00",
    soGio: 4,
    icon: Sun,
    bg: "bg-amber-50 dark:bg-amber-900/20",
    border: "border-amber-200 dark:border-amber-700",
    text: "text-amber-700 dark:text-amber-300",
    dot: "bg-amber-400",
    badge: "bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-300",
  },
  {
    id: "chieu",
    nhan: "Chiều",
    gio: "12:00 – 17:00",
    gioVao: "12:00",
    gioRa: "17:00",
    soGio: 4,
    icon: Sunset,
    bg: "bg-orange-50 dark:bg-orange-900/20",
    border: "border-orange-200 dark:border-orange-700",
    text: "text-orange-700 dark:text-orange-300",
    dot: "bg-orange-400",
    badge: "bg-orange-100 text-orange-700 dark:bg-orange-900/40 dark:text-orange-300",
  },
  {
    id: "toi",
    nhan: "Tối",
    gio: "17:00 – 22:00",
    gioVao: "17:00",
    gioRa: "22:00",
    soGio: 4,
    icon: Moon,
    bg: "bg-violet-50 dark:bg-violet-900/20",
    border: "border-violet-200 dark:border-violet-700",
    text: "text-violet-700 dark:text-violet-300",
    dot: "bg-violet-400",
    badge: "bg-violet-100 text-violet-700 dark:bg-violet-900/40 dark:text-violet-300",
  },
];

function getCaInfo(caId) {
  return DS_CA.find((c) => c.id === caId) ?? null;
}

// ────────── Tuần ──────────
const ngayHienTai = ref(new Date());

function dauTuan(d) {
  const nd = new Date(d);
  const day = nd.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  nd.setDate(nd.getDate() + diff);
  nd.setHours(0, 0, 0, 0);
  return nd;
}

const ngayDauTuan = computed(() => dauTuan(ngayHienTai.value));

const cacNgay = computed(() =>
  Array.from({ length: 7 }, (_, i) => {
    const d = new Date(ngayDauTuan.value);
    d.setDate(d.getDate() + i);
    return d;
  })
);

const NHAN_TUAN = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];

function formatNgay(d) {
  return `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}`;
}

function formatTuanHienThi() {
  const dau = cacNgay.value[0];
  const cuoi = cacNgay.value[6];
  const f = (d) =>
    `${String(d.getDate()).padStart(2, "0")} tháng ${d.getMonth() + 1}, ${d.getFullYear()}`;
  return `${f(dau)} – ${f(cuoi)}`;
}

function formatISODate(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

function laNgayHomNay(d) {
  const hom = new Date();
  return (
    d.getDate() === hom.getDate() &&
    d.getMonth() === hom.getMonth() &&
    d.getFullYear() === hom.getFullYear()
  );
}

function tuanTruoc() {
  const d = new Date(ngayHienTai.value);
  d.setDate(d.getDate() - 7);
  ngayHienTai.value = d;
}
function tuanSau() {
  const d = new Date(ngayHienTai.value);
  d.setDate(d.getDate() + 7);
  ngayHienTai.value = d;
}
function homNay() {
  ngayHienTai.value = new Date();
}

// ────────── Dữ liệu lịch ──────────
const dangTai = ref(false);
const lichTheoNgay = ref(Array(7).fill(null)); // mỗi slot: caId | null

async function taiLich() {
  dangTai.value = true;
  const tuNgay = formatISODate(cacNgay.value[0]);
  const denNgay = formatISODate(cacNgay.value[6]);
  try {
    // Dùng API admin đã có, lọc theo ID nhân viên hiện tại
    const currentUserId = adminSession.value?.id ?? adminSession.value?.nhanVienId;
    const data = await layLichLamViec(tuNgay, denNgay);
    const filtered = (data ?? []).filter(
      (l) => !currentUserId || String(l.nhanVienId) === String(currentUserId)
    );
    lichTheoNgay.value = cacNgay.value.map((ngay) => {
      const dateStr = formatISODate(ngay);
      const item = filtered.find((l) => l.ngay === dateStr);
      return item ? item.ca : null;
    });
  } catch {
    // BE chưa có dữ liệu – hiển thị tuần trống
    lichTheoNgay.value = Array(7).fill(null);
  } finally {
    dangTai.value = false;
  }
}

onMounted(taiLich);
watch(ngayDauTuan, taiLich);

// ────────── Thống kê ──────────
const tongGio = computed(() =>
  lichTheoNgay.value.reduce((sum, ca) => {
    const info = getCaInfo(ca);
    return sum + (info ? info.soGio : 0);
  }, 0)
);

const demTheoLoaiCa = computed(() => {
  const counts = { sang: 0, chieu: 0, toi: 0 };
  lichTheoNgay.value.forEach((ca) => {
    if (ca && counts[ca] !== undefined) counts[ca]++;
  });
  return counts;
});

const soCaNghi = computed(() =>
  lichTheoNgay.value.filter((c) => c === null).length
);
</script>

<template>
  <div class="space-y-6">
    <!-- ── Header ── -->
    <div class="flex flex-wrap items-start justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">
          Lịch làm việc của tôi
        </h1>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Xem ca làm việc cá nhân theo tuần
        </p>
      </div>

      <!-- Điều hướng tuần -->
      <div class="flex items-center gap-2">
        <button
          @click="tuanTruoc"
          class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-600 transition hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-300 dark:hover:bg-slate-600"
        >
          <ChevronLeft class="h-4 w-4" />
        </button>
        <button
          @click="homNay"
          class="h-9 rounded-lg border border-slate-200 bg-white px-4 text-sm font-medium text-slate-700 transition hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-300"
        >
          Hôm nay
        </button>
        <button
          @click="tuanSau"
          class="flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-600 transition hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-300 dark:hover:bg-slate-600"
        >
          <ChevronRight class="h-4 w-4" />
        </button>
        <div class="flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-200">
          <CalendarDays class="h-4 w-4 text-slate-400" />
          {{ formatTuanHienThi() }}
        </div>
      </div>
    </div>

    <!-- ── Thẻ thống kê ── -->
    <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
      <!-- Tổng giờ -->
      <div class="rounded-2xl border border-slate-100 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800">
        <div class="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-400">
          <Clock class="h-3.5 w-3.5" />
          Tổng giờ
        </div>
        <div class="mt-2 text-3xl font-bold text-slate-900 dark:text-white">
          {{ tongGio }}<span class="ml-1 text-base font-normal text-slate-400">h</span>
        </div>
      </div>

      <!-- Ca sáng -->
      <div class="rounded-2xl border border-amber-100 bg-amber-50 p-4 shadow-sm dark:border-amber-800 dark:bg-amber-900/20">
        <div class="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-amber-500">
          <Sun class="h-3.5 w-3.5" />
          Ca sáng
        </div>
        <div class="mt-2 text-3xl font-bold text-amber-700 dark:text-amber-300">
          {{ demTheoLoaiCa.sang }}<span class="ml-1 text-base font-normal text-amber-400">ca</span>
        </div>
      </div>

      <!-- Ca chiều -->
      <div class="rounded-2xl border border-orange-100 bg-orange-50 p-4 shadow-sm dark:border-orange-800 dark:bg-orange-900/20">
        <div class="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-orange-500">
          <Sunset class="h-3.5 w-3.5" />
          Ca chiều
        </div>
        <div class="mt-2 text-3xl font-bold text-orange-700 dark:text-orange-300">
          {{ demTheoLoaiCa.chieu }}<span class="ml-1 text-base font-normal text-orange-400">ca</span>
        </div>
      </div>

      <!-- Ca tối -->
      <div class="rounded-2xl border border-violet-100 bg-violet-50 p-4 shadow-sm dark:border-violet-800 dark:bg-violet-900/20">
        <div class="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-violet-500">
          <Moon class="h-3.5 w-3.5" />
          Ca tối
        </div>
        <div class="mt-2 text-3xl font-bold text-violet-700 dark:text-violet-300">
          {{ demTheoLoaiCa.toi }}<span class="ml-1 text-base font-normal text-violet-400">ca</span>
        </div>
      </div>
    </div>

    <!-- ── Bảng lịch 7 ngày ── -->
    <div class="overflow-hidden rounded-2xl border border-slate-100 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <!-- Skeleton loading -->
      <div v-if="dangTai" class="p-6 space-y-3">
        <div class="grid grid-cols-7 gap-3">
          <div v-for="i in 7" :key="i" class="h-32 rounded-xl bg-slate-100 animate-pulse dark:bg-slate-700" />
        </div>
      </div>

      <div v-else class="p-4 sm:p-6">
        <!-- Header cột ngày -->
        <div class="grid grid-cols-7 gap-2 mb-3">
          <div
            v-for="(ngay, idx) in cacNgay"
            :key="idx"
            class="flex flex-col items-center rounded-xl py-2"
            :class="laNgayHomNay(ngay)
              ? 'bg-primary/10 dark:bg-primary/20'
              : 'bg-slate-50 dark:bg-slate-700/50'"
          >
            <span
              class="text-[11px] font-semibold uppercase tracking-wide"
              :class="laNgayHomNay(ngay) ? 'text-primary' : 'text-slate-400'"
            >
              {{ NHAN_TUAN[idx] }}
            </span>
            <span
              class="mt-0.5 text-lg font-bold"
              :class="laNgayHomNay(ngay) ? 'text-primary' : 'text-slate-700 dark:text-slate-200'"
            >
              {{ ngay.getDate() }}
            </span>
            <span class="text-[10px] text-slate-400">{{ formatNgay(ngay) }}</span>
          </div>
        </div>

        <!-- Ô ca làm -->
        <div class="grid grid-cols-7 gap-2">
          <div
            v-for="(ngay, idx) in cacNgay"
            :key="idx"
            class="min-h-[120px] rounded-xl border-2 p-2 transition"
            :class="[
              laNgayHomNay(ngay) ? 'border-primary/30' : 'border-slate-100 dark:border-slate-600',
              lichTheoNgay[idx] ? getCaInfo(lichTheoNgay[idx])?.bg : 'bg-slate-50 dark:bg-slate-700/30'
            ]"
          >
            <template v-if="lichTheoNgay[idx]">
              <div :class="['flex flex-col h-full gap-1.5']">
                <!-- Icon ca -->
                <div class="flex items-center gap-1.5">
                  <div
                    class="flex h-6 w-6 items-center justify-center rounded-lg"
                    :class="getCaInfo(lichTheoNgay[idx])?.badge"
                  >
                    <component :is="getCaInfo(lichTheoNgay[idx])?.icon" class="h-3.5 w-3.5" />
                  </div>
                  <span
                    class="text-xs font-bold"
                    :class="getCaInfo(lichTheoNgay[idx])?.text"
                  >
                    {{ getCaInfo(lichTheoNgay[idx])?.nhan }}
                  </span>
                </div>

                <!-- Giờ làm -->
                <div
                  class="mt-auto rounded-lg px-2 py-1 text-center text-[10px] font-semibold"
                  :class="getCaInfo(lichTheoNgay[idx])?.badge"
                >
                  {{ getCaInfo(lichTheoNgay[idx])?.gioVao }}<br />{{ getCaInfo(lichTheoNgay[idx])?.gioRa }}
                </div>

                <!-- Số giờ -->
                <div class="text-center text-[10px] font-medium" :class="getCaInfo(lichTheoNgay[idx])?.text">
                  {{ getCaInfo(lichTheoNgay[idx])?.soGio }}h
                </div>
              </div>
            </template>

            <!-- Ngày nghỉ -->
            <template v-else>
              <div class="flex h-full flex-col items-center justify-center gap-1">
                <span class="text-2xl">😴</span>
                <span class="text-[10px] font-medium text-slate-400">Nghỉ</span>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Chú giải ── -->
    <div class="flex flex-wrap items-center gap-4 text-sm text-slate-500 dark:text-slate-400">
      <div v-for="ca in DS_CA" :key="ca.id" class="flex items-center gap-2">
        <span class="h-3 w-3 rounded-full" :class="ca.dot" />
        <span>Ca {{ ca.nhan }} ({{ ca.gio }})</span>
      </div>
      <div class="flex items-center gap-2">
        <span class="h-3 w-3 rounded-full bg-slate-200" />
        <span>Ngày nghỉ</span>
      </div>
    </div>
  </div>
</template>
