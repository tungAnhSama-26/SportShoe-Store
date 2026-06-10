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
  Users,
  CreditCard
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
import Card from "../../../components/ui/Card.vue";
import Button from "../../../components/ui/Button.vue";
import AppPagination from "../../../components/common/AppPagination.vue";
import { layDashboardThongKe } from "../../../services/thong-ke";
import { getDisplayErrorMessage } from "../../../utils/error-message";

export function useThongKeDashboard() {
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
  const brandChartType = ref("REVENUE"); // REVENUE, VOLUME, STOCK
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

  const averageOrderValue = computed(() => {
    const revenue = Number(dashboard.value.tongQuan.tongDoanhThu || 0);
    const orders = Number(dashboard.value.tongQuan.tongDonHang || 0);
    return orders > 0 ? (revenue / orders) : 0;
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
      label: "Giá trị trung bình đơn (AOV)",
      value: formatCurrency(averageOrderValue.value),
      icon: CreditCard,
      iconClass: "text-indigo-600",
      badgeClass: "bg-indigo-50"
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
      iconClass: "text-primary",
      badgeClass: "bg-primary-light"
    }
  ]);

  const salesLabels = computed(() => dashboard.value.bieuDoBanHang.map((item) => item.nhan));

  const salesChartData = computed(() => ({
    labels: salesLabels.value,
    datasets: [
      {
        label: "Sản phẩm bán được",
        data: dashboard.value.bieuDoBanHang.map((item) => item.soLuongBan ?? 0),
        backgroundColor: "#cf1018",
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

  const brandSalesData = computed(() => {
    const map = new Map();

    dashboard.value.sanPhams.forEach((product) => {
      const brandName = product.thuongHieu || "Chưa cập nhật";
      const current = map.get(brandName) || { revenue: 0, volume: 0 };
      current.revenue += Number(product.doanhThu || 0);
      current.volume += Number(product.daBan || 0);
      map.set(brandName, current);
    });

    return Array.from(map.entries()).map(([tenThuongHieu, data]) => ({
      tenThuongHieu,
      revenue: data.revenue,
      volume: data.volume
    }));
  });

  const currentBrandData = computed(() => {
    if (brandChartType.value === "STOCK") {
      return dashboard.value.bieuDoThuongHieu.map((item) => ({
        label: item.tenThuongHieu,
        value: item.tongTonKho ?? 0,
        displayValue: `${formatNumber(item.tongTonKho ?? 0)} sản phẩm tồn`
      }));
    }

    const sales = brandSalesData.value;
    if (brandChartType.value === "REVENUE") {
      return [...sales]
        .sort((a, b) => b.revenue - a.revenue)
        .map((item) => ({
          label: item.tenThuongHieu,
          value: item.revenue,
          displayValue: formatCurrency(item.revenue)
        }));
    } else {
      // VOLUME
      return [...sales]
        .sort((a, b) => b.volume - a.volume)
        .map((item) => ({
          label: item.tenThuongHieu,
          value: item.volume,
          displayValue: `${formatNumber(item.volume)} sản phẩm đã bán`
        }));
    }
  });

  const brandChartData = computed(() => ({
    labels: currentBrandData.value.map((item) => item.label),
    datasets: [
      {
        data: currentBrandData.value.map((item) => item.value),
        backgroundColor: currentBrandData.value.map((_, index) => PIE_COLORS[index % PIE_COLORS.length]),
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
            const item = currentBrandData.value[context.dataIndex];
            return `${context.label}: ${item ? item.displayValue : formatNumber(context.raw)}`;
          }
        }
      }
    }
  }));

  const topBrands = computed(() => currentBrandData.value.slice(0, 5));
  const hasSalesData = computed(() => dashboard.value.bieuDoBanHang.some((item) => (item.soLuongBan ?? 0) > 0));
  const hasBrandData = computed(() => currentBrandData.value.length > 0);
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
      errorMessage.value = getDisplayErrorMessage(error, "Không thể tải dữ liệu thống kê.");
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

  function setQuickPeriod(type) {
    const today = new Date();
    let fromDate = new Date();
    let toDate = new Date();
    let periodType = "DAY";

    switch (type) {
      case "today":
        fromDate = today;
        toDate = today;
        periodType = "DAY";
        break;
      case "last7days":
        fromDate.setDate(today.getDate() - 6);
        toDate = today;
        periodType = "DAY";
        break;
      case "thisMonth":
        fromDate.setDate(1);
        toDate = today;
        periodType = "DAY";
        break;
      case "lastMonth":
        fromDate = new Date(today.getFullYear(), today.getMonth() - 1, 1);
        toDate = new Date(today.getFullYear(), today.getMonth(), 0);
        periodType = "DAY";
        break;
      case "thisYear":
        fromDate = new Date(today.getFullYear(), 0, 1);
        toDate = today;
        periodType = "MONTH";
        break;
      default:
        return;
    }

    filters.periodType = periodType;
    filters.fromDate = formatDateForInput(fromDate);
    filters.toDate = formatDateForInput(toDate);
    fetchDashboard();
  }

  onMounted(() => {
    fetchDashboard();
  });

  onBeforeUnmount(() => {
    if (dashboardFilterTimer) {
      window.clearTimeout(dashboardFilterTimer);
    }
    dashboardRequestController?.abort();
  });

  return { computed, onBeforeUnmount, onMounted, reactive, ref, watch, BarChart3, Calendar, Filter, Package, PieChart, RefreshCw, Search, ShoppingCart, Store, TrendingUp, Users, CreditCard, ArcElement, BarElement, CategoryScale, ChartJS, Legend, LinearScale, Tooltip, Bar, Pie, Card, Button, AppPagination, layDashboardThongKe, getDisplayErrorMessage, PERIOD_OPTIONS, PIE_COLORS, PRODUCT_STOCK_OPTIONS, PRODUCT_SORT_OPTIONS, EMPLOYEE_SORT_OPTIONS, PRODUCT_PAGE_SIZE_OPTIONS, EMPTY_DASHBOARD, dashboard, isLoading, errorMessage, filters, fromDatePickerRef, toDatePickerRef, productFilters, productCurrentPage, employeeFilters, employeeCurrentPage, brandChartType, setQuickPeriod, averageOrderValue, dashboardFilterTimer, dashboardRequestController, latestDashboardRequestId, periodLabel, summaryCards, salesLabels, salesChartData, salesChartOptions, brandChartData, brandChartOptions, topBrands, hasSalesData, hasBrandData, filteredProducts, productTotalPages, paginatedProducts, productCountLabel, filteredEmployees, employeeTotalPages, paginatedEmployees, employeeCountLabel, fetchDashboard, onApplyFilters, onResetFilters, onPeriodTypeChange, openDatePicker, handleDateChange, syncFiltersFromServer, normalizeDashboard, createDefaultFilters, createDefaultProductFilters, createDefaultEmployeeFilters, resetProductFilters, resetEmployeeFilters, scheduleDashboardFetch, resolveDefaultFromDate, formatDateForDisplay, formatDateForInput, formatCurrency, formatNumber, shouldShowSalesTick, sortProducts, sortEmployees, rowBadgeClass };
}

