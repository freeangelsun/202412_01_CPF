# CPF Evidence Index

## 1. 현재 상태

2026-07-22 Repository 정리에서 이전 PC·이전 DB·이전 Commit 기준 Evidence를 제거했다. 2026-07-24부터 현재 기준선의 신규 Evidence만 다시 적재하며, 실패·미검증 Evidence를 완료로 해석하지 않는다.

- 기준 검수 Commit: `8da2e6f395d72ce032e52ea16fef0d1d5f490c5b`
- 실제 신규 Evidence 기준: 문서 Overlay Push 이후 작업 시작 HEAD
- 현재 판정: 요구별 `완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요`

현재 신규 Evidence:

- `cpf-docs/evidence/20260724_01/BASELINE_ENVIRONMENT.sanitized.md`
- `cpf-docs/evidence/20260724_01/DB_STATIC_AUDIT.sanitized.md`
- `cpf-docs/evidence/20260724_01/MARIADB_INITIALIZER_PREFLIGHT.sanitized.md` — 관리자 인증 부재로 `실패`; DB 설치 성공 근거가 아님
- `cpf-docs/evidence/20260724_01/MARIADB_EMPTY_INSTALL.sanitized.md` — 실제 Fresh Provision/Empty Install/Product Seed/Verify `완료`

## 2. 신규 Evidence 위치

```text
cpf-docs/evidence/<yyyyMMdd_nn>/
```

각 실행 묶음은 다음 Metadata를 가진다.

```text
commit
branch
requirementIds
command
profile
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

## 3. 필수 Evidence 묶음

- Baseline, Environment와 Repository Hygiene
- Compile, Unit, Integration와 Package
- ADM/BZA npm ci, lint, typecheck, test와 production build
- Historical Flyway Checksum와 Lockfile Integrity
- Empty MariaDB Install, Seed와 Verify
- 전체 Module Boot와 API Runtime
- Batch, Center-Cut, Agent/Runner/Worker
- Multi-instance, Lease, Fencing와 Failure Recovery
- External Unknown Result와 Reconciliation
- Browser E2E와 Split Deployment
- Upgrade, Rollback, Backup와 Restore
- Security Negative Test, Approval, Audit와 Secret Scan
- Generator Lifecycle와 Reference Domain Regression
- Guide/Link/Matrix/Evidence Consistency

## 4. Stale 판정

다음 중 하나면 Stale이다.

- 기준 Commit 불일치
- Source/SQL/Config 변경 후 미재실행
- 다른 DB Schema/Seed 기준
- 실행 명령 또는 환경 누락
- 실패 로그를 성공으로 분류
- 민감정보 제거 여부 불명

Stale Evidence는 완료 근거로 사용하지 않는다.
