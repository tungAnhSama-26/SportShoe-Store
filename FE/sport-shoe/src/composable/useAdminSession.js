import { computed, readonly, ref } from "vue";

const SESSION_STORAGE_NOTICE_KEY = "sport-shoe-admin-role-notice";
const STORAGE_KEYS = [
  "sport-shoe-admin-session",
  "adminUser",
  "sport-shoe-session",
  "sport-shoe-user",
  "adminSession",
  "currentUser",
  "authUser",
  "user",
];

const DEFAULT_SESSION = Object.freeze({
  id: "",
  hoTen: "Quản trị hệ thống",
  tenTaiKhoan: "admin",
  vaiTro: "Quản trị viên",
  hinhAnh: "",
});

const adminSessionState = ref(readSessionFromStorage());

function safeParse(value) {
  if (!value || typeof value !== "string") {
    return null;
  }

  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function firstNonEmpty(...values) {
  return values.find((value) => typeof value === "string" && value.trim())?.trim() ?? "";
}

function normalizeRole(rawRole) {
  if (typeof rawRole === "number") {
    return rawRole === 1 ? "Quản trị viên" : "Nhân viên";
  }

  const role = String(rawRole ?? "").trim();
  if (!role) {
    return DEFAULT_SESSION.vaiTro;
  }

  const normalized = role.toUpperCase();
  if (normalized === "1" || normalized === "ADMIN" || normalized === "ROLE_ADMIN") {
    return "Quản trị viên";
  }
  if (normalized === "2" || normalized === "STAFF" || normalized === "EMPLOYEE" || normalized === "ROLE_STAFF") {
    return "Nhân viên";
  }

  return role;
}

function pickSessionCandidate(parsedValue) {
  if (!parsedValue || typeof parsedValue !== "object") {
    return null;
  }

  return parsedValue.user
    ?? parsedValue.account
    ?? parsedValue.profile
    ?? parsedValue.data?.user
    ?? parsedValue.data?.account
    ?? parsedValue.data
    ?? parsedValue;
}

function normalizeSession(candidate) {
  if (!candidate || typeof candidate !== "object") {
    return { ...DEFAULT_SESSION };
  }

  return {
    id: candidate.id ?? "",
    hoTen: firstNonEmpty(candidate.hoTen, candidate.fullName, candidate.name, DEFAULT_SESSION.hoTen),
    tenTaiKhoan: firstNonEmpty(
      candidate.tenTaiKhoan,
      candidate.tenDangNhap,
      candidate.username,
      candidate.userName,
      DEFAULT_SESSION.tenTaiKhoan,
    ),
    vaiTro: normalizeRole(
      candidate.vaiTro
      ?? candidate.role
      ?? candidate.tenVaiTro
      ?? candidate.roleName
      ?? candidate.maVaiTro,
    ),
    hinhAnh: firstNonEmpty(candidate.hinhAnh, candidate.avatar, candidate.avatarUrl, candidate.photoURL),
  };
}

function readSessionFromStorage() {
  if (typeof window === "undefined") {
    return { ...DEFAULT_SESSION };
  }

  for (const storageKey of STORAGE_KEYS) {
    const parsedValue = safeParse(window.localStorage.getItem(storageKey));
    const candidate = pickSessionCandidate(parsedValue);

    if (candidate) {
      return normalizeSession(candidate);
    }
  }

  return { ...DEFAULT_SESSION };
}

function refreshAdminSession() {
  adminSessionState.value = readSessionFromStorage();
  return adminSessionState.value;
}

function shouldShowRoleNotice() {
  const currentSession = refreshAdminSession();

  if (typeof window === "undefined") {
    return false;
  }

  const token = `${currentSession.tenTaiKhoan}:${currentSession.vaiTro}`;
  const currentNotice = window.sessionStorage.getItem(SESSION_STORAGE_NOTICE_KEY);

  if (currentNotice === token) {
    return false;
  }

  window.sessionStorage.setItem(SESSION_STORAGE_NOTICE_KEY, token);
  return true;
}

const accountDisplayName = computed(
  () => adminSessionState.value.tenTaiKhoan || adminSessionState.value.hoTen || DEFAULT_SESSION.tenTaiKhoan,
);

const avatarUrl = computed(() => {
  if (adminSessionState.value.hinhAnh) {
    return adminSessionState.value.hinhAnh;
  }

  const encodedName = encodeURIComponent(accountDisplayName.value);
  return `https://ui-avatars.com/api/?name=${encodedName}&background=0f172a&color=ffffff&size=128`;
});

export function useAdminSession() {
  return {
    adminSession: readonly(adminSessionState),
    accountDisplayName,
    avatarUrl,
    refreshAdminSession,
    shouldShowRoleNotice,
  };
}
