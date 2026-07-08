# Codex 요청서 03 최종본 — PFW Service Call Engine Runtime 대형 개발, Service Registry/Health/Circuit/ADM 관제 확장, Evidence Gate 최소 정합성 복구

## 0. 작업 기준

기준 repository:

`https://github.com/freeangelsun/202412_01_CPF`

기준 branch:

`master`

작업 시작 전 로컬 checkout 기준으로 아래 파일을 확인한다.

* `CPF_FINAL_TARGET_REQUIREMENTS.md`
* `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
* `CPF_STABILIZATION_REPORT.md`
* `CPF_GAP_MATRIX.md`
* `CPF_EVIDENCE_INDEX.md`
* `specs/기능_구현_매트릭스.html`
* `scripts/check-report-matrix-evidence-consistency.ps1`

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 대용량 최종 목표파일이다.
상세 본문을 삭제, 축약, 덮어쓰기 하지 않는다.
이번 작업에서 목표파일은 원칙적으로 수정하지 않는다. 필요 시 상단 요청서 작성용 인덱스 보강만 허용한다.

이번 요청은 오늘 마지막 대형 작업 요청이다.
중간 confirmation 요청하지 말고, 가능한 범위는 Codex가 합리적으로 판단해 끝까지 진행한다.
단, 실제 실행하지 못한 검증은 절대 완료로 기록하지 않는다.

Git commit 금지.
Git push 금지.
branch 생성 금지.
민감정보 원문 기록 금지.
실행하지 않은 검증을 완료로 기록 금지.
별도 변경파일 목록 산출물 생성 금지.
문서량 채우기식 문서화 금지.
신규 HTML 작성/수정은 지양한다. 단, 기존 ADM 화면 또는 기존 기능 매트릭스 유지보수에 필요한 최소 수정은 허용한다.
PDF 생성 금지.
`CPF_NEW_REQUEST.md` 임의 수정 금지.

---

## 1. 이번 요청의 우선순위

이번 요청은 문서 정본화가 목적이 아니다.

우선순위는 아래 순서다.

1. PFW Service Call Engine 실제 runtime 호출 구조 개발
2. Service Registry / Endpoint / Instance / Health / Circuit / Call History 개발
3. Remote Facade Proxy 구조 개발
4. ADM Service Registry API / 운영 조회 API 개발
5. SQL / Flyway / all_install / seed / smoke 정합성 반영
6. Source-level test 및 runtime smoke 강화
7. Evidence / Matrix / Report는 검수 가능한 최소 수준으로만 정합성 맞춤

문서는 예쁘게 쓰지 않는다.
문서는 검수자가 실제 완료 여부를 확인할 수 있는 최소 기록만 남긴다.

---

## 2. 필수 완료 범위

### 2.1 PFW Service Call Engine Runtime 1차 완성

현재 PFW Service Call Engine은 착수 단계로 본다.
이번 작업에서는 실제 업무 모듈이 사용할 수 있는 runtime 호출 엔진 1차 구조까지 개발한다.

필수 개발 범위:

1. Service Call Engine 핵심 인터페이스

   * serviceCode 기반 호출
   * endpointCode 또는 logical operation 기반 호출
   * target module 추상화
   * request/response wrapper
   * transaction context 연동
   * standard header propagation
   * extension header propagation
   * sensitive header masking
   * call result 표준화

2. Service registry lookup

   * serviceCode로 service 조회
   * endpoint 조회
   * enabled 여부 확인
   * routing mode 확인
   * timeout/retry/circuit policy 조회
   * instance 후보 조회

3. Instance selection

   * direct instance mode
   * LB endpoint mode
   * health-aware selection
   * weight 기반 selection 1차 구현
   * fallback instance selection
   * selectedInstanceId 기록

4. Actual HTTP adapter

   * 단순 wrapper 수준 금지
   * 실제 outbound HTTP 호출 path 구현
   * local stub server 또는 test endpoint를 통한 runtime smoke 가능 구조
   * request method, path, header, body 처리
   * response status, header, body 처리
   * timeout 처리
   * exception 처리
   * retry/failover 연동

5. Timeout / Retry / Failover

   * default timeout
   * endpoint timeout override
   * retry max attempts
   * retry interval
   * retryable status/error 분리
   * failover 대상 instance 재선택
   * attemptNo 기록
   * 최종 실패 기록

6. Circuit breaker 1차 구현

   * CLOSED
   * OPEN
   * HALF_OPEN
   * failure count
   * success count
   * lastFailureAt
   * openedAt
   * nextRetryAt
   * recovery transition
   * circuit open 시 호출 차단
   * half-open trial 호출

7. Call history 저장

   * transactionGlobalId
   * segmentId
   * parentSegmentId
   * serviceCode
   * endpointCode
   * selectedInstanceId
   * attemptNo
   * routingMode
   * startedAt
   * endedAt
   * durationMs
   * resultStatus
   * httpStatus
   * errorCode
   * sanitized error message
   * retry 여부
   * failover 여부
   * circuit 상태

8. Segment / Timeline 연동

   * transactionGlobalId 하나로 전체 호출 흐름 추적
   * sourceModuleCode
   * targetModuleCode
   * direction
   * role
   * callDepth
   * sequenceNo
   * selectedInstanceId
   * startedAt
   * endedAt
   * durationMs
   * status
   * failure segment 표시

9. Header 정책

   * `X-User-Id`는 고객번호가 아님
   * `X-Customer-No`가 고객번호
   * `X-Member-No`가 회원번호
   * `X-Operator-Id`는 ADM 운영 조치자
   * `X-Original-Channel-Code`는 최초 유입 채널
   * `X-Channel-Code`는 현재 처리 채널
   * `X-Cpf-Ext-*` 확장 헤더 naming rule 유지
   * Authorization, token, api-key, secret, password, credential, signature 계열 원문 저장 금지

완료 기준:

* 실제 HTTP adapter path를 통과하는 runtime smoke 성공
* timeout/retry/failover/circuit 중 최소 1차 상태 전이 검증
* selectedInstanceId 기록 확인
* call history 저장 확인
* transactionGlobalId/segment 연결 확인
* 민감 헤더 원문 미저장 확인
* source/test/smoke/evidence가 함께 존재

완료 불인정 기준:

* serviceCode 없이 URL 직접 호출
* 단순 HTTP utility wrapper 수준
* retry/circuit/failover 상태 전이 미구현
* call history 저장 없음
* selectedInstanceId 미기록
* runtime smoke 없음
* 실행하지 않은 검증을 완료로 기록

---

### 2.2 Remote Facade Proxy 1차 개발

CPF는 same JVM 전용 구조가 아니다.
Local Facade와 Remote Facade Proxy를 모두 고려해야 한다.

필수 개발 범위:

1. Facade Contract / Port 정의

   * 업무 코드가 직접 URL을 만들지 않도록 한다.
   * Controller 직접 호출 금지
   * 타 주제영역 DB 직접 접근 금지
   * Local 구현체와 Remote Proxy 구현체를 분리한다.

2. Remote Facade Proxy

   * Service Call Engine을 통해 호출
   * serviceCode/endpointCode 기반 호출
   * 표준 request/response mapping
   * timeout/retry/circuit 결과 처리
   * transactionGlobalId/segment 전파

3. 샘플 연동

   * MBR 또는 EDU/XYZ 중 하나 이상에 Remote Facade Proxy 샘플을 실제 연결한다.
   * 단순 dummy class 금지
   * 테스트에서 proxy 호출 path가 검증되어야 한다.
   * same JVM local facade와 remote facade proxy 선택 구조를 profile/property로 분리한다.

4. 금지 패턴 제거

   * 업무 코드 URL 직접 조합 금지
   * Controller bean 직접 호출 금지
   * 타 모듈 Mapper/Repository 직접 호출 금지

완료 기준:

* Remote Facade Proxy 테스트 존재
* Service Call Engine 경유 확인
* local/remote 선택 구조 확인
* forbidden direct call scan 통과

---

### 2.3 Service Registry / Endpoint / Instance / Health / Circuit DB 모델 보강

PFW 운영 DB 기준으로 service routing 운영 메타를 관리할 수 있어야 한다.

필수 테이블 또는 기존 테이블 보강 범위:

1. Service Registry

   * serviceCode
   * moduleCode
   * serviceName
   * ownerModuleCode
   * description
   * enabled
   * routingMode
   * defaultTimeoutMs
   * defaultRetryPolicyId
   * defaultCircuitPolicyId
   * createdAt
   * updatedAt

2. Endpoint Registry

   * endpointCode
   * serviceCode
   * operationCode
   * protocol
   * method
   * path
   * enabled
   * timeoutOverrideMs
   * retryPolicyOverrideId
   * circuitPolicyOverrideId

3. Instance Registry

   * instanceId
   * serviceCode
   * moduleCode
   * host
   * port
   * baseUrl
   * lbEndpointUrl
   * directModeEnabled
   * lbModeEnabled
   * weight
   * zone
   * enabled
   * healthStatus
   * lastHeartbeatAt
   * lastHealthCheckAt

4. Health History

   * instanceId
   * healthStatus
   * checkedAt
   * responseTimeMs
   * reasonCode
   * sanitized reason message

5. Circuit State

   * serviceCode
   * endpointCode
   * instanceId nullable
   * circuitStatus
   * failureCount
   * successCount
   * openedAt
   * halfOpenedAt
   * closedAt
   * nextRetryAt
   * lastFailureAt

6. Call History

   * transactionGlobalId
   * segmentId
   * serviceCode
   * endpointCode
   * selectedInstanceId
   * attemptNo
   * routingMode
   * startedAt
   * endedAt
   * durationMs
   * resultStatus
   * httpStatus
   * errorCode
   * sanitized error message
   * retryYn
   * failoverYn
   * circuitStatus

필수 반영 경로:

* split SQL
* Flyway migration
* seed SQL
* `00_all_install.sql`
* `00_all_install_and_smoke.sql`
* `99_smoke_check.sql`

완료 기준:

* Flyway와 all_install이 동일 구조를 반영
* 신규 빈 DB 설치를 고려한 순서 정합성 확보
* seed로 service/endpoint/instance 기본 데이터 생성
* smoke SQL로 registry/health/circuit/call history 최소 확인 가능
* SQL에 한글 설명 주석 충분히 작성

완료 불인정 기준:

* Flyway만 있고 all_install 누락
* all_install만 있고 Flyway 누락
* seed만 있고 smoke 없음
* 기존 개발 DB 기준만 확인하고 신규 빈 DB full install 완료 주장
* 테이블은 있는데 Service Call Engine에서 사용하지 않음

---

### 2.4 Instance Health / Heartbeat / Ghost 판단 1차 개발

Service Registry는 단순 목록이 아니라 runtime routing 판단에 사용되어야 한다.

필수 개발 범위:

1. instance heartbeat update API 또는 service
2. health check service
3. health TTL 판단
4. stale instance 판단
5. ghost instance 판단
6. DOWN/DEGRADED/UNKNOWN 상태 처리
7. routing selection에서 unhealthy instance 제외
8. ADM 조회용 health summary 제공

완료 기준:

* heartbeat update 테스트
* TTL 만료 테스트
* stale/ghost 판단 테스트
* health-aware routing 테스트
* ADM API에서 health 상태 조회 가능

---

### 2.5 ADM Service Registry API 개발

ADM에서 service registry와 routing 상태를 조회할 수 있어야 한다.

필수 API 범위:

1. service 목록 조회
2. service 상세 조회
3. endpoint 목록 조회
4. endpoint 상세 조회
5. instance 목록 조회
6. instance 상세 조회
7. instance health 조회
8. routing policy 조회
9. circuit state 조회
10. call history 목록 조회
11. transactionGlobalId 기준 call history 조회
12. selectedInstanceId 기준 call history 조회
13. serviceCode 기준 최근 실패 호출 조회
14. DOWN/DEGRADED instance 목록 조회

필수 계층:

* Controller
* Service
* DTO
* Mapper
* SQL
* Test
* smoke script

완료 기준:

* `:adm:test` 통과
* ADM API 단위/통합 테스트 존재
* runtime smoke 가능하면 실제 실행
* runtime smoke 불가 시 미검증으로 남김
* API 응답 evidence 저장

완료 불인정 기준:

* Controller만 있고 Mapper/SQL 연결 없음
* 정적 JSON 응답
* runtime smoke 미실행인데 runtime 완료 기록
* 권한/인증 오류를 무시하고 완료 처리

---

### 2.6 ADM Service Registry 화면 1차 개발

문서 정본화는 하지 않지만, ADM 운영 관제 화면은 개발 작업으로 본다.

필수 화면 범위:

1. Service Registry 목록
2. Service 상세
3. Endpoint 목록
4. Instance 목록
5. Health 상태 표시
6. Routing mode 표시
7. Circuit 상태 표시
8. 최근 call history 표시
9. transactionGlobalId 검색
10. selectedInstanceId 표시
11. DOWN/DEGRADED instance 필터
12. 최근 실패 호출 필터

기존 ADM 화면 구조를 우선 사용한다.
신규 HTML 대량 작성은 지양한다.
기존 화면/JS/API 연동 구조 안에서 최소 수정한다.

필수 smoke:

* 정적 marker smoke
* API endpoint 연결 smoke
* 가능하면 browser click smoke

browser click을 실제 실행하지 못하면 `미검증`으로 남긴다.
정적 marker smoke는 browser click 완료가 아니다.

완료 기준:

* ADM 화면에서 신규 API endpoint 연결 확인
* 정적 marker smoke 통과
* browser click은 실제 실행한 경우에만 완료

---

### 2.7 Runtime Smoke Script 개발

이번 작업은 source-level test만으로 끝내지 않는다.
runtime smoke script를 반드시 보강한다.

필수 script 후보:

1. `scripts/smoke-service-call-engine-runtime.ps1`
2. `scripts/smoke-adm-service-registry-runtime.ps1`
3. `scripts/smoke-service-registry-health-runtime.ps1`
4. `scripts/smoke-service-call-engine-circuit-runtime.ps1`
5. `scripts/smoke-service-call-engine-failover-runtime.ps1`
6. `scripts/smoke-adm-service-registry-ui-static.ps1`

각 script는 아래를 명확히 구분한다.

* source-level 검증
* runtime 검증
* browser click 검증
* 미실행
* 실패

완료 기준:

* runtime smoke script가 존재
* 실행 가능한 것은 실제 실행
* 실행 불가한 것은 사유와 함께 미검증 기록
* SKIPPED를 완료로 기록하지 않음

---

### 2.8 Architecture Rule / Forbidden Direct Call Gate 강화

Service Call Engine을 만들었으면 업무 코드가 우회 호출하지 못하게 막아야 한다.

필수 검사:

1. URL 직접 조합 탐지
2. Controller 직접 호출 탐지
3. 타 주제영역 Mapper/Repository 직접 접근 탐지
4. hardcoded host/url 탐지
5. hardcoded secret/token/api-key/password 탐지
6. Authorization/token 계열 로그 출력 탐지
7. Remote Facade Proxy 또는 Service Call Engine 경유 여부 확인

필수 script 또는 qualityGate 연동:

* architecture rule check
* forbidden direct call scan
* hardcoded secret/url scan

완료 기준:

* 신규 코드에 forbidden pattern 없음
* scan이 qualityGate에 포함됨
* 위반 발견 시 실패 또는 재확인 필요로 기록

---

### 2.9 Test Coverage 확대

이번 요청은 개발량을 크게 잡는다.
테스트도 함께 늘린다.

필수 테스트 범위:

1. Service registry lookup test
2. endpoint lookup test
3. instance selection test
4. health-aware routing test
5. heartbeat update test
6. ghost/stale 판단 test
7. timeout test
8. retry test
9. failover test
10. circuit closed/open/half-open/close transition test
11. call history save test
12. transactionGlobalId propagation test
13. segment/timeline save test
14. sensitive header masking test
15. Remote Facade Proxy test
16. ADM service registry API test
17. ADM call history API test
18. SQL smoke test
19. evidence consistency test

완료 기준:

* `:pfw:test` 통과
* `:adm:test` 통과
* 관련 모듈 테스트 통과
* 실패 테스트가 있으면 원인과 미완료 항목 기록

---

## 3. 보강 범위

### 3.1 PFW 파일 로그와 DB 로그 연결

Service Call Engine 호출 결과는 DB call history만으로 끝내지 않는다.

보강 범위:

* `cpf-{moduleCode}-{logType}.log` 파일 로그와 DB call history 연결
* transactionGlobalId 기준 검색 가능
* selectedInstanceId 로그 기록
* retry/failover/circuit 상태 로그 기록
* 민감정보 masking

완료 기준:

* 파일 로그 또는 log appender 테스트/스모크 evidence 존재
* 민감정보 원문 미기록 확인

---

### 3.2 ADM 권한/RBAC 1차 연결

ADM service registry API는 운영성 API다.
가능한 범위에서 권한 체크와 감사 로그를 연결한다.

보강 범위:

* service registry 조회 권한
* circuit/health 조회 권한
* call history 조회 권한
* transactionGlobalId 조회 권한
* audit log 기록
* 다운로드 기능이 있다면 다운로드 감사 기록

완료 기준:

* 권한 체크 테스트 또는 smoke 존재
* 권한 미연결 시 `부분 구현` 또는 `미검증`으로 기록

---

### 3.3 CMN 공통 코드/메시지 연동

Service Call Engine과 ADM API에서 사용하는 상태값/오류코드는 가능한 범위에서 CMN 공통 코드/메시지와 연결한다.

보강 범위:

* health status
* circuit status
* routing mode
* call result status
* retry result
* failover result
* 표준 오류 메시지

완료 기준:

* hardcoded status 난립 방지
* 공통 코드/메시지 seed 또는 TODO가 명확히 구분됨

---

## 4. 문서 최소 갱신 범위

이번 작업은 문서 정본화가 아니다.
문서 작업은 검수 가능한 최소 수준으로만 한다.

필수 갱신 대상:

* `CPF_STABILIZATION_REPORT.md`
* `CPF_GAP_MATRIX.md`
* `CPF_EVIDENCE_INDEX.md`
* `specs/기능_구현_매트릭스.html`
* `README.md` 주요 진입점이 깨진 경우만 보정

문서에는 장문 설명을 추가하지 않는다.
각 항목은 아래 정보만 기록한다.

* 상태값
* 핵심 변경 소스 경로
* 핵심 SQL/Flyway/all_install 경로
* 실행한 검증 명령
* evidence 경로
* 실행하지 못한 검증과 사유
* 남은 gap

상태값은 아래 6개만 사용한다.

* 완료
* 부분 구현
* 미구현
* 미검증
* 실패
* 재확인 필요

완료/부분 구현으로 기록한 항목은 실제 evidence 경로가 있어야 한다.
없는 evidence 파일을 참조하지 않는다.
문서량을 늘리기 위한 설명은 쓰지 않는다.

---

## 5. Evidence 최소 정합성 복구

Evidence gate는 검수 가능한 수준으로 유지한다.
다만 이번 요청의 중심은 문서가 아니라 개발이다.

필수 작업:

1. `CPF_EVIDENCE_INDEX.md`가 참조하는 완료/부분 구현 evidence 파일은 실제 repo에 존재해야 한다.
2. qualityGate 성공 evidence는 실제 파일로 남긴다.
3. `check-report-matrix-evidence-consistency.ps1`는 최소한 아래를 실패 처리해야 한다.

   * 없는 evidence 파일 참조
   * 허용되지 않은 상태값 사용
   * report/matrix/evidence/gap 상태값 불일치
   * 완료 항목에 SKIPPED evidence 연결
   * 기능 매트릭스 `data-check-id`, `data-status` 파싱 실패
4. negative self-test는 가능하면 수행하되, 개발 작업을 과도하게 막지 않는다.
5. negative self-test가 시간상 어려우면 `재확인 필요`로 남기고, 최소한 실제 evidence 누락은 잡아야 한다.

완료 기준:

* 없는 evidence 파일을 완료 근거로 참조하지 않음
* qualityGate 성공 로그 존재
* consistency script 통과
* report/matrix/evidence/gap 상태값 일치

---

## 6. 착수 범위

아래는 이번 작업에서 가능한 범위까지 착수한다.
실제 검증 전까지 완료로 올리지 않는다.

### 6.1 Multi-instance 검증 준비

* 2대/3대 instance seed
* routing weight 테스트 구조
* selectedInstanceId 비교 smoke skeleton
* failover scenario skeleton

실제 2대/3대 WAS 기동 검증을 못 하면 `미검증`으로 남긴다.

### 6.2 Real Broker 검증 준비

Redis/Kafka/MQ real broker는 이번 요청에서 완료하지 않는다.

가능하면 아래만 준비한다.

* broker profile skeleton
* smoke script skeleton
* broker event schema placeholder
* DLQ/replay TODO 명확화

embedded/mock broker 결과를 real broker 완료로 기록하지 않는다.

### 6.3 Runtime Browser Click 준비

ADM browser click은 실제 실행해야만 완료다.

이번 요청에서는 아래를 준비한다.

* browser click script skeleton
* service registry 화면 selector marker
* API 연동 확인용 화면 marker

실제 browser click 미실행 시 `미검증` 유지.

---

## 7. 후순위/제외 범위

아래는 이번 요청에서 완료로 올리지 않는다.

* ADM browser click 실제 미실행 항목
* Redis/Kafka/MQ real broker 검증
* broker 장애/복구/DLQ/replay
* 2대/3대 multi-instance 실검증
* DR/backup/restore
* 성능 benchmark
* 최종 PDF/HTML 정본화
* 전체 15,000개 이상 체크포인트 구현 완료

위 항목은 목표에서 삭제하지 말고 `CPF_GAP_MATRIX.md`에 `미검증`, `미구현`, 또는 `후순위` 성격으로 남긴다.

---

## 8. 필수 실행 명령

가능한 범위에서 아래 명령을 실제 실행한다.
실행하지 못하면 사유를 기록하고 완료로 올리지 않는다.

```powershell
.\gradlew.bat clean :pfw:test :adm:test :mbr:test --no-daemon --console=plain --rerun-tasks
```

```powershell
.\gradlew.bat qualityGate --no-daemon --console=plain
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/build-all-install-sql.ps1
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/check-report-matrix-evidence-consistency.ps1
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-runtime.ps1 -RunRuntime
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-runtime.ps1 -RunRuntime
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-service-registry-health-runtime.ps1 -RunRuntime
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-circuit-runtime.ps1 -RunRuntime
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-failover-runtime.ps1 -RunRuntime
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-ui-static.ps1
```

MariaDB 신규 빈 DB full install 환경이 가능하면 아래도 실행한다.

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1
```

브라우저 click 환경이 가능하면 아래도 실행한다.

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-adm-ui-browser-click.ps1 -RunBrowser
```

브라우저 click을 실행하지 못하면 `미검증`으로 기록한다.
SKIPPED를 완료로 기록하지 않는다.

---

## 9. 필수 Evidence 경로

이번 작업 evidence는 아래 경로를 우선 사용한다.

`specs/evidence/20260707_03/`

필수 또는 권장 evidence:

* `quality-gate.log`
* `report-matrix-evidence-consistency.log`
* `pfw-service-call-engine-test.log`
* `service-call-engine-runtime-success.sanitized.json`
* `service-call-engine-timeout-retry.sanitized.json`
* `service-call-engine-circuit-transition.sanitized.json`
* `service-call-engine-failover.sanitized.json`
* `service-call-engine-call-history.sanitized.json`
* `service-registry-health-runtime.sanitized.json`
* `service-registry-heartbeat-ghost.sanitized.json`
* `adm-service-registry-api-test.log`
* `adm-service-registry-runtime-result.sanitized.json`
* `adm-service-registry-ui-static-smoke.log`
* `remote-facade-proxy-test.log`
* `sql-flyway-all-install-smoke.log`
* `architecture-rule-check.log`
* `forbidden-direct-call-scan.log`
* `hardcoded-secret-url-scan.log`
* `check-html-docs.log`
* `check-feature-evidence.log`
* `check-utf8-mojibake.log`

파일명은 실제 구현에 맞게 조정할 수 있다.
단, `CPF_EVIDENCE_INDEX.md`에 기록한 경로는 반드시 실제 repo에 존재해야 한다.

---

## 10. 완료 기준

이번 요청의 완료 후보는 아래를 만족해야 한다.

1. PFW Service Call Engine 실제 runtime HTTP adapter 구현
2. serviceCode/endpointCode 기반 registry lookup 구현
3. health-aware instance selection 구현
4. selectedInstanceId 기록
5. timeout/retry/failover/circuit 1차 구현
6. call history 저장
7. transactionGlobalId/segment/timeline 연동
8. 민감 헤더 원문 미저장
9. Remote Facade Proxy 1차 구현
10. ADM Service Registry API 구현
11. ADM Service Registry 화면 1차 구현
12. SQL/Flyway/all_install/seed/smoke 정합성 반영
13. Service Registry / Endpoint / Instance / Health / Circuit / Call History 테스트 존재
14. runtime smoke 가능한 항목 실제 실행
15. qualityGate 성공 evidence 존재
16. 없는 evidence 파일을 완료 근거로 참조하지 않음
17. report/matrix/evidence/gap 최소 정합성 유지
18. browser click 미실행 시 미검증 유지
19. real broker 미실행 시 미검증 유지
20. Git commit/push/branch 생성 없음

---

## 11. 완료 불인정 기준

아래 중 하나라도 있으면 완료로 기록하지 않는다.

* 목표파일 상세 본문 삭제 또는 축약
* 목표파일을 작은 파일로 덮어쓰기
* Service Call Engine이 단순 HTTP wrapper 수준
* service registry lookup 없이 URL 직접 호출
* selectedInstanceId 미기록
* call history 미저장
* retry/circuit/failover 미구현
* runtime smoke 없이 runtime 완료 주장
* Remote Facade Proxy 없이 업무 코드가 URL 직접 호출
* SQL/Flyway/all_install 중 한 경로만 반영
* ADM API가 정적 JSON 또는 Controller 단독 구현
* ADM 화면 marker만 있고 API 연결 없음
* browser click SKIPPED를 완료로 기록
* real broker 미실행을 완료로 기록
* 없는 evidence 파일을 완료 근거로 참조
* qualityGate 성공 로그 없음
* `CPF_NEW_REQUEST.md` 임의 수정
* Git commit/push/branch 생성
* 민감정보 원문 evidence 기록

---

## 12. 완료 보고 방식

작업 완료 후 아래 형식으로 보고한다.

1. 작업 요약
2. 실제 개발한 핵심 기능
3. PFW Service Call Engine runtime 구현 내역
4. Service Registry / Health / Circuit / Call History 구현 내역
5. Remote Facade Proxy 구현 내역
6. ADM API/UI 구현 내역
7. SQL/Flyway/all_install/smoke 반영 내역
8. 직접 실행한 검증 명령
9. 실행하지 못한 검증과 사유
10. evidence 파일 경로
11. 완료/부분 구현/미구현/미검증/실패/재확인 필요 판정
12. 남은 gap
13. 다음 작업 후보

보고는 짧아도 된다.
단, 실행한 검증과 evidence 경로는 반드시 정확해야 한다.
실행하지 않은 검증은 완료로 기록하지 않는다.
