<script setup>
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  Button,
  hienModalXacNhan,
  dangCapNhat,
  trangThaiMoiXacNhan,
  ghiChuXacNhan,
  handleXacNhanTrangThai,
} = useInvoiceDetailContext();
</script>

<template>
  <div
    v-if="hienModalXacNhan"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
  >
    <div class="w-full max-w-md overflow-hidden rounded-[28px] bg-white shadow-2xl">
      <div class="p-7">
        <h3 class="text-xl font-bold text-slate-800">
          Xác Nhận Cập Nhật Trạng Thái
        </h3>
        <p class="mt-2 text-sm text-slate-400">
          Cập Nhật Đơn Hàng Sang Trạng Thái
          <span class="font-semibold text-slate-700">{{
            trangThaiMoiXacNhan
          }}</span>
        </p>
        <div class="mt-6">
          <label
            class="mb-2 block text-xs font-semibold uppercase tracking-wide text-slate-400"
            >Ghi Chú</label
          >
          <textarea
            v-model="ghiChuXacNhan"
            rows="4"
            placeholder="Nhập Ghi Chú Cho Bước Này..."
            class="w-full rounded-[6px] border border-slate-200 bg-slate-50 p-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
          ></textarea>
        </div>
        <div class="mt-7 flex gap-3">
          <Button @click="hienModalXacNhan = false" variant="soft" class="flex-1">
            Đóng
          </Button>
          <Button
            @click="handleXacNhanTrangThai"
            :disabled="dangCapNhat"
            variant="primary"
            class="flex-1"
          >
            {{ dangCapNhat ? "Đang Lưu..." : "Xác Nhận" }}
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
