<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import {
  Award,
  BadgePercent,
  Box,
  Feather,
  Footprints,
  MoveVertical,
  Palette,
  Ruler,
  Search,
  Weight,
} from "lucide-vue-next";
import {
  apDungPhieuGiamGiaTaiQuay,
  huyHoaDonCho,
  layChiTietHoaDonCho,
  layDanhSachHoaDonCho,
  thanhToanTaiQuay,
  taoHoaDonCho,
  timKhachHangTheoSoDienThoai,
  timPhieuGiamGiaTaiQuay,
  timSanPhamTaiQuay,
  type HoaDonChoChiTiet,
  type HoaDonChoTomTat,
  type KhachHangTaiQuay,
  type PhieuGiamGiaTaiQuay,
  type SanPhamTaiQuay,
} from "../../../services/ban-hang-tai-quay";

const GUEST_LABEL = "Kh\u00e1ch v\u00e3ng lai";
const HIDDEN_INFO_LABEL = "\u1ea8n th\u00f4ng tin";

interface GioHangItem {
  chiTietId: number;
  maSanPham: string;
  tenSanPham: string;
  soLuong: number;
  giaBan: number;
  soLuongTon: number;
}

const customerKeyword = ref("");
const productKeyword = ref("");
const couponCode = ref("");
const customerResults = ref<KhachHangTaiQuay[]>([]);
const productResults = ref<SanPhamTaiQuay[]>([]);
const selectedProductDetail = ref<SanPhamTaiQuay | null>(null);
const selectedColor = ref("");
const selectedSize = ref("");
const selectedQuantity = ref(1);
const selectedCustomer = ref<KhachHangTaiQuay | null>(null);
const cartItems = ref<GioHangItem[]>([]);
const pendingInvoices = ref<HoaDonChoTomTat[]>([]);
const activePendingInvoice = ref<HoaDonChoTomTat | null>(null);
const appliedCoupon = ref<PhieuGiamGiaTaiQuay | null>(null);

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
const couponResults = ref<PhieuGiamGiaTaiQuay[]>([]);
const loadingCoupons = ref(false);
const showCouponDropdown = ref(false);
const pageError = ref("");
const successMessage = ref("");
const paymentMethod = ref(1);
const amountPaid = ref("");
const paymentNote = ref("");

let customerTimer: number | undefined;
let productTimer: number | undefined;
let couponTimer: number | undefined;
let couponDropdownTimer: number | undefined;

const tongSoLuong = computed(() =>
  cartItems.value.reduce((total, item) => total + item.soLuong, 0),
);
const tongTien = computed(() =>
  cartItems.value.reduce((total, item) => total + item.soLuong * item.giaBan, 0),
);
const tienGiam = computed(() => appliedCoupon.value?.soTienGiam ?? 0);
const productSearchLabel = computed(() =>
  productKeyword.value.trim() ? "K\u1ebft qu\u1ea3 t\u00ecm ki\u1ebfm s\u1ea3n ph\u1ea9m" : "S\u1ea3n ph\u1ea9m t\u1ea1i qu\u1ea7y",
);
const isGuestCustomer = computed(
  () => customerKeyword.value.trim().toLowerCase() === GUEST_LABEL.toLowerCase(),
);
const tenKhachHangHienThi = computed(() => {
  if (selectedCustomer.value) {
    return selectedCustomer.value.hoTen;
  }
  if (isGuestCustomer.value) {
    return GUEST_LABEL;
  }
  return customerKeyword.value.trim() || activePendingInvoice.value?.tenKhachHang || GUEST_LABEL;
});
const soDienThoaiKhachHangHienThi = computed(() => {
  if (selectedCustomer.value) {
    return selectedCustomer.value.sdt;
  }
  if (isGuestCustomer.value) {
    return HIDDEN_INFO_LABEL;
  }
  return activePendingInvoice.value?.soDienThoai || HIDDEN_INFO_LABEL;
});
const maPhieuChuaApDung = computed(() => Boolean(couponCode.value.trim()) && !appliedCoupon.value);
const daChonKhach = computed(
  () => Boolean(selectedCustomer.value) || Boolean(activePendingInvoice.value) || isGuestCustomer.value,
);
const khachCanTra = computed(() => appliedCoupon.value?.tongTienSauGiam ?? tongTien.value);
const coTheTimPhieu = computed(() => cartItems.value.length > 0 && tongTien.value > 0);
const coTheApDungPhieu = computed(() =>
  Boolean(couponCode.value.trim()) &&
  cartItems.value.length > 0 &&
  !applyingCoupon.value &&
  (!appliedCoupon.value || appliedCoupon.value.ma.toLowerCase() !== couponCode.value.trim().toLowerCase()),
);
const tienKhachThanhToan = computed(() => {
  const parsed = Number(amountPaid.value.replace(/[^\d]/g, ""));
  return Number.isFinite(parsed) ? parsed : 0;
});
const tienThua = computed(() => {
  if (paymentMethod.value !== 1) {
    return 0;
  }
  return Math.max(tienKhachThanhToan.value - khachCanTra.value, 0);
});
const canCreatePendingInvoice = computed(
  () => cartItems.value.length > 0 && !savingPendingInvoice.value && !maPhieuChuaApDung.value,
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

function dinhDangTien(value: number) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value || 0);
}

function dinhDangSo(value: number) {
  return new Intl.NumberFormat("vi-VN", {
    maximumFractionDigits: 0,
  }).format(value || 0);
}

function clearFeedback() {
  pageError.value = "";
  successMessage.value = "";
}

function taoDanhSachSanPhamThanhToan() {
  return cartItems.value.map((item) => ({
    chiTietId: item.chiTietId,
    soLuong: item.soLuong,
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

function soLuongDaChon(chiTietId: number) {
  return cartItems.value.find((item) => item.chiTietId === chiTietId)?.soLuong ?? 0;
}

function soLuongConLai(chiTietId: number, soLuongTon: number) {
  return Math.max(soLuongTon - soLuongDaChon(chiTietId), 0);
}

function laySoLuongTonHienTai(chiTietId: number, fallback: number) {
  return productResults.value.find((product) => product.chiTietId === chiTietId)?.soLuongTon ?? fallback;
}

const productDetailFields = computed(() => {
  if (!selectedProductDetail.value) {
    return [];
  }

  return [
    { label: "Lo\u1ea1i gi\u00e0y", value: selectedProductDetail.value.loaiGiay || "--", icon: Box },
    { label: "Th\u01b0\u01a1ng hi\u1ec7u", value: selectedProductDetail.value.thuongHieu || "--", icon: Award },
    { label: "\u0110\u1ebf gi\u00e0y", value: selectedProductDetail.value.deGiay || "--", icon: Footprints },
    { label: "C\u1ed5 gi\u00e0y", value: selectedProductDetail.value.coGiay || "--", icon: MoveVertical },
    { label: "C\u00f4ng ngh\u1ec7 \u0111\u1ec7m", value: selectedProductDetail.value.congNgheDem || "--", icon: Feather },
    { label: "M\u00e0u s\u1eafc", value: selectedProductDetail.value.mauSac || "--", icon: Palette },
    { label: "K\u00edch c\u1ee1", value: selectedProductDetail.value.kichCo || "--", icon: Ruler },
    { label: "Tr\u1ecdng l\u01b0\u1ee3ng", value: selectedProductDetail.value.trongLuong || "--", icon: Weight },
  ];
});
const relatedVariants = computed(() => {
  if (!selectedProductDetail.value) {
    return [];
  }

  return productResults.value.filter(
    (product) =>
      product.maSanPham === selectedProductDetail.value?.maSanPham &&
      product.tenSanPham === selectedProductDetail.value?.tenSanPham,
  );
});
const colorOptions = computed(() => {
  const grouped = new Map<string, SanPhamTaiQuay>();

  for (const variant of relatedVariants.value) {
    const key = variant.mauSac || variant.maBienThe;
    if (!grouped.has(key)) {
      grouped.set(key, variant);
    }
  }

  return Array.from(grouped.values());
});
const sizeOptions = computed(() =>
  relatedVariants.value.filter((variant) => {
    if (!selectedColor.value) {
      return true;
    }
    return (variant.mauSac || variant.maBienThe) === selectedColor.value;
  }),
);
const selectedVariant = computed(() => {
  if (!selectedProductDetail.value) {
    return null;
  }

  return (
    relatedVariants.value.find(
      (variant) =>
        (selectedColor.value ? (variant.mauSac || variant.maBienThe) === selectedColor.value : true) &&
        (selectedSize.value ? (variant.kichCo || "") === selectedSize.value : true),
    ) || selectedProductDetail.value
  );
});
const chiTietDangChon = computed(() => selectedVariant.value || selectedProductDetail.value);
const soLuongTonKhaDungChiTiet = computed(() => {
  if (!chiTietDangChon.value) {
    return 0;
  }

  return soLuongConLai(chiTietDangChon.value.chiTietId, chiTietDangChon.value.soLuongTon);
});
const soLuongTonSauKhiChon = computed(() =>
  Math.max(soLuongTonKhaDungChiTiet.value - selectedQuantity.value, 0),
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
    pageError.value = error instanceof Error ? error.message : "Kh\u00f4ng th\u1ec3 t\u1ea3i danh s\u00e1ch h\u00f3a \u0111\u01a1n ch\u1edd";
  } finally {
    loadingPendingInvoices.value = false;
  }
}

async function fetchCustomers(keyword: string) {
  if (!keyword.trim() || keyword.trim().toLowerCase() === GUEST_LABEL.toLowerCase()) {
    customerResults.value = [];
    return;
  }

  loadingCustomers.value = true;
  try {
    customerResults.value = await timKhachHangTheoSoDienThoai(keyword);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\u00f4ng th\u1ec3 t\u00ecm kh\u00e1ch h\u00e0ng";
  } finally {
    loadingCustomers.value = false;
  }
}

async function fetchProducts(keyword: string) {
  loadingProducts.value = true;
  try {
    productResults.value = await timSanPhamTaiQuay(keyword);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\u00f4ng th\u1ec3 t\u00ecm s\u1ea3n ph\u1ea9m";
  } finally {
    loadingProducts.value = false;
  }
}

async function fetchCoupons(keyword: string) {
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
      tongTienHang: tongTien.value,
    });
  } catch (error) {
    couponResults.value = [];
    pageError.value = error instanceof Error ? error.message : "Kh\u00f4ng th\u1ec3 t\u00ecm phi\u1ebfu gi\u1ea3m gi\u00e1";
  } finally {
    loadingCoupons.value = false;
  }
}

watch(customerKeyword, (value) => {
  if (customerTimer) {
    window.clearTimeout(customerTimer);
  }
  const keyword = value.trim().toLowerCase();
  showCustomerDropdown.value = value.trim().length > 0 && keyword !== GUEST_LABEL.toLowerCase();
  customerTimer = window.setTimeout(() => {
    void fetchCustomers(value);
  }, 250);
});

watch(productKeyword, (value) => {
  if (productTimer) {
    window.clearTimeout(productTimer);
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

  if (!showCouponDropdown.value) {
    return;
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

  if (!showCouponDropdown.value) {
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

function chonPhieuGiamGia(coupon: PhieuGiamGiaTaiQuay) {
  if (couponDropdownTimer) {
    window.clearTimeout(couponDropdownTimer);
  }

  couponCode.value = coupon.ma;
  appliedCoupon.value = coupon;
  showCouponDropdown.value = false;
  capNhatTienKhachThanhToan();
  clearFeedback();
}

function chonKhachHang(customer: KhachHangTaiQuay) {
  selectedCustomer.value = customer;
  customerKeyword.value = customer.hoTen;
  customerResults.value = [];
  showCustomerDropdown.value = false;
  danhDauCanApDungLaiPhieu();
  clearFeedback();
}

function boChonKhachHang() {
  selectedCustomer.value = null;
  customerKeyword.value = GUEST_LABEL;
  customerResults.value = [];
  showCustomerDropdown.value = false;
  danhDauCanApDungLaiPhieu();
}

function chonKhachVangLai() {
  selectedCustomer.value = null;
  customerKeyword.value = GUEST_LABEL;
  customerResults.value = [];
  showCustomerDropdown.value = false;
  danhDauCanApDungLaiPhieu();
  clearFeedback();
}

function moChiTietSanPham(product: SanPhamTaiQuay) {
  if (!daChonKhach.value) {
    pageError.value = "Vui l\u00f2ng ch\u1ecdn kh\u00e1ch h\u00e0ng ho\u1eb7c Kh\u00e1ch v\u00e3ng lai tr\u01b0\u1edbc khi th\u00eam s\u1ea3n ph\u1ea9m";
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

function themSanPham(product: SanPhamTaiQuay, quantity = 1) {
  if (!daChonKhach.value) {
    pageError.value = "Vui l\u00f2ng ch\u1ecdn kh\u00e1ch h\u00e0ng ho\u1eb7c Kh\u00e1ch v\u00e3ng lai tr\u01b0\u1edbc khi th\u00eam s\u1ea3n ph\u1ea9m";
    return;
  }

  const soLuongCoTheThem = soLuongConLai(product.chiTietId, product.soLuongTon);
  const existing = cartItems.value.find((item) => item.chiTietId === product.chiTietId);
  if (existing) {
    if (quantity > soLuongCoTheThem) {
      pageError.value = `S\u1ea3n ph\u1ea9m ${existing.tenSanPham} \u0111\u00e3 \u0111\u1ea1t gi\u1edbi h\u1ea1n t\u1ed3n kho`;
      return;
    }
    existing.soLuong += quantity;
  } else {
    if (quantity > soLuongCoTheThem) {
      pageError.value = `S\u1ea3n ph\u1ea9m ${product.tenSanPham} \u0111\u00e3 v\u01b0\u1ee3t gi\u1edbi h\u1ea1n t\u1ed3n kho`;
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
        soLuongTon: product.soLuongTon,
      },
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

function chonMauSac(value: string) {
  selectedColor.value = value;
  selectedSize.value = sizeOptions.value[0]?.kichCo || "";
  selectedQuantity.value = 1;
}

function chonKichCo(value: string) {
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
    pageError.value = "Vui l\u00f2ng ch\u1ecdn m\u00e0u s\u1eafc v\u00e0 k\u00edch c\u1ee1 ph\u00f9 h\u1ee3p";
    return;
  }

  themSanPham(selectedVariant.value, selectedQuantity.value);
}

function tangSoLuong(chiTietId: number) {
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
    pageError.value = `S\u1ea3n ph\u1ea9m ${reachedLimit} \u0111\u00e3 v\u01b0\u1ee3t gi\u1edbi h\u1ea1n t\u1ed3n kho`;
    return;
  }
  danhDauCanApDungLaiPhieu();
  capNhatTienKhachThanhToan();
}

function giamSoLuong(chiTietId: number) {
  cartItems.value = cartItems.value
    .map((item) =>
      item.chiTietId === chiTietId ? { ...item, soLuong: item.soLuong - 1 } : item,
    )
    .filter((item) => item.soLuong > 0);
  danhDauCanApDungLaiPhieu();
  capNhatTienKhachThanhToan();
}

function mapInvoiceToDraft(invoice: HoaDonChoChiTiet) {
  customerKeyword.value = invoice.tenKhachHang || invoice.soDienThoai || GUEST_LABEL;
  selectedCustomer.value = invoice.khachHangId
    ? {
        id: invoice.khachHangId,
        hoTen: invoice.tenKhachHang,
        sdt: invoice.soDienThoai,
        email: null,
      }
    : null;
  cartItems.value = invoice.items.map((item) => ({
    chiTietId: item.chiTietId,
    maSanPham: item.maSanPham,
    tenSanPham: item.tenSanPham,
    soLuong: item.soLuong,
    giaBan: item.giaBan,
    soLuongTon: laySoLuongTonHienTai(item.chiTietId, item.soLuong),
  }));
  couponCode.value = invoice.phieuGiamGia?.ma ?? "";
  appliedCoupon.value = invoice.phieuGiamGia
    ? {
        id: 0,
        ma: invoice.phieuGiamGia.ma,
        ten: invoice.phieuGiamGia.ten,
        loai: 0,
        giaTri: 0,
        giaTriToiThieu: null,
        giamToiDa: null,
        soTienGiam: invoice.tienGiam || invoice.phieuGiamGia.soTienGiam,
        tongTienHang: invoice.tongTienHang || 0,
        tongTienSauGiam: invoice.tongTien || 0,
      }
    : null;
  couponResults.value = [];
  showCouponDropdown.value = false;
  capNhatTienKhachThanhToan(true);
}

async function chonHoaDonCho(invoice: HoaDonChoTomTat) {
  invoiceLoading.value = true;
  pageError.value = "";
  try {
    await fetchProducts("");
    const detail = await layChiTietHoaDonCho(invoice.id);
    activePendingInvoice.value = invoice;
    mapInvoiceToDraft(detail);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Không thể tải hóa đơn chờ";
  } finally {
    invoiceLoading.value = false;
  }
}

async function handleApplyCoupon() {
  if (!coTheApDungPhieu.value) {
    return;
  }

  applyingCoupon.value = true;
  pageError.value = "";
  successMessage.value = "";

  try {
    const coupon = await apDungPhieuGiamGiaTaiQuay({
      hoaDonId: activePendingInvoice.value?.id ?? null,
      khachHangId: layKhachHangIdHienTai(),
      maPhieuGiamGia: couponCode.value.trim(),
      items: taoDanhSachSanPhamThanhToan(),
    });
    appliedCoupon.value = coupon;
    couponCode.value = coupon.ma;
    couponResults.value = [];
    showCouponDropdown.value = false;
    capNhatTienKhachThanhToan();
    successMessage.value = `Đã áp dụng mã ${coupon.ma}`;
  } catch (error) {
    appliedCoupon.value = null;
    pageError.value = error instanceof Error ? error.message : "Không thể áp dụng phiếu giảm giá";
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
      items: taoDanhSachSanPhamThanhToan(),
    });

    successMessage.value = `\u0110\u00e3 t\u1ea1o h\u00f3a \u0111\u01a1n ch\u1edd ${createdInvoice.ma}`;
    await fetchPendingInvoices();
    const matchedInvoice = pendingInvoices.value.find((invoice) => invoice.id === createdInvoice.id) ?? null;
    activePendingInvoice.value = matchedInvoice;
    mapInvoiceToDraft(createdInvoice);
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\u00f4ng th\u1ec3 t\u1ea1o h\u00f3a \u0111\u01a1n ch\u1edd";
  } finally {
    savingPendingInvoice.value = false;
  }
}

function formatCurrencyInput() {
  if (paymentMethod.value !== 1) {
    amountPaid.value = dinhDangSo(khachCanTra.value);
    return;
  }

  const digits = amountPaid.value.replace(/[^\d]/g, "");
  amountPaid.value = digits ? new Intl.NumberFormat("vi-VN").format(Number(digits)) : "";
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
      items: taoDanhSachSanPhamThanhToan(),
    });

    successMessage.value = `\u0110\u00e3 thanh to\u00e1n ${response.maHoaDon}`;
    await fetchPendingInvoices();
    resetDraft();
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\u00f4ng th\u1ec3 thanh to\u00e1n tr\u1ef1c ti\u1ebfp";
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
    successMessage.value = `\u0110\u00e3 h\u1ee7y h\u00f3a \u0111\u01a1n ch\u1edd ${activePendingInvoice.value.ma}`;
    await fetchPendingInvoices();
    resetDraft();
  } catch (error) {
    pageError.value = error instanceof Error ? error.message : "Kh\u00f4ng th\u1ec3 h\u1ee7y h\u00f3a \u0111\u01a1n ch\u1edd";
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
  <div class="p-6">
    <div class="mb-6 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div>
        <h1 class="mt-2 text-3xl font-bold text-slate-900">B&#225;n h&#224;ng t&#7841;i qu&#7847;y</h1>
      </div>
      <button
        type="button"
        class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-red-300 hover:text-red-500"
        @click="resetDraft"
      >
        T&#7841;o phi&#7871;u m&#7899;i
      </button>
    </div>

    <section class="mb-6 rounded-[28px] border border-white/70 bg-white/90 p-5 shadow-[0_24px_60px_rgba(15,23,42,0.08)] backdrop-blur">
      <div class="mb-4 flex items-center justify-between">
        <div>
          <h2 class="text-lg font-bold text-slate-900">H&#243;a &#273;&#417;n ch&#7901;</h2>
          <p class="text-sm text-slate-500">Ch&#7885;n nhanh &#273;&#7875; xem l&#7841;i h&#243;a &#273;&#417;n &#273;ang ch&#7901; x&#7917; l&#253;.</p>
        </div>
        <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
          {{ loadingPendingInvoices ? "\u0110ang t\u1ea3i..." : `${pendingInvoices.length} h\u00f3a \u0111\u01a1n` }}
        </span>
      </div>

      <div class="flex flex-wrap gap-3">
        <button
          v-for="invoice in pendingInvoices"
          :key="invoice.id"
          type="button"
          class="min-w-[220px] rounded-2xl border px-4 py-3 text-left transition"
          :class="
            activePendingInvoice?.id === invoice.id
              ? 'border-red-500 bg-red-50 shadow-[0_16px_30px_rgba(239,68,68,0.15)]'
              : 'border-slate-200 bg-slate-50 hover:border-red-200 hover:bg-white'
          "
          @click="chonHoaDonCho(invoice)"
        >
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-sm font-bold text-slate-900">{{ invoice.ma }}</p>
              <p class="mt-1 text-sm text-slate-600">{{ invoice.tenKhachHang }}</p>
            </div>
            <span class="rounded-full bg-white px-2 py-1 text-xs font-semibold text-slate-500">
              {{ invoice.tongSanPham }} SP
            </span>
          </div>
          <p class="mt-3 text-sm font-semibold text-red-500">{{ dinhDangTien(invoice.tongTien) }}</p>
        </button>

        <div
          v-if="!loadingPendingInvoices && !pendingInvoices.length"
          class="rounded-2xl border border-dashed border-slate-200 px-4 py-6 text-sm text-slate-500"
        >
          Ch&#432;a c&#243; h&#243;a &#273;&#417;n ch&#7901; n&#224;o.
        </div>
      </div>
    </section>

    <div class="grid gap-6 xl:grid-cols-[1.5fr_0.8fr]">
      <section class="space-y-6 rounded-[32px] border border-white/70 bg-white/95 p-6 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
        <div class="grid gap-4 lg:grid-cols-2">
          <div class="relative">
            <label class="mb-2 block text-sm font-semibold text-slate-700">T&#236;m kh&#225;ch h&#224;ng theo t&#234;n ho&#7863;c s&#7889; &#273;i&#7879;n tho&#7841;i</label>
            <div class="flex gap-3">
              <input
                v-model="customerKeyword"
                type="text"
                placeholder="Nh&#7853;p t&#234;n ho&#7863;c s&#7889; &#273;i&#7879;n tho&#7841;i kh&#225;ch h&#224;ng"
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300 focus:bg-white"
                @focus="moDanhSachKhachHang"
                @blur="dongDanhSachKhachHang"
              />
              <button
                type="button"
                class="shrink-0 rounded-2xl border border-dashed border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-red-300 hover:text-red-500"
                @click="chonKhachVangLai"
              >
                Kh&#225;ch v&#227;ng lai
              </button>
            </div>

            <div v-if="loadingCustomers" class="absolute right-4 top-[46px] text-xs font-semibold text-slate-400">
              &#272;ang t&#236;m...
            </div>

            <div
              v-if="showCustomerDropdown"
              class="absolute z-20 mt-2 w-full rounded-2xl border border-slate-200 bg-white p-2 shadow-[0_24px_50px_rgba(15,23,42,0.12)]"
            >
              <button v-if="false"
                type="button"
                class="mb-1 w-full rounded-2xl border border-dashed border-slate-200 px-3 py-3 text-left transition hover:border-red-200 hover:bg-red-50"
                @click="chonKhachVangLai"
              >
                <p class="text-sm font-semibold text-slate-900">Kh&#225;ch v&#227;ng lai</p>
                <p class="mt-1 text-xs text-slate-500">Kh&#244;ng l&#432;u s&#7889; &#273;i&#7879;n tho&#7841;i ho&#7863;c th&#244;ng tin c&#225; nh&#226;n</p>
              </button>
              <div v-if="!loadingCustomers && !customerResults.length" class="rounded-2xl px-3 py-3 text-sm text-slate-500">
                Kh&#244;ng t&#236;m th&#7845;y kh&#225;ch h&#224;ng ph&#249; h&#7907;p.
              </div>
              <button
                v-for="customer in customerResults"
                :key="customer.id"
                type="button"
                class="w-full rounded-2xl px-3 py-3 text-left transition hover:bg-red-50"
                @click="chonKhachHang(customer)"
              >
                <p class="text-sm font-semibold text-slate-900">{{ customer.hoTen }}</p>
                <p class="mt-1 text-xs text-slate-500">{{ customer.sdt }} <span v-if="customer.email">- {{ customer.email }}</span></p>
              </button>
            </div>
          </div>

          <div class="rounded-3xl border border-slate-100 bg-slate-50 p-4">
            <div class="flex items-start justify-between gap-3">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Khách được chọn</p>
                <p class="mt-2 text-lg font-bold text-slate-900">{{ tenKhachHangHienThi }}</p>
                <p class="mt-1 text-sm text-slate-500">{{ soDienThoaiKhachHangHienThi }}</p>
              </div>
              <button
                v-if="selectedCustomer || isGuestCustomer"
                type="button"
                class="text-sm font-semibold text-slate-400 transition hover:text-red-500"
                @click="boChonKhachHang"
              >
                Bỏ chọn
              </button>
            </div>
          </div>
        </div>

        <div class="relative">
          <label class="mb-2 block text-sm font-semibold text-slate-700">T&#236;m s&#7843;n ph&#7849;m</label>
          <input
            v-model="productKeyword"
            type="text"
            placeholder="Nh&#7853;p m&#227;, t&#234;n s&#7843;n ph&#7849;m, SKU..."
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300 focus:bg-white"
            @focus="moDanhSachSanPham"
            @blur="dongDanhSachSanPham"
          />

          <div v-if="loadingProducts" class="absolute right-4 top-[46px] text-xs font-semibold text-slate-400">
            &#272;ang t&#236;m...
          </div>

          <div
            v-if="showProductDropdown"
            class="absolute z-20 mt-2 w-full rounded-3xl border border-slate-200 bg-white p-2 shadow-[0_24px_50px_rgba(15,23,42,0.12)]"
          >
            <div v-if="!loadingProducts && !productResults.length" class="rounded-2xl px-3 py-3 text-sm text-slate-500">
              Kh&#244;ng t&#236;m th&#7845;y s&#7843;n ph&#7849;m ph&#249; h&#7907;p.
            </div>
            <button
              v-for="product in productResults"
              :key="product.chiTietId"
              type="button"
              class="flex w-full items-start justify-between gap-4 rounded-2xl px-3 py-3 text-left transition hover:bg-red-50"
              @click="moChiTietSanPham(product)"
            >
              <div>
                <p class="text-sm font-bold text-slate-900">{{ product.tenSanPham }}</p>
                <p class="mt-1 text-xs text-slate-500">
                  M&#227;: {{ product.maSanPham }} | SKU: {{ product.sku }} | Bi&#7871;n th&#7875;: {{ product.maBienThe }}
                </p>
              </div>
              <div class="text-right">
                <p class="text-sm font-semibold text-red-500">{{ dinhDangTien(product.giaBan) }}</p>
                <p class="mt-1 text-xs text-slate-500">T&#7891;n: {{ product.soLuongTon }}</p>
              </div>
            </button>
          </div>
        </div>

        <div class="rounded-[28px] border border-slate-100 bg-[linear-gradient(180deg,#fff8f5_0%,#ffffff_100%)] p-4 shadow-[0_18px_40px_rgba(15,23,42,0.06)]">
          <div class="flex flex-col gap-3 border-b border-slate-100 pb-4 md:flex-row md:items-center md:justify-between">
            <div>
              <p class="text-sm font-semibold text-slate-800">{{ productSearchLabel }}</p>
            </div>
            <div class="rounded-2xl bg-white px-4 py-3 text-xs font-semibold text-slate-500 shadow-sm">
              {{ loadingProducts ? "Đang tải sản phẩm..." : productResults.length + " sản phẩm" }}
            </div>
          </div>

          <div class="mt-4 max-h-[360px] space-y-3 overflow-y-auto pr-1">
            <div
              v-if="!loadingProducts && !productResults.length"
              class="rounded-2xl border border-dashed border-slate-200 bg-white px-4 py-8 text-center text-sm text-slate-500"
            >
              Không tìm thấy sản phẩm phù hợp.
            </div>

            <button
              v-for="product in productResults"
              :key="`panel-${product.chiTietId}`"
              type="button"
              class="flex w-full items-center justify-between gap-4 rounded-[24px] border border-white bg-white px-4 py-4 text-left shadow-[0_12px_30px_rgba(15,23,42,0.06)] transition hover:-translate-y-0.5 hover:border-red-200 hover:bg-red-50"
              @click="moChiTietSanPham(product)"
            >
              <div class="flex min-w-0 items-center gap-4">
                <div class="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#fff1eb_0%,#ffe4dc_100%)] text-lg font-bold text-red-400">
                  {{ product.tenSanPham.slice(0, 1) }}
                </div>
                <div class="min-w-0">
                  <p class="truncate text-base font-bold text-slate-900">{{ product.tenSanPham }}</p>
                  <p class="mt-1 truncate text-xs text-slate-500">
                    Mã: {{ product.maSanPham }} | SKU: {{ product.sku }} | Biến thể: {{ product.maBienThe }}
                  </p>
                  <p class="mt-2 text-sm font-semibold text-slate-700">Tồn kho: x{{ product.soLuongTon }}</p>
                </div>
              </div>

              <div class="shrink-0 text-right">
                <p class="text-sm font-semibold text-red-500">{{ dinhDangTien(product.giaBan) }}</p>
                <span class="mt-2 inline-flex rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
                  Xem chi tiết
                </span>
              </div>
            </button>
          </div>
        </div>

        <div class="overflow-hidden rounded-[28px] border border-slate-100">
          <table class="min-w-full border-collapse">
            <thead class="bg-slate-100 text-left text-sm text-slate-600">
              <tr>
                <th class="px-5 py-4 font-semibold">STT</th>
                <th class="px-5 py-4 font-semibold">Mã sản phẩm</th>
                <th class="px-5 py-4 font-semibold">Tên sản phẩm</th>
                <th class="px-5 py-4 font-semibold">Đơn giá</th>
                <th class="px-5 py-4 font-semibold">Số lượng</th>
              </tr>
            </thead>
            <tbody class="bg-white text-sm text-slate-700">
              <tr v-for="(item, index) in cartItems" :key="item.chiTietId" class="border-t border-slate-100">
                <td class="px-5 py-4 font-semibold text-slate-900">{{ index + 1 }}</td>
                <td class="px-5 py-4 font-semibold text-slate-600">{{ item.maSanPham }}</td>
                <td class="px-5 py-4">
                  <p class="font-semibold text-slate-900">{{ item.tenSanPham }}</p>
                </td>
                <td class="px-5 py-4 font-semibold text-slate-700">{{ dinhDangTien(item.giaBan) }}</td>
                <td class="px-5 py-4">
                  <div class="inline-flex items-center rounded-full border border-slate-200 bg-slate-50">
                    <button
                      type="button"
                      class="px-3 py-1 text-base font-bold text-slate-500 transition hover:text-red-500"
                      @click="giamSoLuong(item.chiTietId)"
                    >
                      -
                    </button>
                    <span class="min-w-10 px-2 text-center font-semibold text-slate-900">{{ item.soLuong }}</span>
                    <button
                      type="button"
                      class="px-3 py-1 text-base font-bold transition"
                      :class="
                        soLuongConLai(item.chiTietId, item.soLuongTon) <= 0
                          ? 'cursor-not-allowed text-slate-300'
                          : 'text-slate-500 hover:text-red-500'
                      "
                      @click="tangSoLuong(item.chiTietId)"
                    >
                      +
                    </button>
                  </div>
                  <p class="mt-2 text-xs text-slate-400">Tồn kho: {{ item.soLuongTon }}</p>
                </td>
              </tr>
              <tr v-if="!cartItems.length">
                <td colspan="5" class="px-5 py-14 text-center text-sm text-slate-400">
                  Chọn sản phẩm từ ô tìm kiếm để đưa vào hóa đơn.
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div
          v-if="selectedProductDetail"
          class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/45 px-4 py-6"
          @click.self="dongChiTietSanPham"
        >
          <div class="max-h-[85vh] w-full max-w-3xl overflow-hidden rounded-[32px] bg-white shadow-[0_30px_80px_rgba(15,23,42,0.25)]">
            <div class="flex items-start justify-between border-b border-slate-100 px-6 py-5">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.2em] text-red-400">Sản phẩm chi tiết</p>
                <h3 class="mt-2 text-2xl font-bold text-slate-900">{{ selectedProductDetail.tenSanPham }}</h3>
                <p class="mt-1 text-sm text-slate-500">
                  Mã: {{ selectedProductDetail.maSanPham }} | Biến thể: {{ selectedProductDetail.maBienThe }}
                </p>
              </div>
              <button
                type="button"
                class="rounded-full bg-slate-100 px-3 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-200"
                @click="dongChiTietSanPham"
              >
                Đóng
              </button>
            </div>

            <div class="max-h-[calc(85vh-110px)] overflow-y-auto px-6 py-5">
              <div class="mb-4 rounded-2xl border border-amber-100 bg-amber-50 px-4 py-3 text-sm text-amber-700">
                Chọn màu sắc, kích cỡ và số lượng trước khi thêm vào hóa đơn.
              </div>

              <div class="mb-5 flex items-center justify-between rounded-[24px] border border-slate-200 bg-slate-50 px-5 py-4">
                <div>
                  <p class="text-sm text-slate-500">SKU</p>
                  <p class="mt-1 text-base font-semibold text-slate-900">{{ chiTietDangChon?.sku }}</p>
                </div>
                <div class="text-right">
                  <p class="text-sm text-slate-500">Tồn kho còn lại</p>
                  <p class="mt-1 text-base font-semibold text-slate-900">{{ soLuongTonSauKhiChon }}</p>
                </div>
                <div class="text-right">
                  <p class="text-sm text-slate-500">Giá bán</p>
                  <p class="mt-1 text-base font-bold text-red-500">{{ dinhDangTien(chiTietDangChon?.giaBan || 0) }}</p>
                </div>
              </div>

              <div class="mt-6 grid gap-6">
                <div class="grid gap-3 md:grid-cols-[84px_1fr] md:items-start">
                  <p class="text-base font-medium text-slate-700">Màu sắc</p>
                  <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                    <button
                      v-for="option in colorOptions"
                      :key="`color-${option.mauSac || option.maBienThe}`"
                      type="button"
                      class="flex items-center gap-3 rounded-xl border px-3 py-2 text-left transition"
                      :class="
                        selectedColor === (option.mauSac || option.maBienThe)
                          ? 'border-red-400 bg-red-50 text-red-600'
                          : 'border-slate-200 bg-white text-slate-700 hover:border-red-200 hover:bg-red-50'
                      "
                      @click="chonMauSac(option.mauSac || option.maBienThe)"
                    >
                      <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100 text-xs font-bold text-slate-500">
                        {{ (option.mauSac || "?").slice(0, 1) }}
                      </div>
                      <div class="min-w-0">
                        <p class="truncate text-sm font-semibold">{{ option.mauSac || option.maBienThe }}</p>
                        <p class="truncate text-xs text-slate-500">{{ option.maBienThe }}</p>
                      </div>
                    </button>
                  </div>
                </div>

                <div class="grid gap-3 md:grid-cols-[84px_1fr] md:items-start">
                  <p class="text-base font-medium text-slate-700">Size</p>
                  <div class="flex flex-wrap gap-3">
                    <button
                      v-for="option in sizeOptions"
                      :key="`size-${option.chiTietId}`"
                      type="button"
                      class="min-w-20 rounded-xl border px-5 py-3 text-center text-sm font-semibold transition"
                      :class="
                        selectedSize === (option.kichCo || '')
                          ? 'border-red-400 bg-red-50 text-red-600'
                          : 'border-slate-200 bg-white text-slate-700 hover:border-red-200 hover:bg-red-50'
                      "
                      @click="chonKichCo(option.kichCo || '')"
                    >
                      {{ option.kichCo || '--' }}
                    </button>
                  </div>
                </div>

                <div class="text-sm font-medium text-blue-600">Bảng Quy Đổi Kích Cỡ</div>

                <div class="grid gap-3 md:grid-cols-[84px_1fr_120px] md:items-center">
                  <p class="text-base font-medium text-slate-700">Số lượng</p>
                  <div class="inline-flex w-fit items-center rounded-xl border border-slate-200 bg-white">
                    <button
                      type="button"
                      class="px-4 py-3 text-lg font-bold transition disabled:cursor-not-allowed disabled:text-slate-300"
                      :disabled="selectedQuantity <= 1"
                      @click="giamSoLuongChiTiet"
                    >
                      -
                    </button>
                    <span class="min-w-14 border-x border-slate-200 px-4 py-3 text-center text-base font-semibold text-slate-900">
                      {{ selectedQuantity }}
                    </span>
                    <button
                      type="button"
                      class="px-4 py-3 text-lg font-bold transition disabled:cursor-not-allowed disabled:text-slate-300"
                      :disabled="selectedQuantity >= soLuongTonKhaDungChiTiet"
                      @click="tangSoLuongChiTiet"
                    >
                      +
                    </button>
                  </div>
                  <p class="text-sm font-semibold uppercase text-emerald-600">
                    {{ soLuongTonKhaDungChiTiet > 0 ? 'Còn hàng' : 'Hết hàng' }}
                  </p>
                </div>
              </div>

              <div class="mt-6 flex justify-end">
                <button
                  type="button"
                  class="rounded-2xl bg-red-500 px-5 py-3 text-sm font-bold text-white shadow-[0_20px_40px_rgba(239,68,68,0.25)] transition hover:bg-red-600"
                  @click="themBienTheDangChon"
                >
                  Thêm vào hóa đơn
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

            <aside class="rounded-[32px] border border-white/70 bg-white/95 p-6 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
        <div class="rounded-[28px] bg-[linear-gradient(180deg,#fff7f4_0%,#ffffff_100%)] p-5">
          <p class="text-xs font-semibold uppercase tracking-[0.24em] text-red-400">Tổng quan</p>
          <h2 class="mt-3 text-2xl font-bold text-slate-900">
            {{ activePendingInvoice?.ma || "Hóa đơn mới" }}
          </h2>
          <p class="mt-2 text-sm text-slate-500">
            {{ invoiceLoading ? "Đang tải chi tiết hóa đơn..." : "Hóa đơn bán hàng tại quầy đang thao tác." }}
          </p>

          <div class="mt-6 space-y-4">
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Tổng sản phẩm</span>
              <span class="text-lg font-bold text-slate-900">{{ tongSoLuong }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Tổng tiền hàng</span>
              <span class="text-lg font-bold text-slate-900">{{ dinhDangTien(tongTien) }}</span>
            </div>

            <div class="rounded-3xl border border-amber-100 bg-white p-4 shadow-sm">
              <div class="flex items-start justify-between gap-3">
                <div class="flex items-start gap-3">
                  <div class="rounded-2xl bg-rose-50 p-2.5 text-red-500">
                    <BadgePercent class="h-5 w-5" />
                  </div>
                  <div>
                    <p class="text-sm font-semibold text-slate-800">Mã phiếu giảm giá</p>
                    <p class="mt-1 text-xs text-slate-500">Tìm theo mã hoặc tên, hoặc chọn nhanh trong danh sách gợi ý.</p>
                  </div>
                </div>
                <span
                  v-if="appliedCoupon"
                  class="inline-flex rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-700"
                >
                  Đã áp dụng
                </span>
              </div>

              <div class="mt-3" @focusin="handleCouponFocus" @focusout="handleCouponBlur">
                <div class="flex gap-2">
                  <div class="relative flex-1">
                    <Search class="pointer-events-none absolute top-1/2 left-4 h-4 w-4 -translate-y-1/2 text-slate-400" />
                    <input
                      v-model="couponCode"
                      type="text"
                      placeholder="Tìm mã hoặc tên phiếu"
                      class="w-full rounded-2xl border border-slate-200 bg-white py-3 pr-4 pl-11 text-sm text-slate-900 outline-none transition focus:border-red-300"
                    />
                  </div>
                  <button
                    type="button"
                    class="shrink-0 rounded-2xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                    :disabled="!coTheApDungPhieu"
                    @click="handleApplyCoupon"
                  >
                    <span class="flex items-center gap-2">
                      <Search class="h-4 w-4" />
                      {{ applyingCoupon ? "Đang áp dụng..." : "Áp dụng" }}
                    </span>
                  </button>
                </div>

                <div v-if="showCouponDropdown" class="mt-3 rounded-2xl border border-slate-200 bg-slate-50 p-2">
                  <div v-if="!coTheTimPhieu" class="px-3 py-3 text-sm text-slate-500">
                    Thêm sản phẩm vào hóa đơn để xem phiếu phù hợp.
                  </div>
                  <div v-else-if="loadingCoupons" class="px-3 py-3 text-sm text-slate-500">
                    Đang tìm phiếu giảm giá phù hợp...
                  </div>
                  <div v-else-if="!couponResults.length" class="px-3 py-3 text-sm text-slate-500">
                    {{ couponCode.trim() ? "Không tìm thấy phiếu giảm giá phù hợp." : "Chưa có phiếu giảm giá phù hợp cho hóa đơn này." }}
                  </div>
                  <div v-else class="space-y-2">
                    <button
                      v-for="coupon in couponResults"
                      :key="coupon.id"
                      type="button"
                      class="w-full rounded-2xl bg-white px-3 py-3 text-left transition hover:border-red-200 hover:bg-red-50"
                      @mousedown.prevent
                      @click="chonPhieuGiamGia(coupon)"
                    >
                      <div class="flex items-start gap-3">
                        <div class="rounded-xl bg-rose-50 p-2 text-red-500">
                          <BadgePercent class="h-4 w-4" />
                        </div>
                        <div class="min-w-0 flex-1">
                          <div class="flex items-start justify-between gap-3">
                            <div class="min-w-0">
                              <p class="truncate text-sm font-semibold text-slate-800">{{ coupon.ma }}</p>
                              <p class="mt-1 text-xs text-slate-500">{{ coupon.ten }}</p>
                            </div>
                            <div class="text-right">
                              <p class="text-[11px] uppercase tracking-[0.18em] text-slate-400">Giảm</p>
                              <p class="text-sm font-bold text-emerald-600">{{ dinhDangTien(coupon.soTienGiam) }}</p>
                            </div>
                          </div>
                          <p class="mt-2 text-xs text-slate-400">
                            {{ coupon.giaTriToiThieu ? `Đơn tối thiểu ${dinhDangTien(coupon.giaTriToiThieu)}` : "Áp dụng ngay cho hóa đơn hiện tại" }}
                          </p>
                        </div>
                      </div>
                    </button>
                  </div>
                </div>
              </div>

              <p v-if="maPhieuChuaApDung" class="mt-2 text-xs font-medium text-amber-600">
                Mã phiếu đang thay đổi. Vui lòng áp dụng lại trước khi lưu hoặc thanh toán.
              </p>

              <div
                v-if="appliedCoupon"
                class="mt-3 rounded-2xl border border-emerald-100 bg-emerald-50 px-4 py-3"
              >
                <div class="flex items-start justify-between gap-3">
                  <div>
                    <p class="text-sm font-semibold text-emerald-700">{{ appliedCoupon.ma }}</p>
                    <p class="mt-1 text-xs text-emerald-600">{{ appliedCoupon.ten }}</p>
                  </div>
                  <button
                    type="button"
                    class="text-xs font-semibold text-emerald-700 transition hover:text-emerald-900"
                    @click="handleRemoveCoupon"
                  >
                    Bỏ mã
                  </button>
                </div>
                <div class="mt-3 flex items-center justify-between text-sm">
                  <span class="text-emerald-700">Tiền giảm</span>
                  <span class="font-bold text-emerald-700">{{ dinhDangTien(tienGiam) }}</span>
                </div>
              </div>
            </div>

            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Tiền giảm</span>
              <span class="text-lg font-bold text-emerald-600">{{ dinhDangTien(tienGiam) }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Khách cần trả</span>
              <span class="text-lg font-bold text-slate-900">{{ dinhDangTien(khachCanTra) }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Khách hàng</span>
              <span class="text-right text-sm font-semibold text-slate-700">{{ tenKhachHangHienThi }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Số điện thoại</span>
              <span class="text-right text-sm font-semibold text-slate-700">{{ soDienThoaiKhachHangHienThi }}</span>
            </div>
            <div>
              <p class="mb-2 text-sm text-slate-500">Hình thức thanh toán</p>
              <div class="grid grid-cols-2 gap-x-6 gap-y-3">
                <label class="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
                  <input v-model="paymentMethod" type="radio" class="h-4 w-4 accent-red-500" :value="1" />
                  <span>Tiền mặt</span>
                </label>
                <label class="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
                  <input v-model="paymentMethod" type="radio" class="h-4 w-4 accent-red-500" :value="2" />
                  <span>Chuyển khoản</span>
                </label>
                <label class="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
                  <input v-model="paymentMethod" type="radio" class="h-4 w-4 accent-red-500" :value="4" />
                  <span>Thẻ</span>
                </label>
                <label class="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
                  <input v-model="paymentMethod" type="radio" class="h-4 w-4 accent-red-500" :value="3" />
                  <span>Ví</span>
                </label>
              </div>
            </div>
            <div>
              <label class="mb-2 block text-sm text-slate-500">Khách thanh toán</label>
              <input
                v-model="amountPaid"
                type="text"
                :disabled="paymentMethod !== 1"
                :placeholder="paymentMethod === 1 ? 'Nhập số tiền khách đưa' : 'Tự động bằng số tiền cần thanh toán'"
                class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                @input="formatCurrencyInput"
              />
            </div>
            <div class="flex items-center justify-between border-b border-slate-200 pb-3">
              <span class="text-sm text-slate-500">Tiền thừa trả khách</span>
              <span class="text-lg font-bold text-slate-900">{{ dinhDangTien(tienThua) }}</span>
            </div>
            <div>
              <label class="mb-2 block text-sm text-slate-500">Ghi chú thanh toán</label>
              <textarea
                v-model="paymentNote"
                rows="3"
                placeholder="Ghi chú thêm nếu cần"
                class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
              />
            </div>
          </div>

          <div class="mt-8 grid gap-3 sm:grid-cols-2">
            <button
              type="button"
              class="rounded-2xl bg-slate-200 px-4 py-4 text-sm font-bold text-slate-700 transition hover:bg-slate-300 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
              :disabled="!canCreatePendingInvoice"
              @click="handleCreatePendingInvoice"
            >
              {{ savingPendingInvoice ? "Đang tạo..." : "Tạo hóa đơn chờ" }}
            </button>
            <button
              type="button"
              class="rounded-2xl bg-red-500 px-4 py-4 text-sm font-bold text-white shadow-[0_20px_40px_rgba(239,68,68,0.35)] transition hover:bg-red-600 disabled:cursor-not-allowed disabled:bg-slate-300 disabled:shadow-none"
              :disabled="!canPay"
              @click="handlePayNow"
            >
              {{ payingInvoice ? "Đang thanh toán..." : "Thanh toán" }}
            </button>
          </div>
          <button
            v-if="activePendingInvoice"
            type="button"
            class="mt-3 w-full rounded-2xl border border-red-200 bg-white px-4 py-3 text-sm font-semibold text-red-600 transition hover:bg-red-50 disabled:cursor-not-allowed disabled:border-slate-200 disabled:text-slate-400"
            :disabled="cancelingPendingInvoice"
            @click="handleCancelPendingInvoice"
          >
            {{ cancelingPendingInvoice ? "Đang hủy hóa đơn chờ..." : "Hủy hóa đơn chờ" }}
          </button>
        </div>
      </aside>
    </div>
  </div>
</template>


