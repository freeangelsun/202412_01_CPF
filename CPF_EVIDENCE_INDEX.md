# CPF Evidence Index

## 1. 현재 상태

2026-07-22 Repository 정리에서 이전 PC·이전 DB·이전 Commit 기준 Evidence를 제거했다. 현재 Worktree에 실행 Evidence가 없다는 사실은 의도된 초기화 상태이며, 완료를 의미하지 않는다.

- 기준 검수 Commit: `8da2e6f395d72ce032e52ea16fef0d1d5f490c5b`
- 실제 신규 Evidence 기준: 문서 Overlay Push 이후 작업 시작 HEAD
- 현재 판정: **미구현** 또는 각 요구별 **미검증**

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
