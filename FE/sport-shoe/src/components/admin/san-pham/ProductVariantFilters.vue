<script setup>
import { RotateCcw, FileSpreadsheet, Plus, Search, Filter, QrCode } from 'lucide-vue-next'

const props = defineProps({
  filters: {
    type: Object,
    required: true
  },
  danhMuc: {
    type: Object,
    default: null
  },
  selectedProduct: {
    type: Object,
    default: null
  },
  hasSelectedVariants: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['reset-filters', 'export-excel', 'go-to-form', 'load-data', 'open-scanner', 'download-qr'])

function handleKeywordEnter() {
  emit('load-data', 0)
}

function handleFilterChange() {
  emit('load-data', 0)
}
</script>

<template>
  <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">

    <div class="flex flex-col gap-4">
      <!-- Màn hình quản lý sản phẩm -> Xem chi tiết biến thể -> Tiêu đề (Yêu cầu 1) -->
      <div v-if="selectedProduct" class="mb-2 flex items-center justify-between border-b border-slate-100 pb-4">
        <div>
          <h2 class="text-lg font-bold text-slate-800">Biến thể của: <span class="text-rose-600">{{ selectedProduct.ten }}</span></h2>
          <p class="mt-1 text-sm text-slate-500">Mã sản phẩm: <span class="font-semibold text-slate-700">{{ selectedProduct.ma }}</span></p>
        </div>
      </div>

      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <label class="min-w-0 flex-1 space-y-2">
          <span class="admin-filter-label mb-1">Tìm kiếm</span>
          <div class="relative max-w-3xl">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="filters.keyword"
              type="text"
              placeholder="Tìm theo mã SP / mã CTSP / tên sản phẩm..."
              class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-12 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              @keyup.enter="handleKeywordEnter"
            />
            <button
              type="button"
              class="absolute right-2 top-1/2 -translate-y-1/2 rounded-xl p-1.5 text-slate-400 transition hover:bg-slate-200 hover:text-slate-600"
              title="Quét QR / Mã vạch"
              @click="$emit('open-scanner')"
            >
              <QrCode class="h-4 w-4" />
            </button>
          </div>
        </label>

        <div class="flex flex-wrap items-center gap-3 xl:justify-end">
          <button type="button" class="admin-btn-soft" @click="$emit('reset-filters')">
            <RotateCcw class="h-4 w-4" />
            Đặt lại
          </button>
          <button type="button" class="admin-btn-soft" @click="$emit('export-excel')">
            <FileSpreadsheet class="h-4 w-4" />
            Xuất Excel
          </button>
          <button v-if="hasSelectedVariants" type="button" class="admin-btn-primary" @click="$emit('download-qr')">
            <QrCode class="h-4 w-4" />
            Tải mã QR
          </button>
        </div>
      </div>

      <div class="grid gap-4 md:grid-cols-2 xl:max-w-5xl xl:grid-cols-3">
        <label class="space-y-2">
          <span class="admin-filter-label mb-1">Màu sắc</span>
          <select
            v-model.number="filters.mauSacId"
            class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            @change="handleFilterChange"
          >
            <option :value="null">Tất cả màu sắc</option>
            <option v-for="item in danhMuc?.mauSac || []" :key="item.id" :value="item.id">
              {{ item.ten }}
            </option>
          </select>
        </label>

        <label class="space-y-2">
          <span class="admin-filter-label mb-1">Kích cỡ</span>
          <select
            v-model.number="filters.kichCoId"
            class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            @change="handleFilterChange"
          >
            <option :value="null">Tất cả kích cỡ</option>
            <option v-for="item in danhMuc?.kichCo || []" :key="item.id" :value="item.id">
              {{ item.giaTri }}
            </option>
          </select>
        </label>
        <label class="space-y-2">
          <span class="admin-filter-label mb-1">Trạng thái</span>
          <select
            v-model.number="filters.trangThai"
            class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            @change="handleFilterChange"
          >
            <option :value="null">Tất cả trạng thái</option>
            <option :value="1">Đang bán</option>
            <option :value="2">Ngừng bán</option>
          </select>
        </label>
      </div>
    </div>
  </section>
</template>