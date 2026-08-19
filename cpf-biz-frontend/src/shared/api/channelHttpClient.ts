export type QueryValue = string | number | boolean | undefined
export type ApiOptions = { query?: Record<string, QueryValue>; body?: unknown }

export class BzaHttpError extends Error {
  readonly status: number
  readonly payload: unknown

  constructor(status: number, payload: unknown) {
    super(`BZA HTTP ${status}`)
    this.name = 'BzaHttpError'
    this.status = status
    this.payload = payload
  }
}

const baseUrl = (import.meta.env.VITE_BZA_CHANNEL_BASE_URL ?? '').replace(/\/$/, '')

export async function invokeBza(method: string, path: string, options: ApiOptions = {}) {
  const url = new URL(baseUrl + path, window.location.origin)
  for (const [key, value] of Object.entries(options.query ?? {})) {
    if (value !== undefined) url.searchParams.set(key, String(value))
  }

  const response = await fetch(url, {
    method,
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      ...(options.body === undefined ? {} : { 'Content-Type': 'application/json' }),
    },
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  const contentType = response.headers.get('content-type') ?? ''
  const payload = contentType.includes('application/json') ? await response.json() : await response.text()
  if (!response.ok) throw new BzaHttpError(response.status, payload)
  return payload
}
