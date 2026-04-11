const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:8080/api/v1";

async function request(path, init) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {})
    },
    ...init
  });

  const payload = await response.json();

  if (!response.ok) {
    throw new Error(payload.message || "Khong the ket noi den may chu");
  }

  return payload.data;
}

function layDashboardThongKe(params = {}) {
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
  return request(`/admin/thong-ke/dashboard${queryString ? `?${queryString}` : ""}`);
}

export {
  layDashboardThongKe
};
