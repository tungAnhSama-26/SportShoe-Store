import { apiRequest } from "./api-client";

export async function yeuCauTraHang(payload) {
  return apiRequest("/client/tra-hang/yeu-cau", {
    method: "POST",
    authScope: "customer",
    body: JSON.stringify(payload),
    fallbackMessage: "Không thể gửi yêu cầu trả hàng",
  });
}
