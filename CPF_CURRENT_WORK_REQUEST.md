# CPF 통합 대형 마일스톤 Codex 작업 요청서

## 1. 요청 목적

이 요청서는 사용자 지적사항, 직전 작업 직접 검수 결과,
`CPF_FINAL_TARGET_REQUIREMENTS.md`의 133개 목표 도메인과 최신 `master`
SHA `42c0fda82e0f3061e839f69cad25bbfa9df2aa0f`의 source·test·SQL·script·frontend·evidence inventory를
대조하여 작성한 단일 전체 작업 요청서다.

일부 기능만 수행하는 회차형 요청서가 아니다.
아래 133개 도메인의 모든 `부분 구현`, `미구현`, `미검증`, `실패`,
`재확인 필요` 항목을 같은 대형 마일스톤에서 구현·검증·정리한다.

우선순위는 실행 순서에만 사용한다.
분량이나 환경을 이유로 범위를 다음 요청으로 넘기지 않는다.

## 2. 기준

- repository: `freeangelsun/202412_01_CPF`
- branch: `master`
- 검수 기준 SHA: `42c0fda82e0f3061e839f69cad25bbfa9df2aa0f`
- 최상위 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 활성 요청서: `CPF_CURRENT_WORK_REQUEST.md`
- 검수 기준: `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 현재 상태: `CPF_STABILIZATION_REPORT.md`
- gap: `CPF_GAP_MATRIX.md`
- evidence: `CPF_EVIDENCE_INDEX.md`
- 기능 matrix: `specs/기능_구현_매트릭스.json`
- EDU matrix: `specs/sample-coverage-matrix.md`

현재 repository에는 분할 정본 파일이 없다.
존재하지 않는 파일을 참조하거나 새 정본으로 만들지 않는다.

## 3. 최신 inventory

제출된 architecture inventory 기준:

- module: 9개
- inventory file: 1,118개
- source: 739개
- test: 158개
- SQL: 51개
- script: 81개
- resource: 71개
- deploy: 4개
- manifest: 3개

파일이나 class 존재는 완료 근거가 아니다.
아래 구현 신호는 검토 시작점이며 실제 contract·consumer·runtime을 재확인한다.

## 4. 작업 시작 전 필수 절차

1. 최신 `origin/master`와 local HEAD를 확인한다.
2. 정본 133개 도메인의 모든 상세 카드를 requirement ID 단위로 추출한다.
3. 최신 repository 전체 implementation inventory를 다시 생성한다.
4. 양방향 traceability를 만든다.
   - Requirement → Source/Test/SQL/API/Runtime/Evidence
   - Implementation → Requirement/Owner/Consumer
5. 상태 하향·신규 gap·삭제 대상과 보호 대상을 먼저 기록한다.
6. 구현 후 runtime/evidence를 생성하고 상태 문서는 마지막에 갱신한다.

## 5. 완료 불인정

- source·class·package 존재
- catalog·sampleId 등록
- 일부 unit test
- 정적 grep
- Swagger UI open
- heartbeat sample
- 임시 PYM/LNG/TMP generator smoke
- 설정 문자열
- 과거 SHA evidence
- Codex 성공 보고
- 문서상 완료 표기

## 6. 직전 검수에서 확정된 중대 결함

- ACC 자체 delete→generate lifecycle 미수행
- 독립 Batch Agent/Worker runtime 미확인
- ADM/BZA Vue Global Build와 대형 단일 JavaScript
- Controller·Service 중심의 불완전한 계층 gate
- 중앙 SQL과 module-local SQL의 canonical source 미정리
- generator의 MariaDB/MYSQL 하드코딩
- 실제 file log와 DB log parity evidence 부족
- PFW default/property/typed option/SPI 전수 검증 부족
- 대외연계 결과 불명·대사·장애 복구 검증 부족
- EDU catalog·class 존재 중심 완료 판정
- stale evidence·semantic garbage 완료 판정 결함

## 7. 공통 완료 묶음

모든 도메인은 필요한 다음 요소를 같은 작업에서 완료한다.

- source와 계층 연결
- public contract와 extension point
- 안전한 default
- type-safe property
- typed option·strategy·SPI
- 정상·오류·경계·부분 실패·복구
- 멱등성·동시성·다중 인스턴스
- SQL·migration·install·upgrade·rollback
- security·permission·audit·masking
- ADM 운영 조회·제어
- EDU·OpenAPI·JavaDoc
- unit·integration·runtime·browser
- evidence
- matrix·report·GAP·index·README 정합성
- semantic garbage 정리

필요하지 않은 요소는 이유와 대체 검증을 남긴다.

# 8. 정본 133개 도메인별 구현·검증 요구

## 8.001. `ARCH-MISSION` — CPF 최종 목표/상용 솔루션 원칙

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 2
- resource: 0

확인 시작 파일:
- `pfw/build.gradle`
- `cmn/build.gradle`
- `adm/build.gradle`
- `bza/build.gradle`

**필수 보완**

- 정본의 ownership·dependency·배포 topology와 실제 module/build/package 구조를 일치시킨다.
- 동일 JVM과 분리 WAS에서 동일 contract를 사용하고 금지 dependency를 architecture gate로 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.002. `ARCH-MSA` — MSA-first 및 Modular Monolith 호환

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 80
- test: 20
- SQL: 0
- script: 1
- resource: 9

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`
- `pfw/src/main/java/cpf/pfw/common/archive/LocalCpfArchiveService.java`
- `pfw/src/main/java/cpf/pfw/common/attachment/LocalCpfAttachmentStorageAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/base/CpfApplicationFacade.java`

**필수 보완**

- 정본의 ownership·dependency·배포 topology와 실제 module/build/package 구조를 일치시킨다.
- 동일 JVM과 분리 WAS에서 동일 contract를 사용하고 금지 dependency를 architecture gate로 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.003. `ARCH-BOUNDARY` — 주제영역 경계/Bounded Context

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 76
- test: 15
- SQL: 2
- script: 8
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
- `pfw/src/main/java/cpf/pfw/api/reliability/CpfReliabilityOperationsPort.java`
- `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`

**필수 보완**

- 정본의 ownership·dependency·배포 topology와 실제 module/build/package 구조를 일치시킨다.
- 동일 JVM과 분리 WAS에서 동일 contract를 사용하고 금지 dependency를 architecture gate로 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.004. `ARCH-LAYER` — 계층/패키지/의존성 규칙

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 64
- test: 16
- SQL: 1
- script: 2
- resource: 10

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/channel/adapter/JdbcCpfChannelRegistryAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/attachment/LocalCpfAttachmentStorageAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/base/BaseController.java`
- `pfw/src/main/java/cpf/pfw/common/base/BaseService.java`

**필수 보완**

- 정본의 ownership·dependency·배포 topology와 실제 module/build/package 구조를 일치시킨다.
- 동일 JVM과 분리 WAS에서 동일 contract를 사용하고 금지 dependency를 architecture gate로 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.005. `FACADE-LOCAL` — Local Facade 표준

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 19
- test: 7
- SQL: 0
- script: 0
- resource: 8

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/archive/LocalCpfArchiveService.java`
- `pfw/src/main/java/cpf/pfw/common/attachment/LocalCpfAttachmentStorageAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/base/CpfApplicationFacade.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/LocalCpfFileTransferAdapter.java`

**필수 보완**

- typed request/response, local/remote parity, endpoint/instance 선택, timeout·오류 계약을 source와 runtime으로 완성한다.
- consumer domain에서 internal package를 직접 참조하지 않도록 public API/SPI와 migration을 제공한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.006. `FACADE-REMOTE` — Remote Facade Proxy/Port-Adapter

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 114
- test: 19
- SQL: 1
- script: 7
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
- `pfw/src/main/java/cpf/pfw/api/reliability/CpfReliabilityOperationsPort.java`
- `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`

**필수 보완**

- typed request/response, local/remote parity, endpoint/instance 선택, timeout·오류 계약을 source와 runtime으로 완성한다.
- consumer domain에서 internal package를 직접 참조하지 않도록 public API/SPI와 migration을 제공한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.007. `PFW-CALL` — CpfWebClient/CpfRestClient Service Call Engine

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 42
- test: 8
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfHttpClientProperties.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfLocalServiceIdentity.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfRestClientInterceptor.java`

**필수 보완**

- typed request/response, local/remote parity, endpoint/instance 선택, timeout·오류 계약을 source와 runtime으로 완성한다.
- consumer domain에서 internal package를 직접 참조하지 않도록 public API/SPI와 migration을 제공한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.008. `PFW-REGISTRY` — Service/Endpoint/Instance Registry

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 17
- test: 1
- SQL: 4
- script: 4
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`
- `pfw/src/main/java/cpf/pfw/channel/adapter/JdbcCpfChannelRegistryAdapter.java`
- `pfw/src/main/java/cpf/pfw/channel/api/CpfChannelRegistryPort.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfServiceEndpointRegistry.java`

**필수 보완**

- typed request/response, local/remote parity, endpoint/instance 선택, timeout·오류 계약을 source와 runtime으로 완성한다.
- consumer domain에서 internal package를 직접 참조하지 않도록 public API/SPI와 migration을 제공한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.009. `PFW-ROUTING` — LB mode/direct instance/discovery routing

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 10
- test: 2
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferEndpoint.java`
- `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayRoute.java`
- `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayRouteCatalog.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfServiceEndpointProperties.java`

**필수 보완**

- typed request/response, local/remote parity, endpoint/instance 선택, timeout·오류 계약을 source와 runtime으로 완성한다.
- consumer domain에서 internal package를 직접 참조하지 않도록 public API/SPI와 migration을 제공한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.010. `PFW-HEALTH` — Liveness/Readiness/Dependency Health

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 10
- test: 2
- SQL: 0
- script: 1
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/admin/CpfRuntimeHealthStatusQuery.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerHealthPort.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferHealthPort.java`
- `pfw/src/main/java/cpf/pfw/common/runtime/CpfHealthCheckPort.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.011. `PFW-RESILIENCE` — Timeout/Retry/Circuit/Bulkhead/Backpressure

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 3
- test: 2
- SQL: 1
- script: 1
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferRetryPolicy.java`
- `bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutRetryEducationSample.java`
- `bat/src/main/java/cpf/bat/edu/retry/BatRetryEducationSample.java`
- `bat/src/test/java/cpf/bat/edu/centercut/BatCenterCutRetryEducationSampleTest.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.012. `PFW-DEADLINE` — Request Deadline/Timeout Budget

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.013. `PFW-HEADER` — 표준/확장 헤더

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 22
- test: 8
- SQL: 1
- script: 1
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/header/CpfExtensionHeaderPolicy.java`
- `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderAuditLogger.java`
- `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderCategory.java`
- `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderExtractor.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.014. `PFW-CONTEXT` — TransactionContext/MDC/Thread Context

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 5
- test: 1
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/filter/TransactionContextFilter.java`
- `pfw/src/main/java/cpf/pfw/common/logging/CpfTransactionContextAnomalyMonitor.java`
- `pfw/src/main/java/cpf/pfw/common/logging/TransactionContext.java`
- `pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentContext.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.015. `PFW-TXID` — transactionGlobalId/segment/timeline

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 18
- test: 2
- SQL: 3
- script: 2
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionSegmentFallbackStore.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.016. `PFW-ROLE` — transactionRole/direction/source-target

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 17
- test: 6
- SQL: 0
- script: 2
- resource: 71

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutTargetProvider.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutTarget.java`
- `pfw/src/main/java/cpf/pfw/common/logging/policy/LogPolicyTargetType.java`
- `pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentDirection.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.017. `PFW-OPSDB` — PFW 운영 DB 공유/장애모드

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 26
- test: 9
- SQL: 4
- script: 2
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/reliability/CpfReliabilityOperationsPort.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationRepository.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationType.java`
- `pfw/src/main/java/cpf/pfw/common/reliability/CpfReliabilityOperationsFacade.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.018. `PFW-LOGDB` — DB 로그/Segment 로그

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 51
- test: 14
- SQL: 0
- script: 0
- resource: 2

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLoggingEventPublisher.java`
- `pfw/src/main/java/cpf/pfw/common/logging/CpfLogLevel.java`

**필수 보완**

- 정상·오류·timeout·retry·target-down·local/remote·async·batch·worker 시나리오의 실제 file/DB log를 생성한다.
- transactionGlobalId/segment hierarchy, 누락·중복 0, masking, rotation·retention·spool/fallback을 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.019. `PFW-FILELOG` — cpf-{moduleCode}-{logType}.log 파일 로그

**현재 판정:** `미검증`

**최신 inventory 구현 신호**

- source: 52
- test: 15
- SQL: 0
- script: 0
- resource: 3

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchFileLogWriter.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLoggingEventPublisher.java`

**필수 보완**

- 정상·오류·timeout·retry·target-down·local/remote·async·batch·worker 시나리오의 실제 file/DB log를 생성한다.
- transactionGlobalId/segment hierarchy, 누락·중복 0, masking, rotation·retention·spool/fallback을 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.020. `PFW-LOGFAIL` — 로그 실패/fail-open/local spool

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 52
- test: 14
- SQL: 2
- script: 0
- resource: 2

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLoggingEventPublisher.java`
- `pfw/src/main/java/cpf/pfw/common/logging/CpfLogLevel.java`

**필수 보완**

- 정상·오류·timeout·retry·target-down·local/remote·async·batch·worker 시나리오의 실제 file/DB log를 생성한다.
- transactionGlobalId/segment hierarchy, 누락·중복 0, masking, rotation·retention·spool/fallback을 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.021. `PFW-TRACE` — Trace Boost/동적 로그 레벨

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 13
- test: 1
- SQL: 2
- script: 2
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/common/logging/CpfLogLevel.java`
- `pfw/src/main/java/cpf/pfw/common/logging/DynamicLogLevelRequest.java`
- `pfw/src/main/java/cpf/pfw/common/logging/DynamicLogLevelRule.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.022. `PFW-MASK` — 마스킹/민감정보 보호

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 5
- test: 2
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderMasker.java`
- `pfw/src/main/java/cpf/pfw/common/logging/SensitiveDataMasker.java`
- `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthMaskingRule.java`
- `cmn/src/main/java/cpf/cmn/utils/MaskingUtils.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.023. `PFW-ERROR` — 오류/예외/응답 표준

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 49
- test: 1
- SQL: 0
- script: 0
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/base/CpfResponse.java`
- `pfw/src/main/java/cpf/pfw/common/exception/CpfBusinessException.java`
- `pfw/src/main/java/cpf/pfw/common/exception/CpfDynamicErrorCode.java`
- `pfw/src/main/java/cpf/pfw/common/exception/CpfErrorCode.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.024. `PFW-VALID` — Validation Framework

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 11
- test: 5
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/exception/CpfValidationException.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileChecksumValidationResult.java`
- `pfw/src/main/java/cpf/pfw/common/header/CpfInboundHeaderValidator.java`
- `pfw/src/main/java/cpf/pfw/common/security/CpfCredentialValidationResult.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.025. `PFW-IDEMP` — Idempotency 표준

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 14
- test: 5
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerIdempotencyPort.java`
- `pfw/src/main/java/cpf/pfw/common/idempotency/CpfIdempotencyCommand.java`
- `pfw/src/main/java/cpf/pfw/common/idempotency/CpfIdempotencyEngine.java`
- `pfw/src/main/java/cpf/pfw/common/idempotency/CpfIdempotencyException.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.026. `PFW-STATE` — 상태 전이 State Machine

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 8
- test: 1
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/workflow/CpfWorkflow.java`
- `pfw/src/main/java/cpf/pfw/common/workflow/CpfWorkflowContext.java`
- `pfw/src/main/java/cpf/pfw/common/workflow/CpfWorkflowFailurePolicy.java`
- `pfw/src/main/java/cpf/pfw/common/workflow/CpfWorkflowHeaders.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.027. `PFW-LOCK` — Optimistic/Distributed Lock

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 6
- test: 1
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLockManager.java`
- `pfw/src/main/java/cpf/pfw/common/runtime/CpfDistributedLockPort.java`
- `pfw/src/main/java/cpf/pfw/common/runtime/CpfLockAcquireRequest.java`
- `pfw/src/main/java/cpf/pfw/common/runtime/CpfLockAcquireResult.java`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.028. `PFW-SCHED` — Scheduler 표준

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 3
- test: 0
- SQL: 2
- script: 0
- resource: 0

확인 시작 파일:
- `adm/src/main/java/cpf/adm/opr/dto/PfwBatchScheduleCandidate.java`
- `adm/src/main/java/cpf/adm/opr/service/PfwBatchScheduler.java`
- `adm/src/main/java/cpf/adm/opr/service/PfwBatchScheduleService.java`
- `specs/sql/migration/flyway/V4__batch_schedule_repository_notification.sql`

**필수 보완**

- PFW public contract, 기본 구현, type-safe property, typed option·strategy/SPI, auto-configuration을 한 묶음으로 완성한다.
- 정상·오류·timeout·부분 실패·재시도·다중 인스턴스와 보안 불변조건을 test/runtime evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.029. `CMN-CODE` — 공통코드/참조데이터

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 26
- test: 3
- SQL: 1
- script: 0
- resource: 3

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/exception/CpfDynamicErrorCode.java`
- `pfw/src/main/java/cpf/pfw/common/exception/CpfErrorCode.java`
- `pfw/src/main/java/cpf/pfw/common/exception/CpfFrameworkErrorCode.java`
- `pfw/src/main/java/cpf/pfw/common/exception/CpfResponseCodeResolver.java`

**필수 보완**

- CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
- API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.030. `CMN-MSG` — 공통 메시지/다국어/오류 메시지

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 34
- test: 1
- SQL: 0
- script: 0
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeMessage.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerMessage.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerMessageHandler.java`
- `pfw/src/main/java/cpf/pfw/common/exception/CpfMessageFormatter.java`

**필수 보완**

- CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
- API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.031. `CMN-ID` — 채번/분산 ID

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 4
- test: 3
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java`
- `cmn/src/main/java/cpf/cmn/biz/sequence/CmnSequenceIssueRequest.java`
- `cmn/src/main/java/cpf/cmn/biz/sequence/CmnSequenceIssueResult.java`
- `cmn/src/main/java/cpf/cmn/biz/sequence/CmnSequenceService.java`

**필수 보완**

- CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
- API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.032. `CMN-FILE` — 파일/다운로드/업로드/Object Storage

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 54
- test: 14
- SQL: 1
- script: 3
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/admin/CpfFileTransferStatusQuery.java`
- `pfw/src/main/java/cpf/pfw/common/attachment/CpfAttachmentContent.java`
- `pfw/src/main/java/cpf/pfw/common/attachment/CpfAttachmentStoragePort.java`
- `pfw/src/main/java/cpf/pfw/common/attachment/CpfStoredAttachment.java`

**필수 보완**

- CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
- API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.033. `CMN-FIXED` — 고정길이 전문 parser/formatter/layout

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 24
- test: 2
- SQL: 1
- script: 1
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/exception/CpfMessageFormatter.java`
- `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthAlignment.java`
- `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthFieldSpec.java`
- `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthFieldType.java`

**필수 보완**

- CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
- API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.034. `CMN-CALENDAR` — 영업일/휴일/기관 캘린더

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 1
- test: 0
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `adm/src/main/java/cpf/adm/opr/dto/AdmBusinessDayRequest.java`

**필수 보완**

- CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
- API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.035. `CMN-TEMPLATE` — 템플릿/알림

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 12
- test: 0
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `cmn/src/main/java/cpf/cmn/biz/notification/CmnNotificationLogRequest.java`
- `cmn/src/main/java/cpf/cmn/biz/notification/CmnNotificationLogResult.java`
- `cmn/src/main/java/cpf/cmn/biz/notification/CmnNotificationLogService.java`
- `adm/src/main/java/cpf/adm/opr/controller/AdmNotificationController.java`

**필수 보완**

- CMN이 실제 다수 업무가 공유하는 업무 공통만 소유하도록 하고 PFW 기술 공통·개별 업무 소유와 경계를 정리한다.
- API·저장소·SQL·권한·오류·EDU를 실제 consumer와 함께 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.036. `ADM-AUTH` — ADM 인증/세션/계정 생명주기

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 23
- test: 6
- SQL: 2
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayAuthorizationPort.java`
- `cmn/src/main/java/cpf/cmn/sec/token/CmnOAuthBearerTokenService.java`
- `cmn/src/main/java/cpf/cmn/sec/token/CmnOAuthTokenIntrospectionResult.java`
- `adm/src/main/java/cpf/adm/opr/controller/AdmAuthController.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.037. `ADM-RBAC` — RBAC/ABAC/권한 메타

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 17
- test: 3
- SQL: 2
- script: 3
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentRole.java`
- `adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java`
- `adm/src/main/java/cpf/adm/opr/dto/AdmApiPermission.java`
- `adm/src/main/java/cpf/adm/opr/dto/AdmApiPermissionRoleUpdateRequest.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.038. `ADM-AUDIT` — 감사/보안 이벤트/부인방지

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 7
- test: 1
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderAuditLogger.java`
- `adm/src/main/java/cpf/adm/opr/controller/AdmAuditLogController.java`
- `adm/src/main/java/cpf/adm/opr/controller/AdmLogPolicyAuditController.java`
- `adm/src/main/java/cpf/adm/opr/dto/DownloadAuditLog.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.039. `ADM-TX` — 거래 그룹 목록/상세

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 129
- test: 34
- SQL: 10
- script: 7
- resource: 3

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchFileLogWriter.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchJobLogPath.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.040. `ADM-TIMELINE` — 통합 Timeline

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 14
- test: 1
- SQL: 3
- script: 0
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTransactionTimelineQueryPort.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionSegmentFallbackStore.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionSegmentRecoveryEnvelope.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionSegmentRecoveryWorker.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.041. `ADM-SERVICE` — Service Instance/Routing/Health 관제

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 136
- test: 42
- SQL: 5
- script: 10
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`
- `pfw/src/main/java/cpf/pfw/channel/application/CpfChannelPolicyService.java`
- `pfw/src/main/java/cpf/pfw/common/admin/CpfRuntimeHealthStatusQuery.java`
- `pfw/src/main/java/cpf/pfw/common/aop/ServiceAccessAspect.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.042. `ADM-LOG` — 로그 정책/Trace Boost 화면

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 43
- test: 6
- SQL: 2
- script: 2
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/common/logging/CpfLogLevel.java`
- `pfw/src/main/java/cpf/pfw/common/logging/DynamicLogLevelRequest.java`
- `pfw/src/main/java/cpf/pfw/common/logging/DynamicLogLevelRule.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.043. `ADM-BATCH` — Batch/Worker/Heartbeat/Ghost 관제

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 57
- test: 10
- SQL: 8
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEvent.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventPublisher.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventType.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchExecutionRequest.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.044. `ADM-CENTER` — Center-Cut 관제

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 25
- test: 11
- SQL: 3
- script: 2
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutHandler.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutTargetProvider.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutResult.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutService.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.045. `ADM-EXS` — 대외연계 관제

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 10
- test: 3
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/exception/CpfExternalServiceException.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferEndpoint.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfServiceEndpointProperties.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfServiceEndpointRegistry.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.046. `ADM-COMP` — Compensation/Manual Recovery 관제

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 6
- test: 0
- SQL: 2
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionLogRecoveryWorker.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionSegmentRecoveryEnvelope.java`

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.047. `ADM-INCIDENT` — Incident/Alert/Runbook

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- ADM 화면→API client→Controller→Application Service→Port/Repository→SQL→권한·감사·마스킹을 기능별로 추적한다.
- 목록·검색·상세·조치·통계·다운로드·오류·빈 결과·대량 결과를 실제 browser/runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.048. `ADM-UX` — ADM UX/검색/다운로드/대시보드

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 7
- test: 1
- SQL: 1
- script: 0
- resource: 5

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogDownloadGrant.java`
- `adm/src/main/java/cpf/adm/opr/controller/AdmDownloadController.java`
- `adm/src/main/java/cpf/adm/opr/dto/DownloadAuditLog.java`
- `adm/src/main/java/cpf/adm/opr/dto/DownloadPolicy.java`

**필수 보완**

- ADM/BZA의 Vue Global Build·대형 단일 JS를 Vue 3 SFC·TypeScript·feature architecture로 전환한다.
- lint·typecheck·unit/component/API mock·production build·JAR/WAR packaging·실제 browser E2E를 완료한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.049. `BAT-CORE` — BAT standalone worker/application

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 14
- test: 1
- SQL: 2
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchHeartbeatService.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerConsumerWorker.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerPublisherWorker.java`
- `pfw/src/main/java/cpf/pfw/common/exception/CpfFrameworkErrorCode.java`

**필수 보완**

- 독립 `BatWorkerApplication` 또는 동등 entry point, registration, heartbeat, capability, claim/lease, drain, crash recovery를 구현한다.
- 서로 다른 process/instanceId의 2개 worker로 중복 claim 방지·lease timeout·takeover·restart recovery를 실제 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.050. `BAT-JOB` — Job/Step/Parameter/Dependency

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 25
- test: 8
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/base/CpfJobSupport.java`
- `pfw/src/main/java/cpf/pfw/common/base/CpfStepSupport.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchJobLogPath.java`
- `pfw/src/main/java/cpf/pfw/common/execution/CpfBatchJob.java`

**필수 보완**

- BAT control plane과 독립 worker runtime의 ownership을 분리하고 Job/Step/Item 상태·재시작·재처리를 영속화한다.
- 다중 worker, claim/lease, crash, drain, takeover, 중복 실행 방지와 ADM 관제를 runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.051. `BAT-ITEM` — Batch item claim/retry/skip/rerun

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 6
- test: 4
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferRetryPolicy.java`
- `bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutItemProcessingEducationSample.java`
- `bat/src/main/java/cpf/bat/edu/centercut/BatCenterCutRetryEducationSample.java`
- `bat/src/main/java/cpf/bat/edu/retry/BatRetryEducationSample.java`

**필수 보완**

- BAT control plane과 독립 worker runtime의 ownership을 분리하고 Job/Step/Item 상태·재시작·재처리를 영속화한다.
- 다중 worker, claim/lease, crash, drain, takeover, 중복 실행 방지와 ADM 관제를 runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.052. `BAT-CALL-SYNC` — BAT → 주제영역 동기 호출

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 43
- test: 9
- SQL: 0
- script: 2
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/servicecall/CpfServiceRegistryQueryPort.java`
- `pfw/src/main/java/cpf/pfw/common/base/CpfApplicationFacade.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`
- `pfw/src/main/java/cpf/pfw/common/logging/segment/CpfTransactionTimelineQueryFacade.java`

**필수 보완**

- BAT control plane과 독립 worker runtime의 ownership을 분리하고 Job/Step/Item 상태·재시작·재처리를 영속화한다.
- 다중 worker, claim/lease, crash, drain, takeover, 중복 실행 방지와 ADM 관제를 runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.053. `BAT-CALL-ASYNC` — BAT → Event/Outbox 비동기 호출

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 11
- test: 1
- SQL: 0
- script: 1
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEvent.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventPublisher.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventType.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLoggingEventPublisher.java`

**필수 보완**

- BAT control plane과 독립 worker runtime의 ownership을 분리하고 Job/Step/Item 상태·재시작·재처리를 영속화한다.
- 다중 worker, claim/lease, crash, drain, takeover, 중복 실행 방지와 ADM 관제를 runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.054. `BAT-SHARED` — BAT → SHARED/온라인 Facade 재사용

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 13
- test: 4
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/base/CpfApplicationFacade.java`
- `pfw/src/main/java/cpf/pfw/common/execution/CpfSharedApi.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`
- `pfw/src/main/java/cpf/pfw/common/logging/segment/CpfTransactionTimelineQueryFacade.java`

**필수 보완**

- BAT control plane과 독립 worker runtime의 ownership을 분리하고 Job/Step/Item 상태·재시작·재처리를 영속화한다.
- 다중 worker, claim/lease, crash, drain, takeover, 중복 실행 방지와 ADM 관제를 runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.055. `CENTER-CORE` — Center-Cut 기본 구현

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 25
- test: 11
- SQL: 3
- script: 2
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutHandler.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutTargetProvider.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutResult.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutService.java`

**필수 보완**

- PFW contract, BAT 기본 target/item/result 구현, 업무 domain adapter를 분리한다.
- chunk·동시성·부분 실패·재시작·재처리·보상·진행률·ADM 제어를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.056. `CENTER-ADV` — Center-Cut 고급 패턴

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 26
- test: 11
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutHandler.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CenterCutTargetProvider.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutResult.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutService.java`

**필수 보완**

- PFW contract, BAT 기본 target/item/result 구현, 업무 domain adapter를 분리한다.
- chunk·동시성·부분 실패·재시작·재처리·보상·진행률·ADM 제어를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.057. `EXS-INST` — 기관/Endpoint/Profile

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 5
- test: 0
- SQL: 0
- script: 1
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileTransferEndpoint.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfServiceEndpointProperties.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfServiceEndpointRegistry.java`
- `pfw/src/main/java/cpf/pfw/common/servicecall/CpfEndpointRegistry.java`

**필수 보완**

- EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
- timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.058. `EXS-REST` — EXS REST 송수신

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 12
- test: 5
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/exception/CpfExternalServiceException.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfHttpClientProperties.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfLocalServiceIdentity.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfRestClientInterceptor.java`

**필수 보완**

- EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
- timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.059. `EXS-FIXED` — EXS 고정길이 전문 송수신

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 23
- test: 2
- SQL: 1
- script: 1
- resource: 0

확인 시작 파일:
- `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthAlignment.java`
- `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthFieldSpec.java`
- `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthFieldType.java`
- `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthFormatResult.java`

**필수 보완**

- EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
- timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.060. `EXS-SEC` — EXS OAuth/JWT/mTLS/인증서

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 12
- test: 0
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/admin/CpfCredentialStatusQuery.java`
- `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogServiceCredentialPort.java`
- `pfw/src/main/java/cpf/pfw/common/security/CpfCertificateProviderPort.java`
- `pfw/src/main/java/cpf/pfw/common/security/CpfCredentialProviderPort.java`

**필수 보완**

- EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
- timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.061. `EXS-UNKNOWN` — Unknown Result 처리

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 35
- test: 2
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveResult.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchExecutionResult.java`
- `pfw/src/main/java/cpf/pfw/common/batch/centercut/CpfCenterCutResult.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeResult.java`

**필수 보완**

- EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
- timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.062. `EXS-RECON` — 대외 Reconciliation

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 5
- test: 3
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/reconciliation/CpfReconciliationPort.java`
- `pfw/src/main/java/cpf/pfw/common/reconciliation/CpfUnknownResultRecord.java`
- `pfw/src/main/java/cpf/pfw/common/reconciliation/JdbcCpfReconciliationRepository.java`
- `pfw/src/test/java/cpf/pfw/common/reconciliation/JdbcCpfReconciliationRepositoryTest.java`

**필수 보완**

- EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
- timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.063. `EXS-FILE` — SFTP/File Transfer 연계 후보

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 30
- test: 6
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/admin/CpfFileTransferStatusQuery.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfDuplicatePreventionPort.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileChecksumPolicy.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileChecksumValidationResult.java`

**필수 보완**

- EXS module을 상시 복원하지 않고 PFW 공통 연계 기능과 XYZ EDU, 생성 업무 domain의 기관별 adapter로 분리한다.
- timeout·target-down·인증·전문·결과 불명·대사·재처리를 실제 mock/harness와 evidence로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.064. `EVENT-CORE` — Event/Message Envelope

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 46
- test: 1
- SQL: 0
- script: 1
- resource: 2

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEvent.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventPublisher.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchEventType.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchLoggingEventPublisher.java`

**필수 보완**

- 표준 envelope, schema/version, outbox/inbox, broker adapter, DLQ/replay의 데이터 계약과 트랜잭션 경계를 완성한다.
- 중복·순서·poison message·backpressure·broker 장애·재기동을 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.065. `EVENT-OUTBOX` — Outbox/Inbox

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 3
- test: 1
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerInboxPort.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerOutboxPort.java`
- `xyz/src/main/java/cpf/xyz/messaging/XyzOutboxInboxEducationSample.java`
- `xyz/src/test/java/cpf/xyz/messaging/XyzOutboxInboxEducationSampleTest.java`

**필수 보완**

- 표준 envelope, schema/version, outbox/inbox, broker adapter, DLQ/replay의 데이터 계약과 트랜잭션 경계를 완성한다.
- 중복·순서·poison message·backpressure·broker 장애·재기동을 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.066. `EVENT-BROKER` — Kafka/MQ/Redis real broker

**현재 판정:** `미검증`

**최신 inventory 구현 신호**

- source: 30
- test: 4
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/admin/CpfBrokerStatusQuery.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeHandler.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeMessage.java`

**필수 보완**

- Kafka·RabbitMQ 등 지원 broker의 실제 adapter와 local container/harness를 제공한다.
- broker down, redelivery, ordering, duplicate, poison, DLQ, restart와 multi-instance를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.067. `EVENT-DLQ` — DLQ/Replay/Poison Message

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 4
- test: 0
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerDlqPort.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerDlqReplayRequest.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerDlqReplayResult.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerReplayPort.java`

**필수 보완**

- 표준 envelope, schema/version, outbox/inbox, broker adapter, DLQ/replay의 데이터 계약과 트랜잭션 경계를 완성한다.
- 중복·순서·poison message·backpressure·broker 장애·재기동을 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.068. `SAGA-CORE` — Saga/분산 거래 표준

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- orchestration/choreography 선택 기준, 상태 저장, compensation idempotency, manual recovery와 audit를 구현한다.
- 부분 성공·중복 보상·보상 실패·재기동·operator 조치를 runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.069. `SAGA-COMP` — Compensation

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- orchestration/choreography 선택 기준, 상태 저장, compensation idempotency, manual recovery와 audit를 구현한다.
- 부분 성공·중복 보상·보상 실패·재기동·operator 조치를 runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.070. `SAGA-MANUAL` — Manual Recovery/Adjustment

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 6
- test: 0
- SQL: 2
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionLogRecoveryWorker.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionSegmentRecoveryEnvelope.java`

**필수 보완**

- orchestration/choreography 선택 기준, 상태 저장, compensation idempotency, manual recovery와 audit를 구현한다.
- 부분 성공·중복 보상·보상 실패·재기동·operator 조치를 runtime으로 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.071. `SEC-AUTHN` — 인증/AuthN

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 18
- test: 5
- SQL: 2
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayAuthorizationPort.java`
- `cmn/src/main/java/cpf/cmn/sec/token/CmnJwtCreateRequest.java`
- `cmn/src/main/java/cpf/cmn/sec/token/CmnJwtService.java`
- `cmn/src/main/java/cpf/cmn/sec/token/CmnJwtValidationResult.java`

**필수 보완**

- 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
- 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.072. `SEC-AUTHZ` — 권한/AuthZ/RBAC/ABAC

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 18
- test: 3
- SQL: 2
- script: 3
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayAuthorizationPort.java`
- `pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentRole.java`
- `adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java`
- `adm/src/main/java/cpf/adm/opr/dto/AdmApiPermission.java`

**필수 보완**

- 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
- 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.073. `SEC-SECRET` — Secret/Vault/Key Rotation

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 1
- test: 0
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/security/CpfSecretProviderPort.java`

**필수 보완**

- 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
- 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.074. `SEC-CERT` — Certificate/mTLS 관리

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 1
- test: 0
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/security/CpfCertificateProviderPort.java`

**필수 보완**

- 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
- 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.075. `SEC-PRIVACY` — 개인정보/Privacy/Data Classification

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 5
- test: 2
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderMasker.java`
- `pfw/src/main/java/cpf/pfw/common/logging/SensitiveDataMasker.java`
- `cmn/src/main/java/cpf/cmn/message/fixedlength/FixedLengthMaskingRule.java`
- `cmn/src/main/java/cpf/cmn/utils/MaskingUtils.java`

**필수 보완**

- 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
- 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.076. `SEC-DOWNLOAD` — Download Governance

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 7
- test: 1
- SQL: 1
- script: 5
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogDownloadGrant.java`
- `adm/src/main/java/cpf/adm/opr/controller/AdmDownloadController.java`
- `adm/src/main/java/cpf/adm/opr/dto/DownloadAuditLog.java`
- `adm/src/main/java/cpf/adm/opr/dto/DownloadPolicy.java`

**필수 보완**

- 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
- 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.077. `SEC-APP` — Application Security 기본 통제

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 23
- test: 3
- SQL: 1
- script: 1
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/security/CpfCertificateProviderPort.java`
- `pfw/src/main/java/cpf/pfw/common/security/CpfCredentialProviderPort.java`
- `pfw/src/main/java/cpf/pfw/common/security/CpfCredentialRef.java`
- `pfw/src/main/java/cpf/pfw/common/security/CpfCredentialStatus.java`

**필수 보완**

- 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
- 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.078. `SEC-APPROVAL` — Approval/Dual Control/Break-glass

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- 보안 기본값, 정책, 권한, 감사, masking, secret/certificate lifecycle을 코드와 운영 화면에 연결한다.
- 정상뿐 아니라 401·403·권한 상승·secret 노출·다운로드 남용·break-glass를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.079. `OPS-METRIC` — Metrics/Observability

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 3
- test: 2
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `adm/src/main/java/cpf/adm/opr/controller/AdmObservabilityController.java`
- `adm/src/main/java/cpf/adm/opr/service/AdmObservabilityService.java`
- `adm/src/test/java/cpf/adm/opr/service/AdmObservabilityServiceTest.java`
- `bat/src/main/java/cpf/bat/edu/tasklet/basic/BatJobParameterValidationEducationSample.java`

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.080. `OPS-SLO` — SLI/SLO/Error Budget

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 4
- test: 2
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/archive/CpfZipSlipGuard.java`
- `pfw/src/test/java/cpf/pfw/common/archive/CpfZipSlipGuardTest.java`
- `cmn/src/main/java/cpf/cmn/biz/log/CmnBusinessLogRequest.java`
- `cmn/src/main/java/cpf/cmn/biz/log/CmnBusinessLogResult.java`

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.081. `OPS-ALERT` — Alert Rule Engine

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.082. `OPS-INCIDENT` — Incident Management

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.083. `OPS-RUNBOOK` — Runbook/자동 진단

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 1
- resource: 0

확인 시작 파일:
- `scripts/runtime-diagnostics.ps1`

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.084. `OPS-SELF` — Self-healing

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 6
- test: 0
- SQL: 2
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/logging/CpfTraceRecoveryPort.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/CpfTraceRecoveryFacade.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionLogRecoveryWorker.java`
- `pfw/src/main/java/cpf/pfw/common/logging/fallback/TransactionSegmentRecoveryEnvelope.java`

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.085. `OPS-TOPOLOGY` — Topology/Dependency Map/Service Catalog

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.086. `OPS-MAINT` — Maintenance Mode/Drain

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.087. `OPS-CONFIG` — Config/Policy/Runtime Override

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 82
- test: 14
- SQL: 4
- script: 3
- resource: 4

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/channel/application/CpfChannelPolicyService.java`
- `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelExecutionPolicy.java`
- `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelPolicyDecision.java`
- `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelPolicyPackage.java`

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.088. `OPS-DRIFT` — Config Drift/Policy Versioning

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 1
- test: 0
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/version/CpfPlatformVersion.java`

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.089. `OPS-CAPACITY` — Performance/Capacity/Resource Governance

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 0
- test: 2
- SQL: 0
- script: 1
- resource: 71

확인 시작 파일:
- `pfw/src/main/resources/application-pfw-dev.yml`
- `pfw/src/main/resources/application-pfw-local.yml`
- `pfw/src/main/resources/application-pfw-prod.yml`
- `pfw/src/main/resources/application-pfw-stg.yml`

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.090. `OPS-DR` — DR/Backup/Restore/Archive

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 11
- test: 5
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveChecksum.java`
- `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveEntry.java`
- `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveFormat.java`
- `pfw/src/main/java/cpf/pfw/common/archive/CpfArchivePolicy.java`

**필수 보완**

- metric·trace·health·alert·incident·runbook·운영 제어를 동일 service/instance/transaction 식별자로 연결한다.
- 다중 인스턴스·장애·drain·복구·정책 변경·감사를 실제 runtime 또는 검증 harness로 증명한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.091. `REL-BUILD` — Build/Artifact/Provenance

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 9
- test: 4
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/build.gradle`
- `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveChecksum.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileChecksumPolicy.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileChecksumValidationResult.java`

**필수 보완**

- clean build부터 artifact·checksum·SBOM·provenance·deploy·rollback·migration·compatibility까지 재현 가능한 release chain을 완성한다.
- clean checkout과 package inspection, upgrade/rollback smoke를 evidence로 남긴다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.092. `REL-DEPLOY` — Deployment/Rollback/Feature Flag

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 1
- test: 1
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `bat/src/main/java/cpf/bat/edu/transaction/BatPartialRollbackEducationSample.java`
- `bat/src/test/java/cpf/bat/edu/transaction/BatPartialRollbackEducationSampleTest.java`
- `deploy/inventory/dev-services.json`
- `deploy/inventory/local-services.json`

**필수 보완**

- clean build부터 artifact·checksum·SBOM·provenance·deploy·rollback·migration·compatibility까지 재현 가능한 release chain을 완성한다.
- clean checkout과 package inspection, upgrade/rollback smoke를 evidence로 남긴다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.093. `REL-MIG` — DB Migration/Flyway/Expand-Contract

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 1
- test: 3
- SQL: 35
- script: 1
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/base/CpfMapperContract.java`
- `pfw/src/test/java/cpf/pfw/common/base/CpfBaseContractTest.java`
- `pfw/src/test/java/cpf/pfw/common/capability/CpfCapabilityContractTest.java`
- `xyz/src/test/java/cpf/xyz/catalog/XyzReferenceIdentityContractTest.java`

**필수 보완**

- clean build부터 artifact·checksum·SBOM·provenance·deploy·rollback·migration·compatibility까지 재현 가능한 release chain을 완성한다.
- clean checkout과 package inspection, upgrade/rollback smoke를 evidence로 남긴다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.094. `REL-COMPAT` — API/Event/전문 호환성

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 1
- test: 0
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/version/CpfPlatformVersion.java`

**필수 보완**

- clean build부터 artifact·checksum·SBOM·provenance·deploy·rollback·migration·compatibility까지 재현 가능한 release chain을 완성한다.
- clean checkout과 package inspection, upgrade/rollback smoke를 evidence로 남긴다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.095. `DB-SQL` — SQL 표준/MyBatis/Index

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 15
- test: 4
- SQL: 51
- script: 3
- resource: 18

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/base/CpfMapperContract.java`
- `pfw/src/main/java/cpf/pfw/config/PfwMyBatisConfig.java`
- `pfw/src/main/java/cpf/pfw/mapper/common/logging/TransactionLogMapper.java`
- `pfw/src/main/java/cpf/pfw/mapper/common/logging/TransactionSegmentMapper.java`

**필수 보완**

- canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
- 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.096. `DB-INSTALL` — MariaDB 신규 빈 DB Full Install

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 7
- test: 2
- SQL: 4
- script: 34
- resource: 0

확인 시작 파일:
- `acc/smoke/smoke-acc.ps1`
- `bat/src/main/java/cpf/bat/job/centercut/BatCenterCutSmokeTasklet.java`
- `bat/src/main/java/cpf/bat/job/heartbeat/BatHeartbeatSmokeTasklet.java`
- `bat/src/main/java/cpf/bat/job/smoke/BatSmokeJobConfig.java`

**필수 보완**

- canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
- 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.097. `DB-PERF` — DB 성능/Partition/Retention

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 2

확인 시작 파일:
- `adm/src/main/resources/static/adm/index.html`
- `bza/src/main/resources/static/bza/index.html`

**필수 보완**

- canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
- 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.098. `DB-MULTI` — Multi Datasource/Read Replica

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 7
- test: 2
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/config/PfwDataSourceConfig.java`
- `pfw/src/test/java/cpf/pfw/config/PfwDataSourceConfigTest.java`
- `cmn/src/main/java/cpf/cmn/config/CmnBusinessDataSourceConfig.java`
- `cmn/src/main/java/cpf/cmn/config/CmnDataSourceConfig.java`

**필수 보완**

- canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
- 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.099. `DATA-LINEAGE` — Data Lineage/Quality/Reconciliation

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 5
- test: 3
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/reconciliation/CpfReconciliationPort.java`
- `pfw/src/main/java/cpf/pfw/common/reconciliation/CpfUnknownResultRecord.java`
- `pfw/src/main/java/cpf/pfw/common/reconciliation/JdbcCpfReconciliationRepository.java`
- `pfw/src/test/java/cpf/pfw/common/reconciliation/JdbcCpfReconciliationRepositoryTest.java`

**필수 보완**

- canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
- 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.100. `DATA-RETENTION` — Retention/Purge/Legal Hold/Archive

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 11
- test: 5
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveChecksum.java`
- `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveEntry.java`
- `pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveFormat.java`
- `pfw/src/main/java/cpf/pfw/common/archive/CpfArchivePolicy.java`

**필수 보완**

- canonical SQL·migration·index·retention·lineage·reconciliation의 단일 정본과 owner를 확정한다.
- 신규 설치·재실행·upgrade·rollback·대량 데이터·동시성·다중 datasource를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.101. `API-CONTRACT` — API Contract/OpenAPI/Developer Portal

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 147
- test: 11
- SQL: 0
- script: 3
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/base/BaseController.java`
- `pfw/src/main/java/cpf/pfw/common/base/CpfBaseController.java`
- `pfw/src/main/java/cpf/pfw/common/base/CpfMapperContract.java`
- `pfw/src/main/java/cpf/pfw/config/PfwOpenApiAutoConfiguration.java`

**필수 보완**

- URI·executionId·request/response/error·header·permission·paging contract를 OpenAPI와 runtime에 일치시킨다.
- 하위 호환, rate/size limit, abuse, 대량 조회·다운로드 guard를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.102. `API-GATEWAY` — Gateway/L4/WAF/Service Mesh 연계

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 9
- test: 4
- SQL: 0
- script: 1
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/filetransfer/CpfFileExchangeGateway.java`
- `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayAuthorizationPort.java`
- `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayRoute.java`
- `pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayRouteCatalog.java`

**필수 보완**

- URI·executionId·request/response/error·header·permission·paging contract를 OpenAPI와 runtime에 일치시킨다.
- 하위 호환, rate/size limit, abuse, 대량 조회·다운로드 guard를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.103. `API-LIMIT` — Rate Limit/Quota/Abuse Detection

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- URI·executionId·request/response/error·header·permission·paging contract를 OpenAPI와 runtime에 일치시킨다.
- 하위 호환, rate/size limit, abuse, 대량 조회·다운로드 guard를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.104. `API-PAGING` — Pagination/Search/Sort/Download Guard

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 12
- test: 2
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogArtifactSearch.java`
- `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogDownloadGrant.java`
- `adm/src/main/java/cpf/adm/opr/controller/AdmDownloadController.java`
- `adm/src/main/java/cpf/adm/opr/dto/DownloadAuditLog.java`

**필수 보완**

- URI·executionId·request/response/error·header·permission·paging contract를 OpenAPI와 runtime에 일치시킨다.
- 하위 호환, rate/size limit, abuse, 대량 조회·다운로드 guard를 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.105. `SAMPLE-ACC` — ACC 실전형 샘플

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 26
- test: 6
- SQL: 0
- script: 0
- resource: 7

확인 시작 파일:
- `acc/build.gradle`
- `acc/README.md`
- `acc/manifest/domain-manifest.json`
- `acc/manifest/ownership.json`

**필수 보완**

- 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
- 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.106. `SAMPLE-MBR` — MBR 실전형 샘플

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 20
- test: 9
- SQL: 0
- script: 0
- resource: 8

확인 시작 파일:
- `mbr/build.gradle`
- `mbr/src/main/java/cpf/mbr/MbrApplication.java`
- `mbr/src/main/java/cpf/mbr/bse/controller/MbrAuthController.java`
- `mbr/src/main/java/cpf/mbr/bse/controller/MbrController.java`

**필수 보완**

- 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
- 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.107. `SAMPLE-BIZADM` — BIZADM 업무관리 샘플

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 19
- test: 5
- SQL: 0
- script: 0
- resource: 9

확인 시작 파일:
- `bza/build.gradle`
- `bza/src/main/java/cpf/bza/BzaApplication.java`
- `bza/src/main/java/cpf/bza/auth/controller/BzaAuthController.java`
- `bza/src/main/java/cpf/bza/auth/filter/BzaApiAuthFilter.java`

**필수 보완**

- 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
- 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.108. `SAMPLE-EDU` — EDU 교육 샘플

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 101
- test: 61
- SQL: 4
- script: 4
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/edu/PfwEducationCoverageCatalog.java`
- `pfw/src/test/java/cpf/pfw/common/edu/PfwEducationCoverageCatalogTest.java`
- `adm/src/main/java/cpf/adm/opr/dto/PfwBatchScheduleCandidate.java`
- `adm/src/main/java/cpf/adm/opr/service/PfwBatchScheduler.java`

**필수 보완**

- 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
- 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.109. `SAMPLE-XYZ` — XYZ 신규 주제영역 샘플

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 77
- test: 36
- SQL: 0
- script: 0
- resource: 7

확인 시작 파일:
- `xyz/build.gradle`
- `xyz/src/main/java/cpf/xyz/XyzApplication.java`
- `xyz/src/main/java/cpf/xyz/ai/XyzAiEducationSample.java`
- `xyz/src/main/java/cpf/xyz/ai/controller/XyzAiEducationController.java`

**필수 보완**

- 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
- 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.110. `ONBOARD-DOMAIN` — 신규 주제영역 온보딩

**현재 판정:** `실패`

**최신 inventory 구현 신호**

- source: 1
- test: 1
- SQL: 0
- script: 5
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java`
- `mbr/src/test/java/cpf/mbr/common/logging/TransactionIdGeneratorTest.java`
- `acc/manifest/domain-manifest.json`
- `acc/manifest/ownership.json`

**필수 보완**

- ACC를 disposable worktree에서 snapshot→삭제→잔존 0→순수 생성→compile/test/DB/runtime→기존 ACC diff→재생성 parity로 검증한다.
- 미선택 capability 파일 0건, ownership manifest, 사용자 수정 보호, DB vendor-neutral generator를 완료한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.111. `DEVEX-QUICK` — Developer Quickstart/Local Dev

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 9
- test: 4
- SQL: 0
- script: 0
- resource: 8

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/archive/LocalCpfArchiveService.java`
- `pfw/src/main/java/cpf/pfw/common/attachment/LocalCpfAttachmentStorageAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/LocalCpfFileTransferAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/http/CpfLocalServiceIdentity.java`

**필수 보완**

- 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
- 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.112. `DEVEX-CODEGEN` — Scaffold/Code Generation Governance

**현재 판정:** `실패`

**최신 inventory 구현 신호**

- source: 1
- test: 1
- SQL: 0
- script: 4
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java`
- `mbr/src/test/java/cpf/mbr/common/logging/TransactionIdGeneratorTest.java`
- `scripts/create-domain.ps1`
- `scripts/remove-domain.ps1`

**필수 보완**

- ACC를 disposable worktree에서 snapshot→삭제→잔존 0→순수 생성→compile/test/DB/runtime→기존 ACC diff→재생성 parity로 검증한다.
- 미선택 capability 파일 0건, ownership manifest, 사용자 수정 보호, DB vendor-neutral generator를 완료한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.113. `DEVEX-COMMENT` — 한글 주석/설정 주석 표준

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- 개발자가 public API와 문서만으로 실제 기능을 구현할 수 있도록 generator·quickstart·Basic/Advanced EDU를 완성한다.
- 정상·오류·경계·복구·local/remote·DB·OpenAPI·JavaDoc·runtime evidence를 연결한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.114. `RULE-ARCH` — Architecture Rule Check

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 3
- resource: 0

확인 시작 파일:
- `scripts/check-architecture-ownership.ps1`
- `scripts/check-base-hierarchy.ps1`
- `scripts/export-architecture-inventory.ps1`

**필수 보완**

- 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
- 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.115. `RULE-SEC` — Security/Secret/URL Scan

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 26
- test: 3
- SQL: 1
- script: 1
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/execution/CpfExecutionCatalogScanner.java`
- `pfw/src/main/java/cpf/pfw/common/security/CpfCertificateProviderPort.java`
- `pfw/src/main/java/cpf/pfw/common/security/CpfCredentialProviderPort.java`
- `pfw/src/main/java/cpf/pfw/common/security/CpfCredentialRef.java`

**필수 보완**

- 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
- 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.116. `RULE-QUALITY` — Static Analysis/Dependency/License

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 1
- script: 2
- resource: 7

확인 시작 파일:
- `adm/src/main/resources/static/adm/adm.css`
- `adm/src/main/resources/static/adm/adm.js`
- `adm/src/main/resources/static/adm/index.html`
- `adm/src/main/resources/static/adm/vendor/vue.global.prod.js`

**필수 보완**

- 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
- 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.117. `TEST-UNIT` — Unit/Slice/Integration Test

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 1
- test: 158
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/test/java/cpf/pfw/channel/application/CpfChannelPolicyServiceTest.java`
- `pfw/src/test/java/cpf/pfw/common/archive/CpfArchiveChecksumTest.java`
- `pfw/src/test/java/cpf/pfw/common/archive/CpfArchiveServiceTest.java`
- `pfw/src/test/java/cpf/pfw/common/archive/CpfZipSlipGuardTest.java`

**필수 보완**

- 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
- 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.118. `TEST-CONTRACT` — Contract/Compatibility Test

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 2
- test: 158
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/base/CpfMapperContract.java`
- `pfw/src/test/java/cpf/pfw/channel/application/CpfChannelPolicyServiceTest.java`
- `pfw/src/test/java/cpf/pfw/common/archive/CpfArchiveChecksumTest.java`
- `pfw/src/test/java/cpf/pfw/common/archive/CpfArchiveServiceTest.java`

**필수 보완**

- 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
- 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.119. `TEST-RUNTIME` — Runtime Smoke

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 30
- test: 6
- SQL: 4
- script: 41
- resource: 1

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/admin/CpfRuntimeHealthStatusQuery.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchRuntimeListener.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchRuntimeProgress.java`
- `pfw/src/main/java/cpf/pfw/common/runtime/CpfDistributedLockPort.java`

**필수 보완**

- 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
- 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.120. `TEST-BROWSER` — Browser Click E2E

**현재 판정:** `미검증`

**최신 inventory 구현 신호**

- source: 2
- test: 0
- SQL: 0
- script: 8
- resource: 0

확인 시작 파일:
- `pfw/build.gradle`
- `pfw/src/main/java/cpf/pfw/common/runtime/CpfLockAcquireRequest.java`
- `pfw/src/main/java/cpf/pfw/common/runtime/CpfLockAcquireResult.java`
- `cmn/build.gradle`

**필수 보완**

- ADM과 BZA의 인증·권한·검색·상세·조치·오류·session expiry를 실제 browser로 클릭한다.
- screenshot, network metadata, console error와 basis SHA를 evidence에 남긴다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.121. `TEST-BROKER` — Real Broker/Multi-instance Test

**현재 판정:** `미검증`

**최신 inventory 구현 신호**

- source: 30
- test: 4
- SQL: 1
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/admin/CpfBrokerStatusQuery.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeHandler.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeMessage.java`

**필수 보완**

- 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
- 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.122. `TEST-FAULT` — Fault Injection/Chaos Smoke

**현재 판정:** `미검증`

**최신 inventory 구현 신호**

- source: 7
- test: 1
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/exception/DefaultCpfMessageResolver.java`
- `pfw/src/main/java/cpf/pfw/common/exception/DefaultCpfResponseCodeResolver.java`
- `pfw/src/main/java/cpf/pfw/common/workflow/CpfWorkflowFailurePolicy.java`
- `bat/src/main/java/cpf/bat/job/failure/BatFailTasklet.java`

**필수 보완**

- 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
- 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.123. `TEST-EVIDENCE` — Evidence Index/Consistency Gate

**현재 판정:** `재확인 필요`

**최신 inventory 구현 신호**

- source: 0
- test: 1
- SQL: 0
- script: 5
- resource: 0

확인 시작 파일:
- `pfw/src/test/java/cpf/pfw/common/logging/file/CpfFileLogRuntimeEvidenceTest.java`
- `scripts/check-evidence-path-existence.ps1`
- `scripts/check-feature-evidence.ps1`
- `scripts/check-report-matrix-evidence-consistency.ps1`

**필수 보완**

- 검사 후보 전체 수와 classified/passed/failed/skipped/unclassified/stale를 산출하는 자동 gate를 구현한다.
- 정적 검사와 runtime/browser/external environment 검증을 분리하고 stale evidence를 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.124. `DOC-GOV` — 문서 최소화/정본화 후순위

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 1
- test: 0
- SQL: 51
- script: 1
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderSpecs.java`
- `acc/README.md`
- `scripts/check-document-links.ps1`
- `specs/sql/00_all_install.sql`

**필수 보완**

- 기존 정본 역할을 유지하고 중복·stale 문서는 내용을 통합한 뒤 삭제한다.
- Markdown 렌더링, link, heading, table, code fence, UTF-8과 source 정합성을 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.125. `DOC-PRODUCT` — 제품화 문서/설치/운영/개발자 문서

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 19
- test: 7
- SQL: 7
- script: 4
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/api/reliability/CpfReliabilityOperationsPort.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationRepository.java`
- `pfw/src/main/java/cpf/pfw/common/batch/CpfBatchOperationType.java`
- `pfw/src/main/java/cpf/pfw/common/reliability/CpfReliabilityOperationsFacade.java`

**필수 보완**

- 기존 정본 역할을 유지하고 중복·stale 문서는 내용을 통합한 뒤 삭제한다.
- Markdown 렌더링, link, heading, table, code fence, UTF-8과 source 정합성을 검증한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.126. `PROD-EDITION` — 상용화/Edition/License 후보

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- 상용 제품 후보 기능을 단순 문구가 아니라 extension contract, tenancy/license/package 경계와 운영 정책으로 설계·구현한다.
- 미구현이면 명확히 미구현으로 기록하고 placeholder·과장 문서를 제거한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.127. `PROD-MULTITENANT` — Multi-tenant/Multi-customer 후보

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- 정본이 최종 제품 필수로 유지하는지 우선 확정하고, 유지 시 source contract·보안·운영·테스트까지 구현한다.
- 후보 문구만 존재하는 상태를 완료로 기록하지 않고 미구현 또는 명시적 제외 근거를 남긴다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.128. `PROD-PLUGIN` — Plugin/Adapter/Marketplace 후보

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 13
- test: 10
- SQL: 0
- script: 1
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/channel/adapter/JdbcCpfChannelRegistryAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/attachment/LocalCpfAttachmentStorageAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/broker/CpfBrokerBridgeAdapter.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/DeterministicCpfRemoteFileTransferAdapter.java`

**필수 보완**

- 정본이 최종 제품 필수로 유지하는지 우선 확정하고, 유지 시 source contract·보안·운영·테스트까지 구현한다.
- 후보 문구만 존재하는 상태를 완료로 기록하지 않고 미구현 또는 명시적 제외 근거를 남긴다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.129. `PROD-PACKAGE` — 산업군별 패키지 후보

**현재 판정:** `미구현`

**최신 inventory 구현 신호**

- source: 2
- test: 0
- SQL: 0
- script: 1
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/channel/model/CpfChannelPolicyPackage.java`
- `adm/src/main/java/cpf/adm/opr/dto/AdmChannelPackageImportRequest.java`
- `scripts/check-packaged-runtime-resources.ps1`

**필수 보완**

- 정본이 최종 제품 필수로 유지하는지 우선 확정하고, 유지 시 source contract·보안·운영·테스트까지 구현한다.
- 후보 문구만 존재하는 상태를 완료로 기록하지 않고 미구현 또는 명시적 제외 근거를 남긴다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.130. `REQ-GOV` — 요건 ID/우선순위/추적성

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 2
- resource: 0

확인 시작 파일:
- `scripts/build-sample-coverage-matrix.ps1`
- `scripts/check-report-matrix-evidence-consistency.ps1`

**필수 보완**

- 정본 requirement ID와 implementation/evidence의 양방향 traceability를 자동 생성한다.
- 중복 check ID, 과대 완료, stale evidence와 미소유 구현을 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.131. `REQ-REVIEW` — ChatGPT 검수/완료 불인정 기준

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 1
- test: 0
- SQL: 0
- script: 0
- resource: 0

확인 시작 파일:
- `pfw/src/main/java/cpf/pfw/common/remotelog/CpfRemoteLogPreview.java`

**필수 보완**

- 정본 requirement ID와 implementation/evidence의 양방향 traceability를 자동 생성한다.
- 중복 check ID, 과대 완료, stale evidence와 미소유 구현을 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.132. `REQ-CODEX` — Codex 요청서 작성 기준

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 0
- resource: 0

- 관련 경로 신호가 없다. 이름이 다른 구현을 역추적하고 없으면 미구현으로 처리한다.

**필수 보완**

- 정본 requirement ID와 implementation/evidence의 양방향 traceability를 자동 생성한다.
- 중복 check ID, 과대 완료, stale evidence와 미소유 구현을 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

## 8.133. `REQ-GAP` — Future Requirement Gap Intake

**현재 판정:** `부분 구현`

**최신 inventory 구현 신호**

- source: 0
- test: 0
- SQL: 0
- script: 2
- resource: 0

확인 시작 파일:
- `scripts/build-sample-coverage-matrix.ps1`
- `scripts/check-report-matrix-evidence-consistency.ps1`

**필수 보완**

- 정본 requirement ID와 implementation/evidence의 양방향 traceability를 자동 생성한다.
- 중복 check ID, 과대 완료, stale evidence와 미소유 구현을 차단한다.
- 정본 상세 카드의 module, ownership, yml/env, DB, API, ADM, log, security,
  test, EDU, OpenAPI, runtime과 evidence 필드를 모두 대조한다.
- 구현 신호가 있어도 실제 consumer·오류·복구·다중 인스턴스·운영 연결이 없으면
  `부분 구현`을 유지하고 누락을 구현한다.
- 외부 환경이 없으면 adapter, configuration, local harness, mock,
  failure test와 runbook을 구현하고 실제 외부 실행만 `미검증`으로 남긴다.
- evidence에 basis SHA, command, profile, process/instance, 시작·종료,
  result, requirement ID와 sanitized hash를 기록한다.

# 9. ACC 실제 삭제→생성 검증

1. ACC tracked file·hash·외부 참조 snapshot
2. generator-owned·business-owned·user-owned·framework-owned 분류
3. remover dry-run과 사용자 수정 보호
4. ACC 전체 삭제
5. settings/build/import/SQL/config/deploy/docs/matrix 잔존 0건
6. 최신 generator로 ACC 순수 생성
7. ownership manifest 생성
8. 생성 직후 tree·빈 package·placeholder·중복 feature 검사
9. compile/test/bootJar/bootWar/OpenAPI
10. MariaDB install과 대표 CRUD
11. Gateway→ACC
12. MBR→ACC local/remote
13. 순수 생성 ACC와 기존 ACC diff
14. 정당한 업무 확장만 재적용
15. remove→generate→remove→generate parity

검증 조합:

- DB=N, Batch=N, External=N, UI=N
- DB=Y, Batch=N, External=N, UI=N
- DB=Y, Batch=Y, External=N, UI=N
- DB=Y, Batch=N, External=Y, UI=N
- DB=Y, Batch=N, External=N, UI=Y

선택하지 않은 capability의 file·dependency·SQL·property·test는 0건이어야 한다.

# 10. 전체 계층·Package·Ownership Gate

Controller·Service뿐 아니라 DTO, DAO, Repository, Mapper, Client,
Adapter, Batch, Worker, Configuration, Properties와 SPI를 전수 분류한다.

각 대상은 `extends`, `implements`, `composition`, `not-applicable` 중 하나다.
`unclassified > 0`이면 실패다.

업무 domain과 ADM/BZA backend는 feature 중심 vertical slice를 기본으로 한다.
framework 공통을 업무 module에 복제하지 않는다.

# 11. PFW 확장성

모든 주요 공통 기능은 다음 세 수준을 제공한다.

1. 안전한 framework default
2. type-safe property/environment/secret
3. 요청·feature 단위 typed option/policy/strategy/SPI

TLS, 필수 masking, 감사, endpoint allowlist, secret 보호,
최대 timeout/retry, 개인정보 wire log 금지는 override할 수 없다.

# 12. BAT Agent·Worker

- 독립 worker entry point
- workerId·instanceId·host·process·version
- registration·heartbeat·health·capability
- polling/subscription
- claim/lease
- concurrency·queue capacity
- duplicate execution 방지
- drain·forced stop
- crash·lease timeout·takeover
- restart recovery
- unknown result·reconciliation
- file/DB execution log
- metric·alert
- ADM 목록·상세·timeline·제어·감사

서로 다른 process와 instanceId의 worker 2개로 정상 분산,
중복 claim, drain, crash, lease timeout, takeover, restart recovery,
version mismatch와 capability mismatch를 검증한다.

# 13. ADM/BZA Frontend·Backend

Frontend:

- Vue 3 SFC
- TypeScript
- package lock
- feature-based architecture
- shared auth/error/masking/table/paging/accessibility
- lint·typecheck·unit·component·API mock
- production build
- Gradle/JAR/WAR integration
- CSP/XSS/CSRF/source-map/cache 정책
- actual browser E2E

대형 단일 `adm.js`·`bza.js`와 Vue Global Build를 제거한다.

Backend 기능은 frontend→API→application→repository→SQL→permission→audit→test로 추적한다.

# 14. SQL·DB

- 모든 SQL을 canonical DDL, migration, seed, smoke, test fixture,
  upgrade, rollback, reset, uninstall, generated all-install,
  module-local duplicate, stale, vendor-specific으로 분류한다.
- 중앙 SQL과 module-local schema를 단일 정본으로 통합한다.
- MariaDB, Oracle, PostgreSQL, SQL Server별 lifecycle을 정의한다.
- 운영 install과 test reset을 분리한다.
- table metadata에서 정의서를 자동 생성할 수 있게 한다.

# 15. 실제 File·DB Log

정상, validation, 업무 오류, 기술 오류, timeout, retry, target-down,
local/remote, async, broker, Batch Job/Step, worker crash와 multi-instance에서
실제 file log와 DB log를 생성한다.

transactionGlobalId, traceId, segmentId, parentSegmentId, executionId,
source, target, instance, worker, start/end/status/duration, failure point,
error code와 retry attempt를 검증한다.

누락·중복·민감 원문 0건과 rotation·retention·compression,
backpressure·writer fallback을 검증한다.

# 16. 문서 통합

새 초안·보정본·날짜별 복사본을 repository에 추가하지 않는다.

유지 파일:

- `README.md`
- `CPF_FINAL_TARGET_REQUIREMENTS.md`
- `CPF_CURRENT_WORK_REQUEST.md`
- `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- `CPF_STABILIZATION_REPORT.md`
- `CPF_GAP_MATRIX.md`
- `CPF_EVIDENCE_INDEX.md`
- `specs/기능_구현_매트릭스.json`
- `specs/기능_구현_매트릭스.md`
- `specs/sample-coverage-matrix.md`

중복·stale DOCX/HTML은 내용을 정본에 통합한 뒤 삭제한다.
PDF와 최종 DOCX는 최종 정본화 단계에서만 생성한다.

# 17. 필수 실행 검증

- clean checkout
- full test
- qualityGate
- 133-domain traceability
- architecture/dependency gate
- full layer taxonomy
- ACC lifecycle와 capability matrix
- MariaDB install/rerun/upgrade
- SQL canonical duplicate scan
- Gateway→ACC
- MBR→ACC local/remote
- external integration failure/recovery
- actual file/DB log scenario
- BAT restart/rerun
- 2-worker crash/lease/takeover
- scheduler multi-instance
- center-cut restart/reprocess
- frontend lint/typecheck/unit/component/build
- packaged frontend resource
- ADM/BZA browser E2E
- OpenAPI
- JavaDoc/doclint
- EDU compile/runtime
- security 401/403
- secret scan
- semantic garbage
- JAR/WAR/SBOM/provenance
- final document consistency

# 18. 완료 금지

- 정본 133개 도메인 traceability 없음
- ACC 대신 임시 domain 검증
- 미선택 capability 파일 생성
- Controller·Service만 검사
- PFW 확장에 source 수정 필요
- typed option/SPI 없음
- 보안 불변조건 우회 가능
- 독립 Batch Worker 없음
- 2-worker runtime 없음
- ADM/BZA 단일 JS 유지
- browser E2E 없음
- module-local 중복 SQL
- DB vendor 하드코딩
- 실제 file log evidence 없음
- file/DB parity 없음
- EDU catalog 존재만으로 완료
- stale evidence 사용
- semantic garbage 미분류
- README와 source 불일치
- 미구현을 완료로 기록
- 기존 성공 기능 회귀
- secret/PII 원문
- build/tmp 산출물
- 실행하지 않은 검증 성공 기록

# 19. 상태 문서 갱신 순서

```text
source/test/SQL/config
→ runtime/browser
→ evidence
→ 기능 matrix
→ sample coverage
→ GAP/report/index
→ README
→ final consistency
```

# 20. 최종 제출

1. 시작 SHA와 working tree
2. 133-domain requirement inventory
3. repository implementation inventory
4. 양방향 traceability
5. 변경·삭제·이동·통합 파일
6. domain별 구현·검증 결과
7. 실행 명령과 결과
8. runtime/browser evidence
9. 상태 변경 전후
10. 실제 순감소 gap
11. 잔여 미검증 환경
12. semantic garbage/allowlist
13. 문서 정합성
14. commit·push·branch 미수행 확인

Codex 설명이 아니라 최신 source, SQL, runtime, browser와 evidence가 완료 근거다.
