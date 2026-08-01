const DEFAULT_COLOR_HEX = '#94A3B8'
const SIZE_PREFIXES = ['EU']
const SIZE_VALUE_PATTERN = /^(?:(EU)\s*)?(\d{1,2})(?:([.]5))?$/i

const COLOR_PRESETS = [
  { name: 'Đen', keywords: ['den', 'black'], hex: '#111827', r: 17, g: 24, b: 39 },
  { name: 'Trắng', keywords: ['trang', 'white'], hex: '#FFFFFF', r: 255, g: 255, b: 255 },
  { name: 'Xám nhạt', keywords: ['xam nhat', 'ghi nhat', 'light gray'], hex: '#D1D5DB', r: 209, g: 213, b: 219 },
  { name: 'Xám', keywords: ['xam', 'ghi', 'gray', 'grey'], hex: '#6B7280', r: 107, g: 114, b: 128 },
  { name: 'Xám đậm', keywords: ['xam dam', 'ghi dam', 'dark gray'], hex: '#374151', r: 55, g: 65, b: 81 },
  { name: 'Đỏ', keywords: ['do', 'red'], hex: '#DC2626', r: 220, g: 38, b: 38 },
  { name: 'Đỏ đậm', keywords: ['do dam', 'do do', 'burgundy', 'maroon'], hex: '#991B1B', r: 153, g: 27, b: 27 },
  { name: 'Hồng', keywords: ['hong', 'pink', 'rose'], hex: '#EC4899', r: 236, g: 72, b: 153 },
  { name: 'Hồng nhạt', keywords: ['hong nhat', 'light pink'], hex: '#FBCFE8', r: 251, g: 207, b: 232 },
  { name: 'Tím', keywords: ['tim', 'purple', 'violet'], hex: '#7C3AED', r: 124, g: 58, b: 237 },
  { name: 'Xanh dương', keywords: ['xanh duong', 'xanhduong', 'blue', 'xanh lam'], hex: '#2563EB', r: 37, g: 99, b: 235 },
  { name: 'Xanh đen', keywords: ['xanh den', 'navy', 'dark blue'], hex: '#1E3A8A', r: 30, g: 58, b: 138 },
  { name: 'Xanh lá', keywords: ['xanh la', 'xanhla', 'green'], hex: '#16A34A', r: 22, g: 163, b: 74 },
  { name: 'Xanh rêu', keywords: ['xanh reu', 'olive'], hex: '#4D7C0F', r: 77, g: 124, b: 15 },
  { name: 'Xanh ngọc', keywords: ['xanh ngoc', 'turquoise', 'cyan'], hex: '#06B6D4', r: 6, g: 182, b: 212 },
  { name: 'Vàng', keywords: ['vang', 'yellow', 'gold'], hex: '#EAB308', r: 234, g: 179, b: 8 },
  { name: 'Cam', keywords: ['cam', 'orange'], hex: '#F97316', r: 249, g: 115, b: 22 },
  { name: 'Nâu', keywords: ['nau', 'brown'], hex: '#8B5E3C', r: 139, g: 94, b: 60 },
  { name: 'Be', keywords: ['be', 'beige', 'kem', 'ivory'], hex: '#E7D3A8', r: 231, g: 211, b: 168 },
  { name: 'Bạc', keywords: ['bac', 'silver'], hex: '#94A3B8', r: 148, g: 163, b: 184 }
]

export function normalizeAttributeText(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .replace(/\s+/g, ' ')
    .trim()
}

export function normalizeRequiredText(value) {
  return String(value ?? '').trim()
}

export function normalizeOptionalText(value) {
  const normalized = normalizeRequiredText(value)
  return normalized || null
}

export function exceedsMaxLength(value, maxLength) {
  return normalizeRequiredText(value).length > maxLength
}

export function isInvalidLength(value, min = 4, max = 100) {
  const len = normalizeRequiredText(value).length
  return len < min || len > max
}

export function validateSearchKeyword(keyword) {
  const raw = String(keyword ?? '')
  if (raw.length > 0 && raw.trim() === '') {
    return { valid: false, error: 'Từ khóa tìm kiếm không được chỉ chứa khoảng trắng', keyword: '' }
  }
  return { valid: true, error: '', keyword: raw.trim() }
}


export function isValidWebsiteUrl(value) {
  const normalized = normalizeOptionalText(value)

  if (!normalized) {
    return true
  }

  try {
    const url = new URL(normalized)
    return ['http:', 'https:'].includes(url.protocol) && Boolean(url.host)
  } catch {
    return false
  }
}

export function hasSpecialCharacters(value) {
  if (!value) return false
  const str = String(value)
  
  // Phải có ít nhất 1 chữ cái hoặc số (chặn chuỗi chỉ toàn dấu phẩy, chấm, khoảng trắng, v.v)
  const normalized = normalizeAttributeText(str)
  if (!/[a-zA-Z0-9]/.test(normalized)) {
    return true
  }
  
  const specialCharsRegex = /[@#$%^*+=\\{}\[\]<>;|~`]/
  return specialCharsRegex.test(str)
}

export function createAttributeCodeSeed() {
  return `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, '')
    .slice(-4)
    .padStart(4, '0')
}

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max)
}

function hslToHex(hue, saturation, lightness) {
  const normalizedSaturation = saturation / 100
  const normalizedLightness = lightness / 100
  const chroma = (1 - Math.abs(2 * normalizedLightness - 1)) * normalizedSaturation
  const secondary = chroma * (1 - Math.abs((hue / 60) % 2 - 1))
  const match = normalizedLightness - chroma / 2

  let red = 0
  let green = 0
  let blue = 0

  if (hue < 60) {
    red = chroma
    green = secondary
  } else if (hue < 120) {
    red = secondary
    green = chroma
  } else if (hue < 180) {
    green = chroma
    blue = secondary
  } else if (hue < 240) {
    green = secondary
    blue = chroma
  } else if (hue < 300) {
    red = secondary
    blue = chroma
  } else {
    red = chroma
    blue = secondary
  }

  const toHex = (channel) => Math.round((channel + match) * 255).toString(16).padStart(2, '0')
  return `#${toHex(red)}${toHex(green)}${toHex(blue)}`.toUpperCase()
}

export function isValidHexColor(value) {
  return /^#[0-9A-F]{6}$/i.test(String(value || '').trim())
}

export function getColorNameFromHex(hexColor) {
  if (!isValidHexColor(hexColor)) return ''
  const hex = String(hexColor).trim().replace('#', '')
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)

  let closest = null
  let minDistance = Infinity

  for (const c of COLOR_PRESETS) {
    if (!c.r && c.r !== 0) continue
    const dist = Math.sqrt(
      2 * Math.pow(r - c.r, 2) +
      4 * Math.pow(g - c.g, 2) +
      3 * Math.pow(b - c.b, 2)
    )
    if (dist < minDistance) {
      minDistance = dist
      closest = c
    }
  }

  return closest ? closest.name : ''
}

export function generateHexColorFromText(value) {
  const normalized = normalizeAttributeText(value).toLowerCase()

  if (!normalized) {
    return DEFAULT_COLOR_HEX
  }

  const preset = COLOR_PRESETS.find((item) =>
    item.keywords.some((keyword) => normalized.includes(keyword))
  )

  if (preset) {
    return preset.hex
  }

  let hash = 0
  for (const character of normalized) {
    hash = character.charCodeAt(0) + ((hash << 5) - hash)
    hash |= 0
  }

  const hue = Math.abs(hash) % 360
  const saturation = 58 + (Math.abs(hash >> 8) % 24)
  const lightness = 42 + (Math.abs(hash >> 16) % 20)

  return hslToHex(hue, saturation, lightness)
}

export function generateColorAttributeCode(value, seed = '') {
  return generateAttributeCode(value, 'MS', 'MAU', seed)
}

export function generateAttributeCode(value, prefix = 'DM', fallback = 'ITEM', seed = '') {
  const normalized = normalizeAttributeText(value)
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, '')
    .slice(0, 6)
  const normalizedFallback = String(fallback || 'ITEM')
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, '')
    .slice(0, 6) || 'ITEM'

  const suffix = String(seed || createAttributeCodeSeed())
    .replace(/[^A-Z0-9]/g, '')
    .slice(-4)
    .padStart(4, '0')

  return `${prefix}${normalized || normalizedFallback}${suffix}`
}

export function generateWeightAttributeCode(value, seed = '') {
  const normalized = String(value ?? '')
    .replace(/\D/g, '')
    .slice(0, 4)

  return generateAttributeCode(normalized ? `${normalized}G` : '', 'TL', 'GRAM', seed)
}

export function normalizeSizeValue(value) {
  const rawValue = String(value || '')
    .trim()
    .toUpperCase()
    .replace(/,/g, '.')
    .replace(/\s+/g, ' ')

  if (!rawValue) {
    return ''
  }

  const matched = rawValue.match(SIZE_VALUE_PATTERN)
  if (!matched) {
    return ''
  }

  const prefix = matched[1] ? `${matched[1].toUpperCase()} ` : ''
  const baseValue = clamp(Number(matched[2]), 0, 999)

  if (!Number.isFinite(baseValue) || baseValue < 1 || baseValue > 60) {
    return ''
  }

  const suffix = matched[3] || ''
  return `${prefix}${baseValue}${suffix}`.trim()
}

export function isValidSizeValue(value) {
  return Boolean(normalizeSizeValue(value))
}

export { DEFAULT_COLOR_HEX, SIZE_PREFIXES }
