import { sanitizeErrorMessage } from "../utils/error-message";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ??
  "http://localhost:8080/api/v1";

export async function request(path, init) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
    ...init,
  });
  const payload = await response.json();
  if (!response.ok) {
    throw new Error(
      sanitizeErrorMessage(
        payload.message,
        "Không thể hoàn tất thao tác đăng nhập lúc này. Vui lòng thử lại.",
      ),
    );
  }
  return payload; // Return full payload to get message and data
}

export async function login(username, password) {
  const result = await request("/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });

  if (result.data) {
    localStorage.setItem("user", JSON.stringify(result.data));
  }

  return result.data;
}

export async function adminLogin(username, password) {
  localStorage.removeItem("adminToken");
  localStorage.removeItem("adminUser");
  localStorage.removeItem("sport-shoe-admin-session");

  const result = await request("/auth/admin/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });

  if (result.data) {
    const { token, ...adminUser } = result.data;
    localStorage.setItem("adminToken", token);
    localStorage.setItem("adminUser", JSON.stringify(adminUser));
    localStorage.setItem("sport-shoe-admin-session", JSON.stringify(adminUser));
  }

  return result.data;
}

export async function register(payload) {
  return request("/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function logout() {
  localStorage.removeItem("user");
  localStorage.removeItem("adminToken");
  localStorage.removeItem("adminUser");
  localStorage.removeItem("sport-shoe-admin-session");
}

export function getCurrentUser() {
  const user = localStorage.getItem("adminUser") ?? localStorage.getItem("user");
  return user ? JSON.parse(user) : null;
}

export function getCurrentAdminUser() {
  const user = localStorage.getItem("adminUser");
  return user ? JSON.parse(user) : null;
}

export function getAdminToken() {
  return localStorage.getItem("adminToken") ?? "";
}

export function getAuthHeaders() {
  const token = getAdminToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export function isAdminAuthenticated() {
  return Boolean(getAdminToken() && getCurrentAdminUser());
}

export function isAdminRole() {
  return Number(getCurrentAdminUser()?.vaiTro) === 1;
}

export function hasRequiredAdminCccd() {
  const cccd = String(getCurrentAdminUser()?.cccd ?? "").trim();
  return /^\d{12}$/.test(cccd);
}
