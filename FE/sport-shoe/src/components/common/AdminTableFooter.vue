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
  noMargin: {
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
  if (total <= 5) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  const items = [];
  
  // Show first page
  items.push(1);

  // Calculate start and end for middle pages
  // We want to show at most 3 middle pages
  let start = current - 1;
  let end = current + 1;

  if (start <= 2) {
    start = 2;
    end = 4;
  } else if (end >= total - 1) {
    start = total - 3;
    end = total - 1;
  }

  if (start > 2) {
    items.push('ellipsis-left');
  }

  for (let i = start; i <= end; i++) {
    items.push(i);
  }

  if (end < total - 1) {
    items.push('ellipsis-right');
  }

  // Show last page
  items.push(total);

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
    :class="[
      props.noMargin ? 'border-t border-slate-100 px-6 py-3' : 'mt-5 border-t border-slate-100 pt-4',
      props.compact ? 'flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between' : 'flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between'
    ]"
  >
    <label
      class="inline-flex w-fit items-center border border-slate-200 bg-white text-sm font-medium text-slate-600 shadow-sm"
      :class="props.compact ? 'h-9 rounded-[6px] px-3' : 'h-11 gap-3 rounded-[6px] px-4'"
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


      <div
        class="flex items-center"
        :class="props.compact ? 'gap-1.5' : 'gap-1.5'"
      >
        <!-- Trang đầu -->
        <button
          type="button"
          class="flex h-9 w-9 items-center justify-center border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          :class="props.compact ? 'rounded-[6px]' : 'rounded-[6px]'"
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
          :class="props.compact ? 'rounded-[6px]' : 'rounded-[6px]'"
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
              class="flex h-9 min-w-9 items-center justify-center rounded-[6px] border border-slate-200 bg-white px-3 text-sm text-slate-400 transition hover:bg-slate-50 hover:text-slate-600"
              @click="changePage(item === 'ellipsis-left' ? displayPage - 5 : displayPage + 5)"
            >...</button>
            <button
              v-else
              type="button"
              class="flex h-9 min-w-9 items-center justify-center rounded-[6px] px-3 text-sm font-semibold transition"
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
              class="flex h-9 min-w-9 items-center justify-center rounded-[6px] border border-slate-200 bg-white px-3 text-sm text-slate-400 transition hover:bg-slate-50 hover:text-slate-600"
              @click="changePage(item === 'ellipsis-left' ? displayPage - 5 : displayPage + 5)"
            >...</button>
            <button
              v-else
              type="button"
              class="flex h-9 min-w-9 items-center justify-center rounded-[6px] px-3 text-sm font-semibold transition"
              :class="Number(item) === displayPage ? 'bg-rose-500 text-white shadow-sm' : 'border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 hover:text-slate-800'"
              @click="changePage(Number(item))"
            >{{ item }}</button>
          </template>
        </template>

        <!-- Trang sau -->
        <button
          type="button"
          class="flex h-9 w-9 items-center justify-center border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          :class="props.compact ? 'rounded-[6px]' : 'rounded-[6px]'"
          :disabled="displayPage >= normalizedTotalPages"
          @click="changePage(displayPage + 1)"
        >
          <ChevronRight class="h-4 w-4" />
        </button>

        <!-- Trang cuối -->
        <button
          type="button"
          class="flex h-9 w-9 items-center justify-center border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          :class="props.compact ? 'rounded-[6px]' : 'rounded-[6px]'"
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
