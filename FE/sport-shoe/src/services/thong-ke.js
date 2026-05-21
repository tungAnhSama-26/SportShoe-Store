import { apiRequest } from "./api-client";

async function request(path, init) {
  return apiRequest(path, {
    fallbackMessage: "Không thể tải dữ liệu thống kê lúc này. Vui lòng thử lại sau.",
    ...init,
  });
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
