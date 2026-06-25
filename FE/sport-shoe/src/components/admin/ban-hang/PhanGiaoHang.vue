<script setup>
import { ref, watch } from "vue";
import ChinhSuaGiaoHangModal from "../../common/ChinhSuaGiaoHangModal.vue";
import { layDanhSachDiaChi } from "../../../services/khach-hang";

const props = defineProps({
  shippingInfo: {
    type: Object,
    required: true
  },
  dinhDangTien: {
    type: Function,
    required: true
  },
  customerId: {
    type: Number,
    default: null
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

const hienModalGiaoHang = ref(false);
const savedAddresses = ref([]);

watch(hienModalGiaoHang, async (newVal) => {
  if (newVal && props.customerId) {
    try {
      savedAddresses.value = await layDanhSachDiaChi(props.customerId);
    } catch (error) {
      console.error("Lỗi khi tải địa chỉ khách hàng:", error);
      savedAddresses.value = [];
    }
  } else if (newVal) {
    savedAddresses.value = [];
  }
});

function handleSaveModal(data) {
  emit("update-shipping", data);
  emit("calculate-shipping");
  hienModalGiaoHang.value = false;
}
</script>

<template>
  <div class="rounded-md border border-slate-200 dark:border-slate-700/60 bg-white dark:bg-slate-800 p-4 shadow-sm relative">
    <div class="flex items-center gap-2 mb-4">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-red-500 dark:text-red-400"><path d="M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2"/><path d="M15 18H9"/><path d="M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.624l-3.48-4.35A1 1 0 0 0 17.52 8H14"/><circle cx="17" cy="18" r="2"/><circle cx="7" cy="18" r="2"/></svg>
      <p class="text-[15px] font-bold text-slate-800 dark:text-slate-100">Thông tin giao hàng</p>
    </div>
    
    <button 
      class="absolute top-4 right-4 text-[13px] font-semibold text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 transition underline"
      @click="hienModalGiaoHang = true"
    >
      Chỉnh sửa
    </button>

    <!-- Inline Form when shipping is enabled -->
    <div>
      <div class="space-y-4">
        <div class="grid gap-4 sm:grid-cols-2">
          <label class="space-y-1.5">
            <span class="block text-xs font-medium text-slate-500 dark:text-slate-400">Người nhận</span>
            <input
              :value="shippingInfo.tenNguoiNhan || ''"
              type="text"
              placeholder="Tên người nhận"
              class="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-[13px] text-slate-900 dark:text-slate-100 outline-none transition focus:border-red-300 dark:focus:border-red-500 focus:bg-white dark:focus:bg-slate-900"
              @input="emit('update-shipping', { tenNguoiNhan: $event.target.value })"
              @blur="emit('calculate-shipping')"
            />
          </label>
          <label class="space-y-1.5">
            <span class="block text-xs font-medium text-slate-500 dark:text-slate-400">Số điện thoại</span>
            <input
              :value="shippingInfo.soDienThoaiNguoiNhan || ''"
              type="text"
              inputmode="numeric"
              placeholder="Số điện thoại"
              class="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-[13px] text-slate-900 dark:text-slate-100 outline-none transition focus:border-red-300 dark:focus:border-red-500 focus:bg-white dark:focus:bg-slate-900"
              @input="emit('update-shipping', { soDienThoaiNguoiNhan: $event.target.value })"
              @blur="emit('calculate-shipping')"
            />
          </label>
        </div>
        
        <label class="space-y-1.5">
          <span class="block text-xs font-medium text-slate-500 dark:text-slate-400">Địa chỉ giao hàng</span>
          <textarea
            :value="shippingInfo.diaChiGiaoHang || ''"
            rows="2"
            placeholder="Địa chỉ giao hàng đầy đủ"
            class="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-[13px] text-slate-900 dark:text-slate-100 outline-none transition focus:border-red-300 dark:focus:border-red-500 focus:bg-white dark:focus:bg-slate-900 custom-scrollbar"
            @input="emit('update-shipping', { diaChiGiaoHang: $event.target.value })"
            @blur="emit('calculate-shipping')"
          />
        </label>
      </div>
    </div>
    
    <ChinhSuaGiaoHangModal
      v-model="hienModalGiaoHang"
      title="Chỉnh sửa thông tin giao hàng"
      :initial-data="shippingInfo"
      :saved-addresses="savedAddresses"
      @save="handleSaveModal"
    />
  </div>
</template>
