# CPF ChatGPT Session Handover — 20260728_02

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 시작 SHA: `84b4672e9e8b61ea067bb52b85b838a0b95e44b1`
- 전체 QA: 요구사항 1,749 + 시나리오 369 = 2,118건
- 사용자 직접 Push 예정이며 ChatGPT는 Commit/Push/Branch를 생성하지 않았다.

## 2. 이번 Overlay 핵심

- ADM Runtime Control Public API 경계와 14 Capability Catalog
- Notification 인증 운영자 Fail-Closed, SQL 3 Vendor portability, Simulator 상태 구분
- Gateway 신뢰 Header allowlist, 검증된 Principal/Signature, Route 실행 ID, Path 정규화
- BZA Login 멱등성 구형 Test 교체
- Cache durable Publisher Test와 신규 Instance Snapshot High-Water Race 수정
- Routing weight/priority/zone/cell/drain 회귀 Test
- MariaDB Migration checksum Manifest 4건 → Historical 28건 복구
- Local Web 단일 JVM/Port와 별도 Local Batch 역할별 Context
- Java25 CI, Enterprise QA Gate, Full Closure Evidence 실행기
- ADM Runtime Control 14개 관리 영역 추가 QA 보완 문서

## 3. 적용 후 첫 명령

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1
```

## 4. 전체 완료 검증

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\invoke-cpf-final-closure.ps1 -DatabaseProfilePath .\profiles\mariadb.json,.\profiles\postgresql.json,.\profiles\oracle.json -RunGitHubGovernance
```

실제 Profile 경로로 교체한다. 실행기는 Clean Worktree와 정확한 HEAD를 기록하고 성공·실패 모두 JSON/Log Evidence로 남긴다.

## 5. 다음 세션 진행 순서

1. 사용자 Push 후 최신 master SHA를 다시 확인한다.
2. `check-enterprise-qa-closing.ps1`과 CI 결과를 검수한다.
3. Compile/Test 실패가 있으면 Source와 Test를 동시에 수리한다.
4. 3개 DB Profile로 전체 Closure 실행기를 수행한다.
5. Browser·다중 Instance·Offline 복귀·UNKNOWN_RESULT·Rollback Evidence를 검토한다.
6. `P0_REMEDIATION_LEDGER.csv`의 execution_status가 미검증/재확인 필요인 행을 실제 Evidence로 완료 전환한다.
7. ADM Runtime Control 보완 문서의 14 Capability를 실제 Consumer·ACK·Drift·Rollback까지 양방향 추적한다.
8. Mock/Simulator 결과를 실제 외부 Provider 완료로 승격하지 않는다.

## 6. 중요 판정

- Overlay 자체는 최신 Repository 실행 PASS가 아니다.
- Java25/Gradle, pwsh, 3 DB, Browser, Multi-instance를 이 환경에서 실행하지 않았다.
- 실행하지 않은 결과를 성공으로 기록하지 않는다.
- 실패가 남으면 문서만 완료로 바꾸지 않는다.
