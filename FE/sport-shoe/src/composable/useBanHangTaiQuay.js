import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import {
  apDungPhieuGiamGiaTaiQuay,
  huyHoaDonCho,
  layChiTietHoaDonCho,
  layDanhSachHoaDonCho,
  tinhPhiVanChuyenTaiQuay,
  thanhToanTaiQuay,
  taoHoaDonCho,
  timKhachHangTheoSoDienThoai,
  timPhieuGiamGiaTaiQuay,
  timSanPhamTaiQuay
} from "../services/ban-hang-tai-quay";

const GUEST_LABEL = "Khách vãng lai";
const HIDDEN_INFO_LABEL = "Ẩn thông tin";
const NO_CUSTOMER_LABEL = "Chưa chọn khách hàng";
const NO_CUSTOMER_PHONE_LABEL = "Chọn khách hoặc Khách vãng lai";
const MAX_PENDING_INVOICES = 5;
const MAX_PAYMENT_DIGITS = 15;

function useBanHangTaiQuay() {
  const customerKeyword = ref("");
  const productKeyword = ref("");
  const couponCode = ref("");
  const customerResults = ref([]);
  const productVariantResults = ref([]);
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
  const deliveryEnabled = ref(false);
  const deliveryRecipientName = ref("");
  const deliveryRecipientPhone = ref("");
  const deliveryAddress = ref("");
  const deliveryCarrier = ref("GHN");
  const deliveryFee = ref(0);
  const deliveryResolvedAddress = ref("");
  const deliveryCalculated = ref(false);
  const calculatingDeliveryFee = ref(false);
  const deliveryConfig = ref({
    serviceTypeId: 2,
    length: 30,
    width: 20,
    height: 12,
    weight: 500
  });

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
    () => productKeyword.value.trim() ? "Kết quả tìm kiếm sản phẩm" : "Sản phẩm tại quầy"
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
  const maPhieuChuaApDung = computed(
    () => Boolean(couponCode.value.trim()) && !appliedCoupon.value
  );
  const daChonKhach = computed(
    () => Boolean(selectedCustomer.value) || Boolean(activePendingInvoice.value) || isGuestCustomer.value
  );
  const tenNguoiNhanGiaoHangHienThi = computed(() => {
    if (deliveryRecipientName.value.trim()) {
      return deliveryRecipientName.value.trim();
    }
    if (selectedCustomer.value?.hoTen) {
      return selectedCustomer.value.hoTen;
    }
    return activePendingInvoice.value?.thongTinGiaoHang?.tenNguoiNhan || "";
  });
  const soDienThoaiNguoiNhanGiaoHangHienThi = computed(() => {
    if (deliveryRecipientPhone.value.trim()) {
      return deliveryRecipientPhone.value.trim();
    }
    if (selectedCustomer.value?.sdt) {
      return selectedCustomer.value.sdt;
    }
    return activePendingInvoice.value?.thongTinGiaoHang?.soDienThoaiNguoiNhan || "";
  });
  const phiVanChuyenHienThi = computed(() => deliveryEnabled.value ? deliveryFee.value : 0);
  const khachCanTra = computed(() => tongTienSauGiamHienThi.value + phiVanChuyenHienThi.value);
  const coTheTinhPhiVanChuyen = computed(
    () => deliveryEnabled.value &&
      cartItems.value.length > 0 &&
      Boolean(deliveryAddress.value.trim()) &&
      !calculatingDeliveryFee.value
  );
  const coThongTinGiaoHangHopLe = computed(
    () => !deliveryEnabled.value ||
      (
        Boolean(tenNguoiNhanGiaoHangHienThi.value) &&
        Boolean(soDienThoaiNguoiNhanGiaoHangHienThi.value) &&
        Boolean(deliveryAddress.value.trim()) &&
        deliveryCalculated.value
      )
  );
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
    () => Boolean(phieuGiamGiaHopLeDangNhap.value) &&
      cartItems.value.length > 0 &&
      !applyingCoupon.value &&
      (
        !appliedCoupon.value ||
        appliedCoupon.value.ma.toLowerCase() !== phieuGiamGiaHopLeDangNhap.value.ma.toLowerCase()
      )
  );
  const pendingInvoiceLimitReached = computed(
    () => pendingInvoices.value.length >= MAX_PENDING_INVOICES
  );
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
    () => cartItems.value.length > 0 &&
      !savingPendingInvoice.value &&
      !maPhieuChuaApDung.value &&
      !pendingInvoiceLimitReached.value &&
      coThongTinGiaoHangHopLe.value
  );
  const canPay = computed(() => {
    if (!cartItems.value.length || payingInvoice.value || maPhieuChuaApDung.value || !coThongTinGiaoHangHopLe.value) {
      return false;
    }
    if (paymentMethod.value === 1) {
      return tienKhachThanhToan.value >= khachCanTra.value;
    }
    return true;
  });
  const productResults = computed(() => {
    const grouped = new Map();

    for (const product of productVariantResults.value) {
      const key = `${product.maSanPham}::${product.tenSanPham}`;
      const soLuongKhaDung = soLuongConLai(product.chiTietId, product.soLuongTon);

      if (!grouped.has(key)) {
        grouped.set(key, {
          ...product,
          soLuongTon: 0,
          tongBienThe: 0,
          coGiamGia: false
        });
      }

      const groupedProduct = grouped.get(key);
      groupedProduct.soLuongTon += soLuongKhaDung;
      groupedProduct.tongBienThe += 1;
      groupedProduct.coGiamGia = groupedProduct.coGiamGia || Number(product.giaBan || 0) < Number(product.giaGoc || 0);
      if (!groupedProduct.hinhAnh && product.hinhAnh) {
        groupedProduct.hinhAnh = product.hinhAnh;
      }
    }

    return Array.from(grouped.values());
  });
  const shippingInfo = computed(() => ({
    giaoHang: deliveryEnabled.value,
    tenNguoiNhan: deliveryRecipientName.value,
    soDienThoaiNguoiNhan: deliveryRecipientPhone.value,
    diaChiGiaoHang: deliveryAddress.value,
    donViVanChuyen: deliveryCarrier.value,
    phiVanChuyen: deliveryFee.value,
    diaChiDaDo: deliveryResolvedAddress.value,
    daTinhPhi: deliveryCalculated.value,
    dangTinhPhi: calculatingDeliveryFee.value,
    coTheTinhPhi: coTheTinhPhiVanChuyen.value,
    serviceTypeId: deliveryConfig.value.serviceTypeId,
    length: deliveryConfig.value.length,
    width: deliveryConfig.value.width,
    height: deliveryConfig.value.height,
    weight: deliveryConfig.value.weight
  }));

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

  function markShippingFeeDirty() {
    if (!deliveryEnabled.value) {
      return;
    }
    deliveryFee.value = 0;
    deliveryResolvedAddress.value = "";
    deliveryCalculated.value = false;
  }

  function taoDanhSachSanPhamThanhToan() {
    return cartItems.value.map((item) => ({
      chiTietId: item.chiTietId,
      soLuong: item.soLuong
    }));
  }

  function buildShippingPayload() {
    if (!deliveryEnabled.value) {
      return {
        giaoHang: false,
        tenNguoiNhan: null,
        soDienThoaiNguoiNhan: null,
        diaChiGiaoHang: null,
        phiVanChuyen: 0,
        donViVanChuyen: null
      };
    }

    return {
      giaoHang: true,
      tenNguoiNhan: tenNguoiNhanGiaoHangHienThi.value,
      soDienThoaiNguoiNhan: soDienThoaiNguoiNhanGiaoHangHienThi.value,
      diaChiGiaoHang: deliveryAddress.value.trim(),
      phiVanChuyen: deliveryFee.value,
      donViVanChuyen: deliveryCarrier.value || "GHN"
    };
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
    return productVariantResults.value.find((product) => product.chiTietId === chiTietId)?.soLuongTon ?? fallback;
  }

  const relatedVariants = computed(() => {
    if (!selectedProductDetail.value) {
      return [];
    }
    return productVariantResults.value.filter(
      (product) => product.maSanPham === selectedProductDetail.value?.maSanPham &&
        product.tenSanPham === selectedProductDetail.value?.tenSanPham
    );
  });
  const colorOptions = computed(() => {
    const grouped = new Map();
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
      (variant) => (
        selectedColor.value
          ? (variant.mauSac || variant.maBienThe) === selectedColor.value
          : true
      ) && (
        selectedSize.value
          ? (variant.kichCo || "") === selectedSize.value
          : true
      )
    ) || selectedProductDetail.value;
  });
  const chiTietDangChon = computed(() => selectedVariant.value || selectedProductDetail.value);
  const hinhAnhDangChon = computed(
    () => chiTietDangChon.value?.hinhAnh || selectedProductDetail.value?.hinhAnh || ""
  );
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
    productVariantResults.value = [];
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
    deliveryEnabled.value = false;
    deliveryRecipientName.value = "";
    deliveryRecipientPhone.value = "";
    deliveryAddress.value = "";
    deliveryCarrier.value = "GHN";
    deliveryFee.value = 0;
    deliveryResolvedAddress.value = "";
    deliveryCalculated.value = false;
    calculatingDeliveryFee.value = false;
    deliveryConfig.value = {
      serviceTypeId: 2,
      length: 30,
      width: 20,
      height: 12,
      weight: 500
    };
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
      pageError.value = error instanceof Error
        ? error.message
        : "Không thể tải danh sách hóa đơn chờ";
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
      pageError.value = error instanceof Error ? error.message : "Không thể tìm khách hàng";
    } finally {
      loadingCustomers.value = false;
    }
  }

  async function fetchProducts(keyword) {
    loadingProducts.value = true;
    try {
      productVariantResults.value = await timSanPhamTaiQuay(keyword);
    } catch (error) {
      pageError.value = error instanceof Error ? error.message : "Không thể tìm sản phẩm";
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
      pageError.value = error instanceof Error ? error.message : "Không thể tìm phiếu giảm giá";
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
    if (!deliveryRecipientName.value.trim()) {
      deliveryRecipientName.value = customer.hoTen || "";
    }
    if (!deliveryRecipientPhone.value.trim()) {
      deliveryRecipientPhone.value = customer.sdt || "";
    }
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
    if (!activePendingInvoice.value) {
      deliveryRecipientName.value = "";
      deliveryRecipientPhone.value = "";
    }
    customerResults.value = [];
    showCustomerDropdown.value = false;
    danhDauCanApDungLaiPhieu();
    clearFeedback();
  }

  function moChiTietSanPham(product) {
    if (!daChonKhach.value) {
      pageError.value = "Vui lòng chọn khách hàng hoặc Khách vãng lai trước khi thêm sản phẩm";
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
      pageError.value = "Vui lòng chọn khách hàng hoặc Khách vãng lai trước khi thêm sản phẩm";
      return;
    }
    const soLuongCoTheThem = soLuongConLai(product.chiTietId, product.soLuongTon);
    const existing = cartItems.value.find((item) => item.chiTietId === product.chiTietId);
    if (existing) {
      if (quantity > soLuongCoTheThem) {
        pageError.value = `Sản phẩm ${existing.tenSanPham} đã đạt giới hạn tồn kho`;
        return;
      }
      existing.soLuong += quantity;
    } else {
      if (quantity > soLuongCoTheThem) {
        pageError.value = `Sản phẩm ${product.tenSanPham} đã vượt giới hạn tồn kho`;
        return;
      }
      cartItems.value = [
        ...cartItems.value,
        {
          chiTietId: product.chiTietId,
          maSanPham: product.maSanPham,
          tenSanPham: product.tenSanPham,
          sku: product.sku,
          mauSac: product.mauSac,
          kichCo: product.kichCo,
          hinhAnh: product.hinhAnh || "",
          soLuong: quantity,
          giaBan: product.giaBan,
          soLuongTon: product.soLuongTon
        }
      ];
    }
    productKeyword.value = "";
    productVariantResults.value = [];
    selectedProductDetail.value = null;
    selectedColor.value = "";
    selectedSize.value = "";
    selectedQuantity.value = 1;
    showProductDropdown.value = false;
    danhDauCanApDungLaiPhieu();
    markShippingFeeDirty();
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
      pageError.value = "Vui lòng chọn màu sắc và kích cỡ phù hợp";
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
      pageError.value = `Sản phẩm ${reachedLimit} đã vượt giới hạn tồn kho`;
      return;
    }
    danhDauCanApDungLaiPhieu();
    markShippingFeeDirty();
    capNhatTienKhachThanhToan();
  }

  function giamSoLuong(chiTietId) {
    cartItems.value = cartItems.value
      .map((item) => item.chiTietId === chiTietId ? { ...item, soLuong: item.soLuong - 1 } : item)
      .filter((item) => item.soLuong > 0);
    danhDauCanApDungLaiPhieu();
    markShippingFeeDirty();
    capNhatTienKhachThanhToan();
  }

  function mapInvoiceToDraft(invoice) {
    const thongTinTheoChiTietId = new Map(
      productVariantResults.value.map((product) => [product.chiTietId, product])
    );
    const thongTinGiaoHang = invoice.thongTinGiaoHang || null;

    customerKeyword.value = invoice.tenKhachHang || invoice.soDienThoai || GUEST_LABEL;
    selectedCustomer.value = invoice.khachHangId
      ? {
        id: invoice.khachHangId,
        hoTen: invoice.tenKhachHang,
        sdt: invoice.soDienThoai,
        email: null
      }
      : null;
    cartItems.value = invoice.items.map((item) => {
      const thongTinSanPham = thongTinTheoChiTietId.get(item.chiTietId);
      return {
        chiTietId: item.chiTietId,
        maSanPham: item.maSanPham,
        tenSanPham: item.tenSanPham,
        sku: thongTinSanPham?.sku || "",
        mauSac: thongTinSanPham?.mauSac || "",
        kichCo: thongTinSanPham?.kichCo || "",
        hinhAnh: thongTinSanPham?.hinhAnh || "",
        soLuong: item.soLuong,
        giaBan: item.giaBan,
        soLuongTon: laySoLuongTonHienTai(item.chiTietId, item.soLuong)
      };
    });
    deliveryEnabled.value = Boolean(thongTinGiaoHang?.giaoHang);
    deliveryRecipientName.value = thongTinGiaoHang?.tenNguoiNhan || "";
    deliveryRecipientPhone.value = thongTinGiaoHang?.soDienThoaiNguoiNhan || "";
    deliveryAddress.value = thongTinGiaoHang?.diaChiGiaoHang || "";
    deliveryCarrier.value = thongTinGiaoHang?.donViVanChuyen || "GHN";
    deliveryFee.value = Number(thongTinGiaoHang?.phiVanChuyen || 0);
    deliveryResolvedAddress.value = "";
    deliveryCalculated.value = deliveryEnabled.value;
    deliveryConfig.value = {
      serviceTypeId: 2,
      length: 30,
      width: 20,
      height: 12,
      weight: 500
    };
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
        tongTienSauGiam: Math.max((invoice.tongTienHang || 0) - (invoice.tienGiam || 0), 0)
      }
      : null;
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
      pageError.value = error instanceof Error ? error.message : "Không thể tải hóa đơn chờ";
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

  function updateShippingInfo(patch) {
    const canTinhLai = [
      "diaChiGiaoHang",
      "serviceTypeId",
      "length",
      "width",
      "height",
      "weight"
    ].some((key) => Object.prototype.hasOwnProperty.call(patch, key));

    if (Object.prototype.hasOwnProperty.call(patch, "giaoHang")) {
      deliveryEnabled.value = Boolean(patch.giaoHang);
    }
    if (Object.prototype.hasOwnProperty.call(patch, "tenNguoiNhan")) {
      deliveryRecipientName.value = patch.tenNguoiNhan ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "soDienThoaiNguoiNhan")) {
      deliveryRecipientPhone.value = patch.soDienThoaiNguoiNhan ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "diaChiGiaoHang")) {
      deliveryAddress.value = patch.diaChiGiaoHang ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "serviceTypeId")) {
      deliveryConfig.value = {
        ...deliveryConfig.value,
        serviceTypeId: Number(patch.serviceTypeId) || 2
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "length")) {
      deliveryConfig.value = {
        ...deliveryConfig.value,
        length: Number(patch.length) || 30
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "width")) {
      deliveryConfig.value = {
        ...deliveryConfig.value,
        width: Number(patch.width) || 20
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "height")) {
      deliveryConfig.value = {
        ...deliveryConfig.value,
        height: Number(patch.height) || 12
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "weight")) {
      deliveryConfig.value = {
        ...deliveryConfig.value,
        weight: Number(patch.weight) || 500
      };
    }

    if (canTinhLai) {
      markShippingFeeDirty();
    }
    clearFeedback();
  }

  async function handleCalculateShippingFee() {
    if (!coTheTinhPhiVanChuyen.value) {
      return;
    }

    calculatingDeliveryFee.value = true;
    pageError.value = "";
    successMessage.value = "";
    try {
      const response = await tinhPhiVanChuyenTaiQuay({
        toAddress: deliveryAddress.value.trim(),
        serviceTypeId: Number(deliveryConfig.value.serviceTypeId) || 2,
        length: Number(deliveryConfig.value.length) || 30,
        width: Number(deliveryConfig.value.width) || 20,
        height: Number(deliveryConfig.value.height) || 12,
        weight: Number(deliveryConfig.value.weight) || 500,
        insuranceValue: Math.min(Math.round(tongTien.value || 0), 5000000),
        items: taoDanhSachSanPhamThanhToan()
      });
      deliveryFee.value = Number(response?.phiVanChuyen ?? response?.total ?? 0);
      deliveryResolvedAddress.value = [
        response?.matchedWardName,
        response?.matchedDistrictName,
        response?.matchedProvinceName
      ].filter(Boolean).join(", ");
      deliveryCalculated.value = true;
      successMessage.value = `Da tinh phi giao hang ${dinhDangTien(deliveryFee.value)}`;
    } catch (error) {
      deliveryFee.value = 0;
      deliveryResolvedAddress.value = "";
      deliveryCalculated.value = false;
      pageError.value = error instanceof Error ? error.message : "Khong the tinh phi giao hang";
    } finally {
      calculatingDeliveryFee.value = false;
    }
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
        thongTinGiaoHang: buildShippingPayload(),
        items: taoDanhSachSanPhamThanhToan()
      });
      successMessage.value = `Đã tạo hóa đơn chờ ${createdInvoice.ma}`;
      await fetchPendingInvoices();
      const matchedInvoice = pendingInvoices.value.find((invoice) => invoice.id === createdInvoice.id) ?? null;
      activePendingInvoice.value = matchedInvoice;
      mapInvoiceToDraft(createdInvoice);
    } catch (error) {
      pageError.value = error instanceof Error ? error.message : "Không thể tạo hóa đơn chờ";
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
        thongTinGiaoHang: buildShippingPayload(),
        hinhThucThanhToan: paymentMethod.value,
        tienKhachDua: paymentMethod.value === 1 ? tienKhachThanhToan.value : khachCanTra.value,
        ghiChu: paymentNote.value,
        items: taoDanhSachSanPhamThanhToan()
      });
      successMessage.value = `Đã thanh toán ${response.maHoaDon}`;
      await fetchPendingInvoices();
      resetDraft();
    } catch (error) {
      pageError.value = error instanceof Error ? error.message : "Không thể thanh toán trực tiếp";
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
      successMessage.value = `Đã hủy hóa đơn chờ ${activePendingInvoice.value.ma}`;
      await fetchPendingInvoices();
      resetDraft();
    } catch (error) {
      pageError.value = error instanceof Error ? error.message : "Không thể hủy hóa đơn chờ";
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
      pageError.value = "Vui lòng chọn khách hàng hoặc Khách vãng lai trước khi thêm sản phẩm";
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

  function clearTimers() {
    if (customerTimer) {
      window.clearTimeout(customerTimer);
    }
    if (productTimer) {
      window.clearTimeout(productTimer);
    }
    if (couponTimer) {
      window.clearTimeout(couponTimer);
    }
    if (couponDropdownTimer) {
      window.clearTimeout(couponDropdownTimer);
    }
  }

  onMounted(async () => {
    await fetchProducts("");
    await fetchPendingInvoices();
  });

  onBeforeUnmount(() => {
    clearTimers();
  });

  return {
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
    tienThua,
    paymentNote,
    canCreatePendingInvoice,
    savingPendingInvoice,
    canPay,
    payingInvoice,
    cancelingPendingInvoice,
    dinhDangTien,
    soLuongConLai,
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
    handlePayNow,
    handleCancelPendingInvoice
  };
}

export {
  useBanHangTaiQuay
};
