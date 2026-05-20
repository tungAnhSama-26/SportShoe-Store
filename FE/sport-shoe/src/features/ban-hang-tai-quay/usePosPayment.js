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
      return "Vui lòng nhập số tiền khách đưa.";
    }
    if (tienKhachThanhToan.value <= 0) {
      return "Số tiền khách đưa phải lớn hơn 0.";
    }
    if (tienKhachThanhToan.value < khachCanTra.value) {
      return "Số tiền khách đưa phải lớn hơn hoặc bằng khách cần trả.";
    }
    return "";
  });

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

  function validatePaymentInput() {
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

  watch([paymentMethod, khachCanTra], () => {
    capNhatTienKhachThanhToan();
  });

  return {
    paymentMethod,
    amountPaid,
    paymentNote,
    tienKhachThanhToan,
    tienThua,
    paymentValidationMessage,
    capNhatTienKhachThanhToan,
    validatePaymentInput,
    handleAmountPaidInput
  };
}
