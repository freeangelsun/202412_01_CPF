# CPF Batch Standalone Runtime / Remote Agent Guide

기준 master: `9253097086322c0eacc00c005e944b132e31ae06` (`20260726_04`)

## 1. 제품 구조

BAT는 하나의 Owner 제품이지만 Build/배포/Runtime은 다음과 같이 분리한다.

- `cpf-batch-control-server`: Owner API, Runtime Registry, Deployment/Reconciliation, 기존 `CpfBatchOperationsPort` 호환 API, Retention
- `cpf-batch-scheduler`: HA Scheduler, DB Lease, Fencing, Cron/Calendar 기반 Trigger 생성
- `cpf-batch-worker`: Job Pack 실행, Worker Lease/Fencing, Drain/Resume
- `cpf-center-cut-runner`: Center-Cut 전용 Claim/Lease/Fencing/Takeover Runtime
- `cpf-batch-host-agent`: 원격 Host의 승인 Artifact 설치/기동/중지/재기동/Drain/Resume/Rollback/Log 수집
- `cpf-batch-contract`: Public API/SPI
- `cpf-batch-runtime-common`: Runtime 공통 구현
- `cpf-batch-testkit`: Domain/Job Pack 검증 지원

`cpf-batch` 자체는 Aggregate이며 실행 JAR를 만들지 않는다.

## 2. Host Agent 원격 운영

Host Agent는 임의 Shell 문자열을 받지 않는다. 서비스별 승인 Catalog에 등록된 다음 정보만 사용한다.

- serviceId / artifactId
- installRoot / logRoot
- systemd unit 또는 고정 Windows launcher
- loopback Runtime Control URL
- Health URL / Runtime Mode

설치 흐름은 `Artifact Repository -> HTTPS -> SHA-256 -> Production Ed25519 signature -> releases/<version> -> current.version -> start -> readiness` 순서다.
실패하면 previous.version으로 Rollback한다. 전송/응답이 끊겨 결과를 단정할 수 없으면 `UNKNOWN_RESULT`로 남기고 재시도 전에 조회/대사를 수행한다.

## 3. 서버 디렉터리

Linux 표준:

```text
/opt/cpf/bin
/opt/cpf/<service>/releases/<version>
/opt/cpf/<service>/current.version
/opt/cpf/<service>/previous.version
/opt/cpf/<service>/config
/opt/cpf/<service>/work
/var/log/cpf/<service>/<instance>
/etc/cpf/<service>.env
```

Windows 표준은 `C:\cpf\<service>`와 `C:\cpf\logs`를 사용한다.

역할별 `.sh`, `.ps1`, `.properties`, systemd unit은 `deploy/batch` 아래에 있다.

## 4. 로그 정책

각 Runtime은 자기 파일을 독립적으로 기록한다. ADM이 원격 파일시스템을 직접 mount하지 않는다.

공통 상관키:
`environment`, `cellId`, `serviceId`, `instanceId`, `transactionId`, `segmentId`, `executionId`, `jobId`.

정책:
- 일자 + Size rotation
- gzip archive
- maxHistory / totalSizeCap
- 민감 값 저장 전 마스킹
- Agent log collection은 승인된 logRoot만 접근
- Evidence 다운로드는 대상 instance, operator, reason, checksum을 남긴다.

## 5. Scheduler / Worker / Center-Cut

Scheduler는 DB Lease를 통해 Active Leader 하나만 Trigger를 생성하며 takeover 때 fencing token이 증가한다.
Worker는 `workerId + leaseToken + fencingToken`이 모두 일치할 때만 실행을 갱신/완료한다.
Center-Cut Runner도 Item Claim/Lease/Fencing을 사용하며 `UNKNOWN_RESULT`를 성공으로 바꾸지 않는다.

업무 Job/Step/Center-Cut Handler는 BAT Runtime 소스에 적치하지 않고 Domain Job Pack SPI로 제공한다.

## 6. ADM Control Plane

ADM은 `batDB`를 직접 갱신하지 않는다.

- 상태 조회: ADM -> BAT Control Server
- Deployment Plan 생성: ADM -> BAT Control Server
- 위험 조치: 기존 ADM Approval Engine -> `BatApprovalOwnerCommandPort` -> BAT Owner API -> Host Agent
- Requestor와 승인 실행 주체가 동일하면 거부
- 위험 명령은 reason/idempotency/approval/expectedVersion/transaction/evidence 계약을 가진다.

## 7. Deployment Cell

`deploy/schema/deployment-cell.schema.json`이 정본 계약이다.
Cell은 환경, Runtime Role, Artifact, Instance, Host Agent endpoint, configRef, minHealthy, maxUnavailable, health gate, rollback version을 선언한다.
Secret 원문은 Manifest/Git에 저장하지 않고 `vault://` 같은 reference만 둔다.
