# CPF Enterprise QA 중간 Root Overlay 안내

## 1. 상태

이 파일은 `2026-07-28` Enterprise QA 최종 마무리 작업 중간 동결본이다.

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 대조 기준 SHA: `ca3cf8a12290903cc482b5e092cdb43e6bf8f1eb`
- 산출물 성격: 프로젝트 Root에 덮어쓰는 **중간 Root Overlay**
- Git commit / push / branch 생성: 수행하지 않음
- 완료 판정: **최종본 아님**

이 Overlay는 세션 컨텍스트 단절에 대비해 현재 실제 변경 파일을 보존하기 위한 중간 인수인계 산출물이다. 최종 Java 25 전체 Build, 3 Vendor Runtime DB, 다중 인스턴스, Browser, Generator 실검증 전에는 Release 완료로 판정하면 안 된다.

## 2. 적용

프로젝트 Root를 백업하거나 Git clean 상태를 확인한 뒤 ZIP 내용을 프로젝트 Root에 덮어쓴다.

Windows 예시:

```powershell
$z="$HOME\Downloads\CPF_20260728_ENTERPRISE_QA_INTERMEDIATE_ROOT_OVERLAY.zip"; $t="$env:TEMP\CPF_QA_INTERMEDIATE"; Remove-Item $t -Recurse -Force -ErrorAction SilentlyContinue; Expand-Archive $z $t -Force; Copy-Item "$t\*" "C:\dev\projects\jck\202412_01_CPF" -Recurse -Force
```

적용 후 즉시 commit하지 말고 `git status`, Build, DB Gate와 변경 diff를 먼저 확인한다.

## 3. 현재 주요 반영 범위

### Runtime Control Plane

- Target selector, include/exclude, overbroad guard
- operationId/request fingerprint, expectedVersion CAS
- durable Change/Delivery, ACK, fencing, instance registration/heartbeat
- instance별 delivery 직렬 처리
- payload schema/capability/restart impact 협상
- PREPARED/APPLIED durable Inbox와 UNKNOWN_RESULT fail-closed
- 기능별 desired/actual/hash/drift 상태
- Controller leader lease, scheduled activation, expiry, ACK timeout
- Canary/Wave quorum 및 health gate
- poison/unknown 자동 중단, 적용 대상 기준 rollback
- 분산 rate limit, health, preview, audit 검증 API

### Runtime Feature 실제 Consumer/조건부 Adapter

현재 작업본에는 다음 Runtime Feature의 Applier 또는 실제 Consumer 연결이 포함된다.

- 공통코드, 메시지, 응답코드, Runtime Config Cache
- Business Calendar, Channel Policy
- Log Level, Trace Sampling, Masking Policy
- Broker Consumer, Broker Retry/DLQ
- DB Read Routing, Connection Pool
- Password Policy
- Certificate, Secret Reference, JWT Key, Encryption Key reload-capable SPI
- File Policy, SFTP Transfer
- Attachment Policy, Download Policy
- Fixed Layout, Schema Registry
- Service Route, Circuit, Maintenance 검증
- Gateway Route, Header, CORS, Rate Limit
- API Client key/quota/IP/certificate/expiry 정책
- External Institution, Webhook Callback
- Reconciliation policy/worker

실제 Adapter/Port가 없는 선택 기능은 성공 ACK를 만들지 않고 capability를 광고하지 않는 방향이다.

### DB

- Canonical Platform Schema 정합화
- MariaDB / PostgreSQL / Oracle V64 migration 및 R64 rollback
- Runtime Control feature state, controller lease, rate bucket
- unknown-result reconciliation claim/attempt/lease/version 컬럼
- 공식 지원 Vendor를 MariaDB/PostgreSQL/Oracle 3종으로 제한

## 4. 주의

- 이 중간본은 최종 QA Inventory 1,214개와 실행 시나리오 201개를 모두 최종 PASS 판정한 산출물이 아니다.
- External Institution의 layout/timeout 보강은 현재 Source에 반영됐으나, 해당 최신 변경 이후 전체 Gradle compile은 아직 수행되지 않았다.
- 직접 실행하지 않은 Java 25, DB, Browser, 다중 WAS 검증은 성공으로 기록하지 않는다.
