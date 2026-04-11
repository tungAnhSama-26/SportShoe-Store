<script setup>
import { computed } from "vue";

const props = defineProps({
  modelValue: {
    type: Number,
    required: true,
  },
  totalItems: {
    type: Number,
    required: true,
  },
  pageSize: {
    type: Number,
    required: true,
  },
  siblingCount: {
    type: Number,
    default: 1,
  },
});

const emit = defineEmits(["update:modelValue"]);

const totalPages = computed(() => Math.max(1, Math.ceil(props.totalItems / props.pageSize)));
const currentPage = computed(() => Math.min(Math.max(props.modelValue, 1), totalPages.value));
const startItem = computed(() => (props.totalItems === 0 ? 0 : (currentPage.value - 1) * props.pageSize + 1));
const endItem = computed(() => Math.min(currentPage.value * props.pageSize, props.totalItems));

const pageItems = computed(() => {
  const pages = totalPages.value;
  const page = currentPage.value;
  const siblingCount = props.siblingCount;

  if (pages <= 5 + siblingCount * 2) {
    return Array.from({ length: pages }, (_, index) => index + 1);
  }

  const items = [1];
  const left = Math.max(2, page - siblingCount);
  const right = Math.min(pages - 1, page + siblingCount);

  if (left > 2) {
    items.push("ellipsis");
  }

  for (let value = left; value <= right; value += 1) {
    items.push(value);
  }

  if (right < pages - 1) {
    items.push("ellipsis");
  }

  items.push(pages);
  return items;
});

const changePage = (page) => {
  const nextPage = Math.min(Math.max(page, 1), totalPages.value);
  if (nextPage !== currentPage.value) {
    emit("update:modelValue", nextPage);
  }
};

const itemKey = (item, index) => `${item}-${index}`;
</script>

<template>
  <div class="flex flex-col gap-4 px-2 py-1 md:flex-row md:items-center md:justify-between">
    <span class="text-[13px] font-medium text-gray-500">
      Đang hiển thị {{ startItem }} tới {{ endItem }} trong số {{ totalItems }} kết quả
    </span>

    <div class="flex items-center gap-1.5">
      <button
        class="flex h-8 w-8 items-center justify-center rounded-[8px] border border-gray-200 text-gray-400 transition-colors hover:bg-gray-50 hover:text-gray-600 disabled:cursor-not-allowed disabled:opacity-50"
        :disabled="currentPage === 1"
        @click="changePage(currentPage - 1)"
      >
        <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m15 18-6-6 6-6" />
        </svg>
      </button>

      <template v-for="(item, index) in pageItems" :key="itemKey(item, index)">
        <span v-if="item === 'ellipsis'" class="mx-0.5 text-gray-400">...</span>
        <button
          v-else
          class="flex h-8 w-8 items-center justify-center rounded-[8px] border border-transparent text-[13px] font-semibold transition-colors"
          :class="item === currentPage ? 'bg-[#ff5a5f] text-white shadow-sm shadow-red-200' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'"
          @click="changePage(item)"
        >
          {{ item }}
        </button>
      </template>

      <button
        class="flex h-8 w-8 items-center justify-center rounded-[8px] border border-gray-200 text-gray-600 transition-colors hover:bg-gray-50 hover:text-gray-900 disabled:cursor-not-allowed disabled:opacity-50"
        :disabled="currentPage === totalPages"
        @click="changePage(currentPage + 1)"
      >
        <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m9 18 6-6-6-6" />
        </svg>
      </button>
    </div>
  </div>
</template>
