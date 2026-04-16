<script setup>
import { ChevronLeft, ChevronRight, Filter, Layers3, Plus, Search } from "lucide-vue-next";

const props = defineProps({
  addLabel: {
    type: String,
    default: "Thêm mới",
  },
  currentPage: {
    type: Number,
    default: 0,
  },
  keyword: {
    type: String,
    default: "",
  },
  pageSize: {
    type: Number,
    default: 10,
  },
  pageSizeOptions: {
    type: Array,
    default: () => [10, 20, 50],
  },
  listTitle: {
    type: String,
    default: "Danh sách",
  },
  searchPlaceholder: {
    type: String,
    default: "Tìm kiếm...",
  },
  title: {
    type: String,
    required: true,
  },
  toast: {
    type: Object,
    default: null,
  },
  totalItems: {
    type: Number,
    default: 0,
  },
  totalPages: {
    type: Number,
    default: 0,
  },
  visiblePages: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["add", "change-page-size", "go-page", "search", "update:keyword"]);

function onKeywordInput(event) {
  emit("update:keyword", event.target.value);
}

function onPageSizeChange(event) {
  emit("change-page-size", Number(event.target.value));
}
</script>

<template>
  <div class="space-y-5">
    <Transition name="fade">
      <div
        v-if="toast?.show"
        class="fixed right-4 top-[88px] z-50 rounded-2xl px-4 py-3 text-sm font-medium text-white shadow-lg"
        :class="toast.type === 'success' ? 'bg-emerald-500' : 'bg-rose-500'"
      >
        {{ toast.message }}
      </div>
    </Transition>

    <section class="flex items-center justify-between gap-3">
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">{{ title }}</h1>
      <button
        type="button"
        @click="emit('add')"
        class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-semibold text-white transition hover:bg-rose-600"
      >
        <Plus class="h-4 w-4" />
        {{ addLabel }}
      </button>
    </section>

    <section class="admin-section-card">
      <div class="admin-section-header">
        <div class="admin-section-icon admin-section-icon--slate">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
        </div>
      </div>

      <div class="flex flex-col gap-3 sm:flex-row">
        <div class="relative flex-1">
          <Search class="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            :value="keyword"
            type="text"
            :placeholder="searchPlaceholder"
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            @input="onKeywordInput"
            @keyup.enter="emit('search')"
          />
        </div>

        <label
          class="inline-flex h-11 items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-600"
        >
          <span>Hiển thị</span>
          <select
            :value="pageSize"
            class="bg-transparent font-medium text-slate-700 outline-none"
            @change="onPageSizeChange"
          >
            <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}/trang</option>
          </select>
        </label>

        <button
          type="button"
          @click="emit('search')"
          class="inline-flex h-11 items-center justify-center rounded-2xl bg-rose-500 px-5 text-sm font-medium text-white transition hover:bg-rose-600"
        >
          Tìm kiếm
        </button>
      </div>
    </section>

    <section class="admin-section-card">
      <div class="admin-section-header">
        <div class="admin-section-icon admin-section-icon--violet">
          <Layers3 class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">{{ listTitle }}</h2>
        </div>
      </div>

      <div class="overflow-x-auto">
        <slot name="table" />
      </div>

      <div
        v-if="totalPages > 0"
        class="mt-5 flex items-center justify-between gap-3 text-sm text-slate-500"
      >
        <span>{{ totalItems }} bản ghi</span>

        <div class="flex items-center gap-1">
          <button
            type="button"
            :disabled="currentPage === 0"
            class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200 disabled:opacity-40"
            @click="emit('go-page', currentPage - 1)"
          >
            <ChevronLeft class="h-4 w-4" />
          </button>

          <button
            v-for="page in visiblePages"
            :key="page"
            type="button"
            class="flex h-8 min-w-8 items-center justify-center rounded-lg px-2 text-xs font-medium transition"
            :class="page === currentPage ? 'bg-rose-500 text-white' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
            @click="emit('go-page', page)"
          >
            {{ page + 1 }}
          </button>

          <button
            type="button"
            :disabled="currentPage >= totalPages - 1"
            class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200 disabled:opacity-40"
            @click="emit('go-page', currentPage + 1)"
          >
            <ChevronRight class="h-4 w-4" />
          </button>
        </div>
      </div>
    </section>

    <slot name="modal" />
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
