import { apiRequest } from "./api-client";

export function chatWithAdminAi(message) {
  return apiRequest("/admin/chatbot/chat", {
    method: "POST",
    body: JSON.stringify({ message }),
  });
}
