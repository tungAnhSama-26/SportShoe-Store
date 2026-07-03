<script setup>
defineProps({
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

function isDiscounted(product) {
  return Number(product?.giaBan || 0) < Number(product?.giaGoc || 0);
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="selectedProductDetail"
        class="fixed inset-0 z-[110] flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
        @click.self="emit('close')"
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

          <div class="flex flex-col md:flex-row">
            <!-- Left: Image -->
            <div class="bg-slate-50 md:w-4/12 lg:w-4/12 flex-shrink-0">
              <div class="relative h-64 w-full md:h-full md:min-h-[500px]">
                <img
                  v-if="currentProductImage"
                  :src="currentProductImage"
                  alt="Product Image"
                  class="absolute inset-0 h-full w-full object-cover"
                />
                <div
                  v-else
                  class="absolute inset-0 flex items-center justify-center bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] text-8xl font-black text-red-300"
                >
                  {{ selectedProductDetail.tenSanPham?.slice(0, 1) }}
                </div>
                
                <!-- Badge Giảm giá trên ảnh -->
                <span
                  v-if="isDiscounted(chiTietDangChon)"
                  class="absolute left-4 top-4 inline-flex items-center rounded-md bg-red-500 px-3 py-1.5 text-xs font-bold uppercase tracking-wide text-white shadow-md"
                >
                  Giảm giá
                </span>
              </div>
            </div>

            <!-- Right: Content -->
            <div class="flex flex-col p-6 md:w-8/12 md:p-8 lg:w-8/12 max-h-[85vh] overflow-hidden">
              <div class="shrink-0">
                <p class="text-xs font-semibold uppercase tracking-widest text-slate-400">
                  Mã: {{ selectedProductDetail.maSanPham }}
                </p>
                <h3 class="mt-2 text-2xl font-bold text-slate-900 leading-tight">
                  {{ selectedProductDetail.tenSanPham }}
                </h3>

                <div class="mt-4 flex flex-col gap-1">
                  <p class="text-3xl font-bold text-red-500">
                    {{ dinhDangTien(chiTietDangChon?.giaBan || selectedProductDetail.giaBanKhoiDiem || 0) }}
                  </p>
                  <p
                    v-if="isDiscounted(chiTietDangChon)"
                    class="text-base font-medium text-slate-400 line-through"
                  >
                    {{ dinhDangTien(chiTietDangChon?.giaGoc || 0) }}
                  </p>
                </div>
              </div>

              <div class="my-6 h-px w-full bg-slate-100"></div>

              <!-- Variants Table -->
              <div class="flex-1 overflow-auto min-h-[300px] border border-slate-200 rounded-md my-4 custom-scrollbar">
                <table class="w-full text-left text-sm text-slate-600 whitespace-nowrap">
                  <thead class="bg-slate-50 text-slate-700 font-semibold border-b border-slate-200 sticky top-0 z-10">
                    <tr>
                      <th class="px-4 py-3">Hình ảnh</th>
                      <th class="px-4 py-3">Mã SKU</th>
                      <th class="px-4 py-3">Màu sắc</th>
                      <th class="px-4 py-3">Kích cỡ</th>
                      <th class="px-4 py-3 text-right">Giá bán</th>
                      <th class="px-4 py-3 text-right">Tồn kho</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    <tr
                      v-for="variant in bienTheLienQuan"
                      :key="variant.chiTietId"
                      class="transition-colors"
                      :class="[
                        soLuongConLai(variant.chiTietId, variant.soLuongTon) > 0 ? 'hover:bg-slate-50 cursor-pointer' : 'opacity-50 cursor-not-allowed bg-slate-50',
                        chiTietDangChon && chiTietDangChon.chiTietId === variant.chiTietId ? '!bg-red-50 !border-red-200' : ''
                      ]"
                      @click="soLuongConLai(variant.chiTietId, variant.soLuongTon) > 0 && emit('select-variant', variant)"
                    >
                      <td class="px-4 py-3">
                        <img v-if="variant.hinhAnh" :src="variant.hinhAnh" class="w-10 h-10 rounded-md object-cover border border-slate-200" />
                        <div v-else class="w-10 h-10 rounded-md bg-slate-100 flex items-center justify-center text-xs font-bold text-slate-400">
                          {{ (variant.mauSac || "?").slice(0, 2) }}
                        </div>
                      </td>
                      <td class="px-4 py-3 font-medium text-slate-900">{{ variant.sku || variant.maBienThe }}</td>
                      <td class="px-4 py-3">{{ variant.mauSac || variant.maBienThe }}</td>
                      <td class="px-4 py-3 font-medium">{{ variant.kichCo }}</td>
                      <td class="px-4 py-3 text-right">
                        <div class="flex flex-col">
                          <span class="text-red-500 font-semibold">{{ dinhDangTien(variant.giaBan || 0) }}</span>
                          <span v-if="isDiscounted(variant)" class="text-xs text-slate-400 line-through">{{ dinhDangTien(variant.giaGoc || 0) }}</span>
                        </div>
                      </td>
                      <td class="px-4 py-3 text-right">
                        <span :class="soLuongConLai(variant.chiTietId, variant.soLuongTon) > 0 ? 'text-emerald-600 font-medium' : 'text-red-500 font-medium'">
                          {{ soLuongConLai(variant.chiTietId, variant.soLuongTon) > 0 ? soLuongConLai(variant.chiTietId, variant.soLuongTon) : 'Hết hàng' }}
                        </span>
                      </td>
                    </tr>
                    <tr v-if="!bienTheLienQuan || bienTheLienQuan.length === 0">
                      <td colspan="6" class="px-4 py-8 text-center text-slate-500">
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
                class="w-full rounded-md bg-red-500 py-4 text-base font-semibold text-white transition-all hover:bg-red-600 active:scale-[0.98] disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-400"
                :disabled="!chiTietDangChon || soLuongTonKhaDungChiTiet <= 0"
                @click="emit('add-selected-variant')"
              >
                {{ !chiTietDangChon ? 'Vui lòng chọn phân loại' : (soLuongTonKhaDungChiTiet > 0 ? 'Thêm vào hóa đơn' : 'Đã hết hàng') }}
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
