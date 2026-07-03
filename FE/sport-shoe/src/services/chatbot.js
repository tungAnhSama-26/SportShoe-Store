import { apiRequest } from "./api-client";

// ==========================================
// CLIENT APIS (Khách hàng)
// ==========================================

export async function guiTinNhanClient(message, sessionId = null, customerName = "", phoneNumber = "") {
  return apiRequest("/client/chatbot/chat", {
    method: "POST",
    body: JSON.stringify({ sessionId, message, customerName, phoneNumber }),
    authenticated: false, // public endpoint
  });
}

export async function yeuCauNhanVien(sessionId) {
  return apiRequest(`/client/chatbot/session/${sessionId}/request-staff`, {
    method: "POST",
    authenticated: false,
  });
}

export async function dongPhienChatClientInactivity(sessionId) {
  return apiRequest(`/client/chatbot/session/${sessionId}/close-due-to-inactivity`, {
    method: "POST",
    authenticated: false,
  });
}

export async function layTinNhanClient(sessionId) {
  return apiRequest(`/client/chatbot/session/${sessionId}/messages`, {
    method: "GET",
    authenticated: false,
  });
}

// ==========================================
// ADMIN APIS (Nhân viên / Admin)
// ==========================================

export async function layDanhSachPhienAdmin() {
  return apiRequest("/admin/chat/sessions", {
    method: "GET",
    authScope: "admin",
    authenticated: true,
  });
}

export async function layTinNhanAdmin(sessionId) {
  return apiRequest(`/admin/chat/sessions/${sessionId}/messages`, {
    method: "GET",
    authScope: "admin",
    authenticated: true,
  });
}

export async function nhanVienPhanHoi(sessionId, message) {
  return apiRequest(`/admin/chat/sessions/${sessionId}/reply`, {
    method: "POST",
    body: JSON.stringify({ message }),
    authScope: "admin",
    authenticated: true,
  });
}

export async function dongPhienChatAdmin(sessionId) {
  return apiRequest(`/admin/chat/sessions/${sessionId}/close`, {
    method: "POST",
    authScope: "admin",
    authenticated: true,
  });
}

export async function layLichSuPhienAdmin() {
  return apiRequest("/admin/chat/sessions/history", {
    method: "GET",
    authScope: "admin",
    authenticated: true,
  });
}
