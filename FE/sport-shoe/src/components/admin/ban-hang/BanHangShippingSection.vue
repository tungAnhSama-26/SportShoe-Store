<script setup>
import { ref } from "vue";
import { Pencil, X } from "lucide-vue-next";
import ghnLogo from "../../../assets/logo/Logo-GHN-Blue-Orange.webp";

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

const showModal = ref(false);

function handleToggle(e) {
  const isChecked = e.target.checked;
  emit("update-shipping", { giaoHang: isChecked });
  if (isChecked) {
    showModal.value = true;
  }
}
</script>

<template>
  <div class="rounded-3xl border border-slate-200 bg-white p-4 shadow-sm">
    <div class="flex items-center justify-between gap-3">
      <div class="flex items-center gap-2">
        <p class="text-sm font-semibold text-slate-800">Giao hàng</p>
        <img :src="ghnLogo" alt="GHN" class="h-4 object-contain" />
      </div>
      <label class="relative inline-flex cursor-pointer items-center gap-3">
        <div class="relative flex items-center">
          <input
            type="checkbox"
            class="peer sr-only"
            :checked="shippingInfo.giaoHang"
            @change="handleToggle"
          />
          <div class="h-6 w-11 rounded-full bg-slate-200 transition-colors peer-checked:bg-red-500 peer-focus:outline-none"></div>
          <div class="absolute left-[2px] top-[2px] h-5 w-5 rounded-full border border-slate-300 bg-white transition-all peer-checked:translate-x-full peer-checked:border-white"></div>
        </div>
      </label>
    </div>

    <!-- Summary UI when shipping is enabled -->
    <div
      v-if="shippingInfo.giaoHang"
      class="mt-3 flex cursor-pointer items-center justify-between gap-2 rounded-2xl bg-slate-50 p-3 transition hover:bg-slate-100"
      @click="showModal = true"
    >
      <div class="min-w-0 flex-1 text-sm">
        <div class="font-semibold text-slate-800 truncate">
          {{ shippingInfo.tenNguoiNhan || "Chưa nhập người nhận" }} - {{ shippingInfo.soDienThoaiNguoiNhan || "SĐT trống" }}
        </div>
        <div class="mt-0.5 text-xs text-slate-500 truncate">
          {{ shippingInfo.diaChiGiaoHang || "Chưa nhập địa chỉ giao hàng" }}
        </div>
        <div class="mt-1 font-bold text-red-500">
          Phí ship: {{ shippingInfo.daTinhPhi ? dinhDangTien(shippingInfo.phiVanChuyen || 0) : "Chưa tính" }}
        </div>
      </div>
      <div class="shrink-0 p-2 text-slate-400 hover:text-slate-600">
        <Pencil class="h-4 w-4" />
      </div>
    </div>

    <!-- Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showModal" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 p-4 backdrop-blur-sm transition-all" @click.self="showModal = false">
          <div class="modal-content flex max-h-[90vh] w-full max-w-2xl flex-col overflow-hidden rounded-[24px] bg-white shadow-2xl">
            <!-- Header -->
            <div class="flex shrink-0 items-center justify-between border-b border-slate-100 px-6 py-4">
              <h3 class="text-lg font-bold text-slate-800">Thông tin giao hàng</h3>
              <button @click="showModal = false" class="rounded-full p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600">
                <X class="h-5 w-5" />
              </button>
            </div>
            
            <!-- Body -->
            <div class="flex-1 overflow-y-auto p-6 custom-scrollbar">
              <div class="space-y-4">
                <div class="grid gap-4 sm:grid-cols-2">
                  <label class="space-y-2">
                    <span class="block text-xs font-medium text-slate-500">Người nhận</span>
                    <input
                      :value="shippingInfo.tenNguoiNhan || ''"
                      type="text"
                      placeholder="Nhập tên người nhận"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                      @input="emit('update-shipping', { tenNguoiNhan: $event.target.value })"
                    />
                  </label>
                  <label class="space-y-2">
                    <span class="block text-xs font-medium text-slate-500">Số điện thoại</span>
                    <input
                      :value="shippingInfo.soDienThoaiNguoiNhan || ''"
                      type="text"
                      inputmode="numeric"
                      placeholder="Nhập số điện thoại"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                      @input="emit('update-shipping', { soDienThoaiNguoiNhan: $event.target.value })"
                    />
                  </label>
                </div>

                <label class="space-y-2">
                  <span class="block text-xs font-medium text-slate-500">Địa chỉ giao hàng</span>
                  <textarea
                    :value="shippingInfo.diaChiGiaoHang || ''"
                    rows="2"
                    placeholder="Nhập địa chỉ giao hàng đầy đủ"
                    class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300 custom-scrollbar"
                    @input="emit('update-shipping', { diaChiGiaoHang: $event.target.value })"
                  />
                </label>

                <div v-if="shippingInfo.diaChiDaDo" class="rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-600 border border-slate-100">
                  GHN dò: <span class="font-semibold text-slate-800">{{ shippingInfo.diaChiDaDo }}</span>
                </div>

                <div class="grid gap-4 sm:grid-cols-2">
                  <label class="space-y-2">
                    <span class="block text-xs font-medium text-slate-500">Loại dịch vụ</span>
                    <select
                      :value="shippingInfo.serviceTypeId ?? 2"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                      @change="emit('update-shipping', { serviceTypeId: Number($event.target.value) })"
                    >
                      <option :value="2">Hàng nhẹ</option>
                      <option :value="5">Hàng nặng</option>
                    </select>
                  </label>
                  <label class="space-y-2">
                    <span class="block text-xs font-medium text-slate-500">Cân nặng (gram)</span>
                    <input
                      :value="shippingInfo.weight ?? 500"
                      type="number"
                      min="1"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                      @input="emit('update-shipping', { weight: Number($event.target.value) })"
                    />
                  </label>
                  <label class="space-y-2">
                    <span class="block text-xs font-medium text-slate-500">Dài (cm)</span>
                    <input
                      :value="shippingInfo.length ?? 30"
                      type="number"
                      min="1"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                      @input="emit('update-shipping', { length: Number($event.target.value) })"
                    />
                  </label>
                  <label class="space-y-2">
                    <span class="block text-xs font-medium text-slate-500">Rộng (cm)</span>
                    <input
                      :value="shippingInfo.width ?? 20"
                      type="number"
                      min="1"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                      @input="emit('update-shipping', { width: Number($event.target.value) })"
                    />
                  </label>
                  <label class="space-y-2 sm:col-span-2">
                    <span class="block text-xs font-medium text-slate-500">Cao (cm)</span>
                    <input
                      :value="shippingInfo.height ?? 12"
                      type="number"
                      min="1"
                      class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                      @input="emit('update-shipping', { height: Number($event.target.value) })"
                    />
                  </label>
                </div>
              </div>
            </div>

            <!-- Footer -->
            <div class="flex shrink-0 items-center justify-between border-t border-slate-100 bg-slate-50 px-6 py-4">
              <div class="min-w-0 flex-1">
                <p class="text-xs font-medium uppercase tracking-[0.18em] text-slate-400">Phí ship</p>
                <p class="mt-1 text-lg font-bold text-slate-900">
                  {{ shippingInfo.daTinhPhi ? dinhDangTien(shippingInfo.phiVanChuyen || 0) : "Chưa tính" }}
                </p>
              </div>
              <div class="flex shrink-0 items-center gap-3">
                <button
                  type="button"
                  class="rounded-xl bg-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-300"
                  @click="showModal = false"
                >
                  Xong
                </button>
                <button
                  type="button"
                  class="rounded-xl bg-red-500 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-red-600 disabled:cursor-not-allowed disabled:bg-slate-300 shadow-sm"
                  :disabled="!shippingInfo.coTheTinhPhi"
                  @click="emit('calculate-shipping')"
                >
                  {{ shippingInfo.dangTinhPhi ? "Đang tính..." : "Tính phí GHN" }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.25s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  opacity: 0;
  transform: scale(0.95) translateY(10px);
}
</style>
