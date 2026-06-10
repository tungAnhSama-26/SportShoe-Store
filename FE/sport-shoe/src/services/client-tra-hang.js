import { apiRequest } from "./api-client";
import { layKhachId } from "./gio-hang";

export async function yeuCauTraHang(payload) {
  const id = layKhachId();
  if (!id) throw new Error("Vui lòng đăng nhập");
  return apiRequest(`/client/tra-hang/yeu-cau?khachHangId=${id}`, {
    method: "POST",
    authenticated: false,
    body: JSON.stringify(payload),
    fallbackMessage: "Không thể gửi yêu cầu trả hàng",
  });
}
