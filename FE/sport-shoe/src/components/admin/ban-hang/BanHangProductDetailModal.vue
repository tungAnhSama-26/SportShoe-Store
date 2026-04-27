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
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/45 px-4 py-6"
    @click.self="emit('close')"
  >
    <div class="max-h-[85vh] w-full max-w-4xl overflow-hidden rounded-[32px] bg-white shadow-[0_30px_80px_rgba(15,23,42,0.25)]">
      <div class="flex items-start justify-between border-b border-slate-100 px-6 py-5">
        <div>
          <p class="text-xs font-semibold uppercase tracking-[0.2em] text-red-400">Sản phẩm chi tiết</p>
          <h3 class="mt-2 text-2xl font-bold text-slate-900">{{ selectedProductDetail.tenSanPham }}</h3>
          <p class="mt-1 text-sm text-slate-500">
            Mã: {{ selectedProductDetail.maSanPham }} | Chọn biến thể bên dưới
          </p>
        </div>
        <button
          type="button"
          class="rounded-full bg-slate-100 px-3 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-200"
          @click="emit('close')"
        >
          Đóng
        </button>
      </div>

      <div class="max-h-[calc(85vh-110px)] overflow-y-auto px-6 py-5">
        <div class="mb-4 rounded-2xl border border-amber-100 bg-amber-50 px-4 py-3 text-sm text-amber-700">
          Chọn màu sắc, kích cỡ và số lượng trước khi thêm vào hóa đơn.
        </div>

        <div class="grid gap-5 lg:grid-cols-[280px_minmax(0,1fr)]">
          <div class="rounded-[28px] border border-slate-200 bg-slate-50 p-4">
            <div class="overflow-hidden rounded-[24px] border border-slate-200 bg-white">
              <img
                v-if="currentProductImage"
                :src="currentProductImage"
                alt=""
                class="h-[260px] w-full object-cover"
              />
              <div
                v-else
                class="flex h-[260px] items-center justify-center bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] text-6xl font-black text-red-300"
              >
                {{ selectedProductDetail.tenSanPham?.slice(0, 1) }}
              </div>
            </div>

            <div class="mt-4 space-y-3 rounded-[24px] border border-slate-200 bg-white px-4 py-4">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <p class="text-sm text-slate-500">SKU</p>
                  <p class="mt-1 text-base font-semibold text-slate-900">{{ chiTietDangChon?.sku }}</p>
                </div>
                <div class="text-right">
                  <p class="text-sm text-slate-500">Giá bán</p>
                  <p class="mt-1 text-base font-bold text-red-500">{{ dinhDangTien(chiTietDangChon?.giaBan || 0) }}</p>
                  <p v-if="isDiscounted(chiTietDangChon)" class="mt-1 text-xs text-slate-400 line-through">
                    {{ dinhDangTien(chiTietDangChon?.giaGoc || 0) }}
                  </p>
                  <span
                    v-if="isDiscounted(chiTietDangChon)"
                    class="mt-2 inline-flex rounded-full bg-rose-50 px-2.5 py-1 text-[11px] font-semibold text-rose-600"
                  >
                    Giảm giá
                  </span>
                </div>
              </div>

              <div class="grid gap-3 sm:grid-cols-2">
                <div class="rounded-2xl bg-slate-50 px-4 py-3">
                  <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">Màu sắc</p>
                  <p class="mt-1 text-sm font-semibold text-slate-800">{{ chiTietDangChon?.mauSac || "--" }}</p>
                </div>
                <div class="rounded-2xl bg-slate-50 px-4 py-3">
                  <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">Kích cỡ</p>
                  <p class="mt-1 text-sm font-semibold text-slate-800">{{ chiTietDangChon?.kichCo || "--" }}</p>
                </div>
              </div>

              <div class="rounded-2xl bg-slate-50 px-4 py-3">
                <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">Còn lại</p>
                <p class="mt-1 text-base font-semibold text-slate-900">{{ soLuongTonSauKhiChon }}</p>
              </div>
            </div>
          </div>

          <div class="mt-1 grid gap-6">
            <div class="grid gap-3 md:grid-cols-[96px_1fr] md:items-start">
              <p class="text-base font-medium text-slate-700">Màu sắc</p>
              <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                <button
                  v-for="option in colorOptions"
                  :key="`color-${option.mauSac || option.maBienThe}`"
                  type="button"
                  class="flex items-center gap-3 rounded-xl border px-3 py-2 text-left transition"
                  :class="
                    selectedColor === (option.mauSac || option.maBienThe)
                      ? 'border-red-400 bg-red-50 text-red-600'
                      : 'border-slate-200 bg-white text-slate-700 hover:border-red-200 hover:bg-red-50'
                  "
                  @click="emit('select-color', option.mauSac || option.maBienThe)"
                >
                  <div class="flex h-11 w-11 items-center justify-center overflow-hidden rounded-lg bg-slate-100 text-xs font-bold text-slate-500">
                    <img v-if="option.hinhAnh" :src="option.hinhAnh" alt="" class="h-full w-full object-cover" />
                    <span v-else>{{ (option.mauSac || "?").slice(0, 1) }}</span>
                  </div>
                  <div class="min-w-0">
                    <p class="truncate text-sm font-semibold">{{ option.mauSac || option.maBienThe }}</p>
                    <p class="truncate text-xs text-slate-500">{{ option.maBienThe }}</p>
                  </div>
                </button>
              </div>
            </div>

            <div class="grid gap-3 md:grid-cols-[96px_1fr] md:items-start">
              <p class="text-base font-medium text-slate-700">Size</p>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="option in sizeOptions"
                  :key="`size-${option.chiTietId}`"
                  type="button"
                  class="min-w-20 rounded-xl border px-5 py-3 text-center text-sm font-semibold transition"
                  :class="
                    selectedSize === (option.kichCo || '')
                      ? 'border-red-400 bg-red-50 text-red-600'
                      : 'border-slate-200 bg-white text-slate-700 hover:border-red-200 hover:bg-red-50'
                  "
                  @click="emit('select-size', option.kichCo || '')"
                >
                  {{ option.kichCo || "--" }}
                </button>
              </div>
            </div>

            <div class="rounded-[24px] border border-slate-200 bg-slate-50 px-5 py-4">
              <div class="grid gap-3 md:grid-cols-[96px_1fr_120px] md:items-center">
                <p class="text-base font-medium text-slate-700">Số lượng</p>
                <div class="inline-flex w-fit items-center rounded-xl border border-slate-200 bg-white">
                  <button
                    type="button"
                    class="px-4 py-3 text-lg font-bold transition disabled:cursor-not-allowed disabled:text-slate-300"
                    :disabled="selectedQuantity <= 1"
                    @click="emit('decrease-quantity')"
                  >
                    -
                  </button>
                  <span class="min-w-14 border-x border-slate-200 px-4 py-3 text-center text-base font-semibold text-slate-900">
                    {{ selectedQuantity }}
                  </span>
                  <button
                    type="button"
                    class="px-4 py-3 text-lg font-bold transition disabled:cursor-not-allowed disabled:text-slate-300"
                    :disabled="selectedQuantity >= soLuongTonKhaDungChiTiet"
                    @click="emit('increase-quantity')"
                  >
                    +
                  </button>
                </div>
                <p class="text-sm font-semibold uppercase" :class="soLuongTonKhaDungChiTiet > 0 ? 'text-emerald-600' : 'text-amber-600'">
                  {{ soLuongTonKhaDungChiTiet > 0 ? "Còn hàng" : "Hết hàng" }}
                </p>
              </div>
            </div>

            <div class="flex justify-end">
              <button
                type="button"
                class="rounded-2xl bg-red-500 px-5 py-3 text-sm font-bold text-white shadow-[0_20px_40px_rgba(239,68,68,0.25)] transition hover:bg-red-600"
                @click="emit('add-selected-variant')"
              >
                Thêm vào hóa đơn
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
