<script setup>
import { Plus } from "lucide-vue-next";
defineProps({
  pendingInvoices: {
    type: Array,
    default: () => []
  },
  loadingPendingInvoices: {
    type: Boolean,
    default: false
  },
  maxPendingInvoices: {
    type: Number,
    default: 5
  },
  pendingInvoiceLimitReached: {
    type: Boolean,
    default: false
  },
  activePendingInvoice: {
    type: Object,
    default: null
  },
  dinhDangTien: {
    type: Function,
    required: true
  }
});

const emit = defineEmits(["select-invoice", "create-empty-invoice"]);
</script>

<template>
  <section class="shrink-0 rounded-[20px] border border-white/70 dark:border-slate-700/60 bg-white/90 dark:bg-slate-800/90 p-3 shadow-sm backdrop-blur">
    <div class="mb-2 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <button
          type="button"
          class="flex h-9 items-center justify-center gap-2 rounded-md border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 px-4 text-slate-700 dark:text-slate-300 transition hover:border-red-300 dark:hover:border-red-400 hover:text-red-500 dark:hover:text-red-400 disabled:cursor-not-allowed disabled:bg-slate-100 dark:disabled:bg-slate-900/50 disabled:text-slate-400 dark:disabled:text-slate-500 disabled:border-slate-200 dark:disabled:border-slate-700/50 disabled:hover:text-slate-400 dark:disabled:hover:text-slate-500"
          title="Thêm hóa đơn chờ"
          :disabled="pendingInvoiceLimitReached"
          @click="emit('create-empty-invoice')"
        >
          <Plus class="h-5 w-5" />
          <span class="text-sm font-semibold">Thêm hóa đơn chờ</span>
        </button>
      </div>
      <span class="rounded-full bg-slate-100 dark:bg-slate-700/50 px-3 py-1 text-xs font-semibold text-slate-600 dark:text-slate-300">
        {{ loadingPendingInvoices ? "Đang tải..." : `${pendingInvoices.length}/${maxPendingInvoices} hóa đơn` }}
      </span>
    </div>

    <p v-if="pendingInvoiceLimitReached" class="mb-4 text-xs font-medium text-amber-600">
      Đã đạt giới hạn tối đa {{ maxPendingInvoices }} hóa đơn chờ.
    </p>

    <div class="flex w-full gap-3 overflow-x-auto pb-2 snap-x">
      <button
        v-for="invoice in pendingInvoices"
        :key="invoice.id"
        type="button"
        class="min-w-[200px] max-w-[300px] flex-1 shrink-0 snap-start rounded-md border px-4 py-3 text-left transition"
        :class="
          activePendingInvoice?.id === invoice.id
            ? 'border-red-500 bg-red-50 dark:border-red-500/50 dark:bg-red-900/20 shadow-[0_16px_30px_rgba(239,68,68,0.15)] dark:shadow-none'
            : 'border-slate-200 dark:border-slate-700/60 bg-slate-50 dark:bg-slate-800/50 hover:border-red-200 dark:hover:border-red-500/30 hover:bg-white dark:hover:bg-slate-800'
        "
        @click="emit('select-invoice', invoice)"
      >
        <div class="flex items-center justify-between gap-3">
          <p class="text-sm font-bold text-slate-900 dark:text-slate-100">{{ invoice.ma }}</p>
        </div>
      </button>

      <div
        v-if="!loadingPendingInvoices && !pendingInvoices.length"
        class="rounded-md border border-dashed border-slate-200 dark:border-slate-700 px-4 py-6 text-sm text-slate-500 dark:text-slate-400"
      >
        Chưa có hóa đơn chờ nào.
      </div>
    </div>
  </section>
</template>
