# CPF 상태 점검과 서비스 등록부 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 실행 환경 개발자, 운영자, 경로 선택 담당자
> **목적**: 생존·준비·진단 상태와 서비스 등록·심박·만료·배수 상태를 일관되게 제공한다.
> **관련 문서**: [게이트웨이 운영](CPF_GATEWAY_OPERATIONS_GUIDE.md) · [관측·장애대응·복구](CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | 계약은 `cpf-core`; 운영 API는 `cpf-admin`; Runtime Adapter는 등록부 Owner |
| 이 문서로 완료하는 일 | Service·Endpoint·Instance의 등록·Heartbeat·Health·Expiry·Drain·Maintenance·Delete를 조회/제어 계약으로 분리해 운영한다. |
| 적용 범위 | Registry Query/Control Port, ADM API, Health Policy, Routing State, Call History |
| 주요 독자 | 플랫폼 운영자, Runtime Owner, Gateway/Service Call 개발자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

CPF 상태 점검은 단순한 HTTP 200 확인이 아니다. 프로세스 생존, 신규 요청 수신 가능 여부, 서비스 전체 상태와 경로 선택 가능성을 분리해 판단한다.

## 2. 상태 점검 단계

### 생존 상태

현재 프로세스가 살아 있고 기본 실행 루프가 응답하는지 확인한다.

포함:

- JVM/프로세스
- 이벤트 루프와 기본 스레드 상태
- 치명적 내부 오류

제외:

- 원격 서비스 연쇄 호출
- 전체 DB 조회
- 느린 외부 호출

### 준비 상태

현재 인스턴스가 신규 요청을 받을 수 있는지 판단한다.

포함:

- 필수 로컬 DB
- 리스너
- 실행 환경 초기화
- 필수 비밀값/인증서
- 배수·점검 모드
- 핵심 큐·저장소

### 상세 진단

원격 소유자, 외부 기관, 메시지 중개 시스템 등 상세 의존 대상을 별도로 진단한다. 상세 진단 실패가 항상 현재 인스턴스 준비 상태를 내리는 것은 아니다.

## 3. 등록부 모델

### 조회와 제어 계약 분리

서비스 등록부는 조회용 `CpfServiceRegistryQueryPort`와 상태 변경용 `CpfServiceRegistryControlPort`를 분리한다.

- 조회 계약: 서비스·인스턴스·엔드포인트·상태·버전·용량·점검 정보
- 제어 계약: 등록, 갱신, 심박, 배수, 점검모드, 해제와 만료 처리

ADM은 제어 파사드를 통해 명령하고 등록부 저장소가 상태 전이와 버전을 최종 검증한다. 화면의 사용자 입력 `requestedBy`보다 인증 주체(Principal)을 신뢰한다.


서비스:

- serviceId
- systemCode
- moduleId
- owner
- version
- protocol
- endpoint
- visibility
- capability

인스턴스:

- serverInstanceId
- host
- processId
- zone
- cell
- profile
- startedAt
- heartbeatAt
- liveness
- readiness
- drain
- maintenance
- capacity
- metadata

## 4. 등록

```text
Runtime 시작
→ Identity 생성
→ Local Validation
→ Registry 등록
→ Heartbeat
→ Readiness UP
```

중복 인스턴스 ID를 거부한다.

## 5. 심박

- 주기
- TTL
- Jitter
- 배치 Update
- Network 시간 제한
- Stale 판단
- 복구

심박 Store 장애 시 로컬 실행 환경을 불필요하게 종료하지 않되 경로 선택 안전성을 위해 등록부 상태를 명확히 한다.

## 6. 상태

- UP
- DEGRADED
- DRAINING
- MAINTENANCE
- DOWN
- STALE
- UNKNOWN

상태 전이와 원인을 기록한다.

## 7. 히스테리시스

한 번의 실패로 즉시 DOWN/UP을 반복하지 않는다.

- 연속 실패 수
- 연속 성공 수
- 최소 유지시간
- Passive 오류
- Active Probe

## 8. 경로 선택

신규 요청 대상:

- readiness UP
- drain 아님
- maintenance 아님
- circuit 허용
- zone 정책
- capacity

## 9. 배수

```text
DRAIN_REQUESTED
→ 신규 요청 제외
→ In-flight 감소
→ DRAINED
```

최대 대기시간 후 강제 정책을 정의한다.

## 10. 점검 모드

- 사유
- 시작/종료
- 소유자
- 승인
- 신규 유입 차단
- 상태 점검 표시
- 자동 복귀
- 감사

## 11. 서비스 그룹

등록부는 서버 그룹과 연결된다.

- Member
- Weight
- Priority
- Zone
- 상태 점검
- 버전

## 12. 의존 대상

의존 대상 Graph:

- caller
- target
- protocol
- criticality
- timeout
- fallback
- owner

사고 영향 분석에 사용한다.

## 13. 버전

순차 교체 중 버전 혼재를 표시한다.

- compatible
- deprecated
- minimum peer version
- protocol/schema version

## 14. 보안

상태 점검 응답에 다음을 노출하지 않는다.

- Password
- DB URL 인증정보
- 비밀값
- Private IP 정책상 금지 정보
- 스택 추적
- 개인정보

상세 진단은 운영 권한을 요구한다.

## 15. 지표

- instance count
- healthy count
- stale count
- readiness duration
- heartbeat lag
- state transition
- drain duration
- registry error
- probe latency

## 16. 운영 절차

### 인스턴스 DOWN

1. 등록부 시각
2. 생존 상태
3. 호스트/프로세스
4. 최근 배포
5. 로그
6. Traffic 영향
7. 자동 복구
8. 재기동
9. 복귀
10. 사고

### 준비 상태 DOWN

1. 사유
2. 로컬 의존 대상
3. Pool/리스너
4. 비밀값/인증서
5. 배수·점검 모드
6. 복구 후 경로 선택

## 17. 테스트

- 인스턴스 2개
- 서로 다른 ID
- DB 중단
- 심박 중단
- Network Partition
- Stale
- 히스테리시스
- Drain
- Maintenance
- 순차 교체 버전
- 등록부 Store 장애
- 민감정보

## 18. 체크리스트

- [ ] 생존 상태와 준비 상태를 분리한다.
- [ ] 원격 Fan-out을 생존 상태에 넣지 않는다.
- [ ] 인스턴스 Identity가 로그/추적과 같다.
- [ ] 심박 TTL과 Stale 정책이 있다.
- [ ] 배수·점검 모드가 경로 선택에 반영된다.
- [ ] 상태 전이 원인을 기록한다.
- [ ] 상태 점검에 민감정보가 없다.

## 부록 A. 상태 응답 예

```json
{
  "status": "DEGRADED",
  "systemCode": "PAY",
  "moduleId": "payment-api",
  "instanceId": "pay-03",
  "version": "1.8.0",
  "checkedAt": "2026-07-30T08:10:20Z",
  "reasons": ["PAY_DB_REPLICA_LAG"],
  "dependencies": [
    {"name": "owner-db", "status": "UP", "latencyMs": 8},
    {"name": "read-replica", "status": "DEGRADED", "lagSeconds": 15}
  ]
}
```

민감한 호스트·계정·오류 원문은 공개 상태 응답에 포함하지 않는다.

## 부록 B. 등록 만료

- 심박 간격과 만료 시간은 네트워크 지연보다 충분히 길게 설정한다.
- 일시 누락은 히스테리시스로 흡수한다.
- 만료 인스턴스는 즉시 삭제하지 않고 묘비 상태와 마지막 정보를 보존한다.
- 재등록 시 인스턴스 세대와 버전을 비교한다.

## 부록 C. 배수

`DRAINING` 전환 → 신규 배정 중단 → 진행 요청 수와 최대 종료 시각 확인 → 0건 또는 시간 초과 → 중지·교체 → 준비 상태 확인 → `UP` 복귀

## 22. 실제 Service Registry API 지도

| 목적 | Method와 경로 | 주요 필드 |
|---|---|---|
| 지원 코드 확인 | `GET /adm/api/service-registry/capabilities` | Service·Endpoint 유형, Instance 상태·명령, 환경 |
| 서비스 조회 | `GET /adm/api/service-registry/services` | `serviceId`, `useYn`, `limit` |
| Endpoint 조회 | `GET /adm/api/service-registry/endpoints` | `serviceId`, `endpointCode`, `useYn` |
| Instance 조회 | `GET /adm/api/service-registry/instances` | `serviceId`, `endpointCode`, `status` |
| 상태 점검 조회 | `GET /adm/api/service-registry/health` | 서비스·Endpoint별 실제 상태 |
| 경로 정책·회로 상태 | `GET .../routing-policies`, `GET .../circuit-states` | 적용 정책과 보호 상태 |
| 호출 이력 | `GET /adm/api/service-registry/call-history` | `serviceId`, `transactionId` |
| 정의 저장 | `POST .../services`, `POST .../endpoints`, `POST .../instances` | Operation ID, 기대 Version, 사유 |
| Instance 제어 | `POST .../services/{serviceId}/endpoints/{endpointCode}/instances/{instanceId}/state` | `DRAIN`, `DISABLE`, `RESUME` |

### 22.1 제품 코드 Catalog

`GET /adm/api/service-registry/capabilities`는 UI와 API가 함께 사용하는 Code Catalog를 반환한다. 자유 입력 문자열을 저장하지 않고 `CpfServiceRegistryCatalog`가 서버에서 다시 검증한다.

| 분류 | 허용 값 |
|---|---|
| Service Type | `INTERNAL`, `EXTERNAL`, `PLATFORM`, `MONITOR_ONLY` |
| Endpoint Type | `HTTP`, `HTTPS`, `GRPC`, `TCP`, `WEBSOCKET`, `SSE`, `MONITOR_ONLY` |
| Environment | `DEV`, `TEST`, `STG`, `PROD` |
| Instance Command | `DRAIN`, `DISABLE`, `RESUME` |
| Instance Status | `UP`, `DOWN`, `DEGRADED`, `UNKNOWN`, `DRAINING`, `DISABLED`, `MAINTENANCE`, `STALE`, `RECOVERING` |

응답의 `catalogVersion`을 화면 상태와 함께 보관하고, 지원하지 않는 Code가 입력되면 저장 전에 Field 오류로 표시한다. Backend가 거부한 값을 UI에서 임의 치환하거나 Default로 조용히 바꾸지 않는다.

## 23. 서비스·Endpoint·Instance 등록 예제

### 23.1 서비스

```json
{
  "operationId": "registry-pay-service-001",
  "serviceId": "PAY",
  "serviceName": "결제 서비스",
  "serviceType": "INTERNAL",
  "ownerModuleCode": "PAY",
  "description": "결제 승인과 결과 조회",
  "useYn": "Y",
  "expectedVersion": null,
  "reason": "결제 서비스 최초 등록"
}
```

### 23.2 Endpoint

```json
{
  "operationId": "registry-pay-endpoint-001",
  "endpointCode": "PAY-API",
  "serviceId": "PAY",
  "endpointName": "결제 HTTP API",
  "endpointType": "HTTPS",
  "baseUrl": "https://pay.internal",
  "contextPath": "/api/v1",
  "defaultTimeoutMs": 10000,
  "defaultRetryCount": 0,
  "useYn": "Y",
  "expectedVersion": null,
  "reason": "결제 API Endpoint 최초 등록"
}
```

### 23.3 Instance

```json
{
  "operationId": "registry-pay-instance-001",
  "instanceId": "pay-api-01",
  "serviceId": "PAY",
  "endpointCode": "PAY-API",
  "instanceName": "결제 API 1호기",
  "baseUrl": "https://10.20.1.11:8443",
  "hostName": "pay-api-01",
  "portNo": 8443,
  "environmentCode": "PROD",
  "zoneCode": "SEOUL-A",
  "cellCode": "PAY-A",
  "weight": 100,
  "priorityNo": 1,
  "activeYn": "Y",
  "maintenanceYn": "N",
  "drainYn": "N",
  "expectedVersion": null,
  "reason": "결제 API 운영 인스턴스 등록"
}
```

서버는 검증된 ADM 운영자를 `requestedBy`로 설정한다. `reason`은 5자 이상이어야 하며, 수정·삭제·상태 제어에는 현재 `expectedVersion`을 사용한다.

## 24. 안전한 배수 절차

1. Instance 상세와 Version, 상태 점검, 최근 호출 이력을 조회한다.
2. Gateway와 Service Call 대상군에서 해당 Instance가 맡는 비율과 대체 용량을 확인한다.
3. `DRAIN` 명령을 현재 Version과 사유로 요청한다.
4. 신규 배정이 중지됐는지 확인하고 실행 중 요청이 종료될 때까지 기다린다.
5. 시간 상한을 넘긴 요청은 거래 ID로 추적하고 결과 불명 여부를 대사한다.
6. 배포·점검을 수행하고 Liveness와 Readiness를 확인한다.
7. `RESUME`을 요청한 뒤 점진적으로 Traffic이 돌아오는지 확인한다.
8. 오류율·응답시간·회로 상태·호출 이력을 관찰하고 감사 ID를 기록한다.

```json
{
  "operationId": "drain-pay-api-01-001",
  "command": "DRAIN",
  "expectedVersion": 12,
  "reason": "결제 API 1호기 배포 전 안전 배수"
}
```

`DISABLE`은 단순 배수보다 강한 조치다. 새 요청 중단뿐 아니라 운영 대상에서 제외해야 하는 장애·보안 상황에 사용하고 복귀 전 별도 확인을 수행한다.

## 25. 상태 만료와 복귀 판정

| 관측 | 판정 | 자동·운영 조치 |
|---|---|---|
| Liveness 실패, Heartbeat 유효 | `DOWN` 또는 `DEGRADED` | 신규 배정 제외, 원인 확인 |
| Heartbeat 만료 | `STALE` | 소유 인스턴스·네트워크·시계 확인 |
| 배수 요청 | `DRAINING` | 신규 배정 중지, 실행 중 요청 관찰 |
| 점검 상태 | `MAINTENANCE` | 자동 복귀 금지, 점검 종료 승인 |
| 복구 확인 중 | `RECOVERING` | 낮은 비율로 단계 복귀 |
| 안정 관찰 통과 | `UP` | 정상 가중치 복원 |

상태 저장만 바뀌고 실제 경로 선택이 바뀌지 않으면 완료가 아니다. Registry 상태, Gateway 대상 선택, 서비스 호출 이력과 실제 Traffic을 함께 확인한다.

## 26. 서비스 호출 Attempt 원장

서비스 등록부의 호출 이력은 최종 성공·실패만 보여서는 안 된다. `CpfServiceCallAttempt`는 재시도와 장애 전환의 각 시도를 다음 필드로 표준화한다.

- `attemptNo`, 선택된 `target`, 장애 전환 여부
- 상태와 HTTP 상태
- 시작·종료 시각과 소요시간
- 실패 코드·정제된 실패 메시지
- `unknownResult` 여부

운영자는 하나의 거래에서 대상이 왜 바뀌었는지, 어느 시도가 시간 제한 또는 연결 오류였는지, 최종 결과가 불명인지 확인한다. Attempt Observer 실패가 원 거래를 오염시키지 않도록 기록 책임과 Transaction 경계를 분리하되, 기록 누락은 지표와 경보로 탐지한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Query Port | `CpfServiceRegistryQueryPort.java`, `CpfServiceRegistryView.java` | 조회 모델 |
| Control Port | `CpfServiceRegistryControlPort.java`, `CpfServiceRegistryCatalog.java` | 등록·수정·상태 명령과 제품 Code 정본 |
| ADM API | `AdmServiceRegistryController.java` — `/adm/api/service-registry` | 서비스·Endpoint·Instance 조회/제어 |
| Facade/Repo | `CpfServiceRegistryQueryFacade.java`, `CpfServiceRegistryControlFacade.java`, Repository | 계약과 저장소 연결 |
| Attempt | `CpfServiceCallAttempt.java`, `CpfServiceCallAttemptObserver.java` | Retry·Failover 단일 시도 원장 |
| UI | `cpf-admin/frontend/src/features/service-registry/` | 운영 화면 |

### Z.1 공통 확인 명령

```powershell
git status --short
git diff --check
git grep -n "TODO\|UnsupportedOperationException\|return null" -- ':!cpf-docs/archive/**'
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

명령이 현재 Repository에 존재하지 않거나 Parameter가 달라졌다면 해당 Tool Source와 [도구 상세 참조](CPF_TOOL_REFERENCE.md)를 먼저 갱신한다.

### Z.2 완료 상태 사용

- **완료**: 구현·Consumer·운영 경로·검증·Evidence가 현재 Commit에서 확인됨
- **부분 구현**: 일부 계층 또는 실패·복구·운영 경로가 빠짐
- **미구현**: 제품 동작이 없음
- **미검증**: 구현은 있으나 요구된 실행 검증을 수행하지 않음
- **실패**: 검증을 수행했으나 기대 결과를 충족하지 못함
- **재확인 필요**: Source·문서·Evidence 또는 환경이 서로 달라 현재 상태를 확정할 수 없음
