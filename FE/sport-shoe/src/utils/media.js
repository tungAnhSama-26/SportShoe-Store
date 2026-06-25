import { API_BASE_URL } from "../services/api-client";

const apiOrigin = API_BASE_URL.replace(/\/api\/v1\/?$/, "");

// Chuyển URL ảnh/video (có thể tương đối "/uploads/..") sang URL đầy đủ.
export function resolveMediaUrl(url) {
  const value = String(url || "").trim();
  if (!value) return "";
  if (/^(https?:|data:|blob:)/i.test(value)) return value;
  if (value.startsWith("/")) return `${apiOrigin}${value}`;
  return `${apiOrigin}/${value}`;
}

const PHAN_MO_RONG_VIDEO = ["mp4", "webm", "ogg", "mov", "m4v", "avi", "mkv"];

// Đoán loại media theo phần mở rộng của URL.
export function doanLoaiMedia(url) {
  const ext = String(url || "").split("?")[0].split(".").pop().toLowerCase();
  return PHAN_MO_RONG_VIDEO.includes(ext) ? "video" : "image";
}

function chuanHoa(arr) {
  return arr
    .map((m) => {
      if (typeof m === "string") return { url: m, loai: doanLoaiMedia(m) };
      const url = m?.url || "";
      return { url, loai: m?.loai || doanLoaiMedia(url) };
    })
    .filter((m) => m.url);
}

// Parse chuỗi JSON media (hoặc mảng) -> mảng [{url, loai}]. Bỏ qua dữ liệu lỗi.
export function parseMedia(media) {
  if (!media) return [];
  if (Array.isArray(media)) return chuanHoa(media);
  try {
    const arr = JSON.parse(media);
    return Array.isArray(arr) ? chuanHoa(arr) : [];
  } catch {
    return [];
  }
}
