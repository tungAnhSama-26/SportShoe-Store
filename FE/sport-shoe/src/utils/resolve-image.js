import { API_BASE_URL } from '../services/api-client'

const apiOrigin = API_BASE_URL.replace(/\/api\/v1\/?$/, '')

/**
 * Resolve a potentially relative image URL to an absolute URL.
 * - Strips localhost/127.0.0.1 host prefix → relative path → Vite proxy handles it (avoids CSP issues)
 * - Handles /uploads/... paths, bare paths, and absolute https:// URLs
 */
export function resolveHinhAnh(url) {
  const value = String(url || '').trim()
  if (!value) return ''

  // Absolute URLs pointing to local backend → strip host → let Vite proxy handle it
  if (/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i.test(value)) {
    return value.replace(/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i, '')
  }

  // Already a valid absolute URL (https/data/blob) → use as-is
  if (/^(https?:|data:|blob:)/i.test(value)) return value

  // Relative paths — already suitable for Vite proxy
  if (value.startsWith('/uploads/') || value.startsWith('/assets/')) return value
  if (value.startsWith('uploads/')) return `/${value}`

  // Anything else — try prefixing with apiOrigin if available
  return apiOrigin ? `${apiOrigin}${value.startsWith('/') ? '' : '/'}${value}` : value
}
