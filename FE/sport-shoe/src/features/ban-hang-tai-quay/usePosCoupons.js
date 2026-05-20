import { computed, ref, watch } from "vue";
import {
  apDungPhieuGiamGiaTaiQuay,
  timPhieuGiamGiaTaiQuay
} from "../../services/ban-hang-tai-quay";

export function usePosCoupons({
  cartItems,
  tongTien,
  activePendingInvoice,
  selectedCustomer,
  layKhachHangIdHienTai,
  taoDanhSachSanPhamThanhToan,
  capNhatTienKhachThanhToan,
  pageError,
  successMessage,
  clearFeedback
}) {
  const couponCode = ref("");
  const appliedCoupon = ref(null);
  const applyingCoupon = ref(false);
  const couponResults = ref([]);
  const loadingCoupons = ref(false);
  const showCouponDropdown = ref(false);

  let couponTimer;
  let couponDropdownTimer;

  const tienGiam = computed(() => appliedCoupon.value?.soTienGiam ?? 0);
  const tongTienSauGiamHienThi = computed(() => Math.max(tongTien.value - tienGiam.value, 0));
  const maPhieuChuaApDung = computed(
    () => Boolean(couponCode.value.trim()) && !appliedCoupon.value
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

  function danhDauCanApDungLaiPhieu() {
    if (!couponCode.value.trim()) {
      appliedCoupon.value = null;
      return;
    }
    appliedCoupon.value = null;
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

  function clearCouponTimers() {
    if (couponTimer) {
      window.clearTimeout(couponTimer);
    }
    if (couponDropdownTimer) {
      window.clearTimeout(couponDropdownTimer);
    }
  }

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

  return {
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
    fetchCoupons,
    handleCouponFocus,
    handleCouponBlur,
    chonPhieuGiamGia,
    handleApplyCoupon,
    handleRemoveCoupon,
    clearCouponTimers
  };
}
