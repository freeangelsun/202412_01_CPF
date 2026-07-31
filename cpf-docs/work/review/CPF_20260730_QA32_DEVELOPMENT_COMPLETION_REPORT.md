# CPF QA32 개발 완료 보고서

## 1. 기준

- Base SHA: `d31bd127aa12bb9368933216642a5a9d25bd0bfd`
- Head 상태: `WORKTREE_OVERLAY_UNCOMMITTED`
- Source Payload SHA-256: `1867798de867160153657964ee8f2ac4b994fdfa5e75cb0e624a7b0c6358b301`
- Git Commit/Push/Branch/Tag/PR: 생성하지 않음
- README/Guide 제외 범위: 변경 0

## 2. 구현 결과

### Spring Batch Primary Engine

- `cpf-batch:execution-runtime` 신규 제품 Runtime
- Center-Cut·Scheduler·Worker가 `BatchExecutionControlPort → JobOperator`로 연결
- Job/Step, Tasklet, 조건 Flow, 병렬 Flow, Local Partitioning, Remote Partitioning, Remote Chunking, Spring Batch 6 Remote Step 구현
- JobRepository/ExecutionContext Checkpoint·Restart·Stop·Abandon·Recover/Reconcile 연결
- CPF 승인·Fencing·UNKNOWN_RESULT 원장과 JobInstanceId/JobExecutionId/StepExecutionId 연결
- File/Shell/API/Message 실행은 Spring Batch Step Handler로 이관
- 자체 Center-Cut Dispatcher·Worker Polling/완료 집계·자체 실행 Repository는 삭제 Manifest로 제거

### OSS Primary 전환

- Gateway: Spring Cloud Gateway Server Web MVC `HandlerFunctions.http` 실제 Data Plane, Route Snapshot/Service Registry/Resilience4j/Attempt Ledger 연결
- Frontend: Element Plus, TanStack Table, Vue Router, Pinia, TanStack Vue Query, Zod, Orval, Playwright Primary 경계
- BFF: Spring Security + Spring Session JDBC, 브라우저 Token 저장·Authorization 제거, HttpOnly Session·CSRF 적용
- Messaging/Resilience/Observability/Cache/Flag/Secret: Kafka, Resilience4j, Micrometer/OpenTelemetry, Caffeine, OpenFeature, Provider Registry Starter 분리
- Scheduler: db-scheduler Trigger → Spring Batch JobOperator
- Supply-chain: CycloneDX·ORT·Syft·Grype·License allow/deny 정책과 fail-closed Script

### 보안·운영·자원

- BZA Bootstrap: 1회용 승인 Hash·만료·환경 Fingerprint·원자 Claim·Secret File 파기
- Deployment: 실제 Side Effect 인스턴스만 역순 Selective Rollback
- Probe: Timeout·Typed 상태
- Artifact: Canonical Manifest Ed25519·anti-rollback·서비스별 File Lock·원자 상태
- Agent: STOP 확인 후 Rollback, Process Tree/Output Budget, Log TTL·전송 후 삭제
- Archive/Attachment: bounded streaming, TAR symlink/hardlink/device/FIFO 차단

## 3. Matrix 상태

- Requirement: 62/62 개발 완료, Runtime 검증 미검증 62
- Defect: 60/60 Root Cause 수정/Gate 반영, 정적 검증 완료 37·Runtime 미검증 23
- Scenario: 222/222 실행기·Evidence 계약 구현, 정적 Completion Gate 완료 62·Runtime 미검증 160
- OSS Migration: 23/23 개발 완료, 최종 Runtime/Supply-chain 검증 미검증

`부분 구현` 또는 `미구현` 상태는 결과 원장에 남기지 않았다. 다만 실행 환경이 없는 검증을 성공으로 위장하지 않았으므로 전체 상용 완료 선언은 exact-SHA Runtime Evidence 생성 전까지 금지한다.

## 4. 실제 실행한 검증

| 검증 | 결과 | Evidence |
|---|---:|---|
| QA32 Primary Engine/Legacy/DB parity Gate | Exit 0, 2,863 checks | `cpf-docs/evidence/current/qa32-static-primary-engines.json` |
| Repository Security Gate | Exit 0, 1,253 checks | `cpf-docs/evidence/current/qa32-static-security.json` |
| Negative Fixtures | Exit 0, 3/3 | Console result `QA32_NEGATIVE_FIXTURES_PASS` |
| Result Matrix Coverage | Exit 0, 344 rows | `cpf-docs/evidence/current/qa32-development-completion.json` |
| Supply-chain Policy static | Exit 0 | policy/approved lock |
| ADM/BZA Primary Frontend scanner | Exit 0/0 | frontend verify scripts |
| DB Migration overlay checksum | failures 0 | 7 checksum files |
| Java 21 syntax-only diagnostic | syntax error 0; dependency symbol errors 존재 | 전체 Repository/JDK25가 없어 Compile 성공으로 기록하지 않음 |

## 5. 실행하지 못한 검증

Java 25 전체 Repository, npm 승인 Cache/Browser, Oracle/PostgreSQL/MariaDB, Kafka/Docker, 다중 Gateway/Agent Runtime, ORT/Syft/Grype final Artifact가 현재 환경에 없어 실행하지 못했다. 상세는 `CPF_20260730_QA32_UNRESOLVED_REGISTER.csv`에 검증 항목으로 기록했다. Source 구현 미완료로 분류하지 않지만, QA32 전체 `완료` 판정은 하지 않는다.

## 6. 적용 후 필수 명령

```powershell
pwsh -NoProfile -File cpf-tools/scripts/apply-cpf-qa32-development-result.ps1 -ProjectRoot . -ExpectedBaseSha d31bd127aa12bb9368933216642a5a9d25bd0bfd
pwsh -NoProfile -File cpf-tools/scripts/verify-cpf-qa32-runtime.ps1 -Root .
python cpf-tools/scripts/verify-cpf-qa32-completion.py --root . --release
```
