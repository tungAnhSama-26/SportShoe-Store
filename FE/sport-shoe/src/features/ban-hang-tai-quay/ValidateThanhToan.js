import { showError } from "../../utils/alert";
import { PHUONG_THUC_THANH_TOAN } from "./Enum";

export function validateThanhToan(phuongThucThanhToan, tienKhachDua, thongBaoLoiThanhToan) {
  if (phuongThucThanhToan === PHUONG_THUC_THANH_TOAN.TIEN_MAT && !tienKhachDua) {
    showError("Vui lòng nhập số tiền khách đưa.");
    return false;
  }
  if (thongBaoLoiThanhToan) {
    showError(thongBaoLoiThanhToan);
    return false;
  }
  return true;
}
