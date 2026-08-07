# Codex Independent Review Request — CPF R6S12

## Basis
- instruction basis: `28f823a18eca859cebdbceb382029f595cdf490c`
- review target: Overlay 적용 후 사용자가 생성한 exact result SHA
- scope: `QA-R5I-001~029`, `FDEV-001~025` 전부

## Mandatory review
1. 변경 Source/API/SPI/Internal ownership과 Consumer 전체 호출 경로.
2. policy tuple/effective/version/break-glass, sensitive response, HMAC capability, snapshot hash/single-use.
3. duplicate JSON/BigInteger/BigDecimal/null/CAS/concurrent replay/A→B→A/multi-tab.
4. HTTP 201/200/409/422, runtime OpenAPI/generated client/UI parity.
5. DB3 lifecycle 및 runner secret/timeout/child environment.
6. Spring disabled/default/customer override/missing/duplicate provider Context.
7. Java25/Gradle9.1 build/test/publication, npm verify, Playwright, broker/multiprocess.
8. QA38/QA39/REV004 mutation, Evidence/Manifest/Hash/protected delete.

미실행 항목은 PASS 금지. Codex 영역과 Codex Evidence만 갱신하고 exact result SHA, command, environment, start/end, exit code, sanitized output hash를 기록한다. Remaining finding이 하나라도 있으면 QA R6 재검수 전 `미완료`로 반환한다.
