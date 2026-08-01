# CPF QA37 통합 자체 개발요건

기준 SHA: `1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04`

QA37 64건과 SELF 30건은 공통 원인 기준으로 병합한다. Root Build, False Green Source Closure, Consumer 없는 Bulk Mapping, Generator/DB Drift, Frontend Fresh Clone, exact-SHA Evidence를 선행 공통 원인으로 처리한다.

## 필수 자체 요건

- QA37-SELF-001: EDU135 Handler는 durable repository와 등록된 Concrete Consumer Binding을 가져야 한다.
- QA37-SELF-002: Capability는 제목·고객 시나리오 기준으로 분류하고 반복 공통 문구로 외부전송·보상을 과다 매핑하지 않는다.
- QA37-SELF-003: Package는 기능 중심으로 구성하며 Requirement ID 숫자 Package를 금지한다.
- QA37-SELF-004: EDU는 생성형 도메인과 제품 BZA를 참조하지 않는다.
- QA37-SELF-005: Batch는 `com.cpf.reference.batch`와 `CPF_REF_BAT_*` Pack으로 묶어 통째 제거 가능해야 한다.
- QA37-SELF-006: Query/Schema 변경은 Canonical Source, 3 Vendor, V93/V94 Upgrade, U93/U94 Rollback, Runtime Query, Verify, Checksum, Generator 제외 계약을 함께 변경한다.
- QA37-SELF-007: Frontend Verify 순서는 Source OpenAPI → Generate → Generated Contract → Consumer → Quality로 고정한다.
- QA37-SELF-008: Overlay PASS, merged Source PASS, Runtime PASS, exact-SHA Evidence를 구분한다.
- QA37-SELF-009: stale 검수 요청서와 완료 보고서는 Delete Manifest로만 관리하고 자동 삭제하지 않는다.

수용 결과는 `cpf-docs/quality/CPF_20260801_QA37_SELF_DEVELOPMENT_RESULT_MATRIX.csv`에 기록한다.
