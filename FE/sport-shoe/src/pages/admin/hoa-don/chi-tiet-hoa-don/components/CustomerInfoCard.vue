<script setup>
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const { Card, User, hoaDon } = useInvoiceDetailContext();
</script>

<template>
  <Card class="px-5 py-4">
    <template #header>
      <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
        <User class="h-4.5 w-4.5 text-slate-500" />
        Thông Tin Khách Hàng
      </h2>
    </template>
    <div class="mt-4 space-y-4 text-sm">
      <div class="flex items-center justify-between border-b border-slate-100 pb-3">
        <span class="text-slate-400">Tên Khách Hàng</span>
        <span class="font-semibold text-slate-700">{{ hoaDon.tenKhachHang }}</span>
      </div>
      <div class="flex items-center justify-between border-b border-slate-100 pb-3">
        <span class="text-slate-400">Số Điện Thoại</span>
        <span class="font-semibold text-slate-700">{{ hoaDon.soDienThoai }}</span>
      </div>
      <div class="flex items-center justify-between">
        <span class="text-slate-400">Email</span>
        <span class="font-semibold text-slate-700">{{ hoaDon.email || "—" }}</span>
      </div>
    </div>
  </Card>
</template>
