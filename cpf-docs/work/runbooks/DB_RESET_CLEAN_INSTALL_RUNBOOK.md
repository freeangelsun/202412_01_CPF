# CPF DB Reset·Clean Install Runbook

## 1. 현재 전제

회사 PC와 집 PC 모두 CPF Schema와 Table이 없는 상태다. 첫 실행은 Reset이 아니라 Empty Install이다.

## 2. 준비

- MariaDB Service 실행
- 관리자 접속 확인
- CPF Credential은 환경변수/Secret로 제공
- 다른 Application Schema Inventory 확보
- Repository 최신 HEAD 기록

## 3. 첫 Cycle: Empty Install

```text
preflight
→ provision service users/grants
→ create allowlisted schemas
→ create tables/constraints/indexes
→ mandatory product seed
→ optional sample seed when enabled
→ verify schema/version/seed
→ boot modules
→ API/Batch/ADM/BZA smoke
```

Reset Script를 선행하지 않는다.

## 4. Schema Allowlist

정확한 이름 배열만 허용한다. `%DB`, `cpf%` 같은 Pattern으로 Drop하지 않는다. 예상하지 않은 Schema가 발견되면 중단한다.

## 5. cmnDB

`cmnDB`는 최소 Sample Table 1개만 생성한다. Sequence, Fixed-Length, 통합 Log, Notification과 다수 EDU Table을 만들지 않는다.

## 6. Seed

- Mandatory Product Meta와 Optional Sample/Test를 분리
- password/token/private key/PII 금지
- idempotent rerun
- role/menu/API permission 정합성
- batch/executor/agent/center-cut catalog Owner 확인

## 7. 두 번째 Cycle

```text
reset dry-run
→ allowlist review
→ reset apply
→ reinstall
→ seed rerun
→ module boot
```

## 8. Upgrade/Rollback Cycle

- baseline version install
- representative data
- upgrade migration
- old/new application compatibility
- rollback 또는 forward recovery
- data integrity

## 9. 확인 SQL 범주

- schema/table inventory
- PK/FK/UK/index/constraint
- duplicate/orphan
- product seed count/version
- service user grants
- legacy PFW/XYZ/잘못된 prefix
- cmnDB table count

## 10. DB 접근 불가

정본 Script, 실행 명령과 확인 SQL을 작성하되 상태를 `미검증` 또는 `재확인 필요`로 남긴다. 접속 가능한 환경에서 실제 실행하기 전 완료 처리하지 않는다.

## 11. Evidence

Commit, command, profile, MariaDB version, masked host/port, start/end, exit code, schema diff, verify result, module runtime와 secret review를 남긴다.
