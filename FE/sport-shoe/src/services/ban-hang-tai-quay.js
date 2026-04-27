import { createRequestError, sanitizeErrorMessage } from "../utils/error-message";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8080/api/v1";

function firstFieldError(errors) {
  if (!errors || typeof errors !== "object" || Array.isArray(errors)) {
    return "";
  }

  return Object.values(errors).find((value) => typeof value === "string" && value.trim())?.trim() ?? "";
}

async function request(path, init) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      headers: {
        "Content-Type": "application/json",
        ...init?.headers ?? {}
      },
      ...init
    });
  } catch {
    throw new Error("Khong the hoan tat thao tac ban hang tai quay luc nay. Vui long thu lai.");
  }

  const text = await response.text();
  let payload = null;
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = null;
    }
  }

  if (!response.ok) {
    throw createRequestError(
      firstFieldError(payload?.errors) || payload?.message,
      "Khong the hoan tat thao tac ban hang tai quay luc nay. Vui long thu lai.",
      payload?.errors,
    );
  }

  return payload?.data ?? payload;
}
function timKhachHangTheoSoDienThoai(phone) {
  const params = new URLSearchParams();
  if (phone.trim()) {
    params.set("phone", phone.trim());
  }
  return request(`/admin/ban-hang-tai-quay/khach-hang?${params.toString()}`);
}
function timSanPhamTaiQuay(keyword) {
  const params = new URLSearchParams();
  if (keyword.trim()) {
    params.set("keyword", keyword.trim());
  }
  return request(`/admin/ban-hang-tai-quay/san-pham?${params.toString()}`);
}
function layDanhSachHoaDonCho() {
  return request("/admin/ban-hang-tai-quay/hoa-don-cho");
}
function layChiTietHoaDonCho(id) {
  return request(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}`);
}
function taoHoaDonCho(payload) {
  return request("/admin/ban-hang-tai-quay/hoa-don-cho", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
function apDungPhieuGiamGiaTaiQuay(payload) {
  return request("/admin/ban-hang-tai-quay/phieu-giam-gia/ap-dung", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
function tinhPhiVanChuyenTaiQuay(payload) {
  return request("/admin/ban-hang-tai-quay/phi-van-chuyen/ghn", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
function timPhieuGiamGiaTaiQuay(params) {
  const searchParams = new URLSearchParams();
  if (params.keyword?.trim()) {
    searchParams.set("keyword", params.keyword.trim());
  }
  if (params.hoaDonId != null) {
    searchParams.set("hoaDonId", String(params.hoaDonId));
  }
  if (params.khachHangId) {
    searchParams.set("khachHangId", params.khachHangId);
  }
  if (params.tongTienHang != null) {
    searchParams.set("tongTienHang", String(params.tongTienHang));
  }
  return request(`/admin/ban-hang-tai-quay/phieu-giam-gia?${searchParams.toString()}`);
}
function thanhToanTaiQuay(payload) {
  return request("/admin/ban-hang-tai-quay/thanh-toan", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
function huyHoaDonCho(id) {
  return request(`/admin/ban-hang-tai-quay/hoa-don-cho/${id}/huy`, {
    method: "PATCH"
  });
}
export {
  apDungPhieuGiamGiaTaiQuay,
  huyHoaDonCho,
  layChiTietHoaDonCho,
  layDanhSachHoaDonCho,
  tinhPhiVanChuyenTaiQuay,
  taoHoaDonCho,
  thanhToanTaiQuay,
  timKhachHangTheoSoDienThoai,
  timPhieuGiamGiaTaiQuay,
  timSanPhamTaiQuay
};
