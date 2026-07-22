<script setup>
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const { Button, ArrowLeft, router, hoaDon, dinhDangGio, dinhDangNgay } =
  useInvoiceDetailContext();
</script>

<template>
  <section
    class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between border-b border-slate-100 pb-6"
  >
    <div v-if="hoaDon" class="space-y-1 text-sm text-slate-500">
      <p>
        Mã Đơn Hàng:
        <span class="font-semibold text-slate-700">{{ hoaDon.maHoaDon }}</span>
        <span class="mx-2 text-slate-300">|</span>
        Ngày Tạo: {{ dinhDangGio(hoaDon.ngayTao) }}
        {{ dinhDangNgay(hoaDon.ngayTao) }}
      </p>
      <p>
        Tạo Bởi:
        <span class="font-medium text-slate-700">{{
          hoaDon.nguoiTao || "Hệ Thống"
        }}</span>
        <span class="mx-2 text-slate-300">|</span>
        Cập Nhật Gần Nhất:
        <span class="font-medium text-slate-700">
          {{
            hoaDon.lichSuHoaDon?.[0]
              ? `${dinhDangGio(hoaDon.lichSuHoaDon[0].ngayTao)} ${dinhDangNgay(hoaDon.lichSuHoaDon[0].ngayTao)} -
            ${hoaDon.lichSuHoaDon[0].maNhanVien}`
              : "Chưa Có"
          }}
        </span>
      </p>
    </div>
    <div class="flex items-center gap-2 shrink-0">
      <Button
        @click="router.push('/admin/ban-hang')"
        class="h-10 shrink-0 !bg-red-500 hover:!bg-red-600 !text-white font-bold shadow-sm"
      >
        Quay lại Bán hàng tại quầy
      </Button>
      <Button
        variant="soft"
        @click="router.push({ name: 'admin-hoa-don' })"
        class="h-10 shrink-0"
      >
        <template #prefix>
          <ArrowLeft class="h-4 w-4" />
        </template>
        Quay Lại Danh Sách
      </Button>
    </div>
  </section>
</template>
