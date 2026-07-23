import { computed, ref, watch } from "vue";
import { showError } from "../../utils/alert";
import { dinhDangSo, dinhDangTienNhap, layChuSoTien } from "./TienTe";
import { validateThanhToan } from "./ValidateThanhToan";
import { PHUONG_THUC_THANH_TOAN } from "./Enum";

export function LogicThanhToan({ cartItems, khachCanTra, hoaDonChoDaChon }) {
  const phuongThucThanhToan = ref(PHUONG_THUC_THANH_TOAN.TIEN_MAT);
  const tienKhachDua = ref("");
  const tienMatKetHop = ref("");
  const tienChuyenKhoanKetHop = ref("");
  const ghiChuThanhToan = ref("");

  watch(() => hoaDonChoDaChon.value?.id, (newId, oldId) => {
    if (newId !== oldId) {
      phuongThucThanhToan.value = PHUONG_THUC_THANH_TOAN.TIEN_MAT;
      tienKhachDua.value = "";
      tienMatKetHop.value = "";
      tienChuyenKhoanKetHop.value = "";
      ghiChuThanhToan.value = "";
    }
  });

  const tienMatThanhToan = computed(() => Number(layChuSoTien(tienMatKetHop.value)) || 0);
  const tienChuyenKhoanThanhToan = computed(() => Number(layChuSoTien(tienChuyenKhoanKetHop.value)) || 0);

  const tienKhachThanhToan = computed(() => {
    if (phuongThucThanhToan.value === PHUONG_THUC_THANH_TOAN.KET_HOP) {
      return tienMatThanhToan.value + tienChuyenKhoanThanhToan.value;
    }
    const parsed = Number(layChuSoTien(tienKhachDua.value));
    return Number.isFinite(parsed) ? parsed : 0;
  });
  const tienThua = computed(() => {
    if (phuongThucThanhToan.value === PHUONG_THUC_THANH_TOAN.TIEN_MAT) {
      return Math.max(tienKhachThanhToan.value - khachCanTra.value, 0);
    }
    if (phuongThucThanhToan.value === PHUONG_THUC_THANH_TOAN.KET_HOP) {
      const tongDua = tienMatThanhToan.value + tienChuyenKhoanThanhToan.value;
      return Math.max(tongDua - khachCanTra.value, 0);
    }
    return 0;
  });
  const thongBaoLoiThanhToan = computed(() => {
    if (!cartItems.value.length || khachCanTra.value <= 0) {
      return "";
    }
    if (phuongThucThanhToan.value === PHUONG_THUC_THANH_TOAN.TIEN_MAT) {
      if (!tienKhachDua.value.trim()) {
        return "";
      }
      if (tienKhachThanhToan.value <= 0) {
        return "Số tiền khách đưa phải lớn hơn 0.";
      }
      if (tienKhachThanhToan.value < khachCanTra.value) {
        return "Số tiền khách đưa phải lớn hơn hoặc bằng khách cần trả.";
      }
    }
    if (phuongThucThanhToan.value === PHUONG_THUC_THANH_TOAN.KET_HOP) {
      const tongDua = tienMatThanhToan.value + tienChuyenKhoanThanhToan.value;
      if (tongDua < khachCanTra.value) {
        return "Tổng tiền mặt + chuyển khoản phải lớn hơn hoặc bằng tổng tiền khách cần trả.";
      }
    }
    return "";
  });

  function capNhatTienKhachThanhToan(isPaymentMethodChange = false, force = false) {
    if (!cartItems.value.length) {
      tienKhachDua.value = "";
      tienMatKetHop.value = "";
      tienChuyenKhoanKetHop.value = "";
      return;
    }
    if (phuongThucThanhToan.value === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN) {
      tienKhachDua.value = dinhDangSo(khachCanTra.value);
      return;
    }
    if (phuongThucThanhToan.value === PHUONG_THUC_THANH_TOAN.KET_HOP) {
      if (force || isPaymentMethodChange) {
        tienMatKetHop.value = "";
        tienChuyenKhoanKetHop.value = dinhDangSo(khachCanTra.value);
      }
      return;
    }
    if (force || isPaymentMethodChange) {
      tienKhachDua.value = "";
    }
  }

  function kiemTraLoiThanhToan() {
    return validateThanhToan(phuongThucThanhToan.value, tienKhachDua.value, thongBaoLoiThanhToan.value);
  }

  function dinhDangTienKhachDua() {
    if (phuongThucThanhToan.value === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN) {
      tienKhachDua.value = dinhDangSo(khachCanTra.value);
      return;
    }
    tienKhachDua.value = dinhDangTienNhap(tienKhachDua.value);
  }

  function xuLyTienKhachDuaInput(value) {
    tienKhachDua.value = value;
    dinhDangTienKhachDua();
  }

  function xuLyTienMatKetHopInput(value) {
    tienMatKetHop.value = dinhDangTienNhap(value);
    const parsedMat = Number(layChuSoTien(tienMatKetHop.value)) || 0;
    const conLai = Math.max(khachCanTra.value - parsedMat, 0);
    tienChuyenKhoanKetHop.value = dinhDangSo(conLai);
  }

  function xuLyTienChuyenKhoanKetHopInput(value) {
    tienChuyenKhoanKetHop.value = dinhDangTienNhap(value);
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
    tienMatKetHop,
    tienChuyenKhoanKetHop,
    ghiChuThanhToan,
    tienKhachThanhToan,
    tienThua,
    thongBaoLoiThanhToan,
    capNhatTienKhachThanhToan: (force = false) => capNhatTienKhachThanhToan(false, force),
    kiemTraLoiThanhToan,
    xuLyTienKhachDuaInput,
    xuLyTienMatKetHopInput,
    xuLyTienChuyenKhoanKetHopInput
  };
}
