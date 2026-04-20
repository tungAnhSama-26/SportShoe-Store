<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
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
import AppPagination from "../../../components/common/AppPagination.vue";
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

const PRODUCT_STOCK_OPTIONS = [
  { value: "ALL", label: "Tất cả tồn kho" },
  { value: "IN_STOCK", label: "Còn hàng" },
  { value: "LOW_STOCK", label: "Sắp hết" },
  { value: "OUT_OF_STOCK", label: "Hết hàng" }
];

const PRODUCT_SORT_OPTIONS = [
  { value: "BEST_SELLER", label: "Bán chạy nhất" },
  { value: "REVENUE_DESC", label: "Doanh thu cao nhất" },
  { value: "STOCK_ASC", label: "Tồn kho thấp nhất" },
  { value: "NAME_ASC", label: "Tên A - Z" }
];

const EMPLOYEE_SORT_OPTIONS = [
  { value: "REVENUE_DESC", label: "Doanh thu cao nhất" },
  { value: "ORDER_DESC", label: "Nhiều đơn nhất" },
  { value: "QUANTITY_DESC", label: "Bán nhiều sản phẩm nhất" },
  { value: "NAME_ASC", label: "Tên A - Z" }
];

const PRODUCT_PAGE_SIZE_OPTIONS = [5, 10, 20, 50];

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
  sanPhams: [],
  nhanViens: []
});

const dashboard = ref(EMPTY_DASHBOARD());
const isLoading = ref(false);
const errorMessage = ref("");
const filters = reactive(createDefaultFilters());
const fromDatePickerRef = ref(null);
const toDatePickerRef = ref(null);
const productFilters = reactive(createDefaultProductFilters());
const productCurrentPage = ref(1);
const employeeFilters = reactive(createDefaultEmployeeFilters());
const employeeCurrentPage = ref(1);
let dashboardFilterTimer;
let dashboardRequestController;
let latestDashboardRequestId = 0;

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

const salesLabels = computed(() => dashboard.value.bieuDoBanHang.map((item) => item.nhan));

const salesChartData = computed(() => ({
  labels: salesLabels.value,
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
      offset: true,
      grid: {
        display: false
      },
      ticks: {
        color: "#64748b",
        maxRotation: 0,
        autoSkip: false,
        padding: 8,
        callback(value, index) {
          return shouldShowSalesTick(index, salesLabels.value.length) ? salesLabels.value[index] : "";
        }
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
const filteredProducts = computed(() => {
  const keyword = productFilters.keyword.trim().toLowerCase();

  const matchedProducts = dashboard.value.sanPhams.filter((product) => {
    if (keyword) {
      const searchableValue = [product.maSanPham, product.tenSanPham, product.thuongHieu]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      if (!searchableValue.includes(keyword)) {
        return false;
      }
    }

    const tonKho = Number(product.tonKho ?? 0);
    switch (productFilters.stockStatus) {
      case "OUT_OF_STOCK":
        return tonKho <= 0;
      case "LOW_STOCK":
        return tonKho > 0 && tonKho <= 10;
      case "IN_STOCK":
        return tonKho > 0;
      default:
        return true;
    }
  });

  return [...matchedProducts].sort((left, right) => sortProducts(left, right, productFilters.sortBy));
});
const productTotalPages = computed(() => Math.max(1, Math.ceil(filteredProducts.value.length / productFilters.pageSize)));
const paginatedProducts = computed(() => {
  const start = (productCurrentPage.value - 1) * productFilters.pageSize;
  return filteredProducts.value.slice(start, start + productFilters.pageSize);
});
const productCountLabel = computed(() => {
  if (filteredProducts.value.length === dashboard.value.sanPhams.length) {
    return `${formatNumber(filteredProducts.value.length)} sản phẩm`;
  }

  return `${formatNumber(filteredProducts.value.length)}/${formatNumber(dashboard.value.sanPhams.length)} sản phẩm`;
});

const filteredEmployees = computed(() => {
  const keyword = employeeFilters.keyword.trim().toLowerCase();

  const matchedEmployees = dashboard.value.nhanViens.filter((employee) => {
    if (!keyword) {
      return true;
    }

    const searchableValue = [employee.maNhanVien, employee.tenNhanVien]
      .filter(Boolean)
      .join(" ")
      .toLowerCase();

    return searchableValue.includes(keyword);
  });

  return [...matchedEmployees].sort((left, right) => sortEmployees(left, right, employeeFilters.sortBy));
});
const employeeTotalPages = computed(() => Math.max(1, Math.ceil(filteredEmployees.value.length / employeeFilters.pageSize)));
const paginatedEmployees = computed(() => {
  const start = (employeeCurrentPage.value - 1) * employeeFilters.pageSize;
  return filteredEmployees.value.slice(start, start + employeeFilters.pageSize);
});
const employeeCountLabel = computed(() => {
  if (filteredEmployees.value.length === dashboard.value.nhanViens.length) {
    return `${formatNumber(filteredEmployees.value.length)} nhân viên`;
  }

  return `${formatNumber(filteredEmployees.value.length)}/${formatNumber(dashboard.value.nhanViens.length)} nhân viên`;
});
async function fetchDashboard() {
  if (dashboardFilterTimer) {
    window.clearTimeout(dashboardFilterTimer);
    dashboardFilterTimer = undefined;
  }

  dashboardRequestController?.abort();
  const requestId = ++latestDashboardRequestId;
  dashboardRequestController = new AbortController();
  isLoading.value = true;
  errorMessage.value = "";

  try {
    const data = await layDashboardThongKe({
      fromDate: filters.fromDate,
      toDate: filters.toDate,
      brandId: filters.brandId,
      keyword: filters.keyword,
      periodType: filters.periodType
    }, {
      signal: dashboardRequestController.signal
    });

    if (requestId !== latestDashboardRequestId) {
      return;
    }

    dashboard.value = normalizeDashboard(data);
    syncFiltersFromServer(data.boLoc);
  } catch (error) {
    if (error?.name === "AbortError" || requestId !== latestDashboardRequestId) {
      return;
    }
    dashboard.value = EMPTY_DASHBOARD();
    errorMessage.value = error.message || "Không thể tải dữ liệu thống kê.";
  } finally {
    if (requestId === latestDashboardRequestId) {
      isLoading.value = false;
      dashboardRequestController = undefined;
    }
  }
}

function onApplyFilters() {
  fetchDashboard();
}

function onResetFilters() {
  Object.assign(filters, createDefaultFilters());
  resetProductFilters();
  resetEmployeeFilters();
  fetchDashboard();
}

function onPeriodTypeChange() {
  const nextDefaults = createDefaultFilters(filters.periodType);
  filters.fromDate = nextDefaults.fromDate;
  filters.toDate = nextDefaults.toDate;
  scheduleDashboardFetch();
}

function openDatePicker(field) {
  const input = field === "fromDate" ? fromDatePickerRef.value : toDatePickerRef.value;
  if (!input) {
    return;
  }

  if (typeof input.showPicker === "function") {
    input.showPicker();
    return;
  }

  input.click();
}

function handleDateChange() {
  errorMessage.value = "";
  scheduleDashboardFetch();
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
    sanPhams: Array.isArray(data?.sanPhams) ? data.sanPhams : [],
    nhanViens: Array.isArray(data?.nhanViens) ? data.nhanViens : []
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

function createDefaultProductFilters() {
  return {
    keyword: "",
    stockStatus: "ALL",
    sortBy: "BEST_SELLER",
    pageSize: 10
  };
}

function createDefaultEmployeeFilters() {
  return {
    keyword: "",
    sortBy: "REVENUE_DESC",
    pageSize: 10
  };
}

function resetProductFilters() {
  Object.assign(productFilters, createDefaultProductFilters());
  productCurrentPage.value = 1;
}

function resetEmployeeFilters() {
  Object.assign(employeeFilters, createDefaultEmployeeFilters());
  employeeCurrentPage.value = 1;
}

function scheduleDashboardFetch() {
  if (dashboardFilterTimer) {
    window.clearTimeout(dashboardFilterTimer);
  }

  dashboardFilterTimer = window.setTimeout(() => {
    fetchDashboard();
  }, 250);
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

function formatDateForDisplay(value) {
  const match = String(value ?? "").match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!match) {
    return "";
  }

  return `${match[3]}/${match[2]}/${match[1]}`;
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

function shouldShowSalesTick(index, total) {
  if (index == null || total <= 0) {
    return false;
  }

  if (total <= 6) {
    return true;
  }

  const maxVisibleTicks = 6;
  const visibleIndexes = new Set([0, total - 1]);

  for (let step = 1; step < maxVisibleTicks - 1; step += 1) {
    visibleIndexes.add(Math.round((step * (total - 1)) / (maxVisibleTicks - 1)));
  }

  return visibleIndexes.has(index);
}

function sortProducts(left, right, sortBy) {
  switch (sortBy) {
    case "REVENUE_DESC":
      return Number(right.doanhThu ?? 0) - Number(left.doanhThu ?? 0)
        || Number(right.daBan ?? 0) - Number(left.daBan ?? 0)
        || String(left.tenSanPham ?? "").localeCompare(String(right.tenSanPham ?? ""), "vi");
    case "STOCK_ASC":
      return Number(left.tonKho ?? 0) - Number(right.tonKho ?? 0)
        || Number(right.daBan ?? 0) - Number(left.daBan ?? 0)
        || String(left.tenSanPham ?? "").localeCompare(String(right.tenSanPham ?? ""), "vi");
    case "NAME_ASC":
      return String(left.tenSanPham ?? "").localeCompare(String(right.tenSanPham ?? ""), "vi")
        || String(left.maSanPham ?? "").localeCompare(String(right.maSanPham ?? ""), "vi");
    default:
      return Number(right.daBan ?? 0) - Number(left.daBan ?? 0)
        || Number(right.doanhThu ?? 0) - Number(left.doanhThu ?? 0)
        || String(left.tenSanPham ?? "").localeCompare(String(right.tenSanPham ?? ""), "vi");
  }
}

function sortEmployees(left, right, sortBy) {
  switch (sortBy) {
    case "ORDER_DESC":
      return Number(right.tongDonHang ?? 0) - Number(left.tongDonHang ?? 0)
        || Number(right.doanhThu ?? 0) - Number(left.doanhThu ?? 0)
        || Number(right.sanPhamDaBan ?? 0) - Number(left.sanPhamDaBan ?? 0)
        || String(left.tenNhanVien ?? "").localeCompare(String(right.tenNhanVien ?? ""), "vi");
    case "QUANTITY_DESC":
      return Number(right.sanPhamDaBan ?? 0) - Number(left.sanPhamDaBan ?? 0)
        || Number(right.doanhThu ?? 0) - Number(left.doanhThu ?? 0)
        || Number(right.tongDonHang ?? 0) - Number(left.tongDonHang ?? 0)
        || String(left.tenNhanVien ?? "").localeCompare(String(right.tenNhanVien ?? ""), "vi");
    case "NAME_ASC":
      return String(left.tenNhanVien ?? "").localeCompare(String(right.tenNhanVien ?? ""), "vi")
        || String(left.maNhanVien ?? "").localeCompare(String(right.maNhanVien ?? ""), "vi");
    default:
      return Number(right.doanhThu ?? 0) - Number(left.doanhThu ?? 0)
        || Number(right.tongDonHang ?? 0) - Number(left.tongDonHang ?? 0)
        || Number(right.sanPhamDaBan ?? 0) - Number(left.sanPhamDaBan ?? 0)
        || String(left.tenNhanVien ?? "").localeCompare(String(right.tenNhanVien ?? ""), "vi");
  }
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

watch(
  [
    () => productFilters.keyword,
    () => productFilters.stockStatus,
    () => productFilters.sortBy,
    () => productFilters.pageSize
  ],
  () => {
    productCurrentPage.value = 1;
  }
);

watch(
  [
    () => employeeFilters.keyword,
    () => employeeFilters.sortBy,
    () => employeeFilters.pageSize
  ],
  () => {
    employeeCurrentPage.value = 1;
  }
);

watch(productTotalPages, (nextTotalPages) => {
  if (productCurrentPage.value > nextTotalPages) {
    productCurrentPage.value = nextTotalPages;
  }
}, { immediate: true });

watch(employeeTotalPages, (nextTotalPages) => {
  if (employeeCurrentPage.value > nextTotalPages) {
    employeeCurrentPage.value = nextTotalPages;
  }
}, { immediate: true });

watch(() => dashboard.value.sanPhams, () => {
  productCurrentPage.value = 1;
});

watch(() => dashboard.value.nhanViens, () => {
  employeeCurrentPage.value = 1;
});

onMounted(() => {
  fetchDashboard();
});

onBeforeUnmount(() => {
  if (dashboardFilterTimer) {
    window.clearTimeout(dashboardFilterTimer);
  }
  dashboardRequestController?.abort();
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
            <input
              ref="fromDatePickerRef"
              v-model="filters.fromDate"
              type="date"
              class="pointer-events-none absolute inset-0 opacity-0"
              :max="filters.toDate || undefined"
              tabindex="-1"
              @change="handleDateChange"
            >
            <button
              type="button"
              class="flex h-12 w-full items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-left text-sm text-slate-700 transition hover:border-rose-300 hover:bg-white focus:outline-none focus:ring-2 focus:ring-rose-200"
              @click="openDatePicker('fromDate')"
            >
              <span class="flex h-4 w-4 shrink-0 items-center justify-center text-slate-400">
                <Calendar class="h-4 w-4" />
              </span>
              <span class="text-sm font-medium">
                {{ formatDateForDisplay(filters.fromDate) || "Chọn ngày" }}
              </span>
            </button>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Đến ngày</label>
          <div class="relative">
            <input
              ref="toDatePickerRef"
              v-model="filters.toDate"
              type="date"
              class="pointer-events-none absolute inset-0 opacity-0"
              :min="filters.fromDate || undefined"
              tabindex="-1"
              @change="handleDateChange"
            >
            <button
              type="button"
              class="flex h-12 w-full items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-left text-sm text-slate-700 transition hover:border-rose-300 hover:bg-white focus:outline-none focus:ring-2 focus:ring-rose-200"
              @click="openDatePicker('toDate')"
            >
              <span class="flex h-4 w-4 shrink-0 items-center justify-center text-slate-400">
                <Calendar class="h-4 w-4" />
              </span>
              <span class="text-sm font-medium">
                {{ formatDateForDisplay(filters.toDate) || "Chọn ngày" }}
              </span>
            </button>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Thống kê</label>
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
              @input="scheduleDashboardFetch"
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
            <Users class="h-4 w-4 text-rose-500" />
            Thống kê doanh thu nhân viên
          </div>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <div class="rounded-full bg-slate-50 px-4 py-2 text-sm font-medium text-slate-500">
            {{ employeeCountLabel }}
          </div>
        </div>
      </div>

      <div class="mb-5 grid gap-4 xl:grid-cols-[1.4fr_1fr_240px]">
        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Tìm nhân viên</label>
          <div class="relative">
            <Search class="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="employeeFilters.keyword"
              type="text"
              placeholder="Mã hoặc tên nhân viên"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
            >
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Sắp xếp</label>
          <select
            v-model="employeeFilters.sortBy"
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
          >
            <option
              v-for="option in EMPLOYEE_SORT_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Hiển thị</label>
          <div class="flex gap-2">
            <select
              v-model="employeeFilters.pageSize"
              class="h-11 min-w-0 flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
            >
              <option
                v-for="size in PRODUCT_PAGE_SIZE_OPTIONS"
                :key="size"
                :value="size"
              >
                {{ size }} dòng
              </option>
            </select>
            <button
              type="button"
              class="inline-flex h-11 items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
              @click="resetEmployeeFilters"
            >
              <RefreshCw class="h-4 w-4" />
              Reset
            </button>
          </div>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-[860px] w-full border-separate border-spacing-y-3 text-left">
          <thead>
            <tr class="text-xs uppercase tracking-[0.18em] text-slate-400">
              <th class="px-4 py-2">STT</th>
              <th class="px-4 py-2">Mã nhân viên</th>
              <th class="px-4 py-2">Nhân viên</th>
              <th class="px-4 py-2 text-right">Đơn hàng</th>
              <th class="px-4 py-2 text-right">Sản phẩm bán</th>
              <th class="px-4 py-2 text-right">Doanh thu</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(employee, index) in paginatedEmployees"
              :key="employee.nhanVienId || `unassigned-${index}`"
              class="rounded-[20px] bg-slate-50 text-sm text-slate-700"
            >
              <td class="rounded-l-[20px] px-4 py-4 font-semibold text-slate-500">
                {{ (employeeCurrentPage - 1) * employeeFilters.pageSize + index + 1 }}
              </td>
              <td class="px-4 py-4">
                <span class="inline-flex items-center rounded-full bg-white px-3 py-1 text-xs font-semibold text-slate-600 shadow-sm">
                  {{ employee.maNhanVien || "Chưa có mã" }}
                </span>
              </td>
              <td class="px-4 py-4">
                <div class="flex items-center gap-3">
                  <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-white text-slate-500 shadow-sm">
                    <Users class="h-4 w-4" />
                  </div>
                  <div>
                    <div class="font-semibold text-slate-800">
                      {{ employee.tenNhanVien }}
                    </div>
                    <div class="text-xs text-slate-500">
                      {{ employee.nhanVienId ? "Nhân viên bán hàng" : "Đơn chưa gán nhân viên" }}
                    </div>
                  </div>
                </div>
              </td>
              <td class="px-4 py-4 text-right font-semibold text-slate-800">
                {{ formatNumber(employee.tongDonHang) }}
              </td>
              <td class="px-4 py-4 text-right font-semibold text-slate-800">
                {{ formatNumber(employee.sanPhamDaBan) }}
              </td>
              <td class="rounded-r-[20px] px-4 py-4 text-right font-semibold text-slate-800">
                {{ formatCurrency(employee.doanhThu) }}
              </td>
            </tr>

            <tr v-if="filteredEmployees.length === 0">
              <td colspan="6" class="px-4 py-10 text-center text-sm text-slate-500">
                Không có nhân viên phát sinh doanh thu trong bộ lọc hiện tại.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AppPagination
        v-if="filteredEmployees.length > 0"
        v-model="employeeCurrentPage"
        class="mt-4"
        :total-items="filteredEmployees.length"
        :page-size="employeeFilters.pageSize"
      />
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
          {{ productCountLabel }}
        </div>
      </div>

      <div class="mb-5 grid gap-4 xl:grid-cols-[1.4fr_1fr_1fr_240px]">
        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Lọc trong bảng</label>
          <div class="relative">
            <Search class="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="productFilters.keyword"
              type="text"
              placeholder="Mã, tên hoặc thương hiệu"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
            >
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Tồn kho</label>
          <select
            v-model="productFilters.stockStatus"
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
          >
            <option
              v-for="option in PRODUCT_STOCK_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Sắp xếp</label>
          <select
            v-model="productFilters.sortBy"
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
          >
            <option
              v-for="option in PRODUCT_SORT_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-medium text-slate-500">Hiển thị</label>
          <div class="flex gap-2">
            <select
              v-model="productFilters.pageSize"
              class="h-11 min-w-0 flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
            >
              <option
                v-for="size in PRODUCT_PAGE_SIZE_OPTIONS"
                :key="size"
                :value="size"
              >
                {{ size }} dòng
              </option>
            </select>
            <button
              type="button"
              class="inline-flex h-11 items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
              @click="resetProductFilters"
            >
              <RefreshCw class="h-4 w-4" />
              Reset
            </button>
          </div>
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
              v-for="(product, index) in paginatedProducts"
              :key="product.sanPhamId"
              class="rounded-[20px] bg-slate-50 text-sm text-slate-700"
            >
              <td class="rounded-l-[20px] px-4 py-4 font-semibold text-slate-500">
                {{ (productCurrentPage - 1) * productFilters.pageSize + index + 1 }}
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

            <tr v-if="filteredProducts.length === 0">
              <td colspan="7" class="px-4 py-10 text-center text-sm text-slate-500">
                Không có sản phẩm phù hợp với bộ lọc hiện tại.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AppPagination
        v-if="filteredProducts.length > 0"
        v-model="productCurrentPage"
        class="mt-4"
        :total-items="filteredProducts.length"
        :page-size="productFilters.pageSize"
      />
    </div>
  </div>
</template>
