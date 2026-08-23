<script setup>
import { computed } from "vue";
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";
import { dinhDangDiaChi, diaChiHopLe } from "../../../../../utils/dia-chi";
import {
  hoaDonDaCoShipperGhn,
  layShipperGhnTheoHoaDon,
} from "../../../../../utils/ghn-shipper";

const { Card, MapPin, hoaDon, laDonTaiQuay } = useInvoiceDetailContext();

const thongTinShipper = computed(() => {
  if (
    !hoaDon.value ||
    laDonTaiQuay.value ||
    !hoaDonDaCoShipperGhn(hoaDon.value)
  ) {
    return null;
  }

  return layShipperGhnTheoHoaDon(hoaDon.value);
});

const loaiDonHienThi = computed(() => {
  if (hoaDon.value?.loaiDon === "Trực tuyến" || hoaDon.value?.loaiDon === "Online") {
    return "Trực tuyến";
  }
  if (hoaDon.value?.loaiDon === "Giao hàng") {
    return "Giao hàng";
  }
  if (hoaDon.value?.loaiDon === "Cửa hàng" || hoaDon.value?.loaiDon === "Tại quầy") {
    return "Tại quầy";
  }
  return hoaDon.value?.loaiDon || "—";
});
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
      <div v-if="thongTinShipper" class="border-b border-slate-100 pb-4">
        <div class="mb-2.5 flex items-center justify-between gap-2">
          <span class="text-slate-400">Người Giao Hàng</span>
          <span class="rounded-full bg-orange-50 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide text-orange-600">
            {{ thongTinShipper.donVi }}
          </span>
        </div>
        <div class="flex items-center gap-3 rounded-xl border border-orange-100 bg-orange-50/60 p-3">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-orange-500 text-white shadow-sm">
            <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="8" r="4" />
              <path d="M4.5 21a7.5 7.5 0 0 1 15 0" />
            </svg>
          </div>
          <div class="min-w-0 flex-1">
            <p class="truncate font-semibold text-slate-800">{{ thongTinShipper.hoTen }}</p>
            <p class="mt-0.5 text-xs text-slate-500">{{ thongTinShipper.ma }}</p>
          </div>
          <a
            :href="`tel:${thongTinShipper.soDienThoai}`"
            class="shrink-0 text-right text-xs font-semibold text-orange-600 transition hover:text-orange-700"
          >
            {{ thongTinShipper.soDienThoai }}
          </a>
        </div>
      </div>
      <div class="flex items-center justify-between border-b border-slate-100 pb-3">
        <span class="text-slate-400">Loại Đơn</span>
        <span class="font-semibold text-slate-700">{{ loaiDonHienThi }}</span>
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
