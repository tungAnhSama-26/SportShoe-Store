<script setup>
import { computed, nextTick, ref } from "vue";
import { QrCode, Search, X, RotateCcw } from "lucide-vue-next";
import AdminTableFooter from "../../common/AdminTableFooter.vue";
import { toastSwal } from "../../../utils/alert";

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
  paginatedProducts: {
    type: Array,
    default: () => []
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
  selectedBrandFilter: {
    type: String,
    default: ""
  },
  selectedCategoryFilter: {
    type: String,
    default: ""
  },
  selectedColorFilter: {
    type: String,
    default: ""
  },
  selectedSizeFilter: {
    type: String,
    default: ""
  },
  selectedMinPrice: {
    type: Number,
    default: 0
  },
  selectedMaxPrice: {
    type: Number,
    default: 0
  },
  maxAvailablePrice: {
    type: Number,
    default: 0
  },
  availableBrands: {
    type: Array,
    default: () => []
  },
  availableCategories: {
    type: Array,
    default: () => []
  },
  availableColors: {
    type: Array,
    default: () => []
  },
  availableSizes: {
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
  "update:currentPage",
  "update:selectedBrandFilter",
  "update:selectedCategoryFilter",
  "update:selectedColorFilter",
  "update:selectedSizeFilter",
  "update:selectedMinPrice",
  "update:selectedMaxPrice",
  "update:pageSize",
  "focus-product",
  "blur-product",
  "open-product",
  "scan-product",
  "open-qr-scanner",
  "refresh",
  "update:showProductDropdown"
]);

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
    toastSwal.fire({
      icon: 'error',
      title: 'Thông báo',
      text: 'Vui lòng tạo hóa đơn trước khi chọn sản phẩm.',
      timer: 3000,
      iconColor: '#cf1018'
    });
    return;
  }
  emit("open-product", product);
  showProductModal.value = false;
  emit('update:showProductDropdown', false);
  resetFilters();
}

function dongModal() {
  showProductModal.value = false;
  emit('update:showProductDropdown', false);
  resetFilters();
}

function moQuetQr() {
  if (!props.activePendingInvoice) {
    toastSwal.fire({
      icon: 'error',
      title: 'Thông báo',
      text: 'Vui lòng tạo hóa đơn trước khi chọn sản phẩm.',
      timer: 3000,
      iconColor: '#cf1018'
    });
    return;
  }
  emit("open-qr-scanner");
}

function onPriceFilterChange(e) {
  const value = e.target.value;
  if (!value) {
    emit('update:selectedMinPrice', 0);
    emit('update:selectedMaxPrice', props.maxAvailablePrice || 999999999);
    return;
  }
  const [min, max] = value.split('-');
  emit('update:selectedMinPrice', Number(min));
  emit('update:selectedMaxPrice', Number(max));
}

const currentPriceRangeValue = computed(() => {
  if (props.selectedMinPrice === 0 && props.selectedMaxPrice === 1000000) return "0-1000000";
  if (props.selectedMinPrice === 1000000 && props.selectedMaxPrice === 2000000) return "1000000-2000000";
  if (props.selectedMinPrice === 2000000 && props.selectedMaxPrice === 3000000) return "2000000-3000000";
  if (props.selectedMinPrice === 3000000) return "3000000-999999999";
  return "";
});

function handleBlur() {
  setTimeout(() => {
    emit('blur-product');
  }, 200);
}

function handleEnter(e) {
  const keyword = e.target.value.trim();
  if (keyword) {
    emit('scan-product', keyword);
  }
}

function resetFilters() {
  emit('update:productKeyword', '');
  emit('update:selectedBrandFilter', '');
  emit('update:selectedCategoryFilter', '');
  emit('update:selectedColorFilter', '');
  emit('update:selectedSizeFilter', '');
  emit('update:selectedMinPrice', 0);
  emit('update:selectedMaxPrice', props.maxAvailablePrice || 999999999);
}

import { watch } from "vue";
watch(() => props.showProductDropdown, (newVal) => {
  if (newVal) {
    showProductModal.value = true;
    emit('refresh');
  } else {
    showProductModal.value = false;
    resetFilters();
  }
});
</script>

<template>
  <div class="flex h-full flex-col">
    <!-- Các nút thu gọn ngoài màn hình chính -->
    <div class="flex items-center gap-3">
      <!-- Nút quét QR -->
      <button
        type="button"
        class="inline-flex shrink-0 items-center justify-center rounded-md border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 w-[46px] h-[46px] text-slate-500 dark:text-slate-400 transition hover:bg-slate-50 dark:hover:bg-slate-700 hover:text-red-500 dark:hover:text-red-400 shadow-sm"
        @click="moQuetQr"
        title="Quét QR bằng Camera"
      >
        <QrCode class="h-5 w-5" />
      </button>

      <!-- Nút mở modal danh sách sản phẩm -->
      <button
        type="button"
        class="inline-flex shrink-0 items-center gap-2 rounded-md border border-red-200 dark:border-red-500/30 bg-red-50 dark:bg-red-500/10 px-5 py-[11px] text-sm font-bold text-red-600 dark:text-red-400 transition hover:bg-red-100 dark:hover:bg-red-500/20 shadow-sm"
        @click="showProductModal = true; emit('refresh')"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        Chọn sản phẩm
      </button>
    </div>

    <!-- Modal chọn sản phẩm -->
    <Teleport to="body">
      <div
        v-if="showProductModal"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 dark:bg-slate-900/80 px-4 py-6 backdrop-blur-sm"
      >
        <div class="flex h-[95vh] w-[1400px] max-w-[98vw] flex-col overflow-hidden rounded-[24px] bg-white dark:bg-slate-900 shadow-2xl">

          <!-- Hàng 1: Tiêu đề + nút đóng -->
          <div class="flex shrink-0 items-center justify-between border-b border-slate-100 dark:border-slate-800 px-6 py-4">
            <h3 class="text-xl font-bold text-slate-800 dark:text-slate-100">Danh sách sản phẩm</h3>
            <button
              class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-slate-400 dark:text-slate-500 transition hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-600 dark:hover:text-slate-300"
              @click="dongModal"
            >
              <X class="h-5 w-5" />
            </button>
          </div>

          <!-- Hàng 2: Tìm kiếm + Bộ lọc -->
          <div class="flex shrink-0 items-center gap-3 border-b border-slate-100 dark:border-slate-800 px-6 py-3">
            <!-- Ô tìm kiếm -->
            <div class="relative flex-1">
              <div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                <Search class="h-4 w-4 text-slate-400" />
              </div>
              <input
                :value="productKeyword"
                type="text"
                placeholder="Tìm kiếm mã, tên sản phẩm..."
                class="h-10 w-full rounded-md border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 py-2 pl-10 pr-3 text-sm font-medium text-slate-900 dark:text-slate-100 outline-none transition focus:border-red-300 dark:focus:border-red-500 focus:bg-white dark:focus:bg-slate-900 focus:ring-2 focus:ring-red-50 dark:focus:ring-red-900/20 shadow-sm"
                @input="emit('update:productKeyword', $event.target.value)"
                @keyup.enter="handleEnter"
                @focus="emit('focus-product')"
                @blur="handleBlur"
              />
              <div
                v-if="loadingProducts"
                class="absolute right-4 top-1/2 -translate-y-1/2 text-xs font-semibold text-slate-400"
              >
                Đang tìm...
              </div>
            </div>

            <!-- Bộ lọc thương hiệu -->
            <select
              :value="selectedBrandFilter"
              @change="emit('update:selectedBrandFilter', $event.target.value)"
              class="h-10 rounded-md border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 py-2 px-3 text-sm font-medium text-slate-900 dark:text-slate-100 outline-none transition focus:border-red-300 dark:focus:border-red-500 shadow-sm"
            >
              <option value="">Tất cả thương hiệu</option>
              <option v-for="brand in availableBrands" :key="brand" :value="brand">{{ brand }}</option>
            </select>

            <!-- Bộ lọc thể loại -->
            <select
              :value="selectedCategoryFilter"
              @change="emit('update:selectedCategoryFilter', $event.target.value)"
              class="h-10 w-40 shrink-0 rounded-md border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 py-2 px-3 text-sm font-medium text-slate-900 dark:text-slate-100 outline-none transition focus:border-red-300 dark:focus:border-red-500 shadow-sm"
            >
              <option value="">Tất cả thể loại</option>
              <option v-for="category in availableCategories" :key="category" :value="category">{{ category }}</option>
            </select>

            <!-- Bộ lọc màu sắc -->
            <select
              :value="selectedColorFilter"
              @change="emit('update:selectedColorFilter', $event.target.value)"
              class="h-10 w-32 shrink-0 rounded-md border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 py-2 px-3 text-sm font-medium text-slate-900 dark:text-slate-100 outline-none transition focus:border-red-300 dark:focus:border-red-500 shadow-sm"
            >
              <option value="">Tất cả màu</option>
              <option v-for="color in availableColors" :key="color" :value="color">{{ color }}</option>
            </select>

            <!-- Bộ lọc kích cỡ -->
            <select
              :value="selectedSizeFilter"
              @change="emit('update:selectedSizeFilter', $event.target.value)"
              class="h-10 w-32 shrink-0 rounded-md border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 py-2 px-3 text-sm font-medium text-slate-900 dark:text-slate-100 outline-none transition focus:border-red-300 dark:focus:border-red-500 shadow-sm"
            >
              <option value="">Tất cả size</option>
              <option v-for="size in availableSizes" :key="size" :value="size">{{ size }}</option>
            </select>

            <!-- Bộ lọc giá -->
            <select
              :value="currentPriceRangeValue"
              @change="onPriceFilterChange"
              class="h-10 w-40 shrink-0 rounded-md border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 py-2 px-3 text-sm font-medium text-slate-900 dark:text-slate-100 outline-none transition focus:border-red-300 dark:focus:border-red-500 shadow-sm"
            >
              <option value="">Tất cả mức giá</option>
              <option value="0-1000000">Dưới 1 triệu</option>
              <option value="1000000-2000000">Từ 1 - 2 triệu</option>
              <option value="2000000-3000000">Từ 2 - 3 triệu</option>
              <option value="3000000-999999999">Trên 3 triệu</option>
            </select>

            <!-- Nút Reset -->
            <button
              @click="resetFilters"
              class="flex h-10 shrink-0 items-center gap-1.5 rounded-md border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 px-3 py-2 text-sm font-medium text-slate-600 dark:text-slate-400 transition hover:bg-slate-50 dark:hover:bg-slate-700 hover:text-red-600 dark:hover:text-red-400 focus:outline-none focus:ring-2 focus:ring-red-100 dark:focus:ring-red-900/30 shadow-sm"
            >
              <RotateCcw class="h-4 w-4" />
              Làm mới
            </button>
          </div>

          <!-- Danh sách sản phẩm (cuộn được) -->
          <div class="flex-1 overflow-y-auto">
            <div
              v-if="!loadingProducts && !productResults.length"
              class="px-4 py-16 text-center text-sm text-slate-500"
            >
              Không tìm thấy sản phẩm nào.
            </div>
            <div v-else class="overflow-x-auto w-full custom-scrollbar">
              <table class="w-full text-left text-base text-slate-600 dark:text-slate-400">
                <thead class="sticky top-0 z-10 shadow-sm">
                  <tr class="text-left text-base font-bold text-slate-950 dark:text-slate-200">
                    <th class="whitespace-nowrap bg-slate-100 dark:bg-slate-800/80 px-5 py-4 text-center w-12">STT</th>
                    <th class="whitespace-nowrap bg-slate-100 dark:bg-slate-800/80 px-5 py-4">Mã Sản Phẩm</th>
                    <th class="whitespace-nowrap bg-slate-100 dark:bg-slate-800/80 px-5 py-4">Tên Sản Phẩm</th>
                    <th class="whitespace-nowrap bg-slate-100 dark:bg-slate-800/80 px-5 py-4">Biến Thể (Màu sắc)</th>
                    <th class="whitespace-nowrap bg-slate-100 dark:bg-slate-800/80 px-5 py-4 text-center">Size</th>
                    <th class="whitespace-nowrap bg-slate-100 dark:bg-slate-800/80 px-5 py-4 text-center w-20">Ảnh</th>
                    <th class="whitespace-nowrap bg-slate-100 dark:bg-slate-800/80 px-5 py-4">Số lượng</th>
                    <th class="whitespace-nowrap bg-slate-100 dark:bg-slate-800/80 px-5 py-4">Giá bán</th>
                    <th class="whitespace-nowrap bg-slate-100 dark:bg-slate-800/80 px-5 py-4">Giảm giá</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
                  <tr
                    v-for="(product, index) in paginatedProducts"
                    :key="`panel-${product.chiTietId || product.sanPhamId || product.id}`"
                    class="group cursor-pointer bg-white dark:bg-slate-900 transition hover:bg-red-50/30 dark:hover:bg-red-900/10"
                    @click="handleOpenProduct(product)"
                  >
                    <td class="px-5 py-4 text-center text-slate-500 dark:text-slate-400 font-medium">{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                    <td class="px-5 py-4 font-bold text-slate-800 dark:text-slate-200 text-lg">{{ product.maBienThe || product.maSanPham }}</td>
                    <td class="px-5 py-4">
                      <div
                        class="line-clamp-2 font-bold text-slate-900 dark:text-slate-100 text-lg transition group-hover:text-red-600 dark:group-hover:text-red-400"
                        :title="product.tenSanPham"
                      >
                        {{ product.tenSanPham }}
                      </div>
                    </td>
                    <td class="px-5 py-4">
                      {{ product.mauSac || '-' }}
                    </td>
                    <td class="px-5 py-4 text-center">
                      {{ product.kichCo || '-' }}
                    </td>
                    <td class="px-5 py-4">
                      <div class="relative h-16 w-16 overflow-hidden rounded-md border border-slate-100 dark:border-slate-800 bg-white dark:bg-slate-800 shadow-sm">
                        <img v-if="product.hinhAnh" :src="product.hinhAnh" alt="" class="h-full w-full object-contain" />
                        <div v-else class="flex h-full w-full items-center justify-center bg-slate-50 dark:bg-slate-800/50 text-base font-bold text-slate-400 dark:text-slate-500">
                          {{ product.tenSanPham ? product.tenSanPham.charAt(0).toUpperCase() : '?' }}
                        </div>
                      </div>
                    </td>
                    <td class="whitespace-nowrap px-5 py-4">
                      <span class="font-bold text-slate-800 dark:text-slate-200 text-lg">{{ product.soLuongTon }}</span>
                    </td>
                    <td class="whitespace-nowrap px-5 py-4">
                      <div class="flex flex-col">
                        <span class="font-bold text-red-500">
                           <template v-if="product.minPrice && product.maxPrice && product.minPrice !== product.maxPrice">
                             {{ dinhDangTien(product.minPrice) }} - {{ dinhDangTien(product.maxPrice) }}
                           </template>
                           <template v-else>
                             {{ dinhDangTien(product.giaBan) }}
                           </template>
                        </span>
                        <span v-if="isDiscounted(product)" class="text-[11px] text-slate-400 line-through mt-0.5">
                          {{ dinhDangTien(product.giaGoc) }}
                        </span>
                      </div>
                    </td>
                    <td class="whitespace-nowrap px-4 py-3">
                      <span v-if="isDiscounted(product)" class="inline-flex rounded bg-rose-100 dark:bg-rose-900/30 px-2 py-1 text-[11px] font-bold text-rose-600 dark:text-rose-400">
                        {{ formatDiscountPercent(product) }}
                      </span>
                      <span v-else class="text-slate-400 dark:text-slate-500 text-xs">-</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Phân trang: cố định dưới cùng modal -->
          <AdminTableFooter
            :current-page="currentPage"
            :page-size="pageSize"
            :page-size-options="[5, 10, 20, 50]"
            :total-items="totalItems"
            :total-pages="totalPages"
            :zero-based="false"
            compact
            no-margin
            show-refresh
            @refresh="emit('refresh')"
            @update:current-page="emit('update:currentPage', $event)"
            @update:page-size="emit('update:pageSize', $event)"
          />

        </div>
      </div>
    </Teleport>
  </div>
</template>
