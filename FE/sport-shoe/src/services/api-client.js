import { createRequestError, sanitizeErrorMessage } from "../utils/error-message";

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ??
  "http://localhost:8080/api/v1";

const DEFAULT_FALLBACK =
  "Không thể hoàn tất thao tác này lúc này. Vui lòng thử lại.";

function getStoredAdminToken() {
  return localStorage.getItem("adminToken") ?? "";
}

export function getAuthHeaders() {
  const token = getStoredAdminToken();
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

function buildHeaders(init, authenticated) {
  const initHeaders = init?.headers ?? {};
  const body = init?.body;
  const isFormData = typeof FormData !== "undefined" && body instanceof FormData;
  return {
    ...(isFormData ? {} : { "Content-Type": "application/json" }),
    ...(authenticated ? getAuthHeaders() : {}),
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
    unwrapData = true,
    ...init
  } = options;

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: buildHeaders(init, authenticated),
    });
  } catch (error) {
    if (error?.name === "AbortError") {
      throw error;
    }
    throw new Error(sanitizeErrorMessage(error?.message, fallbackMessage));
  }

  const payload = await parsePayload(response);

  if (!response.ok) {
    const requestError = createRequestError(
      getFirstFieldError(payload?.errors) || payload?.message || `HTTP ${response.status}`,
      fallbackMessage,
      payload?.errors,
    );
    requestError.status = response.status;
    requestError.payload = payload;
    throw requestError;
  }

  return unwrapData ? payload?.data ?? payload : payload;
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
