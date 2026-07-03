<script setup>
import { ref, watch } from "vue";
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  CircleX,
  Pencil,
  hoaDon,
  hienModalThongTin,
  hienModalGiaoHang,
  tabHienTai,
  formThongTin,
  danhSachTrangThaiHienThi,
  hienThiOptionTrangThai,
  isOptionDisabled,
  coTheSuaThongTinGiaoHang,
  dangCapNhat,
  donDaHoanThanh,
  dinhDangGio,
  dinhDangNgay,
  moModalSuaDiaChi,
  handleHuyDonTuModal,
  handleLuuThongTin,
} = useInvoiceDetailContext();

const hienModalLyDo = ref(false);
const lyDo = ref("");
const trangThaiTruocDo = ref("");

const hienModalLyDoHuy = ref(false);
const lyDoHuy = ref("");

watch(hienModalThongTin, (newVal) => {
  if (newVal) {
    trangThaiTruocDo.value = formThongTin.value.trangThai;
  }
});

watch(
  () => formThongTin.value.trangThai,
  (newVal, oldVal) => {
    if (newVal === "Giao hàng thất bại") {
      trangThaiTruocDo.value = oldVal || hoaDon.value.trangThai;
      lyDo.value = "";
      hienModalLyDo.value = true;
    }
  }
);

const xacNhanLyDo = () => {
  if (!lyDo.value.trim()) return;
  formThongTin.value.ghiChu = lyDo.value.trim();
  hienModalLyDo.value = false;
};

const huyLyDo = () => {
  formThongTin.value.trangThai = trangThaiTruocDo.value;
  hienModalLyDo.value = false;
};

const moModalHuy = () => {
  lyDoHuy.value = "";
  hienModalLyDoHuy.value = true;
};

const xacNhanHuy = async () => {
  if (!lyDoHuy.value.trim()) return;
  hienModalLyDoHuy.value = false;
  await handleHuyDonTuModal(lyDoHuy.value);
};
</script>

<template>
  <div
    v-if="hienModalThongTin"
    v-show="!hienModalGiaoHang"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
  >
    <div class="w-full max-w-[600px] overflow-hidden rounded-[16px] bg-white shadow-2xl">
      <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
        <h3 class="text-[17px] font-semibold text-slate-800">
          Cập nhật thông tin đơn hàng
        </h3>
        <button
          @click="hienModalThongTin = false"
          class="text-slate-400 transition hover:text-slate-600"
        >
          <CircleX class="h-5 w-5" />
        </button>
      </div>

      <div class="flex gap-6 border-b border-slate-100 px-6 pt-3 text-[14px]">
        <button
          :class="
            tabHienTai === 'donHang'
              ? 'border-b-2 border-slate-700 font-semibold text-slate-800'
              : 'text-slate-500 hover:text-slate-700'
          "
          class="pb-3 transition-colors"
          @click="tabHienTai = 'donHang'"
        >
          Thông tin đơn hàng
        </button>
        <button
          :class="
            tabHienTai === 'khachHang'
              ? 'border-b-2 border-blue-500 font-semibold text-blue-500'
              : 'text-blue-500 hover:text-blue-600'
          "
          class="pb-3 transition-colors"
          @click="tabHienTai = 'khachHang'"
        >
          Thông tin khách hàng
        </button>
      </div>

      <div class="p-6">
        <div v-if="tabHienTai === 'donHang'" class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="mb-1.5 block text-[13px] font-medium text-slate-600">
                Mã đơn hàng
              </label>
              <input
                type="text"
                readonly
                :value="hoaDon.maHoaDon"
                class="w-full rounded-[8px] bg-slate-100 px-3 py-2.5 text-[14px] text-slate-600 outline-none"
              />
            </div>
            <div>
              <label class="mb-1.5 block text-[13px] font-medium text-slate-600">
                Ngày tạo
              </label>
              <input
                type="text"
                readonly
                :value="dinhDangGio(hoaDon.ngayTao) + ' ' + dinhDangNgay(hoaDon.ngayTao)"
                class="w-full rounded-[8px] bg-slate-100 px-3 py-2.5 text-[14px] text-slate-600 outline-none"
              />
            </div>
          </div>
          <div>
            <label class="mb-1.5 block text-[13px] font-medium text-slate-600">
              Trạng thái
            </label>
            <select
              v-model="formThongTin.trangThai"
              class="w-full rounded-[8px] border border-blue-400 px-3 py-2.5 text-[14px] text-slate-800 outline-none ring-1 ring-blue-100 transition focus:border-blue-500 focus:ring-blue-300 disabled:cursor-not-allowed disabled:bg-slate-100"
            >
              <template v-for="(st, index) in danhSachTrangThaiHienThi" :key="st.key">
                <option
                  v-if="hienThiOptionTrangThai(index, st.key)"
                  :value="st.key"
                  :disabled="isOptionDisabled(st.key)"
                >
                  {{ st.label }}
                </option>
              </template>
            </select>
          </div>
        </div>

        <div v-if="tabHienTai === 'khachHang'" class="space-y-4">
          <div>
            <label class="mb-1.5 block text-[13px] font-medium text-slate-600">
              Tên người nhận
            </label>
            <input
              type="text"
              v-model="formThongTin.tenKhachHang"
              :readonly="!coTheSuaThongTinGiaoHang"
              :class="
                coTheSuaThongTinGiaoHang
                  ? 'bg-white focus:border-rose-300'
                  : 'cursor-not-allowed bg-slate-100 text-slate-500'
              "
              class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition"
            />
          </div>
          <div>
            <label class="mb-1.5 block text-[13px] font-medium text-slate-600">
              Số điện thoại
            </label>
            <input
              type="text"
              v-model="formThongTin.soDienThoai"
              :readonly="!coTheSuaThongTinGiaoHang"
              :class="
                coTheSuaThongTinGiaoHang
                  ? 'bg-white focus:border-rose-300'
                  : 'cursor-not-allowed bg-slate-100 text-slate-500'
              "
              class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition"
            />
          </div>
          <div>
            <label class="mb-1.5 block text-[13px] font-medium text-slate-600">
              Email
            </label>
            <input
              type="text"
              v-model="formThongTin.email"
              readonly
              class="w-full cursor-not-allowed rounded-[8px] border border-slate-200 bg-slate-100 px-3 py-2.5 text-[14px] text-slate-500 outline-none"
            />
          </div>
          <div>
            <div class="mb-1.5 flex items-center justify-between gap-3">
              <label class="block text-[13px] font-medium text-slate-600">
                Địa chỉ giao hàng
              </label>
              <button
                v-if="coTheSuaThongTinGiaoHang"
                type="button"
                class="inline-flex items-center gap-1.5 text-xs font-bold text-[#B82220] hover:text-[#9f1d1b]"
                @click="moModalSuaDiaChi"
              >
                <Pencil class="h-3.5 w-3.5" />
                Sửa địa chỉ
              </button>
            </div>
            <input
              type="text"
              v-model="formThongTin.diaChi"
              :readonly="!coTheSuaThongTinGiaoHang"
              :class="
                coTheSuaThongTinGiaoHang
                  ? 'bg-white focus:border-rose-300'
                  : 'cursor-not-allowed bg-slate-100 text-slate-500'
              "
              class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition"
            />
          </div>
          <p
            v-if="!coTheSuaThongTinGiaoHang"
            class="rounded-[6px] bg-amber-50 px-3 py-2 text-xs text-amber-700"
          >
            Thông tin giao hàng đã khóa vì đơn đã được xác nhận.
          </p>
        </div>
      </div>

      <div class="flex justify-end gap-3 border-t border-slate-100 px-6 py-4">
        <button
          v-if="hoaDon && (hoaDon.trangThai || '').toLowerCase().trim() === 'chờ xác nhận'"
          @click="moModalHuy"
          :disabled="dangCapNhat"
          type="button"
          class="mr-auto inline-flex h-11 items-center justify-center rounded-[6px] bg-red-600 px-5 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60"
        >
          Hủy đơn hàng
        </button>
        <button
          @click="hienModalThongTin = false"
          type="button"
          class="inline-flex h-11 items-center justify-center rounded-[6px] bg-slate-500 px-5 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60"
        >
          Hủy
        </button>
        <button
          @click="handleLuuThongTin"
          :disabled="dangCapNhat || donDaHoanThanh"
          type="button"
          class="inline-flex h-11 items-center justify-center rounded-[6px] bg-[#B82220] px-5 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60"
        >
          {{ dangCapNhat ? "Đang Lưu..." : "Lưu" }}
        </button>
      </div>
    </div>

    <!-- Small modal for delivery failure reason -->
    <div
      v-if="hienModalLyDo"
      class="fixed inset-0 z-[60] flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
    >
      <div class="w-full max-w-sm overflow-hidden rounded-[16px] bg-white p-6 shadow-2xl">
        <div class="flex items-center justify-between border-b border-slate-100 pb-3">
          <h4 class="text-base font-bold text-slate-800">Lý do giao hàng thất bại</h4>
          <button
            @click="huyLyDo"
            class="text-slate-400 transition hover:text-slate-600"
          >
            <CircleX class="h-5 w-5" />
          </button>
        </div>
        <div class="mt-4">
          <label class="mb-2 block text-xs font-semibold uppercase tracking-wide text-slate-400">
            Chi tiết lý do
          </label>
          <textarea
            v-model="lyDo"
            rows="3"
            placeholder="Nhập lý do giao hàng thất bại..."
            class="w-full rounded-[6px] border border-slate-200 bg-slate-50 p-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white resize-none"
          ></textarea>
        </div>
        <div class="mt-6 flex gap-3">
          <button
            @click="huyLyDo"
            type="button"
            class="flex-1 inline-flex h-9 items-center justify-center rounded-[6px] bg-slate-100 text-sm font-semibold text-slate-600 hover:bg-slate-200 transition"
          >
            Hủy
          </button>
          <button
            @click="xacNhanLyDo"
            :disabled="!lyDo.trim()"
            type="button"
            class="flex-1 inline-flex h-9 items-center justify-center rounded-[6px] bg-[#B82220] text-sm font-semibold text-white hover:bg-[#9f1d1b] transition disabled:cursor-not-allowed disabled:opacity-55"
          >
            Xác nhận
          </button>
        </div>
      </div>
    </div>

    <!-- Small modal for cancellation reason -->
    <div
      v-if="hienModalLyDoHuy"
      class="fixed inset-0 z-[60] flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
    >
      <div class="w-full max-w-sm overflow-hidden rounded-[16px] bg-white p-6 shadow-2xl">
        <div class="flex items-center justify-between border-b border-slate-100 pb-3">
          <h4 class="text-base font-bold text-slate-800">Lý do hủy đơn hàng</h4>
          <button
            @click="hienModalLyDoHuy = false"
            class="text-slate-400 transition hover:text-slate-600"
          >
            <CircleX class="h-5 w-5" />
          </button>
        </div>
        <div class="mt-4">
          <label class="mb-2 block text-xs font-semibold uppercase tracking-wide text-slate-400">
            Chi tiết lý do hủy
          </label>
          <textarea
            v-model="lyDoHuy"
            rows="3"
            placeholder="Nhập lý do hủy đơn hàng..."
            class="w-full rounded-[6px] border border-slate-200 bg-slate-50 p-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white resize-none"
          ></textarea>
        </div>
        <div class="mt-6 flex gap-3">
          <button
            @click="hienModalLyDoHuy = false"
            type="button"
            class="flex-1 inline-flex h-9 items-center justify-center rounded-[6px] bg-slate-100 text-sm font-semibold text-slate-600 hover:bg-slate-200 transition"
          >
            Quay lại
          </button>
          <button
            @click="xacNhanHuy"
            :disabled="!lyDoHuy.trim() || dangCapNhat"
            type="button"
            class="flex-1 inline-flex h-9 items-center justify-center rounded-[6px] bg-[#B82220] text-sm font-semibold text-white hover:bg-[#9f1d1b] transition disabled:cursor-not-allowed disabled:opacity-55"
          >
            Xác nhận hủy
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
