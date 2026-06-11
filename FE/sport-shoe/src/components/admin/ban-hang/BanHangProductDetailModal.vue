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
  }
});

const emit = defineEmits([
  "close",
  "select-color",
  "select-size",
  "decrease-quantity",
  "increase-quantity",
  "add-selected-variant"
]);

function isDiscounted(product) {
  return Number(product?.giaBan || 0) < Number(product?.giaGoc || 0);
}
</script>

<template>
  <div
    v-if="selectedProductDetail"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm transition-opacity"
    @click.self="emit('close')"
  >
    <div class="relative w-full max-w-4xl overflow-hidden rounded-[24px] bg-white shadow-2xl">
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
        <div class="bg-slate-50 md:w-5/12 lg:w-1/2">
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
          </div>
        </div>

        <!-- Right: Content -->
        <div class="flex flex-col p-6 md:w-7/12 md:p-8 lg:w-1/2 max-h-[85vh] overflow-y-auto">
          <div>
            <p class="text-xs font-semibold uppercase tracking-widest text-slate-400">
              Mã: {{ selectedProductDetail.maSanPham }}
            </p>
            <h3 class="mt-2 text-2xl font-bold text-slate-900 leading-tight">
              {{ selectedProductDetail.tenSanPham }}
            </h3>

            <div class="mt-4 flex items-end gap-3">
              <p class="text-3xl font-bold text-red-500">
                {{ dinhDangTien(chiTietDangChon?.giaBan || selectedProductDetail.giaBanKhoiDiem || 0) }}
              </p>
              <p
                v-if="isDiscounted(chiTietDangChon)"
                class="mb-1 text-base font-medium text-slate-400 line-through"
              >
                {{ dinhDangTien(chiTietDangChon?.giaGoc || 0) }}
              </p>
              <span
                v-if="isDiscounted(chiTietDangChon)"
                class="mb-1.5 inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-semibold text-red-600"
              >
                Giảm giá
              </span>
            </div>
          </div>

          <div class="my-6 h-px w-full bg-slate-100"></div>

          <!-- Variants -->
          <div class="space-y-6 flex-1">
            <!-- Colors -->
            <div>
              <div class="mb-3 flex items-center justify-between">
                <p class="text-sm font-semibold text-slate-900">Màu sắc</p>
                <span class="text-sm font-medium text-slate-500">{{ selectedColor || "Chưa chọn" }}</span>
              </div>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="option in colorOptions"
                  :key="`color-${option.mauSac || option.maBienThe}`"
                  type="button"
                  class="group relative h-12 w-12 overflow-hidden rounded-full ring-2 ring-offset-2 transition-all"
                  :class="
                    selectedColor === (option.mauSac || option.maBienThe)
                      ? 'ring-red-500'
                      : 'ring-transparent hover:ring-slate-300'
                  "
                  @click="emit('select-color', option.mauSac || option.maBienThe)"
                  :title="option.mauSac || option.maBienThe"
                >
                  <img
                    v-if="option.hinhAnh"
                    :src="option.hinhAnh"
                    alt=""
                    class="h-full w-full object-cover"
                  />
                  <div
                    v-else
                    class="flex h-full w-full items-center justify-center bg-slate-100 text-xs font-bold text-slate-500"
                  >
                    {{ (option.mauSac || "?").slice(0, 2) }}
                  </div>
                </button>
              </div>
            </div>

            <!-- Sizes -->
            <div>
              <div class="mb-3 flex items-center justify-between">
                <p class="text-sm font-semibold text-slate-900">Kích cỡ</p>
                <span class="text-sm font-medium text-slate-500">{{ selectedSize || "Chưa chọn" }}</span>
              </div>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="option in sizeOptions"
                  :key="`size-${option.chiTietId}`"
                  type="button"
                  class="flex h-10 min-w-[3rem] items-center justify-center rounded-lg border px-3 text-sm font-medium transition-all"
                  :class="
                    selectedSize === (option.kichCo || '')
                      ? 'border-red-500 bg-red-500 text-white'
                      : 'border-slate-200 bg-white text-slate-700 hover:border-slate-300 hover:bg-slate-50'
                  "
                  @click="emit('select-size', option.kichCo || '')"
                >
                  {{ option.kichCo || "--" }}
                </button>
              </div>
            </div>

            <!-- Quantity -->
            <div>
              <div class="mb-3 flex items-center justify-between">
                <p class="text-sm font-semibold text-slate-900">Số lượng</p>
                <span
                  class="text-sm font-medium"
                  :class="soLuongTonKhaDungChiTiet > 0 ? 'text-emerald-600' : 'text-red-500'"
                >
                  {{ soLuongTonKhaDungChiTiet > 0 ? `Còn lại ${soLuongTonSauKhiChon} sản phẩm` : "Hết hàng" }}
                </span>
              </div>
              <div class="inline-flex h-12 w-32 items-center justify-between rounded-xl border border-slate-200 bg-white px-1">
                <button
                  type="button"
                  class="flex h-10 w-10 items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 disabled:opacity-50"
                  :disabled="selectedQuantity <= 1"
                  @click="emit('decrease-quantity')"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clip-rule="evenodd" />
                  </svg>
                </button>
                <span class="text-base font-semibold text-slate-900">{{ selectedQuantity }}</span>
                <button
                  type="button"
                  class="flex h-10 w-10 items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 disabled:opacity-50"
                  :disabled="selectedQuantity >= soLuongTonKhaDungChiTiet"
                  @click="emit('increase-quantity')"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- Add to Cart -->
          <div class="mt-8">
            <button
              type="button"
              class="w-full rounded-2xl bg-red-500 py-4 text-base font-semibold text-white transition-all hover:bg-red-600 active:scale-[0.98] disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-400"
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
</template>
