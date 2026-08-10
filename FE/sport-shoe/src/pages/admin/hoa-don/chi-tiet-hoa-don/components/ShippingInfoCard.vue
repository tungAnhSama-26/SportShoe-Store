<script setup>
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";
import { dinhDangDiaChi } from "../../../../../utils/dia-chi";

const { Card, MapPin, hoaDon } = useInvoiceDetailContext();
</script>

<template>
  <Card class="px-5 py-4">
    <template #header>
      <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
        <MapPin class="h-4.5 w-4.5 text-slate-500" />
        Thông Tin Giao Hàng
      </h2>
    </template>
    <div class="mt-4 space-y-4 text-sm">
      <div class="flex items-center justify-between border-b border-slate-100 pb-3">
        <span class="text-slate-400">Địa Chỉ</span>
        <span class="max-w-[58%] text-right font-semibold text-slate-700">{{
          dinhDangDiaChi(hoaDon.diaChi) || "—"
        }}</span>
      </div>
      <div class="flex items-center justify-between border-b border-slate-100 pb-3">
        <span class="text-slate-400">Loại Đơn</span>
        <span class="font-semibold text-slate-700">{{ hoaDon.loaiDon }}</span>
      </div>
      <div class="flex items-center justify-between">
        <span class="text-slate-400">Ghi Chú</span>
        <span class="max-w-[58%] text-right font-semibold text-slate-700">{{
          hoaDon.ghiChu || "—"
        }}</span>
      </div>
    </div>
  </Card>
</template>
