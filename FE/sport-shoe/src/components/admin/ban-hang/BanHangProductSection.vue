<script setup>
import { ref } from "vue";
import { QrCode } from "lucide-vue-next";
import BanHangQrScannerModal from "./BanHangQrScannerModal.vue";

defineProps({
  productKeyword: {
    type: String,
    default: ""
  },
  loadingProducts: {
    type: Boolean,
    default: false
  },
  showProductDropdown: {
    type: Boolean,
    default: false
  },
  productResults: {
    type: Array,
    default: () => []
  },
  productSearchLabel: {
    type: String,
    default: ""
  },
  currentPage: {
    type: Number,
    default: 1
  },
  totalPages: {
    type: Number,
    default: 1
  },
  dinhDangTien: {
    type: Function,
    required: true
  },
  soLuongConLai: {
    type: Function,
    required: true
  }
});

const emit = defineEmits([
  "update:productKeyword",
  "focus-product",
  "blur-product",
  "open-product",
  "scan-product",
  "update:currentPage",
]);

const showQrScanner = ref(false);

function isDiscounted(product) {
  return Boolean(product?.coGiamGia) || Number(product?.giaBan || 0) < Number(product?.giaGoc || 0);
}

function formatDiscountPercent(product) {
  const giaGoc = Number(product?.giaGoc || 0);
  const giaBan = Number(product?.giaBan || 0);
  if (giaGoc <= 0 || giaBan >= giaGoc) return "";
  const pct = ((giaGoc - giaBan) / giaGoc) * 100;
  return pct % 1 === 0 ? `-${pct.toFixed(0)}%` : `-${pct.toFixed(1)}%`;
}

function moQuetQr() {
  showQrScanner.value = true;
}

function dongQuetQr() {
  showQrScanner.value = false;
}

function xuLyMaQuet(value) {
  showQrScanner.value = false;
  emit("scan-product", value);
}
</script>

<template>
  <div class="space-y-6">
    <div class="relative">
      <label class="mb-2 block text-sm font-semibold text-slate-700">Tìm sản phẩm</label>
      <div class="flex gap-3">
        <div class="relative flex-1">
          <input
            :value="productKeyword"
            type="text"
            placeholder="Nhập mã, tên sản phẩm, SKU..."
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300 focus:bg-white"
            @input="emit('update:productKeyword', $event.target.value)"
            @focus="emit('focus-product')"
            @blur="emit('blur-product')"
          />

          <div
            v-if="loadingProducts"
            class="absolute right-4 top-1/2 -translate-y-1/2 text-xs font-semibold text-slate-400"
          >
            Đang tìm...
          </div>
        </div>

        <button
          type="button"
          class="inline-flex shrink-0 items-center gap-2 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-600 transition hover:bg-red-100"
          @click="moQuetQr"
        >
          <QrCode :size="16" />
          Quét QR
        </button>
      </div>
    </div>

    <div class="rounded-[28px] border border-slate-100 bg-[linear-gradient(180deg,#fff8f5_0%,#ffffff_100%)] p-5 shadow-[0_18px_40px_rgba(15,23,42,0.04)]">
      <div class="max-h-[500px] overflow-y-auto pr-1">
        <div
          v-if="!loadingProducts && !productResults.length"
          class="rounded-2xl border border-dashed border-slate-200 bg-white px-4 py-12 text-center text-sm text-slate-500"
        >
          Không tìm thấy sản phẩm phù hợp.
        </div>

        <div v-else class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          <button
            v-for="product in productResults"
            :key="`panel-${product.chiTietId}`"
            type="button"
            class="group flex flex-col justify-between rounded-2xl border border-slate-100 bg-white p-3 text-left shadow-[0_4px_20px_rgba(15,23,42,0.02)] transition-all duration-200 hover:-translate-y-1 hover:border-red-200 hover:shadow-[0_12px_30px_rgba(239,68,68,0.08)]"
            @click="emit('open-product', product)"
          >
            <div>
              <div class="relative h-28 w-full overflow-hidden rounded-xl bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] text-base font-bold text-red-400 flex items-center justify-center mb-2">
                <img v-if="product.hinhAnh" :src="product.hinhAnh" alt="" class="h-full w-full object-contain p-1.5 transition duration-300 group-hover:scale-105" />
                <span v-else>{{ product.tenSanPham.slice(0, 1) }}</span>
                <span
                  v-if="isDiscounted(product)"
                  class="absolute left-2 top-2 inline-flex rounded-full bg-rose-500 px-2 py-0.5 text-[9px] font-bold text-white shadow-sm"
                >
                  {{ formatDiscountPercent(product) || 'GIẢM GIÁ' }}
                </span>
              </div>

              <div class="min-w-0">
                <p class="line-clamp-2 text-xs font-bold text-slate-900 group-hover:text-red-500 transition duration-150 min-h-[32px] leading-snug">{{ product.tenSanPham }}</p>
                <p class="mt-0.5 truncate text-[10px] text-slate-400">
                  Mã: {{ product.maSanPham }}
                </p>
                <div class="mt-1 flex items-center justify-between text-[11px] text-slate-500">
                  <span>{{ product.tongBienThe || 1 }} biến thể</span>
                  <span class="font-semibold text-slate-700">Tồn: {{ product.soLuongTon }}</span>
                </div>
              </div>
            </div>

            <div class="mt-2.5 pt-2 border-t border-slate-50 flex items-center justify-between gap-2">
              <span class="text-xs font-bold text-red-500">{{ dinhDangTien(product.giaBan) }}</span>
              <span class="inline-flex rounded-lg bg-slate-50 px-2 py-1 text-[10px] font-semibold text-slate-600 group-hover:bg-red-50 group-hover:text-red-600 transition">
                Chi tiết
              </span>
            </div>
          </button>
        </div>
      </div>

      <div v-if="totalPages > 1" class="mt-5 flex flex-col items-center justify-between gap-3 border-t border-slate-100 pt-4 sm:flex-row">
        <div class="text-xs font-semibold text-slate-500">
          Trang {{ currentPage }} / {{ totalPages }}
        </div>
        <div class="flex items-center gap-2">
          <button
            type="button"
            :disabled="currentPage === 1"
            class="rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 disabled:opacity-50 disabled:hover:bg-white"
            @click="emit('update:currentPage', currentPage - 1)"
          >
            Trước
          </button>
          <div class="flex gap-1">
            <button
              v-for="page in totalPages"
              :key="page"
              type="button"
              :class="[
                'h-8 w-8 rounded-xl text-xs font-bold transition flex items-center justify-center',
                currentPage === page
                  ? 'bg-red-500 text-white shadow-sm'
                  : 'border border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
              ]"
              @click="emit('update:currentPage', page)"
            >
              {{ page }}
            </button>
          </div>
          <button
            type="button"
            :disabled="currentPage === totalPages"
            class="rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 disabled:opacity-50 disabled:hover:bg-white"
            @click="emit('update:currentPage', currentPage + 1)"
          >
            Sau
          </button>
        </div>
      </div>
    </div>

    <BanHangQrScannerModal
      :open="showQrScanner"
      :show-manual-section="false"
      :show-camera-hint="false"
      :show-retry-button="false"
      @close="dongQuetQr"
      @scan="xuLyMaQuet"
    />
  </div>
</template>
