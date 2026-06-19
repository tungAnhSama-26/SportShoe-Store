import { API_BASE_URL } from "../../../../../services/api-client";

export const productImageFallback =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='72' height='72' viewBox='0 0 72 72'%3E%3Crect width='72' height='72' rx='14' fill='%23f8fafc'/%3E%3Cpath d='M18 44h35c3 0 5-2 5-5 0-2-1-4-3-5l-10-5-7 8H25l-5-5-6 5v3c0 2 2 4 4 4z' fill='%23e2e8f0'/%3E%3Cpath d='M24 48h30' stroke='%2394a3b8' stroke-width='3' stroke-linecap='round'/%3E%3C/svg%3E";

const apiOrigin = API_BASE_URL.replace(/\/api\/v1\/?$/, "");

export function resolveProductImageUrl(url) {
  const value = String(url || "").trim();
  if (!value) return productImageFallback;
  if (/(https?:|data:|blob:)/i.test(value)) return value;
  if (value.startsWith("/uploads/")) return `${apiOrigin}${value}`;
  if (value.startsWith("uploads/")) return `${apiOrigin}/${value}`;
  return value.startsWith("/") ? value : `/${value}`;
}

export function handleProductImageError(event) {
  const target = event.currentTarget;
  if (target && target.src !== productImageFallback) {
    target.src = productImageFallback;
  }
}
