import { computed, ref, watch, type Ref } from 'vue';

export const usePagination = <T>(items: Ref<T[]> | T[], initialPageSize = 10) => {
  const currentPage = ref(1);
  const pageSize = ref(initialPageSize);

  const sourceItems = computed(() => Array.isArray(items) ? items : items.value);
  const totalItems = computed(() => sourceItems.value.length);
  const totalPages = computed(() => Math.max(1, Math.ceil(totalItems.value / pageSize.value)));

  const paginatedItems = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    return sourceItems.value.slice(start, start + pageSize.value);
  });

  watch(totalPages, (nextTotalPages) => {
    if (currentPage.value > nextTotalPages) {
      currentPage.value = nextTotalPages;
    }
  }, { immediate: true });

  const setPage = (page: number) => {
    currentPage.value = Math.min(Math.max(page, 1), totalPages.value);
  };

  const resetPage = () => {
    currentPage.value = 1;
  };

  return {
    currentPage,
    pageSize,
    totalItems,
    totalPages,
    paginatedItems,
    setPage,
    resetPage,
  };
};
