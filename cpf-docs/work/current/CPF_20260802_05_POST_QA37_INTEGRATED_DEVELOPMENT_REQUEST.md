# POST-QA37 통합 개발·검증·보완 요청서

## 1. 기준과 목표

- Review baseline: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
- 실제 시작 기준: 작업 시작 시 `HEAD`와 `origin/master`를 다시 확인
- 목표: QA37 WIP와 후속 Push를 실제 Source 기준으로 재조정하고, 누락 Requirement와 Starter Architecture를 구현한 뒤 최신 exact-SHA 전수 검증까지 완료
- 상태 목표: Source Defect `0`, 미구현 `0`, 필수 미검증 `0`, Environment Blocker `0`

## 2. 먼저 확인할 자료

1. Final Target
2. Requirement Continuity Ledger
3. Starter Architecture Policy
4. Current Request
5. Codex Continuity/Decision Log
6. 이번 Pre Review, Codex History Review, Requirement Recovery Review
7. 최신 Source/SQL/Test/Build/Config/Frontend/Generator
8. 외부 `execution-ledger.csv`와 `defect-ledger.csv`가 존재하면 read-only import

문서의 완료 표시보다 실제 Source와 최신 실행 결과를 우선한다.

## 3. 통합 Backlog

`CPF_20260802_05_POST_QA37_SELF_DEVELOPMENT_REQUIREMENTS.csv`의 모든 행을 하나의 Backlog로 처리한다.
공통 Root Cause와 선행 의존성으로 묶되 범위를 축소하지 않는다.

### Workstream A — Canonical·History

- 169개 Product Requirement와 작업 ID를 구분한다.
- MQ/JMS/IBM MQ/RabbitMQ/TCP와 Fresh DB Requirement를 모든 Matrix/Guide/Test/Evidence에 연결한다.
- 과거 PASS, 현재 PASS, 미검증, Environment Blocker를 분리한다.
- Current 문서는 하나의 진입점만 유지한다.

### Workstream B — Lightweight Core

- `cpf-core`의 선택 OSS, AutoConfiguration, concrete JDBC/runtime worker를 전수 분류한다.
- Public API/SPI compatibility를 보호하며 실제 Consumer를 이관한다.
- Core-only non-Boot fixture가 compile/test되어야 한다.
- Dual Primary를 제거한다.

### Workstream C — Starter·Profile·Bundle

- Leaf Starter를 구현한다.
- Generator Profile은 explicit leaf list와 resolved lock을 생성한다.
- Aggregate Starter는 안정 조합만 제공하고 고유 Bean을 금지한다.
- Provider conflict와 invalid combination은 생성/기동 시 fail-closed한다.
- Generated Domain, ADM/BZA/Gateway/Batch/Reference가 실제 Consumer가 된다.

### Workstream D — Messaging·TCP

- Generic broker contract와 reliability runtime을 분리한다.
- Kafka, JMS, IBM MQ, RabbitMQ를 실제 Provider로 구현·검증한다.
- TCP persistent integration을 framing부터 unknown-result/reconcile까지 완결한다.
- credentials, TLS, masking, audit, readiness와 operations를 포함한다.

### Workstream E — DB Fresh Lifecycle

- Canonical/Generator를 먼저 수정한다.
- Vendor SQL부터 수정하지 않는다.
- 각 Vendor별 전용 QA DB/Schema의 CPF Object 0건을 증명한다.
- Fresh install, mandatory metadata/seed, generated arbitrary Domain, runtime query, upgrade, rollback, reapply, conflict, optional pack, cleanup을 수행한다.
- 기존 사용자 DB와 Docker volumes/assets는 보호한다.

### Workstream F — Final Verification

- Low-cost static once
- Java 25 fresh lifecycle once
- Frontend clean once
- DB one vendor at a time
- Runtime/fault/multi-instance
- Browser 3 engines
- Trivy/ORT/SBOM/license
- Matrix/Evidence/exact SHA

## 4. 완료 규칙

- 검수에서 결함을 발견하면 실제 Source/Test/Generator/Guide/Evidence까지 보완한다.
- Interface/Dependency/Marker만 추가하지 않는다.
- 실제 Consumer 없는 Starter를 완료 처리하지 않는다.
- 실행하지 못한 검증은 미검증이다.
- 과거 SHA Evidence를 현재 성공으로 승계하지 않는다.
- 변경 후 사용자 Commit/Push 전에는 final exact-SHA 완료라고 쓰지 않는다.

## 타 GPT 전담 보호 경로

다음 경로는 Read Only다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

이 작업과 다음 Codex 작업은 해당 경로를 참조할 수 있지만 수정·추가·삭제·이동·이름 변경·자동 포맷·일괄 치환·Stage하지 않는다.
변경 필요성이 발견되면 실제 파일을 건드리지 않고 담당 GPT용 영향도와 작업요건만 기록한다.
Overlay·Delete Manifest·Cleanup 대상에도 포함하지 않는다.
