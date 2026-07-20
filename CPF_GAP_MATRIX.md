# CPF_GAP_MATRIX.md

## 기준

- SHA: `42c0fda82e0f3061e839f69cad25bbfa9df2aa0f`
- 아래 모든 gap은 동일한 단일 대형 마일스톤의 필수 범위다.
- 우선순위는 작업 순서에만 사용한다.

## 전체 Gap

### `ARCH-MISSION` — CPF 최종 목표/상용 솔루션 원칙

- 상태: `재확인 필요`
- 구현 신호: source 0, test 0, SQL 0, script 2
- 완료 조건:
  - 정본의 ownership·dependency·배포 topology와 실제 module/build/package 구조를 일치시킨다.
  - 동일 JVM과 분리 WAS에서 동일 contract를 사용하고 금지 dependency를 architecture gate로 차단한다.

### `ARCH-MSA` — MSA-first 및 Modular Monolith 호환

- 상태: `재확인 필요`
- 구현 신호: source 80, test 20, SQL 0, script 1
- 완료 조건:
  - 정본의 ownership·dependency·배포 topology와 실제 module/build/package 구조를 일치시킨다.
  - 동일 JVM과 분리 WAS에서 동일 contract를 사용하고 금지 dependency를 architecture gate로 차단한다.

### `ARCH-BOUNDARY` — 주제영역 경계/Bounded Context

- 상태: `재확인 필요`
- 구현 신호: source 76, test 15, SQL 2, script 8
- 완료 조건:
  - 정본의 ownership·dependency·배포 topology와 실제 module/build/package 구조를 일치시킨다.
  - 동일 JVM과 분리 WAS에서 동일 contract를 사용하고 금지 dependency를 architecture gate로 차단한다.

### `ARCH-LAYER` — 계층/패키지/의존성 규칙

- 상태: `재확인 필요`
- 구현 신호: source 64, test 16, SQL 1, script 2
- 완료 조건:
  - 정본의 ownership·dependency·배포 topology와 실제 module/build/package 구조를 일치시킨다.
  - 동일 JVM과 분리 WAS에서 동일 contract를 사용하고 금지 dependency를 architecture gate로 차단한다.

### `FACADE-LOCAL` — Local Facade 표준

- 상태: `재확인 필요`
- 구현 신호: source 19, test 7, SQL 0, script 0
- 완료 조건:
  - typed request/response, local/remote parity, endpoint/instance 선택, timeout·오류 계약을 source와 runtime으로 완성한다.
  - consumer domain에서 internal package를 직접 참조하지 않도록 public API/SPI와 migration을 제공한다.

### `FACADE-REMOTE` — Remote Facade Proxy/Port-Adapter

- 상태: `재확인 필요`
- 구현 신호: source 114, test 19, SQL 1, script 7
- 완료 조건:
  - typed request/response, local/remote parity, endpoint/instance 선택, timeout·오류 계약을 source와 runtime으로 완성한다.
  - consumer domain에서 internal package를 직접 참조하지 않도록 public API/SPI와 migration을 제공한다.

### `PFW-CALL` — CpfWebClient/CpfRestClient Service Call Engine

- 상태: `재확인 필요`
- 구현 신호: source 42, test 8, SQL 0, script 0
- 완료 조건:
  - typed request/response, local/remote parity, endpoint/instance 선택, timeout·오류 계약을 source와 runtime으로 완성한다.
  - consumer domain에서 internal package를 직접 참조하지 않도록 public API/SPI와 migration을 제공한다.

### `PFW-REGISTRY` — Service/Endpoint/Instance Registry

- 상태: `재확인 필요`
- 구현 신호: source 17, test 1, SQL 4, script 4
- 완료 조건:
  - typed request/response, local/remote parity, endpoint/instance 선택, timeout·오류 계약을 source와 runtime으로 완성한다.
  - consumer domain에서 internal package를 직접 참조하지 않도록 public API/SPI와 migration을 제공한다.

### `PFW-ROUTING` — LB mode/direct instance/discovery routing

- 상태: `재확인 필요`
- 구현 신호: source 10, test 2, SQL 0, script 0
- 완료 조건:
  - typed request/response, local/remote parity, endpoint/instance 선택, timeout·오류 계약을 source와 runtime으로 완성한다.
  - consumer domain에서 internal package를 직접 참조하지 않도록 public API/SPI와 migration을 제공한다.

### `PFW-HEALTH` — Liveness/Readiness/Dependency Health

- 상태: `재확인 필요`
- 구현 신호: source 10, test 2, SQL 0, script 1
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-RESILIENCE` — Timeout/Retry/Circuit/Bulkhead/Backpressure

- 상태: `재확인 필요`
- 구현 신호: source 3, test 2, SQL 1, script 1
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-DEADLINE` — Request Deadline/Timeout Budget

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-HEADER` — 표준/확장 헤더

- 상태: `재확인 필요`
- 구현 신호: source 22, test 8, SQL 1, script 1
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-CONTEXT` — TransactionContext/MDC/Thread Context

- 상태: `재확인 필요`
- 구현 신호: source 5, test 1, SQL 0, script 0
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-TXID` — transactionGlobalId/segment/timeline

- 상태: `재확인 필요`
- 구현 신호: source 18, test 2, SQL 3, script 2
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-ROLE` — transactionRole/direction/source-target

- 상태: `재확인 필요`
- 구현 신호: source 17, test 6, SQL 0, script 2
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-OPSDB` — PFW 운영 DB 공유/장애모드

- 상태: `재확인 필요`
- 구현 신호: source 26, test 9, SQL 4, script 2
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-LOGDB` — DB 로그/Segment 로그

- 상태: `부분 구현`
- 구현 신호: source 51, test 14, SQL 0, script 0
- 완료 조건:
  - 정상·오류·timeout·retry·target-down·local/remote·async·batch·worker 시나리오의 실제 file/DB log를 생성한다.
  - transactionGlobalId/segment hierarchy, 누락·중복 0, masking, rotation·retention·spool/fallback을 검증한다.

### `PFW-FILELOG` — cpf-{moduleCode}-{logType}.log 파일 로그

- 상태: `미검증`
- 구현 신호: source 52, test 15, SQL 0, script 0
- 완료 조건:
  - 정상·오류·timeout·retry·target-down·local/remote·async·batch·worker 시나리오의 실제 file/DB log를 생성한다.
  - transactionGlobalId/segment hierarchy, 누락·중복 0, masking, rotation·retention·spool/fallback을 검증한다.

### `PFW-LOGFAIL` — 로그 실패/fail-open/local spool

- 상태: `부분 구현`
- 구현 신호: source 52, test 14, SQL 2, script 0
- 완료 조건:
  - 정상·오류·timeout·retry·target-down·local/remote·async·batch·worker 시나리오의 실제 file/DB log를 생성한다.
  - transactionGlobalId/segment hierarchy, 누락·중복 0, masking, rotation·retention·spool/fallback을 검증한다.

### `PFW-TRACE` — Trace Boost/동적 로그 레벨

- 상태: `재확인 필요`
- 구현 신호: source 13, test 1, SQL 2, script 2
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-MASK` — 마스킹/민감정보 보호

- 상태: `재확인 필요`
- 구현 신호: source 5, test 2, SQL 0, script 0
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-ERROR` — 오류/예외/응답 표준

- 상태: `재확인 필요`
- 구현 신호: source 49, test 1, SQL 0, script 0
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-VALID` — Validation Framework

- 상태: `재확인 필요`
- 구현 신호: source 11, test 5, SQL 0, script 0
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-IDEMP` — Idempotency 표준

- 상태: `재확인 필요`
- 구현 신호: source 14, test 5, SQL 0, script 0
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-STATE` — 상태 전이 State Machine

- 상태: `재확인 필요`
- 구현 신호: source 8, test 1, SQL 0, script 0
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-LOCK` — Optimistic/Distributed Lock

- 상태: `재확인 필요`
- 구현 신호: source 6, test 1, SQL 0, script 0
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `PFW-SCHED` — Scheduler 표준

- 상태: `부분 구현`
- 구현 신호: source 3, test 0, SQL 2, script 0
- 완료 조건:
  - PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
  - 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.

### `CMN-CODE` — 공통코드/참조데이터

- 상태: `재확인 필요`
- 구현 신호: source 26, test 3, SQL 1, script 0
- 완료 조건:
  - CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
  - API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.

### `CMN-MSG` — 공통 메시지/다국어/오류 메시지

- 상태: `재확인 필요`
- 구현 신호: source 34, test 1, SQL 0, script 0
- 완료 조건:
  - CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
  - API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.

### `CMN-ID` — 채번/분산 ID

- 상태: `재확인 필요`
- 구현 신호: source 4, test 3, SQL 0, script 0
- 완료 조건:
  - CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
  - API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.

### `CMN-FILE` — 파일/다운로드/업로드/Object Storage

- 상태: `재확인 필요`
- 구현 신호: source 54, test 14, SQL 1, script 3
- 완료 조건:
  - CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
  - API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.

### `CMN-FIXED` — 고정길이 전문 parser/formatter/layout

- 상태: `재확인 필요`
- 구현 신호: source 24, test 2, SQL 1, script 1
- 완료 조건:
  - CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
  - API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.

### `CMN-CALENDAR` — 영업일/휴일/기관 캘린더

- 상태: `부분 구현`
- 구현 신호: source 1, test 0, SQL 0, script 0
- 완료 조건:
  - CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
  - API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.

### `CMN-TEMPLATE` — 템플릿/알림

- 상태: `부분 구현`
- 구현 신호: source 12, test 0, SQL 1, script 0
- 완료 조건:
  - CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
  - API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.

### `ADM-AUTH` — ADM 인증/세션/계정 생명주기

- 상태: `재확인 필요`
- 구현 신호: source 23, test 6, SQL 2, script 0
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-RBAC` — RBAC/ABAC/권한 메타

- 상태: `재확인 필요`
- 구현 신호: source 17, test 3, SQL 2, script 3
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-AUDIT` — 감사/보안 이벤트/부인방지

- 상태: `재확인 필요`
- 구현 신호: source 7, test 1, SQL 0, script 0
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-TX` — 거래 그룹 목록/상세

- 상태: `재확인 필요`
- 구현 신호: source 129, test 34, SQL 10, script 7
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-TIMELINE` — 통합 Timeline

- 상태: `재확인 필요`
- 구현 신호: source 14, test 1, SQL 3, script 0
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-SERVICE` — Service Instance/Routing/Health 관제

- 상태: `재확인 필요`
- 구현 신호: source 136, test 42, SQL 5, script 10
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-LOG` — 로그 정책/Trace Boost 화면

- 상태: `재확인 필요`
- 구현 신호: source 43, test 6, SQL 2, script 2
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-BATCH` — Batch/Worker/Heartbeat/Ghost 관제

- 상태: `부분 구현`
- 구현 신호: source 57, test 10, SQL 8, script 0
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-CENTER` — Center-Cut 관제

- 상태: `재확인 필요`
- 구현 신호: source 25, test 11, SQL 3, script 2
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-EXS` — 대외연계 관제

- 상태: `재확인 필요`
- 구현 신호: source 10, test 3, SQL 0, script 0
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-COMP` — Compensation/Manual Recovery 관제

- 상태: `부분 구현`
- 구현 신호: source 6, test 0, SQL 2, script 0
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-INCIDENT` — Incident/Alert/Runbook

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
  - 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.

### `ADM-UX` — ADM UX/검색/다운로드/대시보드

- 상태: `미구현`
- 구현 신호: source 7, test 1, SQL 1, script 0
- 완료 조건:
  - ADM/BZA의 Vue Global Build·대형 단일 JS를 Vue 3 SFC·TypeScript·feature architecture로 전환한다.
  - lint·typecheck·unit/component/API mock·production build·JAR/WAR packaging·실제 browser E2E를 완료한다.

### `BAT-CORE` — BAT standalone worker/application

- 상태: `미구현`
- 구현 신호: source 14, test 1, SQL 2, script 0
- 완료 조건:
  - 독립 `BatWorkerApplication` 또는 동등 entry point, registration, heartbeat, capability, claim/lease, drain, crash recovery를 구현한다.
  - 서로 다른 process/instanceId의 2개 worker로 중복 claim 방지·lease timeout·takeover·restart recovery를 실제 검증한다.

### `BAT-JOB` — Job/Step/Parameter/Dependency

- 상태: `재확인 필요`
- 구현 신호: source 25, test 8, SQL 1, script 0
- 완료 조건:
  - BAT control plane과 독립 worker runtime의 ownership을 분리하고 Job/Step/Item 상태·재시작·재처리를 영속화한다.
  - 다중 worker, claim/lease, crash, drain, takeover, 중복 실행 방지와 ADM 관제를 runtime으로 검증한다.

### `BAT-ITEM` — Batch item claim/retry/skip/rerun

- 상태: `재확인 필요`
- 구현 신호: source 6, test 4, SQL 1, script 0
- 완료 조건:
  - BAT control plane과 독립 worker runtime의 ownership을 분리하고 Job/Step/Item 상태·재시작·재처리를 영속화한다.
  - 다중 worker, claim/lease, crash, drain, takeover, 중복 실행 방지와 ADM 관제를 runtime으로 검증한다.

### `BAT-CALL-SYNC` — BAT → 주제영역 동기 호출

- 상태: `재확인 필요`
- 구현 신호: source 43, test 9, SQL 0, script 2
- 완료 조건:
  - BAT control plane과 독립 worker runtime의 ownership을 분리하고 Job/Step/Item 상태·재시작·재처리를 영속화한다.
  - 다중 worker, claim/lease, crash, drain, takeover, 중복 실행 방지와 ADM 관제를 runtime으로 검증한다.

### `BAT-CALL-ASYNC` — BAT → Event/Outbox 비동기 호출

- 상태: `재확인 필요`
- 구현 신호: source 11, test 1, SQL 0, script 1
- 완료 조건:
  - BAT control plane과 독립 worker runtime의 ownership을 분리하고 Job/Step/Item 상태·재시작·재처리를 영속화한다.
  - 다중 worker, claim/lease, crash, drain, takeover, 중복 실행 방지와 ADM 관제를 runtime으로 검증한다.

### `BAT-SHARED` — BAT → SHARED/온라인 Facade 재사용

- 상태: `재확인 필요`
- 구현 신호: source 13, test 4, SQL 0, script 0
- 완료 조건:
  - BAT control plane과 독립 worker runtime의 ownership을 분리하고 Job/Step/Item 상태·재시작·재처리를 영속화한다.
  - 다중 worker, claim/lease, crash, drain, takeover, 중복 실행 방지와 ADM 관제를 runtime으로 검증한다.

### `CENTER-CORE` — Center-Cut 기본 구현

- 상태: `재확인 필요`
- 구현 신호: source 25, test 11, SQL 3, script 2
- 완료 조건:
  - PFW contract, BAT 기본 target/item/result 구현, 업무 domain adapter를 분리한다.
  - chunk·동시성·부분 실패·재시작·재처리·보상·진행률·ADM 제어를 검증한다.

### `CENTER-ADV` — Center-Cut 고급 패턴

- 상태: `재확인 필요`
- 구현 신호: source 26, test 11, SQL 1, script 0
- 완료 조건:
  - PFW contract, BAT 기본 target/item/result 구현, 업무 domain adapter를 분리한다.
  - chunk·동시성·부분 실패·재시작·재처리·보상·진행률·ADM 제어를 검증한다.

### `EXS-INST` — 기관/Endpoint/Profile

- 상태: `부분 구현`
- 구현 신호: source 5, test 0, SQL 0, script 1
- 완료 조건:
  - EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
  - timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.

### `EXS-REST` — EXS REST 송수신

- 상태: `재확인 필요`
- 구현 신호: source 12, test 5, SQL 0, script 0
- 완료 조건:
  - EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
  - timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.

### `EXS-FIXED` — EXS 고정길이 전문 송수신

- 상태: `재확인 필요`
- 구현 신호: source 23, test 2, SQL 1, script 1
- 완료 조건:
  - EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
  - timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.

### `EXS-SEC` — EXS OAuth/JWT/mTLS/인증서

- 상태: `부분 구현`
- 구현 신호: source 12, test 0, SQL 0, script 0
- 완료 조건:
  - EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
  - timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.

### `EXS-UNKNOWN` — Unknown Result 처리

- 상태: `부분 구현`
- 구현 신호: source 35, test 2, SQL 0, script 0
- 완료 조건:
  - EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
  - timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.

### `EXS-RECON` — 대외 Reconciliation

- 상태: `부분 구현`
- 구현 신호: source 5, test 3, SQL 0, script 0
- 완료 조건:
  - EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
  - timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.

### `EXS-FILE` — SFTP/File Transfer 연계 후보

- 상태: `부분 구현`
- 구현 신호: source 30, test 6, SQL 0, script 0
- 완료 조건:
  - EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
  - timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.

### `EVENT-CORE` — Event/Message Envelope

- 상태: `재확인 필요`
- 구현 신호: source 46, test 1, SQL 0, script 1
- 완료 조건:
  - 표준 envelope, schema/version, outbox/inbox, broker adapter, DLQ/replay의 데이터 계약과 트랜잭션 경계를 완성한다.
  - 중복·순서·poison message·backpressure·broker 장애·재기동을 검증한다.

### `EVENT-OUTBOX` — Outbox/Inbox

- 상태: `재확인 필요`
- 구현 신호: source 3, test 1, SQL 0, script 0
- 완료 조건:
  - 표준 envelope, schema/version, outbox/inbox, broker adapter, DLQ/replay의 데이터 계약과 트랜잭션 경계를 완성한다.
  - 중복·순서·poison message·backpressure·broker 장애·재기동을 검증한다.

### `EVENT-BROKER` — Kafka/MQ/Redis real broker

- 상태: `미검증`
- 구현 신호: source 30, test 4, SQL 1, script 0
- 완료 조건:
  - Kafka·RabbitMQ 등 지원 broker의 실제 adapter와 local container/harness를 제공한다.
  - broker down, redelivery, ordering, duplicate, poison, DLQ, restart와 multi-instance를 검증한다.

### `EVENT-DLQ` — DLQ/Replay/Poison Message

- 상태: `부분 구현`
- 구현 신호: source 4, test 0, SQL 1, script 0
- 완료 조건:
  - 표준 envelope, schema/version, outbox/inbox, broker adapter, DLQ/replay의 데이터 계약과 트랜잭션 경계를 완성한다.
  - 중복·순서·poison message·backpressure·broker 장애·재기동을 검증한다.

### `SAGA-CORE` — Saga/분산 거래 표준

- 상태: `부분 구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - orchestration/choreography 선택 기준, 상태 저장, compensation idempotency, manual recovery와 audit를 구현한다.
  - 부분 성공·중복 보상·보상 실패·재기동·operator 조치를 runtime으로 검증한다.

### `SAGA-COMP` — Compensation

- 상태: `부분 구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - orchestration/choreography 선택 기준, 상태 저장, compensation idempotency, manual recovery와 audit를 구현한다.
  - 부분 성공·중복 보상·보상 실패·재기동·operator 조치를 runtime으로 검증한다.

### `SAGA-MANUAL` — Manual Recovery/Adjustment

- 상태: `부분 구현`
- 구현 신호: source 6, test 0, SQL 2, script 0
- 완료 조건:
  - orchestration/choreography 선택 기준, 상태 저장, compensation idempotency, manual recovery와 audit를 구현한다.
  - 부분 성공·중복 보상·보상 실패·재기동·operator 조치를 runtime으로 검증한다.

### `SEC-AUTHN` — 인증/AuthN

- 상태: `재확인 필요`
- 구현 신호: source 18, test 5, SQL 2, script 0
- 완료 조건:
  - 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
  - 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.

### `SEC-AUTHZ` — 권한/AuthZ/RBAC/ABAC

- 상태: `재확인 필요`
- 구현 신호: source 18, test 3, SQL 2, script 3
- 완료 조건:
  - 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
  - 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.

### `SEC-SECRET` — Secret/Vault/Key Rotation

- 상태: `부분 구현`
- 구현 신호: source 1, test 0, SQL 0, script 0
- 완료 조건:
  - 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
  - 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.

### `SEC-CERT` — Certificate/mTLS 관리

- 상태: `부분 구현`
- 구현 신호: source 1, test 0, SQL 0, script 0
- 완료 조건:
  - 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
  - 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.

### `SEC-PRIVACY` — 개인정보/Privacy/Data Classification

- 상태: `재확인 필요`
- 구현 신호: source 5, test 2, SQL 0, script 0
- 완료 조건:
  - 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
  - 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.

### `SEC-DOWNLOAD` — Download Governance

- 상태: `재확인 필요`
- 구현 신호: source 7, test 1, SQL 1, script 5
- 완료 조건:
  - 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
  - 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.

### `SEC-APP` — Application Security 기본 통제

- 상태: `재확인 필요`
- 구현 신호: source 23, test 3, SQL 1, script 1
- 완료 조건:
  - 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
  - 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.

### `SEC-APPROVAL` — Approval/Dual Control/Break-glass

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
  - 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.

### `OPS-METRIC` — Metrics/Observability

- 상태: `재확인 필요`
- 구현 신호: source 3, test 2, SQL 0, script 0
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-SLO` — SLI/SLO/Error Budget

- 상태: `미구현`
- 구현 신호: source 4, test 2, SQL 0, script 0
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-ALERT` — Alert Rule Engine

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-INCIDENT` — Incident Management

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-RUNBOOK` — Runbook/자동 진단

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 1
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-SELF` — Self-healing

- 상태: `미구현`
- 구현 신호: source 6, test 0, SQL 2, script 0
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-TOPOLOGY` — Topology/Dependency Map/Service Catalog

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-MAINT` — Maintenance Mode/Drain

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-CONFIG` — Config/Policy/Runtime Override

- 상태: `재확인 필요`
- 구현 신호: source 82, test 14, SQL 4, script 3
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-DRIFT` — Config Drift/Policy Versioning

- 상태: `부분 구현`
- 구현 신호: source 1, test 0, SQL 0, script 0
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-CAPACITY` — Performance/Capacity/Resource Governance

- 상태: `재확인 필요`
- 구현 신호: source 0, test 2, SQL 0, script 1
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `OPS-DR` — DR/Backup/Restore/Archive

- 상태: `부분 구현`
- 구현 신호: source 11, test 5, SQL 1, script 0
- 완료 조건:
  - metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
  - 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.

### `REL-BUILD` — Build/Artifact/Provenance

- 상태: `재확인 필요`
- 구현 신호: source 9, test 4, SQL 0, script 0
- 완료 조건:
  - clean build부터 artifact·checksum·SBOM·provenance·deploy·rollback·migration·compatibility까지 재현 가능한 release chain을 완성한다.
  - clean checkout과 package inspection, upgrade/rollback smoke를 evidence로 남긴다.

### `REL-DEPLOY` — Deployment/Rollback/Feature Flag

- 상태: `부분 구현`
- 구현 신호: source 1, test 1, SQL 0, script 0
- 완료 조건:
  - clean build부터 artifact·checksum·SBOM·provenance·deploy·rollback·migration·compatibility까지 재현 가능한 release chain을 완성한다.
  - clean checkout과 package inspection, upgrade/rollback smoke를 evidence로 남긴다.

### `REL-MIG` — DB Migration/Flyway/Expand-Contract

- 상태: `부분 구현`
- 구현 신호: source 1, test 3, SQL 35, script 1
- 완료 조건:
  - clean build부터 artifact·checksum·SBOM·provenance·deploy·rollback·migration·compatibility까지 재현 가능한 release chain을 완성한다.
  - clean checkout과 package inspection, upgrade/rollback smoke를 evidence로 남긴다.

### `REL-COMPAT` — API/Event/전문 호환성

- 상태: `부분 구현`
- 구현 신호: source 1, test 0, SQL 0, script 0
- 완료 조건:
  - clean build부터 artifact·checksum·SBOM·provenance·deploy·rollback·migration·compatibility까지 재현 가능한 release chain을 완성한다.
  - clean checkout과 package inspection, upgrade/rollback smoke를 evidence로 남긴다.

### `DB-SQL` — SQL 표준/MyBatis/Index

- 상태: `재확인 필요`
- 구현 신호: source 15, test 4, SQL 51, script 3
- 완료 조건:
  - canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
  - 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.

### `DB-INSTALL` — MariaDB 신규 빈 DB Full Install

- 상태: `재확인 필요`
- 구현 신호: source 7, test 2, SQL 4, script 34
- 완료 조건:
  - canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
  - 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.

### `DB-PERF` — DB 성능/Partition/Retention

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
  - 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.

### `DB-MULTI` — Multi Datasource/Read Replica

- 상태: `부분 구현`
- 구현 신호: source 7, test 2, SQL 0, script 0
- 완료 조건:
  - canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
  - 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.

### `DATA-LINEAGE` — Data Lineage/Quality/Reconciliation

- 상태: `재확인 필요`
- 구현 신호: source 5, test 3, SQL 1, script 0
- 완료 조건:
  - canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
  - 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.

### `DATA-RETENTION` — Retention/Purge/Legal Hold/Archive

- 상태: `재확인 필요`
- 구현 신호: source 11, test 5, SQL 1, script 0
- 완료 조건:
  - canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
  - 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.

### `API-CONTRACT` — API Contract/OpenAPI/Developer Portal

- 상태: `재확인 필요`
- 구현 신호: source 147, test 11, SQL 0, script 3
- 완료 조건:
  - URI·executionId·request/response/error·header·permission·paging contract를 OpenAPI와 runtime에 일치시킨다.
  - 하위 호환, rate/size limit, abuse, 대량 조회·다운로드 guard를 검증한다.

### `API-GATEWAY` — Gateway/L4/WAF/Service Mesh 연계

- 상태: `재확인 필요`
- 구현 신호: source 9, test 4, SQL 0, script 1
- 완료 조건:
  - URI·executionId·request/response/error·header·permission·paging contract를 OpenAPI와 runtime에 일치시킨다.
  - 하위 호환, rate/size limit, abuse, 대량 조회·다운로드 guard를 검증한다.

### `API-LIMIT` — Rate Limit/Quota/Abuse Detection

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - URI·executionId·request/response/error·header·permission·paging contract를 OpenAPI와 runtime에 일치시킨다.
  - 하위 호환, rate/size limit, abuse, 대량 조회·다운로드 guard를 검증한다.

### `API-PAGING` — Pagination/Search/Sort/Download Guard

- 상태: `재확인 필요`
- 구현 신호: source 12, test 2, SQL 1, script 0
- 완료 조건:
  - URI·executionId·request/response/error·header·permission·paging contract를 OpenAPI와 runtime에 일치시킨다.
  - 하위 호환, rate/size limit, abuse, 대량 조회·다운로드 guard를 검증한다.

### `SAMPLE-ACC` — ACC 실전형 샘플

- 상태: `재확인 필요`
- 구현 신호: source 26, test 6, SQL 0, script 0
- 완료 조건:
  - 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
  - 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.

### `SAMPLE-MBR` — MBR 실전형 샘플

- 상태: `재확인 필요`
- 구현 신호: source 20, test 9, SQL 0, script 0
- 완료 조건:
  - 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
  - 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.

### `SAMPLE-BIZADM` — BIZADM 업무관리 샘플

- 상태: `재확인 필요`
- 구현 신호: source 19, test 5, SQL 0, script 0
- 완료 조건:
  - 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
  - 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.

### `SAMPLE-EDU` — EDU 교육 샘플

- 상태: `재확인 필요`
- 구현 신호: source 101, test 61, SQL 4, script 4
- 완료 조건:
  - 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
  - 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.

### `SAMPLE-XYZ` — XYZ 신규 주제영역 샘플

- 상태: `재확인 필요`
- 구현 신호: source 77, test 36, SQL 0, script 0
- 완료 조건:
  - 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
  - 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.

### `ONBOARD-DOMAIN` — 신규 주제영역 온보딩

- 상태: `실패`
- 구현 신호: source 1, test 1, SQL 0, script 5
- 완료 조건:
  - ACC를 disposable worktree에서 snapshot→삭제→잔존 0→순수 생성→compile/test/DB/runtime→기존 ACC diff→재생성 parity로 검증한다.
  - 미선택 capability 파일 0건, ownership manifest, 사용자 수정 보호, DB vendor-neutral generator를 완료한다.

### `DEVEX-QUICK` — Developer Quickstart/Local Dev

- 상태: `재확인 필요`
- 구현 신호: source 9, test 4, SQL 0, script 0
- 완료 조건:
  - 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
  - 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.

### `DEVEX-CODEGEN` — Scaffold/Code Generation Governance

- 상태: `실패`
- 구현 신호: source 1, test 1, SQL 0, script 4
- 완료 조건:
  - ACC를 disposable worktree에서 snapshot→삭제→잔존 0→순수 생성→compile/test/DB/runtime→기존 ACC diff→재생성 parity로 검증한다.
  - 미선택 capability 파일 0건, ownership manifest, 사용자 수정 보호, DB vendor-neutral generator를 완료한다.

### `DEVEX-COMMENT` — 한글 주석/설정 주석 표준

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
  - 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.

### `RULE-ARCH` — Architecture Rule Check

- 상태: `재확인 필요`
- 구현 신호: source 0, test 0, SQL 0, script 3
- 완료 조건:
  - 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
  - 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.

### `RULE-SEC` — Security/Secret/URL Scan

- 상태: `재확인 필요`
- 구현 신호: source 26, test 3, SQL 1, script 1
- 완료 조건:
  - 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
  - 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.

### `RULE-QUALITY` — Static Analysis/Dependency/License

- 상태: `재확인 필요`
- 구현 신호: source 0, test 0, SQL 1, script 2
- 완료 조건:
  - 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
  - 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.

### `TEST-UNIT` — Unit/Slice/Integration Test

- 상태: `재확인 필요`
- 구현 신호: source 1, test 158, SQL 1, script 0
- 완료 조건:
  - 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
  - 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.

### `TEST-CONTRACT` — Contract/Compatibility Test

- 상태: `재확인 필요`
- 구현 신호: source 2, test 158, SQL 1, script 0
- 완료 조건:
  - 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
  - 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.

### `TEST-RUNTIME` — Runtime Smoke

- 상태: `재확인 필요`
- 구현 신호: source 30, test 6, SQL 4, script 41
- 완료 조건:
  - 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
  - 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.

### `TEST-BROWSER` — Browser Click E2E

- 상태: `미검증`
- 구현 신호: source 2, test 0, SQL 0, script 8
- 완료 조건:
  - ADM과 BZA의 인증·권한·검색·상세·조치·오류·session expiry를 실제 browser로 클릭한다.
  - screenshot, network metadata, console error와 basis SHA를 evidence에 남긴다.

### `TEST-BROKER` — Real Broker/Multi-instance Test

- 상태: `미검증`
- 구현 신호: source 30, test 4, SQL 1, script 0
- 완료 조건:
  - 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
  - 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.

### `TEST-FAULT` — Fault Injection/Chaos Smoke

- 상태: `미검증`
- 구현 신호: source 7, test 1, SQL 0, script 0
- 완료 조건:
  - 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
  - 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.

### `TEST-EVIDENCE` — Evidence Index/Consistency Gate

- 상태: `재확인 필요`
- 구현 신호: source 0, test 1, SQL 0, script 5
- 완료 조건:
  - 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
  - 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.

### `DOC-GOV` — 문서 최소화/정본화 후순위

- 상태: `부분 구현`
- 구현 신호: source 1, test 0, SQL 51, script 1
- 완료 조건:
  - 기존 정본 역할을 유지하고 중복·stale 문서는 내용을 통합한 뒤 삭제한다.
  - Markdown 렌더링, link, heading, table, code fence, UTF-8과 source 정합성을 검증한다.

### `DOC-PRODUCT` — 제품화 문서/설치/운영/개발자 문서

- 상태: `부분 구현`
- 구현 신호: source 19, test 7, SQL 7, script 4
- 완료 조건:
  - 기존 정본 역할을 유지하고 중복·stale 문서는 내용을 통합한 뒤 삭제한다.
  - Markdown 렌더링, link, heading, table, code fence, UTF-8과 source 정합성을 검증한다.

### `PROD-EDITION` — 상용화/Edition/License 후보

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - 상용 제품 후보 기능을 단순 문구가 아니라 extension contract, tenancy/license/package 경계와 운영 정책으로 설계·구현한다.
  - 미구현이면 명확히 미구현으로 기록하고 placeholder·과장 문서를 제거한다.

### `PROD-MULTITENANT` — Multi-tenant/Multi-customer 후보

- 상태: `미구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - 정본이 최종 제품 필수로 유지하는지 우선 확정하고, 유지 시 source contract·보안·운영·테스트까지 구현한다.
  - 후보 문구만 존재하는 상태를 완료로 기록하지 않고 미구현 또는 명시적 제외 근거를 남긴다.

### `PROD-PLUGIN` — Plugin/Adapter/Marketplace 후보

- 상태: `미구현`
- 구현 신호: source 13, test 10, SQL 0, script 1
- 완료 조건:
  - 정본이 최종 제품 필수로 유지하는지 우선 확정하고, 유지 시 source contract·보안·운영·테스트까지 구현한다.
  - 후보 문구만 존재하는 상태를 완료로 기록하지 않고 미구현 또는 명시적 제외 근거를 남긴다.

### `PROD-PACKAGE` — 산업군별 패키지 후보

- 상태: `미구현`
- 구현 신호: source 2, test 0, SQL 0, script 1
- 완료 조건:
  - 정본이 최종 제품 필수로 유지하는지 우선 확정하고, 유지 시 source contract·보안·운영·테스트까지 구현한다.
  - 후보 문구만 존재하는 상태를 완료로 기록하지 않고 미구현 또는 명시적 제외 근거를 남긴다.

### `REQ-GOV` — 요건 ID/우선순위/추적성

- 상태: `부분 구현`
- 구현 신호: source 0, test 0, SQL 0, script 2
- 완료 조건:
  - 정본 requirement ID와 implementation/evidence의 양방향 traceability를 자동 생성한다.
  - 중복 check ID, 과대 완료, stale evidence와 미소유 구현을 차단한다.

### `REQ-REVIEW` — ChatGPT 검수/완료 불인정 기준

- 상태: `부분 구현`
- 구현 신호: source 1, test 0, SQL 0, script 0
- 완료 조건:
  - 정본 requirement ID와 implementation/evidence의 양방향 traceability를 자동 생성한다.
  - 중복 check ID, 과대 완료, stale evidence와 미소유 구현을 차단한다.

### `REQ-CODEX` — Codex 요청서 작성 기준

- 상태: `부분 구현`
- 구현 신호: source 0, test 0, SQL 0, script 0
- 완료 조건:
  - 정본 requirement ID와 implementation/evidence의 양방향 traceability를 자동 생성한다.
  - 중복 check ID, 과대 완료, stale evidence와 미소유 구현을 차단한다.

### `REQ-GAP` — Future Requirement Gap Intake

- 상태: `부분 구현`
- 구현 신호: source 0, test 0, SQL 0, script 2
- 완료 조건:
  - 정본 requirement ID와 implementation/evidence의 양방향 traceability를 자동 생성한다.
  - 중복 check ID, 과대 완료, stale evidence와 미소유 구현을 차단한다.
