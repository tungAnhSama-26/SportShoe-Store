<script setup>
import { ref } from "vue";
import { QrCode } from "lucide-vue-next";
import BanHangQrScannerModal from "./BanHangQrScannerModal.vue";
import AdminTableFooter from "../../common/AdminTableFooter.vue";

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
  pageSize: {
    type: Number,
    default: 5
  },
  totalItems: {
    type: Number,
    default: 0
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
  "update:page-size",
  "refresh"
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
  <div class="flex h-full flex-col space-y-4">
    <div class="shrink-0 relative">
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

    <div class="flex min-h-0 flex-1 flex-col rounded-[24px] border border-slate-100 bg-[linear-gradient(180deg,#fff8f5_0%,#ffffff_100%)] p-3 shadow-[0_18px_40px_rgba(15,23,42,0.04)]">
      <div class="flex-1 overflow-y-auto pr-1">
        <div
          v-if="!loadingProducts && !productResults.length"
          class="rounded-2xl border border-dashed border-slate-200 bg-white px-4 py-12 text-center text-sm text-slate-500"
        >
          Không tìm thấy sản phẩm phù hợp.
        </div>

        <div v-else class="overflow-x-auto w-full pb-2">
          <table class="w-full text-left text-sm text-slate-600">
            <thead class="sticky top-0 z-10 shadow-sm">
              <tr class="text-left text-sm font-bold text-slate-950">
                <th class="whitespace-nowrap bg-slate-100 px-3 py-3 text-center w-12">STT</th>
                <th class="whitespace-nowrap bg-slate-100 px-3 py-3">Mã SP</th>
                <th class="whitespace-nowrap bg-slate-100 px-3 py-3">Tên sản phẩm</th>
                <th class="whitespace-nowrap bg-slate-100 px-3 py-3 text-center w-16">Ảnh</th>
                <th class="whitespace-nowrap bg-slate-100 px-3 py-3">Tồn kho</th>
                <th class="whitespace-nowrap bg-slate-100 px-3 py-3">Giá bán</th>
                <th class="whitespace-nowrap bg-slate-100 px-3 py-3">Giảm giá</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="(product, index) in productResults"
                :key="`panel-${product.chiTietId || product.sanPhamId || product.id}`"
                class="group cursor-pointer bg-white transition hover:bg-red-50/30"
                @click="emit('open-product', product)"
              >
                <td class="px-4 py-2 text-center text-slate-500">
                  {{ (currentPage - 1) * pageSize + index + 1 }}
                </td>
                <td class="px-4 py-2 font-medium text-slate-700">
                  {{ product.maSanPham }}
                </td>
                <td class="px-4 py-2">
                  <div class="line-clamp-2 font-semibold text-slate-800 transition group-hover:text-red-500" :title="product.tenSanPham">
                    {{ product.tenSanPham }}
                  </div>
                </td>
                <td class="px-4 py-2">
                  <div class="relative h-12 w-12 overflow-hidden rounded-lg border border-slate-100 bg-white">
                    <img v-if="product.hinhAnh" :src="product.hinhAnh" alt="" class="h-full w-full object-contain" />
                    <div v-else class="flex h-full w-full items-center justify-center bg-slate-50 text-xs font-bold text-slate-400">
                      {{ product.tenSanPham ? product.tenSanPham.charAt(0).toUpperCase() : '?' }}
                    </div>
                  </div>
                </td>
                <td class="whitespace-nowrap px-4 py-2">
                  <span class="font-semibold text-slate-700">{{ product.soLuongTon }}</span>
                </td>
                <td class="whitespace-nowrap px-4 py-2">
                  <div class="flex flex-col">
                    <span class="font-bold text-red-500">{{ dinhDangTien(product.giaBan) }}</span>
                    <span v-if="isDiscounted(product)" class="text-[11px] text-slate-400 line-through mt-0.5">
                      {{ dinhDangTien(product.giaGoc) }}
                    </span>
                  </div>
                </td>
                <td class="whitespace-nowrap px-4 py-2">
                  <span v-if="isDiscounted(product)" class="inline-flex rounded bg-rose-100 px-2 py-1 text-[11px] font-bold text-rose-600">
                    {{ formatDiscountPercent(product) }}
                  </span>
                  <span v-else class="text-slate-400 text-xs">-</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="mt-4 shrink-0">
        <AdminTableFooter
          v-if="totalPages > 1 || totalItems > 0"
        :current-page="currentPage"
        :page-size="pageSize"
        :page-size-options="[5, 10, 20, 50]"
        :total-items="totalItems"
        :total-pages="totalPages"
        compact
        show-refresh
        @update:current-page="emit('update:currentPage', $event)"
        @update:page-size="emit('update:page-size', $event)"
        @refresh="emit('refresh')"
        />
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
