# CPF 영향도 분석·변경 Closure 표준

## 1. 모든 변경의 고정 순서

모든 Requirement와 Finding은 **변경 전 Impact Review → 구현 → 변경 후 Impact Replay → Targeted Test → Side Effect/Regression → Required Runtime → Evidence → Closure Review** 순서를 지킨다. 어느 단계도 "해당 없음"을 근거 없이 기입할 수 없다.

### 변경 전 Impact Review 필수 축
- Owner/Architecture: cpf-core/common/admin/backoffice/batch/gateway/starter/generated-domain 경계와 dependency direction.
- Public API/SPI/Internal: binary/source compatibility, deprecation, consumer migration, JavaDoc.
- Consumer: same JVM/remote/HTTP/message/batch/frontend/CLI/operator/generator/sample/EDU.
- Data: Canonical DB source, Oracle/PostgreSQL/MariaDB render, migration, seed, index/FK, runtime query, upgrade, rollback/recovery.
- Config: common + local/dev/stg/test/prod, secret boundary, default/override precedence, configuration metadata.
- Runtime: single/multi instance, process kill, retry, idempotency, concurrency, timeout, UNKNOWN, reconcile.
- Security: authN/authZ, permission/data scope, approval/reason, audit, masking, secret, trust boundary.
- Observability: transaction/trace/span/log/metric/history/timeline and instance/system correlation.
- Delivery: generator, generated output, OpenAPI/generated client, frontend, sample/EDU, bootstrap, public release, SBOM/provenance.
- User quality: task discoverability, error recovery, accessibility, operational diagnostics, readable config/message/docs.

## 2. 변경 후 검증
변경 전 식별한 모든 영향 축을 동일 ID로 재검증한다. `impact_before`가 있는데 `impact_after`가 비거나 결과가 없는 경우 Closure 금지. 새로운 영향이 발견되면 원 Work Item을 확장하거나 신규 하위 Work Item을 등록한다.

## 3. 완료 근거
Closure는 다음 증거가 모두 현재 Source Identity에 연결될 때만 가능하다: 변경 파일/Consumer 목록, 실제 실행 명령, 환경, exit code, test result, runtime result, evidence path/hash, regression result, 완료 사유. Partial/Skipped/Unknown/Not Executed는 PASS가 아니다.

## 4. 재발 방지
동일 Root Cause가 재발하면 Source만 다시 고치지 않고 Test/Verifier/Negative Fixture/Gate를 강화한다. 이전 CLOSED가 다시 열리면 이전 Closure 누락 원인을 별도 필드에 기록한다.
