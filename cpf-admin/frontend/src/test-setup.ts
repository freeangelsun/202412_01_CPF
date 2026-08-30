/**
 * Vitest(jsdom) 전용 환경 보정.
 *
 * jsdom은 window.matchMedia를 구현하지 않는다. 실제 브라우저에는 항상 존재하는 표준 API이므로
 * 제품 코드에 부재 방어 분기를 넣지 않고, 테스트 환경에서만 표준 동작을 제공한다.
 * 여기에는 제품 동작을 바꾸는 stub을 두지 않는다.
 */
if (typeof window !== "undefined" && typeof window.matchMedia !== "function") {
  window.matchMedia = ((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addEventListener: () => undefined,
    removeEventListener: () => undefined,
    addListener: () => undefined,
    removeListener: () => undefined,
    dispatchEvent: () => false
  })) as typeof window.matchMedia;
}
