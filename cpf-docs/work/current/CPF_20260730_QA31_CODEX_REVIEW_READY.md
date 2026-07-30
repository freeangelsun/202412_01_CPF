# CPF QA31 Codex 독립검수 준비 요청

## 목적

Credit을 아끼기 위해 변경된 수직 Slice와 Evidence부터 검수한다. 기존 QA31 원본 ID·Priority·Acceptance를 변경하지 않는다. README/Guide 제외 범위는 검수하지 않는다.

## 통합 Matrix 기준

- Requirement 708, Scenario 218, 총 926
- 개발 완료 652, 미검증 274
- 검증 완료 82, 미검증 844

## Base/Head

- Base: `9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e`
- 전달 Head: `WORKTREE-OVERLAY`
- 사용자가 Push한 뒤에는 반드시 새 exact SHA를 Head로 교체한다.

## 1차 저비용 Gate

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-cpf-qa31-development-result.ps1 -Root . -BaseSha 9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e -Mode full
```

검수 우선순위:

1. QA 원본 Hash와 23/99/66 Matrix Count
2. Result Matrix 165 ID 및 Unresolved Defect 23 ID 보존
3. README/Guide 제외 위반 0
4. Legacy Gateway 삭제 Manifest와 신규 Public API Consumer
5. Oracle/PostgreSQL/MariaDB V81 위치·Checksum·Rollback
6. 완료행 exact Evidence — 현재 완료행 0이므로 임의 승격 금지


## 현재 실제 실행 Evidence

- QA31 Full Static Gate: Exit Code `0`, `476 checks`, `failures 0`
- QA31 Gate Self-Test: Exit Code `0`, 5개 Negative/Positive Check PASS
- EDU/BZA Coverage Gate 자체 Positive/Negative Fixture: Exit Code `0`, 4개 Check PASS
- Java 21 Gateway HMAC Harness: Exit Code `0`
- Java 21 Batch JCA/PKIX Signature Harness: Exit Code `0`
- Modified Vue Script TypeScript Parse: Exit Code `0`
- Java 25·전체 Gradle·3 DB Runtime·Redis·Browser E2E: `미검증`

## Root Cause Batch

### RC-02/03 Gateway Data Plane

- ID: `QA31-D003`, `D008~D012`, `QA31-GWY-*`, `QA31-GHL-*`
- 주요 파일: Gateway Route/PathRewriter/Snapshot/Probe/Proxy/Transport, ServiceCall Attempt Observer
- 확인: 실제 Ingress Path, Target Rewrite, ACK-before-active, LKG, Probe 계층, streaming completion, attempt ledger

### RC-04 Gateway Control Security

- ID: `QA31-D013~D015`, `QA31-GSC-*`
- 주요 파일: Control Signer/Headers/Nonce/Security Audit, Authentication Filter, ADM Remote Adapter
- 확인: Body tamper, audience mismatch, cross-instance nonce, audit failure fail-closed, timeout/unknown

### RC-05 Batch Runtime

- ID: `QA31-D016~D020`, `QA31-BAT-*`
- 주요 파일: RuntimeExecutorRegistry, JobPackDispatcher, FileProcessHandler Registry, Shell Verifier, Attempt Repository
- 확인: Canonical JSON, 실제 FILE_PROCESS consumer, Claim/Release, signature/PKIX, secret delivery, attempt detail

### RC-06 ADM

- ID: `QA31-D021~D023`, `QA31-ADM-*`
- 주요 파일: Reference Catalog, Log Export, Service Registry Catalog, Gateway UI, App menu search
- 확인: secret value 미노출, paging, durable artifact, permission-bound menu search

### RC-07 EDU/BZA

- ID: `QA31-EDU-*`, `QA31-BZA-*`
- 확인: 실제 Source+Test+Frontend 구조, FILE_PROCESS Reference consumer, 기존 기능 회귀

## 고비용 독립검수

- Java 25 전체 Gradle
- Frontend `npm ci`, lint, test, build
- 3 DB Install→Upgrade→Rollback→재설치
- Gateway 2+ instance, Redis, replay, target-down, timeout, retry, streaming disconnect
- Batch full chain and crash/unknown/recovery
- ADM/BZA Browser E2E, SSE, 권한·승인

## 완료 처리 금지

- 파일 존재/문자열 Anchor만 확인
- 실행하지 않은 환경을 PASS
- 과거 Commit Evidence 승계
- Interface·SPI만 추가하고 Consumer 미확인
- README/Guide 변경을 QA31 기능 결과로 포함
