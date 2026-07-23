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
  }
})

const emit = defineEmits(['reset-filters', 'export-excel', 'go-to-form', 'load-data'])

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
      <div class="flex h-11 w-11 items-center justify-center rounded-md bg-slate-100 text-slate-600">
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
          <div class="relative w-full">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="filters.tuKhoa"
              type="text"
              placeholder="Tìm theo mã SP / tên sản phẩm..."
              class="admin-field h-11 w-full rounded-md border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              
            />
          </div>
        </label>

        <div class="flex flex-wrap items-center gap-3 xl:justify-end">
          <button type="button" class="admin-btn-soft" @click="$emit('reset-filters')">
            <RotateCcw class="h-4 w-4" />
            Đặt lại bộ lọc
          </button>
          <button type="button" class="admin-btn-soft" @click="$emit('export-excel')">
            <FileSpreadsheet class="h-4 w-4" />
            Xuất Excel
          </button>
          <button type="button" class="admin-btn-primary" @click="$emit('go-to-form')">
            <Plus class="h-4 w-4" />
            Thêm sản phẩm
          </button>
        </div>
      </div>

      <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <label class="space-y-2">
          <span class="admin-filter-label mb-1">Thương hiệu</span>
          <select
            v-model.number="filters.thuongHieuId"
            class="admin-field h-11 w-full rounded-md border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            @change="handleFilterChange"
          >
            <option :value="null">Tất cả thương hiệu</option>
            <option v-for="item in danhMuc?.thuongHieu || []" :key="item.id" :value="item.id">
              {{ item.ten }}
            </option>
          </select>
        </label>

        <label class="space-y-2">
          <span class="admin-filter-label mb-1">Loại giày</span>
          <select
            v-model.number="filters.loaiGiayId"
            class="admin-field h-11 w-full rounded-md border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            @change="handleFilterChange"
          >
            <option :value="null">Tất cả loại giày</option>
            <option v-for="item in danhMuc?.loaiGiay || []" :key="item.id" :value="item.id">
              {{ item.ten }}
            </option>
          </select>
        </label>
        <label class="space-y-2">
          <span class="admin-filter-label mb-1">Trạng thái</span>
          <select
            v-model.number="filters.trangThai"
            class="admin-field h-11 w-full rounded-md border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            @change="handleFilterChange"
          >
            <option :value="null">Tất cả trạng thái</option>
            <option :value="0">Ngừng bán</option>
            <option :value="1">Kinh doanh</option>
            <option :value="2">Hết hàng</option>
          </select>
        </label>
      </div>
    </div>
  </section>
</template>