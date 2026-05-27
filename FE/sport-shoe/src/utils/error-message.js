const GENERIC_ERROR_PATTERNS = [
  /^co loi xay ra$/,
  /^loi he thong khong mong muon$/,
  /^unexpected system error$/,
  /^khong the xu ly yeu cau(?: [a-z0-9 ]+)? luc nay vui long thu lai sau$/,
  /^khong the hoan tat thao tac(?: [a-z0-9 ]+)? luc nay vui long thu lai(?: sau)?$/,
  /^thao tac chua the hoan tat(?: [a-z0-9 ]+)? vui long thu lai(?: sau)?$/,
  /^khong the ket noi den may chu(?: https?:.*)?$/,
  /^http [45]\d\d$/,
  /^internal server error$/,
  /^failed to fetch$/,
  /^network error$/,
  /^load failed$/
]

const FIELD_LABELS = {
  ma: 'Mã sản phẩm',
  ten: 'Tên sản phẩm',
  thuongHieuId: 'Thương hiệu',
  loaiGiayId: 'Loại giày',
  gioiTinh: 'Giới tính',
  chatLieuGiayId: 'Chất liệu',
  deGiayId: 'Đế giày',
  coGiayId: 'Cổ giày',
  congNgheDemId: 'Công nghệ đệm',
  trongLuongId: 'Trọng lượng',
  mauSacId: 'Màu sắc',
  kichCoId: 'Kích cỡ',
  soLuong: 'Số lượng tồn',
  giaGoc: 'Giá gốc',
  giaBan: 'Giá bán',
  bienThes: 'Danh sách chi tiết sản phẩm',
  email: 'Email',
  matKhau: 'Mật khẩu',
  tenDangNhap: 'Tên đăng nhập',
  hoTen: 'Họ tên',
  sdt: 'Số điện thoại',
  cccd: 'CCCD'
}

function normalizeErrorText(value) {
  return String(value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[đĐ]/g, 'd')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, ' ')
    .trim()
}

function trimTrailingPunctuation(value) {
  return String(value ?? '').trim().replace(/[.:;!?]\s*$/, '')
}

function resolveFieldName(fieldKey) {
  return String(fieldKey ?? '')
    .replace(/\[\d+\]/g, '')
    .split('.')
    .filter(Boolean)
    .pop() || ''
}

function resolveFieldLabel(fieldKey) {
  return FIELD_LABELS[resolveFieldName(fieldKey)] || ''
}

export function translateValidationMessage(message, fieldKey = '') {
  const resolved = typeof message === 'string' ? message.trim() : ''
  if (!resolved) return ''

  const normalized = normalizeErrorText(resolved)
  const label = resolveFieldLabel(fieldKey)
  const subject = label || 'Giá trị'
  const subjectLower = label ? label.toLocaleLowerCase('vi-VN') : 'giá trị'

  if (
    normalized === 'must be greater than 0'
    || normalized.startsWith('must be greater than 0 ')
    || normalized.includes('must be greater than 0')
  ) {
    return `${subject} phải lớn hơn 0`
  }

  if (
    normalized.includes('must be greater than or equal to 0 01')
    || normalized.includes('must be greater than or equal to 1')
  ) {
    return `${subject} phải lớn hơn 0`
  }

  if (normalized.includes('must be greater than or equal to 0')) {
    return `${subject} không được âm`
  }

  if (normalized.includes('must be less than or equal to')) {
    return `${subject} không hợp lệ`
  }

  if (normalized.includes('must not be null')) {
    return label ? `Vui lòng nhập ${subjectLower}` : 'Vui lòng nhập đầy đủ thông tin bắt buộc'
  }

  if (normalized.includes('must not be blank') || normalized.includes('must not be empty')) {
    return label ? `Vui lòng nhập ${subjectLower}` : 'Vui lòng không để trống thông tin bắt buộc'
  }

  if (normalized.includes('must be a well formed email address')) {
    return 'Email không đúng định dạng'
  }

  if (normalized.includes('size must be between')) {
    return label ? `${subject} không đúng độ dài cho phép` : 'Thông tin nhập không đúng độ dài cho phép'
  }

  if (normalized.includes('numeric value out of bounds')) {
    return label ? `${subject} vượt quá giới hạn cho phép` : 'Giá trị số vượt quá giới hạn cho phép'
  }

  return resolved
}

function getMeaningfulErrorMessage(message, fieldKey = '') {
  const resolved = typeof message === 'string' ? message.trim() : ''
  if (!resolved || isGenericErrorMessage(resolved)) {
    return ''
  }
  return translateValidationMessage(resolved, fieldKey)
}

export function isGenericErrorMessage(message) {
  const normalized = normalizeErrorText(message)
  if (!normalized) return true
  return GENERIC_ERROR_PATTERNS.some((pattern) => pattern.test(normalized))
}

export function sanitizeErrorMessage(
  message,
  fallback = 'Không thể hoàn tất thao tác này lúc này. Vui lòng thử lại.',
  fieldKey = ''
) {
  const resolved = getMeaningfulErrorMessage(message, fieldKey)
  if (!resolved) {
    return fallback
  }
  return resolved
}

export function createRequestError(
  message,
  fallback = 'Không thể hoàn tất thao tác này lúc này. Vui lòng thử lại.',
  errors = null
) {
  const requestError = new Error(sanitizeErrorMessage(message, fallback))
  if (errors && typeof errors === 'object' && !Array.isArray(errors)) {
    requestError.errors = Object.fromEntries(
      Object.entries(errors).map(([key, value]) => [
        key,
        typeof value === 'string' ? sanitizeErrorMessage(value, value, key) : value,
      ])
    )
  }
  return requestError
}

export function getFieldErrors(error) {
  if (!error || typeof error !== 'object') {
    return {}
  }

  if (error.errors && typeof error.errors === 'object' && !Array.isArray(error.errors)) {
    return Object.fromEntries(
      Object.entries(error.errors).map(([key, value]) => [
        key,
        typeof value === 'string' ? sanitizeErrorMessage(value, value, key) : value,
      ])
    )
  }

  return {}
}

export function getDisplayErrorMessage(
  error,
  fallback = 'Không thể hoàn tất thao tác này lúc này. Vui lòng thử lại.'
) {
  let detail = ''

  if (typeof error === 'string') {
    detail = getMeaningfulErrorMessage(error)
  } else if (error instanceof Error) {
    detail = getMeaningfulErrorMessage(error.message)
  } else if (error && typeof error.message === 'string') {
    detail = getMeaningfulErrorMessage(error.message)
  }

  if (!detail) {
    return fallback
  }

  const normalizedFallback = normalizeErrorText(fallback)
  const normalizedDetail = normalizeErrorText(detail)

  if (
    !normalizedFallback
    || normalizedDetail === normalizedFallback
    || normalizedDetail.startsWith(normalizedFallback)
  ) {
    return detail
  }

  return `${trimTrailingPunctuation(fallback)}: ${detail}`
}
