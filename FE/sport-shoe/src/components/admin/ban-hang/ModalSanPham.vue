<script setup>
import { ref, computed } from "vue";
import { resolveHinhAnh } from "../../../utils/resolve-image";

const props = defineProps({
  selectedProductDetail: {
    type: Object,
    default: null
  },
  chiTietDangChon: {
    type: Object,
    default: null
  },
  currentProductImage: {
    type: String,
    default: ""
  },
  soLuongTonSauKhiChon: {
    type: Number,
    default: 0
  },
  bienTheLienQuan: {
    type: Array,
    default: () => []
  },
  colorOptions: {
    type: Array,
    default: () => []
  },
  sizeOptions: {
    type: Array,
    default: () => []
  },
  selectedColor: {
    type: String,
    default: ""
  },
  selectedSize: {
    type: String,
    default: ""
  },
  selectedQuantity: {
    type: Number,
    default: 1
  },
  soLuongTonKhaDungChiTiet: {
    type: Number,
    default: 0
  },
  dinhDangTien: {
    type: Function,
    required: true
  },
  soLuongConLai: {
    type: Function,
    default: (id, qty) => qty
  },
  isEditMode: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits([
  "close",
  "select-color",
  "select-size",
  "decrease-quantity",
  "increase-quantity",
  "update-quantity",
  "select-variant",
  "add-selected-variant"
]);

const filterColor = ref("");
const filterSize = ref("");

const availableColors = computed(() => {
  if (!props.bienTheLienQuan) return [];
  const set = new Set();
  props.bienTheLienQuan.forEach(v => {
    const val = v.mauSac || v.maBienThe;
    if (val) set.add(val);
  });
  return Array.from(set);
});

const availableSizes = computed(() => {
  if (!props.bienTheLienQuan) return [];
  const set = new Set();
  props.bienTheLienQuan.forEach(v => {
    if (v.kichCo) set.add(v.kichCo);
  });
  return Array.from(set).sort((a, b) => {
    const numA = parseFloat(a);
    const numB = parseFloat(b);
    if (!isNaN(numA) && !isNaN(numB)) return numA - numB;
    return String(a).localeCompare(String(b));
  });
});

const filteredVariants = computed(() => {
  if (!props.bienTheLienQuan) return [];
  return props.bienTheLienQuan.filter(v => {
    const matchColor = !filterColor.value || (v.mauSac || v.maBienThe) === filterColor.value;
    const matchSize = !filterSize.value || String(v.kichCo) === String(filterSize.value);
    return matchColor && matchSize;
  });
});

function isDiscounted(product) {
  return Number(product?.giaBan || 0) < Number(product?.giaGoc || 0);
}

function formatDiscountPercent(product) {
  const giaGoc = Number(product?.giaGoc || 0);
  const giaBan = Number(product?.giaBan || 0);
  if (giaGoc <= 0 || giaBan >= giaGoc) return "";
  const pct = ((giaGoc - giaBan) / giaGoc) * 100;
  return pct % 1 === 0 ? `-${pct.toFixed(0)}%` : `-${pct.toFixed(1)}%`;
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="selectedProductDetail"
        class="fixed inset-0 z-[110] flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
      >
        <div class="modal-content relative w-full max-w-6xl overflow-hidden rounded-[24px] bg-white shadow-2xl">
          <!-- Close Button -->
          <button
            type="button"
            class="absolute right-4 top-4 z-10 flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-500 transition hover:bg-slate-200 hover:text-slate-900"
            @click="emit('close')"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>

          <div class="flex flex-col md:flex-row min-h-[480px]">
            <!-- Left: Image (Compact fixed w-64) -->
            <div class="bg-slate-50 border-r border-slate-100 p-4 w-full md:w-64 flex items-center justify-center relative shrink-0">
              <img
                v-if="currentProductImage"
                :src="resolveHinhAnh(currentProductImage)"
                alt="Product Image"
                class="max-h-60 max-w-full object-contain drop-shadow-md transition-transform hover:scale-105"
              />
              <div
                v-else
                class="flex h-36 w-36 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] text-5xl font-black text-red-300 shadow-inner"
              >
                {{ selectedProductDetail.tenSanPham?.slice(0, 1) }}
              </div>
              
              <!-- Badge Giảm giá trên ảnh -->
              <span
                v-if="isDiscounted(chiTietDangChon)"
                class="absolute left-3 top-3 inline-flex items-center rounded-md bg-red-500 px-2.5 py-1 text-xs font-bold uppercase tracking-wide text-white shadow-md"
              >
                Giảm giá
              </span>
            </div>

            <!-- Right: Content (Spacious flex-1 min-w-0 for table) -->
            <div class="flex flex-col p-5 md:p-6 flex-1 min-w-0 max-h-[85vh] overflow-hidden">
              <div class="shrink-0">
                <p class="text-xs font-semibold uppercase tracking-widest text-slate-400">
                  Mã: {{ selectedProductDetail.maSanPham }}
                </p>
                <h3 class="mt-1.5 text-2xl font-bold text-slate-900 leading-tight">
                  {{ selectedProductDetail.tenSanPham }}
                </h3>

                <div class="mt-3 flex items-baseline gap-3">
                  <p class="text-2xl font-bold text-red-500">
                    {{ dinhDangTien(chiTietDangChon?.giaBan || selectedProductDetail.giaBanKhoiDiem || 0) }}
                  </p>
                  <p
                    v-if="isDiscounted(chiTietDangChon)"
                    class="text-sm font-medium text-slate-400 line-through"
                  >
                    {{ dinhDangTien(chiTietDangChon?.giaGoc || 0) }}
                  </p>
                </div>
              </div>

              <div class="my-3 h-px w-full bg-slate-100"></div>

              <!-- Filter Combobox Bar -->
              <div class="flex items-center gap-3 mb-2 p-2 bg-slate-50 dark:bg-slate-800/50 rounded-xl border border-slate-200/80 dark:border-slate-700">
                <div class="flex items-center gap-1.5 text-xs font-bold text-slate-600 dark:text-slate-300">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-rose-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
                  </svg>
                  Lọc biến thể:
                </div>

                <!-- Color Combobox -->
                <div class="relative flex-1 max-w-[170px]">
                  <select
                    v-model="filterColor"
                    class="w-full rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-2.5 py-1.5 text-xs font-semibold text-slate-800 dark:text-slate-200 outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 shadow-sm transition"
                  >
                    <option value="">Tất cả màu sắc</option>
                    <option v-for="c in availableColors" :key="c" :value="c">
                      Màu {{ c }}
                    </option>
                  </select>
                </div>

                <!-- Size Combobox -->
                <div class="relative flex-1 max-w-[150px]">
                  <select
                    v-model="filterSize"
                    class="w-full rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-2.5 py-1.5 text-xs font-semibold text-slate-800 dark:text-slate-200 outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 shadow-sm transition"
                  >
                    <option value="">Tất cả kích cỡ</option>
                    <option v-for="s in availableSizes" :key="s" :value="s">
                      Size {{ s }}
                    </option>
                  </select>
                </div>

                <!-- Clear Filter -->
                <button
                  v-if="filterColor || filterSize"
                  type="button"
                  class="text-xs font-bold text-rose-600 hover:text-rose-700 underline ml-auto cursor-pointer flex items-center gap-1"
                  @click="filterColor = ''; filterSize = ''"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
                  </svg>
                  Xóa lọc
                </button>
              </div>

              <!-- Variants Table -->
              <div class="flex-1 overflow-auto min-h-[280px] border border-slate-200 rounded-lg my-2 custom-scrollbar">
                <table class="w-full text-left text-xs text-slate-600 whitespace-nowrap">
                  <thead class="bg-slate-50 text-slate-700 font-bold uppercase tracking-wider border-b border-slate-200 sticky top-0 z-10 text-[11px]">
                    <tr>
                      <th class="px-2.5 py-2.5 text-center">Hình ảnh</th>
                      <th class="px-2.5 py-2.5">Mã SKU</th>
                      <th class="px-2.5 py-2.5">Màu sắc</th>
                      <th class="px-2.5 py-2.5 text-center">Kích cỡ</th>
                      <th class="px-2.5 py-2.5 text-right">Giá bán</th>
                      <th class="px-2.5 py-2.5 text-center">Số lượng</th>
                      <th class="px-2.5 py-2.5 text-center">Giảm giá</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    <tr
                      v-for="variant in filteredVariants"
                      :key="variant.chiTietId"
                      class="transition-colors"
                      :class="[
                        soLuongConLai(variant.chiTietId, variant.soLuongTon) > 0 ? 'hover:bg-slate-50 cursor-pointer' : 'opacity-50 cursor-not-allowed bg-slate-50',
                        chiTietDangChon && chiTietDangChon.chiTietId === variant.chiTietId ? '!bg-red-50 !border-red-200' : ''
                      ]"
                      @click="soLuongConLai(variant.chiTietId, variant.soLuongTon) > 0 && emit('select-variant', variant)"
                    >
                      <td class="px-2.5 py-2 text-center">
                        <img v-if="variant.hinhAnh" :src="resolveHinhAnh(variant.hinhAnh)" class="w-9 h-9 mx-auto rounded-md object-cover border border-slate-200" />
                        <div v-else class="w-9 h-9 mx-auto rounded-md bg-slate-100 flex items-center justify-center text-xs font-bold text-slate-400">
                          {{ (variant.mauSac || "?").slice(0, 2) }}
                        </div>
                      </td>
                      <td class="px-2.5 py-2 font-medium text-slate-900 text-xs">{{ variant.sku || variant.maBienThe }}</td>
                      <td class="px-2.5 py-2 text-xs">{{ variant.mauSac || variant.maBienThe }}</td>
                      <td class="px-2.5 py-2 text-center font-semibold text-xs">{{ variant.kichCo }}</td>
                      <td class="px-2.5 py-2 text-right">
                        <div class="flex flex-col">
                          <span class="text-red-500 font-semibold text-xs">{{ dinhDangTien(variant.giaBan || 0) }}</span>
                          <span v-if="isDiscounted(variant)" class="text-[10px] text-slate-400 line-through">{{ dinhDangTien(variant.giaGoc || 0) }}</span>
                        </div>
                      </td>
                      <td class="px-2.5 py-2 text-center">
                        <span :class="soLuongConLai(variant.chiTietId, variant.soLuongTon) > 0 ? 'text-emerald-600 font-medium' : 'text-red-500 font-medium'">
                          {{ soLuongConLai(variant.chiTietId, variant.soLuongTon) > 0 ? soLuongConLai(variant.chiTietId, variant.soLuongTon) : 'Hết hàng' }}
                        </span>
                      </td>
                      <td class="px-2.5 py-2 text-center">
                        <div v-if="isDiscounted(variant)" class="flex flex-col gap-1 items-center justify-center">
                          <span class="inline-flex rounded bg-rose-100 dark:bg-rose-900/30 px-2 py-0.5 text-[10px] font-bold text-rose-600 dark:text-rose-400">
                            {{ formatDiscountPercent(variant) }}
                          </span>
                        </div>
                        <span v-else class="text-slate-400 dark:text-slate-500 text-xs">-</span>
                      </td>
                    </tr>
                    <tr v-if="!bienTheLienQuan || bienTheLienQuan.length === 0">
                      <td colspan="7" class="px-4 py-8 text-center text-slate-500">
                        Không có biến thể nào
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- Quantity -->
              <div class="shrink-0">
                <div class="mb-3 flex items-center justify-between">
                  <p class="text-sm font-semibold text-slate-900">Số lượng</p>
                  <span
                    class="text-sm font-medium"
                    :class="soLuongTonKhaDungChiTiet > 0 ? 'text-emerald-600' : 'text-red-500'"
                  >
                    {{ soLuongTonKhaDungChiTiet > 0 ? `Còn lại ${soLuongTonSauKhiChon} sản phẩm` : "Hết hàng" }}
                  </span>
                </div>
                <div class="inline-flex h-12 w-32 items-center justify-between rounded-md border border-slate-200 bg-white px-1">
                  <button
                    type="button"
                    class="flex h-10 w-10 items-center justify-center rounded-md text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 disabled:opacity-50"
                    :disabled="selectedQuantity <= 1"
                    @click="emit('decrease-quantity')"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clip-rule="evenodd" />
                    </svg>
                  </button>
                  <input
                    type="number"
                    :value="selectedQuantity"
                    class="w-12 text-center text-base font-semibold text-slate-900 bg-transparent outline-none hide-spin-button"
                    min="1"
                    @change="emit('update-quantity', $event.target.value)"
                  />
                  <button
                    type="button"
                    class="flex h-10 w-10 items-center justify-center rounded-md text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 disabled:opacity-50"
                    :disabled="selectedQuantity >= soLuongTonKhaDungChiTiet"
                    @click="emit('increase-quantity')"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
                    </svg>
                  </button>
                </div>
              </div>

              <!-- Add to Cart -->
            <div class="mt-4 shrink-0">
              <button
                type="button"
                class="w-full rounded-md py-4 text-base font-semibold text-white transition-all active:scale-[0.98] disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-400"
                :class="isEditMode ? 'bg-amber-500 hover:bg-amber-600 shadow-md' : 'bg-red-500 hover:bg-red-600'"
                :disabled="!chiTietDangChon || soLuongTonKhaDungChiTiet <= 0"
                @click="emit('add-selected-variant')"
              >
                <template v-if="isEditMode">
                  Xác nhận đổi sản phẩm
                </template>
                <template v-else>
                  {{ !chiTietDangChon ? 'Vui lòng chọn phân loại' : (soLuongTonKhaDungChiTiet > 0 ? 'Thêm vào hóa đơn' : 'Đã hết hàng') }}
                </template>
              </button>
            </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  opacity: 0;
  transform: scale(0.95) translateY(10px);
}
.hide-spin-button::-webkit-inner-spin-button,
.hide-spin-button::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.hide-spin-button {
  -moz-appearance: textfield;
}
</style>
