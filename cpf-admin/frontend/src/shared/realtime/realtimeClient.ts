export interface CpfRealtimeEvent<T = unknown> {
  sequence: number
  eventId: string
  channel: string
  topic: string
  tenantId: string
  subjectId: string
  transactionId: string
  payload: T
  occurredAt: string
}

export interface CpfRealtimeSubscriptionOptions<T> {
  tenantId: string
  channel: string
  topic: string
  subjectId?: string
  streamPath?: string
  pollPath?: string
  pollIntervalMs?: number
  maxSseFailuresBeforePolling?: number
  parse?: (raw: string) => T
  onEvent: (event: CpfRealtimeEvent<T>) => void
  onModeChange?: (mode: 'sse' | 'poll') => void
  onError?: (error: unknown) => void
}

/** Typed same-origin SSE client. Browser Last-Event-ID reconnect is used first, bounded polling is fallback. */
export function subscribeCpfRealtime<T = unknown>(options: CpfRealtimeSubscriptionOptions<T>): () => void {
  const streamPath = options.streamPath ?? '/cpf/realtime/stream'
  const pollPath = options.pollPath ?? '/cpf/realtime/events'
  const pollIntervalMs = Math.max(1000, options.pollIntervalMs ?? 5000)
  const maxFailures = Math.max(1, options.maxSseFailuresBeforePolling ?? 3)
  const parse = options.parse ?? ((raw: string) => JSON.parse(raw) as T)
  let closed = false
  let failures = 0
  let lastEventId = ''
  const seenEventIds = new Set<string>()
  let source: EventSource | undefined
  let pollTimer: number | undefined

  const query = () => {
    const params = new URLSearchParams({ tenantId: options.tenantId, channel: options.channel, topic: options.topic })
    if (options.subjectId) params.set('subjectId', options.subjectId)
    return params
  }

  const deliver = (raw: string) => {
    const envelope = JSON.parse(raw) as Omit<CpfRealtimeEvent<T>, 'payload'> & { payload: unknown }
    if (seenEventIds.has(envelope.eventId)) return
    seenEventIds.add(envelope.eventId)
    if (seenEventIds.size > 2048) seenEventIds.clear()
    lastEventId = envelope.eventId
    const payload = typeof envelope.payload === 'string' ? parse(envelope.payload) : envelope.payload as T
    options.onEvent({ ...envelope, payload })
  }

  const poll = async () => {
    if (closed) return
    try {
      const params = query(); if (lastEventId) params.set('afterEventId', lastEventId); params.set('limit', '100')
      const response = await fetch(`${pollPath}?${params.toString()}`, { credentials: 'same-origin', headers: { Accept: 'application/json' } })
      if (!response.ok) throw new Error(`Realtime poll failed: ${response.status}`)
      const events = await response.json() as Array<CpfRealtimeEvent<T>>
      for (const event of events) {
        if (seenEventIds.has(event.eventId)) continue
        seenEventIds.add(event.eventId); lastEventId = event.eventId; options.onEvent(event)
      }
    } catch (error) { options.onError?.(error) }
    if (!closed) pollTimer = window.setTimeout(poll, pollIntervalMs)
  }

  const startPolling = () => {
    source?.close(); source = undefined
    if (pollTimer !== undefined) return
    options.onModeChange?.('poll')
    void poll()
  }

  const startSse = () => {
    const params = query()
    source = new EventSource(`${streamPath}?${params.toString()}`, { withCredentials: true })
    options.onModeChange?.('sse')
    source.onmessage = event => { failures = 0; deliver(event.data) }
    source.onerror = error => {
      failures += 1; options.onError?.(error)
      if (failures >= maxFailures) startPolling()
    }
  }

  startSse()
  return () => {
    closed = true; source?.close()
    if (pollTimer !== undefined) window.clearTimeout(pollTimer)
  }
}
