# CPF QA37 Codex Docker 통합 Continuity

## 이전 상태

QA37 Source와 문서가 원격에 반영됐지만 Java 25, Frontend, DB 3종,
Runtime, Browser, Supply-chain은 최종 SHA 기준으로 검증되지 않았다.

## 이번 변경

Codex가 준비된 Docker 환경을 이용해 Fresh DB 생성부터 장애·복구,
관측성, Browser, Supply-chain까지 검증하고 실제 결함을 보완하도록
단일 실행 요청서를 구성했다.

## 다음 상태 전이

```text
미검증
→ Read-only Baseline
→ Low-cost Gate
→ Java
→ DB 3 Vendor
→ Runtime/Fault/Recovery
→ Frontend/Browser
→ Supply-chain
→ exact-SHA Evidence
→ 완료 또는 실제 실패 상태
```

환경 부족은 완료가 아니라 `미검증`이며, Source 결함은 보완 개발 후 재검증한다.
