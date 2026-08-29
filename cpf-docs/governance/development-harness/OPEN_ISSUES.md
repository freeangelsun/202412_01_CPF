# CPF OPEN ISSUES — Development Harness Current

## 1. Product Conformance

- 이전 11건의 Product Conformance Finding은 Source에서 보정했고 현재 `PRODUCT_CONFORMANCE=PASS FINDINGS=0`이다.
- 해당 Finding을 삭제·은폐한 것이 아니라 `WP-CF01` Source Closure 및 Current Test/Evidence에 반영했다.

## 2. Mandatory Physical / Independent Verification 미완료

1. `WP-B01` — Java25 Root Build/Test/Publication/SBOM: **BLOCKED_EXTERNAL** (현재 Java21).
2. `WP-B02` — Fresh VS Code/Buildship Error=0 Warning=0: **BLOCKED_EXTERNAL** (Windows IDE 필요).
3. `WP-B03` — Messaging/JMS Java25 compile + Fresh JDT + provider runtime parity: **BLOCKED_EXTERNAL**.
4. `WP-DB01` — Oracle/PostgreSQL/MariaDB Physical Full Lifecycle: **BLOCKED_EXTERNAL** (Docker 없음).
5. `WP-CLI01` — Windows/Linux Unified CLI/Generator actual lifecycle: **BLOCKED_EXTERNAL** (`pwsh`/Java25 없음).
6. `WP-BAT01` — 5-role/Worker×2/kill/takeover/fencing/UNKNOWN/reconcile: **BLOCKED_EXTERNAL**.
7. `WP-ONE01` — One-WAS transaction/log durability/runtime OpenAPI: **BLOCKED_EXTERNAL**.
8. `WP-FE01` — npm install/lint/typecheck/test/build/Playwright/browser/a11y/error states: **BLOCKED_EXTERNAL**.
9. `WP-PF01` — live load/soak/resource/backpressure: **BLOCKED_EXTERNAL**.
10. `WP-RL02` — Actual Open Git Fresh Release/Fresh Consumer/leakage 0: **BLOCKED_EXTERNAL**.
11. `WP-FIN01` — Same Source Full Runtime/Fresh Replay: **BLOCKED_EXTERNAL** until above prerequisites are satisfied.
12. Independent Reviewer(Codex/Claude): **NOT_EXECUTED**.
13. QA final acceptance: **NOT_EXECUTED**.

## 3. Delete / Current-only

- Migration Semantic Closure: **PASS 265/265**.
- Exact Delete Manifest: 265 rows / delete eligible 246 / protected retain 19.
- 실제 사용자 Repository 삭제 적용은 **NOT_EXECUTED**이며 Overlay 적용 후 Harness Gate PASS 상태에서만 실행한다.

Static/Contract PASS로 위 Physical Gate를 대체하지 않는다.
