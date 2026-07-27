# CPF ADM/BZA Data Safety 운영·개발 Guide

## 1. 목적

이 Guide는 ADM 운영자와 BZA 업무 관리자/직원의 인증 Identity, Directory/Profile, 상태, 개인정보, 감사, DB 오류 처리와 Migration 운영 기준을 설명한다.

## 2. ADM Persistence Mode

### DATABASE — 제품 기본

```yaml
cpf:
  adm:
    persistence:
      mode: DATABASE
```

- 제품 기본값이다.
- 필수 ADM DB가 없으면 기동 검증에 실패한다.
- Runtime DB 오류를 Memory 성공으로 바꾸지 않는다.
- Readiness에서 ADM DB 장애를 `DOWN`으로 표시한다.

### MEMORY — 명시적 Demo/Test

```text
CPF_ADM_PERSISTENCE_MODE=MEMORY
```

- `local`, `test`, `demo`, `library` Profile에서만 허용한다.
- 실제 DB Connection을 사용하지 않는 Demo 저장소다.
- Product/Prod에서는 사용할 수 없다.
- 운영 Evidence로 인정하지 않는다.

## 3. ADM 운영자 Lifecycle

### 일반 생성

1. `operationId` 필수
2. Identity 생성
3. Profile/연락처 저장
4. 명시 Role Mapping 저장
5. 전체 작업은 `admTransactionManager` 경계
6. 초기 상태 `PENDING_ACTIVATION`
7. Role 자동 부여 없음
8. 비밀번호 변경 필요 상태

동일 `operationId` + 동일 운영자 재시도는 동일 결과를 반환한다. 다른 운영자가 같은 `operationId`를 사용하면 충돌한다.

### Bootstrap

최초 설치의 명시적 Bootstrap만 일반 생성과 분리한다. Bootstrap은 별도 환경변수/보안 정책으로 통제하며 `ACTIVE + ADM_ADMIN` 예외를 가질 수 있다.

### 상태

- `PENDING_ACTIVATION`
- `ACTIVE`
- `LOCKED`
- `SUSPENDED`
- `DISABLED`

Role 없는 계정을 `ACTIVE`로 전환하지 않는다. 잠금 해제는 `LOCKED`만 대상으로 한다.

## 4. ADM Identity/Profile Ownership

`adm_operator`:
- Login/Credential metadata
- Account status
- Failure/lock state
- Version/idempotency

`adm_operator_profile`:
- Display name
- Mobile
- Office phone
- Directory/Profile data

연락처 장애가 인증 Identity의 의미를 변경해서는 안 된다.

## 5. BZA 상태 Catalog

### 직원 재직상태

- `EMPLOYED`
- `ON_LEAVE`
- `SECONDMENT`
- `DISPATCHED`
- `RETIRED`
- `TERMINATED`

과거 `ACTIVE`는 재직상태 신규 입력값으로 허용하지 않는다. V61 Migration에서 명시적으로 `EMPLOYED`로 정규화한다.

### 관리자 계정상태

- `PENDING_ACTIVATION`
- `ACTIVE`
- `LOCKED`
- `SUSPENDED`
- `DISABLED`

신규 BZA 관리자는 `PENDING_ACTIVATION`, Role 미부여가 기본이다. Role은 User-Role 이력 정본에서 명시 부여한 뒤 활성화한다.

## 6. 개인정보 계약

### 기본 Projection

목록/일반 상세는 Masked 값만 반환한다.

예:

```text
mobile: ***-****-5678
email : u***@example.com
```

### Raw 조회

Raw PII는 다음을 모두 만족해야 한다.

- 별도 `PII_RAW` 권한
- 사용자가 입력한 사유
- `transactionId`
- 대상 식별자
- Immutable/Business Audit
- HTTP `POST` body로 사유 전달
- `Cache-Control: no-store`

사유를 query string에 넣지 않는다. Proxy access log, Browser history와 URL telemetry에 사유가 남는 것을 방지하기 위함이다.

## 7. 입력/NULL 규칙

- 전화번호는 숫자형이 아니라 String
- `+`, 선행 0, Extension 보존
- 제어문자 금지
- Blank 입력은 신규 저장에서 NULL
- 수정 화면 Blank는 기존값 보존
- 값 삭제는 명시 `clear*` Flag
- 문자열 `"null"`, `N/A`, `0`을 누락값 대용으로 저장하지 않음

## 8. Audit Redaction

BZA Audit canonical snapshot은 저장 전 Field Classification을 적용한다.

- email/mobile/phone/contact → `[MASKED]`
- password/secret/token/credential/attachment → `[REDACTED]`

Hash chain은 Redaction 이후 canonical snapshot을 대상으로 계산한다.

## 9. BZA Query Ownership

BZA Repository/Service에 Vendor SQL literal을 두지 않는다.

```text
BZA Java Consumer
 → com.cpf.core.api.database.CpfVendorSqlCatalog
 → cpf-tools/db/vendor/<vendor>/runtime/bza/repository/*.sql
```

BZA/ADM/Generated Domain이 `com.cpf.core.common.*`를 직접 import하지 않는다.

## 10. MariaDB V61

Forward:
- ADM account status/version/create operation id
- ADM profile display/version
- BZA admin account status/version, nullable legacy representative role
- legacy employee `ACTIVE` → `EMPLOYED`
- status constraints/indexes

Rollback:
- V61 상태/버전/멱등 컬럼과 V61 constraint/index 제거
- `EMPLOYED`를 `ACTIVE`로 역변환하지 않음
- Role 없는 PENDING 계정 때문에 `role_code NOT NULL`을 강제 복원하지 않음
- 가짜 Role 주입이나 사용자 삭제 금지

운영 Rollback은 DB 스크립트만 보지 말고 Code/DB 호환과 Data-loss 영향까지 판단한다.

## 11. 정적 Gate

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-admin-data-safety.ps1
```

선택 옵션:

```powershell
-RootPath <CPF repository root>
```

검사:
- ADM product fail-closed
- Transaction/idempotency/default status
- Masked/Raw API 경계
- BZA 상태 Catalog
- Audit redaction
- Core Internal import
- BZA inline SQL
- Query resource 존재
- V61 source/mirror/checksum/lifecycle parity
- safe rollback

이 Gate는 `DEV_ONLY/CI_RELEASE`이며 Runtime 제품 배포물에 포함하지 않는다.
실제 DB/Browser/Multi-instance 검증을 대체하지 않는다.

## 12. 외부 검증 Runbook

Java25/Gradle:

```text
gradlew clean test assemble --no-daemon --max-workers=1
gradlew checkAdminDataSafety --no-daemon
```

MariaDB:

```text
V58/V59/V60 baseline 준비
→ V61 upgrade
→ ADM/BZA API runtime probe
→ V61 rollback
→ reapply
→ fresh install 비교
```

Browser:

```text
Masked 목록
→ Raw 조회 권한 거부
→ 권한 부여
→ 사유 입력 Raw 조회
→ Audit 확인
→ 상태/expectedVersion conflict
→ explicit contact clear
```

Evidence에는 Commit SHA, 명령, Profile, DB version, 시작/종료시각, 실제 결과와 민감정보 제거 여부를 기록한다.
