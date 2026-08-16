# CPF Common Code·Message·Parameter Catalog / Seed 상세 개발요건

> Currentization source/basis: `e6f2e7a599a948277b118967d0fb5f840f65c114` (`18_19`)  
> 다음 실행 시 최신 master 기준으로 Source/Canonical DB/Consumer를 다시 확인한다.

## 1. 목표

CPF 설치 직후 `Code / Message / Parameter`가 빈 껍데기가 아니어야 한다.
다만 고객 고유 업무값을 Framework가 임의 Seed해서도 안 된다.

목표:

```text
Canonical Catalog/Seed
→ Oracle / PostgreSQL / MariaDB
→ Fresh Install / Upgrade / Reapply
→ Common Service/API
→ ADM/BZA 관리 UI
→ 실제 Runtime Consumer
→ Audit/Evidence
```

## 1A. 18_19 실제 Seed Current-State 검수

`cpf-tools/db/canonical/seed-model.json`에는 이미 CMN_CODE/CMN_MESSAGE/CMN_RESPONSE_CODE/CMN_PARAMETER 계열 Seed가 다수 존재하므로 **Seed 개수를 늘리는 작업 자체를 목표로 하지 않는다.**

후속 개발은 다음을 검증/보완한다.

- Source enum/status/ADM/BZA/Batch/Notification/Incident/Download Consumer inventory와 Seed를 대조하여 실제 누락만 추가하고 중복/Dead Seed는 제거한다.
- `MODULE=REF`, `MREF*`처럼 구 Education/Reference 의미를 가진 Seed를 EDU target과 함께 currentize한다.
- `IN_MEMORY` fallback, sample-only feature default 등 현재 no-silent-fallback/production 정책과 충돌할 수 있는 legacy default를 Consumer까지 추적하여 제거/격리한다.
- Fresh install/upgrade/reapply/idempotency/customer modification protection/checksum/drift/DB3 lifecycle을 실제 실행한다.
- 각 Seed는 Runtime 또는 ADM/BZA/Generator Reserved Fallback의 실제 Consumer가 있어야 한다.
- Browser에서 Search/Paging/Detail/Create/Update/Disable/Version/Effective/Diff/Refresh/Audit/Permission을 검증한다.

## 2. Ownership

### Platform/Common Seed
`cpf-starters/common` 및 Platform DB의 `CMN_*` Owner가 관리할 범용 공통값.

### ADM
Platform Runtime/운영 설정, Framework reserved code/message/parameter를 운영자가 조회·변경해야 하는 경우 ADM에서 관리.

### BZA
고객 업무관리자가 관리할 Business/Common Catalog만 제공.
Platform의 위험 Runtime Parameter를 BZA에 노출하지 않는다.

## 3. Seed 대상 선정 규칙

Seed는 다음 4가지 조건을 모두 만족하는 항목을 우선한다.

1. 대부분의 고객 프로젝트에서 의미가 동일하다.
2. Framework Runtime/공통 기능이 실제 Consumer로 사용한다.
3. Secret/환경별 접속정보가 아니다.
4. 고객 업무정책을 강제로 고정하지 않는다.

## 4. Code 후보군

최신 Source와 실제 Consumer를 검색하여 최소 다음 계열을 검토한다.

- 활성/비활성.
- 처리상태.
- 성공/실패/부분실패/UNKNOWN.
- 승인 요청/승인/반려/만료/취소/회수.
- Audit Action.
- Log Level.
- Batch Job/Execution/Step 상태.
- Retry/Reconcile/Recovery 상태.
- Incident Severity/Status.
- Download/Attachment 처리상태.
- Notification Delivery 상태.
- Runtime Health/Drain/Maintenance 상태.

**고객 업무코드(상품종류, 회원등급 등)는 Seed 금지.**

## 5. Message 후보군

Framework 공통 Message:

- Validation required/format/range.
- Authentication required/expired.
- Authorization denied.
- Not Found.
- Duplicate.
- Optimistic conflict / 409.
- Rate limit / 429.
- Timeout.
- External unavailable.
- Retry exhausted.
- UNKNOWN result.
- Maintenance.
- File validation/security.
- Download denied/limit.
- Approval required/expired.
- Generic system failure.

다국어 Message는 `messageCode + locale + arguments`로 관리하고 Source에서 임의 문자열을 중복 Hardcoding하지 않는지 검수한다.

## 6. Parameter 후보군

Parameter는 **안전한 Default**만 Seed한다.

검토 예:

- 기본 Page Size.
- 최대 Page Size.
- Search limit.
- Export row/size limit.
- Operation timeout.
- Retry max attempt.
- Retry backoff/jitter default.
- Saved Search limit.
- UI Auto-refresh interval 범위.
- Audit/Log 조회 기본 기간.
- Attachment size limit의 Framework default.
- Batch/Transaction 조회 기간 기본값.

실제 값은 Source 요구와 운영안전을 검토해 결정한다.

금지:
- Password.
- Token.
- Secret.
- API Key.
- DB Credential.
- 환경별 URL/IP/Hostname.
- 고객 개인정보.
- 고객 업무정책을 강제하는 값.

## 7. 데이터 모델

각 Catalog는 가능한 범위에서 다음을 표현한다.

- code/key.
- name/message.
- description.
- category/group.
- locale.
- value/type.
- default.
- validation rule/range.
- status.
- effective from/to.
- version.
- owner.
- source.
- created/updated metadata.

실제 Canonical Schema와 충돌하면 기존 Schema를 우선하고 부족 필드는 Requirement Gap으로 기록한다.

## 8. Seed Lifecycle

- Fresh Install.
- Upgrade.
- Rollback 또는 Forward Recovery.
- Reapply/Idempotency.
- 기존 고객 수정값 보호.
- 신규 Seed 추가.
- Deprecated Seed 비활성화.
- Duplicate 방지.
- Checksum/Drift.

Seed SQL을 Vendor별 수작업 3벌의 정본으로 운영하지 않는다.
Canonical Source에서 DB3를 생성하고 Vendor Override는 불가피한 차이만 허용한다.

## 9. Consumer Gate

각 Seed 항목은 다음 중 하나가 있어야 한다.

- Runtime Consumer.
- ADM/BZA Consumer.
- Generator/Template Consumer.
- 명시적 Framework Reserved Fallback.

아무 Consumer도 없는 Seed는 제거 후보 또는 문서용 Sample로 분류한다.

## 10. ADM/BZA UI

- Search/Paging/Detail.
- Category/Status Filter.
- Create/Update/Disable.
- Validation.
- Version/Conflict.
- 변경 전/후 Diff.
- Effective range.
- Runtime Refresh.
- Refresh 성공/실패/Partial.
- Audit.
- Permission.
- 위험 Parameter는 Approval/Reason.

## 11. 검증

- Canonical duplicate.
- Code/Message/Parameter key duplicate.
- Locale missing.
- Source hardcoded duplicate.
- Runtime consumer.
- DB3 render parity.
- Fresh install.
- Upgrade/reapply.
- ADM/BZA OpenAPI/Generated Client.
- Browser UI.
- Secret/PII scan.

## 12. 완료

단순 Seed 파일 존재가 아니라 `DB → Service → Consumer → UI/Test`까지 연결돼야 완료다.
