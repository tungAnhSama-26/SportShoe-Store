<script setup>
import { computed } from "vue";
import { ChevronFirst, ChevronLast, ChevronLeft, ChevronRight, RefreshCw } from "lucide-vue-next";

const props = defineProps({
  currentPage: {
    type: Number,
    required: true,
  },
  pageSize: {
    type: Number,
    required: true,
  },
  pageSizeOptions: {
    type: Array,
    default: () => [5, 10, 20, 50],
  },
  totalItems: {
    type: Number,
    default: null,
  },
  totalPages: {
    type: Number,
    required: true,
  },
  zeroBased: {
    type: Boolean,
    default: false,
  },
  compact: {
    type: Boolean,
    default: false,
  },
  showRefresh: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["refresh", "update:currentPage", "update:pageSize"]);

const normalizedTotalPages = computed(() => Math.max(0, Number(props.totalPages) || 0));
const displayPage = computed(() => (props.zeroBased ? props.currentPage + 1 : props.currentPage));
const hasResults = computed(() => {
  if (props.totalItems === null || props.totalItems === undefined) {
    return normalizedTotalPages.value > 0;
  }
  return props.totalItems > 0;
});

const pageItems = computed(() => {
  const total = normalizedTotalPages.value;
  const current = Math.min(Math.max(displayPage.value, 1), Math.max(total, 1));

  if (!total) return [];
  if (total <= 5) return Array.from({ length: total }, (_, i) => i + 1);

  const items = [];

  // Luôn hiện 5 trang xung quanh current
  let start = Math.max(1, current - 2);
  let end = Math.min(total, start + 4);
  if (end - start < 4) start = Math.max(1, end - 4);

  if (start > 1) {
    items.push(1);
    if (start > 2) items.push('ellipsis-left');
  }

  for (let p = start; p <= end; p++) items.push(p);

  if (end < total) items.push('ellipsis-right');

  return items;
});

function changePage(nextPage) {
  const total = normalizedTotalPages.value;
  if (!total) return;
  const boundedPage = Math.min(Math.max(nextPage, 1), total);
  emit("update:currentPage", props.zeroBased ? boundedPage - 1 : boundedPage);
}

function handlePageSizeChange(event) {
  emit("update:pageSize", Number(event.target.value));
}

function itemKey(item, index) {
  return `${item}-${index}`;
}
</script>

<template>
  <div
    class="mt-5 border-t border-slate-100 pt-4"
    :class="props.compact ? 'flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between' : 'flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between'"
  >
    <label
      class="inline-flex w-fit items-center border border-slate-200 bg-white text-sm font-medium text-slate-600 shadow-sm"
      :class="props.compact ? 'h-9 rounded-2xl px-3' : 'h-11 gap-3 rounded-2xl px-4'"
    >
      <span v-if="!props.compact">Hiển thị:</span>
      <select
        :value="pageSize"
        class="bg-transparent font-semibold text-slate-700 outline-none"
        :class="props.compact ? 'min-w-[2.25rem] pr-1 text-center' : ''"
        @change="handlePageSizeChange"
      >
        <option
          v-for="size in pageSizeOptions"
          :key="size"
          :value="size"
        >
          {{ props.compact ? size : `${size}/trang` }}
        </option>
      </select>
    </label>

    <div
      v-if="hasResults"
      class="flex flex-wrap items-center justify-end gap-2"
    >
      <button
        v-if="showRefresh"
        type="button"
        class="inline-flex items-center justify-center border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700"
        :class="props.compact ? 'h-9 w-9 rounded-2xl' : 'h-9 w-9 rounded-xl'"
        title="Làm mới"
        @click="emit('refresh')"
      >
        <RefreshCw class="h-4 w-4" />
      </button>

      <div
        class="flex items-center"
        :class="props.compact ? 'gap-1.5' : 'gap-1.5'"
      >
        <!-- Trang đầu -->
        <button
          type="button"
          class="flex h-9 w-9 items-center justify-center border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          :class="props.compact ? 'rounded-2xl' : 'rounded-xl'"
          :disabled="displayPage <= 1"
          title="Trang đầu"
          @click="changePage(1)"
        >
          <ChevronFirst class="h-4 w-4" />
        </button>

        <!-- Trang trước -->
        <button
          type="button"
          class="flex h-9 w-9 items-center justify-center border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          :class="props.compact ? 'rounded-2xl' : 'rounded-xl'"
          :disabled="displayPage <= 1"
          @click="changePage(displayPage - 1)"
        >
          <ChevronLeft class="h-4 w-4" />
        </button>

        <template v-if="props.compact">
          <template v-for="(item, index) in pageItems" :key="itemKey(item, index)">
            <button
              v-if="String(item).startsWith('ellipsis')"
              type="button"
              class="flex h-9 min-w-9 items-center justify-center rounded-2xl border border-slate-200 bg-white px-3 text-sm text-slate-400 transition hover:bg-slate-50 hover:text-slate-600"
              @click="changePage(item === 'ellipsis-left' ? displayPage - 5 : displayPage + 5)"
            >...</button>
            <button
              v-else
              type="button"
              class="flex h-9 min-w-9 items-center justify-center rounded-2xl px-3 text-sm font-semibold transition"
              :class="Number(item) === displayPage ? 'border border-violet-200 bg-violet-50 text-violet-600' : 'border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 hover:text-slate-800'"
              @click="changePage(Number(item))"
            >{{ item }}</button>
          </template>
        </template>
        <template v-else>
          <template v-for="(item, index) in pageItems" :key="itemKey(item, index)">
            <button
              v-if="String(item).startsWith('ellipsis')"
              type="button"
              class="flex h-9 min-w-9 items-center justify-center rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-400 transition hover:bg-slate-50 hover:text-slate-600"
              @click="changePage(item === 'ellipsis-left' ? displayPage - 5 : displayPage + 5)"
            >...</button>
            <button
              v-else
              type="button"
              class="flex h-9 min-w-9 items-center justify-center rounded-xl px-3 text-sm font-semibold transition"
              :class="Number(item) === displayPage ? 'bg-rose-500 text-white shadow-sm' : 'border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 hover:text-slate-800'"
              @click="changePage(Number(item))"
            >{{ item }}</button>
          </template>
        </template>

        <!-- Trang sau -->
        <button
          type="button"
          class="flex h-9 w-9 items-center justify-center border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          :class="props.compact ? 'rounded-2xl' : 'rounded-xl'"
          :disabled="displayPage >= normalizedTotalPages"
          @click="changePage(displayPage + 1)"
        >
          <ChevronRight class="h-4 w-4" />
        </button>

        <!-- Trang cuối -->
        <button
          type="button"
          class="flex h-9 w-9 items-center justify-center border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          :class="props.compact ? 'rounded-2xl' : 'rounded-xl'"
          :disabled="displayPage >= normalizedTotalPages"
          title="Trang cuối"
          @click="changePage(normalizedTotalPages)"
        >
          <ChevronLast class="h-4 w-4" />
        </button>
      </div>
    </div>
  </div>
</template>
