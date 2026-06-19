<script setup>
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  Banknote,
  CheckCircle2,
  X,
  hoaDon,
  hienModalHoanTien,
  dangXacNhanHoanTien,
  formHoanTien,
  tongTienHoan,
  dsTaiKhoanNganHangKhach,
  dangTaiNganHangKhach,
  taiKhoanNganHangChon,
  qrHoanTienUrl,
  dinhDangTien,
  handleXacNhanHoanTien,
} = useInvoiceDetailContext();

const dinhDangTienKhongDonVi = (value) => {
  return new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 0 }).format(
    Number(value) || 0,
  );
};
</script>

<template>
  <div
    v-if="hienModalHoanTien"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm overflow-y-auto"
  >
    <div class="flex flex-col md:flex-row gap-6 max-w-[900px] w-full max-h-[92vh] items-center md:items-stretch justify-center">
      <!-- Main Form Card -->
      <div class="w-full max-w-[500px] rounded-[24px] bg-white shadow-2xl flex flex-col justify-between border border-slate-100 overflow-hidden">
        <div class="bg-amber-50 px-6 py-5 shrink-0">
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-start gap-3">
              <div
                class="flex h-11 w-11 shrink-0 items-center justify-center rounded-[6px] bg-amber-500 text-white"
              >
                <Banknote class="h-5 w-5" />
              </div>
              <div>
                <h3 class="text-lg font-extrabold uppercase tracking-wide text-slate-800">
                  Xác nhận hoàn tiền
                </h3>
                <p class="mt-1 text-sm text-slate-500">
                  {{ hoaDon.maHoaDon }} - {{ hoaDon.tenKhachHang }}
                </p>
              </div>
            </div>
            <button
              type="button"
              class="flex h-9 w-9 items-center justify-center rounded-full bg-white text-slate-400 shadow-sm transition hover:text-slate-700"
              @click="hienModalHoanTien = false"
            >
              <X class="h-5 w-5" />
            </button>
          </div>
        </div>
        <div class="space-y-4 px-6 py-5 overflow-y-auto max-h-[60vh] md:max-h-none">
          <div
            class="flex items-center justify-between rounded-[6px] border border-amber-100 bg-amber-50/60 px-4 py-3"
          >
            <span class="text-sm font-bold text-slate-600">Số tiền cần hoàn</span>
            <span class="text-lg font-extrabold text-amber-600">{{
              dinhDangTienKhongDonVi(tongTienHoan)
            }}</span>
          </div>
          <label class="block space-y-2">
            <span class="text-sm font-bold text-slate-600">Phương thức hoàn</span>
            <select
              v-model.number="formHoanTien.hinhThucHoanTien"
              class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 outline-none transition focus:border-amber-300 focus:bg-white focus:ring-4 focus:ring-amber-100"
            >
              <option :value="2">Chuyển khoản</option>
              <option :value="1">Tiền mặt</option>
              <option :value="3">Hoàn qua cổng thanh toán</option>
            </select>
          </label>
          <div v-if="formHoanTien.hinhThucHoanTien === 2" class="space-y-4">
            <div class="space-y-2">
              <span class="text-sm font-bold text-slate-600">
                Tài khoản ngân hàng nhận tiền của khách
              </span>
              <div v-if="dangTaiNganHangKhach" class="text-xs text-slate-400">
                Đang tải danh sách tài khoản...
              </div>
              <select
                v-else-if="dsTaiKhoanNganHangKhach.length > 0"
                v-model="taiKhoanNganHangChon"
                class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 outline-none transition focus:border-amber-300 focus:bg-white focus:ring-4 focus:ring-amber-100"
              >
                <option v-for="tk in dsTaiKhoanNganHangKhach" :key="tk.id" :value="tk">
                  {{ tk.tenNganHang }} - {{ tk.soTaiKhoan }} ({{
                    tk.tenChuTaiKhoan
                  }}) {{ tk.laMacDinh ? "[Mặc định]" : "" }}
                </option>
              </select>
              <div
                v-else
                class="rounded-[6px] border border-rose-100 bg-rose-50/50 px-4 py-3 text-xs font-semibold text-rose-700"
              >
                Khách hàng chưa liên kết tài khoản ngân hàng nào.
              </div>
            </div>
          </div>
          <div class="grid gap-4 sm:grid-cols-2">
            <label class="block space-y-2">
              <span class="text-sm font-bold text-slate-600">Số tiền hoàn</span>
              <input
                v-model="formHoanTien.soTienHoan"
                type="number"
                min="0"
                inputmode="numeric"
                class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 outline-none transition focus:border-amber-300 focus:bg-white focus:ring-4 focus:ring-amber-100"
              />
              <p class="text-[11px] font-semibold text-slate-400 mt-1">
                Định dạng: {{ dinhDangTienKhongDonVi(formHoanTien.soTienHoan) }}
              </p>
            </label>
            <label class="block space-y-2">
              <span class="text-sm font-bold text-slate-600">Mã giao dịch hoàn</span>
              <input
                v-model="formHoanTien.maGiaoDichHoan"
                type="text"
                placeholder="VD: RF20260524..."
                class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-amber-300 focus:bg-white focus:ring-4 focus:ring-amber-100"
              />
            </label>
          </div>
          <label class="block space-y-2">
            <span class="text-sm font-bold text-slate-600">Ghi chú</span>
            <textarea
              v-model="formHoanTien.ghiChu"
              rows="3"
              class="w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-amber-300 focus:bg-white focus:ring-4 focus:ring-amber-100"
            ></textarea>
          </label>
        </div>
        <div class="flex flex-col-reverse gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4 sm:flex-row sm:justify-end shrink-0">
          <button
            type="button"
            class="h-11 rounded-[6px] border border-slate-200 bg-white px-5 text-sm font-bold text-slate-600 transition hover:bg-slate-100"
            @click="hienModalHoanTien = false"
          >
            Hủy
          </button>
          <button
            type="button"
            class="inline-flex h-11 items-center justify-center gap-2 rounded-[6px] bg-amber-500 px-5 text-sm font-bold text-white shadow-[0_12px_28px_rgba(245,158,11,0.24)] transition hover:bg-amber-600 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="
              dangXacNhanHoanTien ||
              (formHoanTien.hinhThucHoanTien === 2 && !taiKhoanNganHangChon)
            "
            @click="handleXacNhanHoanTien"
          >
            <CheckCircle2 class="h-4 w-4" />
            {{ dangXacNhanHoanTien ? "Đang xác nhận..." : "Xác nhận hoàn tiền" }}
          </button>
        </div>
      </div>

      <!-- Separate QR Card -->
      <div
        v-if="formHoanTien.hinhThucHoanTien === 2 && taiKhoanNganHangChon"
        class="w-full md:w-[320px] rounded-[24px] bg-white shadow-2xl border border-slate-100 p-6 flex flex-col items-center justify-center text-center shrink-0 self-center"
      >
        <span class="text-xs font-bold text-slate-500 uppercase tracking-wider">
          Quét mã VietQR để chuyển tiền
        </span>
        <div
          class="my-4 h-48 w-48 overflow-hidden rounded-[16px] border border-slate-200 bg-white p-2.5 flex items-center justify-center shadow-sm"
        >
          <img :src="qrHoanTienUrl" alt="VietQR Hoàn Tiền" class="h-full w-full object-contain" />
        </div>
        <div class="space-y-1.5 text-center">
          <p class="text-sm font-bold text-slate-700">
            Chủ TK:
            <span class="uppercase text-[#B82220]">{{
              taiKhoanNganHangChon.tenChuTaiKhoan
            }}</span>
          </p>
          <p class="text-xs font-semibold text-slate-600">
            STK: {{ taiKhoanNganHangChon.soTaiKhoan }}
          </p>
          <p class="text-xs font-semibold text-slate-500">
            Ngân hàng: {{ taiKhoanNganHangChon.tenNganHang }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
