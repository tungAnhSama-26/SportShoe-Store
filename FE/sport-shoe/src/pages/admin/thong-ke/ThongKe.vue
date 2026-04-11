<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  BarChart3,
  Calendar,
  Filter,
  Package,
  PieChart,
  RefreshCw,
  Search,
  ShoppingCart,
  Store,
  TrendingUp,
  Users
} from "lucide-vue-next";
import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Tooltip
} from "chart.js";
import { Bar, Pie } from "vue-chartjs";
import { layDashboardThongKe } from "../../../services/thong-ke";

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Tooltip, Legend);

const PERIOD_OPTIONS = [
  { value: "DAY", label: "Theo ngày" },
  { value: "MONTH", label: "Theo tháng" },
  { value: "YEAR", label: "Theo năm" }
];

const PIE_COLORS = [
  "#ef4444",
  "#f97316",
  "#f59e0b",
  "#10b981",
  "#06b6d4",
  "#3b82f6",
  "#6366f1",
  "#8b5cf6",
  "#ec4899",
  "#14b8a6"
];

const EMPTY_DASHBOARD = () => ({
  boLoc: {
    kyThongKe: "DAY",
    tuNgay: "",
    denNgay: "",
    thuongHieuId: null,
    keyword: null
  },
  tongQuan: {
    tongDoanhThu: 0,
    tongDonHang: 0,
    sanPhamDaBan: 0,
    khachMoi: 0
  },
  thuongHieus: [],
  bieuDoBanHang: [],
  bieuDoThuongHieu: [],
  sanPhams: []
});

const dashboard = ref(EMPTY_DASHBOARD());
const isLoading = ref(false);
const errorMessage = ref("");
const filters = reactive(createDefaultFilters());

const periodLabel = computed(() => {
  switch (filters.periodType) {
    case "MONTH":
      return "tháng";
    case "YEAR":
      return "năm";
    default:
      return "ngày";
  }
});

const summaryCards = computed(() => [
  {
    label: "Tổng doanh thu",
    value: formatCurrency(dashboard.value.tongQuan.tongDoanhThu),
    icon: TrendingUp,
    iconClass: "text-emerald-600",
    badgeClass: "bg-emerald-50"
  },
  {
    label: "Tổng đơn hàng",
    value: formatNumber(dashboard.value.tongQuan.tongDonHang),
    icon: ShoppingCart,
    iconClass: "text-sky-600",
    badgeClass: "bg-sky-50"
  },
  {
    label: "Sản phẩm đã bán",
    value: formatNumber(dashboard.value.tongQuan.sanPhamDaBan),
    icon: Package,
    iconClass: "text-amber-600",
    badgeClass: "bg-amber-50"
  },
  {
    label: "Khách mới",
    value: formatNumber(dashboard.value.tongQuan.khachMoi),
    icon: Users,
    iconClass: "text-rose-600",
    badgeClass: "bg-rose-50"
  }
]);

const salesChartData = computed(() => ({
  labels: dashboard.value.bieuDoBanHang.map((item) => item.nhan),
  datasets: [
    {
      label: "Sản phẩm bán được",
      data: dashboard.value.bieuDoBanHang.map((item) => item.soLuongBan ?? 0),
      backgroundColor: "#ff6b6b",
      borderRadius: 14,
      borderSkipped: false,
      maxBarThickness: 34
    }
  ]
}));

const salesChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      backgroundColor: "#111827",
      padding: 12,
      displayColors: false,
      callbacks: {
        label(context) {
          return `${formatNumber(context.raw)} sản phẩm`;
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: {
        color: "#f1f5f9"
      },
      ticks: {
        color: "#64748b",
        precision: 0
      }
    },
    x: {
      grid: {
        display: false
      },
      ticks: {
        color: "#64748b",
        maxRotation: 0,
        autoSkip: true
      }
    }
  }
}));

const brandChartData = computed(() => ({
  labels: dashboard.value.bieuDoThuongHieu.map((item) => item.tenThuongHieu),
  datasets: [
    {
      data: dashboard.value.bieuDoThuongHieu.map((item) => item.tongTonKho ?? 0),
      backgroundColor: dashboard.value.bieuDoThuongHieu.map((_, index) => PIE_COLORS[index % PIE_COLORS.length]),
      borderWidth: 0
    }
  ]
}));

const brandChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: "bottom",
      labels: {
        usePointStyle: true,
        boxWidth: 8,
        padding: 16,
        color: "#475569",
        font: {
          size: 11
        }
      }
    },
    tooltip: {
      backgroundColor: "#111827",
      padding: 12,
      callbacks: {
        label(context) {
          return `${context.label}: ${formatNumber(context.raw)} sản phẩm tồn`;
        }
      }
    }
  }
}));

const topBrands = computed(() => dashboard.value.bieuDoThuongHieu.slice(0, 5));
const hasSalesData = computed(() => dashboard.value.bieuDoBanHang.some((item) => (item.soLuongBan ?? 0) > 0));
const hasBrandData = computed(() => dashboard.value.bieuDoThuongHieu.length > 0);

async function fetchDashboard() {
  isLoading.value = true;
  errorMessage.value = "";

  try {
    const data = await layDashboardThongKe({
      fromDate: filters.fromDate,
      toDate: filters.toDate,
      brandId: filters.brandId,
      keyword: filters.keyword,
      periodType: filters.periodType
    });

    dashboard.value = normalizeDashboard(data);
    syncFiltersFromServer(data.boLoc);
  } catch (error) {
    dashboard.value = EMPTY_DASHBOARD();
    errorMessage.value = error.message || "Không thể tải dữ liệu thống kê.";
  } finally {
    isLoading.value = false;
  }
}

function onApplyFilters() {
  fetchDashboard();
}

function onResetFilters() {
  Object.assign(filters, createDefaultFilters());
  fetchDashboard();
}

function onPeriodTypeChange() {
  const nextDefaults = createDefaultFilters(filters.periodType);
  filters.fromDate = nextDefaults.fromDate;
  filters.toDate = nextDefaults.toDate;
}

function syncFiltersFromServer(serverFilters) {
  if (!serverFilters) {
    return;
  }

  filters.periodType = serverFilters.kyThongKe || filters.periodType;
  filters.fromDate = serverFilters.tuNgay || filters.fromDate;
  filters.toDate = serverFilters.denNgay || filters.toDate;
  filters.brandId = serverFilters.thuongHieuId ?? null;
  filters.keyword = serverFilters.keyword ?? filters.keyword;
}

function normalizeDashboard(data) {
  return {
    boLoc: data?.boLoc ?? EMPTY_DASHBOARD().boLoc,
    tongQuan: {
      tongDoanhThu: Number(data?.tongQuan?.tongDoanhThu ?? 0),
      tongDonHang: Number(data?.tongQuan?.tongDonHang ?? 0),
      sanPhamDaBan: Number(data?.tongQuan?.sanPhamDaBan ?? 0),
      khachMoi: Number(data?.tongQuan?.khachMoi ?? 0)
    },
    thuongHieus: Array.isArray(data?.thuongHieus) ? data.thuongHieus : [],
    bieuDoBanHang: Array.isArray(data?.bieuDoBanHang) ? data.bieuDoBanHang : [],
    bieuDoThuongHieu: Array.isArray(data?.bieuDoThuongHieu) ? data.bieuDoThuongHieu : [],
    sanPhams: Array.isArray(data?.sanPhams) ? data.sanPhams : []
  };
}

function createDefaultFilters(periodType = "DAY") {
  const today = new Date();
  const toDate = formatDateForInput(today);
  const fromDate = resolveDefaultFromDate(today, periodType);

  return {
    fromDate,
    toDate,
    brandId: null,
    keyword: "",
    periodType
  };
}

function resolveDefaultFromDate(today, periodType) {
  const fromDate = new Date(today);

  if (periodType === "YEAR") {
    fromDate.setFullYear(today.getFullYear() - 4, 0, 1);
    return formatDateForInput(fromDate);
  }

  if (periodType === "MONTH") {
    fromDate.setMonth(0, 1);
    return formatDateForInput(fromDate);
  }

  fromDate.setDate(1);
  return formatDateForInput(fromDate);
}

function formatDateForInput(date) {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function formatCurrency(value) {
  const amount = Number(value) || 0;
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
  }).format(amount);
}

function formatNumber(value) {
  const amount = Number(value) || 0;
  return new Intl.NumberFormat("vi-VN").format(amount);
}

function rowBadgeClass(stock) {
  if (stock <= 0) {
    return "bg-red-50 text-red-600 border-red-100";
  }
  if (stock <= 10) {
    return "bg-amber-50 text-amber-700 border-amber-100";
  }
  return "bg-emerald-50 text-emerald-700 border-emerald-100";
}

onMounted(() => {
  fetchDashboard();
});
</script>

<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-2 md:flex-row md:items-end md:justify-between">
      <div>
        <h2 class="text-[24px] font-bold text-slate-800">Thống kê </h2>
      </div>
    </div>

    <div class="rounded-[28px] border border-rose-100/70 bg-white p-5 shadow-sm">
      <div class="mb-4 flex items-center gap-2 text-sm font-semibold text-slate-700">
        <Filter class="h-4 w-4 text-rose-500" />
        Bộ lọc thống kê
      </div>

      <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-6">
        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Từ ngày</label>
          <div class="relative">
            <Calendar class="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="filters.fromDate"
              type="date"
              class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
            >
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Đến ngày</label>
          <div class="relative">
            <Calendar class="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="filters.toDate"
              type="date"
              class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
            >
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Kiểu thống kê</label>
          <div class="relative">
            <BarChart3 class="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <select
              v-model="filters.periodType"
              class="h-12 w-full appearance-none rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
              @change="onPeriodTypeChange"
            >
              <option v-for="option in PERIOD_OPTIONS" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Thương hiệu</label>
          <div class="relative">
            <Store class="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <select
              v-model="filters.brandId"
              class="h-12 w-full appearance-none rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
            >
              <option :value="null">Tất cả thương hiệu</option>
              <option v-for="brand in dashboard.thuongHieus" :key="brand.id" :value="brand.id">
                {{ brand.ten }}
              </option>
            </select>
          </div>
        </div>

        <div class="space-y-2 md:col-span-2 xl:col-span-1">
          <label class="text-xs font-medium text-slate-500">Tìm sản phẩm</label>
          <div class="relative">
            <Search class="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="filters.keyword"
              type="text"
              placeholder="Mã hoặc tên sản phẩm"
              class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
              @keyup.enter="onApplyFilters"
            >
          </div>
        </div>

        <div class="flex gap-3 md:col-span-2 xl:col-span-1 xl:self-end">
          <button
            type="button"
            class="inline-flex h-12 flex-1 items-center justify-center gap-2 rounded-2xl bg-rose-500 px-4 text-sm font-semibold text-white shadow-sm shadow-rose-200 transition hover:bg-rose-600 disabled:cursor-not-allowed disabled:opacity-70"
            :disabled="isLoading"
            @click="onApplyFilters"
          >
            <Filter class="h-4 w-4" />
            Lọc
          </button>
          <button
            type="button"
            class="inline-flex h-12 flex-1 items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-70"
            :disabled="isLoading"
            @click="onResetFilters"
          >
            <RefreshCw class="h-4 w-4" />
            Reset
          </button>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="rounded-[22px] border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">
      {{ errorMessage }}
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      <div
        v-for="card in summaryCards"
        :key="card.label"
        class="rounded-[24px] border border-slate-100 bg-white p-5 shadow-sm"
      >
        <div class="mb-4">
          <div :class="['flex h-12 w-12 items-center justify-center rounded-2xl', card.badgeClass]">
            <component :is="card.icon" :class="['h-5 w-5', card.iconClass]" />
          </div>
        </div>
        <div class="text-[24px] font-bold tracking-tight text-slate-800 break-words">
          {{ card.value }}
        </div>
        <div class="mt-1 text-sm font-medium text-slate-500">
          {{ card.label }}
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[1.65fr_1fr]">
      <div class="rounded-[28px] border border-slate-100 bg-white p-6 shadow-sm">
        <div class="mb-6 flex items-start justify-between gap-3">
          <div>
            <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
              <BarChart3 class="h-4 w-4 text-rose-500" />
              Sản phẩm bán được theo {{ periodLabel }}
            </div>
            <p class="mt-1 text-sm text-slate-500">Số lượng sản phẩm bán ra theo từng mốc thời gian.</p>
          </div>
          <div v-if="isLoading" class="rounded-full bg-slate-50 px-3 py-1 text-xs font-medium text-slate-500">
            Đang tải...
          </div>
        </div>

        <div v-if="hasSalesData" class="h-[320px]">
          <Bar :data="salesChartData" :options="salesChartOptions" />
        </div>
        <div
          v-else
          class="flex h-[320px] items-center justify-center rounded-[24px] border border-dashed border-slate-200 bg-slate-50 text-sm text-slate-500"
        >
          Chưa có dữ liệu bán hàng trong bộ lọc hiện tại.
        </div>
      </div>

      <div class="rounded-[28px] border border-slate-100 bg-white p-6 shadow-sm">
        <div class="mb-6">
          <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
            <PieChart class="h-4 w-4 text-rose-500" />
            Thương hiệu
          </div>
        </div>

        <div v-if="hasBrandData" class="space-y-5">
          <div class="h-[250px]">
            <Pie :data="brandChartData" :options="brandChartOptions" />
          </div>

          <div class="space-y-3">
            <div
              v-for="(brand, index) in topBrands"
              :key="brand.thuongHieuId"
              class="flex items-center justify-between rounded-2xl bg-slate-50 px-4 py-3"
            >
              <div class="flex items-center gap-3 text-sm font-medium text-slate-700">
                <span
                  class="h-3 w-3 rounded-full"
                  :style="{ backgroundColor: PIE_COLORS[index % PIE_COLORS.length] }"
                />
                {{ brand.tenThuongHieu }}
              </div>
              <span class="text-sm font-semibold text-slate-800">
                {{ formatNumber(brand.tongTonKho) }}
              </span>
            </div>
          </div>
        </div>

        <div
          v-else
          class="flex h-[360px] items-center justify-center rounded-[24px] border border-dashed border-slate-200 bg-slate-50 text-sm text-slate-500"
        >
          Chưa có dữ liệu thương hiệu phù hợp.
        </div>
      </div>
    </div>

    <div class="rounded-[28px] border border-slate-100 bg-white p-5 shadow-sm">
      <div class="mb-5 flex flex-col gap-2 md:flex-row md:items-end md:justify-between">
        <div>
          <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
            <Package class="h-4 w-4 text-rose-500" />
            Thống kê sản phẩm
          </div>
        </div>
        <div class="rounded-full bg-slate-50 px-4 py-2 text-sm font-medium text-slate-500">
          {{ formatNumber(dashboard.sanPhams.length) }} sản phẩm
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-[900px] w-full border-separate border-spacing-y-3 text-left">
          <thead>
            <tr class="text-xs uppercase tracking-[0.18em] text-slate-400">
              <th class="px-4 py-2">STT</th>
              <th class="px-4 py-2">Mã sản phẩm</th>
              <th class="px-4 py-2">Tên sản phẩm</th>
              <th class="px-4 py-2">Thương hiệu</th>
              <th class="px-4 py-2 text-right">Đã bán</th>
              <th class="px-4 py-2 text-right">Doanh thu</th>
              <th class="px-4 py-2 text-right">Tồn kho</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="product in dashboard.sanPhams"
              :key="product.sanPhamId"
              class="rounded-[20px] bg-slate-50 text-sm text-slate-700"
            >
              <td class="rounded-l-[20px] px-4 py-4 font-semibold text-slate-500">
                {{ product.stt }}
              </td>
              <td class="px-4 py-4 font-semibold text-slate-800">
                {{ product.maSanPham }}
              </td>
              <td class="px-4 py-4">
                <div class="flex items-center gap-3">
                  <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-white text-slate-500 shadow-sm">
                    <Package class="h-4 w-4" />
                  </div>
                  <div>
                    <div class="font-semibold text-slate-800">
                      {{ product.tenSanPham }}
                    </div>
                    <div class="text-xs text-slate-500">
                      Tổng dữ liệu
                    </div>
                  </div>
                </div>
              </td>
              <td class="px-4 py-4">
                <span class="inline-flex items-center gap-2 rounded-full bg-white px-3 py-1 text-xs font-semibold text-slate-600 shadow-sm">
                  <Store class="h-3.5 w-3.5" />
                  {{ product.thuongHieu || "Chưa cập nhật" }}
                </span>
              </td>
              <td class="px-4 py-4 text-right font-semibold text-slate-800">
                {{ formatNumber(product.daBan) }}
              </td>
              <td class="px-4 py-4 text-right font-semibold text-slate-800">
                {{ formatCurrency(product.doanhThu) }}
              </td>
              <td class="rounded-r-[20px] px-4 py-4 text-right">
                <span
                  :class="[
                    'inline-flex items-center rounded-full border px-3 py-1 text-xs font-semibold',
                    rowBadgeClass(product.tonKho)
                  ]"
                >
                  {{ formatNumber(product.tonKho) }}
                </span>
              </td>
            </tr>

            <tr v-if="dashboard.sanPhams.length === 0">
              <td colspan="7" class="px-4 py-10 text-center text-sm text-slate-500">
                Hiện chưa có dữ liệu sản phẩm để hiển thị.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
