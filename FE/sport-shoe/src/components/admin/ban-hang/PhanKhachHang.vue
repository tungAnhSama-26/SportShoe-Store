<script setup>
defineProps({
  customerKeyword: {
    type: String,
    default: ""
  },
  loadingCustomers: {
    type: Boolean,
    default: false
  },
  showCustomerDropdown: {
    type: Boolean,
    default: false
  },
  customerResults: {
    type: Array,
    default: () => []
  },
  tenKhachHangHienThi: {
    type: String,
    default: ""
  },
  soDienThoaiKhachHangHienThi: {
    type: String,
    default: ""
  },
  selectedCustomer: {
    type: Object,
    default: null
  },
  isGuestCustomer: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits([
  "update:customerKeyword",
  "focus-customer",
  "blur-customer",
  "select-customer",
  "select-guest",
  "clear-customer"
]);
</script>

<template>
  <div class="flex flex-col gap-2 w-full">
    <div v-if="!selectedCustomer" class="relative">

      <div class="flex gap-3">
        <input
          :value="customerKeyword"
          type="text"
          placeholder="Nhập tên hoặc số điện thoại khách hàng"
          class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300 focus:bg-white"
          @input="emit('update:customerKeyword', $event.target.value)"
          @focus="emit('focus-customer')"
          @blur="emit('blur-customer')"
        />
      </div>
      <div v-if="loadingCustomers" class="absolute right-4 top-[46px] text-xs font-semibold text-slate-400">
        Đang tìm...
      </div>

      <div
        v-if="showCustomerDropdown"
        class="absolute z-20 mt-2 w-full rounded-2xl border border-slate-200 bg-white p-2 shadow-[0_24px_50px_rgba(15,23,42,0.12)]"
      >
        <div v-if="!loadingCustomers && !customerResults.length" class="rounded-2xl px-3 py-3 text-sm text-slate-500">
          Không tìm thấy khách hàng phù hợp.
        </div>
        <button
          v-for="customer in customerResults"
          :key="customer.id"
          type="button"
          class="w-full rounded-2xl px-3 py-3 text-left transition hover:bg-red-50"
          @click="emit('select-customer', customer)"
        >
          <p class="text-sm font-semibold text-slate-900">{{ customer.hoTen }}</p>
          <p class="mt-1 text-xs text-slate-500">{{ customer.sdt }} <span v-if="customer.email">- {{ customer.email }}</span></p>
        </button>
      </div>
    </div>

    <div v-if="selectedCustomer" class="rounded-xl border border-slate-100 bg-slate-50 px-3 py-2">
      <div class="flex items-center justify-between gap-2">
        <div>
          <p class="text-sm font-bold text-slate-900">{{ tenKhachHangHienThi }}</p>
          <p class="text-xs text-slate-500">{{ soDienThoaiKhachHangHienThi }}</p>
        </div>
        <button
          type="button"
          class="text-xs font-semibold text-slate-400 transition hover:text-red-500"
          @click="emit('clear-customer')"
        >
          Bỏ chọn
        </button>
      </div>
    </div>
  </div>
</template>
