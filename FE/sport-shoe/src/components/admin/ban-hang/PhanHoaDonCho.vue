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
  <section class="shrink-0 rounded-[20px] border border-white/70 bg-white/90 p-3 shadow-[0_24px_60px_rgba(15,23,42,0.08)] backdrop-blur">
    <div class="mb-2 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <button
          type="button"
          class="flex h-9 items-center justify-center gap-2 rounded-md border border-slate-200 bg-white px-4 text-slate-700 transition hover:border-red-300 hover:text-red-500 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400 disabled:border-slate-200 disabled:hover:text-slate-400"
          title="Thêm hóa đơn chờ"
          :disabled="pendingInvoiceLimitReached"
          @click="emit('create-empty-invoice')"
        >
          <Plus class="h-5 w-5" />
          <span class="text-sm font-semibold">Thêm hóa đơn chờ</span>
        </button>
      </div>
      <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
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
            ? 'border-red-500 bg-red-50 shadow-[0_16px_30px_rgba(239,68,68,0.15)]'
            : 'border-slate-200 bg-slate-50 hover:border-red-200 hover:bg-white'
        "
        @click="emit('select-invoice', invoice)"
      >
        <div class="flex items-center justify-between gap-3">
          <p class="text-sm font-bold text-slate-900">{{ invoice.ma }}</p>
        </div>
      </button>

      <div
        v-if="!loadingPendingInvoices && !pendingInvoices.length"
        class="rounded-md border border-dashed border-slate-200 px-4 py-6 text-sm text-slate-500"
      >
        Chưa có hóa đơn chờ nào.
      </div>
    </div>
  </section>
</template>
