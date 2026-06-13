import { computed, ref, watch } from "vue";
import { dinhDangSo, dinhDangTienNhap, layChuSoTien } from "./money";

export function usePosPayment({ cartItems, khachCanTra, pageError }) {
  const paymentMethod = ref(1);
  const amountPaid = ref("");
  const paymentNote = ref("");

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
  const paymentValidationMessage = computed(() => {
    if (paymentMethod.value !== 1 || !cartItems.value.length || khachCanTra.value <= 0) {
      return "";
    }
    if (!amountPaid.value.trim()) {
      return "";
    }
    if (tienKhachThanhToan.value <= 0) {
      return "Số tiền khách đưa phải lớn hơn 0.";
    }
    if (tienKhachThanhToan.value < khachCanTra.value) {
      return "Số tiền khách đưa phải lớn hơn hoặc bằng khách cần trả.";
    }
    return "";
  });

  function capNhatTienKhachThanhToan(isPaymentMethodChange = false, force = false) {
    if (!cartItems.value.length) {
      amountPaid.value = "";
      return;
    }
    if (paymentMethod.value !== 1) {
      amountPaid.value = dinhDangSo(khachCanTra.value);
      return;
    }
    // Nếu là tiền mặt:
    // 1. Nếu force (tải lại hóa đơn) hoặc vừa chuyển từ phương thức khác sang tiền mặt -> Xóa trắng để nhập lại
    if (force || isPaymentMethodChange) {
      amountPaid.value = "";
    }
    // 2. Ngược lại (chỉ đổi tổng tiền), giữ nguyên số tiền nhân viên ĐÃ nhập (không tự động điền)
  }

  function validatePaymentInput() {
    if (paymentMethod.value === 1 && !amountPaid.value.trim()) {
      pageError.value = "Vui lòng nhập số tiền khách đưa.";
      return false;
    }
    if (paymentValidationMessage.value) {
      pageError.value = paymentValidationMessage.value;
      return false;
    }
    return true;
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

  watch(khachCanTra, () => {
    capNhatTienKhachThanhToan(false, false);
  });

  watch(paymentMethod, () => {
    capNhatTienKhachThanhToan(true, false);
  });

  return {
    paymentMethod,
    amountPaid,
    paymentNote,
    tienKhachThanhToan,
    tienThua,
    paymentValidationMessage,
    capNhatTienKhachThanhToan: (force = false) => capNhatTienKhachThanhToan(false, force),
    validatePaymentInput,
    handleAmountPaidInput
  };
}
