# CPF Current Work Request — QA Final Closure

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Overlay 작업 시작 Commit: `84b4672e9e8b61ea067bb52b85b838a0b95e44b1`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA 추적 대상: 요구사항 1,749건 + 실행 시나리오 369건 = 2,118건
- 추가 ADM Runtime Control 지시는 기존 목록에 Root Cause·Consumer 기준으로 병합한다.

## 2. 이번 변경의 직접 완료 범위

1. ADM Runtime Control의 `cpf-core.common.runtimecontrol` 직접 의존을 Public API 경계로 이관한다.
2. 14개 Runtime Capability Catalog와 ADM 조회 API, reason/operationId/expectedVersion/approval Fail-Closed 검증을 추가한다.
3. ADM Notification의 운영자 fallback과 requestUser 위조 경로를 제거한다.
4. Notification 공통 Service의 MariaDB 전용 SQL을 제거하고 공식 3 DB 호환 JDBC 계약으로 바꾼다.
5. Gateway Header Trust, 인증 판단, Route 정본 실행 ID, 정규화 Path 검증을 강화한다.
6. BZA Login 멱등성 최신 Source와 뒤처진 Test를 정합화한다.
7. Cache Durable Event Publisher Test를 최신 계약으로 교체하고 신규 Instance Snapshot High-Water Race를 제거한다.
8. Routing weighted rendezvous·priority·zone·cell·drain 구현을 회귀 Test/Gate로 고정한다.
9. MariaDB Historical Migration 28건의 source/runtime checksum Manifest를 복구한다.
10. Local Web 1 JVM·1 Port Runtime과 별도 Local Batch 역할별 독립 Context Launcher를 추가한다.
11. Java 25 전체 Test/Assemble/Frontend/Static Gate CI와 전체 Closure Evidence 실행기를 추가한다.

## 3. 완료 판정 규칙

- Overlay 정적 검증은 Repository 최신 Commit 실행 검증을 대체하지 않는다.
- `invoke-cpf-final-closure.ps1`이 Clean Worktree의 정확한 Commit에서 Java 25, 전체 Test/Assemble, Frontend, Runtime, Generator, 3개 공식 DB Lifecycle, GitHub Governance를 실행하고 `FINAL_CLOSURE_EVIDENCE.json`을 생성해야 제품 완료 Evidence가 된다.
- 실제 실행하지 않은 Browser·DB·다중 Instance·외부 Provider 항목은 성공으로 기록하지 않는다.
- Gate 실패 시 문서 상태를 바꾸지 말고 Source·SQL·Test·Script를 함께 수리한다.

## 4. 적용 후 실행

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1
```

전체 Closure:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\invoke-cpf-final-closure.ps1 -DatabaseProfilePath .\profiles\mariadb.json,.\profiles\postgresql.json,.\profiles\oracle.json -RunGitHubGovernance
```

Profile 경로는 실제 환경 파일로 교체하며 Secret 원문을 Git이나 Evidence에 저장하지 않는다.
