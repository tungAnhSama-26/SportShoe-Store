import { showError } from "../../utils/alert";

export function validateGioHang(sanPhamValidationMessage, isPayment, cartItemsLength) {
  if (sanPhamValidationMessage) {
    showError(sanPhamValidationMessage);
    return false;
  }
  if (isPayment && cartItemsLength === 0) {
    showError("Vui lòng thêm ít nhất 1 sản phẩm vào hóa đơn để thanh toán.");
    return false;
  }
  return true;
}
