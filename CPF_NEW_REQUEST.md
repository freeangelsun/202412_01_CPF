# CPF 다음 대형 작업 최종 요청서

## 0. 작업 기준과 금지사항

- repository: `https://github.com/freeangelsun/202412_01_CPF`
- branch: `master`
- 작업 시작 시 최신 master SHA를 기록한다.
- 최상위 정본은 `CPF_FINAL_TARGET_REQUIREMENTS.md`다.
- `CPF_FINAL_TARGET_REQUIREMENTS_01.md`~`05.md`는 검색 보조이며 정본을 대체하지 않는다.
- 운영 완료 기준은 `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`를 따른다.
- 이번 요청의 시작 기준은 최신 master와 이 요청서다.
- 이번 요청은 최신 master 검수에서 확인된 ACC 삭제, EXS 기능 대체 미입증, 생성기 자동 통합 부족, qualityGate 약화, runtime 미검증을 보완하고, 지금까지 협의한 Gateway·ADM·헬스·배포·패키지 표준을 함께 구현하는 대형 마일스톤이다.
- 이 요청을 모두 구현하더라도 실제 외부 broker/file server/object storage/secret manager, 물리 다중 서버, 장시간 부하·보안·DR 및 최종 문서 정본화가 남으면 CPF 최종 목표 100% 완료로 기록하지 않는다.
- 목표 파일 확인 없이 범위를 축소하거나 삭제를 완료로 바꾸지 않는다.
- Git commit, push, branch 생성은 하지 않는다.
- 민감정보 원문을 source, yml, env, SQL, evidence, report에 기록하지 않는다.
- 실행하지 않은 검증을 완료로 기록하지 않는다.
- 문서량을 채우기 위한 문서 작성은 금지한다.
- 신규 HTML 작성·대규모 문서 정본화는 지양한다.
- PDF는 최종 정본화 단계에서만 만든다.
- 이번 작업 중 DOCX 9종을 반복 재생성하지 않는다. source·test·SQL·runtime 구조가 안정된 뒤 최종 정본화 작업에서 재생성한다.
- 허용 상태값은 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`만 사용한다.
- `CoverageCatalog`, 파일 존재, compile 성공, Swagger UI 접근, 정적 marker만으로 완료 처리하지 않는다.
- 기존 evidence가 신규 SQL/source/profile/runtime 변경 이전 것이면 stale로 간주한다.
- 실패를 숨기거나 검증 gate를 줄여 통과시키지 않는다.
- 요청서에 열거된 파일만 기계적으로 수정하지 말고 최신 master의 source, test, SQL, Flyway, all_install, config, OpenAPI, UI, script, evidence를 함께 확인해 기능적으로 필요한 누락을 먼저 찾아 보완한다.
- 표준이 없는 부분은 PFW/CMN ownership과 정본 목표에 맞는 최소 공통 표준을 정의한 뒤 source, test, SQL, OpenAPI, ADM, evidence에 일관되게 적용한다.
- 기존에 더 나은 표준 구현이 있으면 새 체계를 중복 생성하지 말고 기존 구조를 확장한다.

---

# 1. 필수 완료 범위

## 1.1 ACC reference domain 복구 및 유지

ACC는 삭제된 상태로 두지 않는다. 반드시 `scripts/create-domain.ps1`의 실제 실행 결과로 다시 생성한다.

### 생성 원칙

- Codex가 ACC source를 직접 손으로 작성해 생성 결과를 흉내 내지 않는다.
- 먼저 현재 생성기를 사용해 순수 생성한다.
- 순수 생성 결과를 수정하지 않은 상태로 검증한다.
- 실패하면 generated ACC를 직접 patch하지 말고 생성기 또는 PFW/CMN 공통 기반을 수정한다.
- 수정 후 기존 generated ACC를 삭제하고 처음부터 다시 생성해 재검증한다.
- 순수 생성 검증이 끝난 뒤에만 대표 업무 기능과 regression test를 추가한다.
- 최종 ACC는 삭제하지 않고 repository/worktree에 유지한다.
- ACC는 default 필수 업무 모듈이 아니라 `generator verification/reference domain`으로 명시한다.
- ACC 삭제는 향후 별도 명시 요청에서만 수행한다.

### ACC 생성 결과 필수 구성

- Gradle module과 settings 연결
- Java 25 toolchain
- executable bootJar
- external Tomcat WAR/bootWar
- `SpringBootServletInitializer`
- local/dev/stg/prod profile
- embedded mode external-config DataSource
- external Tomcat JNDI DataSource
- application/service registry
- standard execution ID
- ownership manifest
- feature-first package
- BaseController/BaseService hierarchy
- Controller/Service/DTO/Repository/Mapper/Port/Local Adapter/Remote Proxy
- OpenAPI
- 표준 오류 response
- 필수 header
- permission/audit/masking 설명
- SQL split/Flyway/all_install/smoke
- ADM execution catalog
- ADM service/instance catalog
- BZA menu candidate
- Gateway route candidate
- build/deploy/start/stop/status/smoke integration
- unit/contract/controller/OpenAPI/DB/runtime test

### 순수 생성 검증

다음을 generated ACC 수정 전에 실행하고 evidence를 남긴다.

- generator dry-run
- generator apply
- settings 포함
- compile Java 25
- test
- bootJar
- bootWar/WAR
- embedded startup
- external Tomcat WAR deployment
- external-config DB mode
- JNDI DataSource lookup
- empty MariaDB schema install
- Flyway
- repeat install
- OpenAPI
- standard execution ID scan
- ownership
- registry
- ADM catalog
- Gateway route
- transactionGlobalId
- file/DB log
- health/readiness
- common build/deploy scripts
- multi-instance configuration
- qualityGate

### ACC 대표 업무 보강

순수 생성 성공 후 중립적인 계정 기본 기능을 추가한다.

- 등록
- 단건 조회
- 목록
- 조건 검색
- 정렬 whitelist
- offset/page와 keyset 예제
- 수정
- optimistic lock
- logical delete
- validation
- permission
- masking
- audit
- 표준 오류
- standard execution ID
- OpenAPI
- MariaDB runtime smoke

---

## 1.2 ACC·EXS 삭제 기능 inventory와 EDU 복원

삭제된 파일을 단순히 제거 완료로 기록하지 않는다.

### inventory 작성

직전 commit의 ACC/EXS/MBR composite/reference source, test, SQL, smoke를 기능 단위로 목록화한다.

각 항목에 대해 다음 중 하나를 기록한다.

- PFW capability로 이동
- CMN helper로 이동
- XYZ EDU로 이동
- BAT EDU로 이동
- ACC reference domain으로 복원
- 업무 특화로 폐기
- 미이동
- 재확인 필요

### 반드시 복원 또는 대체할 예제

- local facade와 remote facade proxy
- MBR → ACC 서비스 호출
- 복합 거래 segment/timeline
- 하위 서비스 실패
- timeout
- retry 허용/금지
- failover
- circuit open
- unknown result
- reconciliation
- header propagation
- selectedInstanceId
- transactionGlobalId parent/child
- 외부연계 neutral fake server
- 실패 지점 ADM 조회

EXS를 다시 실행 모듈로 만들 필요는 없다. 기술 engine은 PFW, 전문/helper는 CMN, 외부연계 예제는 XYZ EDU가 소유한다. 단, 삭제 전 기능이 사라진 채로 `cleanup 완료` 처리하지 않는다.

---

## 1.3 BAT 온디맨드 배치 실행 EDU

`cpf.bat.edu.ondemand` feature slice를 추가한다.

### 호출 흐름

- 온라인 client 또는 운영 API가 표준 batch execution ID로 실행 요청
- `POST` 요청은 동기 완료를 기다리지 않고 `202 Accepted`
- executionRequestId, standardBatchId, transactionGlobalId, status URL 반환
- BAT worker가 실제 Spring Batch Job을 실행
- 상태 조회, step 조회, 결과 조회, 실패 상세 제공

### 필수 정책

- raw Spring bean name 직접 입력 금지
- standardBatchId allowlist
- permission
- 실행 사유
- idempotency key
- duplicate execution policy
- business date
- job parameter validation
- 이미 실행 중/완료/실패/재시작 구분
- restart와 rerun 구분
- cancel/stop 가능 여부
- audit
- JobInstance/JobExecution/StepExecution 연결
- Gateway 경유
- ADM 조회 연결
- OpenAPI
- 정상·실패·권한·중복·재시작 test
- MariaDB JobRepository runtime evidence

---

## 1.4 PFW Gateway Core와 선택 실행 Runtime

Gateway는 별도 업무 도메인이 아니라 PFW가 소유하는 선택 실행 runtime capability로 구현한다.

### 제품 구조

- `pfw-gateway-core`
- `pfw-gateway-runtime` 또는 동등한 PFW 소유 실행 모듈
- 일반 업무 개발자가 Gateway 내부 route 코드를 작성하지 않아야 한다.
- Gateway가 필요하지 않은 환경에서는 runtime을 기동하지 않는다.
- Gateway가 필요한 환경에서는 PFW 배포물만으로 기동할 수 있어야 한다.

### PFW Gateway mode의 외부 호출 계약

PFW Gateway를 선택한 설치 topology에서는 다음 계약을 지원한다.

- 단일 host
- 단일 port
- 단일 URI
- 기존 CPF 표준 실행 ID header

예시 개념:

```http
POST /cpf/execute
X-Cpf-Standard-Execution-Id: OMBRMB0001
```

이 mode에서 Client는 MBR/ACC/BZA/ADM/BAT의 개별 IP, port, instance를 알지 않는다. `direct` 또는 `external-gateway` topology까지 이 계약을 강제하지 않는다.

Gateway 처리:

1. 표준 거래 ID 형식과 상태 검증
2. source channel과 permission 검증
3. transactionGlobalId 생성 또는 허용 규칙 검증
4. route snapshot에서 target service와 endpoint 결정
5. registry health에서 대상 instance 선택
6. CPF 내부 표준 header 재생성
7. service 호출
8. 응답 전달
9. route/instance/log/metric 기록

### CPF 표준 실행 ID 중심 호출·라우팅 계약

CPF의 모든 거래 기반 기능은 URL, 클래스명, 메서드명보다 **표준 실행 ID를 우선 기준키**로 사용한다. 표준 실행 ID는 Gateway routing만을 위한 값이 아니라 거래 catalog, 권한, 감사, 로그, OpenAPI, ADM 조회, Service Call Engine, batch, scheduler, worker, broker, DLQ/replay, file transfer, unknown/reconciliation을 연결하는 불변 식별자다.

#### 신규 표준 실행 ID 형식

기존 `OXYZ-EDU-12-0001`과 같은 가변 구분자 형식은 신규 표준으로 사용하지 않는다. 신규 표준은 구분자 없는 영문 대문자·숫자 조합의 **10자리 고정 길이**로 한다.

```text
[실행유형 1자리][실행 주제영역 3자리][업무·기능 코드 2자리][순번 4자리]
```

예:

```text
OXYZAA0001
OMBRMB0001
OBZAUS0001
BBATOD0001
```

자리 의미:

- 1자리 `O`, `S`, `B`: 온라인 거래, CPF 주제영역 간 공유 API, 배치·비동기 실행 유형
- 3자리: 실제 실행 주제영역 코드
- 2자리: 해당 실행 주제영역 내부의 업무·기능 분류 코드
- 4자리: `유형 + 주제영역 + 기능 코드` 범위에서 유일한 순번

검증 정규식:

```regex
^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$
```

다음 원칙을 지킨다.

- `XYZ`, `MBR`, `BZA`, `ACC`, `BAT`, `ADM` 등 실제 실행 주체는 3자리 실행 주제영역에만 기록한다.
- `PFW`, `CMN`, `EDU`처럼 capability 소유 모듈 또는 샘플 성격을 실행 주제영역 뒤에 중복 표기하지 않는다.
- 기능 코드 `AA`, `MB`, `US`, `OD` 등은 2자리 코드표로 관리하고 ADM과 문서에서 이름·설명을 함께 제공한다.
- ID는 URL, host, port, instance, package, class, method가 변경되어도 가능한 한 유지되는 안정적 계약이어야 한다.
- 동일 ID의 중복 등록, 다른 handler 중복 매핑, retired ID 재사용을 금지한다.
- ID를 단순 문자열로만 저장하지 말고 유형, 실행 주제영역, 기능 코드, 순번을 파싱·검증하는 PFW 표준 value object와 validator를 제공한다.

#### 실행 유형별 호출 정책

- `O`: 온라인 거래다. 자연어 URI와 표준 실행 ID 호출을 모두 지원한다. 외부·채널 노출 여부는 catalog의 공개 정책, permission, source channel 정책으로 통제한다.
- `S`: CPF 주제영역 간 공유 API다. 자연어 내부 URI와 표준 실행 ID 호출을 모두 지원하되 CPF 내부 호출만 허용한다.
- `B`: 배치·비동기 실행이다. 표준 실행 ID를 기준으로 job, scheduler, worker, center-cut, broker 처리와 운영 접수 API를 연결한다.

`S` 공유 API는 다음을 강제한다.

- 동일 JVM에서는 Facade Contract와 Local Facade Adapter를 통해 호출한다.
- MSA에서는 Remote Facade Proxy와 PFW Service Call Engine을 통해 호출한다.
- 호출 주체의 CPF service identity, serviceId, instanceId, 권한, transactionGlobalId와 segment를 검증한다.
- 내부 자연어 URI와 내부 ID 호출을 모두 허용하지만 외부 채널, 웹·모바일 client, 공개 Gateway route, 외부 API Gateway, 일반 `/cpf/execute` 공개 endpoint에서는 실행할 수 없다.
- 외부에서 `S` ID header 또는 `S` URI를 직접 보내도 Gateway와 대상 서비스 ingress filter 양쪽에서 거부한다.
- URL을 알고 직접 호출하는 우회를 막기 위해 Gateway 숨김만으로 처리하지 말고 대상 서비스 자체에서 internal ingress, service identity, 신뢰 header 재생성 여부를 검증한다.
- public route catalog와 internal route catalog를 분리하고 `S` route는 public snapshot에 포함하지 않는다.
- `S` API는 다른 주제영역의 Controller, Service, Repository, Mapper를 직접 참조하는 수단이 아니며 반드시 Facade Contract 경계를 유지한다.
- `S` API의 성공·실패·권한 거부·외부 우회 차단은 감사 로그와 ADM timeline에서 확인할 수 있어야 한다.

#### 지원해야 하는 HTTP 진입 방식

`O` 온라인 거래와 허용된 내부 `S` 공유 API는 다음 호출 표현을 지원한다. 단, `S`는 CPF 내부 신뢰 경로에서만 동작하고 공개 Gateway endpoint에는 노출하지 않는다.

1. 표준 실행 ID header 방식

```http
POST /cpf/execute
X-Cpf-Standard-Execution-Id: OXYZAA0001
```

2. URI 표준 실행 ID 방식

```http
POST /cpf/execute/OXYZAA0001
```

3. 기존 업무 URL 방식

```http
POST /xyz/sequence/issue
```

세 방식은 별도 기능을 실행하면 안 된다. 모두 동일한 route catalog, 동일한 권한·감사·마스킹·표준 오류·timeout·circuit·로그 정책을 적용하고 **동일한 최종 Spring handler method**를 실행해야 한다.

Swagger/OpenAPI에는 URI 호출과 ID 호출을 모두 안내한다.

- `O` 거래는 public API group에 자연어 URI, ID header 방식, ID URI 방식, 실행 ID, 권한, 필수 header, 요청·응답 schema, 표준 오류 response 예시를 제공한다.
- `S` 거래는 internal CPF API group에만 노출하고 자연어 내부 URI와 내부 ID 호출 예시, CPF service identity 요구사항, 외부 호출 금지 정책을 명시한다.
- OpenAPI extension 또는 동등한 metadata로 standardExecutionId, executionType, serviceId, featureCode, visibility, direct/Gateway 허용 여부, internal-only 여부를 제공한다.
- URI 호출과 ID 호출을 서로 다른 업무 API처럼 중복 정의하지 말고 동일 operation/handler의 대체 호출 방식임을 명확히 안내한다.
- Swagger UI가 열린다는 사실만으로 완료 처리하지 않고 실제 generated OpenAPI JSON과 호출 smoke를 검증한다.

- URI 방식으로 받은 ID는 Gateway 내부에서 검증한 뒤 `X-Cpf-Standard-Execution-Id`와 CPF 거래 context로 정규화한다.
- 기존 업무 URL로 호출된 경우에도 annotation과 route catalog를 통해 연결된 표준 실행 ID를 자동 식별하고 context에 설정한다.
- URL 직접 호출에 표준 실행 ID header가 함께 들어오면 URL에 매핑된 ID와 동일한지 검증한다.
- URI ID, header ID, URL 매핑 ID가 둘 이상 존재하면서 서로 다르면 요청을 거부하고 표준 오류·감사 로그를 남긴다.
- 외부 client가 내부 전용 route·service·instance 정보를 위조해 전달하지 못하도록 Gateway가 신뢰 경계에서 제거·재생성한다.
- `direct` topology에서도 URL 호출은 계속 가능해야 하지만, 거래 처리 이후에는 표준 실행 ID가 없는 무식별 거래가 남지 않도록 한다.

#### annotation과 실제 endpoint의 자동 catalog 생성

`@CpfOnlineTransaction`, `@CpfBatchTransaction` 및 동등한 CPF 거래 annotation과 실제 실행 지점을 스캔해 catalog를 자동 생성한다. HTTP 거래는 Spring의 class-level·method-level mapping을 결합해 전체 경로를 계산해야 한다.

예:

```java
@RequestMapping("/xyz")
@PostMapping("/sequence/issue")
@CpfOnlineTransaction(id = "OXYZAA0001", name = "XYZ 공통 채번 발급")
```

자동 catalog 결과에는 최소 다음을 포함한다.

- standardExecutionId
- executionType
- serviceId 또는 moduleCode
- featureCode
- sequence
- transaction name과 description
- HTTP method
- class-level + method-level 전체 path
- OpenAPI operationId
- controller class와 handler method
- request/response schema
- permission·role·masking·audit 정책
- timeout·retry·circuit·idempotency 정책
- route status와 route version
- direct/Gateway 허용 여부
- 등록 source와 build version

수동 route 등록만으로 완료 처리하지 않는다. annotation과 실제 mapping이 불일치하거나, ID는 등록됐지만 실제 endpoint로 전달되지 않거나, endpoint는 존재하지만 ID catalog에 없으면 qualityGate가 실패해야 한다.

#### HTTP 외 거래 기반 기능의 ID 적용

표준 실행 ID 적용 범위는 REST Controller에 한정하지 않는다. 최소 다음 실행 유형 모두에 적용한다.

- 외부 REST 거래
- 동일 JVM Local Facade 호출
- Remote Facade Proxy 호출
- PFW Service Call Engine 호출
- 온디맨드 배치
- 정기 scheduler 실행
- worker 실행
- center-cut 실행
- broker publish·consume
- outbox·inbox
- DLQ·replay
- file transfer
- attachment 처리
- unknown result·reconciliation
- 운영자 수동 재처리·보상 처리

HTTP가 아닌 실행은 URL이 없으므로 표준 실행 ID를 transaction context, Facade contract metadata, job parameter, scheduler metadata, message header, outbox/inbox row, worker execution record에 전달한다. parent·child segment 간에는 각 실행의 ID와 `transactionGlobalId` 관계를 보존한다.

#### Gateway의 책임과 비책임

Gateway는 요청 방식에 관계없이 다음을 수행한다.

- ID·URL·HTTP method 해석 및 일치 검증
- 인증·권한·source channel 검증
- 표준 실행 ID와 transactionGlobalId context 생성·전파
- route snapshot에서 target service와 endpoint 결정
- registry health 기반 정상 instance 선택
- timeout·circuit·deadline·header 정책 적용
- ingress, Gateway instance, target service·instance 기록
- 응답과 표준 오류 전달
- 로그·감사·metric·trace 기록

Gateway는 업무 로직, 업무 DB 처리, saga 상태, broker workflow, reconciliation 업무 판단을 소유하지 않는다. 어떤 방식으로 호출이 들어와도 정책을 적용한 뒤 대상 실행 주체로 전달하는 data plane 역할을 유지한다.

#### 기존 ID 마이그레이션과 호환성

기존 `OXYZ-EDU-12-0001` 형식의 ID는 inventory 후 신규 10자리 ID로 전환한다.

- annotation
- 표준 실행 catalog와 seed/Flyway
- Gateway route snapshot
- ADM 화면·검색·권한·감사
- OpenAPI extension과 operation binding
- Service Call Engine contract
- batch·scheduler·worker·broker metadata
- 로그·metric·trace
- 테스트·EDU·sample coverage
- evidence와 report/matrix/index

외부 계약이나 기존 로그 조회 호환이 필요한 ID는 alias/deprecation 정보를 catalog에 두고 `구 ID → 신규 ID` 조회를 지원할 수 있다. 신규 저장·신규 로그·신규 header 전파는 신규 10자리 ID로 단일화한다. alias를 영구적인 이중 표준으로 방치하지 않는다.

#### 필수 테스트와 완료 기준

최소 다음을 실제 검증한다.

- header ID 방식 호출 성공
- URI ID 방식 호출 성공
- 기존 URL 직접 호출 성공
- 세 방식이 동일 handler를 실행하는지 검증
- URL·header·URI ID 불일치 차단
- 존재하지 않는 ID, retired ID, 중복 ID 차단
- class-level + method-level path 결합
- 동일 ID 중복 annotation 탐지
- direct와 Gateway 호출의 동일 권한·감사·마스킹·오류 계약
- Local Facade·Remote Proxy·Service Call Engine ID 전파
- batch·scheduler·worker·broker ID 전파
- transactionGlobalId와 segment timeline에서 실행 ID 조회
- ADM에서 ID, 기능 코드, 서비스, URL, operationId, instance를 함께 조회
- 신규 ID 기반 로그·감사·metric·trace 검색
- 기존 ID alias 조회와 신규 ID 단일 기록
- OpenAPI, catalog, source annotation, route snapshot 간 정합성
- qualityGate에서 구 형식 신규 사용, 누락 ID, 중복 ID, orphan route, stale evidence 탐지

위 검증 evidence가 없으면 표준 실행 ID 기반 호출 또는 Gateway route 완료로 기록하지 않는다.

### 표준 Gateway·Ingress context/header와 ADM 거래 추적

호출 방식이 `direct`, `pfw-gateway`, `external-gateway` 중 무엇이든 동일한 거래 로그 모델과 ADM 조회 화면을 사용한다.

기존 표준과 충돌 없이 최소 다음 context를 추가한다.

- ingressType: `DIRECT`, `PFW_GATEWAY`, `EXTERNAL_GATEWAY`
- ingressServiceId
- ingressInstanceId
- gatewayInstanceId
- externalGatewayId 또는 externalGatewayInstanceId
- gatewayRouteId
- gatewayRouteVersion
- gatewayReceivedAt
- currentServiceId
- currentInstanceId
- selectedInstanceId
- sourceChannel
- targetServiceId
- serviceCallMode: `LOCAL`, `REMOTE_DIRECT`, `REMOTE_GATEWAY`
- parentSegmentId
- segmentId
- hopSequence 또는 hopCount

의미를 혼용하지 않는다.

- `ingressInstanceId`: 외부 요청을 최초로 받은 CPF 실행 인스턴스
- `gatewayInstanceId`: PFW Gateway를 경유한 경우 실제 요청을 처리한 Gateway 인스턴스
- `currentInstanceId`: 현재 segment를 실행 중인 서비스 인스턴스
- `selectedInstanceId`: Gateway 또는 Service Call Engine이 다음 호출 대상으로 선택한 인스턴스

예시:

```text
Client
→ GW-02
→ MBR-01
→ ACC-02
```

ADM에서는 동일 transactionGlobalId로 다음을 보여야 한다.

- ingressType = PFW_GATEWAY
- gatewayInstanceId = GW-02
- ingressServiceId = MBR
- ingressInstanceId = MBR-01
- MBR segment currentInstanceId = MBR-01
- MBR→ACC segment selectedInstanceId = ACC-02
- ACC segment currentInstanceId = ACC-02

직접 호출 예시:

```text
Client
→ MBR-02
→ ACC-01
```

ADM에서는:

- ingressType = DIRECT
- gatewayInstanceId = 없음
- ingressInstanceId = MBR-02
- MBR→ACC selectedInstanceId = ACC-01

외부 Gateway 예시:

```text
Client
→ EXT-GW-03
→ MBR-01
```

외부 Gateway 인스턴스 식별은 trusted header, mTLS identity, service token 또는 adapter가 제공하는 검증 가능한 값만 기록한다. 검증 가능한 식별값이 없으면 임의 추정하지 않고 `EXTERNAL_GATEWAY` 경유 사실과 확인 가능한 peer/VIP 정보만 남긴다.

외부 client가 내부 전용 header를 보내면 신뢰하지 않는다. 제거·거부 후 trusted Gateway 또는 CPF ingress가 재생성한다.

Gateway/Ingress 정보는 단순 로그 필드로만 두지 말고 **기존 PFW 표준 헤더 체계를 보존한 상태에서 필요한 항목만 확장**한다.

작업 시작 전에 최신 master의 다음 구현을 전수 inventory 한다.

- `CpfHeaderNames`
- `CpfHeaderSpecs`
- `CpfHeaderPropagator`
- `TransactionHeader`
- `TransactionContext`
- `TransactionSegmentContext`
- 수신 filter/interceptor
- `CpfRestClient`/`CpfWebClient`
- Service Call Engine
- DB·파일 거래 로그
- OpenAPI 공통 header
- XYZ 표준 헤더 EDU와 runtime smoke

현재 존재하는 `X-Transaction-Id`, `X-Cpf-Standard-Execution-Id`, `X-Root-Transaction-Id`, `X-Transaction-Segment-Id`, `X-Parent-Transaction-Segment-Id`, `X-Transaction-Call-Depth`, `X-Caller-Service`, `X-Caller-Instance-Id`, `X-Cpf-Ext-*` 등 기존 표준 헤더의 이름·의미·필수 여부·전파 규칙을 임의로 변경하거나 삭제하지 않는다.

Gateway 정보 추가 원칙:

- 기존 필드로 표현 가능한 값은 신규 헤더를 만들지 않고 기존 헤더를 사용한다.
- `callerService`, `callerInstanceId`, root/segment/call-depth와 의미가 겹치는 신규 헤더를 중복 생성하지 않는다.
- `selectedInstanceId`처럼 이미 로그/context에 존재하는 값은 wire header로 실제 전파가 필요한지 ownership과 보안 경계를 먼저 검토한다.
- Gateway에서만 생성되는 정보 중 거래 추적과 운영에 필수이며 기존 표준으로 표현할 수 없는 최소 항목만 신규 표준 헤더 후보로 제안한다.
- 신규 헤더명은 기존 CPF 명명 규칙과 `X-Cpf-Ext-*` 확장 정책을 따르고, 이름·의미·생성 주체·신뢰 경계·전파 여부·로그 여부·마스킹 여부·최대 길이를 `CpfHeaderSpecs`에 등록한다.
- 외부 공개 헤더, CPF 내부 전용 헤더, 로그 전용 context를 구분한다.
- 내부 전용 헤더는 외부 client 입력을 신뢰하지 않고 trusted Gateway/PFW ingress가 재생성한다.
- 기존 API와 서비스 호출의 backward compatibility를 유지한다.
- 헤더 변경이 불가피하면 alias 수신 기간, deprecation, dual-read/single-write, migration 순서와 회귀 테스트를 먼저 설계한다.
- 한 번에 기존 헤더명을 전면 변경하거나 신규 명명 체계로 치환하지 않는다.

신규 후보는 구현자가 inventory와 영향도 분석 후 확정한다. 최소 검토 대상은 Gateway instance, route ID/version, ingress type 정도이며, 기존 caller/segment/transaction 헤더로 표현 가능한 항목은 재사용한다.

확정된 확장은 다음을 한 묶음으로 구현한다.

- `CpfHeaderNames`
- `CpfHeaderSpecs`
- `CpfHeaderPropagator`
- ingress validation/filter/interceptor
- `TransactionHeader`와 `TransactionContext`
- `CpfRestClient`/`CpfWebClient`
- Service Call Engine
- segment/timeline
- DB 거래 로그
- 파일 로그
- 감사·metric·trace
- OpenAPI 필수 header 설명
- ADM 검색·상세·통계
- 기존 표준 헤더 unit/contract/runtime 회귀
- direct/PFW Gateway/external Gateway/Local Facade/Remote Proxy E2E

표준 헤더 상수만 추가하거나 로그 필드만 추가한 상태는 완료가 아니다. 기존 표준의 의미와 전파가 깨지지 않았다는 회귀 evidence와, 신규 Gateway 정보의 생성·검증·전파·위조 차단·ADM 조회 evidence가 모두 있어야 한다.

호출 경로와 무관하게 다음은 동일하게 남겨야 한다.

- standardExecutionId
- transactionGlobalId
- traceId/spanId
- parent/child segment
- source/target module
- 시작·종료 시각
- 상태·결과 코드
- failureStage
- duration
- timeout/retry/failover/circuit
- permission/audit/masking
- request/response metadata
- file log와 DB log 연결

ADM의 거래 상세·segment/timeline·검색·통계 화면은 호출 방식별 별도 구현이 아니라 동일 모델을 사용하고, ingressType과 Gateway 관련 필드로 경로 차이를 표현한다.

header, TransactionContext, segment/timeline, DB log, file log, audit, metric, OpenAPI 설명이 모두 일치해야 한다.

### runtime 기능

- route snapshot
- registry/health
- round-robin 또는 least-request
- weight
- draining 제외
- timeout
- 안전한 retry
- failover
- circuit breaker
- bulkhead
- connection pool
- request size
- rate limit
- load shedding
- graceful shutdown
- health/readiness
- client cancellation
- streaming pass-through
- 202 Accepted pass-through
- SSE/WebSocket은 기능 가능 여부와 제한을 명확히 검증
- Gateway가 업무 비동기 상태를 소유하지 않음

Gateway request path에서 매번 ADM DB/API를 동기 호출하지 않는다. ADM에서 버전형 route snapshot을 배포하고 Gateway는 마지막 정상 snapshot을 local memory와 안전한 local persistence에 유지한다.

### 외부 진입 및 내부 주제영역 간 호출 원칙

외부 Client·채널의 CPF 호출 방식은 인프라 구성과 설치 정책에 따라 선택 가능해야 한다. PFW Gateway 경유를 강제하지 않는다.

지원 모드:

1. `direct`
   - Client 또는 인프라가 대상 CPF 서비스의 주소를 알고 직접 호출
   - 단일 서버, modular-monolith, 소규모 설치, 개발·테스트 환경에서 사용 가능
   - 직접 호출하더라도 표준 실행 ID, transactionGlobalId, 인증·권한·감사·마스킹·표준 오류·로그 계약은 동일하게 적용

2. `pfw-gateway`
   - Client는 단일 host/port/URI와 표준 거래 ID를 사용
   - PFW Gateway Runtime이 route catalog와 registry를 이용해 대상 service/instance를 선택
   - MSA, 다중 인스턴스, 다수 채널, 단일 진입점이 필요한 환경에 권장

3. `external-gateway`
   - 회사 API Gateway, WAF, L4/L7, ingress 등 외부 인프라를 사용
   - 외부 Gateway가 CPF 서비스를 직접 route하거나 PFW Gateway 앞단에 위치할 수 있음
   - 외부 Gateway가 CPF 표준 header/context를 생성·검증·전파하는 경우 external adapter contract로 연동

설치 환경별 기본값을 강제하지 말고 `local/dev/stg/prod`와 배포 topology에서 명시적으로 선택한다. 어떤 모드를 선택해도 업무 source와 Facade Contract를 변경하지 않아야 한다.

주제영역 간 East-West 호출은 다음을 기본으로 한다.

- 동일 JVM 또는 modular-monolith: Facade Contract → Local Facade Adapter
- 별도 WAS/MSA: Facade Contract → Remote Facade Proxy → PFW Service Call Engine → Registry/Health → 대상 instance
- 서비스가 다른 서비스의 URL·IP·port를 source에 직접 조합하거나 Controller를 직접 호출하지 않는다.
- 내부 호출은 기본적으로 PFW Gateway를 재경유하지 않는다.
- `selectedInstanceId`, transactionGlobalId, segment/timeline, timeout/retry/circuit/failover는 PFW Service Call Engine이 처리한다.
- 망분리·DMZ·중앙 보안 정책 등 명시적 운영 요구가 있는 경우에만 target/service/profile별 Gateway 경유 adapter를 선택할 수 있다.
- 외부 Gateway, PFW Gateway, Service Call Engine에서 timeout/retry가 중복 적용되지 않도록 정책 owner와 deadline propagation을 명확히 한다.

### 외부 API Gateway 연동

다음 구성을 모두 지원한다.

1. 외부 Client → 대상 CPF 서비스 직접 호출
2. 외부 Client → PFW Gateway → CPF 서비스
3. 외부 API Gateway/WAF/L4 → CPF 서비스
4. 외부 API Gateway/WAF/L4 → PFW Gateway pool → CPF 서비스
5. 외부 Gateway가 CPF contract를 완전히 수행하는 external adapter mode

외부 Gateway에 거래별 route를 수동 등록하는 방식도 금지하지 않는다. 다만 생성기·ADM catalog·OpenAPI와 route 설정의 불일치를 방지할 수 있는 자동 검증 또는 동기화 방안을 제공한다.

---

## 1.5 PFW 공통 헬스·Registry와 ADM Gateway/서버 관리

PFW가 공통 health/registry capability를 소유한다. PFW 자체는 업무 실행 서버가 아니며, ADM/BZA/MBR/XYZ/ACC/BAT/generated domain/PFW Gateway Runtime이 PFW auto-configuration을 포함해 동일한 표준 health·readiness·heartbeat 기능을 자동으로 사용한다.

기존 actuator/health URI와 CPF health 구현을 먼저 inventory하고, 기존 URI·응답 계약을 임의로 바꾸거나 주제영역별 HealthController를 복제하지 않는다.

### PFW 공통 health capability

최소 다음을 PFW가 제공한다.

- liveness
- readiness
- startup 상태
- serviceId
- instanceId
- host/port/context path
- profile
- application/build/version
- startupAt
- lastHeartbeatAt
- health/readiness reason
- draining/maintenance
- active request 수
- worker/scheduler 상태
- DB/cache/broker/file server 등 contributor 확장 port
- Gateway route snapshot/registry cache 상태
- 표준 health client
- heartbeat/registration client
- health 결과와 ADM/registry 모델의 공통 DTO·상태 코드

업무 주제영역은 고유 dependency 상태만 contributor로 추가한다. PFW 공통 endpoint를 다시 구현하지 않는다.

### Service와 Instance Registry

ADM에서 다음을 등록·조회·통제할 수 있어야 한다.

서비스:

- serviceId
- 서비스명
- 주제영역
- 실행 유형
- owner
- routing 사용 여부
- 기본 timeout/retry/circuit/failover
- 등록 상태
- 배포 version

인스턴스:

- instanceId
- serviceId
- host/port/context path
- profile
- zone/node
- application version
- online/offline/stale/recovering
- liveness/readiness
- last heartbeat
- last active health check
- active/standby
- priority/weight
- draining
- maintenance
- manual block
- auto recovery policy
- active request 수
- 최근 선택·성공·실패 횟수

등록 방식은 자동 등록·heartbeat·정적 inventory·ADM 수동 등록을 지원할 수 있다. 수동 등록됐더라도 실제 readiness가 실패하면 routing 대상이 될 수 없다.

### Gateway와 Service Call Engine의 인스턴스 선택

PFW Gateway와 PFW Service Call Engine은 같은 registry/health 표준을 사용한다.

지원 정책:

- healthy/readiness 인스턴스만 선택
- round-robin
- least-request
- weight
- priority
- active/passive
- preferred instance
- standby
- draining 제외
- maintenance/manual block 제외
- zone/affinity
- 자동 failover
- 필요 시 특정 인스턴스로만 전달

예:

```text
MBR-01 ACTIVE priority=1 HEALTHY
MBR-02 STANDBY priority=2 HEALTHY
```

정상 시 MBR-01만 사용하고, MBR-01이 routing 불가능해지면 MBR-02로 전환할 수 있어야 한다.

### ADM 실시간 통제와 자동 대응

ADM은 control plane이며 실시간 업무 request 중계 서버가 아니다.

운영자는 ADM에서 다음을 즉시 통제할 수 있어야 한다.

- 신규 요청 차단
- 강제 route 제외·복귀
- ACTIVE/STANDBY 전환
- priority/weight 변경
- 특정 인스턴스로만 routing
- draining
- maintenance
- auto recovery 허용·금지
- route/policy version publish
- rollback
- circuit 강제 open/reset
- Gateway instance 제외
- override 만료시간 설정

Gateway와 Service Call Engine은 ADM 명령만 기다리지 않고 다음을 독립적으로 수행한다.

- heartbeat timeout 판단
- 주기적 active health/readiness check
- 실제 호출 결과 기반 passive health
- connection refused/timeout/연속 5xx/TLS/pool 오류 감지
- 자동 제외
- recovery probe
- RECOVERING → HEALTHY 단계적 복귀
- active/passive 자동 원복 또는 운영자 승인 후 원복 정책

우선순위:

```text
운영자 manual block
> maintenance/draining
> readiness/health 실패
> 자동 routing 정책
```

운영자가 ACTIVE로 지정했더라도 readiness가 실패하면 요청을 보내지 않는다. 운영자가 manual block한 인스턴스는 health가 회복돼도 자동 복귀시키지 않는다.

ADM 장애 중에도 Gateway와 Service Call Engine은 마지막 정상 route/instance snapshot과 자체 health 판단으로 계속 동작한다. 매 request마다 ADM API 또는 DB를 동기 조회하지 않는다.

### ADM 메뉴/API/UI

최소 메뉴:

- Gateway 대시보드
- Gateway 인스턴스
- 서비스 관리
- 서비스 인스턴스 관리
- health/readiness/heartbeat
- route/거래 catalog
- routing policy
- route snapshot version
- traffic/statistics
- 거래 로그/timeline
- 장애·미등록 거래
- 설정 변경 이력·감사
- publish/apply/rollback 상태

### ADM UI 완성도와 디자인·사용성 기준

ADM UI는 단순 HTML 존재, API 버튼 나열, 개발자용 JSON 출력 수준으로 완료 처리하지 않는다. 이후 사용자가 일괄 브라우저 검수할 때 즉시 운영 가능한 제품 화면으로 보이도록 다음을 구현한다.

#### 공통 화면 설계

- 일관된 header, left navigation, breadcrumb, page title, search/filter area, summary card, data grid, detail drawer/modal, action bar 구조를 사용한다.
- 서비스·인스턴스·Gateway·거래·배치·스케줄러·로그·감사 등 메뉴 간 spacing, typography, button, badge, form, table, pagination 스타일을 통일한다.
- 정상, 경고, 장애, 점검, draining, standby, disabled 상태는 색상만으로 구분하지 않고 label, icon, tooltip을 함께 제공한다.
- 과도한 장식보다 금융권 운영 화면에 맞는 깔끔하고 안정적인 디자인을 사용한다.
- 최소 desktop 운영 해상도에서 가독성을 보장하고, 창 폭 축소 시 검색 조건과 표가 깨지지 않도록 반응형 또는 가로 스크롤 정책을 제공한다.
- loading, empty, no-result, partial failure, API timeout, permission denied, session expired 상태를 각각 사용자 친화적으로 표시한다.
- confirm이 필요한 위험 작업은 영향 대상, 현재 상태, 변경 후 상태, 사유 입력을 보여주고 이중 실행을 막는다.
- 성공·실패 notification은 표준 오류 코드와 transactionGlobalId를 함께 표시하고 상세 기술 원문은 노출하지 않는다.
- 날짜·시간, duration, TPS, byte, count는 공통 formatter를 사용한다.
- 표의 정렬, pagination, page size, column visibility, refresh, auto-refresh on/off, last refreshed time을 필요한 화면에 제공한다.
- 브라우저 뒤로가기·새로고침·deep link에서 화면 상태와 route가 비정상화되지 않아야 한다.
- 키보드 focus, label, button title, contrast 등 기본 접근성을 확보한다.

#### 운영 기본 기능 누락 방지

각 목록 화면에는 기능 성격에 맞게 다음 기본 기능을 적용한다.

- 다중 검색 조건
- 검색 초기화
- 기간 검색
- 상태·서비스·인스턴스 filter
- 정렬
- pagination
- 상세 조회
- refresh
- 권한별 action 노출
- 변경 사유 입력
- 변경 전·후 값 확인
- audit 연결
- 오류 code와 transactionGlobalId 복사
- 긴 값 말줄임과 전체보기
- 민감정보 masking
- CSV/다운로드가 허용되는 화면의 download audit

서비스·인스턴스·Gateway 제어 화면에는 다음이 실제 동작해야 한다.

- active/standby 전환
- enable/disable
- draining/maintenance 시작·해제
- weight/priority 변경
- 자동 failback/manual failback 정책
- route snapshot publish/apply/rollback
- 적용 대상 Gateway와 적용 version 확인
- 현재값과 desired value 구분
- 처리 중 상태와 최종 반영 결과
- 실패 시 재시도 또는 rollback 안내
- optimistic locking 또는 version 충돌 방지
- 모든 조작의 operator, reason, timestamp, before/after audit

#### 대시보드 시각화

- 핵심 상태는 summary card와 표를 함께 제공한다.
- TPS, 성공/실패율, latency, timeout, retry, failover, circuit open, instance 선택 분포는 실제 API 데이터로 표시한다.
- 차트는 범례, 단위, 기간, tooltip, 데이터 없음 상태가 명확해야 한다.
- 실시간 갱신은 polling 주기와 마지막 갱신 시각을 보여주고 화면을 과도하게 깜박이지 않는다.
- 장애 인스턴스와 영향 route, 최근 실패 거래, failover 대상에 빠르게 이동할 수 있어야 한다.
- dashboard 숫자와 상세 목록의 filter 결과가 정합해야 한다.

#### UI 검증 evidence

- ADM을 실제 기동하고 주요 menu별 browser smoke를 수행한다.
- 최소 viewport, 일반 desktop viewport에서 screenshot을 남긴다.
- 정상, empty, validation, permission denied, API failure, loading 상태를 확인한다.
- 조회뿐 아니라 제어 작업은 API 결과, DB 또는 snapshot 상태, 감사 로그까지 연결해 검증한다.
- mock/static JSON만으로 UI 완료 처리하지 않는다.
- 화면 source가 있어도 실제 browser 확인 evidence가 없으면 `미검증`이다.
- evidence에는 URL, profile, commit SHA, 사용 계정 역할, 실행 시각, API 응답 결과를 민감정보 없이 기록한다.

### 서버·Gateway 상태 대시보드

- 서비스별 전체/정상/장애/stale/recovering/draining 수
- Gateway instance online/offline/stale/draining
- 인스턴스별 관리 상태와 실제 health 상태
- routing 가능 여부와 제외 사유
- active/standby/priority/weight
- last heartbeat/health check
- TPS와 active request
- 성공률·오류율
- average/P95/P99
- timeout/retry/failover
- circuit open
- rate-limit rejection
- load shedding
- 인스턴스 선택 분포와 편중
- route version과 배포 version 불일치
- CPU/memory/thread/connection pool
- request/response size
- 거래·서비스별 부하

### 거래 ID·transactionGlobalId 상세

호출 방식이 direct/PFW Gateway/external Gateway 중 무엇이든 동일한 거래 모델로 조회한다.

한 화면에서 다음을 연결한다.

- standardExecutionId
- transactionGlobalId
- ingress type
- PFW Gateway 경유 여부
- gatewayInstanceId
- 최초 업무 service/instance
- 각 segment의 currentInstanceId
- 다음 대상으로 선택한 selectedInstanceId
- local facade/remote proxy/gateway 경로
- route ID/version
- 선택 당시 health와 policy
- retry/failover 이력
- failure stage
- 전체 timeline
- recent failure
- success rate
- P95/P99
- 변경 이력

예:

```text
Client → GW-02 → MBR-01 → ACC-02
```

ADM은 GW-02, MBR-01, ACC-02를 같은 transactionGlobalId와 segment/timeline으로 정확히 보여야 한다.

### 설정 반영

- draft
- validation
- maker-checker 또는 approval
- publish
- versioned snapshot
- instance별 apply result
- partial failure
- rollback
- last-known-good recovery
- ADM 장애 중 지속 동작
- 변경 전후·사유·작업자 audit

prod route/policy 변경은 BZA approval capability와 연계 가능하되 route/registry 운영 ownership은 ADM에 둔다.

---

## 1.6 CPF 전체 embedded/external Tomcat 표준화

모든 실행 애플리케이션에 동일 원칙을 적용한다.

대상:

- ADM
- BZA
- MBR
- XYZ
- ACC
- BAT
- generated domain
- PFW Gateway Runtime

### embedded mode

- executable bootJar 또는 executable WAR
- embedded Tomcat
- `java -jar`
- DB 정보는 jar 내부에 포함하지 않음
- 외부 yml/properties
- environment variable
- JVM system property
- secret provider

### external mode

- standard WAR
- external Tomcat
- JNDI DataSource
- 동일 source와 동일 기능
- context path 명시
- classloader/thread/JDBC driver cleanup
- graceful undeploy/redeploy

### parity 원칙

다음 기능은 embedded/external에서 같아야 한다.

- authentication/permission
- audit/masking
- transactionGlobalId
- standard header
- Gateway header
- OpenAPI
- SQL/Flyway
- logging
- scheduler/worker
- health/readiness
- ADM monitoring
- error response
- graceful shutdown
- profile loading

### 실제 evidence

- embedded startup
- external Tomcat WAR deployment
- JNDI lookup
- external-config lookup
- 동일 API contract
- 동일 DB CRUD
- 동일 log/header
- undeploy 후 thread/port cleanup
- 여러 CPF WAR를 한 Tomcat에 배포할 때 context/JNDI/MBean/log/scheduler 충돌 검증

---

## 1.7 로컬 개발 전체 실행 환경

로컬 개발자 PC에서도 필요한 실행 주체를 선택해 모두 기동할 수 있어야 한다.

### 지원 시나리오

- local-min
- local-full
- local-multi-instance
- 선택 서비스 기동
- 단일 서비스 기동
- 전체 상태 조회
- 전체 종료
- 장애 재현

### 선택 기동 대상

- PFW Gateway Runtime
- ADM
- BZA
- MBR
- XYZ
- ACC
- BAT
- generated domain
- MariaDB
- local broker adapter 또는 실제 broker profile

### 로컬 운영 기능

- 기본 port inventory
- port conflict preflight
- instance ID
- automatic port increment
- PID tracking
- stale PID cleanup
- registry registration
- Gateway route refresh
- log root
- lightweight JVM heap
- small DB pool
- optional scheduler/worker
- start/stop/status/smoke 공통 명령
- Windows 기준 우선 지원, Linux shell parity 가능 여부 기록

지원한다고 선언한 각 topology(`direct`, `pfw-gateway`, `external-gateway`)는 해당 topology별 runtime evidence를 남긴다. Gateway를 사용하지 않는 설치 환경에 Gateway 경유 evidence를 강제하지 않으며, PFW Gateway 기능 완료 판정에는 PFW Gateway 경유 E2E가 반드시 필요하다.

---

## 1.8 PFW 표준 오류 코드·메시지·응답 체계 완성

기존 PFW의 `CpfException`, `CpfBusinessException`, `CpfValidationException`, `CpfSystemException`, `CpfExternalServiceException`, `CpfErrorCode`, `CpfFrameworkErrorCode`, `CpfDynamicErrorCode`, `CpfErrorDefinition`, `CpfMessageResolver`, `CpfMessageFormatter`, `CpfResponseCodeResolver`, `CpfErrorResponse`, `CpfGlobalExceptionHandler` 구조를 먼저 전수 확인한다.

잘 구현된 기존 구조는 삭제·복제·재작성하지 않고 재사용한다. 부족한 예외 경로, 메시지 안전성, 추적 context, Gateway/OpenAPI/ADM 연계를 보강한다.

### 오류 코드와 메시지 catalog

- 업무·기술 source는 외부 사용자 문장을 임의로 조립하지 않고 오류 코드와 named message argument를 발생시킨다.
- 외부 메시지는 오류 code catalog에 등록된 locale별 external message template에서 생성한다.
- 내부 운영 메시지, root cause, exception class, SQL, host, port, file path, 내부 URI는 별도로 관리하고 외부 응답에 노출하지 않는다.
- 오류 정의에는 최소 errorCode, HTTP status, category, externalMessageTemplate, retryable, severity, locale, enabled, version을 포함한다.
- 필요 시 operatorMessageTemplate, alarmPolicy, owner, runbookId를 내부 운영 metadata로 관리한다.
- 오류 코드 중복, 미등록 코드 사용, 비활성 코드 사용, placeholder 불일치, locale 누락을 build/qualityGate에서 탐지한다.
- 오류 코드 형식은 기존 정본과 구현을 먼저 확인하고 충돌이 없으면 유지한다. 표준이 불명확하거나 혼재하면 PFW 소유 고정 길이 규칙과 module/feature/sequence 의미를 정의하고 일괄 migration한다.
- 표준 실행 ID와 오류 코드는 목적이 다르므로 혼동하지 않는다. 응답에는 둘을 별도 필드로 제공한다.

### named parameter 메시지 조립

예시 개념:

```java
throw CpfBusinessException.of(
    "ECMNSQ0001",
    Map.of("sequenceKey", sequenceKey),
    cause
);
```

등록 템플릿:

```text
이미 등록된 채번 키입니다: {sequenceKey}
```

외부 응답 메시지:

```text
이미 등록된 채번 키입니다: ORDER_NO
```

필수 정책:

- 위치 기반 `{0}`, `{1}`보다 이름 기반 placeholder를 사용한다.
- 허용된 argument 이름, 필수 argument, type, 최대 길이를 검증한다.
- null과 누락 argument에 대한 안전한 fallback을 제공한다.
- 개인정보·계좌·token·secret·credential·전문 원문은 argument로 외부 노출하지 않는다.
- masking, 길이 제한, 제어문자·개행 제거 후 메시지를 조립한다.
- message format 실패 시 내부 exception 원문을 사용하지 않고 등록된 안전 fallback code/message로 반환한다.
- `exception.getMessage()` 또는 개발자가 넘긴 임의 externalMessage가 등록 template보다 무조건 우선하지 못하게 한다.
- 외부 메시지 override가 필요하면 명시적 allowlist와 안전성 검증을 거친다.

### 단일 표준 오류 envelope

Controller, validation, security, Gateway, Service Call Engine, batch 접수, scheduler/worker 상태 API, broker 처리 조회가 서로 다른 오류 body를 반환하지 않는다.

권장 응답 개념:

```json
{
  "success": false,
  "error": {
    "code": "ECMNSQ0001",
    "message": "이미 등록된 채번 키입니다: ORDER_NO",
    "category": "CONFLICT",
    "retryable": false,
    "fieldErrors": []
  },
  "context": {
    "standardExecutionId": "OXYZAA0001",
    "transactionGlobalId": "20260716XYZ0000001234",
    "segmentId": "SEG0003",
    "timestamp": "2026-07-16T09:10:11.123+09:00"
  }
}
```

- 실제 field name은 기존 `CpfErrorResponse`와 backward compatibility를 먼저 확인해 확정한다.
- standardExecutionId, transactionGlobalId, segmentId, timestamp를 오류 응답과 response header에 일관되게 연결한다.
- exception class simple name을 외부 body/header에 노출하지 않고 `BUSINESS`, `VALIDATION`, `SECURITY`, `ROUTING`, `SYSTEM` 같은 안전 category로 변환한다.
- stack trace, SQL, DB table/column, server address, internal path, secret를 외부에 반환하지 않는다.
- 상세 원인은 transactionGlobalId와 segment를 통해 ADM에서 조회한다.
- 모든 오류를 HTTP 200으로 감싸지 않고 400, 401, 403, 404, 409, 422, 429, 500, 502, 503, 504 등 의미에 맞는 HTTP status를 유지한다.

### 전역 예외 처리 범위

최소 다음을 모두 단일 표준 오류 envelope로 변환한다.

- `CpfException` 계열
- Bean Validation
- request parameter type mismatch
- JSON parse/deserialization
- missing header/parameter/body
- authentication failure
- access denied
- CSRF 또는 내부 service identity 실패
- unsupported HTTP method/media type
- not found route
- Gateway unknown ID/route/instance unavailable/timeout/circuit
- Service Call Engine timeout/failover exhausted/unknown result
- optimistic lock/duplicate/idempotency conflict
- file upload/size/content type 오류
- batch duplicate/restart/rerun 상태 오류
- 일반 미처리 `Exception`

Spring 기본 error HTML/JSON 또는 module별 임의 Map 응답이 외부에 노출되면 완료가 아니다.

### Gateway·직접 URL·ID 호출의 오류 일관성

- 자연어 URI, ID header, ID URI 호출이 같은 오류 code, HTTP status, message policy, context를 사용해야 한다.
- URI ID, header ID, URL catalog ID 불일치는 별도 표준 오류 code로 거부한다.
- Gateway가 대상 서비스의 표준 오류를 받으면 안전성을 검증한 뒤 의미를 보존해 전달한다.
- Gateway 자체 오류도 동일 PFW formatter/resolver를 사용한다.
- `S` 공유 API 외부 우회 호출은 표준 security/routing 오류로 반환하고 감사 로그를 남긴다.

### Swagger/OpenAPI와 ADM

- 모든 `O`, `S` API에 공통 오류 schema와 실제 발생 가능한 HTTP status를 연결한다.
- 오류 code 예시, message parameter 조립 예시, transactionGlobalId로 ADM에서 조회하는 방법을 제공한다.
- ADM에는 오류 code catalog 조회·검색, locale message, HTTP status, retryable, enabled/version을 확인할 수 있는 기능을 제공한다.
- catalog 변경 기능이 기존 ADM에 있다면 권한, validation, version, audit, rollback을 보강한다.
- 외부 메시지와 내부 운영 메시지는 화면과 권한에서 명확히 분리한다.

### 필수 테스트와 evidence

- 등록 message와 named argument 정상 조립
- placeholder 누락·초과·잘못된 이름
- masking과 최대 길이
- locale 선택과 fallback
- 미등록 error code fallback
- validation/security/Gateway/Service Call/일반 예외
- exception class, SQL, stack trace 비노출
- HTTP status 정합성
- transactionGlobalId, standardExecutionId, segmentId 연결
- URI/ID/direct 호출 오류 동일성
- OpenAPI 공통 오류 schema
- ADM code/message 조회
- MariaDB message/code seed와 Flyway/all_install 정합성

위 범위가 실제 runtime evidence로 확인되지 않으면 오류 표준은 `부분 구현` 또는 `미검증`으로 기록한다.

## 1.9 Domain/Feature-first package 구조와 안정적 확장점

### 최상위 원칙

패키지는 다음 순서를 따른다.

```text
주제영역 또는 PFW/CMN
→ 업무·기술 기능
→ controller/service/dto/repository/port/adapter 등 구현 계층
```

기술 계층을 먼저 만들고 서로 무관한 기능을 한 package에 섞지 않는다.

금지 예:

```text
cpf.mbr.controller
cpf.mbr.service
cpf.mbr.dto
cpf.mbr.repository
```

권장 예:

```text
cpf.mbr.auth.controller
cpf.mbr.auth.service
cpf.mbr.member.controller
cpf.mbr.member.service
cpf.mbr.partner.repository
cpf.mbr.terms.policy
```

### PFW package

PFW는 공통 기술 capability별로 나눈다.

```text
cpf.pfw.gateway
cpf.pfw.servicecall
cpf.pfw.registry
cpf.pfw.health
cpf.pfw.transaction
cpf.pfw.header
cpf.pfw.logging
cpf.pfw.audit
cpf.pfw.security
cpf.pfw.masking
cpf.pfw.idempotency
cpf.pfw.outbox
cpf.pfw.inbox
cpf.pfw.broker
cpf.pfw.reconciliation
cpf.pfw.filetransfer
cpf.pfw.attachment
cpf.pfw.remotelog
cpf.pfw.archive
cpf.pfw.scheduler
cpf.pfw.runtime
cpf.pfw.openapi
cpf.pfw.web
```

필요한 capability 내부에서만 `core/port/adapter/config/model/support`를 사용한다.

PFW 금지:

- ACC/MBR/BZA 등 업무 domain 의존
- 특정 업무명 기술 class
- 업무 DTO·테이블 소유
- 업무 모듈별 기술 engine 복제
- 범용 개발자 EDU 소유

### CMN package

CMN은 공통 업무 helper와 데이터 처리 기능별로 나눈다.

```text
cpf.cmn.code
cpf.cmn.message
cpf.cmn.validation
cpf.cmn.converter
cpf.cmn.fixedlength
cpf.cmn.telegram
cpf.cmn.money
cpf.cmn.calendar
cpf.cmn.text
cpf.cmn.number
cpf.cmn.fixture
```

CMN은 Gateway/retry/circuit 같은 PFW engine이나 업무 repository/service를 소유하지 않는다.

### 업무 주제영역 package

업무 모듈은 domain/feature vertical slice로 구성한다.

MBR 예:

```text
cpf.mbr.foundation
cpf.mbr.auth
cpf.mbr.member
cpf.mbr.partner
cpf.mbr.terms
```

각 기능 아래:

```text
controller
service
dto
entity
repository
mapper
port
adapter
policy
```

를 필요한 만큼만 둔다.

ACC 예:

```text
cpf.acc.foundation
cpf.acc.account
cpf.acc.reference
```

BZA 예:

```text
cpf.bza.foundation
cpf.bza.auth
cpf.bza.operator
cpf.bza.organization
cpf.bza.role
cpf.bza.permission
cpf.bza.menu
cpf.bza.approval
cpf.bza.attachment
cpf.bza.audit
cpf.bza.support
```

ADM 예:

```text
cpf.adm.foundation
cpf.adm.dashboard
cpf.adm.gateway
cpf.adm.registry
cpf.adm.health
cpf.adm.transaction
cpf.adm.batch
cpf.adm.centercut
cpf.adm.scheduler
cpf.adm.log
cpf.adm.audit
cpf.adm.security
cpf.adm.configuration
cpf.adm.notification
cpf.adm.reliability
```

ADM `gateway/registry/health`는 control-plane 기능이며 PFW Gateway의 data-plane routing engine을 복제하지 않는다.

### XYZ EDU package

온라인·일반 개발 예제는 XYZ에 둔다.

```text
cpf.xyz.edu.transaction
cpf.xyz.edu.header
cpf.xyz.edu.servicecall
cpf.xyz.edu.gateway
cpf.xyz.edu.health
cpf.xyz.edu.security
cpf.xyz.edu.permission
cpf.xyz.edu.masking
cpf.xyz.edu.audit
cpf.xyz.edu.idempotency
cpf.xyz.edu.broker
cpf.xyz.edu.attachment
cpf.xyz.edu.remotelog
cpf.xyz.edu.filetransfer
cpf.xyz.edu.archive
cpf.xyz.edu.reconciliation
cpf.xyz.edu.fixedlength
cpf.xyz.edu.openapi
```

각 EDU도 기능 아래 controller/service/dto/port/adapter를 둔다. XYZ EDU는 실제 PFW port/engine을 호출하며 local Map/List나 가짜 자체 engine으로 완료 처리하지 않는다.

### BAT package

배치·센터컷 예제는 BAT에 둔다.

```text
cpf.bat.foundation
cpf.bat.runtime
cpf.bat.scheduler
cpf.bat.worker
cpf.bat.centercut
cpf.bat.edu.ondemand
cpf.bat.edu.tasklet
cpf.bat.edu.chunk
cpf.bat.edu.partition
cpf.bat.edu.restart
cpf.bat.edu.checkpoint
cpf.bat.edu.retry
cpf.bat.edu.idempotency
cpf.bat.edu.reconciliation
cpf.bat.edu.servicecall
```

BAT EDU도 실제 PFW transaction/service-call/logging/scheduler capability를 사용한다.

### BaseController/BaseService

안정적 확장점은 다음 깊이로 제한한다.

```text
CpfBaseController
→ ModuleBaseController
→ FeatureController
```

```text
CpfBaseService
→ ModuleBaseService
→ FeatureService
```

예:

```text
cpf.pfw.web.base.CpfBaseController
cpf.mbr.foundation.web.MbrBaseController
cpf.mbr.member.controller.MemberController
```

```text
cpf.pfw.service.base.CpfBaseService
cpf.mbr.foundation.service.MbrBaseService
cpf.mbr.member.service.MemberService
```

초기 base class가 비어 있어도 허용한다. DTO/entity/repository/mapper를 억지 상속시키지 않는다. port/adapter는 interface 중심으로 둔다.

### 주제영역 간 호출 package

다른 주제영역의 내부 service/repository/mapper를 import하지 않는다.

```text
feature service
→ facade/port contract
→ local adapter 또는 remote proxy
→ PFW Service Call Engine
```

상대 업무 모듈의 내부 package 직접 참조, Controller 호출, URL 직접 조합을 금지한다.

### test package

test package는 main package를 mirror한다.

```text
src/main/java/cpf/mbr/member/service/MemberService.java
src/test/java/cpf/mbr/member/service/MemberServiceTest.java
```

전역 `cpf.*.test`, `cpf.*.tst`에 기능별 test를 몰아넣지 않는다. 공통 fixture만 `testsupport`로 제한한다.

### common/util/support 제한

`common`, `util`, `helper`, `support`, `misc`, `base`를 범용 쓰레기통으로 사용하지 않는다. 소유 capability나 feature 아래에서만 제한적으로 사용한다.

### package 이전 절차

전체 package를 한 번에 이름만 변경하지 않는다.

1. class·기능 inventory
2. ownership 확정
3. feature 단위 이동
4. import·Spring scan·bean name 확인
5. MyBatis namespace와 mapper XML 확인
6. AOP·transaction·reflection 문자열 확인
7. OpenAPI path/operationId 확인
8. test package 동시 이동
9. runtime 회귀
10. 이전 package 잔존 검사

compile 성공만으로 완료하지 않는다. Controller route, Spring bean, AOP, transaction, MyBatis, OpenAPI, logging, permission, runtime smoke까지 확인한다.

---

# 2. 품질 게이트 복구 및 강화

## 2.1 check-feature-evidence 복구

현재 13개 파일 존재 check로 축소된 gate를 최종 완료 판정용으로 사용하지 않는다.

각 기능 check ID마다 다음 묶음을 검증한다.

- capability source
- 계층 연결
- unit/contract test
- EDU sample source
- EDU test
- Controller
- OpenAPI annotation
- unique operationId
- request/response schema
- standard error response
- required header
- permission/audit/masking description
- sample coverage
- SQL/Flyway/all_install
- runtime evidence
- report/matrix/evidence consistency

완료 상태인데 하나라도 누락되면 gate 실패.

## 2.2 반드시 탐지할 오류

- 없는 evidence
- stale evidence
- 다른 branch/profile/DB evidence
- `SKIPPED` test가 완료 근거
- CoverageCatalog-only 완료
- source만 있고 test 없음
- test만 있고 runtime 필수 기능 미실행
- OpenAPI path count만 있고 schema/오류/header 누락
- EDU가 local Map/List/fake logic만 사용
- XYZ/BAT EDU가 실제 PFW port/engine을 호출하지 않음
- generated domain을 수동 patch해 통과
- ACC/EXS 디렉터리 부재만 cleanup 완료
- in-memory adapter만 있는 cluster 기능 완료
- static UI marker만으로 browser 완료
- SQL 합본 생성만으로 MariaDB 설치 완료
- external Tomcat WAR 생성만으로 배포 완료
- 상태값 불일치
- report/matrix/index 간 check ID 누락

## 2.3 architecture gate

ArchUnit 또는 동등한 gate로 검증한다.

- PFW → 업무 domain 의존 금지
- CMN → 업무 구현 의존 금지
- 업무 domain 간 repository/mapper/internal class 참조 금지
- Controller → Repository 직접 접근 금지
- 기술 engine의 업무 모듈 소유 금지
- feature package 혼합 금지
- root controller/service/dto package 신규 추가 금지
- main/test package mismatch
- BaseController/BaseService hierarchy
- generated domain ownership
- EDU ownership
- Gateway core/runtime ownership
- ADM control-plane과 Gateway data-plane 분리

---

## 2.4 한글 주석·JavaDoc 품질과 지속 유지 gate

기존 source, 신규 source, generator template, EDU sample을 전수 점검해 기능·역할·정책을 설명하는 한글 주석을 보강한다.

### 필수 주석 기준

- 주요 class/interface/annotation/configuration에는 한글 JavaDoc으로 목적, 소유 capability, 호출 주체, 주요 흐름, transaction/보안/감사 유의사항을 설명한다.
- public/protected method에는 기능, parameter, return, 주요 예외, transaction 경계, 멱등성, 권한, 실패·재시도 정책을 설명한다.
- 복잡한 private method도 이름만으로 정책을 이해하기 어려우면 한글 설명을 추가한다.
- timeout/retry/circuit/failover, row lock, idempotency, outbox/inbox, broker ack/replay, unknown/reconciliation, masking, Gateway route 선택, multi-instance, scheduler/worker, batch restart/rerun, 상태 전이 앞에는 왜 해당 처리가 필요한지 block 주석을 둔다.
- DTO field, enum status, configuration property, SQL/Flyway의 중요한 column·index·constraint에도 의미와 운영 목적을 설명한다.
- EDU는 학습자가 PFW/CMN port와 실제 처리 흐름을 따라갈 수 있도록 class·method·핵심 단계 설명을 제공한다.

### 금지

- 모든 line에 기계적으로 `값을 설정한다`, `조건을 확인한다` 같은 코드 반복 주석을 추가하지 않는다.
- 영어 자동 생성 문장, 내용 없는 JavaDoc, 복사된 잘못된 domain 설명으로 수량만 채우지 않는다.
- 구현 변경 후 주석이 실제 동작과 달라진 채 남지 않게 한다.
- 민감정보, 계정, URL, password 예시를 주석에 기록하지 않는다.

### 지속 유지

- create-domain generator가 생성하는 class와 test에도 동일한 한글 JavaDoc 기준을 적용한다.
- 신규·수정된 Java/Kotlin/SQL/script 파일의 주석 품질을 review checklist와 qualityGate에서 확인한다.
- 단순 주석 비율만 강제하지 말고 public API JavaDoc 누락, 핵심 annotation 설명 누락, 복잡 정책 block 설명 누락을 탐지한다.
- suppression이 필요하면 사유와 범위를 명시한다.
- 주석 보강으로 기능 구현·테스트를 대신하지 않는다.

### evidence

- 대상 파일 inventory와 보강 결과
- 대표 PFW capability, 업무 feature, XYZ/BAT EDU, generated ACC sample
- 주석 gate 실행 결과
- 잘못된 주석을 탐지하는 negative test

# 3. 보강 범위

## 3.1 BZA

현재 구현을 유지하며 실제 DB/browser 기준으로 완성도를 높인다.

- bootstrap
- 최초 비밀번호 변경
- lock/unlock
- access/refresh rotation
- concurrent refresh reuse rejection
- role/menu/button/API permission
- data scope
- organization/employee
- approval
- optimistic lock
- audit before/after/reason
- attachment
- download audit
- permission simulation
- 인증 후 browser E2E
- account별 200/403
- MariaDB transaction/rollback

DB runtime과 browser E2E가 없으면 완료로 승격하지 않는다.

## 3.2 remote log

- 운영용 HTTP/mTLS node client
- service credential/secret provider
- DB/Redis 공유 job queue
- cluster rate limit
- multi-instance claim
- process restart recovery
- expiry cleanup
- one-time token atomic consume
- partial failure bundle
- poison isolation
- checksum
- masking
- symlink/path traversal
- shared storage 또는 central logging adapter
- ADM multi-node E2E

`InMemoryCpfRemoteLogBundleJobAdapter`는 local 개발 adapter로 명확히 표시한다.

## 3.3 attachment

- local adapter
- shared filesystem adapter
- object storage port/adapter skeleton
- antivirus scan port
- content type/extension/size/checksum
- upload/download audit
- permission/masking
- one-time/expiring grant 필요 여부
- cleanup/orphan reconciliation
- multi-instance
- XYZ EDU
- BZA 실제 업무 사용
- MariaDB metadata runtime

## 3.4 standard execution catalog

- online/batch/Gateway route 단일 catalog 모델
- source annotation/manifest 자동 discovery
- duplicate, retired ID, version conflict
- DB registration
- ADM 관리
- generated domain 자동 등록
- log/timeline binding
- OpenAPI operationId binding
- Gateway route binding
- batch standard ID binding

---

# 4. 착수 범위

실제 외부 환경이 없으면 port/adapter/test harness까지 착수하고 상태를 정확히 남긴다.

- Kafka/RabbitMQ/Redis real adapter
- SFTP/FTP/FTPS/SCP/SSH real server
- object storage
- external secret manager
- central log collector
- WebFlux/Reactor Netty Gateway runtime 비교 PoC

Gateway 기본 운영형은 embedded/external Tomcat parity가 가능한 Servlet/Web MVC 계열로 완성한다. WebFlux는 대량 동시 연결·streaming 요구를 실제 부하 테스트로 비교한 뒤 선택 runtime으로 추가한다. 두 runtime을 동시에 미완성으로 만들지 않는다.

---

# 5. 후순위·제외 범위

- API 상품/과금
- 개발자 portal
- 범용 ESB
- 업무 payload 변환 엔진
- Gateway의 saga/orchestration 소유
- Gateway의 batch/broker 업무 상태 소유
- 무분별한 신규 HTML
- source가 계속 바뀌는 상태에서 DOCX/PDF 최종 정본화
- ACC 자동 삭제
- EXS 재실행 모듈 복원

---

# 6. 실행 검증과 evidence

## 6.1 필수 실행

환경 접근이 가능하면 반드시 실행한다.

- Java 25 compile/test/bootJar/bootWar
- qualityGate
- empty MariaDB full install
- repeat install
- upgrade Flyway
- all_install
- all_install_and_smoke
- smoke_check
- embedded startup
- external Tomcat WAR
- JNDI
- local Gateway
- MBR/ACC route
- BAT on-demand
- BZA auth/browser
- ADM Gateway UI
- multi-instance Gateway
- multi-instance target service
- failover/circuit/drain
- log/timeline
- shutdown/port closure

접속 불가 시 임의 설치하지 않는다. preflight 결과와 재현 명령을 evidence로 남기고 `미검증` 처리한다.

## 6.2 evidence 메타데이터

모든 evidence에 다음을 포함한다.

- commit SHA
- branch
- dirty/clean 상태
- OS
- Java
- Gradle
- profile
- DB vendor/version/schema
- broker/file server 종류
- instance topology
- startedAt/endedAt
- command
- exit code
- result
- sanitized hash

민감정보 원문은 제거한다.

---

# 7. 문서와 상태 갱신

필수 갱신:

- `CPF_STABILIZATION_REPORT.md`
- `CPF_GAP_MATRIX.md`
- `CPF_EVIDENCE_INDEX.md`
- `specs/기능_구현_매트릭스.json`
- `specs/기능_구현_매트릭스.md`
- `specs/sample-coverage-matrix.md`
- `README.md`

`CPF_STABILIZATION_REPORT.md`는 초기화하거나 기존 실패·미검증·이력을 삭제하지 않는다.

- 기존 누적 이력과 기준 commit을 보존한다.
- 이번 작업의 시작/종료 SHA, 실제 구현, 실행 검증, stale evidence 교체, 남은 gap을 추가·재판정한다.
- 오래된 중복 설명은 정리할 수 있으나 과거 미검증·실패와 근거를 지워 완료처럼 보이게 하지 않는다.
- report, gap, evidence index, 기능 matrix, sample coverage의 상태를 일치시킨다.

`README.md`는 개발 진행 보고서가 아니라 완성 제품의 공식 소개 문서로 재작성한다.

README에서 제거:

- 최종 목표 목록
- 현재 개발 중·다음 작업·미구현·gap·완료율
- Codex 요청·검수 과정·커밋별 작업 일지
- 내부 evidence 경로의 장황한 나열
- 반복되는 정본화 계획

README에 유지·정리:

- CPF 제품 개요
- 핵심 기능
- 기술 사양
- 아키텍처
- 모듈 역할
- `O/S/B` 표준 실행 ID 개념
- 자연어 URI와 ID 호출 모델
- `S` 내부 공유 API 정책
- 표준 오류 처리
- security/audit/masking
- 배치·비동기·Gateway·ADM
- embedded/external Tomcat
- local/dev/stg/prod 실행·배포
- domain/feature-first 개발 구조
- 실제 확인된 설치·실행 명령

README는 완제품 문서처럼 간결하고 전문적으로 작성하되, 미구현·미검증 기능을 사실과 다르게 구현 완료라고 단정하지 않는다. 구현 상태와 gap은 report/matrix에서만 관리한다.

DOCX 9종은 이번 구조 변경 중 반복 재생성하지 않는다. 최종 구조와 기능 상태가 안정된 후 별도 정본화 작업에서 생성한다.

각 항목은 다음을 분리해 기록한다.

- 검증 사실
- Codex 주장
- 추정
- 미확인 사항
- 직접 실행 미수행

---

# 7.1 이번 마일스톤 완료와 CPF 최종 완료 구분

이번 요청 범위를 100% 구현·검증해도 다음이 남으면 CPF 전체 목표를 `완료` 또는 진척률 100%로 기록하지 않는다.

- 실제 broker
- 실제 SFTP/FTP/FTPS/SCP/SSH
- object storage
- external secret manager
- 물리 다중 서버·network partition
- 장시간 soak/load/capacity
- 보안 취약점·침투 점검
- backup/restore·DR
- rolling deployment·rollback
- 무중단 schema migration
- 최종 문서 정본화와 전수 evidence 재검증

이번 마일스톤 완료는 `상용화 직전 통합 완성 후보`로 기록하고, 정본 요구사항 ID별 잔여 상태를 다시 산정한다.

---

# 8. 최종 제출 형식

1. 시작/종료 SHA와 worktree 상태
2. 변경 파일 전체 목록
3. 삭제·이동·복원 inventory
4. 구현 완료 목록
5. 부분 구현
6. 미구현
7. 미검증
8. 실패와 원인 분류
9. 직접 실행 명령과 결과
10. evidence 경로
11. 상태 ledger
12. ADM/BZA 주요 화면별 브라우저 검증 표와 screenshot 경로
13. URI·ID 호출, `S` 내부 전용, 표준 오류 runtime 검증 표
14. 다음 gap

최종 보고에서 다음 표현을 금지한다.

- 파일이 있으므로 완료
- 테스트가 있으므로 완료
- Swagger UI가 열리므로 완료
- sampleId가 있으므로 완료
- 환경이 없어 실행하지 않았지만 완료
- qualityGate가 통과했으므로 전체 완료
- ACC/EXS를 삭제했으므로 cleanup 완료

이번 작업은 기존 구현을 파괴하거나 목표를 낮추는 정리 작업이 아니라, CPF를 실제 개발·배포·운영·감사할 수 있는 상용 framework 구조로 끌어올리는 작업이다.
