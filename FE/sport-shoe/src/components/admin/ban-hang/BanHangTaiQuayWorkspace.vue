<script setup>
import BanHangCartTable from "./BanHangCartTable.vue";
import BanHangCustomerSection from "./BanHangCustomerSection.vue";
import BanHangProductSection from "./BanHangProductSection.vue";
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
  sanPhamValidationMessage: {
    type: String,
    default: ""
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
  paymentValidationMessage: {
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
  },
  hasPrintedInvoice: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits([
  "reset-draft",
  "create-empty-invoice",
  "select-invoice",
  "update:customerKeyword",
  "focus-customer",
  "blur-customer",
  "select-customer",
  "select-guest",
  "clear-customer",
  "update:productKeyword",

  "refresh-products",
  "focus-product",
  "blur-product",
  "open-product",
  "scan-product",
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
  "print-invoice",
  "pay-now",
  "cancel-pending-invoice"
]);
</script>

<template>
  <div class="flex h-[calc(100vh-80px)] flex-col gap-2 overflow-hidden p-2">
    <BanHangPendingInvoicesSection
      :pending-invoices="pendingInvoices"
      :loading-pending-invoices="loadingPendingInvoices"
      :max-pending-invoices="maxPendingInvoices"
      :pending-invoice-limit-reached="pendingInvoiceLimitReached"
      :active-pending-invoice="activePendingInvoice"
      :dinh-dang-tien="dinhDangTien"
      @select-invoice="emit('select-invoice', $event)"
      @create-empty-invoice="emit('create-empty-invoice')"
    />

    <div class="grid min-h-0 flex-1 gap-2 xl:grid-cols-[1.6fr_0.8fr]">
      <div class="flex min-h-0 flex-col gap-2">
        <section class="shrink-0">
          <BanHangProductSection
            :active-pending-invoice="activePendingInvoice"
            :product-keyword="productKeyword"
            :loading-products="loadingProducts"
            :show-product-dropdown="showProductDropdown"
            :product-results="productResults"
            :product-search-label="productSearchLabel"
            :dinh-dang-tien="dinhDangTien"
            :so-luong-con-lai="soLuongConLai"
            @update:product-keyword="emit('update:productKeyword', $event)"
            @refresh="emit('update:productKeyword', '')"
            @focus-product="emit('focus-product')"
            @blur-product="emit('blur-product')"
            @open-product="emit('open-product', $event)"
            @scan-product="emit('scan-product', $event)"
          />
        </section>

        <section class="flex min-h-0 flex-1 flex-col overflow-hidden rounded-[24px] border border-white/70 bg-white/95 p-3 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
          <div class="mb-2 flex shrink-0 items-center justify-between">
            <h2 class="text-lg font-bold text-slate-900">Giỏ hàng</h2>
            <span class="rounded-full bg-red-50 px-3 py-1 text-xs font-semibold text-red-600">
              {{ tongSoLuong }} sản phẩm
            </span>
          </div>

          <div class="flex-1 overflow-y-auto pr-2">
            <BanHangCartTable
              :cart-items="cartItems"
              :dinh-dang-tien="dinhDangTien"
              :so-luong-con-lai="soLuongConLai"
              @increase-item="emit('increase-item', $event)"
              @decrease-item="emit('decrease-item', $event)"
            />
          </div>
        </section>
      </div>

      <div class="flex min-h-0 flex-col gap-2">
        <section class="shrink-0 rounded-[24px] border border-white/70 bg-white/95 p-3 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
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
        </section>

        <section class="flex min-h-0 flex-1 flex-col overflow-hidden rounded-[24px] border border-white/70 bg-white/95 p-3 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
          <BanHangPaymentSection
            :active-pending-invoice="activePendingInvoice"
            :invoice-loading="invoiceLoading"
            :tong-so-luong="tongSoLuong"
            :tong-tien-sau-giam-hien-thi="tongTienSauGiamHienThi"
            :tien-giam="tienGiam"
            :tong-tien="tongTien"
            :san-pham-validation-message="sanPhamValidationMessage"
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
            :is-guest-customer="isGuestCustomer"
            :shipping-info="shippingInfo"
            :ten-khach-hang-hien-thi="tenKhachHangHienThi"
            :so-dien-thoai-khach-hang-hien-thi="soDienThoaiKhachHangHienThi"
            :payment-method="paymentMethod"
            :amount-paid="amountPaid"
            :payment-validation-message="paymentValidationMessage"
            :tien-thua="tienThua"
            :payment-note="paymentNote"
            :can-create-pending-invoice="canCreatePendingInvoice"
            :saving-pending-invoice="savingPendingInvoice"
            :pending-invoice-limit-reached="pendingInvoiceLimitReached"
            :can-pay="canPay"
            :paying-invoice="payingInvoice"
            :canceling-pending-invoice="cancelingPendingInvoice"
            :dinh-dang-tien="dinhDangTien"
            :has-printed-invoice="hasPrintedInvoice"
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
            @print-invoice="emit('print-invoice')"
            @pay-now="emit('pay-now')"
            @cancel-pending-invoice="emit('cancel-pending-invoice')"
            @create-empty-invoice="emit('create-empty-invoice')"
          />
        </section>
      </div>
    </div>

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
  </div>
</template>
