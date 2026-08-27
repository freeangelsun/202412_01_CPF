# CPF Current Work Request — C 개발/QA 관리_1_7

## 1. Current Source Authority

- 기준 입력: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_195052.zip`
- 입력 ZIP SHA-256: `00abb643557a9562ff3aa40f088c8791af4e01d0cfb056e5509f70d146b90ec0`
- 현재 Git-independent Product Source Identity SHA-256: `79264c2975bd0b8504a0e2f8ec375070c08699ebcb512e26323d90d7e39490fb`
- Product Source: `8,334` files / `38,599,665` bytes
- 상위 제품 Requirement 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Canonical Product Requirement: **208개**
- 개발 Closure Inventory: **127/127 개발 완료**
- 검증 상태: **7 완료 / 120 미검증**. Java25/Windows/Docker/Browser/DB3 등 실환경 Runtime이 필요한 행은 정적 PASS와 분리하여 미검증으로 유지한다.
- 과거 PASS/CLOSED/Evidence는 현재 Source PASS 근거로 자동 승계하지 않는다.
- RT-02에서 Git은 Local history provenance read-only일 뿐 Source 정본이 아니다. baseline 부재는 `UNAVAILABLE`, current provenance는 Working Tree SHA-256이다.

## 2. 이번 개발에서 실제 보정한 핵심

1. **Compile/JDT blocker**: `CpfAttributes` java.util import, Comparator/generic inference, lambda shadowing, JPA Operator owner, CSRF/Header/Test import, messaging overload 등 실제 Source 오류를 Root Cause 단위로 보정했다.
2. **Logging**: Canonical MDC/File structured logging에 transaction/trace/correlation/execution/segment/System6/operationId lineage를 일치시켰다. 존재하지 않는 EDU Runtime probe를 실제 `/edu/online/member-processing` 거래로 교체하고 Header6/W3C trace를 사용한다.
3. **Security Context**: session raw ID 대신 hashed request-scope projection을 유지하고 stale verifier의 변수명 고정을 제거했다.
4. **Execution Scope**: repository 밖 Temp Evidence도 안전한 external identity로 검증하도록 path False Red를 제거했다.
5. **Generator**: member/external idempotency를 깨던 Generator-owned stale import를 Template Owner에서 수정했다. clean generation/idempotency/public boundary를 재검증했다.
6. **Batch Standalone Shell**: Control Plane/Scheduler/Worker/Agent/Center-Cut 각각 Windows/Linux run/stop, 총 20개 Shell을 추가했다.
7. **Batch Profile**: 5개 역할의 dev/test/prod 물리 Profile 15개를 구성하고 prod localhost/default credential 금지를 Gate로 고정했다.
8. **Open Git**: Projection/CLI/README stale 계약을 currentize하고 disposable staging Projection을 실제 생성·검증했다. Fresh Java25 binary publication/fresh remote clone runtime은 실환경 검증 대기다.
9. **DB3/RT-02**: Migration verifier가 Git SHA를 강제하지 않도록 Working Tree SHA-256을 authoritative provenance로 지원했다. Released V/R byte/baseline은 re-anchor하지 않았다.
10. **Performance Live**: broker/batch/resource trust boundary가 40-hex Git HEAD만 허용하던 결함을 수정하여 64-hex Working Tree SHA-256 attestation을 canonical로 검증한다. 40-hex는 명시적 legacy compatibility만 허용한다.
11. **Garbage**: Gradle plugin compiled `.class` 6개를 제품 Source garbage로 판정하여 Delete Manifest에 추가했고 transient pytest/generator evidence/cache는 결과 Source에서 제거했다.

## 3. Work Package 상태

| WP | 세부항목 | 개발 | 검증 |
|---|---:|---:|---:|
| WP-R00 | 7 | 7/7 | 정본/정적 완료 |
| WP-R01 | 10 | 10/10 | 실환경 Build/Runtime 대기 포함 |
| WP-R02 | 8 | 8/8 | VSCode Java25 Fresh Import 0/0 대기 |
| WP-R03 | 6 | 6/6 | 정적 회귀 완료, Full Harness 실환경 재실행 대기 |
| WP-R04 | 7 | 7/7 | Generator 정적/Lifecycle 완료, Java25 generated build 대기 |
| WP-R05 | 8 | 8/8 | Shell 실물/계약 완료, Windows/Linux 물리 기동 대기 |
| WP-R06 | 7 | 7/7 | Profile 실물/계약 완료, role별 effective Runtime 대기 |
| WP-R07 | 10 | 10/10 | Projection 완료, Fresh binary release/Golden Path Runtime 대기 |
| WP-R08 | 8 | 8/8 | Zero-footprint/contract 완료, full mutation Runtime 대기 |
| WP-R09 | 6 | 6/6 | Security contract 완료, full auth/session Runtime 대기 |
| WP-R10 | 8 | 8/8 | DB3 static/lifecycle contract 완료, 3-vendor physical Runtime 대기 |
| WP-R11 | 10 | 10/10 | Batch static/contract 완료, 2-worker/kill/UNKNOWN physical Runtime 대기 |
| WP-R12 | 8 | 8/8 | Logging/OpenAPI contract 완료, One-WAS/Browser/Performance Live 대기 |
| WP-R13 | 7 | 7/7 | ADM/Backoffice consumer/route/approval 완료, Node22.18+/Browser 대기 |
| WP-R14 | 7 | 7/7 | UTF-8 static/path/garbage 완료, Windows console/log 물리 검증 대기 |
| WP-R15 | 10 | 10/10 개발처리 | 최종 Full Runtime/Fresh Replay/Codex 독립검수 대기 |

개발 진행률은 **127/127 = 100%**다. 전체 QA 완료율과 동일한 의미가 아니며, 필수 실환경 Acceptance가 남아 있으므로 Overall은 완료가 아니다.

## 4. 비협상 검증 규칙

- 모든 Static/Unit/Contract/Build/DB/Runtime/Browser/Performance Test는 **고강도**다.
- Targeted/Smoke/DRY_RUN은 진단 보조일 뿐 최종 Closure 근거가 아니다.
- 정상/오류/경계/null/empty/duplicate/timeout/retry/concurrency/race/partial failure/process kill/restart/UNKNOWN/reconcile/idempotency/provider down/reconnect/security/approval/audit/cleanup/rerun/fresh replay를 영향 범위에 맞게 포함한다.
- Source 변경은 Consumer, Test, Config, SQL, Generator, Frontend, OpenAPI, 운영 문서와 동시에 currentize한다.
- `FAIL / mandatory SKIP / NOT_EXECUTED / UNKNOWN / Source·Managed drift / Evidence mismatch`가 하나라도 있으면 QA 완료가 아니다.

## 5. 다음 필수 물리 검증

1. Java25 + Gradle9.1 clean root build/test/publication/SBOM + Generated Domain build.
2. Windows VSCode Java25 Fresh Gradle Import/JDT: **Error 0 / Warning 0**.
3. Oracle/PostgreSQL/MariaDB Fresh→Seed→Verify→Runtime→Upgrade→Rollback→Reapply→Fault→Cleanup→Fresh Replay.
4. Kafka-free Batch 5-role + Worker×2 + kill/takeover/fencing/UNKNOWN/reconcile/Center-Cut/Gateway.
5. One-WAS real transaction + File Log↔DB Log↔Trace↔ADM transaction/timeline correlation.
6. ADM/Backoffice Runtime OpenAPI + Node22.18+ lint/typecheck/test/build + Browser E2E/a11y/error statuses.
7. Performance Live broker/batch/resource probes with signed `sourceIdentitySha256` attestation and required load/soak.
8. Windows PowerShell/Java/Gradle/Docker/Tee/log UTF-8/mojibake 0.
9. Actual Open Git Fresh Binary Release + fresh remote clone-equivalent Golden Path.
10. 동일 Source Fresh Replay 후 `FAIL=0`, mandatory `SKIP_ENV=0`, `NOT_EXECUTED=0`, unresolved `UNKNOWN=0`, Source/Managed drift=0.

## 6. Current-only / Garbage

- 이 파일과 `CPF_CANONICAL_DEVELOPMENT_CLOSURE_INVENTORY.csv`가 Developer 작업 정본이다.
- `CODEX_FINDING_CLOSURE.csv`는 Codex carry-over 원장으로 유지하며 Developer GPT가 상태를 수정하지 않는다.
- superseded narrative는 `DELETE_MANIFEST.csv`에서 대체 정본/참조 전환 조건을 갖춘 뒤 삭제한다.
- transient cache/build/generated test output은 final product Source에 남기지 않는다.

## 7. Codex / Claude LONG-TURN MODE 및 병행 세션 규칙

- `CPF_EXTERNAL_AGENT_LONG_TURN_POLICY.md`를 Codex/Claude 요청서 최상단 비협상 규칙으로 항상 포함한다.
- 외부 에이전트가 이미 하던 작업을 **먼저 완전히 종결**한 뒤 남은 크레딧으로 필수 독립검증만 수행한다.
- 기존 미완료 Finding 전체/Repository 전체 전수 재검수 지시 금지.
- 기본 추가 검증 우선순위는 Build/Compile → Logging 실제 추적 → DB3 → Batch → Generator → Performance/Open Git이다.
- ADM/Backoffice/Frontend/Browser는 최후순위다.
- 계획/중간보고/진행률은 중단점이 아니며 현재 WP 완결 전 자의로 turn을 종료하거나 다음 진행 여부를 묻지 않는다.
- 같은 Root Cause/Build/DB Fresh/Runtime은 가능한 한 한 번의 연속 turn에서 묶어 처리한다.
- 크레딧이 부족해지면 신규 범위를 열지 말고 진행 중 WP를 Source/Test/Runtime/Evidence/문서 현행화까지 먼저 완결한다.
- Git/HEAD/전체 Local Working Tree/전체 Source Identity를 작업 시작·적용·완료 차단 Gate로 쓰지 않는다.
- 다른 세션 변경을 조사·복구·초기화하지 않고 각 에이전트 자신의 변경 범위만 처리한다.
- RT-02/Performance 제품 기능 자체의 provenance 검증은 해당 기능 계약 안에서만 수행한다.
