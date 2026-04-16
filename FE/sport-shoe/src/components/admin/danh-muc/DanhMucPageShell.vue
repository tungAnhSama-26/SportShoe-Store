<script setup>
import { FileSpreadsheet, Filter, Layers3, Plus, Search } from "lucide-vue-next";
import AdminTableFooter from "../../common/AdminTableFooter.vue";

defineProps({
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
    default: 5,
  },
  pageSizeOptions: {
    type: Array,
    default: () => [5, 10, 20, 50],
  },
  listTitle: {
    type: String,
    default: "Danh sách",
  },
  searchPlaceholder: {
    type: String,
    default: "Tìm kiếm...",
  },
  showExport: {
    type: Boolean,
    default: true,
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
});

const emit = defineEmits(["add", "change-page-size", "export", "go-page", "search", "update:keyword"]);

function onKeywordInput(event) {
  emit("update:keyword", event.target.value);
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

    <section>
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">{{ title }}</h1>
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

      <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
        <div class="min-w-0 flex-1">
          <div class="relative max-w-3xl">
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
        </div>

        <div class="flex flex-wrap items-center gap-3 xl:justify-end">
          <button
            type="button"
            class="inline-flex h-11 items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 hover:text-slate-800"
            @click="emit('search')"
          >
            <Search class="h-4 w-4" />
            Tìm kiếm
          </button>
          <button
            v-if="showExport"
            type="button"
            class="inline-flex h-11 items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 hover:text-slate-800"
            @click="emit('export')"
          >
            <FileSpreadsheet class="h-4 w-4" />
            Xuất Excel
          </button>
          <button
            type="button"
            class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-semibold text-white transition hover:bg-rose-600"
            @click="emit('add')"
          >
            <Plus class="h-4 w-4" />
            {{ addLabel }}
          </button>
        </div>
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

      <AdminTableFooter
        :current-page="currentPage"
        :page-size="pageSize"
        :page-size-options="pageSizeOptions"
        :total-items="totalItems"
        :total-pages="totalPages"
        compact
        show-refresh
        zero-based
        @refresh="emit('go-page', currentPage)"
        @update:current-page="emit('go-page', $event)"
        @update:page-size="emit('change-page-size', $event)"
      />
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

