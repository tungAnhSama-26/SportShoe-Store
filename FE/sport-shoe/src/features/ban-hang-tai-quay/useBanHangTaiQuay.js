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
import { showConfirm, showToastSuccess, showError, toastSwal } from "../../utils/alert";

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
    clearCouponTimers,
    suggestBestCoupon
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
    showToastSuccess(message);
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
      showToastSuccess(`Đã thêm ${selectedQuantity.value} sản phẩm vào hóa đơn`);
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
    isSavingInternal = true;
    cartItems.value = invoice.items.map((item) => {
      const thongTinSanPham = thongTinTheoChiTietId.get(item.chiTietId);
      return {
        cartItemId: Date.now().toString() + Math.random().toString(),
        chiTietId: item.chiTietId,
        maSanPham: item.maSanPham,
        tenSanPham: item.tenSanPham,
        sku: item.sku || thongTinSanPham?.sku || "",
        mauSac: item.mauSac || thongTinSanPham?.mauSac || "",
        kichCo: item.kichCo || thongTinSanPham?.kichCo || "",
        hinhAnh: item.hinhAnh || thongTinSanPham?.hinhAnh || "",
        soLuong: item.soLuong,
        soLuongBanDau: item.soLuong,
        giaBan: item.giaBan,
        soLuongTon: laySoLuongTonHienTai(item.chiTietId, item.soLuong)
      };
    });
    setTimeout(() => { isSavingInternal = false; }, 50);
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
      const response = await capNhatHoaDonCho(activePendingInvoice.value.id, payload);
      // Cập nhật lại soLuongBanDau vì backend đã trừ tồn kho
      isSavingInternal = true;
      cartItems.value = cartItems.value.map(item => ({
        ...item,
        soLuongBanDau: item.soLuong
      }));
      setTimeout(() => { isSavingInternal = false; }, 50);
    } catch (error) {
      console.error("Lỗi khi lưu hóa đơn chờ trước khi chuyển trang", error);
      throw error;
    }
  }
  
  let isSavingInternal = false;
  let autoSaveTimeout = null;
  watch(() => cartItems.value, () => {
    if (isSavingInternal) return;
    if (autoSaveTimeout) clearTimeout(autoSaveTimeout);
    autoSaveTimeout = setTimeout(() => {
      saveCurrentInvoice().catch(() => {});
    }, 1000);
  }, { deep: true });

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
    if (cancelingPendingInvoice.value) {
      return;
    }
    
    if (!activePendingInvoice.value) {
      toastSwal.fire({
        icon: 'warning',
        title: 'Thông báo',
        text: 'Vui lòng chọn hóa đơn cần hủy',
        timer: 3000,
        iconColor: '#cf1018'
      });
      return;
    }

    const isConfirmed = await showConfirm(`Bạn có chắc chắn muốn hủy hóa đơn ${activePendingInvoice.value.ma} không?`);
    if (!isConfirmed) {
      return;
    }

    cancelingPendingInvoice.value = true;
    pageError.value = "";
    try {
      await huyHoaDonCho(activePendingInvoice.value.id);
      
      toastSwal.fire({
        icon: 'success',
        title: 'Thành công!',
        text: `Đã hủy hóa đơn chờ ${activePendingInvoice.value.ma}`,
        timer: 3000,
        iconColor: '#cf1018'
      });

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

  function handlePrintStoreInvoice() {
    if (!activePendingInvoice.value) return;

    successMessage.value = `Đang tạo PDF hóa đơn ${activePendingInvoice.value.ma}...`;

    const rowsHtml = cartItems.value.map((item, index) => `
      <tr>
        <td>${index + 1}</td>
        <td>
          <strong>${item.tenSanPham}</strong>
          <span>${item.mauSac || "-"} / ${item.kichCo || "-"}</span>
        </td>
        <td class="cell-center">${item.soLuong}</td>
        <td class="cell-money">${item.giaBan.toLocaleString("vi-VN")} đ</td>
        <td class="cell-money">${(item.soLuong * item.giaBan).toLocaleString("vi-VN")} đ</td>
      </tr>
    `).join("");

    const deliveryFeeRow = deliveryFee.value > 0 ? `
      <div class="money-row">
        <span>Phí giao hàng</span>
        <strong>+ ${deliveryFee.value.toLocaleString("vi-VN")} đ</strong>
      </div>
    ` : "";

    const discountRow = tienGiam.value > 0 ? `
      <div class="money-row discount">
        <span>Giảm giá</span>
        <strong>- ${tienGiam.value.toLocaleString("vi-VN")} đ</strong>
      </div>
    ` : "";

    const invoiceHtml = `
      <div class="pos-invoice">
        <style>
          .pos-invoice {
            width: 148mm;
            margin: 0 auto;
            overflow: hidden;
            border: 1px solid #fecaca;
            border-radius: 6px;
            background: #ffffff;
            color: #0f172a;
            font-family: "Inter", "Segoe UI", Arial, sans-serif;
            font-size: 12px;
            line-height: 1.45;
          }

          .pos-header {
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 16px;
            background: #c52220;
            color: #ffffff;
            padding: 14px 16px;
          }

          .pos-brand {
            margin: 0;
            font-size: 18px;
            font-weight: 900;
          }

          .pos-subtitle,
          .pos-code-label {
            margin: 4px 0 0;
            color: #fee2e2;
            font-size: 10px;
          }

          .pos-code {
            text-align: right;
          }

          .pos-code strong {
            display: block;
            margin-top: 4px;
            font-size: 14px;
          }

          .pos-title {
            display: flex;
            justify-content: space-between;
            gap: 14px;
            padding: 14px 16px;
            border-bottom: 1px solid #fecaca;
            background: #fff7f7;
          }

          .pos-title h1 {
            margin: 0;
            color: #991b1b;
            font-size: 24px;
            line-height: 1.1;
          }

          .pos-title p {
            margin: 7px 0 0;
            color: #64748b;
            font-size: 11px;
          }

          .pos-badge {
            align-self: flex-start;
            border: 1px solid #fecaca;
            border-radius: 999px;
            color: #c52220;
            padding: 6px 11px;
            font-weight: 700;
            white-space: nowrap;
          }

          .pos-section {
            padding: 14px 16px;
            border-bottom: 1px solid #e2e8f0;
          }

          .pos-section h2 {
            margin: 0 0 10px;
            font-size: 14px;
            font-weight: 900;
          }

          .pos-info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 8px 18px;
          }

          .pos-info {
            display: grid;
            grid-template-columns: 92px minmax(0, 1fr);
            gap: 8px;
          }

          .pos-info span {
            color: #64748b;
            font-weight: 700;
          }

          .pos-info strong {
            font-weight: 700;
            word-break: break-word;
          }

          .pos-table {
            width: 100%;
            overflow: hidden;
            border: 1px solid #dbe3ef;
            border-radius: 6px;
            border-spacing: 0;
          }

          .pos-table th,
          .pos-table td {
            padding: 9px 10px;
            border-bottom: 1px solid #e2e8f0;
            text-align: left;
            vertical-align: top;
          }

          .pos-table th {
            background: #fff1f2;
            color: #991b1b;
            font-size: 10px;
            font-weight: 900;
            text-transform: uppercase;
          }

          .pos-table tr:last-child td {
            border-bottom: 0;
          }

          .pos-table strong {
            display: block;
          }

          .pos-table span {
            display: block;
            margin-top: 3px;
            color: #64748b;
            font-size: 10px;
          }

          .cell-center {
            text-align: center !important;
          }

          .cell-money {
            text-align: right !important;
            white-space: nowrap;
          }

          .money-row {
            display: flex;
            justify-content: space-between;
            gap: 16px;
            padding: 6px 0;
            color: #64748b;
          }

          .money-row strong {
            color: #0f172a;
            white-space: nowrap;
          }

          .money-row.discount strong {
            color: #059669;
          }

          .money-total {
            margin-top: 8px;
            padding-top: 12px;
            border-top: 1px solid #fecaca;
            color: #c52220;
            font-size: 16px;
            font-weight: 900;
          }

          .money-total strong {
            color: #c52220;
            font-size: 18px;
          }

          .pos-thanks {
            padding: 14px 16px 16px;
            color: #64748b;
          }

          .pos-thanks strong {
            display: block;
            margin-bottom: 5px;
            color: #0f172a;
            font-size: 14px;
          }
        </style>

        <header class="pos-header">
          <div>
            <p class="pos-brand">SportShoe</p>
            <p class="pos-subtitle">Giày thể thao chính hãng</p>
          </div>
          <div class="pos-code">
            <span class="pos-code-label">Mã hóa đơn</span>
            <strong>${activePendingInvoice.value.ma}</strong>
          </div>
        </header>

        <section class="pos-title">
          <div>
            <h1>Hóa đơn bán hàng</h1>
            <p>Ngày tạo: ${new Date().toLocaleString("vi-VN")} · Loại đơn: Cửa hàng</p>
          </div>
          <span class="pos-badge">Tại quầy</span>
        </section>

        <section class="pos-section">
          <h2>Thông tin hóa đơn</h2>
          <div class="pos-info-grid">
            <div class="pos-info">
              <span>Nhân viên</span>
              <strong>${activePendingInvoice.value.maNhanVien || "Chưa gán"}</strong>
            </div>
            <div class="pos-info">
              <span>Khách hàng</span>
              <strong>${tenKhachHangHienThi.value}</strong>
            </div>
            <div class="pos-info">
              <span>Số điện thoại</span>
              <strong>${soDienThoaiKhachHangHienThi.value || "Không có"}</strong>
            </div>
            <div class="pos-info">
              <span>Địa chỉ</span>
              <strong>${deliveryAddress.value || "Mua tại quầy"}</strong>
            </div>
          </div>
        </section>

        <section class="pos-section">
          <h2>Danh sách sản phẩm</h2>
          <table class="pos-table">
            <colgroup>
              <col style="width: 42px" />
              <col />
              <col style="width: 78px" />
              <col style="width: 108px" />
              <col style="width: 118px" />
            </colgroup>
            <thead>
              <tr>
                <th>STT</th>
                <th>Sản phẩm</th>
                <th>Số lượng</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              ${rowsHtml || '<tr><td colspan="5" class="cell-center">Không có sản phẩm</td></tr>'}
            </tbody>
          </table>
        </section>

        <section class="pos-section">
          <h2>Tổng kết thanh toán</h2>
          <div class="money-row">
            <span>Tổng tiền hàng</span>
            <strong>${tongTien.value.toLocaleString("vi-VN")} đ</strong>
          </div>
          ${deliveryFeeRow}
          ${discountRow}
          <div class="money-row money-total">
            <span>Khách cần trả</span>
            <strong>${khachCanTra.value.toLocaleString("vi-VN")} đ</strong>
          </div>
        </section>

        <footer class="pos-thanks">
          <strong>Cảm ơn quý khách!</strong>
          Hóa đơn được phát hành bởi SportShoe. Vui lòng kiểm tra sản phẩm và tổng thanh toán trước khi rời quầy.
        </footer>
      </div>
    `;

    const opt = {
      margin: 10,
      filename: `HoaDon_${activePendingInvoice.value.ma}.pdf`,
      image: { type: "jpeg", quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true },
      jsPDF: { unit: "mm", format: "a5", orientation: "portrait" }
    };

    html2pdf().set(opt).from(invoiceHtml).save().then(() => {
      successMessage.value = `Đã tải PDF hóa đơn ${activePendingInvoice.value.ma}.`;
      setTimeout(() => { successMessage.value = ""; }, 3000);
    }).catch((err) => {
      pageError.value = `Có lỗi xảy ra khi in PDF: ${err.message}`;
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
    suggestBestCoupon,
    updateShippingInfo,
    handleCalculateShippingFee,
    handleAmountPaidInput,
    handleCreatePendingInvoice,
    handleCreateEmptyInvoice,
    handlePayNow,
    handleCancelPendingInvoice,
    handlePrintInvoice: handlePrintStoreInvoice
  };
}

export {
  useBanHangTaiQuay
};
