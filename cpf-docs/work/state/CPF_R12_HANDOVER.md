# CPF R12 Handover — 2026-07-26

## 기준 상태
- 작업 시작 master: `90130d3f34a8483718b4222b57b3618e8fffc919`
- 작업 산출: R12 Root-relative overlay
- 사용자 Commit/Push: 아직 수행하지 않음
- Post-apply SHA: `PENDING_USER_PUSH`

## R12 완료 구현 범위
- ADM Mandatory Audit durable reservation/outbox + retry/recovery + ADM 복구 UI
- ADM mutation verified actor fail-closed, Batch `requestUser` 제거, legacy body actor mismatch guard
- BAT 조회 장애 은폐 제거
- BAT Ghost state CAS + exact lock key/owner + BAT transaction
- CMN Calendar product/DB-less mode 분리, create/version/delete conflict, actual actor 기록
- Calendar ADM WRITE/DELETE 권한·감사 사유·409 UX
- Generated Domain `cpf-common` 기본 dependency, memory non-product 제한
- Generator 정본 단일화
- Full Verification read-only, checksum 검증/갱신 분리
- MariaDB migration canonical lifecycle drift V6/V29/V40~V51 정리
- 삭제된 ADM frontend feature methods import 회귀 복구 및 permission fallback fail-closed

## Architecture 결정
- `cpf-common`은 공식 필수 Library JAR이며 독립 WAS가 아니다.
- Product mode CMN DB/JDBC Calendar는 필수, DB-less는 Library/EDU/Test/Local read-only.
- ADM은 Control Plane이며 actor 신뢰 근거는 인증 Filter의 `adm.operatorId` 하나다.
- Audit은 XA 대신 Owner 작업 전 durable reservation을 먼저 확정하고 relay/retry한다.
- BAT Ghost/Lock은 `cpf-batch` Owner transaction에서 처리한다.
- Generated Domain은 `cpf-common`과 공개 Core API/SPI만 사용한다.

## 반드시 보호할 성공 기능
- Generated Domain `com.cpf.core.common.*` import 0
- `cpf-common.utils` consumer 0
- BAT/Center-Cut Runtime ownership
- ADM→BAT/MBR Owner Port
- UNKNOWN_RESULT 및 recovery 구조
- REF public EDU
- ADM/BZA fail-closed permission
- 삭제된 `cpf-tools/db/source`/`cpf-external` 비부활
- canonical Generator 단일 정본
- Verification read-only

## Push 전 정확한 삭제 대상
`cpf-tools/db/vendor/mariadb/migration/flyway/V6__bizadm_exs_transaction_identity.sql`

대체: `V6__transaction_server_identity.sql` (PRE-GA canonical source와 동일). 광범위 wildcard 삭제 금지. 제공된 cleanup script를 사용한다.

## 미검증
전체 Gradle/DB/Browser/Multi-instance/Jenkins/Release Full Verification. 실행 Evidence 없으므로 완료 선언 금지.
