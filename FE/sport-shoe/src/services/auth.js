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
    // Store user info in localStorage
    localStorage.setItem("user", JSON.stringify(result.data));
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
}

export function getCurrentUser() {
  const user = localStorage.getItem("user");
  return user ? JSON.parse(user) : null;
}
