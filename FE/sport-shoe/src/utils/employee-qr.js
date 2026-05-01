export const EMPLOYEE_QR_DRAFT_KEY = "admin-nhan-vien-qr-draft"

function ensureBrowserStorage() {
  if (typeof window === "undefined" || !window.sessionStorage) {
    return null
  }

  return window.sessionStorage
}

function normalizeToken(value) {
  return String(value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim()
    .toLowerCase()
}

function resolveGioiTinh(value) {
  const normalized = normalizeToken(value)
  return normalized === "nu" || normalized === "female" ? "Nữ" : "Nam"
}

export function parseEmployeeQrPayload(rawValue) {
  const normalizedValue = String(rawValue ?? "").trim()
  const cccdValue = normalizedValue.replace(/\r?\n/g, "|")

  if (!normalizedValue) {
    throw new Error("Vui long cung cap du lieu QR hop le.")
  }

  if (cccdValue.includes("|")) {
    const parts = cccdValue
      .split("|")
      .map((part) => String(part ?? "").trim())
      .filter((part, index) => part || index < 6)

    if (parts.length >= 6) {
      const rawNgaySinh = String(parts[3] ?? "").trim()
      const ngaySinh =
        rawNgaySinh.length === 8
          ? `${rawNgaySinh.substring(4, 8)}-${rawNgaySinh.substring(2, 4)}-${rawNgaySinh.substring(0, 2)}`
          : ""

      return {
        hoTen: String(parts[2] ?? "").trim(),
        email: "",
        sdt: "",
        cccd: String(parts[0] ?? "").trim(),
        gioiTinh: resolveGioiTinh(parts[4]),
        ngaySinh,
        tinhThanh: "",
        quanHuyen: "",
        xaPhuong: "",
        diaChiCuThe: String(parts[5] ?? "").trim(),
        vaiTro: 2,
        matKhau: "",
        sourceMessage: "Da dien thong tin tu CCCD.",
      }
    }
  }

  let parsed = null
  try {
    parsed = JSON.parse(normalizedValue)
  } catch {
    throw new Error("Ma QR khong dung dinh dang CCCD hoac du lieu hop le.")
  }

  if (!parsed || parsed.type !== "sportshoe-employee") {
    throw new Error("Ma QR nay khong phai du lieu nhan vien SportShoe.")
  }

  return {
    hoTen: String(parsed.hoTen ?? "").trim(),
    email: String(parsed.email ?? "").trim(),
    sdt: String(parsed.sdt ?? "").trim(),
    cccd: String(parsed.cccd ?? "").trim(),
    gioiTinh: resolveGioiTinh(parsed.gioiTinh),
    ngaySinh: String(parsed.ngaySinh ?? "").trim(),
    tinhThanh: String(parsed.tinhThanh ?? "").trim(),
    quanHuyen: String(parsed.quanHuyen ?? "").trim(),
    xaPhuong: String(parsed.xaPhuong ?? "").trim(),
    diaChiCuThe: String(parsed.diaChiCuThe ?? "").trim(),
    vaiTro: Number(parsed.vaiTro ?? 2) || 2,
    matKhau: String(parsed.matKhau ?? "").trim(),
    sourceMessage: "Da dien thong tin tu ma QR.",
  }
}

export function saveEmployeeQrDraft(payload) {
  const storage = ensureBrowserStorage()
  if (!storage) return

  storage.setItem(EMPLOYEE_QR_DRAFT_KEY, JSON.stringify(payload))
}

export function readEmployeeQrDraft() {
  const storage = ensureBrowserStorage()
  if (!storage) return null

  const rawValue = storage.getItem(EMPLOYEE_QR_DRAFT_KEY)
  if (!rawValue) return null

  try {
    return JSON.parse(rawValue)
  } catch {
    storage.removeItem(EMPLOYEE_QR_DRAFT_KEY)
    return null
  }
}

export function clearEmployeeQrDraft() {
  const storage = ensureBrowserStorage()
  if (!storage) return

  storage.removeItem(EMPLOYEE_QR_DRAFT_KEY)
}
