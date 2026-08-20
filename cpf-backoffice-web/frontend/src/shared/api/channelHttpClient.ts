export type QueryValue = string | number | boolean | undefined
export type ApiOptions = { query?: Record<string, QueryValue>; body?: unknown }

export class BackofficeHttpError extends Error {
  readonly status: number
  readonly payload: unknown

  constructor(status: number, payload: unknown) {
    super(`Backoffice HTTP ${status}`)
    this.name = 'BackofficeHttpError'
    this.status = status
    this.payload = payload
  }
}

const baseUrl = (import.meta.env.VITE_MBW_WEB_BASE_URL ?? '').replace(/\/$/, '')
const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS'])

function cookie(name: string): string | undefined {
  const prefix = `${encodeURIComponent(name)}=`
  return document.cookie.split(';').map((value) => value.trim()).find((value) => value.startsWith(prefix))?.slice(prefix.length)
}

async function ensureCsrfToken(): Promise<string> {
  let token = cookie('XSRF-TOKEN')
  if (token) return decodeURIComponent(token)
  const response = await fetch(new URL(baseUrl + '/api/v1/backoffice/security/csrf', window.location.origin), {
    method: 'GET',
    credentials: 'include',
    headers: { Accept: 'application/json' },
  })
  if (!response.ok) throw new BackofficeHttpError(response.status, await response.text())
  const payload = await response.json() as { token?: string }
  token = cookie('XSRF-TOKEN')
  if (token) return decodeURIComponent(token)
  if (payload.token) return payload.token
  throw new Error('Backoffice CSRF token was not initialized')
}

export async function invokeBackoffice(method: string, path: string, options: ApiOptions = {}) {
  const normalizedMethod = method.toUpperCase()
  const url = new URL(baseUrl + path, window.location.origin)
  for (const [key, value] of Object.entries(options.query ?? {})) {
    if (value !== undefined) url.searchParams.set(key, String(value))
  }

  const csrfToken = SAFE_METHODS.has(normalizedMethod) ? undefined : await ensureCsrfToken()
  const response = await fetch(url, {
    method: normalizedMethod,
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      ...(options.body === undefined ? {} : { 'Content-Type': 'application/json' }),
      ...(csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : {}),
    },
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  const contentType = response.headers.get('content-type') ?? ''
  const payload = contentType.includes('application/json') ? await response.json() : await response.text()
  if (!response.ok) throw new BackofficeHttpError(response.status, payload)
  return payload
}
