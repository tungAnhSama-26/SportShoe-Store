<script setup>
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  Card,
  Banknote,
  logoGhn,
  hoaDon,
  tongTienHang,
  tongKhachCanTra,
  coPhieuGiamGia,
  moTaGiaTriPhieuGiamGia,
  dinhDangTien,
  laHoanThanh,
  xuLyMuaLai,
} = useInvoiceDetailContext();
</script>

<template>
  <Card class="flex h-full flex-col px-5 py-4">
    <template #header>
      <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
        <Banknote class="h-4.5 w-4.5 text-slate-500" />
        Tổng Kết Thanh Toán
      </h2>
    </template>
    <div class="mt-4 flex-1 space-y-3 text-sm">
      <div class="flex items-center justify-between">
        <span class="text-slate-500">Tổng Tiền Hàng</span>
        <span class="font-semibold text-slate-700">{{
          dinhDangTien(tongTienHang)
        }}</span>
      </div>
      <div v-if="coPhieuGiamGia" class="flex items-center justify-between">
        <span class="text-slate-500">
          Phiếu Giảm Giá
          <span class="ml-1 font-semibold text-slate-600">
            {{ hoaDon.voucher }}
          </span>
        </span>
        <span class="font-semibold text-emerald-500">{{
          moTaGiaTriPhieuGiamGia
        }}</span>
      </div>
      <div
        v-if="Number(hoaDon.phiVanChuyen || 0) > 0"
        class="flex items-center justify-between"
      >
        <span class="flex items-center gap-2 text-slate-500">
          Phí Vận Chuyển
          <img :src="logoGhn" alt="GHN" class="h-4 w-auto object-contain" />
        </span>
        <span class="font-semibold text-slate-700"
          >+ {{ dinhDangTien(hoaDon.phiVanChuyen) }}</span
        >
      </div>
      <div class="border-t border-slate-200 pt-4">
        <div class="flex items-center justify-between">
          <span class="text-[15px] font-bold tracking-wide text-[#B82220]"
            >Tổng Tiền</span
          >
          <span class="text-[18px] font-bold text-[#B82220]">{{
            dinhDangTien(tongKhachCanTra)
          }}</span>
        </div>
      </div>
      
      <div v-if="laHoanThanh" class="mt-4 pt-4 border-t border-slate-100">
        <button
          @click="xuLyMuaLai"
          class="w-full flex items-center justify-center gap-2 rounded-xl bg-[#B82220] text-white py-2.5 px-4 text-sm font-semibold hover:bg-[#B82220]/95 transition duration-200"
        >
          <svg class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 1121.21 7.89H18" />
          </svg>
          Mua lại
        </button>
      </div>
    </div>
  </Card>
</template>
