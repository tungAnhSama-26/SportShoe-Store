<script setup>
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

const emit = defineEmits(["select-invoice"]);
</script>

<template>
  <section class="mb-6 rounded-[28px] border border-white/70 bg-white/90 p-5 shadow-[0_24px_60px_rgba(15,23,42,0.08)] backdrop-blur">
    <div class="mb-4 flex items-center justify-between">
      <div>
        <h2 class="text-lg font-bold text-slate-900">Hóa đơn chờ</h2>
        <p class="text-sm text-slate-500">Chọn nhanh để xem lại hóa đơn đang chờ xử lý.</p>
      </div>
      <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
        {{ loadingPendingInvoices ? "Đang tải..." : `${pendingInvoices.length}/${maxPendingInvoices} hóa đơn` }}
      </span>
    </div>

    <p v-if="pendingInvoiceLimitReached" class="mb-4 text-xs font-medium text-amber-600">
      Đã đạt giới hạn tối đa 5 hóa đơn chờ.
    </p>

    <div class="flex flex-wrap gap-3">
      <button
        v-for="invoice in pendingInvoices"
        :key="invoice.id"
        type="button"
        class="min-w-[220px] rounded-2xl border px-4 py-3 text-left transition"
        :class="
          activePendingInvoice?.id === invoice.id
            ? 'border-red-500 bg-red-50 shadow-[0_16px_30px_rgba(239,68,68,0.15)]'
            : 'border-slate-200 bg-slate-50 hover:border-red-200 hover:bg-white'
        "
        @click="emit('select-invoice', invoice)"
      >
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-sm font-bold text-slate-900">{{ invoice.ma }}</p>
            <p class="mt-1 text-sm text-slate-600">{{ invoice.tenKhachHang }}</p>
          </div>
          <span class="rounded-full bg-white px-2 py-1 text-xs font-semibold text-slate-500">
            {{ invoice.tongSanPham }} SP
          </span>
        </div>
        <p class="mt-3 text-sm font-semibold text-red-500">{{ dinhDangTien(invoice.tongTien) }}</p>
      </button>

      <div
        v-if="!loadingPendingInvoices && !pendingInvoices.length"
        class="rounded-2xl border border-dashed border-slate-200 px-4 py-6 text-sm text-slate-500"
      >
        Chưa có hóa đơn chờ nào.
      </div>
    </div>
  </section>
</template>
