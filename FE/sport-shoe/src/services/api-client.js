import { createRequestError, sanitizeErrorMessage } from "../utils/error-message";

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ??
  "/api/v1";

const DEFAULT_FALLBACK =
  "Không thể hoàn tất thao tác này lúc này. Vui lòng thử lại.";

const ERROR_PAGE_STATUSES = new Set([401, 403]);

// Màn đăng nhập của khách (khác hẳn màn đăng nhập nhân viên /admin/login).
const DUONG_DAN_DANG_NHAP_KHACH = "/login";
const THONG_BAO_HET_PHIEN_KHACH =
  "Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.";

function resolveAuthScope(path, authScope) {
  if (authScope === "admin" || authScope === "customer") {
    return authScope;
  }

  const currentPath = typeof window === "undefined" ? "" : window.location.pathname;
  if (currentPath.startsWith("/admin") || currentPath.startsWith("/nhanvien")) {
    return "admin";
  }

  if (path.startsWith("/admin/") || path === "/admin" || path.startsWith("/nhanvien/")) {
    return "admin";
  }
  if (path.startsWith("/client/")) {
    return "customer";
  }

  return "customer";
}

function getStoredToken(authScope = "auto", path = "") {
  const scope = resolveAuthScope(path, authScope);
  return scope === "admin"
    ? localStorage.getItem("adminToken") ?? ""
    : localStorage.getItem("customerToken") ?? "";
}

export function getAuthHeaders(authScope = "auto", path = "") {
  const token = getStoredToken(authScope, path);
  return token ? { Authorization: `Bearer ${token}` } : {};
}

function getFirstFieldError(errors) {
  if (!errors || typeof errors !== "object" || Array.isArray(errors)) {
    return "";
  }

  const entry = Object.entries(errors)
    .find(([, value]) => typeof value === "string" && value.trim());

  return entry ? sanitizeErrorMessage(entry[1], entry[1], entry[0]) : "";
}

function buildHeaders(path, init, authenticated, authScope) {
  const initHeaders = init?.headers ?? {};
  const body = init?.body;
  const isFormData = typeof FormData !== "undefined" && body instanceof FormData;
  return {
    ...(isFormData ? {} : { "Content-Type": "application/json" }),
    ...(authenticated ? getAuthHeaders(authScope, path) : {}),
    "Cache-Control": "no-cache, no-store, must-revalidate",
    "Pragma": "no-cache",
    "Expires": "0",
    ...initHeaders,
  };
}

async function parsePayload(response) {
  const text = await response.text();
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

export async function apiRequest(path, options = {}) {
  const {
    fallbackMessage = DEFAULT_FALLBACK,
    authenticated = true,
    authScope = "auto",
    unwrapData = true,
    ...init
  } = options;
  const resolvedAuthScope = resolveAuthScope(path, authScope);

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: buildHeaders(path, init, authenticated, resolvedAuthScope),
    });
  } catch (error) {
    if (error?.name === "AbortError") {
      throw error;
    }
    throw new Error(sanitizeErrorMessage(error?.message, fallbackMessage));
  }

  const payload = await parsePayload(response);

  if (!response.ok) {
    const message = getFirstFieldError(payload?.errors) || payload?.message || `HTTP ${response.status}`;
    if (response.status === 401) {
      clearStoredSession(resolvedAuthScope);
    }
    redirectToErrorPage(response.status, message, resolvedAuthScope);

    const requestError = createRequestError(
      message,
      fallbackMessage,
      payload?.errors,
    );
    requestError.status = response.status;
    requestError.payload = payload;
    throw requestError;
  }

  if (unwrapData) {
    if (payload && typeof payload === "object" && ("success" in payload || "message" in payload)) {
      return payload.data !== undefined ? payload.data : null;
    }
    return payload?.data ?? payload;
  }
  return payload;
}

function clearStoredSession(authScope) {
  if (authScope === "admin") {
    localStorage.removeItem("adminToken");
    localStorage.removeItem("adminUser");
    localStorage.removeItem("sport-shoe-admin-session");
    return;
  }

  localStorage.removeItem("user");
  localStorage.removeItem("customerToken");
}

function redirectToErrorPage(status, message, authScope) {
  if (!ERROR_PAGE_STATUSES.has(status) || typeof window === "undefined") {
    return;
  }
  if (window.location.pathname.startsWith("/error/")) {
    return;
  }

  const duongDanHienTai = window.location.pathname + window.location.search + window.location.hash;

  // Khách hàng bị admin khóa (hoặc token hết hạn) -> đưa thẳng về màn đăng nhập của KHÁCH.
  // Không dùng trang lỗi chung vì nút chính của trang đó trỏ sang đăng nhập nhân viên.
  if (authScope === "customer" && status === 401) {
    if (window.location.pathname === DUONG_DAN_DANG_NHAP_KHACH) {
      return;
    }
    const queryKhach = new URLSearchParams();
    queryKhach.set("thongBao", sanitizeErrorMessage(message, THONG_BAO_HET_PHIEN_KHACH));
    queryKhach.set("redirect", duongDanHienTai);
    window.location.assign(`${DUONG_DAN_DANG_NHAP_KHACH}?${queryKhach.toString()}`);
    return;
  }

  const query = new URLSearchParams();
  if (message) {
    query.set("message", sanitizeErrorMessage(message, String(message)));
  }
  query.set("redirect", duongDanHienTai);
  // Trang lỗi dựa vào phạm vi này để điều hướng đúng khu vực (khách / quản trị).
  query.set("scope", authScope === "customer" ? "customer" : "admin");

  window.location.assign(`/error/${status}?${query.toString()}`);
}

export function buildQuery(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      query.set(key, String(value));
    }
  });
  const result = query.toString();
  return result ? `?${result}` : "";
}

export async function uploadFileRequest(file, fallbackMessage) {
  const formData = new FormData();
  formData.append("file", file);

  const payload = await apiRequest("/upload", {
    method: "POST",
    body: formData,
    fallbackMessage,
  });

  if (!payload?.url) {
    throw new Error("Không nhận được URL ảnh sau khi upload");
  }

  return payload.url;
}
