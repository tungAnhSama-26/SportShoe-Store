<script setup lang="ts">
import { useQuanLyKhachHang } from "./useQuanLyKhachHang";
const { computed, onActivated, onMounted, ref, watch, useRouter, CheckCircle2, Eye, FileSpreadsheet, Filter, Home, MapPin, Package, Plus, RotateCcw, Search, ShoppingBag, Trash2, Users, X, doiTrangThaiKhachHang, layDanhSachKhachHang, layDanhSachDiaChi, themDiaChi, capNhatDiaChi, xoaDiaChi, datMacDinhDiaChi, layHoaDonTheoKhachHang, AdminTableFooter, AdminQuickStatusAction, exportRowsToExcel, getDisplayErrorMessage, Card, Button, Input, Badge, Table, router, CUSTOMER_CREATE_TOAST_KEY, danhSach, dangTai, loiTrang, boLoc, dsTrangThai, hienThiThongBao, taiThongBaoDieuHuong, mauTrangThai, dinhDangNgay, dinhDangTien, mauTrangThaiDon, badgeTrangThaiDon, soPhanTuMotTrang, trangHienTai, pageSizeOptions, tongSoTrang, danhSachPhanTrang, taiDanhSach, lamMoiBoLoc, xemChiTiet, dangDoiTrangThai, toggleTrangThai, themMoi, xuatExcel, timer, khModalDiaChi, dsDiaChiModal, dangTaiDiaChi, loiDiaChi, hienFormDiaChi, diaChiDangSua, dangLuuDiaChi, formDiaChi, dsTinh, dsHuyen, dsXa, maTinhChon, maHuyenChon, dangTaiDiaPhuong, taiDsTinh, onTinhChange, onHuyenChange, onXaChange, preFillCascadeForEdit, moModalDiaChi, taiDsModalDiaChi, dongModalDiaChi, moThemDiaChiModal, moSuaDiaChiModal, luuDiaChiModal, xoaDiaChiModal, datMacDinhModal, capNhatDiaChiMacDinhTrongBang, moModalDonHang } = useQuanLyKhachHang();
</script>

<template>
  <div>
    <div class="space-y-5">


    <!-- Header -->
    <section>
      <h1 class="admin-page-title text-[30px]">
        Quản lý khách hàng
      </h1>
    </section>

    <!-- Bộ lọc -->
    <Card>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
            <Filter class="h-5 w-5" />
          </div>
          <h2 class="admin-section-title">Bộ lọc</h2>
        </div>
      </template>

      <div class="flex flex-col gap-5">
        <div class="grid grid-cols-1 gap-4 lg:grid-cols-12 items-end">
          <div class="lg:col-span-8">
            <div class="relative">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                v-model="boLoc.keyword"
                type="text"
                placeholder="Tìm theo tên đăng nhập, họ tên, email, SĐT..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              />
            </div>
          </div>
          <div class="lg:col-span-4">
            <select
              v-model="boLoc.trangThai"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
            >
              <option v-for="tt in dsTrangThai" :key="tt.value" :value="tt.value">
                {{ tt.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="flex flex-wrap items-center justify-end gap-3">
          <Button variant="soft" @click="lamMoiBoLoc">
            <template #prefix><RotateCcw class="h-4 w-4" /></template>
            Đặt lại bộ lọc
          </Button>
          <Button variant="soft" @click="xuatExcel">
            <template #prefix><FileSpreadsheet class="h-4 w-4" /></template>
            Xuất Excel
          </Button>
          <Button variant="primary" @click="themMoi">
            <template #prefix><Plus class="h-4 w-4" /></template>
            Thêm khách hàng
          </Button>
        </div>
      </div>
    </Card>

    <!-- Danh sách -->
    <Card>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/5 text-primary">
            <Users class="h-5 w-5" />
          </div>
          <div>
            <h2 class="admin-section-title">
              Danh sách khách hàng
            </h2>
          </div>
        </div>
      </template>

      <div
        v-if="loiTrang"
        class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600"
      >
        {{ loiTrang }}
      </div>

      <div class="admin-table-scroll">
        <Table>
          <template #header>
            <th class="px-3 py-3 whitespace-nowrap">STT</th>
            <th class="px-3 py-3 whitespace-nowrap">Ảnh</th>
            <th class="px-3 py-3 whitespace-nowrap">Tên đăng nhập</th>
            <th class="px-3 py-3 whitespace-nowrap">Họ tên</th>
            <th class="px-3 py-3 whitespace-nowrap">Email</th>
            <th class="px-3 py-3 whitespace-nowrap">Địa chỉ</th>
            <th class="px-3 py-3 whitespace-nowrap">Số điện thoại</th>
            <th class="px-3 py-3 whitespace-nowrap">Trạng thái</th>
            <th class="px-3 py-3 text-center whitespace-nowrap">Hành động</th>
          </template>

          <template #body>
            <tr v-if="dangTai">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu khách hàng...</td>
            </tr>
            <tr v-else-if="!danhSachPhanTrang.length">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">Không có khách hàng phù hợp.</td>
            </tr>
            <tr
              v-else
              v-for="(kh, index) in danhSachPhanTrang"
              :key="kh.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
            >
              <td class="rounded-l-2xl px-3 py-3 font-semibold">
                {{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}
              </td>
              <td class="px-3 py-3">
                <img
                  :src="kh.hinhAnh || ('https://ui-avatars.com/api/?name=' + encodeURIComponent(kh.hoTen || 'KH') + '&background=f1f5f9&color=475569&size=64')"
                  :alt="kh.hoTen"
                  class="h-10 w-10 rounded-full object-cover ring-2 ring-slate-100"
                />
              </td>
              <td class="px-3 py-3 font-semibold text-slate-800">
                <div class="truncate max-w-[120px]" :title="kh.tenDangNhap">{{ kh.tenDangNhap }}</div>
              </td>
              <td class="px-3 py-3 font-semibold text-slate-800">
                <div class="truncate max-w-[120px]" :title="kh.hoTen">{{ kh.hoTen }}</div>
              </td>
              <td class="px-3 py-3 text-slate-600">
                <div class="text-xs truncate max-w-[160px]" :title="kh.email || '—'">{{ kh.email || '—' }}</div>
              </td>
              <td class="px-3 py-3 text-slate-600">
                <div class="min-w-[180px] max-w-[320px] break-words whitespace-normal leading-normal text-sm" :title="kh.diaChiMacDinh || '—'">
                  {{ kh.diaChiMacDinh || '—' }}
                </div>
              </td>
              <td class="px-3 py-3 text-slate-600">
                <div class="truncate" :title="kh.sdtMacDinh || kh.sdt || '—'">{{ kh.sdtMacDinh || kh.sdt || '—' }}</div>
              </td>
              <td class="px-3 py-3">
                <Badge :variant="kh.trangThai === 1 ? 'success' : 'danger'">
                  {{ kh.tenTrangThai }}
                </Badge>
              </td>
              <td class="rounded-r-2xl px-3 py-3 align-top text-center">
                <div class="flex items-center justify-center gap-0.5">
                  <AdminQuickStatusAction
                    :loading="dangDoiTrangThai === kh.id"
                    :action-label="kh.trangThai === 1 ? 'Khóa tài khoản' : 'Kích hoạt tài khoản'"
                    :intent="kh.trangThai === 1 ? 'deactivate' : 'activate'"
                    @toggle="toggleTrangThai(kh)"
                  />
                  <button
                    @click="moModalDiaChi(kh)"
                    class="admin-table-action text-sky-500 hover:text-sky-700"
                    title="Quản lý địa chỉ"
                  >
                    <MapPin :size="14" />
                  </button>
                  <button
                    @click="moModalDonHang(kh)"
                    class="admin-table-action text-violet-500 hover:text-violet-700"
                    title="Xem đơn hàng"
                  >
                    <ShoppingBag :size="14" />
                  </button>
                  <button
                    @click="xemChiTiet(kh.id)"
                    class="admin-table-action text-slate-600 hover:text-slate-900"
                    title="Xem chi tiết"
                  >
                    <Eye :size="14" />
                  </button>
                </div>
              </td>
            </tr>
          </template>
        </Table>
      </div>

      <template #footer>
        <AdminTableFooter
          :current-page="trangHienTai"
          :page-size="soPhanTuMotTrang"
          :page-size-options="pageSizeOptions"
          :total-items="(danhSach || []).length"
          :total-pages="tongSoTrang"
          compact
          show-refresh
          @refresh="taiDanhSach"
          @update:current-page="trangHienTai = $event"
          @update:page-size="soPhanTuMotTrang = $event"
        />
      </template>
    </Card>
  </div>

  <!-- ===== MODAL ĐỊA CHỈ ===== -->
  <Teleport to="body">
    <div v-if="khModalDiaChi" class="fixed inset-0 z-[100] flex items-start justify-center bg-slate-900/40 p-4 overflow-y-auto">
      <div class="w-full max-w-2xl rounded-[32px] bg-white shadow-2xl my-8">
        <!-- Header modal -->
        <div class="flex items-center justify-between border-b border-slate-100 px-8 py-5">
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-sky-50 text-sky-500">
              <MapPin class="h-5 w-5" />
            </div>
            <div>
              <h2 class="text-base font-bold text-slate-800">Địa chỉ của {{ khModalDiaChi.hoTen }}</h2>
              <p class="text-xs text-slate-400">Quản lý địa chỉ giao hàng</p>
            </div>
          </div>
          <Button variant="ghost" size="icon" @click="dongModalDiaChi" class="h-8 w-8 rounded-full bg-slate-100 text-slate-500 hover:bg-slate-200">
            <X class="h-4 w-4" />
          </Button>
        </div>

        <div class="p-8 space-y-5">
          <!-- Lỗi -->
          <div v-if="loiDiaChi" class="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ loiDiaChi }}</div>

          <!-- Danh sách địa chỉ -->
          <div v-if="!hienFormDiaChi">
            <div v-if="dangTaiDiaChi" class="py-8 text-center text-sm text-slate-400">Đang tải...</div>
            <div v-else-if="!dsDiaChiModal.length" class="py-8 text-center border-2 border-dashed border-slate-100 rounded-3xl text-slate-400 text-sm">
              Chưa có địa chỉ nào.
            </div>
            <div v-else class="grid gap-3 sm:grid-cols-2">
              <div
                v-for="dc in dsDiaChiModal"
                :key="dc.id"
                class="group relative rounded-2xl border p-4 transition"
                :class="dc.laMacDinh ? 'border-emerald-200 bg-emerald-50/40' : 'border-slate-100 bg-slate-50 hover:border-primary/40 hover:bg-white'"
              >
                <div class="flex items-start justify-between gap-2">
                  <div class="space-y-1 flex-1 min-w-0">
                    <div class="flex items-center gap-2 flex-wrap">
                      <span class="font-bold text-slate-800 text-sm">{{ dc.hoTen }}</span>
                      <span v-if="dc.laMacDinh" class="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-bold text-emerald-700">
                        <CheckCircle2 class="h-3 w-3" /> Mặc định
                      </span>
                    </div>
                    <p class="text-[12px] text-slate-600">{{ dc.sdt }}</p>
                    <p class="text-[12px] text-slate-500 leading-relaxed">{{ dc.diaChiCuThe }}, {{ dc.phuongXa }}, {{ dc.quanHuyen }}, {{ dc.tinhThanh }}</p>
                  </div>
                  <div class="flex flex-col gap-1 shrink-0">
                    <Button v-if="!dc.laMacDinh" variant="soft" size="sm" class="h-7 px-2 text-[11px] bg-emerald-50 text-emerald-600 hover:bg-emerald-100" @click="datMacDinhModal(dc.id)">Mặc định</Button>
                    <Button variant="soft" size="sm" class="h-7 px-2 text-[11px] bg-slate-100 text-slate-500 hover:text-sky-600 hover:bg-sky-50" @click="moSuaDiaChiModal(dc)">Sửa</Button>
                    <Button v-if="!dc.laMacDinh" variant="soft" size="sm" class="h-7 px-2 text-[11px] bg-slate-100 text-slate-500 hover:text-rose-600 hover:bg-rose-50" @click="xoaDiaChiModal(dc.id)">Xóa</Button>
                  </div>
                </div>
              </div>
            </div>

            <Button
              variant="outline"
              class="mt-4 w-full justify-center border-dashed border-2 hover:border-primary/50 hover:text-primary py-3 text-slate-500"
              @click="moThemDiaChiModal"
            >
              <Plus class="h-4 w-4 mr-2" /> Thêm địa chỉ mới
            </Button>
          </div>

          <!-- Form thêm/sửa địa chỉ -->
          <div v-else class="space-y-4">
            <h3 class="text-sm font-bold text-slate-700">{{ diaChiDangSua ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ mới' }}</h3>
            <div class="grid gap-4 sm:grid-cols-2">
              <label class="space-y-1.5">
                <span class="text-[13px] font-semibold text-slate-500">Họ tên người nhận <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.hoTen" type="text" placeholder="Họ tên" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/10 focus:bg-white" />
              </label>
              <label class="space-y-1.5">
                <span class="text-[13px] font-semibold text-slate-500">Số điện thoại <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.sdt" type="tel" placeholder="Số điện thoại" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/10 focus:bg-white" />
              </label>
              <label class="space-y-1.5">
                <span class="text-[13px] font-semibold text-slate-500">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
                <select
                  :value="maTinhChon"
                  @change="onTinhChange(Number(($event.target as HTMLSelectElement).value) || null)"
                  class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/10 focus:bg-white"
                >
                  <option value="">-- Chọn tỉnh/thành --</option>
                  <option v-for="t in dsTinh" :key="t.code" :value="t.code">{{ t.name }}</option>
                </select>
              </label>
              <label class="space-y-1.5">
                <span class="text-[13px] font-semibold text-slate-500">Quận/Huyện <span class="text-rose-500">*</span></span>
                <select
                  :value="maHuyenChon"
                  @change="onHuyenChange(Number(($event.target as HTMLSelectElement).value) || null)"
                  :disabled="!maTinhChon"
                  class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/10 focus:bg-white disabled:opacity-50"
                >
                  <option value="">-- Chọn quận/huyện --</option>
                  <option v-for="h in dsHuyen" :key="h.code" :value="h.code">{{ h.name }}</option>
                </select>
              </label>
              <label class="space-y-1.5">
                <span class="text-[13px] font-semibold text-slate-500">Phường/Xã <span class="text-rose-500">*</span></span>
                <select
                  :value="formDiaChi.phuongXa"
                  @change="onXaChange(($event.target as HTMLSelectElement).value)"
                  :disabled="!maHuyenChon"
                  class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/10 focus:bg-white disabled:opacity-50"
                >
                  <option value="">-- Chọn phường/xã --</option>
                  <option v-for="x in dsXa" :key="x.code" :value="x.name">{{ x.name }}</option>
                </select>
              </label>
              <label class="space-y-1.5 sm:col-span-2">
                <span class="text-[13px] font-semibold text-slate-500">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.diaChiCuThe" type="text" placeholder="Số nhà, tên đường..." class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-primary/50 focus:ring-2 focus:ring-primary/10 focus:bg-white" />
              </label>
              <div class="sm:col-span-2 flex items-center gap-2">
                <input v-model="formDiaChi.laMacDinh" type="checkbox" id="laMacDinhModal" class="h-4 w-4 rounded" />
                <label for="laMacDinhModal" class="text-sm font-semibold text-slate-600 cursor-pointer">Đặt làm địa chỉ mặc định</label>
              </div>
            </div>
            <div class="flex gap-3 pt-2">
              <Button
                variant="primary"
                class="flex-1"
                :disabled="dangLuuDiaChi"
                @click="luuDiaChiModal"
              >
                {{ dangLuuDiaChi ? "Đang lưu..." : "Lưu địa chỉ" }}
              </Button>
              <Button
                variant="soft"
                class="flex-1"
                @click="hienFormDiaChi = false; diaChiDangSua = null"
              >
                Hủy
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>

  </div>
</template>
