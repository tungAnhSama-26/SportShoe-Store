<script setup>
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  Banknote,
  CheckCircle2,
  X,
  hoaDon,
  hienModalThanhToanCod,
  dangXacNhanThanhToanCod,
  formThanhToanCod,
  tongTienThanhToanCod,
  noiDungChuyenKhoanCod,
  qrThanhToanCodUrl,
  tienThieuThanhToanCod,
  dinhDangTien,
  handleXacNhanThanhToanCod,
} = useInvoiceDetailContext();
</script>

<template>
  <div
    v-if="hienModalThanhToanCod"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-3 backdrop-blur-sm"
  >
    <div
      class="flex max-h-[calc(100vh-1.5rem)] w-full max-w-[780px] flex-col overflow-hidden rounded-[24px] border border-white/70 bg-white shadow-[0_28px_90px_rgba(15,23,42,0.28)]"
    >
      <div
        class="relative shrink-0 bg-gradient-to-br from-white via-rose-50/70 to-white px-5 pb-3 pt-4"
      >
        <button
          type="button"
          class="absolute right-5 top-5 inline-flex h-9 w-9 items-center justify-center rounded-full bg-white/80 text-slate-400 shadow-sm transition hover:bg-white hover:text-slate-700"
          @click="hienModalThanhToanCod = false"
        >
          <X class="h-5 w-5" />
        </button>
        <div class="flex items-center gap-3">
          <div
            class="flex h-11 w-11 items-center justify-center rounded-[6px] bg-[#B82220] text-white shadow-[0_14px_30px_rgba(184,34,32,0.25)]"
          >
            <Banknote class="h-6 w-6" />
          </div>
          <div>
            <h3 class="text-lg font-extrabold uppercase tracking-wide text-slate-900">
              Thanh toán COD
            </h3>
            <p class="mt-1 text-sm text-slate-500">
              {{ hoaDon.maHoaDon }} - {{ hoaDon.tenKhachHang }}
            </p>
          </div>
        </div>
        <div class="mt-4 rounded-[6px] border border-rose-100 bg-white/80 px-4 py-3">
          <div class="flex items-center justify-between">
            <span class="text-sm font-semibold text-slate-500">Tổng tiền hàng</span>
            <span class="text-lg font-extrabold text-[#B82220]">{{
              dinhDangTien(tongTienThanhToanCod)
            }}</span>
          </div>
        </div>
      </div>
      <div class="min-h-0 flex-1 space-y-3 overflow-hidden px-5 py-4">
        <div class="grid grid-cols-2 gap-2 rounded-[6px] bg-slate-100 p-1">
          <button
            type="button"
            class="h-10 rounded-[6px] text-sm font-bold outline-none transition focus-visible:ring-4 focus-visible:ring-rose-100"
            :class="
              formThanhToanCod.hinhThucThanhToan === 2
                ? 'bg-[#B82220] text-white shadow-sm'
                : 'text-slate-500 hover:bg-white/70'
            "
            @click="formThanhToanCod.hinhThucThanhToan = 2"
          >
            Chuyển khoản
          </button>
          <button
            type="button"
            class="h-10 rounded-[6px] text-sm font-bold outline-none transition focus-visible:ring-4 focus-visible:ring-rose-100"
            :class="
              formThanhToanCod.hinhThucThanhToan === 1
                ? 'bg-[#B82220] text-white shadow-sm'
                : 'text-slate-500 hover:bg-white/70'
            "
            @click="formThanhToanCod.hinhThucThanhToan = 1"
          >
            Tiền mặt
          </button>
        </div>
        <div
          class="grid gap-4"
          :class="
            formThanhToanCod.hinhThucThanhToan === 2
              ? 'lg:grid-cols-[1.05fr_0.95fr]'
              : 'place-items-center'
          "
        >
          <div
            v-if="formThanhToanCod.hinhThucThanhToan === 2"
            class="rounded-[6px] border border-slate-200 bg-white p-4 text-center shadow-sm"
          >
            <div class="space-y-1 text-sm">
              <p>
                <span class="font-bold text-slate-800">Ngân hàng:</span>
                <span class="text-slate-600">VCB Bank</span>
              </p>
              <p>
                <span class="font-bold text-slate-800">Số tài khoản:</span>
                <span class="text-slate-600">0965852782</span>
              </p>
              <p>
                <span class="font-bold text-slate-800">Nội dung:</span>
                <span class="text-[#B82220]">{{ noiDungChuyenKhoanCod }}</span>
              </p>
            </div>
            <div
              class="mx-auto mt-3 flex w-fit rounded-[6px] border border-slate-200 bg-white p-3 shadow-inner"
            >
              <img
                :src="qrThanhToanCodUrl"
                alt="QR thanh toán COD"
                class="h-52 w-52 object-contain"
              />
            </div>
            <p class="mt-2 text-xs text-slate-400">
              Quét mã để thanh toán đúng số tiền.
            </p>
          </div>
          <div
            class="w-full space-y-3"
            :class="formThanhToanCod.hinhThucThanhToan === 1 ? 'max-w-[420px]' : ''"
          >
            <label v-if="formThanhToanCod.hinhThucThanhToan === 1" class="block space-y-2">
              <span class="text-sm font-bold text-slate-600">Tiền khách đưa</span>
              <input
                v-model="formThanhToanCod.tienKhachDua"
                type="number"
                min="0"
                inputmode="numeric"
                placeholder="Nhập số tiền..."
                class="h-11 w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-800 outline-none transition focus:border-rose-300 focus:bg-white focus:ring-4 focus:ring-rose-100"
              />
            </label>
            <div class="overflow-hidden rounded-[6px] border border-slate-200">
              <table class="w-full text-sm">
                <thead class="bg-slate-50 text-xs font-bold text-slate-500">
                  <tr>
                    <th class="px-3 py-2.5 text-left">STT</th>
                    <th class="px-3 py-2.5 text-left">Phương thức</th>
                    <th class="px-3 py-2.5 text-right">Số tiền</th>
                  </tr>
                </thead>
                <tbody>
                  <tr class="border-t border-slate-100">
                    <td class="px-3 py-3 font-semibold text-slate-500">1</td>
                    <td class="px-3 py-3 font-semibold text-slate-700">
                      {{
                        formThanhToanCod.hinhThucThanhToan === 2
                          ? "Chuyển khoản"
                          : "Tiền mặt"
                      }}
                    </td>
                    <td class="px-3 py-3 text-right font-bold text-[#B82220]">
                      {{ dinhDangTien(tongTienThanhToanCod) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <label class="block space-y-2">
              <span class="text-sm font-bold text-slate-600">Ghi chú</span>
              <textarea
                v-model="formThanhToanCod.ghiChu"
                rows="2"
                class="w-full rounded-[6px] border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white focus:ring-4 focus:ring-rose-100"
              ></textarea>
            </label>
            <div class="flex items-center justify-between rounded-[6px] bg-rose-50 px-4 py-3">
              <span class="text-sm font-bold text-slate-600">Tiền thiếu</span>
              <span class="text-lg font-extrabold text-[#B82220]">{{
                dinhDangTien(tienThieuThanhToanCod)
              }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="shrink-0 border-t border-slate-100 bg-slate-50/90 px-5 py-4 sm:px-6">
        <div class="flex flex-col-reverse gap-3 sm:flex-row sm:items-center sm:justify-end">
          <button
            type="button"
            class="h-11 rounded-[6px] border border-slate-200 bg-white px-5 text-sm font-bold text-slate-600 outline-none transition hover:bg-slate-100 focus-visible:ring-4 focus-visible:ring-slate-200"
            @click="hienModalThanhToanCod = false"
          >
            Hủy
          </button>
          <button
            type="button"
            class="inline-flex h-11 items-center justify-center gap-2 rounded-[6px] bg-[#B82220] px-5 text-sm font-bold text-white shadow-[0_12px_28px_rgba(184,34,32,0.24)] outline-none transition hover:bg-[#991b1b] focus-visible:ring-4 focus-visible:ring-rose-100 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="dangXacNhanThanhToanCod"
            @click="handleXacNhanThanhToanCod"
          >
            <CheckCircle2 class="h-4 w-4" />
            {{
              dangXacNhanThanhToanCod
                ? "Đang xác nhận..."
                : "Xác nhận thanh toán"
            }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
