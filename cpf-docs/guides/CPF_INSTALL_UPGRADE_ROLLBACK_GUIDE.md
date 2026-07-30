# CPF 설치·업그레이드·되돌리기 가이드

## 1. 목적

이 문서는 CPF를 신규 환경에 설치하고, 기존 환경을 안전하게 업그레이드하며, 장애 시 이전 상태로 되돌리는 전체 절차를 정의한다.

## 2. 설치 대상

- CPF Library Artifact
- Platform Application
- Gateway
- Batch Runtime
- ADM/BZA Frontend
- Database
- Config와 Secret Reference
- Registry
- Monitoring
- Certificate
- Generated Domain

## 3. 사전 준비

- 지원 Java/Gradle/Node
- OS와 Filesystem
- DB Vendor/Version
- Network
- DNS
- TLS
- Artifact Repository
- Secret Provider
- Service Account
- Port
- Backup Storage
- 운영 승인

## 4. 설치 Manifest

Manifest:

- releaseId
- productVersion
- sourceCommit
- artifact Hash
- module list
- DB version
- config version
- environment
- owner
- createdAt
- signature
- SBOM Reference

## 5. Artifact 검증

```text
Download
→ Manifest
→ SHA-256
→ Signature
→ SBOM/License
→ Version
→ Compatibility
→ Install
```

## 6. 신규 설치 순서

1. Filesystem과 계정
2. Secret Provider
3. Database Provision
4. Database Install/Seed/Verify
5. Artifact 배치
6. Config
7. Registry
8. Application 시작
9. Readiness
10. Gateway Binding
11. Batch Runtime
12. Frontend
13. Smoke
14. Evidence

## 7. Database 설치

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

Generated Domain도 설치한다.

## 8. Runtime 시작

```powershell
pwsh -File .\cpf-tools\scripts\start-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\status-cpf-local.ps1
```

운영 환경에서는 Deployment Manifest와 Process Manager를 사용한다.

## 9. 설치 검증

- Process
- Liveness
- Readiness
- Registry
- DB
- Login
- 대표 API
- Gateway
- Batch Smoke
- Log/Trace
- Audit
- Frontend Route
- Secret 노출
- Version

## 10. 업그레이드 계획

변경 분류:

- API
- DB
- Config
- Message
- File
- Artifact
- Frontend
- Runtime Policy
- Certificate
- Job Definition

호환성 Matrix를 만든다.

## 11. 업그레이드 전략

- Rolling
- Canary
- Blue-Green
- Stop-the-world
- DB Expand/Contract

DB 파괴 변경은 Application 호환 기간을 둔다.

## 12. Expand/Contract

```text
Expand
→ 새 Column/Table 추가
→ 구/신 Version 동시 지원
→ 데이터 이관
→ 신 Version 전환
→ 검증
→ Contract
```

한 번에 Rename/Drop하지 않는다.

## 13. 사전 검사

- Clean Source
- Artifact Signature
- Backup
- DB Drift
- Disk
- Capacity
- Certificate
- Secret
- Current Incident
- Maintenance Window
- Rollback Artifact

## 14. 업그레이드 실행

1. Change 승인
2. Traffic Drain
3. Backup
4. DB Upgrade
5. Canary Instance
6. Health/Smoke
7. 단계적 확대
8. Gateway/Policy Apply
9. Batch Resume
10. 최종 Verify
11. Audit/Evidence

## 15. Rolling

- maxUnavailable
- minHealthy
- Readiness Gate
- In-flight Drain
- Version 혼재 호환
- 실패 시 중단
- 자동 Rollback

## 16. Canary

- 대상 비율
- 사용자/Channel
- 관측 지표
- Error Budget
- 최소 관찰 시간
- 확대 조건
- 중단 조건

## 17. Blue-Green

- DB 호환
- Session
- Queue
- File
- DNS/LB
- Warm-up
- Cutover
- Backout

## 18. Rollback 판단

- 오류율
- Latency
- Readiness
- 데이터 정합성
- Migration 실패
- Gateway Drift
- Batch 실패
- Security
- 운영 승인

## 19. Application Rollback

- 이전 Artifact
- Config Version
- Registry
- Gateway Binding
- Runtime Policy
- Health
- Cache
- Session

## 20. DB Rollback

DB Rollback은 데이터 손실 가능성을 검사한다.

- 신규 데이터
- 신규 Column 사용
- Identity/Sequence
- Foreign Key
- Archive
- Message Schema
- Application Version

Rollback 불가 시 Forward Fix 또는 Bridge Migration을 사용한다.

## 21. Config Rollback

Versioned 정책을 과거 Version으로 Publish한다. Instance ACK와 Drift를 확인한다.

## 22. Gateway Rollback

검증된 Binding Version으로 되돌리고 Instance별 Apply 상태를 확인한다.

## 23. Batch Rollback

- Definition Version
- Schedule
- 실행 중 Execution
- Agent Artifact
- Checkpoint
- Restart 호환
- Unknown

실행 중인 Job의 의미를 임의로 변경하지 않는다.

## 24. Frontend Rollback

Backend API 호환성을 확인하고 Static Artifact를 교체한다. Browser Cache와 Service Worker 정책을 처리한다.

## 25. 실패 복구

설치/업그레이드 중 실패하면:

1. 단계 확인
2. 변경 중단
3. Traffic 차단
4. DB 상태
5. Artifact Version
6. Config
7. 결과 불명 거래
8. Rollback 또는 Forward Fix
9. Verify
10. Incident

## 26. 재해복구

- Backup 복구
- Artifact 복구
- Config/Secret
- Registry
- DNS/LB
- Message Offset
- File
- Batch Checkpoint
- 거래 대사
- RPO/RTO

## 27. Evidence

- Change ID
- 승인
- Release Manifest
- Source Commit
- Artifact Hash
- DB Plan
- Backup Manifest
- Command
- 시각
- Instance별 결과
- Smoke
- Rollback 여부
- Incident
- Sanitizing

## 28. 체크리스트

- [ ] Release Manifest가 있다.
- [ ] Artifact Hash와 Signature를 검증했다.
- [ ] DB Backup과 Rollback 계획이 있다.
- [ ] Version 혼재 호환성을 확인했다.
- [ ] Drain과 Health Gate가 있다.
- [ ] 결과 불명 거래를 대사한다.
- [ ] Gateway/Batch/Config Version을 함께 관리한다.
- [ ] 설치·업그레이드·Rollback Evidence가 있다.
