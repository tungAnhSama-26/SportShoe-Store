<script setup>
import BanHangCartTable from "./BanHangCartTable.vue";
import BanHangCustomerProductSection from "./BanHangCustomerProductSection.vue";
import BanHangPendingInvoicesSection from "./BanHangPendingInvoicesSection.vue";
import BanHangPaymentSection from "./BanHangPaymentSection.vue";
import BanHangProductDetailModal from "./BanHangProductDetailModal.vue";

defineProps({
  pendingInvoices: {
    type: Array,
    default: () => []
  },
  loadingPendingInvoices: {
    type: Boolean,
    default: false
  },
  maxPendingInvoices: {
    type: Number,
    default: 5
  },
  pendingInvoiceLimitReached: {
    type: Boolean,
    default: false
  },
  activePendingInvoice: {
    type: Object,
    default: null
  },
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
  cartItems: {
    type: Array,
    default: () => []
  },
  selectedProductDetail: {
    type: Object,
    default: null
  },
  chiTietDangChon: {
    type: Object,
    default: null
  },
  hinhAnhDangChon: {
    type: String,
    default: ""
  },
  soLuongTonSauKhiChon: {
    type: Number,
    default: 0
  },
  colorOptions: {
    type: Array,
    default: () => []
  },
  sizeOptions: {
    type: Array,
    default: () => []
  },
  selectedColor: {
    type: String,
    default: ""
  },
  selectedSize: {
    type: String,
    default: ""
  },
  selectedQuantity: {
    type: Number,
    default: 1
  },
  soLuongTonKhaDungChiTiet: {
    type: Number,
    default: 0
  },
  invoiceLoading: {
    type: Boolean,
    default: false
  },
  tongSoLuong: {
    type: Number,
    default: 0
  },
  tongTienSauGiamHienThi: {
    type: Number,
    default: 0
  },
  tienGiam: {
    type: Number,
    default: 0
  },
  tongTien: {
    type: Number,
    default: 0
  },
  couponCode: {
    type: String,
    default: ""
  },
  coTheApDungPhieu: {
    type: Boolean,
    default: false
  },
  applyingCoupon: {
    type: Boolean,
    default: false
  },
  showCouponDropdown: {
    type: Boolean,
    default: false
  },
  coTheTimPhieu: {
    type: Boolean,
    default: false
  },
  loadingCoupons: {
    type: Boolean,
    default: false
  },
  couponResults: {
    type: Array,
    default: () => []
  },
  appliedCoupon: {
    type: Object,
    default: null
  },
  maPhieuChuaApDung: {
    type: Boolean,
    default: false
  },
  khachCanTra: {
    type: Number,
    default: 0
  },
  shippingInfo: {
    type: Object,
    default: () => ({})
  },
  paymentMethod: {
    type: Number,
    default: 1
  },
  amountPaid: {
    type: String,
    default: ""
  },
  tienThua: {
    type: Number,
    default: 0
  },
  paymentNote: {
    type: String,
    default: ""
  },
  canCreatePendingInvoice: {
    type: Boolean,
    default: false
  },
  savingPendingInvoice: {
    type: Boolean,
    default: false
  },
  canPay: {
    type: Boolean,
    default: false
  },
  payingInvoice: {
    type: Boolean,
    default: false
  },
  cancelingPendingInvoice: {
    type: Boolean,
    default: false
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
  "reset-draft",
  "select-invoice",
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
  "increase-item",
  "decrease-item",
  "close-product-detail",
  "select-color",
  "select-size",
  "decrease-quantity",
  "increase-quantity",
  "add-selected-variant",
  "update:couponCode",
  "focus-coupon",
  "blur-coupon",
  "apply-coupon",
  "select-coupon",
  "remove-coupon",
  "update-shipping",
  "calculate-shipping",
  "update:paymentMethod",
  "amount-input",
  "update:paymentNote",
  "create-pending-invoice",
  "pay-now",
  "cancel-pending-invoice"
]);
</script>

<template>
  <div class="p-6">
    <div class="mb-6 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div>
        <h1 class="mt-2 text-3xl font-bold text-slate-900">Bán hàng tại quầy</h1>
      </div>
      <button
        type="button"
        class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-red-300 hover:text-red-500"
        @click="emit('reset-draft')"
      >
        Tạo phiếu mới
      </button>
    </div>

    <BanHangPendingInvoicesSection
      :pending-invoices="pendingInvoices"
      :loading-pending-invoices="loadingPendingInvoices"
      :max-pending-invoices="maxPendingInvoices"
      :pending-invoice-limit-reached="pendingInvoiceLimitReached"
      :active-pending-invoice="activePendingInvoice"
      :dinh-dang-tien="dinhDangTien"
      @select-invoice="emit('select-invoice', $event)"
    />

    <div class="grid gap-6 xl:grid-cols-[1.5fr_0.8fr]">
      <section class="space-y-6 rounded-[32px] border border-white/70 bg-white/95 p-6 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
        <BanHangCustomerProductSection
          :customer-keyword="customerKeyword"
          :loading-customers="loadingCustomers"
          :show-customer-dropdown="showCustomerDropdown"
          :customer-results="customerResults"
          :ten-khach-hang-hien-thi="tenKhachHangHienThi"
          :so-dien-thoai-khach-hang-hien-thi="soDienThoaiKhachHangHienThi"
          :selected-customer="selectedCustomer"
          :is-guest-customer="isGuestCustomer"
          :product-keyword="productKeyword"
          :loading-products="loadingProducts"
          :show-product-dropdown="showProductDropdown"
          :product-results="productResults"
          :product-search-label="productSearchLabel"
          :dinh-dang-tien="dinhDangTien"
          :so-luong-con-lai="soLuongConLai"
          @update:customer-keyword="emit('update:customerKeyword', $event)"
          @focus-customer="emit('focus-customer')"
          @blur-customer="emit('blur-customer')"
          @select-customer="emit('select-customer', $event)"
          @select-guest="emit('select-guest')"
          @clear-customer="emit('clear-customer')"
          @update:product-keyword="emit('update:productKeyword', $event)"
          @focus-product="emit('focus-product')"
          @blur-product="emit('blur-product')"
          @open-product="emit('open-product', $event)"
        />

        <BanHangCartTable
          :cart-items="cartItems"
          :dinh-dang-tien="dinhDangTien"
          :so-luong-con-lai="soLuongConLai"
          @increase-item="emit('increase-item', $event)"
          @decrease-item="emit('decrease-item', $event)"
        />

        <BanHangProductDetailModal
          :selected-product-detail="selectedProductDetail"
          :chi-tiet-dang-chon="chiTietDangChon"
          :current-product-image="hinhAnhDangChon"
          :so-luong-ton-sau-khi-chon="soLuongTonSauKhiChon"
          :color-options="colorOptions"
          :size-options="sizeOptions"
          :selected-color="selectedColor"
          :selected-size="selectedSize"
          :selected-quantity="selectedQuantity"
          :so-luong-ton-kha-dung-chi-tiet="soLuongTonKhaDungChiTiet"
          :dinh-dang-tien="dinhDangTien"
          @close="emit('close-product-detail')"
          @select-color="emit('select-color', $event)"
          @select-size="emit('select-size', $event)"
          @decrease-quantity="emit('decrease-quantity')"
          @increase-quantity="emit('increase-quantity')"
          @add-selected-variant="emit('add-selected-variant')"
        />
      </section>

      <BanHangPaymentSection
        :active-pending-invoice="activePendingInvoice"
        :invoice-loading="invoiceLoading"
        :tong-so-luong="tongSoLuong"
        :tong-tien-sau-giam-hien-thi="tongTienSauGiamHienThi"
        :tien-giam="tienGiam"
        :tong-tien="tongTien"
        :coupon-code="couponCode"
        :co-the-ap-dung-phieu="coTheApDungPhieu"
        :applying-coupon="applyingCoupon"
        :show-coupon-dropdown="showCouponDropdown"
        :co-the-tim-phieu="coTheTimPhieu"
        :loading-coupons="loadingCoupons"
        :coupon-results="couponResults"
        :applied-coupon="appliedCoupon"
        :ma-phieu-chua-ap-dung="maPhieuChuaApDung"
        :khach-can-tra="khachCanTra"
        :shipping-info="shippingInfo"
        :ten-khach-hang-hien-thi="tenKhachHangHienThi"
        :so-dien-thoai-khach-hang-hien-thi="soDienThoaiKhachHangHienThi"
        :payment-method="paymentMethod"
        :amount-paid="amountPaid"
        :tien-thua="tienThua"
        :payment-note="paymentNote"
        :can-create-pending-invoice="canCreatePendingInvoice"
        :saving-pending-invoice="savingPendingInvoice"
        :pending-invoice-limit-reached="pendingInvoiceLimitReached"
        :can-pay="canPay"
        :paying-invoice="payingInvoice"
        :canceling-pending-invoice="cancelingPendingInvoice"
        :dinh-dang-tien="dinhDangTien"
        @update:coupon-code="emit('update:couponCode', $event)"
        @focus-coupon="emit('focus-coupon')"
        @blur-coupon="emit('blur-coupon')"
        @apply-coupon="emit('apply-coupon')"
        @select-coupon="emit('select-coupon', $event)"
        @remove-coupon="emit('remove-coupon')"
        @update-shipping="emit('update-shipping', $event)"
        @calculate-shipping="emit('calculate-shipping')"
        @update:payment-method="emit('update:paymentMethod', $event)"
        @amount-input="emit('amount-input', $event)"
        @update:payment-note="emit('update:paymentNote', $event)"
        @create-pending-invoice="emit('create-pending-invoice')"
        @pay-now="emit('pay-now')"
        @cancel-pending-invoice="emit('cancel-pending-invoice')"
      />
    </div>
  </div>
</template>
