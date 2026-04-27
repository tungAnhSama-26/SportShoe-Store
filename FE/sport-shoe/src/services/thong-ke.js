import { sanitizeErrorMessage } from "../utils/error-message";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8080/api/v1";

async function request(path, init) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      headers: {
        "Content-Type": "application/json",
        ...(init?.headers ?? {})
      },
      ...init
    });
  } catch (error) {
    if (error?.name === "AbortError") {
      throw error;
    }
    throw new Error("Không thể tải dữ liệu thống kê lúc này. Vui lòng thử lại sau.");
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
    throw new Error(
      sanitizeErrorMessage(
        payload?.message,
        "Không thể tải dữ liệu thống kê lúc này. Vui lòng thử lại sau.",
      ),
    );
  }

  return payload?.data ?? payload;
}

function layDashboardThongKe(params = {}, init) {
  const searchParams = new URLSearchParams();

  if (params.fromDate) {
    searchParams.set("fromDate", params.fromDate);
  }
  if (params.toDate) {
    searchParams.set("toDate", params.toDate);
  }
  if (params.brandId != null) {
    searchParams.set("brandId", String(params.brandId));
  }
  if (params.keyword?.trim()) {
    searchParams.set("keyword", params.keyword.trim());
  }
  if (params.periodType?.trim()) {
    searchParams.set("periodType", params.periodType.trim());
  }

  const queryString = searchParams.toString();
  return request(`/admin/thong-ke/dashboard${queryString ? `?${queryString}` : ""}`, init);
}

export {
  layDashboardThongKe
};
