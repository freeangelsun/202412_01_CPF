# CPF R14 Handover

## 기준
- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- R14 작성 기준 Commit: `56b165513f73f0548d41d2d52197abcdf69a0d14`
- 최상위 목표: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA 입력: `cpf-docs/review/CPF_MASTER_FULL_DEFECT_AUDIT_20260726.md`
- 산출 방식: Repository Root Overlay. AI는 commit/push/branch를 생성하지 않았다.

## R14 실제 구현 범위
### 1. ADM Frontend P0/P1 즉시 결함
- LogsPage route target 복원 및 Vue SFC 중복 종료 태그 제거.
- non-2xx False Success 차단.
- 401/logout 민감 state reset.
- access token persistent localStorage 사용을 sessionStorage로 축소하고 legacy key 제거.
- 로그 Copy/local JSON export가 서버 감사 없이 실행되지 않도록 fail-closed.
- 거래 상세 section partial-result 구조.
- Button/API allow 필드를 menu delete와 분리.

### 2. BZA 인증·Role·Permission
- 동시 401 refresh single-flight 및 최종 실패 session clear.
- 강제 비밀번호 변경/만료 Backend 집행.
- active role/menu filtering, environment scope, deny precedence 보강.
- Role history 존재 계정은 legacy roleCode fallback으로 만료 Role을 되살리지 않음.
- `bza_user_role` 재부여 이력, operationId idempotency, primary role serialization, version CAS.
- 기존 User 편집에서 Role 직접 변경을 차단하고 User Role 이력 화면으로 분리.
- domain/dataScope/httpMethod/apiPattern 전체 통합 evaluator는 아직 Runtime 완결 검증 전이므로 부분 구현.

### 3. BZA Approval / Directory / 운영 UI
- Request body requester spoof 차단: 인증 operator→employee 기준.
- Delegation 검증.
- 사용된 Approval Policy version immutable.
- Legacy direct approval mutation controller/service 차단.
- Employee/Assignment canonical 관계 정리, stale delegated/absence SQL 제거.
- Organization cycle guard.
- Position/JobTitle/Assignment/Responsibility/Org/Employee/AdminUser/Menu/Role/Permission/UserRole server paging/CAS.
- Attachment scan lifecycle와 Notification 운영 UI 보강.

### 4. BZA Audit Chain
- canonical JSON + SHA-256 chain.
- `bza_audit_chain_lock` row를 `FOR UPDATE`해 다중 writer 직렬화.
- VALID/PARTIAL_LEGACY/BROKEN 검증.
- content/link뿐 아니라 final chain-head mismatch로 tail deletion 탐지.
- 주요 BZA operation/backoffice/support/session revoke를 hash audit service로 수렴.
- WORM 외부 archive/DB privilege 완결은 별도 검증 필요.

### 5. Public Paging / ADM Member
- `CpfPageRequest`, `CpfPage<T>` Public API.
- ADM `/adm/api/members/page` → MBR Owner operation port.
- MBR owner DB count + LIMIT/OFFSET paging.
- ADM이 MBR DB를 직접 조회하지 않는 ownership 유지.

### 6. Health / Service Registry
- ADM/BAT liveness/readiness 인스턴스 식별 정보 강화: moduleId, wasId, serverInstanceId, host, processId, profiles, checkedAt.
- readiness는 local mandatory dependency를 gate.
- remote owner failure는 기본 diagnostic/non-gating, 필요 시 property로 명시 gate.
- 모든 instance fan-out은 probe가 아니라 Service Registry aggregation 책임.
- ADM Service Registry UI/API와 운영자 UI 보강.

### 7. Secret / Certificate
- Secret Reference/Metadata/Value Public API와 Provider/Rotatable Provider SPI.
- ENV bootstrap provider.
- ADM provider/metadata/rotate API와 Secrets UI.
- raw value API 반환 금지.
- certificate expiry 검사 Script.
- 실제 Vault/KMS/HSM adapter와 전체 JWT key lifecycle은 부분 구현으로 유지.

### 8. Retention / Legal Hold
- Public Retention policy/command/result/operations + handler SPI.
- BAT operation log concrete handler.
- dry-run, KEEP, legal hold, ARCHIVE, PURGE.
- destructive action은 cutoff + `cpf.retention.execute-enabled=true` 필요, default OFF.
- archive table과 guarded rollback.
- 모든 업무 Owner retention handler가 구현된 것은 아님.

### 9. Tenant Boundary
- `CpfTenantContext`, Resolver SPI, conditional filter.
- default OFF, resolver missing 503, tenant missing 400, finally clear.
- DB row/schema/connection isolation은 미완성 범위이므로 Tenant 전체 완료 처리 금지.

### 10. DB Canonical / Migration / Metadata
- MariaDB canonical source와 lifecycle 구조 유지, standalone `cpf-tools/db/source` 금지.
- 고정 EXS provision/verify 의존 제거.
- V53 BZA governance/operability, V54 BAT retention archive 및 guarded R53/R54.
- fresh source schema에 version/history/audit/archive 구조 반영.
- default metadata catalog: 22 group, 89 required values, 15 messages, 15 response codes, 7 configs.
- source plan 기반 8개 bundle 재생성 및 central lifecycle 동기화.
- unsupported Vendor는 not-implemented/fail-closed.

### 11. Backup / Restore / DR / Promotion
- MariaDB backup + SHA manifest.
- Restore confirm/manifest/hash/vendor/database 검증.
- isolated/full DR verification 모드와 Evidence.
- certificate expiry.
- environment change-set 생성/검증 + file hash/base SHA.
- 실제 조직 승인/signature/CD adapter는 부분 구현.

### 12. 문서
- Developer/Admin/BZA/DB/Generator/Security/Tool/Health/Metadata 상세 가이드.
- Current/Next/Handover/Codex integrated validation request.
- QA 원문과 R14 static Evidence.

## 보호할 기존 성공 구조
- Generated Domain `com.cpf.core.common.*` 직접 import 금지.
- `cpf-common.utils` legacy 복원 금지.
- BAT = Batch/Center-Cut Runtime Owner.
- ADM → MBR/BAT Owner DB 직접 query 금지.
- `UNKNOWN_RESULT` 보존.
- ADM/BZA authz fail-closed.
- EXS는 Generator 생성형.
- DB 정본은 `cpf-tools/db/vendor/<vendor>/source`.
- unsupported Vendor SQL 복사 금지.

## 상태와 미검증
R14 Overlay source/static 범위가 구현되었어도 아래 실제 실행은 이 작업환경에서 수행하지 못했으므로 `미검증`이다.
- JDK25 전체 Gradle clean/test/assemble/qualityGate.
- ADM/BZA npm ci/verify/Browser E2E.
- MariaDB fresh/upgrade/rollback/re-apply/drift.
- 2 instance health/role/audit/cache/failover.
- Generator create/build/remove/re-create.
- Service Call/Async/Batch/File UNKNOWN_RESULT fault matrix.
- Backup/Restore/DR 실제 DB 실행.
- Release SBOM/License/CVE/Signature/Provenance.

QA 289건 + 제품 Gap 12건은 R14에서 전부 완료한 것이 아니다. R14에서 직접 해결/보강한 항목과 별개로, 해당 전수 QA는 **최신 Push 뒤 Codex가 ID별 재판정하면서 잔존 P0/P1을 수정하는 통합검증 입력**이다. 문서 완료표시만으로 QA ID를 완료로 바꾸지 않는다.
