<script setup>
import { RotateCcw, FileSpreadsheet, Plus, Search, Filter } from 'lucide-vue-next'

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
  }
})

const emit = defineEmits(['reset-filters', 'export-excel', 'go-to-form', 'load-data', 'clear-product-filter'])

function handleKeywordEnter() {
  emit('load-data', 0)
}

function handleFilterChange() {
  emit('load-data', 0)
}
</script>

<template>
  <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
    <div class="mb-5 flex items-center gap-3">
      <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
        <Filter class="h-5 w-5" />
      </div>
      <div>
        <h2 class="admin-section-title">Bộ lọc</h2>
      </div>
    </div>

    <div class="flex flex-col gap-4">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <label class="min-w-0 flex-1 space-y-2">
          <span class="admin-filter-label mb-1">Tìm kiếm</span>
          <div class="relative max-w-3xl">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="filters.keyword"
              type="text"
              placeholder="Tìm theo mã SP / mã CTSP / tên sản phẩm..."
              class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              @keyup.enter="handleKeywordEnter"
            />
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
          <button type="button" class="admin-btn-primary" @click="$emit('go-to-form')">
            <Plus class="h-4 w-4" />
            Thêm sản phẩm chi tiết
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
            <option :value="2">Ngừng bán / Hết hàng</option>
          </select>
        </label>
      </div>
    </div>
  </section>
</template>