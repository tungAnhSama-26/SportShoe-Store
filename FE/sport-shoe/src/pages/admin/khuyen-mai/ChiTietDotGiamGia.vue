<script setup>
import { useChiTietDotGiamGia } from "./useChiTietDotGiamGia";
const { computed, onMounted, reactive, ref, watch, useRoute, useRouter, ArrowLeft, ArrowUpRight, CheckCircle2, CheckSquare, CircleX, RefreshCcw, Save, Search, Square, Tag, X, AdminTableFooter, createDotGiamGia, getDotGiamGiaDetail, updateDotGiamGia, getDotGiamGiaSanPhamList, syncDotGiamGiaSanPham, chiTietGiay, layDanhSachGiay, layBienThe, getDisplayErrorMessage, route, router, id, laMoi, dangTai, dangTaiSP, saving, loiTrang, toast, toastTimer, toastClass, toastIconClass, toastAccentClass, ToastIcon, hienThiThongBao, formErrors, form, isReadOnly, searchSP, danhSachSP, selectedVariants, blockedVariantIds, trangBienThe, soHangMoiTrang, pageSizeOptions, tatCaBienThe, tongSoTrang, bienTheTrang, getToday, resetErrors, formatCurrency, resolveProductImage, normalizeVariantForSelection, hopNhatBienThe, dedupeSelectedVariants, dongBoBienTheDaChonTheoDanhSachSanPham, taiSanPhamDaChonConThieu, tinhGiaGiam, taoMaNgauNhien, taiDanhSachSP, searchTimer, isVariantSelected, isVariantBlocked, tatCaCoTheChon, tatCaDaChon, motSoDaChon, toggleChonTatCa, toggleVariant, removeSelectedVariant, expandedProducts, toggleProductExpansion, taiChiTiet, submitForm } = useChiTietDotGiamGia();
</script>

<template>
  <div class="space-y-5 pb-10">
    <!-- Toast Notification -->
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="translate-y-3 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-3 opacity-0"
    >
      <div
        v-if="toast.hienThi"
        class="fixed right-5 top-5 z-[70] w-[360px] max-w-[calc(100vw-2rem)] overflow-hidden rounded-2xl border bg-white shadow-[0_18px_60px_rgba(15,23,42,0.18)]"
        :class="toastClass"
      >
        <div class="flex gap-3 p-4">
          <div
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full"
            :class="toastIconClass"
          >
            <component :is="ToastIcon" class="h-5 w-5" />
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-sm font-bold text-slate-800">{{ toast.tieuDe }}</p>
            <p
              v-if="toast.noiDung"
              class="mt-1 text-sm leading-5 text-slate-600"
            >
              {{ toast.noiDung }}
            </p>
          </div>
          <button
            type="button"
            class="rounded-full p-1 text-slate-400 transition hover:bg-white/70 hover:text-slate-600"
            @click="toast.hienThi = false"
          >
            <X class="h-4 w-4" />
          </button>
        </div>
        <div class="h-1.5 w-full" :class="toastAccentClass"></div>
      </div>
    </Transition>

    <!-- Header -->
    <section class="flex items-center gap-4">
      <button
        @click="router.push({ name: 'admin-dot-giam-gia' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div class="flex-1 min-w-0">
        <div>
          <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
            {{ laMoi ? "Thêm đợt giảm giá mới" : "Chi tiết đợt giảm giá" }}
          </h1>
        </div>
      </div>
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
                    class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-4 pr-11 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white disabled:opacity-70 disabled:bg-slate-100"
                    placeholder="Ví dụ: SUMMER2024"
                  />
                  <button
                    v-if="!isReadOnly"
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
                      type="number"
                      min="1"
                      max="100"
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
                    class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
                  />
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
                    :min="form.ngayBatDau || getToday()"
                    type="date"
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
        <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-5">
          <!-- Header -->
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div
                class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600"
              >
                <Search class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">Chọn sản phẩm áp dụng</h2>
                <p class="text-[13px] text-slate-400">Đã chọn {{ selectedVariants.length }} biến thể</p>
              </div>
            </div>
          </div>

          <!-- Tìm kiếm -->
          <div class="flex gap-3">
            <div class="relative flex-1">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-rose-500" />
              <input v-model="searchSP" :disabled="isReadOnly" @keyup.enter="taiDanhSachSP" type="text" placeholder="Tìm theo tên hoặc mã sản phẩm..." class="h-11 w-full rounded-2xl border border-rose-100 bg-rose-50/40 pl-11 pr-4 text-sm text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white disabled:opacity-70 disabled:bg-slate-100" />
            </div>
            <button v-if="!isReadOnly" @click="taiDanhSachSP" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-medium text-white transition hover:bg-rose-600">
              <Search class="h-4 w-4" />
              Tìm kiếm
            </button>
          </div>

          <!-- Danh sách biến thể - 1 bảng duy nhất -->
          <div>
            <div v-if="dangTaiSP" class="py-10 text-center text-slate-400">Đang tải sản phẩm...</div>
            <div v-else-if="!tatCaBienThe.length" class="py-10 text-center text-slate-400">Không tìm thấy sản phẩm nào.</div>
            <template v-else>
              <table class="w-full text-sm">
                <thead>
                  <tr class="text-[11px] font-semibold text-slate-400 border-b border-slate-100">
                    <th class="px-3 py-2 text-left w-8">
                      <input
                        type="checkbox"
                        class="h-3.5 w-3.5 accent-rose-500 cursor-pointer"
                        :checked="tatCaDaChon"
                        :indeterminate="motSoDaChon"
                        :disabled="isReadOnly || tatCaCoTheChon.length === 0"
                        @change="toggleChonTatCa"
                      />
                    </th>
                    <th class="px-3 py-2 text-left w-8">STT</th>
                    <th class="px-3 py-2 text-left w-12">Ảnh</th>
                    <th class="px-3 py-2 text-left">Tên sản phẩm</th>
                    <th class="px-3 py-2 text-left">Màu sắc</th>
                    <th class="px-3 py-2 text-left">Kích cỡ</th>
                    <th class="px-3 py-2 text-left">Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(bt, idx) in bienTheTrang" :key="bt.id"
                    class="border-b border-slate-50 last:border-0 transition"
                    :class="[
                      isVariantBlocked(bt.id) ? 'opacity-40 pointer-events-none bg-slate-50' :
                      isVariantSelected(bt.id) ? 'bg-rose-50/30' :
                      'hover:bg-slate-50'
                    ]"
                  >
                    <td class="px-3 py-2.5">
                      <button :disabled="isReadOnly || isVariantBlocked(bt.id)" @click="toggleVariant(bt, bt._sp)" class="flex items-center justify-center disabled:cursor-not-allowed">
                        <CheckSquare v-if="isVariantSelected(bt.id)" class="h-4 w-4 text-rose-500" />
                        <Square v-else class="h-4 w-4 text-slate-300" />
                      </button>
                    </td>
                    <td class="px-3 py-2.5 text-slate-400 text-xs">{{ (trangBienThe - 1) * soHangMoiTrang + idx + 1 }}</td>
                    <td class="px-3 py-2.5">
                      <div class="h-9 w-9 rounded-lg bg-slate-100 overflow-hidden border border-slate-100">
                        <img v-if="bt.hinhAnh || bt._sp?.hinhAnh" :src="bt.hinhAnh || bt._sp?.hinhAnh" class="h-full w-full object-cover" />
                      </div>
                    </td>
                    <td class="px-3 py-2.5 text-slate-600">{{ bt._sp?.ten || bt.tenSanPham }}</td>
                    <td class="px-3 py-2.5 text-slate-600">{{ bt.mauSac }}</td>
                    <td class="px-3 py-2.5 text-slate-600">{{ bt.kichCo }}</td>
                    <td class="px-3 py-2.5">
                      <span class="inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-medium"
                        :class="bt.soLuong > 0 ? 'bg-emerald-50 text-emerald-600 border border-emerald-100' : 'bg-slate-100 text-slate-400'">
                        {{ bt.soLuong > 0 ? 'Còn hàng' : 'Hết hàng' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>

              <!-- Phân trang -->
              <AdminTableFooter
                :current-page="trangBienThe"
                :page-size="soHangMoiTrang"
                :page-size-options="pageSizeOptions"
                :total-items="tatCaBienThe.length"
                :total-pages="tongSoTrang"
                compact
                @update:current-page="trangBienThe = $event"
                @update:page-size="soHangMoiTrang = $event; trangBienThe = 1"
              />
            </template>
          </div>
        </section>
      </div>
    </div>
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
