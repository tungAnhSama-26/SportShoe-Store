<script setup>
import { computed, onMounted, ref, watch } from "vue";
import {
  apDungPhieuGiamGiaTaiQuay,
  huyHoaDonCho,
  layChiTietHoaDonCho,
  layDanhSachHoaDonCho,
  thanhToanTaiQuay,
  taoHoaDonCho,
  timKhachHangTheoSoDienThoai,
  timPhieuGiamGiaTaiQuay,
  timSanPhamTaiQuay
} from "../../../services/ban-hang-tai-quay";
import BanHangTaiQuayWorkspace from "../../../components/admin/ban-hang/BanHangTaiQuayWorkspace.vue";
const GUEST_LABEL = "Kh\xE1ch v\xE3ng lai";
const HIDDEN_INFO_LABEL = "\u1EA8n th\xF4ng tin";
const NO_CUSTOMER_LABEL = "Chưa chọn khách hàng";
const NO_CUSTOMER_PHONE_LABEL = "Chọn khách hoặc Khách vãng lai";
const MAX_PENDING_INVOICES = 5;
const MAX_PAYMENT_DIGITS = 15;
const customerKeyword = ref("");
const productKeyword = ref("");
const couponCode = ref("");
const customerResults = ref([]);
const productResults = ref([]);
const selectedProductDetail = ref(null);
const selectedColor = ref("");
const selectedSize = ref("");
const selectedQuantity = ref(1);
const selectedCustomer = ref(null);
const cartItems = ref([]);
const pendingInvoices = ref([]);
const activePendingInvoice = ref(null);
const appliedCoupon = ref(null);
const loadingCustomers = ref(false);
const loadingProducts = ref(false);
const loadingPendingInvoices = ref(false);
const savingPendingInvoice = ref(false);
const cancelingPendingInvoice = ref(false);
const payingInvoice = ref(false);
const invoiceLoading = ref(false);
const applyingCoupon = ref(false);
const showCustomerDropdown = ref(false);
const showProductDropdown = ref(false);
const couponResults = ref([]);
const loadingCoupons = ref(false);
const showCouponDropdown = ref(false);
const pageError = ref("");
const successMessage = ref("");
const paymentMethod = ref(1);
const amountPaid = ref("");
const paymentNote = ref("");
let customerTimer;
let productTimer;
let couponTimer;
let couponDropdownTimer;
const tongSoLuong = computed(
  () => cartItems.value.reduce((total, item) => total + item.soLuong, 0)
);
const tongTien = computed(
  () => cartItems.value.reduce((total, item) => total + item.soLuong * item.giaBan, 0)
);
const tienGiam = computed(() => appliedCoupon.value?.soTienGiam ?? 0);
const tongTienSauGiamHienThi = computed(() => Math.max(tongTien.value - tienGiam.value, 0));
const productSearchLabel = computed(
  () => productKeyword.value.trim() ? "K\u1EBFt qu\u1EA3 t\xECm ki\u1EBFm s\u1EA3n ph\u1EA9m" : "S\u1EA3n ph\u1EA9m t\u1EA1i qu\u1EA7y"
);
const isGuestCustomer = computed(
  () => customerKeyword.value.trim().toLowerCase() === GUEST_LABEL.toLowerCase()
);
const tenKhachHangHienThi = computed(() => {
  if (selectedCustomer.value) {
    return selectedCustomer.value.hoTen;
  }
  if (isGuestCustomer.value) {
    return GUEST_LABEL;
  }
  return activePendingInvoice.value?.tenKhachHang || NO_CUSTOMER_LABEL;
});
const soDienThoaiKhachHangHienThi = computed(() => {
  if (selectedCustomer.value) {
    return selectedCustomer.value.sdt;
  }
  if (isGuestCustomer.value) {
    return HIDDEN_INFO_LABEL;
  }
  return activePendingInvoice.value?.soDienThoai || NO_CUSTOMER_PHONE_LABEL;
});
const maPhieuChuaApDung = computed(() => Boolean(couponCode.value.trim()) && !appliedCoupon.value);
const daChonKhach = computed(
  () => Boolean(selectedCustomer.value) || Boolean(activePendingInvoice.value) || isGuestCustomer.value
);
const khachCanTra = computed(() => appliedCoupon.value?.tongTienSauGiam ?? tongTien.value);
const coTheTimPhieu = computed(() => cartItems.value.length > 0 && tongTien.value > 0);
const phieuGiamGiaHopLeDangNhap = computed(() => {
  const keyword = couponCode.value.trim().toLowerCase();
  if (!keyword) {
    return null;
  }

  const trungKhopChinhXac = couponResults.value.find((coupon) => {
    const ma = coupon.ma?.trim().toLowerCase() ?? "";
    const ten = coupon.ten?.trim().toLowerCase() ?? "";
    return ma === keyword || ten === keyword;
  });

  if (trungKhopChinhXac) {
    return trungKhopChinhXac;
  }

  if (couponResults.value.length === 1) {
    return couponResults.value[0];
  }

  return null;
});
const coTheApDungPhieu = computed(
  () => Boolean(phieuGiamGiaHopLeDangNhap.value) && cartItems.value.length > 0 && !applyingCoupon.value && (!appliedCoupon.value || appliedCoupon.value.ma.toLowerCase() !== phieuGiamGiaHopLeDangNhap.value.ma.toLowerCase())
);
const pendingInvoiceLimitReached = computed(() => pendingInvoices.value.length >= MAX_PENDING_INVOICES);
const tienKhachThanhToan = computed(() => {
  const parsed = Number(layChuSoTien(amountPaid.value));
  return Number.isFinite(parsed) ? parsed : 0;
});
const tienThua = computed(() => {
  if (paymentMethod.value !== 1) {
    return 0;
  }
  return Math.max(tienKhachThanhToan.value - khachCanTra.value, 0);
});
const canCreatePendingInvoice = computed(
  () => cartItems.value.length > 0 && !savingPendingInvoice.value && !maPhieuChuaApDung.value && !pendingInvoiceLimitReached.value
);
const canPay = computed(() => {
  if (!cartItems.value.length || payingInvoice.value || maPhieuChuaApDung.value) {
    return false;
  }
  if (paymentMethod.value === 1) {
    return tienKhachThanhToan.value >= khachCanTra.value;
  }
  return true;
});
function dinhDangTien(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
  }).format(value || 0);
}
function dinhDangSo(value) {
  return new Intl.NumberFormat("vi-VN", {
    maximumFractionDigits: 0
  }).format(value || 0);
}
function layChuSoTien(value) {
  return value.replace(/[^\d]/g, "").slice(0, MAX_PAYMENT_DIGITS);
}
function dinhDangTienNhap(value) {
  const digits = layChuSoTien(value);
  return digits ? dinhDangSo(Number(digits)) : "";
}
function clearFeedback() {
  pageError.value = "";
  successMessage.value = "";
}
function taoDanhSachSanPhamThanhToan() {
  return cartItems.value.map((item) => ({
    chiTietId: item.chiTietId,
    soLuong: item.soLuong
  }));
}
function layKhachHangIdHienTai() {
  if (selectedCustomer.value) {
    return selectedCustomer.value.id;
  }
  if (isGuestCustomer.value) {
    return null;
  }
  return activePendingInvoice.value?.khachHangId ?? null;
}
function capNhatTienKhachThanhToan(force = false) {
  if (!cartItems.value.length) {
    amountPaid.value = "";
    return;
  }
  if (paymentMethod.value !== 1 || force) {
    amountPaid.value = dinhDangSo(khachCanTra.value);
    return;
  }
  if (!amountPaid.value.trim()) {
    amountPaid.value = dinhDangSo(khachCanTra.value);
  }
}
function danhDauCanApDungLaiPhieu() {
  if (!couponCode.value.trim()) {
    appliedCoupon.value = null;
    return;
  }
  appliedCoupon.value = null;
}
function soLuongDaChon(chiTietId) {
  return cartItems.value.find((item) => item.chiTietId === chiTietId)?.soLuong ?? 0;
}
function soLuongConLai(chiTietId, soLuongTon) {
  return Math.max(soLuongTon - soLuongDaChon(chiTietId), 0);
}
function laySoLuongTonHienTai(chiTietId, fallback) {
  return productResults.value.find((product) => product.chiTietId === chiTietId)?.soLuongTon ?? fallback;
}
const relatedVariants = computed(() => {
  if (!selectedProductDetail.value) {
    return [];
  }
  return productResults.value.filter(
    (product) => product.maSanPham === selectedProductDetail.value?.maSanPham && product.tenSanPham === selectedProductDetail.value?.tenSanPham
  );
});
const colorOptions = computed(() => {
  const grouped = /* @__PURE__ */ new Map();
  for (const variant of relatedVariants.value) {
    const key = variant.mauSac || variant.maBienThe;
    if (!grouped.has(key)) {
      grouped.set(key, variant);
    }
  }
  return Array.from(grouped.values());
});
const sizeOptions = computed(
  () => relatedVariants.value.filter((variant) => {
    if (!selectedColor.value) {
      return true;
    }
    return (variant.mauSac || variant.maBienThe) === selectedColor.value;
  })
);
const selectedVariant = computed(() => {
  if (!selectedProductDetail.value) {
    return null;
  }
  return relatedVariants.value.find(
    (variant) => (selectedColor.value ? (variant.mauSac || variant.maBienThe) === selectedColor.value : true) && (selectedSize.value ? (variant.kichCo || "") === selectedSize.value : true)
  ) || selectedProductDetail.value;
});
const chiTietDangChon = computed(() => selectedVariant.value || selectedProductDetail.value);
const soLuongTonKhaDungChiTiet = computed(() => {
  if (!chiTietDangChon.value) {
    return 0;
  }
  return soLuongConLai(chiTietDangChon.value.chiTietId, chiTietDangChon.value.soLuongTon);
});
const soLuongTonSauKhiChon = computed(
  () => Math.max(soLuongTonKhaDungChiTiet.value - selectedQuantity.value, 0)
);
function resetDraft() {
  selectedCustomer.value = null;
  customerKeyword.value = "";
  productKeyword.value = "";
  couponCode.value = "";
  customerResults.value = [];
  productResults.value = [];
  couponResults.value = [];
  selectedProductDetail.value = null;
  selectedColor.value = "";
  selectedSize.value = "";
  selectedQuantity.value = 1;
  cartItems.value = [];
  activePendingInvoice.value = null;
  appliedCoupon.value = null;
  paymentMethod.value = 1;
  amountPaid.value = "";
  paymentNote.value = "";
  showCustomerDropdown.value = false;
  showProductDropdown.value = false;
  showCouponDropdown.value = false;
  clearFeedback();
  void fetchProducts("");
}
async function fetchPendingInvoices() {
  loadingPendingInvoices.value = true;
  try {
    pendingInvoices.value = await layDanhSachHoaDonCho();
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\xF4ng th\u1EC3 t\u1EA3i danh s\xE1ch h\xF3a \u0111\u01A1n ch\u1EDD";
  } finally {
    loadingPendingInvoices.value = false;
  }
}
async function fetchCustomers(keyword) {
  if (!keyword.trim() || keyword.trim().toLowerCase() === GUEST_LABEL.toLowerCase()) {
    customerResults.value = [];
    return;
  }
  loadingCustomers.value = true;
  try {
    customerResults.value = await timKhachHangTheoSoDienThoai(keyword);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\xF4ng th\u1EC3 t\xECm kh\xE1ch h\xE0ng";
  } finally {
    loadingCustomers.value = false;
  }
}
async function fetchProducts(keyword) {
  loadingProducts.value = true;
  try {
    productResults.value = await timSanPhamTaiQuay(keyword);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\xF4ng th\u1EC3 t\xECm s\u1EA3n ph\u1EA9m";
  } finally {
    loadingProducts.value = false;
  }
}
async function fetchCoupons(keyword) {
  if (!coTheTimPhieu.value) {
    couponResults.value = [];
    return;
  }
  loadingCoupons.value = true;
  try {
    couponResults.value = await timPhieuGiamGiaTaiQuay({
      keyword,
      hoaDonId: activePendingInvoice.value?.id ?? null,
      khachHangId: layKhachHangIdHienTai(),
      tongTienHang: tongTien.value
    });
  } catch (error) {
    couponResults.value = [];
    pageError.value = error instanceof Error ? error.message : "Kh\xF4ng th\u1EC3 t\xECm phi\u1EBFu gi\u1EA3m gi\xE1";
  } finally {
    loadingCoupons.value = false;
  }
}
watch(customerKeyword, (value) => {
  if (customerTimer) {
    window.clearTimeout(customerTimer);
  }
  const keyword = value.trim().toLowerCase();
  if (selectedCustomer.value) {
    const tenKhachDangChon = selectedCustomer.value.hoTen?.trim().toLowerCase() ?? "";
    const soDienThoaiDangChon = selectedCustomer.value.sdt?.trim().toLowerCase() ?? "";
    if (keyword !== tenKhachDangChon && keyword !== soDienThoaiDangChon) {
      selectedCustomer.value = null;
      danhDauCanApDungLaiPhieu();
    }
  }
  showCustomerDropdown.value = value.trim().length > 0 && keyword !== GUEST_LABEL.toLowerCase();
  customerTimer = window.setTimeout(() => {
    void fetchCustomers(value);
  }, 250);
});
watch(productKeyword, (value) => {
  if (productTimer) {
    window.clearTimeout(productTimer);
  }
  if (!daChonKhach.value) {
    showProductDropdown.value = false;
    return;
  }
  showProductDropdown.value = value.trim().length > 0;
  productTimer = window.setTimeout(() => {
    void fetchProducts(value);
  }, 250);
});
watch(couponCode, (value) => {
  if (couponTimer) {
    window.clearTimeout(couponTimer);
  }
  const trimmed = value.trim();
  if (!trimmed) {
    appliedCoupon.value = null;
    if (showCouponDropdown.value) {
      couponTimer = window.setTimeout(() => {
        void fetchCoupons("");
      }, 250);
    }
    return;
  }
  if (appliedCoupon.value && appliedCoupon.value.ma.toLowerCase() !== trimmed.toLowerCase()) {
    appliedCoupon.value = null;
  }
  couponTimer = window.setTimeout(() => {
    void fetchCoupons(value);
  }, 250);
});
watch([paymentMethod, khachCanTra], () => {
  capNhatTienKhachThanhToan();
});
watch([coTheTimPhieu, tongTien, selectedCustomer, activePendingInvoice], ([coTheTim]) => {
  if (!coTheTim) {
    couponResults.value = [];
    showCouponDropdown.value = false;
    return;
  }
  if (!couponCode.value.trim() && !showCouponDropdown.value) {
    return;
  }
  void fetchCoupons(couponCode.value);
});
watch(pageError, (message) => {
  if (!message) {
    return;
  }
  window.alert(message);
  pageError.value = "";
});
watch(successMessage, (message) => {
  if (!message) {
    return;
  }
  window.alert(message);
  successMessage.value = "";
});
function handleCouponFocus() {
  if (couponDropdownTimer) {
    window.clearTimeout(couponDropdownTimer);
  }
  showCouponDropdown.value = true;
  void fetchCoupons(couponCode.value);
}
function handleCouponBlur() {
  if (couponDropdownTimer) {
    window.clearTimeout(couponDropdownTimer);
  }
  couponDropdownTimer = window.setTimeout(() => {
    showCouponDropdown.value = false;
  }, 150);
}
function chonPhieuGiamGia(coupon) {
  if (couponDropdownTimer) {
    window.clearTimeout(couponDropdownTimer);
  }
  couponCode.value = coupon.ma;
  showCouponDropdown.value = false;
  clearFeedback();
}
async function timPhieuPhuHopDeApDung() {
  const keyword = couponCode.value.trim();
  if (!keyword || !coTheTimPhieu.value) {
    return null;
  }

  const keywordDaChuanHoa = keyword.toLowerCase();
  let ketQua = couponResults.value;

  if (!ketQua.length) {
    try {
      ketQua = await timPhieuGiamGiaTaiQuay({
        keyword,
        hoaDonId: activePendingInvoice.value?.id ?? null,
        khachHangId: layKhachHangIdHienTai(),
        tongTienHang: tongTien.value
      });
      couponResults.value = ketQua;
    } catch (error) {
      pageError.value = error instanceof Error ? error.message : "Khong the tim phieu giam gia";
      return null;
    }
  }

  const trungKhopChinhXac = ketQua.find((coupon) => {
    const ma = coupon.ma?.trim().toLowerCase() ?? "";
    const ten = coupon.ten?.trim().toLowerCase() ?? "";
    return ma === keywordDaChuanHoa || ten === keywordDaChuanHoa;
  });

  if (trungKhopChinhXac) {
    return trungKhopChinhXac;
  }

  if (ketQua.length === 1) {
    return ketQua[0];
  }

  return null;
}
function chonKhachHang(customer) {
  selectedCustomer.value = customer;
  customerKeyword.value = customer.hoTen;
  customerResults.value = [];
  showCustomerDropdown.value = false;
  danhDauCanApDungLaiPhieu();
  clearFeedback();
}
function boChonKhachHang() {
  selectedCustomer.value = null;
  customerKeyword.value = "";
  customerResults.value = [];
  showCustomerDropdown.value = false;
  danhDauCanApDungLaiPhieu();
  clearFeedback();
}
function chonKhachVangLai() {
  selectedCustomer.value = null;
  customerKeyword.value = GUEST_LABEL;
  customerResults.value = [];
  showCustomerDropdown.value = false;
  danhDauCanApDungLaiPhieu();
  clearFeedback();
}
function moChiTietSanPham(product) {
  if (!daChonKhach.value) {
    pageError.value = "Vui l\xF2ng ch\u1ECDn kh\xE1ch h\xE0ng ho\u1EB7c Kh\xE1ch v\xE3ng lai tr\u01B0\u1EDBc khi th\xEAm s\u1EA3n ph\u1EA9m";
    return;
  }
  selectedProductDetail.value = product;
  selectedColor.value = product.mauSac || product.maBienThe;
  selectedSize.value = product.kichCo || "";
  selectedQuantity.value = 1;
}
function dongChiTietSanPham() {
  selectedProductDetail.value = null;
  selectedColor.value = "";
  selectedSize.value = "";
  selectedQuantity.value = 1;
}
function themSanPham(product, quantity = 1) {
  if (!daChonKhach.value) {
    pageError.value = "Vui l\xF2ng ch\u1ECDn kh\xE1ch h\xE0ng ho\u1EB7c Kh\xE1ch v\xE3ng lai tr\u01B0\u1EDBc khi th\xEAm s\u1EA3n ph\u1EA9m";
    return;
  }
  const soLuongCoTheThem = soLuongConLai(product.chiTietId, product.soLuongTon);
  const existing = cartItems.value.find((item) => item.chiTietId === product.chiTietId);
  if (existing) {
    if (quantity > soLuongCoTheThem) {
      pageError.value = `S\u1EA3n ph\u1EA9m ${existing.tenSanPham} \u0111\xE3 \u0111\u1EA1t gi\u1EDBi h\u1EA1n t\u1ED3n kho`;
      return;
    }
    existing.soLuong += quantity;
  } else {
    if (quantity > soLuongCoTheThem) {
      pageError.value = `S\u1EA3n ph\u1EA9m ${product.tenSanPham} \u0111\xE3 v\u01B0\u1EE3t gi\u1EDBi h\u1EA1n t\u1ED3n kho`;
      return;
    }
    cartItems.value = [
      ...cartItems.value,
      {
        chiTietId: product.chiTietId,
        maSanPham: product.maSanPham,
        tenSanPham: product.tenSanPham,
        soLuong: quantity,
        giaBan: product.giaBan,
        soLuongTon: product.soLuongTon
      }
    ];
  }
  productKeyword.value = "";
  productResults.value = [];
  selectedProductDetail.value = null;
  selectedColor.value = "";
  selectedSize.value = "";
  selectedQuantity.value = 1;
  showProductDropdown.value = false;
  danhDauCanApDungLaiPhieu();
  capNhatTienKhachThanhToan();
  clearFeedback();
}
function chonMauSac(value) {
  selectedColor.value = value;
  selectedSize.value = sizeOptions.value[0]?.kichCo || "";
  selectedQuantity.value = 1;
}
function chonKichCo(value) {
  selectedSize.value = value;
  selectedQuantity.value = 1;
}
function giamSoLuongChiTiet() {
  selectedQuantity.value = Math.max(selectedQuantity.value - 1, 1);
}
function tangSoLuongChiTiet() {
  if (!selectedVariant.value) {
    return;
  }
  const soLuongToiDa = soLuongConLai(selectedVariant.value.chiTietId, selectedVariant.value.soLuongTon);
  selectedQuantity.value = Math.min(selectedQuantity.value + 1, Math.max(soLuongToiDa, 1));
}
function themBienTheDangChon() {
  if (!selectedVariant.value) {
    pageError.value = "Vui l\xF2ng ch\u1ECDn m\xE0u s\u1EAFc v\xE0 k\xEDch c\u1EE1 ph\xF9 h\u1EE3p";
    return;
  }
  themSanPham(selectedVariant.value, selectedQuantity.value);
}
function tangSoLuong(chiTietId) {
  let reachedLimit = "";
  cartItems.value = cartItems.value.map((item) => {
    if (item.chiTietId !== chiTietId) {
      return item;
    }
    if (item.soLuong >= item.soLuongTon) {
      reachedLimit = item.tenSanPham;
      return item;
    }
    return { ...item, soLuong: item.soLuong + 1 };
  });
  if (reachedLimit) {
    pageError.value = `S\u1EA3n ph\u1EA9m ${reachedLimit} \u0111\xE3 v\u01B0\u1EE3t gi\u1EDBi h\u1EA1n t\u1ED3n kho`;
    return;
  }
  danhDauCanApDungLaiPhieu();
  capNhatTienKhachThanhToan();
}
function giamSoLuong(chiTietId) {
  cartItems.value = cartItems.value.map(
    (item) => item.chiTietId === chiTietId ? { ...item, soLuong: item.soLuong - 1 } : item
  ).filter((item) => item.soLuong > 0);
  danhDauCanApDungLaiPhieu();
  capNhatTienKhachThanhToan();
}
function mapInvoiceToDraft(invoice) {
  customerKeyword.value = invoice.tenKhachHang || invoice.soDienThoai || GUEST_LABEL;
  selectedCustomer.value = invoice.khachHangId ? {
    id: invoice.khachHangId,
    hoTen: invoice.tenKhachHang,
    sdt: invoice.soDienThoai,
    email: null
  } : null;
  cartItems.value = invoice.items.map((item) => ({
    chiTietId: item.chiTietId,
    maSanPham: item.maSanPham,
    tenSanPham: item.tenSanPham,
    soLuong: item.soLuong,
    giaBan: item.giaBan,
    soLuongTon: laySoLuongTonHienTai(item.chiTietId, item.soLuong)
  }));
  couponCode.value = invoice.phieuGiamGia?.ma ?? "";
  appliedCoupon.value = invoice.phieuGiamGia ? {
    id: 0,
    ma: invoice.phieuGiamGia.ma,
    ten: invoice.phieuGiamGia.ten,
    loai: 0,
    giaTri: 0,
    giaTriToiThieu: null,
    giamToiDa: null,
    soTienGiam: invoice.tienGiam || invoice.phieuGiamGia.soTienGiam,
    tongTienHang: invoice.tongTienHang || 0,
    tongTienSauGiam: invoice.tongTien || 0
  } : null;
  couponResults.value = [];
  showCouponDropdown.value = false;
  capNhatTienKhachThanhToan(true);
}
async function chonHoaDonCho(invoice) {
  invoiceLoading.value = true;
  pageError.value = "";
  try {
    await fetchProducts("");
    const detail = await layChiTietHoaDonCho(invoice.id);
    activePendingInvoice.value = invoice;
    mapInvoiceToDraft(detail);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\xF4ng th\u1EC3 t\u1EA3i h\xF3a \u0111\u01A1n ch\u1EDD";
  } finally {
    invoiceLoading.value = false;
  }
}
async function handleApplyCoupon() {
  if (!coTheApDungPhieu.value) {
    if (couponCode.value.trim()) {
      showCouponDropdown.value = true;
      pageError.value = "Phiếu giảm giá này chưa đủ điều kiện áp dụng cho hóa đơn hiện tại";
    }
    return;
  }

  const couponDaTimThay = phieuGiamGiaHopLeDangNhap.value ?? await timPhieuPhuHopDeApDung();
  const maPhieuDeApDung = couponDaTimThay?.ma ?? couponCode.value.trim();

  if (couponDaTimThay) {
    couponCode.value = couponDaTimThay.ma;
  }

  if (!couponDaTimThay && couponResults.value.length > 1) {
    showCouponDropdown.value = true;
    pageError.value = "Vui lòng chọn đúng phiếu giảm giá trong danh sách gợi ý";
    return;
  }

  applyingCoupon.value = true;
  pageError.value = "";
  successMessage.value = "";
  try {
    const coupon = await apDungPhieuGiamGiaTaiQuay({
      hoaDonId: activePendingInvoice.value?.id ?? null,
      khachHangId: layKhachHangIdHienTai(),
      maPhieuGiamGia: maPhieuDeApDung,
      items: taoDanhSachSanPhamThanhToan()
    });
    appliedCoupon.value = coupon;
    couponCode.value = coupon.ma;
    couponResults.value = [];
    showCouponDropdown.value = false;
    capNhatTienKhachThanhToan();
    successMessage.value = `\u0110\xE3 \xE1p d\u1EE5ng m\xE3 ${coupon.ma}`;
  } catch (error) {
    appliedCoupon.value = null;
    pageError.value = error instanceof Error ? error.message : "Kh\xF4ng th\u1EC3 \xE1p d\u1EE5ng phi\u1EBFu gi\u1EA3m gi\xE1";
  } finally {
    applyingCoupon.value = false;
  }
}
function handleRemoveCoupon() {
  couponCode.value = "";
  appliedCoupon.value = null;
  couponResults.value = [];
  capNhatTienKhachThanhToan();
  clearFeedback();
}
async function handleCreatePendingInvoice() {
  if (pendingInvoiceLimitReached.value) {
    pageError.value = `Chỉ được tạo tối đa ${MAX_PENDING_INVOICES} hóa đơn chờ.`;
    return;
  }
  if (!canCreatePendingInvoice.value) {
    return;
  }
  savingPendingInvoice.value = true;
  pageError.value = "";
  successMessage.value = "";
  try {
    const createdInvoice = await taoHoaDonCho({
      khachHangId: layKhachHangIdHienTai(),
      tenKhachHang: tenKhachHangHienThi.value,
      soDienThoai: selectedCustomer.value?.sdt || activePendingInvoice.value?.soDienThoai || "",
      maPhieuGiamGia: appliedCoupon.value?.ma ?? null,
      items: taoDanhSachSanPhamThanhToan()
    });
    successMessage.value = `\u0110\xE3 t\u1EA1o h\xF3a \u0111\u01A1n ch\u1EDD ${createdInvoice.ma}`;
    await fetchPendingInvoices();
    const matchedInvoice = pendingInvoices.value.find((invoice) => invoice.id === createdInvoice.id) ?? null;
    activePendingInvoice.value = matchedInvoice;
    mapInvoiceToDraft(createdInvoice);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\xF4ng th\u1EC3 t\u1EA1o h\xF3a \u0111\u01A1n ch\u1EDD";
  } finally {
    savingPendingInvoice.value = false;
  }
}
function formatCurrencyInput() {
  if (paymentMethod.value !== 1) {
    amountPaid.value = dinhDangSo(khachCanTra.value);
    return;
  }
  amountPaid.value = dinhDangTienNhap(amountPaid.value);
}
function handleAmountPaidInput(value) {
  amountPaid.value = value;
  formatCurrencyInput();
}
async function handlePayNow() {
  if (!canPay.value) {
    return;
  }
  payingInvoice.value = true;
  pageError.value = "";
  successMessage.value = "";
  try {
    const response = await thanhToanTaiQuay({
      hoaDonId: activePendingInvoice.value?.id ?? null,
      khachHangId: layKhachHangIdHienTai(),
      tenKhachHang: tenKhachHangHienThi.value,
      soDienThoai: selectedCustomer.value?.sdt || activePendingInvoice.value?.soDienThoai || "",
      maPhieuGiamGia: appliedCoupon.value?.ma ?? null,
      hinhThucThanhToan: paymentMethod.value,
      tienKhachDua: paymentMethod.value === 1 ? tienKhachThanhToan.value : khachCanTra.value,
      ghiChu: paymentNote.value,
      items: taoDanhSachSanPhamThanhToan()
    });
    successMessage.value = `\u0110\xE3 thanh to\xE1n ${response.maHoaDon}`;
    await fetchPendingInvoices();
    resetDraft();
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\xF4ng th\u1EC3 thanh to\xE1n tr\u1EF1c ti\u1EBFp";
  } finally {
    payingInvoice.value = false;
  }
}
async function handleCancelPendingInvoice() {
  if (!activePendingInvoice.value || cancelingPendingInvoice.value) {
    return;
  }
  cancelingPendingInvoice.value = true;
  pageError.value = "";
  successMessage.value = "";
  try {
    await huyHoaDonCho(activePendingInvoice.value.id);
    successMessage.value = `\u0110\xE3 h\u1EE7y h\xF3a \u0111\u01A1n ch\u1EDD ${activePendingInvoice.value.ma}`;
    await fetchPendingInvoices();
    resetDraft();
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\xF4ng th\u1EC3 h\u1EE7y h\xF3a \u0111\u01A1n ch\u1EDD";
  } finally {
    cancelingPendingInvoice.value = false;
  }
}
async function moDanhSachKhachHang() {
  const keyword = customerKeyword.value.trim();
  if (keyword && keyword.toLowerCase() !== GUEST_LABEL.toLowerCase()) {
    showCustomerDropdown.value = true;
    await fetchCustomers(customerKeyword.value);
    return;
  }
  showCustomerDropdown.value = false;
}
async function moDanhSachSanPham() {
  if (!daChonKhach.value) {
    showProductDropdown.value = false;
    pageError.value = "Vui long chon khach hang hoac Khach vang lai truoc khi them san pham";
    return;
  }
  showProductDropdown.value = true;
  await fetchProducts(productKeyword.value);
}
function dongDanhSachKhachHang() {
  window.setTimeout(() => {
    showCustomerDropdown.value = false;
  }, 150);
}
function dongDanhSachSanPham() {
  window.setTimeout(() => {
    showProductDropdown.value = false;
  }, 150);
}
onMounted(async () => {
  await fetchProducts("");
  await fetchPendingInvoices();
});
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
    :product-search-label="productSearchLabel"
    :cart-items="cartItems"
    :selected-product-detail="selectedProductDetail"
    :chi-tiet-dang-chon="chiTietDangChon"
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
    :payment-method="paymentMethod"
    :amount-paid="amountPaid"
    :tien-thua="tienThua"
    :payment-note="paymentNote"
    :can-create-pending-invoice="canCreatePendingInvoice"
    :saving-pending-invoice="savingPendingInvoice"
    :can-pay="canPay"
    :paying-invoice="payingInvoice"
    :canceling-pending-invoice="cancelingPendingInvoice"
    :dinh-dang-tien="dinhDangTien"
    :so-luong-con-lai="soLuongConLai"
    @reset-draft="resetDraft"
    @select-invoice="chonHoaDonCho"
    @update:customer-keyword="customerKeyword = $event"
    @focus-customer="moDanhSachKhachHang"
    @blur-customer="dongDanhSachKhachHang"
    @select-customer="chonKhachHang"
    @select-guest="chonKhachVangLai"
    @clear-customer="boChonKhachHang"
    @update:product-keyword="productKeyword = $event"
    @focus-product="moDanhSachSanPham"
    @blur-product="dongDanhSachSanPham"
    @open-product="moChiTietSanPham"
    @increase-item="tangSoLuong"
    @decrease-item="giamSoLuong"
    @close-product-detail="dongChiTietSanPham"
    @select-color="chonMauSac"
    @select-size="chonKichCo"
    @decrease-quantity="giamSoLuongChiTiet"
    @increase-quantity="tangSoLuongChiTiet"
    @add-selected-variant="themBienTheDangChon"
    @update:coupon-code="couponCode = $event"
    @focus-coupon="handleCouponFocus"
    @blur-coupon="handleCouponBlur"
    @apply-coupon="handleApplyCoupon"
    @select-coupon="chonPhieuGiamGia"
    @remove-coupon="handleRemoveCoupon"
    @update:payment-method="paymentMethod = $event"
    @amount-input="handleAmountPaidInput"
    @update:payment-note="paymentNote = $event"
    @create-pending-invoice="handleCreatePendingInvoice"
    @pay-now="handlePayNow"
    @cancel-pending-invoice="handleCancelPendingInvoice"
  />
</template>
