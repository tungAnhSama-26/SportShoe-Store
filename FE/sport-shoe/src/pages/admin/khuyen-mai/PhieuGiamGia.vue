<script setup>
import { usePhieuGiamGiaList } from "./usePhieuGiamGiaList";
const {
  computed,
  onMounted,
  ref,
  watch,
  useRoute,
  useRouter,
  CheckCircle2,
  CircleX,
  Eye,
  FileSpreadsheet,
  Filter,
  Plus,
  RotateCcw,
  Search,
  X,
  getPhieuGiamGiaKhachHangList,
  getPhieuGiamGiaList,
  updatePhieuGiamGia,
  updatePhieuGiamGiaKhachHang,
  AdminTableFooter,
  AdminQuickStatusAction,
  exportRowsToExcel,
  getDisplayErrorMessage,
  router,
  route,
  dangTai,
  loiTrang,
  resolveActiveTab,
  activeTab,
  toast,
  toastTimer,
  toastClass,
  toastIconClass,
  toastAccentClass,
  ToastIcon,
  hienThiThongBao,
  boLoc,
  boLocKh,
  danhSach,
  tongSoTrang,
  soPhanTuMotTrang,
  trangHienTai,
  totalItems,
  danhSachKh,
  tongSoTrangKh,
  soPhanTuMotTrangKh,
  trangHienTaiKh,
  totalItemsKh,
  dsTrangThai,
  dsLoai,
  isHetHan,
  mauTrangThai,
  statusText,
  loaiGiamText,
  loaiPhieuText,
  formatGiaTri,
  formatTien,
  toDisplayDate,
  soLuongDaDung,
  soLuongConLai,
  timer,
  taiDanhSach,
  taiDanhSachKh,
  lamMoiBoLoc,
  nhanhDoiTrangThai,
  nhanhDoiTrangThaiKh,
  openCreateModal,
  openEditModal,
  xuatExcel,
} = usePhieuGiamGiaList();
</script>

<template>
  <div class="space-y-5">
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

    <!-- Header removed -->

    <section
      class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm"
    >
      <div class="mb-5 flex items-center gap-3">
        <div
          class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500"
        >
          <Filter class="h-5 w-5" />
        </div>
        <div>
          <h2 class="admin-section-title">Bộ lọc</h2>
          <p class="text-sm text-slate-400">Tra cứu nhanh dữ liệu.</p>
        </div>
      </div>

      <div class="flex flex-col gap-6">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-5">
          <div class="space-y-2">
            <label class="admin-filter-label">Tìm kiếm</label>
            <div class="relative">
              <Search
                class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
              />
              <input
                v-model="(activeTab === 'phieu' ? boLoc : boLocKh).keyword"
                type="text"
                placeholder="Mã, tên phiếu..."
                class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>
          </div>

          <template v-if="activeTab === 'phieu'">
            <div class="space-y-2">
              <label class="admin-filter-label">Ngày bắt đầu</label>
              <input
                v-model="boLoc.tuNgay"
                type="date"
                class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>

            <div class="space-y-2">
              <label class="admin-filter-label">Ngày kết thúc</label>
              <input
                v-model="boLoc.denNgay"
                type="date"
                class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>

            <div class="space-y-2">
              <label class="admin-filter-label">Loại giảm</label>
              <select
                v-model="boLoc.loai"
                class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              >
                <option
                  v-for="loai in dsLoai"
                  :key="loai.value"
                  :value="loai.value"
                >
                  {{ loai.label }}
                </option>
              </select>
            </div>
          </template>

          <div
            class="space-y-2"
            :class="activeTab !== 'phieu' ? 'lg:col-span-2' : ''"
          >
            <label class="admin-filter-label">Trạng thái</label>
            <select
              v-model="(activeTab === 'phieu' ? boLoc : boLocKh).trangThai"
              class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            >
              <option
                v-for="trangThai in dsTrangThai"
                :key="trangThai.value"
                :value="trangThai.value"
              >
                {{ trangThai.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="flex flex-wrap items-center justify-end gap-3">
          <button
            @click="lamMoiBoLoc"
            class="inline-flex h-11 items-center gap-2 rounded-2xl border border-rose-200 bg-white px-5 text-sm font-semibold text-rose-500 shadow-[0_10px_24px_rgba(244,63,94,0.08)] transition hover:border-rose-300 hover:bg-rose-50/70 hover:text-rose-600"
          >
            <RotateCcw class="h-4 w-4" /> Đặt lại bộ lọc
          </button>
          <button
            @click="xuatExcel"
            class="inline-flex h-11 items-center gap-2 rounded-2xl border border-rose-200 bg-white px-5 text-sm font-semibold text-rose-500 shadow-[0_10px_24px_rgba(244,63,94,0.08)] transition hover:border-rose-300 hover:bg-rose-50/70 hover:text-rose-600"
          >
            <FileSpreadsheet class="h-4 w-4" /> Xuất Excel
          </button>
          <button
            @click="openCreateModal"
            class="inline-flex h-11 items-center gap-2 rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-5 text-sm font-semibold text-white shadow-[0_14px_30px_rgba(239,68,68,0.28)] transition hover:-translate-y-0.5 hover:from-rose-600 hover:to-red-500 hover:shadow-[0_18px_34px_rgba(239,68,68,0.32)]"
          >
            <Plus class="h-4 w-4" />
            {{
              activeTab === "phieu" ? "Tạo phiếu mới" : "Tặng phiếu khách hàng"
            }}
          </button>
        </div>
      </div>
    </section>

    <section
      class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm"
    >
      <div class="mb-5 flex items-center justify-between">
        <h2 class="admin-section-title text-lg">
          {{
            activeTab === "phieu"
              ? "Danh sách phiếu giảm giá"
              : "Danh sách phiếu khách hàng"
          }}
        </h2>
        <p class="text-sm font-medium text-slate-400">
          {{ activeTab === "phieu" ? totalItems : totalItemsKh }} bản ghi hiển
          thị.
        </p>
      </div>

      <div
        v-if="loiTrang"
        class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600"
      >
        {{ loiTrang }}
      </div>

      <div class="overflow-x-auto admin-table-scroll w-full">
        <table
          v-if="activeTab === 'phieu'"
          class="w-full border-separate border-spacing-y-2 text-sm"
        >
          <thead>
            <tr
              class="text-left text-sm font-bold text-slate-950 [&>th]:whitespace-nowrap"
            >
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã</th>
              <th class="bg-slate-100 px-4 py-3">Tên phiếu</th>
              <th class="bg-slate-100 px-4 py-3">Hình thức</th>
              <th class="bg-slate-100 px-4 py-3">Giá trị giảm</th>
              <th class="bg-slate-100 px-4 py-3">Ngày bắt đầu</th>
              <th class="bg-slate-100 px-4 py-3">Ngày kết thúc</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th
                class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center whitespace-nowrap"
              >
                Hành động
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">
                Đang tải...
              </td>
            </tr>
            <tr v-else-if="!danhSach.length">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">
                Không có dữ liệu.
              </td>
            </tr>
            <tr
              v-for="(item, index) in danhSach"
              :key="item.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100 transition hover:ring-slate-200"
            >
              <td class="rounded-l-2xl px-4 py-3 font-semibold">
                {{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}
              </td>
              <td class="px-4 py-3 font-semibold text-slate-900">
                {{ item.ma }}
              </td>
              <td class="px-4 py-3 text-slate-900">{{ item.ten }}</td>
              <td class="px-4 py-3 whitespace-nowrap">
                {{ loaiPhieuText(item.loaiPhieu) }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap">
                {{ formatGiaTri(item.giaTri, item.loai) }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap">
                {{ toDisplayDate(item.ngayBatDau) }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap">
                {{ toDisplayDate(item.ngayKetThuc) }}
              </td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold"
                  :class="mauTrangThai(item.trangThai, item.ngayKetThuc)"
                >
                  {{ statusText(item.trangThai, item.ngayKetThuc) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex items-center justify-center gap-3">
                  <AdminQuickStatusAction
                    :loading="false"
                    :disabled="
                      isHetHan(item.ngayKetThuc) || Number(item.trangThai) === 0
                    "
                    :disabled-title="
                      isHetHan(item.ngayKetThuc)
                        ? 'Phiếu đã hết hạn, không thể thay đổi trạng thái'
                        : 'Phiếu đã ngừng hoạt động, không thể thay đổi trạng thái'
                    "
                    :action-label="
                      Number(item.trangThai) === 1
                        ? 'Ngừng hoạt động'
                        : 'Kích hoạt'
                    "
                    :confirm-message="
                      Number(item.trangThai) === 1
                        ? 'Bạn có chắc chắn muốn ngừng hoạt động phiếu này không?'
                        : 'Bạn có chắc chắn muốn kích hoạt phiếu này không?'
                    "
                    :intent="
                      Number(item.trangThai) === 1 ? 'deactivate' : 'activate'
                    "
                    @toggle="nhanhDoiTrangThai(item)"
                  />
                  <button
                    @click="openEditModal('phieu', item)"
                    class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500"
                  >
                    <Eye class="h-5 w-5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <table
          v-else
          class="min-w-[1100px] w-full border-separate border-spacing-y-2 text-sm"
        >
          <thead>
            <tr class="text-left text-sm font-bold text-slate-950">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã phiếu</th>
              <th class="bg-slate-100 px-4 py-3">Tên phiếu</th>
              <th class="bg-slate-100 px-4 py-3">Khách hàng</th>
              <th class="bg-slate-100 px-4 py-3">Ngày tặng</th>
              <th class="bg-slate-100 px-4 py-3">Ngày dùng</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">
                Hành động
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">
                Đang tải...
              </td>
            </tr>
            <tr v-else-if="!danhSachKh.length">
              <td colspan="8" class="py-10 text-center text-sm text-slate-400">
                Không có dữ liệu.
              </td>
            </tr>
            <tr
              v-for="(item, index) in danhSachKh"
              :key="item.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100 transition hover:ring-slate-200"
            >
              <td class="rounded-l-2xl px-4 py-3 font-semibold">
                {{ (trangHienTaiKh - 1) * soPhanTuMotTrangKh + index + 1 }}
              </td>
              <td class="px-4 py-3 font-semibold text-slate-900">
                {{ item.maPhieuGiamGia }}
              </td>
              <td class="px-4 py-3 text-slate-900">
                {{ item.tenPhieuGiamGia }}
              </td>
              <td class="px-4 py-3">
                {{ item.tenKhachHang }}
              </td>
              <td class="px-4 py-3">
                {{ toDisplayDate(item.ngayTao) }}
              </td>
              <td class="px-4 py-3">
                {{
                  item.ngaySuDung
                    ? toDisplayDate(item.ngaySuDung)
                    : "Chưa sử dụng"
                }}
              </td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold"
                  :class="mauTrangThai(item.trangThai)"
                >
                  {{ statusText(item.trangThai) }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex items-center justify-center gap-3">
                  <AdminQuickStatusAction
                    :loading="false"
                    :disabled="
                      Number(item.trangThai) === 0 || isHetHan(item.ngayKetThuc)
                    "
                    :disabled-title="
                      Number(item.trangThai) === 0
                        ? 'Không thể thao tác trên phiếu đã ngừng hoạt động'
                        : 'Phiếu đã hết hạn, không thể thao tác trên phiếu này'
                    "
                    action-label="Tắt liên kết"
                    confirm-message="Bạn có chắc chắn muốn tắt liên kết này không?"
                    intent="deactivate"
                    @toggle="nhanhDoiTrangThaiKh(item)"
                  />
                  <button
                    @click="openEditModal('khach-hang', item)"
                    class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-rose-500"
                  >
                    <Eye class="h-5 w-5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminTableFooter
        :current-page="activeTab === 'phieu' ? trangHienTai : trangHienTaiKh"
        :page-size="
          activeTab === 'phieu' ? soPhanTuMotTrang : soPhanTuMotTrangKh
        "
        :page-size-options="[5, 10, 20]"
        :total-items="activeTab === 'phieu' ? totalItems : totalItemsKh"
        :total-pages="activeTab === 'phieu' ? tongSoTrang : tongSoTrangKh"
        compact
        show-refresh
        @refresh="activeTab === 'phieu' ? taiDanhSach() : taiDanhSachKh()"
        @update:current-page="
          activeTab === 'phieu'
            ? (trangHienTai = $event)
            : (trangHienTaiKh = $event)
        "
        @update:page-size="
          activeTab === 'phieu'
            ? (soPhanTuMotTrang = $event)
            : (soPhanTuMotTrangKh = $event)
        "
      />
    </section>
  </div>
</template>
