<script setup>
import { useInvoiceDetailContext } from "../composables/useInvoiceDetailContext";

const {
  Button,
  TriangleAlert,
  hienModalXacNhanHuy,
  dangCapNhat,
  handleXacNhanHuyDon,
} = useInvoiceDetailContext();
</script>

<template>
  <div
    v-if="hienModalXacNhanHuy"
    class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm"
  >
    <div class="w-full max-w-md overflow-hidden rounded-[24px] bg-white shadow-2xl">
      <div class="p-6">
        <div class="flex items-start gap-4">
          <div
            class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-rose-50 text-[#B82220]"
          >
            <TriangleAlert class="h-6 w-6" />
          </div>
          <div>
            <h3 class="text-[19px] font-bold text-slate-800">
              Xác Nhận Hủy Đơn Hàng?
            </h3>
            <p class="mt-2 text-sm leading-6 text-slate-500">
              Bạn có chắc chắn muốn xác nhận hủy đơn hàng này không? Sau khi xác
              nhận, đơn hàng sẽ chuyển sang trạng thái đã hủy.
            </p>
          </div>
        </div>
        <div class="mt-7 flex gap-3">
          <Button
            @click="hienModalXacNhanHuy = false"
            :disabled="dangCapNhat"
            variant="soft"
            class="flex-1"
          >
            Quay Lại
          </Button>
          <Button
            @click="handleXacNhanHuyDon"
            :disabled="dangCapNhat"
            variant="primary"
            class="flex-1"
          >
            {{ dangCapNhat ? "Đang Hủy..." : "Xác Nhận Hủy" }}
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
