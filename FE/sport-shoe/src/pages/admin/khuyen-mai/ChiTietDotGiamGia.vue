<script setup>
import { Plus, Minus } from "lucide-vue-next";
import { resolveHinhAnh } from "../../../utils/resolve-image";
import { useChiTietDotGiamGia } from "./useChiTietDotGiamGia";
const {
  computed,
  onMounted,
  reactive,
  ref,
  watch,
  useRoute,
  useRouter,
  ArrowLeft,
  ArrowUpRight,
  CheckCircle2,
  CheckSquare,
  CircleX,
  RefreshCcw,
  Save,
  Search,
  Square,
  Tag,
  X,
  AdminTableFooter,
  createDotGiamGia,
  getDotGiamGiaDetail,
  updateDotGiamGia,
  getDotGiamGiaSanPhamList,
  syncDotGiamGiaSanPham,
  chiTietGiay,
  layDanhSachGiay,
  layBienThe,
  getDisplayErrorMessage,
  route,
  router,
  id,
  laMoi,
  dangTai,
  dangTaiSP,
  saving,
  loiTrang,
  formErrors,
  form,
  isReadOnly,
  searchSP,
  danhSachSP,
  spTrang,
  selectedVariants,
  blockedVariantIds,
  trangBienThe,
  soHangMoiTrang,
  pageSizeOptions,
  tatCaBienThe,
  tongSoTrang,
  bienTheTrang,
  getToday,
  resetErrors,
  formatCurrency,
  resolveProductImage,
  normalizeVariantForSelection,
  hopNhatBienThe,
  dedupeSelectedVariants,
  dongBoBienTheDaChonTheoDanhSachSanPham,
  taiSanPhamDaChonConThieu,
  tinhGiaGiam,
  taoMaNgauNhien,
  taiDanhSachSP,
  searchTimer,
  isVariantSelected,
  isVariantBlocked,
  tatCaCoTheChon,
  tatCaDaChon,
  motSoDaChon,
  isProductBlocked,
  getProductSelectState,
  toggleProduct,
  toggleChonTatCa,
  toggleVariant,
  removeSelectedVariant,
  expandedProducts,
  toggleProductExpansion,
  taiChiTiet,
  submitForm,
  filterMauSac,
  filterKichCo,
  danhMuc,
  danhSachSPSauKhiLoc,
  todayStr,
  isNgayBatDauReadOnly,
  onlyAllowDigitsKey,
} = useChiTietDotGiamGia();

const searchSelectedText = ref("");
const filterSelectedMauSac = ref("");
const filterSelectedKichCo = ref("");

// Phân trang cho bảng đã chọn
const trangDaChon = ref(1);
const soHangDaChonMoiTrang = ref(10);
const pageSizeDaChonOptions = [5, 10, 20, 50];

const filteredSelectedVariants = computed(() => {
  let result = selectedVariants.value;

  if (filterSelectedMauSac.value) {
    result = result.filter(bt => String(bt.mauSacId) === String(filterSelectedMauSac.value));
  }

  if (filterSelectedKichCo.value) {
    result = result.filter(bt => String(bt.kichCoId) === String(filterSelectedKichCo.value));
  }

  if (searchSelectedText.value.trim()) {
    const keyword = searchSelectedText.value.trim().toLowerCase();
    result = result.filter((bt) => {
      const productName = (bt.tenSanPham || "").toLowerCase();
      const productCode = (bt.maSanPham || "").toLowerCase();
      const variantCode = (bt.maBienThe || bt.sku || "").toLowerCase();
      return (
        productName.includes(keyword) ||
        productCode.includes(keyword) ||
        variantCode.includes(keyword)
      );
    });
  }

  return result;
});

// Reset trang về 1 khi filter/search thay đổi
watch([searchSelectedText, filterSelectedMauSac, filterSelectedKichCo], () => {
  trangDaChon.value = 1;
});

const tongSoTrangDaChon = computed(() =>
  Math.max(1, Math.ceil(filteredSelectedVariants.value.length / soHangDaChonMoiTrang.value))
);

const filteredSelectedVariantsTrang = computed(() => {
  const start = (trangDaChon.value - 1) * soHangDaChonMoiTrang.value;
  return filteredSelectedVariants.value.slice(start, start + soHangDaChonMoiTrang.value);
});
</script>

<template>
  <div class="space-y-5 pb-10 radius-6px">
    <!-- Toast Notification -->


    <section class="flex items-center gap-4 border-b border-slate-100 pb-4">
      <button
        @click="router.push({ name: 'admin-dot-giam-gia' })"
        class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-4 w-4" />
      </button>
    </section>

    <div
      v-if="loiTrang"
      class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600"
    >
      {{ loiTrang }}
    </div>

    <div
      v-if="isReadOnly"
      class="rounded-2xl border border-amber-100 bg-amber-50 px-5 py-3 text-sm font-medium text-amber-700"
    >
      Đợt giảm giá này đã hết hạn hoặc ngừng hoạt động nên chỉ có thể xem chi
      tiết.
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-12 gap-6">
      <!-- Cột trái: Thông tin đợt giảm -->
      <div class="xl:col-span-4 space-y-6">
        <section
          class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6"
        >
          <fieldset :disabled="isReadOnly" class="space-y-6">
            <div class="flex items-center gap-3">
              <div
                class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500"
              >
                <Tag class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">
                  Thông tin đợt giảm
                </h2>
              </div>
            </div>

            <div class="space-y-4">
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500"
                  >Mã đợt <span class="text-rose-500">*</span></label
                >
                <div class="relative">
                  <input
                    v-model="form.ma"
                    :readonly="!laMoi"
                    class="h-11 w-full rounded-2xl border border-slate-200 pl-4 pr-11 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
                    :class="!laMoi ? 'cursor-not-allowed bg-slate-100 text-slate-500' : 'bg-slate-50'"
                    placeholder="Ví dụ: SUMMER2024"
                  />
                  <button
                    v-if="laMoi && !isReadOnly"
                    @click="taoMaNgauNhien"
                    type="button"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-rose-500 transition-colors"
                  >
                    <RefreshCcw class="h-4 w-4" />
                  </button>
                </div>
                <p v-if="formErrors.ma" class="text-xs text-rose-500 mt-1">
                  {{ formErrors.ma }}
                </p>
              </div>

              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500"
                  >Tên đợt <span class="text-rose-500">*</span></label
                >
                <input
                  v-model="form.ten"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
                  placeholder="Ví dụ: Siêu giảm giá mùa hè"
                />
                <p v-if="formErrors.ten" class="text-xs text-rose-500 mt-1">
                  {{ formErrors.ten }}
                </p>
              </div>

              <div class="grid grid-cols-1 gap-4">
                <div class="space-y-2">
                  <label class="text-[13px] font-semibold text-slate-500"
                    >Giá trị giảm (%)
                    <span class="text-rose-500">*</span></label
                  >
                  <div class="relative">
                    <input
                      v-model="form.giaTriGiam"
                      type="text"
                      inputmode="numeric"
                      @keydown="onlyAllowDigitsKey"
                      class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-10 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
                      placeholder="0"
                    />
                    <span
                      class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 font-bold"
                      >%</span
                    >
                  </div>
                  <p
                    v-if="formErrors.giaTriGiam"
                    class="text-xs text-rose-500 mt-1"
                  >
                    {{ formErrors.giaTriGiam }}
                  </p>
                  <p v-else-if="Number(form.giaTriGiam) === 100" class="text-xs text-emerald-600 font-medium">
                    ✓ Sản phẩm sẽ miễn phí hoàn toàn (giảm 100%)
                  </p>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div class="space-y-2">
                  <label class="text-[13px] font-semibold text-slate-500"
                    >Từ ngày <span class="text-rose-500">*</span></label
                  >
                  <input
                    v-model="form.ngayBatDau"
                    type="date"
                    :min="isNgayBatDauReadOnly ? undefined : todayStr"
                    :readonly="isNgayBatDauReadOnly"
                    class="h-11 w-full rounded-2xl border border-slate-200 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
                    :class="
                      isNgayBatDauReadOnly
                        ? 'cursor-not-allowed bg-slate-100 text-slate-500'
                        : 'bg-slate-50'
                    "
                  />
                  <p v-if="isNgayBatDauReadOnly && !laMoi" class="mt-1 text-xs text-amber-600 font-medium">
                    Đợt giảm giá đang hoạt động không thể thay đổi ngày bắt đầu
                  </p>
                  <p
                    v-if="formErrors.ngayBatDau"
                    class="text-xs text-rose-500 mt-1"
                  >
                    {{ formErrors.ngayBatDau }}
                  </p>
                </div>
                <div class="space-y-2">
                  <label class="text-[13px] font-semibold text-slate-500"
                    >Đến ngày <span class="text-rose-500">*</span></label
                  >
                  <input
                    v-model="form.ngayKetThuc"
                    type="date"
                    :min="form.ngayBatDau || undefined"
                    class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
                  />
                  <p
                    v-if="formErrors.ngayKetThuc"
                    class="text-xs text-rose-500 mt-1"
                  >
                    {{ formErrors.ngayKetThuc }}
                  </p>
                </div>
              </div>

              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500"
                  >Mô tả</label
                >
                <textarea
                  v-model="form.moTa"
                  rows="3"
                  class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
                  placeholder="Nhập mô tả..."
                ></textarea>
                <p v-if="formErrors.moTa" class="text-xs text-rose-500">
                  {{ formErrors.moTa }}
                </p>
              </div>
            </div>
          </fieldset>

          <div class="pt-4 flex flex-col gap-3">
            <button
              v-if="!isReadOnly"
              @click="submitForm"
              :disabled="saving"
              class="w-full inline-flex items-center justify-center gap-2 rounded-2xl bg-rose-500 px-6 py-3 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60"
            >
              <Save class="h-4 w-4" />
              {{
                saving
                  ? "Đang lưu..."
                  : laMoi
                    ? "Tạo đợt giảm giá"
                    : "Lưu thay đổi"
              }}
            </button>
            <button
              @click="router.push({ name: 'admin-dot-giam-gia' })"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-6 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-100"
            >
              {{ isReadOnly ? "Quay lại" : "Hủy" }}
            </button>
          </div>
        </section>
      </div>

      <!-- Cột phải: Chọn sản phẩm -->
      <div class="xl:col-span-8 space-y-6">
        <section
          class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-5"
        >
          <!-- Header -->
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div
                class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600"
              >
                <Search class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">
                  Chọn sản phẩm áp dụng
                </h2>
                <p class="text-[13px] text-slate-400">
                  Đã chọn {{ selectedVariants.length }} biến thể
                </p>
                <p
                  v-if="formErrors.giayChiTietIds"
                  class="mt-1 text-xs text-rose-500"
                >
                  {{ formErrors.giayChiTietIds }}
                </p>
              </div>
            </div>
          </div>

          <!-- Tìm kiếm và Lọc -->
          <div class="flex flex-col sm:flex-row items-end gap-3">
            <div class="relative flex-1 w-full">
              <Search
                class="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
              />
              <input
                v-model="searchSP"
                :disabled="isReadOnly"
                type="text"
                placeholder="Tìm theo tên hoặc mã sản phẩm..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-white pl-11 pr-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 disabled:opacity-70 disabled:bg-slate-100"
              />
            </div>
            
            <div class="flex gap-3 w-full sm:w-auto">
              <div class="space-y-1.5 flex-1 sm:flex-none">
                <label class="text-[13px] font-semibold text-slate-500">Màu sắc</label>
                <select
                  v-model="filterMauSac"
                  :disabled="isReadOnly"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-white px-4 text-sm font-normal text-slate-700 outline-none transition focus:border-rose-300 disabled:opacity-70 disabled:bg-slate-100 min-w-[140px]"
                >
                  <option value="">Tất cả màu sắc</option>
                  <option v-for="mau in danhMuc.mauSac" :key="mau.id" :value="mau.id">
                    {{ mau.ten }}
                  </option>
                </select>
              </div>

              <div class="space-y-1.5 flex-1 sm:flex-none">
                <label class="text-[13px] font-semibold text-slate-500">Kích cỡ</label>
                <select
                  v-model="filterKichCo"
                  :disabled="isReadOnly"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-white px-4 text-sm font-normal text-slate-700 outline-none transition focus:border-rose-300 disabled:opacity-70 disabled:bg-slate-100 min-w-[140px]"
                >
                  <option value="">Tất cả kích cỡ</option>
                  <option v-for="size in danhMuc.kichCo" :key="size.id" :value="size.id">
                    {{ size.giaTri }}
                  </option>
                </select>
              </div>

              <button
                v-if="!isReadOnly"
                @click="taiDanhSachSP"
                class="mb-0 h-11 inline-flex items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-medium text-white transition hover:bg-rose-600 shrink-0"
              >
                <Search class="h-4 w-4" />
                Tìm kiếm
              </button>
            </div>
          </div>

          <!-- Danh sách biến thể - 1 bảng duy nhất -->
          <div>
            <div v-if="dangTaiSP" class="py-10 text-center text-slate-400">
              Đang tải sản phẩm...
            </div>
            <div
              v-else-if="!tatCaBienThe.length"
              class="py-10 text-center text-slate-400"
            >
              Không tìm thấy sản phẩm nào.
            </div>
            <template v-else>
              <table class="w-full text-sm">
                <thead>
                  <tr
                    class="text-[12px] font-semibold text-slate-500 border-b border-slate-100 bg-slate-50 whitespace-nowrap"
                  >
                    <th class="px-4 py-3 text-left w-12 rounded-tl-xl">
                      <input
                        type="checkbox"
                        class="h-4 w-4 rounded border-slate-400 text-rose-500 focus:ring-rose-500 cursor-pointer"
                        :checked="tatCaDaChon"
                        :indeterminate="motSoDaChon"
                        :disabled="isReadOnly || tatCaCoTheChon.length === 0"
                        @change="toggleChonTatCa"
                      />
                    </th>
                    <th class="px-4 py-3 text-left">STT</th>
                    <th class="px-4 py-3 text-left">Mã SP</th>
                    <th class="px-4 py-3 text-left">Tên sản phẩm</th>
                    <th class="px-4 py-3 text-left w-12 rounded-tr-xl"></th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="(sp, idx) in spTrang" :key="sp.id">
                    <!-- Product Row -->
                    <tr
                      class="border-b border-slate-100 last:border-0 transition bg-white"
                      :class="[
                        isProductBlocked(sp)
                          ? 'opacity-40'
                          : 'hover:bg-slate-50',
                      ]"
                    >
                      <td class="px-4 py-3 text-left align-middle">
                        <button
                          :disabled="
                            isReadOnly || getProductSelectState(sp).disabled
                          "
                          @click="toggleProduct(sp)"
                          class="flex items-center justify-start disabled:cursor-not-allowed"
                        >
                          <CheckSquare
                            v-if="getProductSelectState(sp).checked"
                            class="h-4 w-4 text-rose-500"
                          />
                          <div
                            v-else-if="getProductSelectState(sp).indeterminate"
                            class="h-4 w-4 rounded border border-rose-500 bg-rose-500 flex items-center justify-center"
                          >
                            <div class="h-0.5 w-2 bg-white rounded-full"></div>
                          </div>
                          <Square v-else class="h-4 w-4 text-slate-500 hover:text-slate-600 transition-colors" />
                        </button>
                      </td>
                      <td
                        class="px-4 py-3 text-left text-slate-400 font-medium whitespace-nowrap"
                      >
                        {{ (trangBienThe - 1) * soHangMoiTrang + idx + 1 }}
                      </td>
                      <td
                        class="px-4 py-3 text-left text-slate-500 font-medium whitespace-nowrap"
                      >
                        {{ sp.ma }}
                      </td>
                      <td class="px-4 py-3 text-left text-slate-600 whitespace-nowrap">
                        {{ sp.ten }}
                      </td>
                      <td class="px-4 py-3 text-left align-middle">
                        <button
                          type="button"
                          @click="toggleProductExpansion(sp.id)"
                          class="text-slate-500 hover:text-slate-800 focus:outline-none flex items-center justify-start w-full transition-colors"
                        >
                          <Plus
                            v-if="!expandedProducts.has(sp.id)"
                            class="w-4 h-4"
                          />
                          <Minus v-else class="w-4 h-4" />
                        </button>
                      </td>
                    </tr>

                    <!-- Variants Rows -->
                    <template v-if="expandedProducts.has(sp.id)">
                      <tr class="bg-slate-50/50 border-b border-slate-200">
                        <td colspan="5" class="p-0">
                          <div class="pl-8 pr-4 py-3">
                            <table class="w-full text-[13px]">
                              <thead
                                class="text-slate-800 text-[12px] font-semibold whitespace-nowrap"
                              >
                                <tr>
                                  <th class="px-3 py-2 text-left font-medium">
                                    Chọn
                                  </th>
                                  <th class="px-3 py-2 text-left font-medium">
                                    Ảnh
                                  </th>
                                  <th class="px-3 py-2 text-left font-medium">
                                    Mã biến thể
                                  </th>
                                  <th class="px-3 py-2 text-left font-medium">
                                    Màu sắc
                                  </th>
                                  <th class="px-3 py-2 text-left font-medium">
                                    Kích cỡ
                                  </th>
                                  <th class="px-3 py-2 text-left font-medium">
                                    Số lượng
                                  </th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr
                                  v-for="bt in sp.bienThes"
                                  :key="bt.id"
                                  class="border-b border-slate-100 last:border-0 transition whitespace-nowrap"
                                  :class="[
                                    isVariantBlocked(bt.id || bt.giayChiTietId)
                                      ? 'opacity-40 pointer-events-none'
                                      : isVariantSelected(
                                            bt.id || bt.giayChiTietId,
                                          )
                                        ? 'bg-rose-50/30'
                                        : 'hover:bg-white',
                                  ]"
                                >
                                  <td class="px-3 py-2 text-left">
                                    <button
                                      :disabled="
                                        isReadOnly ||
                                        isVariantBlocked(
                                          bt.id || bt.giayChiTietId,
                                        )
                                      "
                                      @click="toggleVariant(bt, sp)"
                                      class="flex items-center justify-start disabled:cursor-not-allowed"
                                    >
                                      <CheckSquare
                                        v-if="
                                          isVariantSelected(
                                            bt.id || bt.giayChiTietId,
                                          )
                                        "
                                        class="h-4 w-4 text-rose-500"
                                      />
                                      <Square
                                        v-else
                                        class="h-4 w-4 text-slate-500 hover:text-slate-600 transition-colors"
                                      />
                                    </button>
                                  </td>
                                  <td class="px-3 py-2 text-left">
                                    <div
                                      class="h-8 w-8 rounded bg-white overflow-hidden border border-slate-200"
                                    >
                                      <img
                                        v-if="bt.hinhAnh || sp.hinhAnh"
                                        :src="resolveHinhAnh(bt.hinhAnh || sp.hinhAnh)"
                                        class="h-full w-full object-cover"
                                      />
                                    </div>
                                  </td>
                                  <td
                                    class="px-3 py-2 text-left text-slate-500 font-medium"
                                  >
                                    {{ bt.sku || bt.maBienThe || sp.ten }}
                                  </td>
                                  <td class="px-3 py-2 text-left text-slate-600">
                                    {{ bt.mauSac }}
                                  </td>
                                  <td class="px-3 py-2 text-left text-slate-600">
                                    {{ bt.kichCo }}
                                  </td>
                                  <td class="px-3 py-2 text-left">
                                    <span
                                      class="inline-flex items-center text-[13px] font-medium text-slate-700"
                                    >
                                      {{ bt.soLuong || 0 }}
                                    </span>
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </div>
                        </td>
                      </tr>
                    </template>
                  </template>
                </tbody>
              </table>

              <!-- Phân trang -->
              <AdminTableFooter
                :current-page="trangBienThe"
                :page-size="soHangMoiTrang"
                :page-size-options="pageSizeOptions"
                :total-items="danhSachSPSauKhiLoc.length"
                :total-pages="tongSoTrang"
                compact
                @update:current-page="trangBienThe = $event"
                @update:page-size="
                  soHangMoiTrang = $event;
                  trangBienThe = 1;
                "
              />
            </template>
          </div>
        </section>
      </div>
    </div>

    <!-- Bảng sản phẩm & biến thể đã chọn ở dưới cùng -->
    <section
      class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-5"
    >
      <div
        class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4"
      >
        <div class="flex items-center gap-3">
          <div
            class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500"
          >
            <CheckSquare class="h-5 w-5" />
          </div>
          <div>
            <h2 class="text-base font-bold text-slate-800">
              Sản phẩm & biến thể đã chọn áp dụng
            </h2>
            <p class="text-[13px] text-slate-400">
              Danh sách chi tiết gồm {{ selectedVariants.length }} biến thể đã
              chọn
            </p>
          </div>
        </div>

        <div v-if="selectedVariants.length > 0" class="flex flex-col sm:flex-row items-end gap-3 w-full sm:w-auto">
          <div class="space-y-1.5 flex-1 sm:flex-none">
            <label class="text-[13px] font-semibold text-slate-500">Màu sắc</label>
            <select
              v-model="filterSelectedMauSac"
              :disabled="isReadOnly"
              class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-3 text-sm font-normal text-slate-700 outline-none transition focus:border-rose-300 disabled:opacity-70 min-w-[130px]"
            >
              <option value="">Tất cả màu sắc</option>
              <option v-for="mau in danhMuc.mauSac" :key="mau.id" :value="mau.id">
                {{ mau.ten }}
              </option>
            </select>
          </div>

          <div class="space-y-1.5 flex-1 sm:flex-none">
            <label class="text-[13px] font-semibold text-slate-500">Kích cỡ</label>
            <select
              v-model="filterSelectedKichCo"
              :disabled="isReadOnly"
              class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-3 text-sm font-normal text-slate-700 outline-none transition focus:border-rose-300 disabled:opacity-70 min-w-[130px]"
            >
              <option value="">Tất cả kích cỡ</option>
              <option v-for="size in danhMuc.kichCo" :key="size.id" :value="size.id">
                {{ size.giaTri }}
              </option>
            </select>
          </div>

          <div class="relative w-full sm:w-64">
            <Search
              class="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
            />
            <input
              v-model="searchSelectedText"
              type="text"
              placeholder="Tìm trong danh sách..."
              class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-10 pr-4 text-sm text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
            />
          </div>
        </div>
      </div>

      <div>
        <div
          v-if="!selectedVariants.length"
          class="flex flex-col items-center justify-center py-12 text-slate-400 space-y-3"
        >
          <div class="rounded-full bg-slate-50 p-4">
            <Tag class="h-8 w-8 text-slate-300" />
          </div>
          <p class="text-sm font-medium">
            Chưa có sản phẩm hoặc biến thể nào được chọn.
          </p>
          <p class="text-xs text-slate-400">
            Hãy chọn các biến thể ở bảng tìm kiếm phía trên.
          </p>
        </div>

        <div
          v-else-if="!filteredSelectedVariants.length"
          class="text-center py-10 text-slate-400 text-sm"
        >
          Không tìm thấy sản phẩm đã chọn nào khớp với từ khóa tìm kiếm.
        </div>

        <div
          v-else
          class="overflow-x-auto admin-table-scroll pr-1"
        >
          <table
            class="w-full text-xs border-separate border-spacing-y-2"
          >
            <thead>
              <tr
                class="text-[11px] font-semibold text-slate-500 border-b border-slate-100 bg-slate-50 whitespace-nowrap"
              >
                <th class="px-3 py-3 text-left rounded-l-xl w-10">STT</th>
                <th class="px-3 py-3 text-left w-[32%]">Sản phẩm</th>
                <th class="px-3 py-3 text-left w-[22%]">Biến thể</th>
                <th class="px-3 py-3 text-left w-[14%]">Giá bán</th>
                <th class="px-3 py-3 text-left w-[14%]">Giá sau giảm</th>
                <th class="px-3 py-3 text-left w-[8%]">Số lượng</th>
                <th class="px-3 py-3 text-left rounded-r-xl w-12">
                  <span v-if="!isReadOnly">Xóa</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(bt, index) in filteredSelectedVariantsTrang"
                :key="bt.id"
                class="bg-white text-slate-950 shadow-sm ring-1 ring-slate-100 transition hover:ring-slate-200 [&>td]:align-middle"
              >
                <td class="rounded-l-xl px-3 py-3 text-left font-semibold text-slate-400">
                  {{ (trangDaChon - 1) * soHangDaChonMoiTrang + index + 1 }}
                </td>
                <!-- Cột Sản phẩm: Ảnh + Tên + Mã SP -->
                <td class="px-3 py-3">
                  <div class="flex items-center gap-2.5 min-w-0">
                    <div class="h-9 w-9 shrink-0 rounded-lg bg-slate-50 overflow-hidden border border-slate-200 flex items-center justify-center">
                      <img
                        v-if="bt.hinhAnh"
                        :src="resolveHinhAnh(bt.hinhAnh)"
                        class="h-full w-full object-cover"
                      />
                      <Tag v-else class="h-3.5 w-3.5 text-slate-300" />
                    </div>
                    <div class="min-w-0">
                      <p class="font-bold text-slate-800 truncate text-[12px] leading-snug">{{ bt.tenSanPham }}</p>
                      <p class="text-[11px] text-slate-400 font-medium mt-0.5">{{ bt.maSanPham || "—" }}</p>
                    </div>
                  </div>
                </td>
                <!-- Cột Biến thể: Mã BT + Màu + Kích cỡ -->
                <td class="px-3 py-3">
                  <div class="flex flex-col gap-1 min-w-0">
                    <span class="text-[11px] font-semibold text-slate-500 truncate">{{ bt.maBienThe || bt.sku || "—" }}</span>
                    <div class="flex items-center gap-1.5 flex-wrap">
                      <span v-if="bt.mauSac" class="inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-medium text-slate-600">
                        {{ bt.mauSac }}
                      </span>
                      <span v-if="bt.kichCo" class="inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-medium text-slate-600">
                        {{ bt.kichCo }}
                      </span>
                    </div>
                  </div>
                </td>
                <!-- Giá bán -->
                <td class="px-3 py-3 text-left font-semibold text-slate-500 whitespace-nowrap">
                  {{ formatCurrency(bt.giaBan) }}
                </td>
                <!-- Giá sau giảm -->
                <td class="px-3 py-3 text-left font-bold text-rose-600 whitespace-nowrap">
                  {{ formatCurrency(tinhGiaGiam(bt.giaBan)) }}
                </td>
                <!-- Số lượng -->
                <td class="px-3 py-3 text-left font-semibold text-slate-700">
                  <span class="inline-flex items-center justify-center rounded-lg bg-slate-50 border border-slate-200 min-w-[40px] px-2 py-1 text-[12px]">
                    {{ bt.soLuong || 0 }}
                  </span>
                </td>
                <!-- Xóa -->
                <td class="rounded-r-xl px-3 py-3 text-left">
                  <button
                    v-if="!isReadOnly"
                    type="button"
                    @click="removeSelectedVariant(bt.id)"
                    class="inline-flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 hover:bg-rose-50 hover:text-rose-500 transition-colors"
                    title="Xóa biến thể khỏi danh sách áp dụng"
                  >
                    <X class="h-4 w-4" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Phân trang bảng đã chọn -->
        <AdminTableFooter
          :current-page="trangDaChon"
          :page-size="soHangDaChonMoiTrang"
          :page-size-options="pageSizeDaChonOptions"
          :total-items="filteredSelectedVariants.length"
          :total-pages="tongSoTrangDaChon"
          compact
          @update:current-page="trangDaChon = $event"
          @update:page-size="soHangDaChonMoiTrang = $event; trangDaChon = 1"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}
table {
  border-collapse: separate;
  border-spacing: 0 8px;
}
</style>
