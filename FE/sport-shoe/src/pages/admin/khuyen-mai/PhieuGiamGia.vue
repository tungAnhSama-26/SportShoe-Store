<script setup>
import Button from "../../../components/ui/Button.vue";
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
  dsLoaiPhieu,
  isHetHan,
  mauTrangThai,
  statusText,
  statusTextKh,
  mauTrangThaiKh,
  loaiGiamText,
  loaiPhieuText,
  mauLoaiPhieu,
  Globe,
  User,
  formatGiaTri,
  formatTien,
  toDisplayDate,
  soLuongDaDung,
  timer,
  taiDanhSach,
  taiDanhSachKh,
  lamMoiBoLoc,
  nhanhDoiTrangThai,
  nhanhDoiTrangThaiKh,
  openCreateModal,
  openEditModal,
  xuatExcel,
  todayStr,
} = usePhieuGiamGiaList();
</script>

<template>
  <div class="w-full min-w-0 space-y-5 radius-6px">


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
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
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
              <label class="admin-filter-label">Hình thức</label>
              <select
                v-model="boLoc.loaiPhieu"
                class="admin-field h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              >
                <option
                  v-for="item in dsLoaiPhieu"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </option>
              </select>
            </div>

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
                :min="boLoc.tuNgay || undefined"
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
          <Button
            variant="soft"
            @click="lamMoiBoLoc"
          >
            <template #prefix><RotateCcw class="h-4 w-4" /></template> Đặt lại bộ lọc
          </Button>
          <Button
            variant="soft"
            @click="xuatExcel"
          >
            <template #prefix><FileSpreadsheet class="h-4 w-4" /></template> Xuất Excel
          </Button>
          <Button
            variant="primary"
            @click="openCreateModal"
          >
            <template #prefix><Plus class="h-4 w-4" /></template>
            {{
              activeTab === "phieu" ? "Tạo phiếu mới" : "Tặng phiếu khách hàng"
            }}
          </Button>
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

      <div class="w-full overflow-hidden rounded-[6px] border border-slate-200 bg-white shadow-sm">
        <div class="overflow-x-auto admin-table-scroll">
          <table
            v-if="activeTab === 'phieu'"
            class="w-full border-collapse text-left text-sm text-slate-600"
          >
            <thead class="bg-slate-50 border-b border-slate-200 text-slate-700 font-semibold">
              <tr
                class="text-left text-sm font-bold text-slate-950 [&>th]:whitespace-nowrap"
              >
                <th class="px-4 py-3">STT</th>
                <th class="px-4 py-3">Mã</th>
                <th class="px-4 py-3">Tên phiếu</th>
                <th class="px-4 py-3">Hình thức</th>
                <th class="px-4 py-3">Giá trị giảm</th>
                <th class="px-4 py-3">Ngày bắt đầu</th>
                <th class="px-4 py-3">Ngày kết thúc</th>
                <th class="px-4 py-3">Trạng thái</th>
                <th class="px-4 py-3 text-center whitespace-nowrap">
                  Hành động
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
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
                v-else
                v-for="(item, index) in danhSach"
                :key="item.id"
                class="transition hover:bg-slate-50/50"
              >
                <td class="px-4 py-3 font-semibold">
                  {{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}
                </td>
                <td class="px-4 py-3 font-semibold text-slate-900">
                  {{ item.ma }}
                </td>
                <td class="px-4 py-3 text-slate-900">{{ item.ten }}</td>
                <td class="px-4 py-3">
                  <span
                    class="inline-flex items-center gap-1 whitespace-nowrap rounded-full px-2.5 py-0.5 text-xs font-semibold"
                    :class="mauLoaiPhieu(item.loaiPhieu)"
                  >
                    <component :is="Number(item.loaiPhieu) === 2 ? User : Globe" class="h-3 w-3" />
                    {{ loaiPhieuText(item.loaiPhieu) }}
                  </span>
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
                <td class="px-4 py-3 text-center">
                  <div class="flex items-center justify-center gap-3">
                    <AdminQuickStatusAction
                      :loading="false"
                      :disabled="isHetHan(item.ngayKetThuc) || Number(item.trangThai) === 2"
                      :disabled-title="
                        isHetHan(item.ngayKetThuc) || Number(item.trangThai) === 2
                          ? 'Phiếu đã hết hạn, vui lòng vào chi tiết để gia hạn'
                          : undefined
                      "
                      :action-label="
                        Number(item.trangThai) === 1 || Number(item.trangThai) === 4
                          ? 'Ngừng hoạt động'
                          : 'Đang hoạt động'
                      "
                      :confirm-message="
                        Number(item.trangThai) === 1 || Number(item.trangThai) === 4
                          ? 'Bạn có chắc chắn muốn ngừng hoạt động phiếu này không?'
                          : 'Bạn có chắc chắn muốn chuyển phiếu này sang trạng thái đang hoạt động không?'
                      "
                      :intent="
                        Number(item.trangThai) === 1 || Number(item.trangThai) === 4 ? 'deactivate' : 'activate'
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
            class="min-w-[1100px] w-full border-collapse text-left text-sm text-slate-600"
          >
            <thead class="bg-slate-50 border-b border-slate-200 text-slate-700 font-semibold">
              <tr class="text-left text-sm font-bold text-slate-950">
                <th class="px-4 py-3">STT</th>
                <th class="px-4 py-3">Mã phiếu</th>
                <th class="px-4 py-3">Tên phiếu</th>
                <th class="px-4 py-3">Khách hàng</th>
                <th class="px-4 py-3">Ngày tặng</th>
                <th class="px-4 py-3">Ngày dùng</th>
                <th class="px-4 py-3">Trạng thái</th>
                <th class="px-4 py-3 text-center">
                  Hành động
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
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
                v-else
                v-for="(item, index) in danhSachKh"
                :key="item.id"
                class="transition hover:bg-slate-50/50"
              >
                <td class="px-4 py-3 font-semibold">
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
                    :class="mauTrangThaiKh(item.trangThai)"
                  >
                    {{ statusTextKh(item.trangThai) }}
                  </span>
                </td>
                <td class="px-4 py-3 text-center">
                  <div class="flex items-center justify-center gap-3">
                    <AdminQuickStatusAction
                      :loading="false"
                      :disabled="isHetHan(item.ngayKetThuc) || Number(item.trangThai) === 2"
                      :disabled-title="
                        isHetHan(item.ngayKetThuc) || Number(item.trangThai) === 2
                          ? 'Phiếu đã hết hạn, không thể thao tác'
                          : undefined
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
