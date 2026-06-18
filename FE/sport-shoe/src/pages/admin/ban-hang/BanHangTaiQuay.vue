<script setup>
import BanHangTaiQuayWorkspace from "../../../components/admin/ban-hang/BanHangTaiQuayWorkspace.vue";
import { useBanHangTaiQuay } from "../../../composable/useBanHangTaiQuay";

const {
  MAX_PENDING_INVOICES,
  pendingInvoices,
  loadingPendingInvoices,
  pendingInvoiceLimitReached,
  activePendingInvoice,
  customerKeyword,
  loadingCustomers,
  showCustomerDropdown,
  customerResults,
  tenKhachHangHienThi,
  soDienThoaiKhachHangHienThi,
  selectedCustomer,
  isGuestCustomer,
  productKeyword,
  loadingProducts,
  showProductDropdown,
  productResults,
  paginatedProducts,
  currentPage,
  pageSize,
  totalItems,
  totalPages,
  selectedBrandFilter,
  selectedCategoryFilter,
  availableBrands,
  availableCategories,
  productSearchLabel,
  cartItems,
  selectedProductDetail,
  chiTietDangChon,
  hinhAnhDangChon,
  soLuongTonSauKhiChon,
  colorOptions,
  sizeOptions,
  selectedColor,
  selectedSize,
  selectedQuantity,
  soLuongTonKhaDungChiTiet,
  invoiceLoading,
  tongSoLuong,
  tongTienSauGiamHienThi,
  tienGiam,
  tongTien,
  sanPhamValidationMessage,
  couponCode,
  coTheApDungPhieu,
  applyingCoupon,
  showCouponDropdown,
  coTheTimPhieu,
  loadingCoupons,
  couponResults,
  appliedCoupon,
  maPhieuChuaApDung,
  khachCanTra,
  shippingInfo,
  paymentMethod,
  amountPaid,
  paymentValidationMessage,
  tienThua,
  paymentNote,
  canCreatePendingInvoice,
  savingPendingInvoice,
  canPay,
  payingInvoice,
  cancelingPendingInvoice,
  dinhDangTien,
  soLuongConLai,
  fetchProducts,
  resetDraft,
  chonHoaDonCho,
  moDanhSachKhachHang,
  dongDanhSachKhachHang,
  chonKhachHang,
  chonKhachVangLai,
  boChonKhachHang,
  moDanhSachSanPham,
  dongDanhSachSanPham,
  moChiTietSanPham,
  handleProductQrScan,
  tangSoLuong,
  giamSoLuong,
  dongChiTietSanPham,
  chonMauSac,
  chonKichCo,
  giamSoLuongChiTiet,
  tangSoLuongChiTiet,
  themBienTheDangChon,
  handleCouponFocus,
  handleCouponBlur,
  handleApplyCoupon,
  chonPhieuGiamGia,
  handleRemoveCoupon,
  updateShippingInfo,
  handleCalculateShippingFee,
  handleAmountPaidInput,
  handleCreatePendingInvoice,
  handleCreateEmptyInvoice,
  handlePayNow,
  handleCancelPendingInvoice,
  handlePrintInvoice,
  hasPrintedInvoice
} = useBanHangTaiQuay();

import { onBeforeRouteLeave } from "vue-router";

onBeforeRouteLeave(async (to, from, next) => {
  if (cartItems.value && cartItems.value.length > 0 && !activePendingInvoice.value) {
    try {
      await handleCreatePendingInvoice();
    } catch (error) {
      console.error("Tự động tạo hóa đơn chờ khi chuyển trang thất bại:", error);
    }
  }
  next();
});

const setCustomerKeyword = (val) => { customerKeyword.value = val; };
const setProductKeyword = (val) => { productKeyword.value = val; };
const setCurrentPage = (val) => { currentPage.value = val; };
const setPageSize = (val) => { pageSize.value = val; };
const setSelectedBrandFilter = (val) => { selectedBrandFilter.value = val; };
const setSelectedCategoryFilter = (val) => { selectedCategoryFilter.value = val; };
const setCouponCode = (val) => { couponCode.value = val; };
const setPaymentMethod = (val) => { paymentMethod.value = val; };
const setPaymentNote = (val) => { paymentNote.value = val; };
const setAmountPaid = (val) => { amountPaid.value = val; };

</script>

<template>
  <BanHangTaiQuayWorkspace
    :pending-invoices="pendingInvoices"
    :loading-pending-invoices="loadingPendingInvoices"
    :max-pending-invoices="MAX_PENDING_INVOICES"
    :pending-invoice-limit-reached="pendingInvoiceLimitReached"
    :active-pending-invoice="activePendingInvoice"
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
    :paginated-products="paginatedProducts"
    :current-page="currentPage"
    :page-size="pageSize"
    :total-items="totalItems"
    :total-pages="totalPages"
    :selected-brand-filter="selectedBrandFilter"
    :selected-category-filter="selectedCategoryFilter"
    :available-brands="availableBrands"
    :available-categories="availableCategories"
    :product-search-label="productSearchLabel"
    :cart-items="cartItems"
    :selected-product-detail="selectedProductDetail"
    :chi-tiet-dang-chon="chiTietDangChon"
    :hinh-anh-dang-chon="hinhAnhDangChon"
    :so-luong-ton-sau-khi-chon="soLuongTonSauKhiChon"
    :color-options="colorOptions"
    :size-options="sizeOptions"
    :selected-color="selectedColor"
    :selected-size="selectedSize"
    :selected-quantity="selectedQuantity"
    :so-luong-ton-kha-dung-chi-tiet="soLuongTonKhaDungChiTiet"
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
    :shipping-info="shippingInfo"
    :payment-method="paymentMethod"
    :amount-paid="amountPaid"
    :payment-validation-message="paymentValidationMessage"
    :tien-thua="tienThua"
    :payment-note="paymentNote"
    :can-create-pending-invoice="canCreatePendingInvoice"
    :saving-pending-invoice="savingPendingInvoice"
    :can-pay="canPay"
    :paying-invoice="payingInvoice"
    :canceling-pending-invoice="cancelingPendingInvoice"
    :dinh-dang-tien="dinhDangTien"
    :so-luong-con-lai="soLuongConLai"
    :has-printed-invoice="hasPrintedInvoice"
    @reset-draft="resetDraft"
    @create-empty-invoice="handleCreateEmptyInvoice"
    @select-invoice="chonHoaDonCho"
    @update:customer-keyword="setCustomerKeyword"
    @focus-customer="moDanhSachKhachHang"
    @blur-customer="dongDanhSachKhachHang"
    @select-customer="chonKhachHang"
    @select-guest="chonKhachVangLai"
    @clear-customer="boChonKhachHang"
    @update:product-keyword="setProductKeyword"
    @update:current-page="setCurrentPage"
    @update:page-size="setPageSize"
    @update:selected-brand-filter="setSelectedBrandFilter"
    @update:selected-category-filter="setSelectedCategoryFilter"
    @focus-product="moDanhSachSanPham"
    @blur-product="dongDanhSachSanPham"
    @open-product="moChiTietSanPham"
    @scan-product="handleProductQrScan"
    @increase-item="tangSoLuong"
    @decrease-item="giamSoLuong"
    @close-product-detail="dongChiTietSanPham"
    @select-color="chonMauSac"
    @select-size="chonKichCo"
    @decrease-quantity="giamSoLuongChiTiet"
    @increase-quantity="tangSoLuongChiTiet"
    @add-selected-variant="themBienTheDangChon"
    @update:coupon-code="setCouponCode"
    @focus-coupon="handleCouponFocus"
    @blur-coupon="handleCouponBlur"
    @apply-coupon="handleApplyCoupon"
    @select-coupon="chonPhieuGiamGia"
    @remove-coupon="handleRemoveCoupon"
    @update-shipping="updateShippingInfo"
    @calculate-shipping="handleCalculateShippingFee"
    @update:payment-method="setPaymentMethod"
    @amount-input="setAmountPaid"
    @update:payment-note="setPaymentNote"
    @print-invoice="handlePrintInvoice"
    @pay-now="handlePayNow"
    @cancel-pending-invoice="handleCancelPendingInvoice"
  />
</template>
