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

function normalizeErrorText(value) {
  return String(value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, ' ')
    .trim()
}

function trimTrailingPunctuation(value) {
  return String(value ?? '').trim().replace(/[.:;!?]\s*$/, '')
}

function getMeaningfulErrorMessage(message) {
  const resolved = typeof message === 'string' ? message.trim() : ''
  if (!resolved || isGenericErrorMessage(resolved)) {
    return ''
  }
  return resolved
}

export function isGenericErrorMessage(message) {
  const normalized = normalizeErrorText(message)
  if (!normalized) return true
  return GENERIC_ERROR_PATTERNS.some((pattern) => pattern.test(normalized))
}

export function sanitizeErrorMessage(
  message,
  fallback = 'Không thể hoàn tất thao tác này lúc này. Vui lòng thử lại.'
) {
  const resolved = getMeaningfulErrorMessage(message)
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
    requestError.errors = errors
  }
  return requestError
}

export function getFieldErrors(error) {
  if (!error || typeof error !== 'object') {
    return {}
  }

  if (error.errors && typeof error.errors === 'object' && !Array.isArray(error.errors)) {
    return error.errors
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
