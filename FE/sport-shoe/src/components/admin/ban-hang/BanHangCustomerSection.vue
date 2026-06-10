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
  <div class="flex flex-col gap-2">
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
        <button
          type="button"
          class="shrink-0 rounded-2xl border px-4 py-3 text-sm font-semibold transition"
          :class="isGuestCustomer ? 'border-red-500 bg-red-50 text-red-600' : 'border-dashed border-slate-300 bg-white text-slate-700 hover:border-red-300 hover:text-red-500'"
          @click="isGuestCustomer ? emit('clear-customer') : emit('select-guest')"
        >
          Khách vãng lai
        </button>
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

    <div v-if="selectedCustomer" class="rounded-2xl border border-slate-100 bg-slate-50 p-3">
      <div class="flex items-start justify-between gap-3">
        <div>
          <p class="text-lg font-bold text-slate-900">{{ tenKhachHangHienThi }}</p>
          <p class="mt-1 text-sm text-slate-500">{{ soDienThoaiKhachHangHienThi }}</p>
        </div>
        <button
          type="button"
          class="text-sm font-semibold text-slate-400 transition hover:text-red-500"
          @click="emit('clear-customer')"
        >
          Bỏ chọn
        </button>
      </div>
    </div>
  </div>
</template>
