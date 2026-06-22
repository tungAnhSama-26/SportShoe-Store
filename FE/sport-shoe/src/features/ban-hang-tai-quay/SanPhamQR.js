import { KHOA_QR_SAN_PHAM } from "./HangSo";

export function trichXuatTuKhoaSanPhamTuQr(rawValue) {
  const normalizedRawValue = String(rawValue ?? "").trim();
  if (!normalizedRawValue) {
    return "";
  }

  try {
    const parsed = JSON.parse(normalizedRawValue);
    if (parsed && typeof parsed === "object") {
      for (const key of KHOA_QR_SAN_PHAM) {
        const value = parsed[key];
        if (typeof value === "string" && value.trim()) {
          return value.trim();
        }
      }
    }
  } catch {
    // Ignore non-JSON QR payloads.
  }

  try {
    const url = new URL(normalizedRawValue);
    for (const key of KHOA_QR_SAN_PHAM) {
      const value = url.searchParams.get(key);
      if (value?.trim()) {
        return value.trim();
      }
    }

    const pathSegments = url.pathname.split("/").filter(Boolean);
    const lastSegment = pathSegments.at(-1);
    if (lastSegment && /^[A-Za-z0-9._-]+$/.test(lastSegment)) {
      return lastSegment.trim();
    }
  } catch {
    // Ignore plain-text QR payloads.
  }

  return normalizedRawValue;
}
