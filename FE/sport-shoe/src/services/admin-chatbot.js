import { apiRequest } from "./api-client";

export function chatWithAdminAi(message) {
  return apiRequest("/admin/chatbot/chat", {
    method: "POST",
    body: JSON.stringify({ message }),
  });
}

export function getAdminChatHistory() {
  return apiRequest("/admin/chatbot/history", {
    method: "GET",
  });
}

export function closeAdminAiSession() {
  return apiRequest("/admin/chatbot/close-session", {
    method: "POST",
  });
}

export function getAdminAiSessions() {
  return apiRequest("/admin/chatbot/sessions", {
    method: "GET",
  });
}

export function getAdminAiSessionMessages(sessionId) {
  return apiRequest(`/admin/chatbot/session/${sessionId}/messages`, {
    method: "GET",
  });
}
