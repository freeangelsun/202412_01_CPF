# CPF R8 Static Validation Report — 2026-07-25

- Baseline master: `512e5f2c7f32ba21ef6be570b2efa3dbcbd7a482`
- R8 overlay files at validation time: 152 (top-level APPLY/VERIFY/manifest excluded)
- Commit/push: 수행하지 않음

## 직접 수행한 정적 검증

| 검증 | 결과 | 비고 |
|---|---|---|
| Canonical Requirement matrix | PASS | 162 rows / 162 unique / Legacy Alias 0 / invalid status 0 |
| Overlay Java parser-level syntax | PASS | 69 Java files, parser-level syntax finding 0 |
| Pure Saga Java compile | PASS | Spring/JDBC 의존 없는 Saga runtime 12 files `javac` |
| ADM TS/Vue script parse | PASS | 30 TS/Vue script blocks, parse error 0 |
| BZA TS/Vue script parse | PASS | 30 TS/Vue script blocks, parse error 0 |
| ADM route ↔ menu coverage | PASS | 24 routes / 24 menu IDs / diff 0 / missing lazy page 0 |
| BZA lazy route coverage | PASS | 27 routes / 27 unique / missing lazy page 0 |
| External frontend runtime asset | PASS | CDN/remote CSS/font/script reference 0 |
| Legacy R7 coarse UI in overlay | PASS | stale artifact 0 |
| ADM Approval operator fail-closed | PASS | `adm.operatorId` required, `SYSTEM` fallback 0 |
| Heuristic secret scan | PASS | common credential/token pattern hit 0 |
| V40 checksum | PASS | manifest value equals actual SHA-256 |
| V41 checksum | PASS | manifest value equals actual SHA-256 |

## 이번 환경에서 직접 실행하지 못한 검증

아래는 **미검증**이며 PASS로 간주하지 않는다.

- Repository 전체 Gradle Java 25 compile/test/assemble
- 실제 Spring ApplicationContext bean wiring
- MariaDB Empty Install / Upgrade / V39~V41 migration / rollback
- `sync-database-artifacts.ps1`, drift/manifest PowerShell runtime
- ADM/BZA `npm test` 및 production build
- ADM/BZA Browser E2E
- Local/Remote BAT·Center-Cut ServiceCall runtime
- BAT 다중 인스턴스/lease/fencing/failover/fault injection
- Generated `external/EXS` create → DB → build/test/runtime → remove → regenerate parity

전체 overlay를 classpath 없이 `javac`로 읽으면 Spring/Jackson/Swagger 등 외부 의존성 unresolved symbol 오류가 발생한다. 이를 프로젝트 compile 실패나 성공 근거로 사용하지 않았으며, parser-level syntax만 별도로 판정했다.

## 통합 검증

R8 적용 후 다음 Runner가 static/build/DB/generator/browser를 한 번에 실행한다.

`cpf-tools/scripts/verify-full-product.ps1`

Runner는 PASS/FAIL/SKIPPED를 분리하고, `-RequireAll`에서는 SKIPPED도 성공으로 인정하지 않는다. 실행 로그는 민감정보를 정제한 Evidence로 `cpf-docs/work/review/20260725_02/evidence/` 아래에 자동 저장한다.
