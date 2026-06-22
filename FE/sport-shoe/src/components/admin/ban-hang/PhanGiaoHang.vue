<script setup>
const props = defineProps({
  shippingInfo: {
    type: Object,
    required: true
  },
  dinhDangTien: {
    type: Function,
    required: true
  }
});

const emit = defineEmits([
  "update-shipping",
  "calculate-shipping"
]);

function handleToggle(e) {
  const isChecked = e.target.checked;
  emit("update-shipping", { giaoHang: isChecked });
}
</script>

<template>
  <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
    <div class="flex items-center gap-2 mb-4">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-500"><path d="M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2"/><path d="M15 18H9"/><path d="M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.624l-3.48-4.35A1 1 0 0 0 17.52 8H14"/><circle cx="17" cy="18" r="2"/><circle cx="7" cy="18" r="2"/></svg>
      <p class="text-[15px] font-bold text-slate-800">Thông tin giao hàng</p>
    </div>

    <!-- Inline Form when shipping is enabled -->
    <div>
      <div class="space-y-4">
        <div class="grid gap-4 sm:grid-cols-2">
          <label class="space-y-1.5">
            <span class="block text-xs font-medium text-slate-500">Người nhận</span>
            <input
              :value="shippingInfo.tenNguoiNhan || ''"
              type="text"
              placeholder="Tên người nhận"
              class="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-[13px] text-slate-900 outline-none transition focus:border-blue-300 focus:bg-white"
              @input="emit('update-shipping', { tenNguoiNhan: $event.target.value })"
              @blur="emit('calculate-shipping')"
            />
          </label>
          <label class="space-y-1.5">
            <span class="block text-xs font-medium text-slate-500">Số điện thoại</span>
            <input
              :value="shippingInfo.soDienThoaiNguoiNhan || ''"
              type="text"
              inputmode="numeric"
              placeholder="Số điện thoại"
              class="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-[13px] text-slate-900 outline-none transition focus:border-blue-300 focus:bg-white"
              @input="emit('update-shipping', { soDienThoaiNguoiNhan: $event.target.value })"
              @blur="emit('calculate-shipping')"
            />
          </label>
        </div>
        
        <label class="space-y-1.5">
          <span class="block text-xs font-medium text-slate-500">Địa chỉ giao hàng</span>
          <textarea
            :value="shippingInfo.diaChiGiaoHang || ''"
            rows="2"
            placeholder="Địa chỉ giao hàng đầy đủ"
            class="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-[13px] text-slate-900 outline-none transition focus:border-blue-300 focus:bg-white custom-scrollbar"
            @input="emit('update-shipping', { diaChiGiaoHang: $event.target.value })"
            @blur="emit('calculate-shipping')"
          />
        </label>
      </div>
    </div>
  </div>
</template>
