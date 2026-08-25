import { apiRequest } from "./api-client";

// Gợi ý giày bằng AI: khách trả lời bộ câu hỏi trắc nghiệm (chọn nhiều đáp án).

// Lấy bộ câu hỏi để hiển thị.
export async function layCauHoiGoiY() {
  return apiRequest("/client/goi-y/cau-hoi", {
    fallbackMessage: "Không tải được câu hỏi gợi ý",
  });
}

// Gửi đáp án -> nhận gợi ý.
export async function layGoiYGiay({ traLoi } = {}) {
  return apiRequest("/client/goi-y", {
    method: "POST",
    body: JSON.stringify({ traLoi }),
    fallbackMessage: "AI chưa gợi ý được, vui lòng thử lại",
  });
}
