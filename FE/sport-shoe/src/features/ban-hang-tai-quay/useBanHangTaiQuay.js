import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import html2pdf from "html2pdf.js";
import {
  huyHoaDonCho,
  layChiTietHoaDonCho,
  layDanhSachHoaDonCho,
  thanhToanTaiQuay,
  taoHoaDonCho,
  capNhatHoaDonCho
} from "../../services/ban-hang-tai-quay";
import {
  GUEST_LABEL,
  MAX_PENDING_INVOICES,
} from "./constants";
import { dinhDangTien } from "./money";
import { usePosCart } from "./usePosCart";
import { usePosCoupons } from "./usePosCoupons";
import { usePosCustomers } from "./usePosCustomers";
import { usePosPayment } from "./usePosPayment";
import { usePosProducts } from "./usePosProducts";
import { usePosShipping } from "./usePosShipping";
import { showConfirm, showSuccess, showError } from "../../utils/alert";

function useBanHangTaiQuay() {
  const pendingInvoices = ref([]);
  const activePendingInvoice = ref(null);
  const loadingPendingInvoices = ref(false);
  const savingPendingInvoice = ref(false);
  const cancelingPendingInvoice = ref(false);
  const payingInvoice = ref(false);
  const invoiceLoading = ref(false);
  const pageError = ref("");
  const successMessage = ref("");
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

  const pendingInvoiceLimitReached = computed(
    () => pendingInvoices.value.length >= MAX_PENDING_INVOICES
  );
  const canCreatePendingInvoice = computed(
    () => !savingPendingInvoice.value &&
      !maPhieuChuaApDung.value &&
      !pendingInvoiceLimitReached.value &&
      coThongTinGiaoHangHopLe.value &&
      !sanPhamValidationMessage.value
  );
  const canPay = computed(() => {
    if (!cartItems?.value?.length || sanPhamValidationMessage?.value || payingInvoice?.value || maPhieuChuaApDung?.value || !coThongTinGiaoHangHopLe?.value || !daChonKhach?.value) {
      return false;
    }
    if (paymentMethod?.value === 1) {
      return !paymentValidationMessage?.value;
    }
    return true;
  });
  const {
    customerKeyword,
    customerResults,
    selectedCustomer,
    loadingCustomers,
    showCustomerDropdown,
    isGuestCustomer,
    tenKhachHangHienThi,
    soDienThoaiKhachHangHienThi,
    fetchCustomers,
    chonKhachHang,
    boChonKhachHang,
    chonKhachVangLai,
    moDanhSachKhachHang,
    dongDanhSachKhachHang,
    clearCustomerTimer
  } = usePosCustomers({
    activePendingInvoice,
    deliveryRecipientName,
    deliveryRecipientPhone,
    deliveryAddress,
    danhDauCanApDungLaiPhieu: markCouponDirty,
    clearFeedback,
    pageError
  });

  const daChonKhach = computed(() => {
    if (selectedCustomer.value) return true;
    if (isGuestCustomer.value) return true;
    if (activePendingInvoice.value) {
      if (activePendingInvoice.value.khachHangId) return true;
      if (activePendingInvoice.value.tenKhachHang === GUEST_LABEL) return true;
    }
    return false;
  });

  const {
    cartItems,
    tongSoLuong,
    tongTien,
    sanPhamValidationMessage,
    validateCartItems,
    taoDanhSachSanPhamThanhToan,
    soLuongConLai,
    themSanPham,
    tangSoLuong,
    giamSoLuong
  } = usePosCart({
    daChonKhach,
    markShippingFeeDirty: syncShippingDirty,
    capNhatTienKhachThanhToan: syncPaymentAmount,
    danhDauCanApDungLaiPhieu: markCouponDirty,
    syncProductAfterCartAdd,
    pageError,
    clearFeedback
  });

  const {
    tenNguoiNhanGiaoHangHienThi,
    soDienThoaiNguoiNhanGiaoHangHienThi,
    phiVanChuyenHienThi,
    coTheTinhPhiVanChuyen,
    coThongTinGiaoHangHopLe,
    shippingInfo,
    markShippingFeeDirty,
    buildShippingPayload,
    updateShippingInfo,
    handleCalculateShippingFee
  } = usePosShipping({
    deliveryEnabled,
    deliveryRecipientName,
    deliveryRecipientPhone,
    deliveryAddress,
    deliveryCarrier,
    deliveryFee,
    deliveryResolvedAddress,
    deliveryCalculated,
    calculatingDeliveryFee,
    deliveryConfig,
    selectedCustomer,
    activePendingInvoice,
    cartItems,
    pageError
  });

  const {
    couponCode,
    appliedCoupon,
    applyingCoupon,
    couponResults,
    loadingCoupons,
    showCouponDropdown,
    tienGiam,
    tongTienSauGiamHienThi,
    maPhieuChuaApDung,
    coTheTimPhieu,
    coTheApDungPhieu,
    danhDauCanApDungLaiPhieu,
    handleCouponFocus,
    handleCouponBlur,
    chonPhieuGiamGia,
    handleApplyCoupon,
    handleRemoveCoupon,
    clearCouponTimers
  } = usePosCoupons({
    cartItems,
    tongTien,
    activePendingInvoice,
    selectedCustomer,
    layKhachHangIdHienTai,
    taoDanhSachSanPhamThanhToan,
    capNhatTienKhachThanhToan: syncPaymentAmount,
    pageError,
    successMessage,
    clearFeedback
  });

  const khachCanTra = computed(() => tongTienSauGiamHienThi.value + phiVanChuyenHienThi.value);

  const {
    paymentMethod,
    amountPaid,
    paymentNote,
    tienKhachThanhToan,
    tienThua,
    paymentValidationMessage,
    capNhatTienKhachThanhToan,
    validatePaymentInput,
    handleAmountPaidInput
  } = usePosPayment({
    cartItems,
    khachCanTra,
    pageError,
    activePendingInvoice
  });

  const {
    productKeyword,
    productVariantResults,
    selectedProductDetail,
    selectedColor,
    selectedSize,
    selectedQuantity,
    loadingProducts,
    showProductDropdown,
    productSearchLabel,
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
    colorOptions,
    sizeOptions,
    selectedVariant,
    chiTietDangChon,
    hinhAnhDangChon,
    soLuongTonKhaDungChiTiet,
    soLuongTonSauKhiChon,
    fetchProducts,
    laySoLuongTonHienTai,
    moChiTietSanPham,
    dongChiTietSanPham,
    handleProductQrScan,
    chonMauSac,
    chonKichCo,
    giamSoLuongChiTiet,
    tangSoLuongChiTiet,
    moDanhSachSanPham,
    dongDanhSachSanPham,
    clearProductTimer
  } = usePosProducts({
    daChonKhach,
    soLuongConLai,
    themSanPham,
    clearFeedback,
    pageError,
    successMessage
  });

  function clearFeedback() {
    pageError.value = "";
    successMessage.value = "";
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

  function syncShippingDirty() {
    markShippingFeeDirty();
  }



  function markCouponDirty() {
    danhDauCanApDungLaiPhieu();
  }

  function syncPaymentAmount(force = false) {
    capNhatTienKhachThanhToan(force);
  }

  function syncProductAfterCartAdd({
    preserveProductSearch = false,
    scannedKeyword = "",
    scannedProducts = [],
  } = {}) {
    if (preserveProductSearch) {
      productKeyword.value = scannedKeyword;
      productVariantResults.value = scannedProducts;
    }
    selectedProductDetail.value = null;
    selectedColor.value = "";
    selectedSize.value = "";
    selectedQuantity.value = 1;
    showProductDropdown.value = false;
  }

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

  watch(pageError, (message) => {
    if (!message) {
      return;
    }
    showError(message);
    pageError.value = "";
  });

  watch(successMessage, (message) => {
    if (!message) {
      return;
    }
    showSuccess(message);
    successMessage.value = "";
  });

  function themBienTheDangChon() {
    if (!selectedVariant.value) {
      pageError.value = "Vui lòng chọn màu sắc và kích cỡ phù hợp";
      return;
    }
    const success = themSanPham(selectedVariant.value, selectedQuantity.value);
    if (success) {
      dongChiTietSanPham();
      showSuccess(`Đã thêm ${selectedQuantity.value} sản phẩm vào hóa đơn`);
    }
  }

  function mapInvoiceToDraft(invoice) {
    const thongTinTheoChiTietId = new Map(
      productVariantResults.value.map((product) => [product.chiTietId, product])
    );
    const thongTinGiaoHang = invoice.thongTinGiaoHang || null;

    customerKeyword.value = invoice.tenKhachHang || invoice.soDienThoai || "";
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

  async function saveCurrentInvoice() {
    if (!activePendingInvoice.value) return;
    try {
      const payload = {
        tenKhachHang: selectedCustomer.value?.hoTen || deliveryRecipientName.value || (isGuestCustomer.value ? GUEST_LABEL : ""),
        soDienThoai: selectedCustomer.value?.sdt || deliveryRecipientPhone.value || "",
        ghiChu: "",
        khachHangId: selectedCustomer.value?.id || null,
        maPhieuGiamGia: appliedCoupon.value?.ma || null,
        thongTinGiaoHang: deliveryEnabled.value ? buildShippingPayload() : null,
        items: cartItems.value.map(item => ({
          chiTietId: item.chiTietId,
          soLuong: item.soLuong
        })),
      };
      await capNhatHoaDonCho(activePendingInvoice.value.id, payload);
    } catch (error) {
      console.error("Lỗi khi lưu hóa đơn chờ trước khi chuyển trang", error);
      throw error;
    }
  }

  async function chonHoaDonCho(invoice) {
    if (activePendingInvoice.value && activePendingInvoice.value.id !== invoice.id) {
      try {
        await saveCurrentInvoice();
      } catch (e) {
        // ignore error when switching tabs
      }
    }

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

  async function handleCreatePendingInvoice() {
    if (!validateCartItems(false)) {
      return;
    }
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
        tenKhachHang: selectedCustomer.value?.hoTen || (isGuestCustomer.value ? GUEST_LABEL : ""),
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

  async function handlePayNow() {
    if (!daChonKhach.value) {
      pageError.value = "Vui lòng chọn khách hàng hoặc Khách vãng lai trước khi thanh toán.";
      return;
    }
    if (!validateCartItems(true) || !validatePaymentInput()) {
      return;
    }
    if (!canPay.value) {
      return;
    }

    const isConfirmed = await showConfirm('Bạn có chắc chắn muốn thanh toán đơn hàng này không?');
    if (!isConfirmed) {
      return;
    }

    payingInvoice.value = true;
    pageError.value = "";
    successMessage.value = "";
    try {
      if (activePendingInvoice.value) {
        await saveCurrentInvoice();
      }

      const response = await thanhToanTaiQuay({
        hoaDonId: activePendingInvoice.value?.id ?? null,
        khachHangId: layKhachHangIdHienTai(),
        tenKhachHang: selectedCustomer.value?.hoTen || (isGuestCustomer.value ? GUEST_LABEL : ""),
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

    const isConfirmed = await showConfirm(`Bạn có chắc chắn muốn hủy hóa đơn ${activePendingInvoice.value.ma} không?`);
    if (!isConfirmed) {
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

  async function handleCreateEmptyInvoice() {
    if (pendingInvoiceLimitReached.value) {
      pageError.value = `Chỉ được tạo tối đa ${MAX_PENDING_INVOICES} hóa đơn chờ.`;
      return;
    }
    savingPendingInvoice.value = true;
    pageError.value = "";
    successMessage.value = "";
    try {
      resetDraft();
      const createdInvoice = await taoHoaDonCho({
        khachHangId: null,
        tenKhachHang: "",
        soDienThoai: "",
        maPhieuGiamGia: null,
        thongTinGiaoHang: {
          giaoHang: false,
          tenNguoiNhan: null,
          soDienThoaiNguoiNhan: null,
          diaChiGiaoHang: null,
          phiVanChuyen: 0,
          donViVanChuyen: null
        },
        items: []
      });
      successMessage.value = `Đã tạo hóa đơn chờ ${createdInvoice.ma}`;
      await fetchPendingInvoices();
      const matchedInvoice = pendingInvoices.value.find((invoice) => invoice.id === createdInvoice.id) ?? null;
      activePendingInvoice.value = matchedInvoice;
    } catch (error) {
      pageError.value = error instanceof Error ? error.message : "Không thể tạo hóa đơn chờ";
    } finally {
      savingPendingInvoice.value = false;
    }
  }

  function clearTimers() {
    clearCustomerTimer();
    clearProductTimer();
    clearCouponTimers();
  }

  function handlePrintInvoice() {
    if (!activePendingInvoice.value) return;
    
    successMessage.value = `Đang tạo PDF hóa đơn ${activePendingInvoice.value.ma}...`;

    const rowsHtml = cartItems.value.map(item => `
      <tr style="border-bottom: 1px dashed #eee;">
        <td style="padding: 8px 0;">
          <div style="font-weight: bold;">${item.tenSanPham}</div>
          <div style="font-size: 12px; color: #666;">Mã: ${item.maSanPham} | Màu: ${item.mauSac} | Size: ${item.kichCo}</div>
        </td>
        <td style="text-align: center; padding: 8px 0;">${item.soLuong}</td>
        <td style="text-align: right; padding: 8px 0;">${item.giaBan.toLocaleString('vi-VN')} đ</td>
        <td style="text-align: right; padding: 8px 0;">${(item.soLuong * item.giaBan).toLocaleString('vi-VN')} đ</td>
      </tr>
    `).join('');
    const invoiceHtml = `
      <div style="font-family: Arial, sans-serif; padding: 20px; color: #333; max-width: 800px; margin: auto; line-height: 1.5;">
        <div style="text-align: center; border-bottom: 2px dashed #ccc; padding-bottom: 10px; margin-bottom: 20px;">
          <h2 style="margin: 0; font-size: 24px; color: #d32f2f;">SPORT SHOE STORE</h2>
          <p style="margin: 5px 0 0; font-size: 14px;">Địa chỉ: 123 Đường Bán Giày, Hà Nội</p>
          <p style="margin: 0; font-size: 14px;">Điện thoại: 0123.456.789</p>
        </div>

        <div style="text-align: center; margin-bottom: 20px;">
          <h3 style="margin: 0; font-size: 20px;">HÓA ĐƠN BÁN HÀNG</h3>
          <p style="margin: 5px 0 0; font-size: 14px;">Mã HĐ: <strong>${activePendingInvoice.value.ma}</strong></p>
          <p style="margin: 5px 0 0; font-size: 14px;">Ngày: ${new Date().toLocaleString('vi-VN')}</p>
        </div>

        <div style="margin-bottom: 20px; font-size: 14px;">
          <p style="margin: 5px 0;"><strong>Khách hàng:</strong> ${tenKhachHangHienThi.value}</p>
          <p style="margin: 5px 0;"><strong>SĐT:</strong> ${soDienThoaiKhachHangHienThi.value}</p>
        </div>

        <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 14px;">
          <thead>
            <tr style="border-bottom: 1px solid #ccc;">
              <th style="text-align: left; padding: 8px 0;">Sản phẩm</th>
              <th style="text-align: center; padding: 8px 0;">SL</th>
              <th style="text-align: right; padding: 8px 0;">Đơn giá</th>
              <th style="text-align: right; padding: 8px 0;">Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            ${rowsHtml}
          </tbody>
        </table>

        <div style="border-top: 1px solid #ccc; padding-top: 10px; font-size: 14px;">
          <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
            <span>Tổng tiền hàng:</span>
            <span>${tongTien.value.toLocaleString('vi-VN')} đ</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
            <span>Phí giao hàng:</span>
            <span>${deliveryFee.value.toLocaleString('vi-VN')} đ</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
            <span>Giảm giá:</span>
            <span>-${tienGiam.value.toLocaleString('vi-VN')} đ</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-top: 10px; font-size: 16px; font-weight: bold;">
            <span>Khách cần trả:</span>
            <span>${khachCanTra.value.toLocaleString('vi-VN')} đ</span>
          </div>
        </div>

        <div style="text-align: center; margin-top: 30px; font-size: 14px; font-style: italic;">
          <p>Cảm ơn quý khách đã mua hàng!</p>
          <p>Hẹn gặp lại!</p>
        </div>
      </div>
    `;

    const opt = {
      margin:       10,
      filename:     `HoaDon_${activePendingInvoice.value.ma}.pdf`,
      image:        { type: 'jpeg', quality: 0.98 },
      html2canvas:  { scale: 2, useCORS: true },
      jsPDF:        { unit: 'mm', format: 'a5', orientation: 'portrait' }
    };

    html2pdf().set(opt).from(invoiceHtml).save().then(() => {
       successMessage.value = `Đã tải PDF hóa đơn ${activePendingInvoice.value.ma}.`;
       setTimeout(() => { successMessage.value = ""; }, 3000);
    }).catch(err => {
       pageError.value = "Có lỗi xảy ra khi in PDF: " + err.message;
    });
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
    handleProductQrScan,
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
    handlePrintInvoice
  };
}

export {
  useBanHangTaiQuay
};
