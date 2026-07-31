<script setup>
import { ref } from "vue";
import { FileSpreadsheet, Filter, Layers3, Plus, Search, RotateCcw } from "lucide-vue-next";
import AdminTableFooter from "../../common/AdminTableFooter.vue";
import Button from "../../ui/Button.vue";
import { validateSearchKeyword } from "../../../utils/thuoc-tinh-san-pham";

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

const searchError = ref("");

let timeout = null;
function onKeywordInput(event) {
  const val = event.target.value;
  const res = validateSearchKeyword(val);
  if (!res.valid) {
    searchError.value = res.error;
  } else {
    searchError.value = "";
  }
  emit("update:keyword", val);
  if (timeout) clearTimeout(timeout);
  timeout = setTimeout(() => {
    emit("search");
  }, 300);
}

function handleReset() {
  searchError.value = "";
  emit("update:keyword", "");
  emit("search");
}
</script>

<template>
  <div class="space-y-5 radius-6px">
    <Transition name="fade">
      <div
        v-if="toast?.show && toast.type !== 'success'"
        class="fixed right-4 top-[88px] z-50 rounded-md px-4 py-3 text-sm font-medium text-white shadow-lg"
        :class="toast.type === 'success' ? 'bg-[#ff6a00]' : 'bg-[#cf1018]'"
      >
        {{ toast.message }}
      </div>
    </Transition>

    <section class="admin-section-card">
      <div class="admin-section-header">
        <div class="admin-section-icon admin-section-icon--slate">
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="admin-section-title">Bộ lọc</h2>
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
              class="admin-field h-11 w-full rounded-md border bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:bg-white"
              :class="searchError ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300'"
              @input="onKeywordInput"
              @keyup.enter="emit('search')"
            />
          </div>
          <p v-if="searchError" class="mt-1 text-xs text-rose-500 font-medium">
            {{ searchError }}
          </p>
        </div>

        <div class="flex flex-wrap items-center gap-3 xl:justify-end">
          <Button variant="soft" @click="handleReset">
            <template #prefix><RotateCcw class="h-4 w-4" /></template>
            Đặt lại bộ lọc
          </Button>

          <Button v-if="showExport" variant="soft" @click="emit('export')">
            <template #prefix><FileSpreadsheet class="h-4 w-4" /></template>
            Xuất Excel
          </Button>
          <Button variant="primary" @click="emit('add')">
            <template #prefix><Plus class="h-4 w-4" /></template>
            {{ addLabel }}
          </Button>
        </div>
      </div>
    </section>

    <section class="admin-section-card">
      <div class="admin-section-header">
        <div class="admin-section-icon admin-section-icon--violet">
          <Layers3 class="h-5 w-5" />
        </div>
        <div>
          <h2 class="admin-section-title">{{ listTitle }}</h2>
        </div>
      </div>

      <div class="admin-table-scroll">
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

