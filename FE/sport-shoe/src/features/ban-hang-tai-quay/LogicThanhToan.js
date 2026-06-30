import { computed, ref, watch } from "vue";
import { showError } from "../../utils/alert";
import { dinhDangSo, dinhDangTienNhap, layChuSoTien } from "./TienTe";
import { validateThanhToan } from "./ValidateThanhToan";
import { PHUONG_THUC_THANH_TOAN } from "./Enum";

export function LogicThanhToan({ cartItems, khachCanTra, hoaDonChoDaChon }) {
  const phuongThucThanhToan = ref(PHUONG_THUC_THANH_TOAN.TIEN_MAT);
  const tienKhachDua = ref("");
  const ghiChuThanhToan = ref("");

  watch(() => hoaDonChoDaChon.value?.id, (newId, oldId) => {
    if (newId !== oldId) {
      phuongThucThanhToan.value = PHUONG_THUC_THANH_TOAN.TIEN_MAT;
      tienKhachDua.value = "";
      ghiChuThanhToan.value = "";
    }
  });

  const tienKhachThanhToan = computed(() => {
    const parsed = Number(layChuSoTien(tienKhachDua.value));
    return Number.isFinite(parsed) ? parsed : 0;
  });
  const tienThua = computed(() => {
    if (phuongThucThanhToan.value !== PHUONG_THUC_THANH_TOAN.TIEN_MAT) {
      return 0;
    }
    return Math.max(tienKhachThanhToan.value - khachCanTra.value, 0);
  });
  const thongBaoLoiThanhToan = computed(() => {
    if (phuongThucThanhToan.value !== PHUONG_THUC_THANH_TOAN.TIEN_MAT || !cartItems.value.length || khachCanTra.value <= 0) {
      return "";
    }
    if (!tienKhachDua.value.trim()) {
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
      tienKhachDua.value = "";
      return;
    }
    if (phuongThucThanhToan.value !== PHUONG_THUC_THANH_TOAN.TIEN_MAT) {
      tienKhachDua.value = dinhDangSo(khachCanTra.value);
      return;
    }
    // Nếu là tiền mặt:
    // 1. Nếu force (tải lại hóa đơn) hoặc vừa chuyển từ phương thức khác sang tiền mặt -> Xóa trắng để nhập lại
    if (force || isPaymentMethodChange) {
      tienKhachDua.value = "";
    }
    // 2. Ngược lại (chỉ đổi tổng tiền), giữ nguyên số tiền nhân viên ĐÃ nhập (không tự động điền)
  }

  function kiemTraLoiThanhToan() {
    return validateThanhToan(phuongThucThanhToan.value, tienKhachDua.value, thongBaoLoiThanhToan.value);
  }

  function dinhDangTienKhachDua() {
    if (phuongThucThanhToan.value !== PHUONG_THUC_THANH_TOAN.TIEN_MAT) {
      tienKhachDua.value = dinhDangSo(khachCanTra.value);
      return;
    }
    tienKhachDua.value = dinhDangTienNhap(tienKhachDua.value);
  }

  function xuLyTienKhachDuaInput(value) {
    tienKhachDua.value = value;
    dinhDangTienKhachDua();
  }

  watch(khachCanTra, () => {
    capNhatTienKhachThanhToan(false, false);
  });

  watch(phuongThucThanhToan, () => {
    capNhatTienKhachThanhToan(true, false);
  });

  return {
    phuongThucThanhToan,
    tienKhachDua,
    ghiChuThanhToan,
    tienKhachThanhToan,
    tienThua,
    thongBaoLoiThanhToan,
    capNhatTienKhachThanhToan: (force = false) => capNhatTienKhachThanhToan(false, force),
    kiemTraLoiThanhToan,
    xuLyTienKhachDuaInput
  };
}
