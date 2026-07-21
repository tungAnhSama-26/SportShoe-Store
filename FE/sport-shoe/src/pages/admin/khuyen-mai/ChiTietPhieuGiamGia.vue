<script setup>
import {
  ArrowLeft,
  CheckSquare,
  Eye,
  RefreshCcw,
  Save,
  Search,
  ShoppingBag,
  Ticket,
  Users,
  X,
} from "lucide-vue-next";
import { useChiTietPhieuGiamGia } from "./useChiTietPhieuGiamGia";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";

const {
  route,
  router,
  id,
  laMoi,
  dangTai,
  saving,
  loiTrang,
  formErrors,
  form,
  soLuongVoHan,
  soLuongDisplay,
  handleSoLuongEnter,
  isReadOnly,
  isHetHan,
  giaTriDisplay,
  giaTriToiThieuVnd,
  giamToiDaVnd,
  searchKh,
  dsEmailChon,
  dangTaiKh,
  trangKh,
  soPhanTuMotTrangKh,
  boLocKh,
  danhSachKhFiltered,
  tongSoTrangKh,
  danhSachKhTrang,
  taoMaNgauNhien,
  laKhachHangDaDung,
  toggleEmail,
  chonTatCa,
  handleSearch,
  submitForm,
  listHoaDonApplied,
  dangTaiHoaDon,
  loiTaiHoaDon,
  getTongDonHangCuaKhachHang,
  getDonHangGanNhat,
  dinhDangNgaySinh,
  dinhDangTien,
  dinhDangNgay,
  xemChiTietHoaDon,
  mauTrangThai,
  parseVndNumber,
  todayStr,
} = useChiTietPhieuGiamGia();
</script>

<template>
  <div class="w-full min-w-0 space-y-5 pb-10 radius-6px">

    <section class="flex items-center gap-4 border-b border-slate-100 pb-4">
      <button
        @click="router.push({ name: 'admin-phieu-giam-gia' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
    </section>

    <div
      v-if="loiTrang"
      class="rounded-2xl border border-rose-100 bg-rose-50 px-5 py-3 text-sm font-medium text-rose-600"
    >
      {{ loiTrang }}
    </div>

    <div
      v-if="isReadOnly"
      class="rounded-2xl border border-amber-100 bg-amber-50 px-5 py-3 text-sm font-medium text-amber-700"
    >
      Phiếu giảm giá cá nhân này đã hết hạn nên không thể chỉnh sửa thông tin.
    </div>

    <section
      class="w-full min-w-0 space-y-6 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm"
    >
      <fieldset :disabled="isReadOnly" class="min-w-0 space-y-6">
        <div class="flex items-center gap-3">
          <div
            class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500"
          >
            <Ticket class="h-5 w-5" />
          </div>
          <div>
            <h2 class="text-base font-bold text-slate-800">Thông tin phiếu</h2>
          </div>
        </div>

        <div class="grid grid-cols-1 gap-6 xl:grid-cols-2">
          <div class="min-w-0 space-y-2">
            <label
              class="block whitespace-nowrap text-[13px] font-semibold text-slate-500"
              >Mã phiếu <span class="text-rose-500">*</span></label
            >
            <div class="relative">
              <input
                v-model="form.ma"
                :readonly="!laMoi"
                class="h-11 w-full rounded-2xl border border-slate-200 pl-4 pr-11 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
                :class="!laMoi ? 'cursor-not-allowed bg-slate-100 text-slate-500' : 'bg-slate-50'"
                placeholder="Ví dụ: VOUCHER2024"
              />
              <button
                v-if="laMoi && !isReadOnly"
                @click="taoMaNgauNhien"
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 transition-colors hover:text-rose-500"
              >
                <RefreshCcw class="h-4 w-4" />
              </button>
            </div>
            <p v-if="formErrors.ma" class="mt-1 text-xs text-rose-500">
              {{ formErrors.ma }}
            </p>
          </div>

          <div class="min-w-0 space-y-2">
            <label
              class="block whitespace-nowrap text-[13px] font-semibold text-slate-500"
              >Tên phiếu <span class="text-rose-500">*</span></label
            >
            <input
              v-model="form.ten"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
              placeholder="Ví dụ: Giảm giá hè 2024"
            />
            <p v-if="formErrors.ten" class="mt-1 text-xs text-rose-500">
              {{ formErrors.ten }}
            </p>
          </div>

          <div class="space-y-2">
            <label class="text-[13px] font-semibold text-slate-500"
              >Hình thức phiếu <span class="text-rose-500">*</span></label
            >
            <div class="flex gap-6 pt-2">
              <label class="group flex cursor-pointer items-center gap-2">
                <input
                  type="radio"
                  v-model="form.loaiPhieu"
                  value="1"
                  class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500"
                />
                <span class="text-sm font-normal text-slate-600"
                  >Công khai</span
                >
              </label>
              <label class="group flex cursor-pointer items-center gap-2">
                <input
                  type="radio"
                  v-model="form.loaiPhieu"
                  value="2"
                  class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500"
                />
                <span class="text-sm font-normal text-slate-600">Cá nhân</span>
              </label>
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-[13px] font-semibold text-slate-500"
              >Loại giảm <span class="text-rose-500">*</span></label
            >
            <div class="flex gap-6 pt-2">
              <label class="group flex cursor-pointer items-center gap-2">
                <input
                  type="radio"
                  v-model="form.loai"
                  value="1"
                  class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500"
                />
                <span class="text-sm font-normal text-slate-600"
                  >Phần trăm (%)</span
                >
              </label>
              <label class="group flex cursor-pointer items-center gap-2">
                <input
                  type="radio"
                  v-model="form.loai"
                  value="2"
                  class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500"
                />
                <span class="text-sm font-normal text-slate-600"
                  >Tiền mặt (VNĐ)</span
                >
              </label>
            </div>
          </div>

          <div class="min-w-0 space-y-2">
            <label
              class="block whitespace-nowrap text-[13px] font-semibold text-slate-500"
              >Giá trị giảm ({{ form.loai === "1" ? "%" : "VNĐ" }})
              <span class="text-rose-500">*</span></label
            >
            <div class="relative">
              <input
                v-model="giaTriDisplay"
                :type="form.loai === '1' ? 'number' : 'text'"
                :min="form.loai === '1' ? '1' : undefined"
                :max="form.loai === '1' ? '100' : undefined"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
                placeholder="0"
              />
              <span
                class="absolute right-4 top-1/2 -translate-y-1/2 text-[11px] font-bold text-slate-400"
                >{{ form.loai === "1" ? "%" : "VNĐ" }}</span
              >
            </div>
            <p v-if="formErrors.giaTri" class="mt-1 text-xs text-rose-500">
              {{ formErrors.giaTri }}
            </p>
            <p v-else-if="Number(form.loai) === 1 && Number(form.giaTri) === 100" class="text-xs text-emerald-600 font-medium">
              ✓ Sản phẩm sẽ miễn phí hoàn toàn (giảm 100%)
            </p>
          </div>

          <div class="min-w-0 space-y-2">
            <label
              class="block whitespace-nowrap text-[13px] font-semibold text-slate-500"
              >Giá trị đơn tối thiểu (VNĐ)</label
            >
            <input
              v-model="giaTriToiThieuVnd"
              type="text"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
            />
            <p
              v-if="formErrors.giaTriToiThieu"
              class="mt-1 text-xs text-rose-500"
            >
              {{ formErrors.giaTriToiThieu }}
            </p>
          </div>

          <div v-if="form.loai === '1'" class="min-w-0 space-y-2">
            <label
              class="block whitespace-nowrap text-[13px] font-semibold text-slate-500"
              >Giảm tối đa (VNĐ)</label
            >
            <input
              v-model="giamToiDaVnd"
              type="text"
              :disabled="Number(form.loai) === 1 && Number(form.giaTri) === 100"
              class="h-11 w-full rounded-2xl border border-slate-200 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white disabled:bg-slate-100 disabled:cursor-not-allowed disabled:text-slate-400"
              :class="Number(form.loai) === 1 && Number(form.giaTri) === 100 ? '' : 'bg-slate-50'"
            />
            <p v-if="Number(form.loai) === 1 && Number(form.giaTri) === 100" class="text-xs text-slate-400">
              Không cần giảm tối đa khi giảm 100%
            </p>
            <p v-else-if="parseVndNumber(giamToiDaVnd) === 0" class="text-xs text-amber-600 font-medium">
              ⚠ Lưu ý: Voucher này sẽ không giới hạn số tiền giảm tối đa.
            </p>
            <p v-if="formErrors.giamToiDa" class="text-xs text-rose-500">
              {{ formErrors.giamToiDa }}
            </p>
          </div>

          <div class="min-w-0 space-y-2">
            <label
              class="block whitespace-nowrap text-[13px] font-semibold text-slate-500"
              >Số lượng <span class="text-rose-500">*</span></label
            >
            <div class="space-y-3">
              <input
                v-model="soLuongDisplay"
                type="text"
                inputmode="numeric"
                @keydown.enter.prevent="handleSoLuongEnter"
                :readonly="form.loaiPhieu === '2' || soLuongVoHan"
                :disabled="soLuongVoHan"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
                :class="
                  form.loaiPhieu === '2' || soLuongVoHan
                    ? 'cursor-not-allowed bg-slate-100 text-slate-500 focus:border-slate-200 focus:bg-slate-100'
                    : ''
                "
                placeholder="Nhập số lượng"
              />
              <label
                v-if="form.loaiPhieu === '1'"
                class="flex items-center gap-2 cursor-pointer"
              >
                <input
                  type="checkbox"
                  v-model="soLuongVoHan"
                  class="h-4 w-4 rounded border-slate-300 text-rose-500 focus:ring-rose-500"
                />
                <span class="text-sm text-slate-600">
                  Vô hạn số lượng
                </span>
              </label>
            </div>
            <p v-if="formErrors.soLuong" class="mt-1 text-xs text-rose-500">
              {{ formErrors.soLuong }}
            </p>
            <p v-else-if="soLuongVoHan" class="text-xs text-emerald-600 font-medium">
              ✓ Phiếu giảm giá có số lượng không giới hạn
            </p>
          </div>

          <div class="min-w-0 space-y-2">
            <label
              class="block whitespace-nowrap text-[13px] font-semibold text-slate-500"
              >Ngày bắt đầu <span class="text-rose-500">*</span></label
            >
            <input
              v-model="form.ngayBatDau"
              type="date"
              :max="todayStr"
              :readonly="isReadOnly"
              class="h-11 w-full rounded-2xl border border-slate-200 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
              :class="
                isReadOnly
                  ? 'cursor-not-allowed bg-slate-100 text-slate-500'
                  : 'bg-slate-50'
              "
            />
            <p v-if="formErrors.ngayBatDau" class="mt-1 text-xs text-rose-500">
              {{ formErrors.ngayBatDau }}
            </p>
          </div>

          <div class="min-w-0 space-y-2">
            <label
              class="block whitespace-nowrap text-[13px] font-semibold text-slate-500"
              >Ngày kết thúc <span class="text-rose-500">*</span></label
            >
            <input
              v-model="form.ngayKetThuc"
              type="date"
              :min="form.ngayBatDau || undefined"
              :readonly="isReadOnly"
              class="h-11 w-full rounded-2xl border border-slate-200 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
              :class="
                isReadOnly
                  ? 'cursor-not-allowed bg-slate-100 text-slate-500'
                  : 'bg-slate-50'
              "
            />
            <p v-if="formErrors.ngayKetThuc" class="mt-1 text-xs text-rose-500">
              {{ formErrors.ngayKetThuc }}
            </p>
          </div>
        </div>

        <div
          v-if="form.loaiPhieu === '2'"
          class="w-full min-w-0 mt-6 space-y-4 rounded-3xl border border-slate-100 bg-slate-50/30 p-5"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3 text-slate-800">
              <Users class="h-5 w-5 text-rose-500" />
              <span class="text-sm font-bold">Chọn khách hàng mục tiêu</span>
              <span class="text-xs text-slate-500">({{ dsEmailChon.length }} đã chọn)</span>
            </div>
            <button
              v-if="!isReadOnly"
              type="button"
              @click="chonTatCa"
              class="text-xs font-semibold text-rose-500 transition-colors hover:text-rose-600"
            >
              {{
                danhSachKhTrang.length > 0 &&
                danhSachKhTrang.every((kh) => dsEmailChon.includes(kh.email))
                  ? "Bỏ chọn tất cả"
                  : "Chọn tất cả trang này"
              }}
            </button>
          </div>

          <div class="flex gap-3">
            <div class="relative flex-1">
              <Search
                class="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
              />
              <input
                v-model="searchKh"
                type="text"
                placeholder="Tìm theo tên hoặc số điện thoại..."
                @input="handleSearch"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-white pl-11 pr-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300"
              />
            </div>
            
            <select
              v-model="boLocKh"
              @change="trangKh = 1"
              class="h-11 rounded-2xl border border-slate-200 bg-white px-4 text-sm font-medium text-slate-700 outline-none transition focus:border-rose-300"
            >
              <option value="tat-ca">Tất cả khách hàng</option>
              <option value="da-chon">Đã chọn ({{ dsEmailChon.length }})</option>
            </select>
          </div>

          <div
            class="custom-scrollbar max-h-[400px] overflow-x-auto overflow-y-auto rounded-2xl border border-slate-100 bg-white shadow-sm"
          >
            <table class="w-full border-collapse text-left text-sm">
              <thead
                class="sticky top-0 z-10 bg-slate-50 text-[12px] font-semibold text-slate-500"
              >
                <tr>
                  <th class="w-12 px-4 py-3 text-center">#</th>
                  <th class="px-4 py-3">Họ và tên</th>
                  <th class="px-4 py-3">Số điện thoại</th>
                  <th class="px-4 py-3">Email</th>
                  <th class="px-4 py-3">Tổng đơn hàng</th>
                  <th class="px-4 py-3">Đơn hàng gần nhất</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-if="dangTaiKh">
                  <td
                    colspan="6"
                    class="px-4 py-6 text-center text-sm text-slate-400"
                  >
                    Đang tải danh sách khách hàng...
                  </td>
                </tr>
                <tr v-else-if="!danhSachKhTrang.length">
                  <td
                    colspan="6"
                    class="px-4 py-6 text-center text-sm text-slate-400"
                  >
                    {{ boLocKh === 'da-chon' ? 'Chưa chọn khách hàng nào.' : 'Không có khách hàng phù hợp.' }}
                  </td>
                </tr>
                <tr
                  v-for="(kh, index) in danhSachKhTrang"
                  v-else
                  :key="kh.id"
                  @click="!isReadOnly && !laKhachHangDaDung(kh.email) && toggleEmail(kh.email)"
                  class="transition-colors"
                  :class="[
                    isReadOnly || laKhachHangDaDung(kh.email) ? 'opacity-60 cursor-not-allowed bg-slate-50' : 'cursor-pointer hover:bg-rose-50/50',
                    dsEmailChon.includes(kh.email) && !laKhachHangDaDung(kh.email) ? 'bg-rose-50/30' : ''
                  ]"
                >
                  <td class="px-4 py-3 text-center">
                    <CheckSquare
                      v-if="dsEmailChon.includes(kh.email)"
                      class="mx-auto h-5 w-5"
                      :class="isReadOnly || laKhachHangDaDung(kh.email) ? 'text-slate-400' : 'text-rose-500'"
                    />
                    <div
                      v-else
                      class="mx-auto h-5 w-5 rounded border border-slate-300 bg-white"
                    ></div>
                  </td>
                  <td class="px-4 py-3 font-semibold text-slate-800">
                    <div class="flex items-center gap-2">
                      <span>{{ kh.hoTen }}</span>
                      <span v-if="laKhachHangDaDung(kh.email)" class="text-[10px] bg-slate-100 border border-slate-200 text-slate-500 rounded px-1.5 py-0.5 whitespace-nowrap">Đã dùng</span>
                    </div>
                  </td>
                  <td class="px-4 py-3 text-slate-500 font-medium">
                    {{ kh.sdt || "—" }}
                  </td>
                  <td class="px-4 py-3 text-slate-500 font-medium">
                    {{ kh.email }}
                  </td>
                  <td class="px-4 py-3" @click.stop>
                    <div
                      v-if="getTongDonHangCuaKhachHang(kh.email) === 0"
                      class="text-slate-400 text-xs font-medium"
                    >
                      Chưa mua
                    </div>
                    <div
                      v-else
                      class="text-[11px] font-bold text-emerald-600 bg-emerald-50 border border-emerald-100 rounded-full px-2.5 py-0.5 inline-block"
                    >
                      {{ getTongDonHangCuaKhachHang(kh.email) }} đơn
                    </div>
                  </td>
                  <td class="px-4 py-3" @click.stop>
                    <div
                      v-if="!getDonHangGanNhat(kh.email)"
                      class="text-slate-400 text-xs font-medium"
                    >
                      —
                    </div>
                    <div v-else class="flex flex-col gap-0.5">
                      <span
                        @click="
                          xemChiTietHoaDon(getDonHangGanNhat(kh.email).id)
                        "
                        class="inline-flex items-center gap-0.5 text-[11px] font-bold text-rose-500 bg-rose-50 border border-rose-100 hover:bg-rose-100 transition-colors rounded px-1.5 py-0.5 cursor-pointer w-max"
                        title="Xem chi tiết đơn hàng gần nhất"
                      >
                        {{ getDonHangGanNhat(kh.email).maHoaDon }}
                      </span>
                      <span
                        class="text-[10px] text-slate-400 font-medium mt-0.5"
                      >
                        {{ dinhDangNgay(getDonHangGanNhat(kh.email).ngayTao) }}
                      </span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <AdminTableFooter
            :current-page="trangKh"
            :page-size="soPhanTuMotTrangKh"
            :page-size-options="[5, 10, 20]"
            :total-items="danhSachKhFiltered.length"
            :total-pages="tongSoTrangKh"
            compact
            @update:current-page="trangKh = $event"
            @update:page-size="soPhanTuMotTrangKh = $event; trangKh = 1"
          />

          <div class="text-xs font-medium text-slate-400">
            Đã chọn:
            <span class="font-bold text-slate-700">{{
              dsEmailChon.length
            }}</span>
            khách hàng
          </div>
          <p v-if="formErrors.email" class="text-xs text-rose-500">
            {{ formErrors.email }}
          </p>
        </div>
      </fieldset>

      <div class="flex items-center gap-3 border-t border-slate-100 pt-6">
        <button
          v-if="!isReadOnly"
          @click="submitForm"
          :disabled="saving"
          class="inline-flex items-center gap-2 rounded-2xl bg-rose-500 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60"
        >
          <Save class="h-4 w-4" />
          {{
            saving
              ? "Đang lưu..."
              : laMoi
                ? "Tạo phiếu giảm giá"
                : "Lưu thay đổi"
          }}
        </button>
        <button
          @click="router.push({ name: 'admin-phieu-giam-gia' })"
          class="whitespace-nowrap rounded-2xl border border-slate-200 bg-slate-50 px-6 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100"
        >
          {{ isReadOnly ? "Quay lại" : "Hủy" }}
        </button>
      </div>
    </section>

    <!-- Danh sách đơn hàng đã áp dụng phiếu giảm giá -->
    <section
      v-if="!laMoi"
      class="w-full min-w-0 space-y-6 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm"
    >
      <div
        class="flex items-center justify-between border-b border-slate-100 pb-4"
      >
        <div class="flex items-center gap-3">
          <div
            class="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-500"
          >
            <ShoppingBag class="h-5 w-5" />
          </div>
          <div>
            <h2 class="text-base font-bold text-slate-800">
              Đơn hàng đã áp dụng phiếu giảm giá
            </h2>
            <p class="text-xs font-medium text-slate-400">
              Danh sách các hóa đơn sử dụng mã phiếu giảm giá này
            </p>
          </div>
        </div>
        <div
          class="inline-flex items-center gap-2 rounded-full bg-emerald-50 px-3.5 py-1 text-xs font-semibold text-emerald-700"
        >
          Tổng đơn hàng: {{ listHoaDonApplied.length }}
        </div>
      </div>

      <div
        v-if="dangTaiHoaDon"
        class="py-10 text-center text-sm text-slate-400"
      >
        Đang tải danh sách hóa đơn liên quan...
      </div>

      <div
        v-else-if="loiTaiHoaDon"
        class="rounded-2xl border border-rose-100 bg-rose-50 px-5 py-3 text-sm font-medium text-rose-600"
      >
        {{ loiTaiHoaDon }}
      </div>

      <div
        v-else-if="!listHoaDonApplied.length"
        class="py-12 text-center text-sm text-slate-400"
      >
        <div
          class="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-slate-50 text-slate-400"
        >
          <ShoppingBag class="h-6 w-6" />
        </div>
        Chưa có đơn hàng nào áp dụng phiếu giảm giá này.
      </div>

      <div
        v-else
        class="overflow-x-auto rounded-2xl border border-slate-100 bg-white shadow-sm"
      >
        <table class="w-full border-collapse text-left text-sm">
          <thead class="bg-slate-50 text-[12px] font-semibold text-slate-500">
            <tr>
              <th class="w-12 px-4 py-3.5 text-center">STT</th>
              <th class="px-4 py-3.5">Mã hóa đơn</th>
              <th class="px-4 py-3.5">Khách hàng</th>
              <th class="px-4 py-3.5">Số điện thoại</th>
              <th class="px-4 py-3.5">Tổng tiền</th>
              <th class="px-4 py-3.5">Ngày tạo</th>
              <th class="px-4 py-3.5">Loại đơn</th>
              <th class="px-4 py-3.5 text-center">Trạng thái</th>
              <th class="w-20 px-4 py-3.5 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 text-[13px] text-slate-700">
            <tr
              v-for="(hd, index) in listHoaDonApplied"
              :key="hd.id"
              class="transition-colors hover:bg-slate-50/50"
            >
              <td class="px-4 py-3.5 text-center font-medium text-slate-400">
                {{ index + 1 }}
              </td>
              <td class="px-4 py-3.5 font-bold text-slate-800">
                {{ hd.maHoaDon }}
              </td>
              <td class="px-4 py-3.5 font-semibold text-slate-800">
                {{ hd.tenKhachHang || "—" }}
              </td>
              <td class="px-4 py-3.5 text-slate-500">
                {{ hd.soDienThoai || "—" }}
              </td>
              <td class="px-4 py-3.5 font-bold text-slate-800">
                {{ dinhDangTien(hd.tongTien) }}
              </td>
              <td class="px-4 py-3.5 text-slate-500">
                {{ dinhDangNgay(hd.ngayTao) }}
              </td>
              <td class="px-4 py-3.5">
                <span
                  class="inline-flex rounded-full bg-slate-100 px-2.5 py-0.5 text-[11px] font-medium text-slate-600"
                >
                  {{ hd.loaiDon }}
                </span>
              </td>
              <td class="px-4 py-3.5 text-center">
                <span
                  class="inline-flex items-center justify-center rounded-full px-2.5 py-0.5 text-[11px] font-semibold"
                  :class="
                    mauTrangThai[hd.trangThai] ||
                    'bg-slate-100 text-slate-600 border border-slate-200'
                  "
                >
                  {{ hd.trangThai }}
                </span>
              </td>
              <td class="px-4 py-3.5 text-center">
                <button
                  type="button"
                  @click="xemChiTietHoaDon(hd.id)"
                  class="inline-flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-emerald-50 hover:text-emerald-600"
                  title="Xem chi tiết đơn hàng"
                >
                  <Eye class="h-4 w-4" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
</style>
