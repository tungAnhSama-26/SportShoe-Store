import { apiRequest } from "./api-client";

// Gợi ý giày bằng AI: khách trả lời trắc nghiệm (chọn nhiều đáp án) + quét form chân không bắt buộc.

// Lấy bộ câu hỏi để hiển thị.
export async function layCauHoiGoiY() {
  return apiRequest("/client/goi-y/cau-hoi", {
    fallbackMessage: "Không tải được câu hỏi gợi ý",
  });
}

// Gửi đáp án + ảnh bàn chân (data URI base64, có thể bỏ trống) -> nhận gợi ý.
export async function layGoiYGiay({ traLoi, anhChan } = {}) {
  return apiRequest("/client/goi-y", {
    method: "POST",
    body: JSON.stringify({ traLoi, anhChan: anhChan || null }),
    fallbackMessage: "AI chưa gợi ý được, vui lòng thử lại",
  });
}
