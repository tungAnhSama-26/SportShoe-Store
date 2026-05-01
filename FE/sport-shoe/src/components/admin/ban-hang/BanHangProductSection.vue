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
]);

const showQrScanner = ref(false);

function isDiscounted(product) {
  return Boolean(product?.coGiamGia) || Number(product?.giaBan || 0) < Number(product?.giaGoc || 0);
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

      <div
        v-if="showProductDropdown"
        class="absolute z-20 mt-2 w-full rounded-3xl border border-slate-200 bg-white p-2 shadow-[0_24px_50px_rgba(15,23,42,0.12)]"
      >
        <div v-if="!loadingProducts && !productResults.length" class="rounded-2xl px-3 py-3 text-sm text-slate-500">
          Không tìm thấy sản phẩm phù hợp.
        </div>

        <button
          v-for="product in productResults"
          :key="product.chiTietId"
          type="button"
          class="flex w-full items-start justify-between gap-4 rounded-2xl px-3 py-3 text-left transition hover:bg-red-50"
          @click="emit('open-product', product)"
        >
          <div class="flex min-w-0 items-start gap-3">
            <div class="flex h-14 w-14 shrink-0 items-center justify-center overflow-hidden rounded-2xl bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] text-sm font-bold text-red-400">
              <img v-if="product.hinhAnh" :src="product.hinhAnh" alt="" class="h-full w-full object-cover" />
              <span v-else>{{ product.tenSanPham.slice(0, 1) }}</span>
            </div>

            <div class="min-w-0">
              <p class="truncate text-sm font-bold text-slate-900">{{ product.tenSanPham }}</p>
              <p class="mt-1 truncate text-xs text-slate-500">
                Mã: {{ product.maSanPham }} | {{ product.tongBienThe || 1 }} biến thể
              </p>
            </div>
          </div>

          <div class="text-right">
            <p class="text-sm font-semibold text-red-500">{{ dinhDangTien(product.giaBan) }}</p>
            <p v-if="isDiscounted(product)" class="mt-1">
              <span class="inline-flex rounded-full bg-rose-50 px-2.5 py-1 text-[11px] font-semibold text-rose-600">
                Giảm giá
              </span>
            </p>
            <p class="mt-1 text-xs text-slate-500">Tồn kho: {{ product.soLuongTon }}</p>
          </div>
        </button>
      </div>
    </div>

    <div class="rounded-[28px] border border-slate-100 bg-[linear-gradient(180deg,#fff8f5_0%,#ffffff_100%)] p-4 shadow-[0_18px_40px_rgba(15,23,42,0.06)]">
      <div class="flex flex-col gap-3 border-b border-slate-100 pb-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-sm font-semibold text-slate-800">{{ productSearchLabel }}</p>
        </div>
        <div class="rounded-2xl bg-white px-4 py-3 text-xs font-semibold text-slate-500 shadow-sm">
          {{ loadingProducts ? "Đang tải sản phẩm..." : productResults.length + " sản phẩm" }}
        </div>
      </div>

      <div class="mt-4 max-h-[360px] space-y-3 overflow-y-auto pr-1">
        <div
          v-if="!loadingProducts && !productResults.length"
          class="rounded-2xl border border-dashed border-slate-200 bg-white px-4 py-8 text-center text-sm text-slate-500"
        >
          Không tìm thấy sản phẩm phù hợp.
        </div>

        <button
          v-for="product in productResults"
          :key="`panel-${product.chiTietId}`"
          type="button"
          class="flex w-full items-center justify-between gap-4 rounded-[24px] border border-white bg-white px-4 py-4 text-left shadow-[0_12px_30px_rgba(15,23,42,0.06)] transition hover:-translate-y-0.5 hover:border-red-200 hover:bg-red-50"
          @click="emit('open-product', product)"
        >
          <div class="flex min-w-0 items-center gap-4">
            <div class="flex h-16 w-16 shrink-0 items-center justify-center overflow-hidden rounded-2xl bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] text-lg font-bold text-red-400">
              <img v-if="product.hinhAnh" :src="product.hinhAnh" alt="" class="h-full w-full object-cover" />
              <span v-else>{{ product.tenSanPham.slice(0, 1) }}</span>
            </div>

            <div class="min-w-0">
              <p class="truncate text-base font-bold text-slate-900">{{ product.tenSanPham }}</p>
              <p class="mt-1 truncate text-xs text-slate-500">
                Mã: {{ product.maSanPham }} | {{ product.tongBienThe || 1 }} biến thể
              </p>
              <div class="mt-2 flex flex-wrap items-center gap-2">
                <p class="text-sm font-semibold text-slate-700">Tồn kho: {{ product.soLuongTon }}</p>
                <span
                  v-if="isDiscounted(product)"
                  class="inline-flex rounded-full bg-rose-50 px-2.5 py-1 text-[11px] font-semibold text-rose-600"
                >
                  Giảm giá
                </span>
              </div>
            </div>
          </div>

          <div class="shrink-0 text-right">
            <p class="text-sm font-semibold text-red-500">{{ dinhDangTien(product.giaBan) }}</p>
            <span class="mt-2 inline-flex rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
              Xem chi tiết
            </span>
          </div>
        </button>
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
