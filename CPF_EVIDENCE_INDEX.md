# CPF Evidence Index

## 1. 현재 기준

- 검수/Overlay 기준 Commit: `22b1874e67547372b51a4bcd21f47aea6fcb5c25`
- Overlay 자체는 Runtime Evidence가 아니다.
- `cpf-docs/evidence/20260722_01`은 이전 Commit/구조 기준 Stale Evidence이므로 동봉 Cleanup 대상이다.
- 현재 Overlay가 추가한 ADM/BZA DDL, Central Pack fail-fast, Requirement 복구는 실제 Build/DB/Runtime 재실행 전까지 `미검증` 또는 해당 Requirement의 `부분 구현`이다.

현재 유효한 신규 Runtime Evidence 경로를 이 문서에 미리 만들거나 존재한다고 주장하지 않는다. 다음 Codex가 실제 실행 후 새 실행 묶음을 생성하고 이 Index를 같은 Commit 기준으로 갱신해야 한다.

## 2. 신규 Evidence 위치

```text
cpf-docs/evidence/<yyyyMMdd_nn>/
```

각 실행 묶음은 최소 다음 Metadata를 가진다.

```text
commit
branch
hostClass           # HOME / COMPANY 등 민감하지 않은 구분
requirementIds
command
profile
dbVendor
environment
startTime
endTime
exitCode
result
rawArtifact
sanitizedArtifact
secretReview
staleStatus
```

Credential/Password/Token/Private Key/개인정보 원문은 Evidence에 저장하지 않는다.

## 3. 필수 Evidence 묶음

- Baseline, Environment, Repository Hygiene와 Secret Scan
- Clean Compile, Unit, Integration와 Package
- ADM/BZA npm ci, lint, typecheck, test와 Production Build
- Historical Flyway Checksum/Lockfile Integrity
- MariaDB Reset Dry-run/Apply, Fresh Empty Install, Product Seed와 Verify
- Split DDL / Generated Bundle / Central Pack exact parity
- 전체 Module Boot와 주요 API Runtime
- Local/Remote Service Call과 Standard Header/transactionGlobalId
- Batch, Center-Cut, Agent/Runner/Worker
- Multi-instance, Lease, Fencing, Failure/Unknown/Recovery
- External 연계, Unknown Result와 Reconciliation
- ADM/BZA Approval의 ALL/ANY/N_OF_M, 부서합의, 위임, SoD, 동시성
- Browser E2E와 Split Deployment
- Upgrade, Rollback, Backup/Restore
- Generator arbitrary-domain lifecycle
- OpenAPI/JavaDoc/Guide/Matrix/Evidence consistency
- Protected Baseline regression

## 4. Stale 판정

다음 중 하나면 Stale이다.

- 기준 Commit 불일치
- Source/SQL/Config 변경 후 미재실행
- 다른 PC/DB Schema/Seed 결과를 현재 환경 실행으로 재사용
- 실행 명령/환경/결과 누락
- 실패 로그를 성공으로 분류
- 민감정보 제거 여부 불명

Stale Evidence는 완료 근거로 사용하지 않는다.
