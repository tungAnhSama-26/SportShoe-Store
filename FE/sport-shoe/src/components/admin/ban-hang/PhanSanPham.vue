<script setup>
import { computed, nextTick, ref } from "vue";
import { QrCode, Search, X } from "lucide-vue-next";
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
}

function dongModal() {
  showProductModal.value = false;
  emit('update:showProductDropdown', false);
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

import { watch } from "vue";
watch(() => props.showProductDropdown, (newVal) => {
  if (newVal) {
    showProductModal.value = true;
    emit('refresh');
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
        class="inline-flex shrink-0 items-center justify-center rounded-2xl border border-slate-200 bg-white w-[46px] h-[46px] text-slate-500 transition hover:bg-slate-50 hover:text-red-500 shadow-sm"
        @click="moQuetQr"
        title="Quét QR bằng Camera"
      >
        <QrCode class="h-5 w-5" />
      </button>

      <!-- Nút mở modal danh sách sản phẩm -->
      <button
        type="button"
        class="inline-flex shrink-0 items-center gap-2 rounded-2xl border border-red-200 bg-red-50 px-5 py-[11px] text-sm font-bold text-red-600 transition hover:bg-red-100 shadow-sm"
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
        class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 px-4 py-6 backdrop-blur-sm"
      >
        <div class="flex h-[90vh] w-[1100px] max-w-full flex-col overflow-hidden rounded-[24px] bg-white shadow-2xl">

          <!-- Hàng 1: Tiêu đề + nút đóng -->
          <div class="flex shrink-0 items-center justify-between border-b border-slate-100 px-6 py-4">
            <h3 class="text-xl font-bold text-slate-800">Danh sách sản phẩm</h3>
            <button
              class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
              @click="dongModal"
            >
              <X class="h-5 w-5" />
            </button>
          </div>

          <!-- Hàng 2: Tìm kiếm + Bộ lọc -->
          <div class="flex shrink-0 items-center gap-3 border-b border-slate-100 px-6 py-3">
            <!-- Ô tìm kiếm -->
            <div class="relative flex-1">
              <div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4">
                <Search class="h-5 w-5 text-slate-400" />
              </div>
              <input
                :value="productKeyword"
                type="text"
                placeholder="Tìm kiếm sản phẩm..."
                class="w-full rounded-xl border border-slate-200 bg-slate-50 py-2.5 pl-11 pr-4 text-sm font-medium text-slate-900 outline-none transition focus:border-red-300 focus:bg-white focus:ring-4 focus:ring-red-50 shadow-sm"
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
              class="rounded-xl border border-slate-200 bg-slate-50 py-2.5 px-3 text-sm font-medium text-slate-900 outline-none transition focus:border-red-300 shadow-sm"
            >
              <option value="">Tất cả thương hiệu</option>
              <option v-for="brand in availableBrands" :key="brand" :value="brand">{{ brand }}</option>
            </select>

            <!-- Bộ lọc thể loại -->
            <select
              :value="selectedCategoryFilter"
              @change="emit('update:selectedCategoryFilter', $event.target.value)"
              class="w-40 shrink-0 rounded-xl border border-slate-200 bg-slate-50 py-2.5 px-3 text-sm font-medium text-slate-900 outline-none transition focus:border-red-300 shadow-sm"
            >
              <option value="">Tất cả thể loại</option>
              <option v-for="category in availableCategories" :key="category" :value="category">{{ category }}</option>
            </select>

            <!-- Bộ lọc giá -->
            <select
              :value="currentPriceRangeValue"
              @change="onPriceFilterChange"
              class="w-40 shrink-0 rounded-xl border border-slate-200 bg-slate-50 py-2.5 px-3 text-sm font-medium text-slate-900 outline-none transition focus:border-red-300 shadow-sm"
            >
              <option value="">Tất cả mức giá</option>
              <option value="0-1000000">Dưới 1 triệu</option>
              <option value="1000000-2000000">Từ 1 - 2 triệu</option>
              <option value="2000000-3000000">Từ 2 - 3 triệu</option>
              <option value="3000000-999999999">Trên 3 triệu</option>
            </select>
          </div>

          <!-- Danh sách sản phẩm (cuộn được) -->
          <div class="flex-1 overflow-y-auto">
            <div
              v-if="!loadingProducts && !productResults.length"
              class="px-4 py-16 text-center text-sm text-slate-500"
            >
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
                    v-for="(product, index) in paginatedProducts"
                    :key="`panel-${product.chiTietId || product.sanPhamId || product.id}`"
                    class="group cursor-pointer bg-white transition hover:bg-red-50/30"
                    @click="handleOpenProduct(product)"
                  >
                    <td class="px-4 py-3 text-center text-slate-500">{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                    <td class="px-4 py-3 font-medium text-slate-700">{{ product.maSanPham }}</td>
                    <td class="px-4 py-3">
                      <div
                        class="line-clamp-2 font-semibold text-slate-800 transition group-hover:text-red-500"
                        :title="product.tenSanPham"
                      >
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
