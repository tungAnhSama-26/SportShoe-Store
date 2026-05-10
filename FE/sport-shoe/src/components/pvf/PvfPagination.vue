<script setup>
import { computed } from "vue";
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-vue-next";

const props = defineProps({
  currentPage: {
    type: Number,
    default: 3,
  },
  pages: {
    type: Array,
    default: () => [1, 2, 3, 4, 5, 6],
  },
});

const emit = defineEmits(["update:currentPage"]);

const firstPage = computed(() => props.pages[0] ?? 1);
const lastPage = computed(() => props.pages.at(-1) ?? firstPage.value);

function goTo(page) {
  emit("update:currentPage", page);
}

function iconClass(disabled) {
  return [
    "pvf-pagination-button",
    disabled ? "cursor-not-allowed opacity-45" : "",
  ];
}
</script>

<template>
  <div class="flex flex-wrap items-center justify-end gap-1.5">
    <button
      type="button"
      :class="iconClass(currentPage === firstPage)"
      @click="goTo(firstPage)"
    >
      <ChevronsLeft class="h-3.5 w-3.5" />
    </button>
    <button
      type="button"
      :class="iconClass(currentPage === firstPage)"
      @click="goTo(Math.max(firstPage, currentPage - 1))"
    >
      <ChevronLeft class="h-3.5 w-3.5" />
    </button>
    <button
      v-for="page in pages"
      :key="page"
      type="button"
      class="pvf-pagination-button"
      :class="page === currentPage ? 'pvf-pagination-button--active' : ''"
      @click="goTo(page)"
    >
      {{ page }}
    </button>
    <button
      type="button"
      :class="iconClass(currentPage === lastPage)"
      @click="goTo(Math.min(lastPage, currentPage + 1))"
    >
      <ChevronRight class="h-3.5 w-3.5" />
    </button>
    <button
      type="button"
      :class="iconClass(currentPage === lastPage)"
      @click="goTo(lastPage)"
    >
      <ChevronsRight class="h-3.5 w-3.5" />
    </button>
  </div>
</template>
