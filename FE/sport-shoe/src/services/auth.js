import { apiRequest, getAuthHeaders as getApiAuthHeaders } from "./api-client";

const ADMIN_ROLE_NOTICE_KEY = "sport-shoe-admin-role-notice";
const ADMIN_ROLE_NOTICE_PENDING_KEY = "sport-shoe-admin-role-notice-pending";

export async function request(path, init) {
  return apiRequest(path, {
    authenticated: false,
    fallbackMessage:
      "Không thể hoàn tất thao tác đăng nhập lúc này. Vui lòng thử lại.",
    unwrapData: false,
    ...init,
  });
}

export async function login(username, password) {
  logoutCustomer();

  const result = await request("/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });

  if (result.data) {
    localStorage.setItem("customerToken", result.data.token);
    localStorage.setItem("user", JSON.stringify(result.data.user));
  }

  return result.data?.user;
}

export async function adminLogin(username, password) {
  localStorage.removeItem("adminToken");
  localStorage.removeItem("adminUser");
  localStorage.removeItem("sport-shoe-admin-session");

  const result = await request("/auth/admin/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });

  if (result.data) {
    const { token, ...adminUser } = result.data;
    localStorage.setItem("adminToken", token);
    localStorage.setItem("adminUser", JSON.stringify(adminUser));
    localStorage.setItem("sport-shoe-admin-session", JSON.stringify(adminUser));
    sessionStorage.removeItem(ADMIN_ROLE_NOTICE_KEY);
    sessionStorage.setItem(ADMIN_ROLE_NOTICE_PENDING_KEY, "1");
  }

  return result.data;
}

export async function register(payload) {
  return request("/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function logoutCustomer() {
  localStorage.removeItem("user");
  localStorage.removeItem("customerToken");
}

export function logoutAdmin() {
  localStorage.removeItem("adminToken");
  localStorage.removeItem("adminUser");
  localStorage.removeItem("sport-shoe-admin-session");
}

export function logout() {
  logoutCustomer();
  logoutAdmin();
}

export function getCurrentUser() {
  const user = localStorage.getItem("adminUser") ?? localStorage.getItem("user");
  return user ? JSON.parse(user) : null;
}

export function getCurrentAdminUser() {
  const user = localStorage.getItem("adminUser");
  return user ? JSON.parse(user) : null;
}

export function getAdminToken() {
  return localStorage.getItem("adminToken") ?? "";
}

export function getCustomerToken() {
  return localStorage.getItem("customerToken") ?? "";
}

export function getAuthHeaders() {
  return getApiAuthHeaders();
}

export function isAdminAuthenticated() {
  return Boolean(getAdminToken() && getCurrentAdminUser());
}

export function isAdminRole() {
  return Number(getCurrentAdminUser()?.vaiTro) === 1;
}

export function hasRequiredAdminCccd() {
  const cccd = String(getCurrentAdminUser()?.cccd ?? "").trim();
  return /^\d{12}$/.test(cccd);
}

export function mustChangeAdminPassword() {
  const user = getCurrentAdminUser();
  return Number(user?.vaiTro) === 2
    && user?.batBuocDoiMatKhau === true
    && Boolean(user?.hanDoiMatKhau);
}
