<script setup>
import BanHangCustomerSection from "./BanHangCustomerSection.vue";
import BanHangProductSection from "./BanHangProductSection.vue";

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
  },
  productKeyword: {
    type: String,
    default: ""
  },
  loadingProducts: {
    type: Boolean,
    default: false
  },
  showProductDropdown: {
    type: Boolean,
    default: false
  },
  productResults: {
    type: Array,
    default: () => []
  },
  productSearchLabel: {
    type: String,
    default: ""
  },
  dinhDangTien: {
    type: Function,
    required: true
  },
  soLuongConLai: {
    type: Function,
    required: true
  }
});

const emit = defineEmits([
  "update:customerKeyword",
  "focus-customer",
  "blur-customer",
  "select-customer",
  "select-guest",
  "clear-customer",
  "update:productKeyword",
  "focus-product",
  "blur-product",
  "open-product",
  "scan-product",
]);
</script>

<template>
  <div class="space-y-6">
    <BanHangCustomerSection
      :customer-keyword="customerKeyword"
      :loading-customers="loadingCustomers"
      :show-customer-dropdown="showCustomerDropdown"
      :customer-results="customerResults"
      :ten-khach-hang-hien-thi="tenKhachHangHienThi"
      :so-dien-thoai-khach-hang-hien-thi="soDienThoaiKhachHangHienThi"
      :selected-customer="selectedCustomer"
      :is-guest-customer="isGuestCustomer"
      @update:customer-keyword="emit('update:customerKeyword', $event)"
      @focus-customer="emit('focus-customer')"
      @blur-customer="emit('blur-customer')"
      @select-customer="emit('select-customer', $event)"
      @select-guest="emit('select-guest')"
      @clear-customer="emit('clear-customer')"
    />

    <BanHangProductSection
      :product-keyword="productKeyword"
      :loading-products="loadingProducts"
      :show-product-dropdown="showProductDropdown"
      :product-results="productResults"
      :product-search-label="productSearchLabel"
      :dinh-dang-tien="dinhDangTien"
      :so-luong-con-lai="soLuongConLai"
      @update:product-keyword="emit('update:productKeyword', $event)"
      @focus-product="emit('focus-product')"
      @blur-product="emit('blur-product')"
      @open-product="emit('open-product', $event)"
      @scan-product="emit('scan-product', $event)"
    />
  </div>
</template>
