<script setup>
import TaoPhieuTraHangModal from "../../../../../components/admin/hoa-don/TaoPhieuTraHangModal.vue";
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";
import {
  lopTrangThaiThanhToan,
  nhanTrangThaiThanhToan,
} from "../utils/paymentDisplay";

const {
  Card,
  Button,
  Banknote,
  CircleCheck,
  CircleX,
  History,
  Package,
  Pencil,
  Printer,
  TriangleAlert,
  router,
  hoaDon,
  dangCapNhat,
  coTheThanhToanCod,
  coTheHoanTien,
  donYeuCauHuy,
  donDaHoanThanh,
  donDaXacNhan,
  donDaKetThuc,
  laDonTaiQuay,
  dinhDangGio,
  dinhDangNgay,
  dinhDangTien,
  moModalThanhToanCod,
  moModalHoanTien,
  moModalXacNhanHuy,
  handleXuLyYeuCauHuy,
  handlePrint,
  moModalThongTin,
} = useInvoiceDetailContext();
</script>

<template>
  <div class="space-y-3">
    <Card class="px-5 py-4">
      <template #header>
        <div class="flex items-center justify-between gap-3">
          <h2
            class="flex items-center gap-2 text-[15px] font-semibold text-slate-700"
          >
            <History class="h-4.5 w-4.5 text-slate-500" />
            Lịch Sử Thanh Toán
          </h2>
          <div class="flex flex-wrap items-center justify-end gap-2">
            <button
              v-if="hoaDon.phieuTraHangId"
              type="button"
              class="inline-flex h-9 items-center gap-2 rounded-full border border-[#B82220]/20 bg-[#B82220]/5 px-3.5 text-xs font-bold text-[#B82220] transition hover:bg-[#B82220]/10 active:scale-95"
              @click="
                router.push({
                  name: 'admin-tra-hang-chi-tiet',
                  params: { id: hoaDon.phieuTraHangId },
                })
              "
            >
              <Package class="h-4 w-4" />
              {{
                hoaDon.trangThaiPhieuTraHang === 6
                  ? "Xử lý trả hàng"
                  : "Xem phiếu trả hàng"
              }}
            </button>
            <button
              v-if="coTheThanhToanCod"
              type="button"
              class="inline-flex h-9 items-center gap-2 rounded-full bg-[#B82220] px-3.5 text-xs font-bold text-white shadow-[0_10px_22px_rgba(184,34,32,0.22)] transition hover:bg-[#991b1b] active:scale-95"
              @click="moModalThanhToanCod"
            >
              <Banknote class="h-4 w-4" />
              Thanh toán
            </button>
            <button
              v-if="coTheHoanTien"
              type="button"
              class="inline-flex h-9 items-center gap-2 rounded-full bg-amber-500 px-3.5 text-xs font-bold text-white shadow-[0_10px_22px_rgba(245,158,11,0.22)] transition hover:bg-amber-600 active:scale-95"
              @click="moModalHoanTien"
            >
              <Banknote class="h-4 w-4" />
              Hoàn tiền
            </button>
          </div>
        </div>
      </template>
      <div v-if="hoaDon.lichSuThanhToan?.length" class="mt-4 space-y-3 text-sm">
        <div
          v-for="thanhToan in hoaDon.lichSuThanhToan"
          :key="thanhToan.id"
          class="rounded-[6px] border border-slate-100 bg-slate-50/60 p-3"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <p class="font-semibold text-slate-700">
                {{ thanhToan.phuongThucThanhToan }}
              </p>
              <p class="mt-1 text-xs text-slate-400">
                {{ thanhToan.loaiGiaoDich }}
              </p>
            </div>
            <span
              class="shrink-0 rounded-full px-2.5 py-1 text-[11px] font-semibold"
              :class="lopTrangThaiThanhToan(thanhToan)"
            >
              {{ nhanTrangThaiThanhToan(thanhToan) }}
            </span>
          </div>
          <div class="mt-3 flex items-center justify-between gap-3">
            <span class="text-xs text-slate-400">
              {{ dinhDangGio(thanhToan.thoiGian) }}
              {{ dinhDangNgay(thanhToan.thoiGian) }}
            </span>
            <span class="font-bold text-[#B82220]">{{
              dinhDangTien(thanhToan.tongTien)
            }}</span>
          </div>
          <p v-if="thanhToan.ghiChu" class="mt-2 text-xs text-slate-500">
            {{ thanhToan.ghiChu }}
          </p>
        </div>
      </div>
      <div v-else class="mt-4 text-sm text-slate-400">
        Chưa Có Lịch Sử Thanh Toán.
      </div>
    </Card>
    <div
      v-if="donYeuCauHuy"
      class="rounded-[26px] border border-rose-100 bg-white px-5 py-4 shadow-sm"
    >
      <h2 class="flex items-center gap-2 text-[15px] font-semibold text-[#B82220]">
        <TriangleAlert class="h-4.5 w-4.5" />
        Khách Hàng Yêu Cầu Hủy Đơn
      </h2>
      <p class="mt-2 text-xs text-slate-500">
        Xem lịch sử thao tác để biết lý do yêu cầu hủy.
      </p>
      <div class="mt-4 grid gap-2 sm:grid-cols-2">
        <button
          type="button"
          @click="moModalXacNhanHuy"
          :disabled="dangCapNhat"
          class="inline-flex h-9 items-center justify-center gap-2 rounded-full bg-[#B82220] px-4 text-sm font-semibold text-white transition hover:bg-[#B82220]/90 disabled:cursor-not-allowed disabled:opacity-60"
        >
          <CircleCheck class="h-4 w-4" />
          Xác Nhận Hủy
        </button>
        <button
          type="button"
          @click="handleXuLyYeuCauHuy('Chờ xác nhận')"
          :disabled="dangCapNhat"
          class="inline-flex h-9 items-center justify-center gap-2 rounded-full border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
        >
          <CircleX class="h-4 w-4" />
          Từ Chối Hủy
        </button>
      </div>
    </div>
    <Button
      v-if="donDaHoanThanh || donDaXacNhan"
      @click="handlePrint"
      class="w-full bg-sky-500 hover:bg-sky-600 text-white border-transparent"
    >
      <template #prefix>
        <Printer class="h-4 w-4" />
      </template>
      In Hóa Đơn
    </Button>
    <!-- <TaoPhieuTraHangModal v-if="laDonTaiQuay" :hoa-don="hoaDon" /> -->
    <Button
      v-if="!donDaKetThuc"
      @click="moModalThongTin"
      class="w-full bg-amber-500 hover:bg-amber-600 text-white border-transparent"
    >
      <template #prefix>
        <Pencil class="h-4 w-4" />
      </template>
      Chỉnh Sửa Đơn Hàng
    </Button>
  </div>
</template>
