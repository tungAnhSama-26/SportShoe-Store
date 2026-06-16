<script setup>
import { ref } from "vue";
import { QrCode, Search, X } from "lucide-vue-next";
import BanHangQrScannerModal from "./BanHangQrScannerModal.vue";
import { showError } from "../../../utils/alert";

const props = defineProps({
  activePendingInvoice: {
    type: Object,
    default: null
  },
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
  "refresh"
]);

const showQrScanner = ref(false);
const showProductModal = ref(false);

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

function handleOpenProduct(product) {
  if (!props.activePendingInvoice) {
    showError("Vui lòng tạo hóa đơn trước khi chọn sản phẩm.");
    return;
  }
  emit("open-product", product);
  showProductModal.value = false;
}

function moQuetQr() {
  if (!props.activePendingInvoice) {
    showError("Vui lòng tạo hóa đơn trước khi chọn sản phẩm.");
    return;
  }
  showQrScanner.value = true;
}

function dongQuetQr() {
  showQrScanner.value = false;
}

function xuLyMaQuet(value) {
  showQrScanner.value = false;
  emit("scan-product", value);
}

function handleBlur() {
  setTimeout(() => {
    emit('blur-product');
  }, 200);
}
</script>

<template>
  <div class="flex h-full flex-col">
    <!-- Tìm kiếm thu gọn và Nút mở danh sách -->
    <div class="flex items-center gap-3">
      <!-- Ô tìm kiếm (có thể tự động focus để scan mã vạch) -->
      <div class="relative flex-1">
        <div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4">
          <Search class="h-5 w-5 text-slate-400" />
        </div>
        <input
          :value="productKeyword"
          type="text"
          placeholder="Nhập mã vạch hoặc tìm kiếm (F2)..."
          class="w-full rounded-2xl border border-slate-200 bg-white py-3 pl-11 pr-4 text-sm font-medium text-slate-900 outline-none transition focus:border-red-300 focus:ring-4 focus:ring-red-50 shadow-sm"
          @input="emit('update:productKeyword', $event.target.value)"
          @focus="emit('focus-product')"
          @blur="handleBlur"
        />
        <div
          v-if="loadingProducts"
          class="absolute right-4 top-1/2 -translate-y-1/2 text-xs font-semibold text-slate-400"
        >
          Đang tìm...
        </div>

        <!-- Dropdown menu cho kết quả tìm kiếm nhanh -->
        <div v-if="showProductDropdown && productResults.length > 0" class="absolute z-50 mt-2 w-full max-h-80 overflow-y-auto rounded-2xl border border-slate-100 bg-white p-2 shadow-[0_10px_40px_rgba(0,0,0,0.08)]">
          <div 
            v-for="product in productResults" 
            :key="product.maSanPham"
            class="flex cursor-pointer items-center gap-3 rounded-xl p-2 transition hover:bg-slate-50"
            @click="handleOpenProduct(product)"
          >
            <div class="h-10 w-10 shrink-0 overflow-hidden rounded-lg bg-slate-100 border border-slate-200">
              <img v-if="product.hinhAnh" :src="product.hinhAnh" class="h-full w-full object-cover" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="truncate text-sm font-bold text-slate-700">{{ product.tenSanPham }}</div>
              <div class="text-xs text-slate-500">{{ product.maSanPham }}</div>
            </div>
            <div class="shrink-0 text-right">
              <div class="text-sm font-bold text-red-500">{{ dinhDangTien(product.giaBan) }}</div>
              <div class="text-xs text-slate-500">Tồn: {{ product.soLuongTon }}</div>
            </div>
          </div>
        </div>
        
        <div v-if="showProductDropdown && !loadingProducts && productResults.length === 0" class="absolute z-50 mt-2 w-full rounded-2xl border border-slate-100 bg-white p-4 text-center text-sm font-medium text-slate-500 shadow-[0_10px_40px_rgba(0,0,0,0.08)]">
          Không tìm thấy sản phẩm phù hợp.
        </div>
      </div>

      <!-- Nút quét QR thủ công -->
      <button
        type="button"
        class="inline-flex shrink-0 items-center justify-center rounded-2xl border border-slate-200 bg-white w-[46px] h-[46px] text-slate-500 transition hover:bg-slate-50 hover:text-red-500 shadow-sm"
        @click="moQuetQr"
        title="Quét QR bằng Camera"
      >
        <QrCode class="h-5 w-5" />
      </button>

      <!-- Nút mở modal hiển thị tất cả sản phẩm -->
      <button
        type="button"
        class="inline-flex shrink-0 items-center gap-2 rounded-2xl border border-red-200 bg-red-50 px-5 py-[11px] text-sm font-bold text-red-600 transition hover:bg-red-100 shadow-sm"
        @click="showProductModal = true"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        Chọn sản phẩm
      </button>
    </div>

    <!-- Modal chọn sản phẩm bảng lớn -->
    <Teleport to="body">
      <div v-if="showProductModal" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 px-4 py-6 backdrop-blur-sm transition-all">
        <div class="flex h-[90vh] w-[1100px] max-w-full flex-col overflow-hidden rounded-[24px] bg-white shadow-2xl">
          <!-- Header -->
          <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
            <h3 class="text-xl font-bold text-slate-800">Danh sách sản phẩm</h3>
            <button
              class="flex h-10 w-10 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
              @click="showProductModal = false"
            >
              <X class="h-5 w-5" />
            </button>
          </div>

          <!-- Body -->
          <div class="flex-1 overflow-y-auto p-6 bg-slate-50/50">
            <div class="rounded-[20px] border border-slate-200 bg-white shadow-sm overflow-hidden">
              <div v-if="!loadingProducts && !productResults.length" class="px-4 py-16 text-center text-sm text-slate-500">
                Không tìm thấy sản phẩm nào.
              </div>
              <div v-else class="overflow-x-auto w-full">
                <table class="w-full text-left text-sm text-slate-600">
                  <thead class="sticky top-0 z-10 shadow-sm">
                    <tr class="text-left text-sm font-bold text-slate-950">
                      <th class="whitespace-nowrap bg-slate-100 px-4 py-3 text-center w-12">STT</th>
                      <th class="whitespace-nowrap bg-slate-100 px-4 py-3">Mã SP</th>
                      <th class="whitespace-nowrap bg-slate-100 px-4 py-3">Tên sản phẩm</th>
                      <th class="whitespace-nowrap bg-slate-100 px-4 py-3 text-center w-16">Ảnh</th>
                      <th class="whitespace-nowrap bg-slate-100 px-4 py-3">Số lượng</th>
                      <th class="whitespace-nowrap bg-slate-100 px-4 py-3">Giá bán</th>
                      <th class="whitespace-nowrap bg-slate-100 px-4 py-3">Giảm giá</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    <tr
                      v-for="(product, index) in productResults"
                      :key="`panel-${product.chiTietId || product.sanPhamId || product.id}`"
                      class="group cursor-pointer bg-white transition hover:bg-red-50/30"
                      @click="handleOpenProduct(product)"
                    >
                      <td class="px-4 py-3 text-center text-slate-500">{{ index + 1 }}</td>
                      <td class="px-4 py-3 font-medium text-slate-700">{{ product.maSanPham }}</td>
                      <td class="px-4 py-3">
                        <div class="line-clamp-2 font-semibold text-slate-800 transition group-hover:text-red-500" :title="product.tenSanPham">
                          {{ product.tenSanPham }}
                        </div>
                      </td>
                      <td class="px-4 py-3">
                        <div class="relative h-12 w-12 overflow-hidden rounded-lg border border-slate-100 bg-white">
                          <img v-if="product.hinhAnh" :src="product.hinhAnh" alt="" class="h-full w-full object-contain" />
                          <div v-else class="flex h-full w-full items-center justify-center bg-slate-50 text-xs font-bold text-slate-400">
                            {{ product.tenSanPham ? product.tenSanPham.charAt(0).toUpperCase() : '?' }}
                          </div>
                        </div>
                      </td>
                      <td class="whitespace-nowrap px-4 py-3">
                        <span class="font-semibold text-slate-700">{{ product.soLuongTon }}</span>
                      </td>
                      <td class="whitespace-nowrap px-4 py-3">
                        <div class="flex flex-col">
                          <span class="font-bold text-red-500">{{ dinhDangTien(product.giaBan) }}</span>
                          <span v-if="isDiscounted(product)" class="text-[11px] text-slate-400 line-through mt-0.5">
                            {{ dinhDangTien(product.giaGoc) }}
                          </span>
                        </div>
                      </td>
                      <td class="whitespace-nowrap px-4 py-3">
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
          </div>
        </div>
      </div>
    </Teleport>

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
