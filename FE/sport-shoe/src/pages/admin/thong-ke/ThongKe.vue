<script setup>
import { useThongKeDashboard } from "./useThongKeDashboard";
import { TrendingDown } from "lucide-vue-next";
const { computed, onBeforeUnmount, onMounted, reactive, ref, watch, BarChart3, Calendar, Filter, Package, PieChart, RefreshCw, Search, ShoppingCart, Store, TrendingUp, Users, CreditCard, ArcElement, BarElement, CategoryScale, ChartJS, Legend, LinearScale, Tooltip, Bar, Pie, Card, Button, AdminTableFooter, layDashboardThongKe, getDisplayErrorMessage, PERIOD_OPTIONS, PIE_COLORS, PRODUCT_STOCK_OPTIONS, PRODUCT_SORT_OPTIONS, EMPLOYEE_SORT_OPTIONS, PRODUCT_PAGE_SIZE_OPTIONS, EMPTY_DASHBOARD, dashboard, isLoading, errorMessage, filters, fromDatePickerRef, toDatePickerRef, productFilters, productCurrentPage, employeeFilters, employeeCurrentPage, brandChartType, setQuickPeriod, averageOrderValue, dashboardFilterTimer, dashboardRequestController, latestDashboardRequestId, periodLabel, summaryCards, salesLabels, salesChartData, salesChartOptions, brandChartData, brandChartOptions, topBrands, hasSalesData, hasBrandData, filteredProducts, productTotalPages, paginatedProducts, productCountLabel, filteredEmployees, employeeTotalPages, paginatedEmployees, employeeCountLabel, fetchDashboard, onApplyFilters, onResetFilters, onPeriodTypeChange, openDatePicker, handleDateChange, syncFiltersFromServer, normalizeDashboard, createDefaultFilters, createDefaultProductFilters, createDefaultEmployeeFilters, resetProductFilters, resetEmployeeFilters, scheduleDashboardFetch, resolveDefaultFromDate, formatDateForDisplay, formatDateForInput, formatCurrency, formatNumber, shouldShowSalesTick, sortProducts, sortEmployees, rowBadgeClass, hasOrderStatusData, orderStatusChartData, orderStatusChartOptions } = useThongKeDashboard();
</script>

<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-2 md:flex-row md:items-end md:justify-between">
      <div>
        <h2 class="text-[24px] font-bold text-slate-800 dark:text-slate-100">Thống kê </h2>
      </div>
    </div>

    <Card>
      <div class="mb-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between border-b border-slate-100 pb-3">
        <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
          <Filter class="h-4 w-4 text-primary" />
          Bộ lọc thống kê
        </div>
        <div class="flex flex-wrap gap-1.5">
          <button
            type="button"
            class="rounded-xl border border-slate-200 bg-white hover:bg-slate-50 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-slate-300"
            @click="setQuickPeriod('today')"
          >
            Hôm nay
          </button>
          <button
            type="button"
            class="rounded-xl border border-slate-200 bg-white hover:bg-slate-50 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-slate-300"
            @click="setQuickPeriod('last7days')"
          >
            7 ngày qua
          </button>
          <button
            type="button"
            class="rounded-xl border border-slate-200 bg-white hover:bg-slate-50 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-slate-300"
            @click="setQuickPeriod('thisMonth')"
          >
            Tháng này
          </button>
          <button
            type="button"
            class="rounded-xl border border-slate-200 bg-white hover:bg-slate-50 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-slate-300"
            @click="setQuickPeriod('lastMonth')"
          >
            Tháng trước
          </button>
          <button
            type="button"
            class="rounded-xl border border-slate-200 bg-white hover:bg-slate-50 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:border-slate-300"
            @click="setQuickPeriod('thisYear')"
          >
            Năm nay
          </button>
        </div>
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
              class="flex h-12 w-full items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-left text-sm text-slate-700 transition hover:border-primary/50 hover:bg-white focus:outline-none focus:ring-2 focus:ring-primary/20"
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
              class="flex h-12 w-full items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-left text-sm text-slate-700 transition hover:border-primary/50 hover:bg-white focus:outline-none focus:ring-2 focus:ring-primary/20"
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
              class="h-12 w-full appearance-none rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm text-slate-700 outline-none transition focus:border-primary focus:bg-white"
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
              class="h-12 w-full appearance-none rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm text-slate-700 outline-none transition focus:border-primary focus:bg-white"
              @change="scheduleDashboardFetch"
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
              class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm text-slate-700 outline-none transition focus:border-primary focus:bg-white"
              @input="scheduleDashboardFetch"
              @keyup.enter="onApplyFilters"
            >
          </div>
        </div>

        <div class="flex gap-3 md:col-span-2 xl:col-span-1 xl:self-end">
          <button
            type="button"
            class="inline-flex h-12 w-full items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-70"
            :disabled="isLoading"
            @click="onResetFilters"
          >
            <RefreshCw class="h-4 w-4" />
            Reset
          </button>
        </div>
      </div>
    </Card>

    <div v-if="errorMessage" class="rounded-[22px] border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">
      {{ errorMessage }}
    </div>

    <div v-if="filters.fromDate && filters.toDate" class="rounded-2xl border border-slate-100 bg-white px-4 py-3 text-sm text-slate-700 flex items-center gap-2 shadow-sm">
      <span class="flex h-2 w-2 rounded-full bg-blue-500 animate-pulse"></span>
      <span>Đang hiển thị dữ liệu thống kê từ ngày <strong class="text-slate-900 font-bold">{{ formatDateForDisplay(filters.fromDate) }}</strong> đến ngày <strong class="text-slate-900 font-bold">{{ formatDateForDisplay(filters.toDate) }}</strong></span>
    </div>

    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
      <div
        v-for="card in summaryCards"
        :key="card.label"
        class="rounded-[6px] border border-slate-100 bg-white p-5 shadow-sm"
      >
        <div class="mb-4">
          <div :class="['flex h-12 w-12 items-center justify-center rounded-2xl', card.badgeClass]">
            <component :is="card.icon" :class="['h-5 w-5', card.iconClass]" />
          </div>
        </div>
        <div class="text-[24px] font-bold tracking-tight text-slate-800 break-words">
          {{ card.value }}
        </div>
        <div class="mt-1 text-xs font-medium text-slate-500 truncate" :title="card.label">
          {{ card.label }}
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[2.5fr_1fr]">
      <Card>
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
      </Card>

      <Card>
        <div class="mb-6 flex items-center justify-between gap-2">
          <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
            <PieChart class="h-4 w-4 text-rose-500" />
            Thương hiệu
          </div>
          <select
            v-model="brandChartType"
            class="h-9 rounded-xl border border-slate-200 bg-slate-50 px-3 text-xs font-semibold text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
          >
            <option value="REVENUE">Theo doanh thu</option>
            <option value="VOLUME">Theo số lượng bán</option>
            <option value="STOCK">Theo lượng tồn kho</option>
          </select>
        </div>

        <div v-if="hasBrandData" class="space-y-5">
          <div class="h-[250px]">
            <Pie :data="brandChartData" :options="brandChartOptions" />
          </div>

          <div class="space-y-3">
            <div
              v-for="(brand, index) in topBrands"
              :key="brand.label"
              class="flex items-center justify-between rounded-2xl bg-slate-50 px-4 py-3"
            >
              <div class="flex items-center gap-3 text-sm font-medium text-slate-700">
                <span
                  class="h-3 w-3 rounded-full"
                  :style="{ backgroundColor: PIE_COLORS[index % PIE_COLORS.length] }"
                />
                {{ brand.label }}
              </div>
              <span class="text-sm font-semibold text-slate-800">
                {{ brand.displayValue }}
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
      </Card>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[2.5fr_1fr]">
      <Card>
        <div class="mb-5 flex flex-col gap-2 md:flex-row md:items-end md:justify-between">
          <div>
            <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
              <Calendar class="h-4 w-4 text-emerald-500" />
              Thống kê theo chu kỳ thời gian
            </div>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="min-w-[600px] w-full border-separate border-spacing-y-3 text-center">
            <thead>
              <tr class="text-xs font-bold tracking-[0.05em] text-slate-950">
                <th class="px-4 py-2 text-center whitespace-nowrap">Thời gian</th>
                <th class="px-4 py-2 text-center whitespace-nowrap">Doanh thu gốc</th>
                <th class="px-4 py-2 text-center whitespace-nowrap">Doanh thu thực tế</th>
                <th class="px-4 py-2 text-center whitespace-nowrap">Số đơn</th>
                <th class="px-4 py-2 text-center whitespace-nowrap">AOV (Trung bình/đơn)</th>
                <th class="px-4 py-2 text-center whitespace-nowrap">Tăng trưởng</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in (dashboard.thongKeTheoThoiGian || [])"
                :key="row.kyThongKe"
                class="rounded-[20px] bg-slate-50 text-sm text-slate-700 hover:bg-slate-100/70 transition-colors"
              >
                <td class="rounded-l-[20px] px-4 py-4 font-semibold text-slate-800 text-center">
                  {{ row.kyThongKe }}
                </td>
                <td class="px-4 py-4 text-center font-semibold text-slate-700">
                  {{ formatCurrency(row.doanhThu) }}
                </td>
                <td class="px-4 py-4 text-center font-semibold text-emerald-600">
                  {{ formatCurrency(row.doanhThuThucTe) }}
                </td>
                <td class="px-4 py-4 text-center font-semibold text-slate-800">
                  {{ formatNumber(row.soDon) }}
                </td>
                <td class="px-4 py-4 text-center font-semibold text-slate-700">
                  {{ formatCurrency(row.giaTriTrungBinh) }}
                </td>
                <td class="rounded-r-[20px] px-4 py-4 text-center font-semibold">
                  <span
                    :class="[
                      'inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-semibold',
                      row.tangTruong >= 0
                        ? 'bg-emerald-50 text-emerald-600'
                        : 'bg-rose-50 text-rose-600'
                    ]"
                  >
                    <TrendingUp v-if="row.tangTruong >= 0" class="h-3.5 w-3.5" />
                    <TrendingDown v-else class="h-3.5 w-3.5" />
                    {{ row.tangTruong >= 0 ? '+' : '' }}{{ row.tangTruong }}%
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </Card>

      <Card>
        <div class="mb-6 flex items-center justify-between gap-2">
          <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
            <PieChart class="h-4 w-4 text-sky-500" />
            Cơ cấu trạng thái đơn hàng
          </div>
        </div>

        <div v-if="hasOrderStatusData" class="space-y-5">
          <div class="h-[250px]">
            <Pie :data="orderStatusChartData" :options="orderStatusChartOptions" />
          </div>

          <div class="grid grid-cols-2 gap-2 max-h-[140px] overflow-y-auto pr-1">
            <div
              v-for="(status, index) in dashboard.bieuDoTrangThaiDonHang"
              :key="status.trangThai"
              class="flex items-center justify-between rounded-xl bg-slate-50 px-3 py-2 text-xs"
            >
              <div class="flex items-center gap-2 font-medium text-slate-700 min-w-0">
                <span
                  class="h-2 w-2 rounded-full shrink-0"
                  :style="{ backgroundColor: PIE_COLORS[index % PIE_COLORS.length] }"
                />
                <span class="truncate">{{ status.trangThai }}</span>
              </div>
              <span class="font-semibold text-slate-800 ml-1">
                {{ formatNumber(status.soLuong) }}
              </span>
            </div>
          </div>
        </div>

        <div
          v-else
          class="flex h-[360px] items-center justify-center rounded-[24px] border border-dashed border-slate-200 bg-slate-50 text-sm text-slate-500"
        >
          Chưa có dữ liệu trạng thái đơn hàng phù hợp.
        </div>
      </Card>
    </div>

    <Card>
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

      <div class="mb-5 grid gap-4 xl:grid-cols-[1.4fr_1fr_120px]">
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

        <div class="space-y-2 xl:self-end">
          <Button
            variant="outline"
            class="inline-flex h-11 w-full items-center justify-center gap-2 px-4"
            @click="resetEmployeeFilters"
          >
            <RefreshCw class="h-4 w-4" />
            Reset
          </Button>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-[860px] w-full border-separate border-spacing-y-3 text-left">
          <thead>
            <tr class="text-xs font-bold uppercase tracking-[0.18em] text-slate-950">
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

      <AdminTableFooter
        v-if="filteredEmployees.length > 0"
        :current-page="employeeCurrentPage"
        :page-size="employeeFilters.pageSize"
        :page-size-options="PRODUCT_PAGE_SIZE_OPTIONS"
        :total-items="filteredEmployees.length"
        :total-pages="employeeTotalPages"
        compact
        @update:current-page="employeeCurrentPage = $event"
        @update:page-size="employeeFilters.pageSize = $event"
      />
    </Card>

    <Card>
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

      <div class="mb-5 grid gap-4 xl:grid-cols-[1.4fr_1fr_1fr_120px]">
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

        <div class="space-y-2 xl:self-end">
          <Button
            variant="outline"
            class="inline-flex h-11 w-full items-center justify-center gap-2 px-4"
            @click="resetProductFilters"
          >
            <RefreshCw class="h-4 w-4" />
            Reset
          </Button>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-[900px] w-full border-separate border-spacing-y-3 text-left">
          <thead>
            <tr class="text-xs font-bold uppercase tracking-[0.18em] text-slate-950">
              <th class="px-4 py-2">STT</th>
              <th class="px-4 py-2">Mã sản phẩm</th>
              <th class="px-4 py-2">Tên sản phẩm</th>
              <th class="px-4 py-2">Thương hiệu</th>
              <th class="px-4 py-2 text-right">Đã bán</th>
              <th class="px-4 py-2 text-right">Số lượng trả</th>
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
              <td class="px-4 py-4 text-right font-semibold text-rose-600">
                {{ formatNumber(product.soLuongTra) }}
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

      <AdminTableFooter
        v-if="filteredProducts.length > 0"
        :current-page="productCurrentPage"
        :page-size="productFilters.pageSize"
        :page-size-options="PRODUCT_PAGE_SIZE_OPTIONS"
        :total-items="filteredProducts.length"
        :total-pages="productTotalPages"
        compact
        @update:current-page="productCurrentPage = $event"
        @update:page-size="productFilters.pageSize = $event"
      />
    </Card>
  </div>
</template>
