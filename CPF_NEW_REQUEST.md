# CPF 다음 대형 작업 최종 요청서 — 전체 프레임워크 상용화 범위

## 0. 작업 기준과 금지사항

- repository: `https://github.com/freeangelsun/202412_01_CPF`
- branch: `master`
- 작업 시작 시 최신 master SHA를 기록한다.
- 최상위 정본은 `CPF_FINAL_TARGET_REQUIREMENTS.md` 단일 파일이다.
- 분할 보조 목표 파일은 사용하지 않으며 모든 목표 확인과 완료 판정은 원본 정본 전체를 기준으로 한다.
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
- 외부에서 `S` ID header 또는 `S` URI를 직접 보내도 Gateway 유무와 관계없이 PFW 공통 실행·보안 정책과 대상 애플리케이션 ingress에서 거부한다. Gateway가 존재하면 동일 정책을 재사용한 선행 차단을 추가할 수 있다.
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
- 외부 client가 내부 전용 route·service·instance 정보를 위조해 전달하지 못하도록 최초 신뢰 ingress와 PFW inbound policy가 외부 입력을 제거·재생성한다. Gateway가 없는 topology에서도 동일해야 한다.
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

PFW Gateway는 선택 설치 구성이다. Gateway를 사용하지 않는 direct, L4/WAF 직접 전달, 동일 JVM Local Facade, scheduler, worker, batch, broker 호출에서도 동일한 실행 정책이 적용되어야 한다.

Gateway의 기본 책임은 다음과 같은 경량 data plane·transfer 역할이다.

- 외부 요청 수신
- ID·URL·HTTP method 해석
- route snapshot에 따른 target service·endpoint·instance 선택
- 표준 header와 거래 context 전달
- timeout·deadline 등 전송 계층 정책 적용
- 대상 서비스 응답과 표준 오류 전달
- Gateway 구간 로그·metric·trace 기록
- 명백히 잘못된 public route 또는 형식 오류의 선택적 선행 차단

다음 최종 판단은 Gateway의 독점 책임으로 두지 않는다.

- `O/S/B` 실행 유형 정책
- `S` 내부 전용 접근 통제
- originalChannelCode·currentChannelCode 검증
- client/channel/service identity binding
- caller channel·caller service 허용 여부
- target domain·target service 일치 여부
- 거래별 허용 채널
- permission·security·audit·masking
- idempotency·unknown/reconciliation 업무 정책

위 정책은 Gateway 유무와 관계없이 PFW 공통 Policy Engine과 대상 실행 애플리케이션에서 최종 강제한다. Gateway는 같은 PFW 정책 component를 재사용해 조기 차단할 수 있으나 Gateway만 통과하면 실행을 허용하는 구조로 만들지 않는다.

Gateway는 업무 로직, 업무 DB 처리, saga 상태, broker workflow, reconciliation 업무 판단을 소유하지 않는다.

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

- 통합 운영 대시보드
- BAM 거래·채널 통계
- 성공/오류 거래 통계
- 거래별 처리 현황
- 채널별 처리 현황
- 거래×채널 교차 통계
- Gateway 대시보드
- Gateway 인스턴스
- 서비스 관리
- 서비스 인스턴스 관리
- health/readiness/heartbeat
- 표준 실행 거래 catalog
- 거래별 채널 정책
- 채널 마스터 관리
- client/service-channel binding
- 거래 정책 Export
- 거래 정책 파일 Import
- 정책 사전 등록·배포 준비 현황
- 승인결재 요청함
- 승인결재 처리함
- 정책 적용·rollback 이력
- O/S/B 거래 테스트 콘솔
- routing policy
- route snapshot version
- traffic/statistics
- 거래 로그/timeline
- 로그 원본/포맷 조회
- 오류 코드·메시지 catalog
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
- ADM과 BAM을 포함한 모든 운영·관리 UI는 반응형을 기본 표준으로 한다. 특정 desktop 폭에 고정하지 않고 desktop, notebook, tablet, mobile viewport에서 메뉴·검색·표·카드·차트·modal·drawer가 깨지지 않아야 한다.
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

### PFW 통합 채널 레지스트리와 거래별 채널 정책

CPF에 접속하거나 거래를 발생시키는 외부 client, 내부 업무도메인, 운영 기능, 배치, 프레임워크 처리 주체를 공통 채널 레지스트리에서 관리한다.

#### 채널 범위

최소 다음을 채널로 정의할 수 있어야 한다.

- 외부 업무 채널: `WEB`, `MOB`
- 개발·테스트 채널: `JUT`
- 운영 채널: `ADM`
- 내부 업무도메인 채널: `MBR`, `BZA`, `XYZ`, `BAT` 및 생성된 신규 주제영역
- 내부 프레임워크 채널: `PFW`
- 제휴·기관·외부 시스템 채널
- 고객사별 추가 채널

`PFW`, `MBR` 같은 채널 코드는 실행 애플리케이션 식별자와 혼동하지 않는다.

```text
originalChannelCode
= 전체 거래를 최초로 발생시킨 논리 채널
= parent/child segment 전체에서 원칙적으로 불변

currentChannelCode
= 현재 처리 segment를 발생시킨 논리 채널

callerServiceId
= 실제 호출 서비스 identity

targetServiceId
= 실제 대상 서비스 identity

selectedInstanceId
= 실제 선택된 실행 instance
```

예:

```text
MOB → MBR → PFW capability

originalChannelCode = MOB

MBR segment:
currentChannelCode = MBR
callerServiceId = mobile-bff
targetServiceId = mbr-service

PFW 내부 segment:
currentChannelCode = PFW
callerServiceId = mbr-service
targetServiceId = mbr-service
```

PFW가 scheduler, retry worker, reconciliation worker 등으로 직접 시작한 거래는 `originalChannelCode=PFW`를 사용할 수 있다. 다만 PFW 자체를 별도 executable 업무 서비스로 만드는 의미는 아니다.

#### 채널 마스터

ADM에서 모든 사용 가능 채널을 등록·조회·수정·비활성·폐기할 수 있어야 한다.

필수 속성:

- channelCode
- physicalName 또는 channelCode
- logicalName
- description
- channelType
- internal/external 구분
- ownerDomain
- testOnly 여부
- prodAllowed 여부
- authenticationType
- active status
- displayOrder
- effectiveFrom/effectiveTo
- version
- createdBy/createdAt
- updatedBy/updatedAt

권장 channelType:

- `EXTERNAL_CLIENT`
- `INTERNAL_DOMAIN`
- `INTERNAL_FRAMEWORK`
- `OPERATION`
- `BATCH`
- `TEST`
- `PARTNER`

채널 description은 단문으로 제한하지 않는다.

- DB는 최소 `VARCHAR(1000)` 또는 동등한 충분한 길이를 제공한다.
- 채널의 실제 의미, 연결 시스템, 사용 목적, 운영 유의사항을 여러 줄로 기록할 수 있어야 한다.
- 목록에서는 말줄임하고 tooltip/popover/상세 drawer에서 전체 설명을 표시한다.
- 등록 순서와 별도로 displayOrder를 관리할 수 있다.

#### 입력 정규화

채널 코드 입력은 불필요하게 타이트하게 처리하지 않는다.

- 앞뒤 공백은 `trim`
- 영문은 대문자 정규화
- 빈 값은 거부
- 중간 공백은 식별자 혼선을 막기 위해 거부
- 중복 코드 검사
- 허용 문자와 최대 길이는 명확히 정의
- description과 logicalName에는 한글, 공백, 일반 문장 허용

예:

```text
" mob " → MOB
" MBR " → MBR
"MO B"  → 오류
```

채널 코드가 trim 가능한 앞뒤 공백 때문에 등록·import·실행 오류가 발생하지 않게 한다.

#### 상태와 삭제 정책

채널 상태는 최소 다음을 지원한다.

- `ACTIVE`
- `DISABLED`
- `RETIRED`

과거 거래·로그·감사 이력이 있는 채널은 원칙적으로 물리 삭제하지 않는다.

- `DISABLED`: 일시 사용 중지
- `RETIRED`: 영구 폐기, 신규 거래 연결 불가
- 물리 삭제: 사용 이력과 연결 거래가 전혀 없는 오등록 건에만 제한

채널 마스터가 `DISABLED` 또는 `RETIRED`이면 거래별 허용 목록에 남아 있어도 모든 호출을 차단한다.

```text
채널 마스터 ACTIVE
AND 거래별 허용
AND client/service binding 정상
AND caller/target 정책 정상
= 실행 허용
```

거래별 override나 관리자 권한으로 비활성 채널을 우회 실행할 수 없게 한다.

#### client/service와 채널 binding

헤더에 `MOB`, `MBR`, `PFW`가 전달됐다는 이유만으로 해당 채널을 신뢰하지 않는다.

외부 client 또는 내부 service identity와 채널을 binding한다.

예:

```text
clientId = mobile-bff-prod
allowedChannel = MOB

serviceId = mbr-service
allowedChannel = MBR
```

모든 inbound에서 다음을 검증한다.

1. 전달 채널 정규화
2. 채널 마스터 존재·활성 여부
3. 인증된 client/service identity
4. identity와 channel binding
5. 실행 ID의 거래별 허용 최초 채널
6. 거래별 허용 caller channel/service
7. catalog target domain/service와 현재 애플리케이션 일치
8. `O/S/B` 유형별 정책
9. permission·security·audit

외부 사용자가 `X-Caller-Service`, `X-Original-Channel-Code`, 내부 identity header를 임의 전송해도 내부 채널로 인정하지 않는다.

### 거래별 허용 채널 정책

최종 운영 정책은 채널 그룹이 아니라 표준 실행 ID 단위의 거래별 정책으로 관리한다.

예:

```text
OXYZAA0001
allowedOriginalChannels = WEB, MOB, JUT
allowedCallerChannels = ADM, BZA
targetDomain = XYZ
targetServiceId = xyz-service
```

```text
SMBRAA0001
allowedOriginalChannels = WEB, MOB, JUT
allowedCallerChannels = BZA, XYZ
targetDomain = MBR
internalOnly = true
```

거래별 정책은 최소 다음을 포함한다.

- standardExecutionId
- executionType `O/S/B`
- ownerDomain
- targetDomain
- targetServiceId
- public/internal visibility
- allowedOriginalChannels
- allowedCallerChannels
- allowedCallerServices
- channelValidationEnabled
- enabled
- sourcePolicyVersion
- operationPolicyVersion
- effectiveFrom/effectiveTo
- preRegistrationStatus
- approvalStatus
- description

일반 거래 개발 시 반복 입력을 줄이기 위해 annotation 또는 source manifest에 기본 채널을 선언할 수 있다. class-level 기본값을 method가 상속할 수 있으나 DB와 ADM에서는 각 표준 실행 ID별 독립 정책으로 materialize한다.

예시 개념:

```java
@CpfOnlineTransaction(
    id = "OXYZAA0001",
    name = "회원 조회"
)
@CpfAllowedOriginalChannels({"WEB", "MOB", "JUT"})
```

class-level 기본 선언은 개발 편의 수단이며 운영상 채널 그룹을 만드는 의미가 아니다.

#### 소스 값과 운영 값의 소유권

소스 scanner 또는 policy manifest가 갱신할 수 있는 값:

- execution ID
- execution type
- owner domain
- class/method
- HTTP method/URI
- operationId
- request/response schema fingerprint
- source default allowed channels
- source description
- source version

운영자가 소유하며 재기동·재배포로 덮어쓰면 안 되는 값:

- 운영 활성·비활성
- 긴급 차단
- 운영 채널 override
- caller override
- 적용 기간
- 승인 상태
- 변경 사유
- 운영 version

우선순위:

```text
시스템 강제 차단
> 채널 마스터 비활성/폐기
> 승인된 운영자 거래별 override
> 소스의 거래별 기본 정책
> 유형별 안전 기본값
```

소스 정책 변경은 운영값을 덮어쓰지 않고 diff와 영향도를 ADM에 표시해 승인 후 반영한다.

### JUT 개발·테스트 기본 채널

`JUT`를 CPF 개발·테스트 표준 채널로 제공한다.

적용 대상:

- JUnit
- Spring integration test
- local/dev 테스트 client
- 등록된 개발용 Postman client
- ADM 거래 테스트 콘솔

테스트 지원 component는 다음 header를 기본 설정할 수 있어야 한다.

```http
X-Original-Channel-Code: JUT
X-Channel-Code: JUT
```

단순히 User-Agent가 Postman이라는 이유로 신뢰하지 않는다.

JUT 자동 보충 조건:

```text
local/dev/stg 허용 profile
AND test-default-channel property enabled
AND 등록된 test client/service identity
AND original channel 누락
```

`prod`에서는 다음을 강제한다.

- JUT 자동 설정 금지
- JUT 거래 호출 기본 차단
- 누락 채널을 JUT로 대체 금지
- ADM에서 JUT 활성화 금지
- 허용하지 않은 JUT request는 표준 오류와 감사 로그 기록

### 고성능 정책 적용

DB는 관리 정본으로 사용하고 모든 호출에서 DB 또는 ADM API를 동기 조회하지 않는다.

구조:

```text
ADM/DB 정책 정본
→ 승인된 policy version
→ Policy Compiler
→ 애플리케이션별 immutable Policy Snapshot
→ PFW Policy Engine의 메모리 검증
```

실행 메모리 구조 예:

```text
Map<StandardExecutionId, CompiledExecutionPolicy>
ActiveChannelBitmap
```

`CompiledExecutionPolicy`에는 최소 다음을 포함한다.

- 실행 유형
- target domain/service
- allowed original channel bitmap
- allowed caller channel bitmap
- allowed caller service set
- internalOnly/testOnly
- permission
- policyVersion
- checksum

매 호출은 메모리에서 수행한다.

```text
executionId Map lookup
+ active channel bitmap 검사
+ 거래별 channel bitmap 검사
+ caller/target 검사
```

정책 검증 경로에서 다음을 금지한다.

- 매 request DB 조회
- 매 request ADM API 호출
- 매 request Redis 원격 조회
- 거래×전체 채널 조합의 무조건적인 대량 갱신

DB에는 거래 정책 본체와 선택된 채널 관계만 저장한다. 채널 하나를 비활성화할 때 거래별 관계 레코드를 모두 수정하지 않고 active channel snapshot만 원자적으로 교체한다.

새 snapshot 적용:

1. version과 checksum 확인
2. 별도 메모리에서 전체 검증
3. 잘못된 거래·채널·binding이 있으면 적용 실패
4. 기존 last-known-good snapshot 유지
5. 정상일 때 atomic swap
6. 각 instance의 loadedVersion·checksum·loadedAt·entryCount 기록

ADM에서 서비스·instance별 정책 적용 version, 불일치, stale, load failure를 조회해야 한다.

### ADM 거래별 채널 선택 UI

거래 상세·등록 화면에서는 세로형 채널 표가 아니라 가로형 checkbox/chip 배열을 사용한다.

표시 형식:

```text
☑ MOB(모바일)
☑ WEB(웹)
☑ JUT(개발테스트)
☐ ADM(운영관리)
```

- `체크박스 + 물리명(논리명)` 순서
- 화면 폭에 따라 한 줄 표시 개수를 자동 조정
- 고정 개수 하드코딩 금지
- CSS grid/flex 또는 동등한 반응형 layout
- desktop에서는 여러 개를 한 줄에 배치
- tablet/mobile에서는 적절히 자동 줄바꿈
- 터치 가능한 충분한 선택 영역
- 선택 상태를 색상만으로 표시하지 않고 checkbox·border·label 함께 사용

채널 수가 많을 때 제공:

- channel code/name 검색
- 전체/외부/내부/업무도메인/운영/배치/테스트 화면 필터
- 전체 선택
- 전체 해제
- 선택된 채널만 보기
- 선택 건수
- 선택 결과 요약
- logical/physical name tooltip
- description popover
- 비활성·폐기 badge

화면 필터는 저장 정책 그룹이 아니다. 최종 저장은 거래별 개별 채널 관계다.

최소 두 영역을 분리한다.

```text
허용 최초 채널
[WEB] [MOB] [JUT] [ADM] ...

허용 서버 호출 채널
[PFW] [MBR] [BZA] [XYZ] [BAT] ...
```

비활성 채널은 기존 연결 관계를 알 수 있도록 표시하되 선택할 수 없게 하고 실제 실행은 차단한다.

### 채널 변경 영향도와 통제

채널 비활성·폐기·binding 변경 전 ADM은 다음 영향도를 보여준다.

- 연결 거래 수
- 활성 거래 수
- `O/S/B` 유형별 건수
- 영향 주제영역
- 최근 24시간·7일 호출 건수
- 현재 TPS
- 최근 오류율
- 영향 caller/target service
- 적용 대상 instance
- 운영 override 존재 여부

위험 변경에는 사유 입력, 공통 승인결재, optimistic lock, before/after audit가 필요하다.

---

### ADM O/S/B 거래 테스트 콘솔

ADM은 자동 발견된 표준 실행 거래를 직접 검증할 수 있는 통제형 거래 테스트 UI를 제공한다.

#### 환경과 기능 이중 차단

거래 테스트 기능은 메뉴 권한만으로 활성화하지 않는다.

```text
허용 profile
AND 기능 property enabled
AND 사용자/그룹 권한
AND 거래별 실행 권한
= 사용 가능
```

설정 예시 개념:

```yaml
cpf:
  adm:
    transaction-test:
      enabled: false
      allowed-profiles:
        - local
        - dev
        - stg
```

`prod`에서는 기본적으로 다음을 강제한다.

- Controller/Service Bean 미등록 또는 강제 비활성
- API endpoint 미노출
- 메뉴 미노출
- 관련 permission 부여 불가
- 기존 permission 보유자도 실행 불가
- runtime ADM 조작으로 property를 켤 수 없음
- 잘못된 prod 설정 조합은 기동 실패 또는 안전 비활성

property가 꺼져 있으면 권한 관리 화면에서도 거래 테스트 권한을 부여할 수 없게 한다.

#### 권한 세분화

- 거래 테스트 메뉴 조회
- `O` 테스트 실행
- `S` 테스트 실행
- `B` 테스트 실행
- request header 편집
- test channel 변경
- 원본 응답 조회
- 결과 다운로드
- 민감 거래 테스트
- 저장 sample 관리

UI 숨김뿐 아니라 API에서도 검사한다.

#### 거래 선택과 요청 작성

거래 검색:

- standardExecutionId
- 실행명
- `O/S/B`
- 주제영역
- 기능 코드
- URI
- operationId
- public/internal
- 활성 상태
- 배포 준비 상태

`O` 테스트:

- 자연어 URI 호출
- ID header 호출
- ID URI 호출
- HTTP method
- path/query parameter
- request header
- JSON/XML/text/fixed-length request body
- originalChannelCode 기본 `JUT`
- channelCode 기본 `JUT`

`S` 테스트:

- Local/Remote 호출 방식
- caller channel
- callerServiceId
- targetServiceId
- 내부 identity
- 허용 호출 관계
- PFW Service Call Engine 경로

외부 public endpoint를 통해 `S`를 실행하는 우회 기능을 만들지 않는다.

`B` 테스트:

- job parameter
- business date
- request reason
- rerun/restart 구분
- idempotency key
- dry-run 가능 여부
- 실행 접수·상태 조회

#### 실행 전 사전 검증

- ID 형식
- 거래 catalog 존재
- 소스 handler match
- 정책 상태
- target service match
- instance health
- channel master active
- JUT 허용
- caller binding
- permission
- request schema
- required header
- 위험 거래 여부

사전 검증 결과를 항목별 성공·경고·실패로 표시하고 실패 항목이 있으면 실행하지 않는다.

#### 결과 포맷

단순 textarea 또는 raw JSON 출력만으로 완료 처리하지 않는다.

요약:

- 성공/실패 badge
- HTTP status
- 표준 오류 코드
- 외부 오류 메시지
- standardExecutionId
- transactionGlobalId
- segmentId
- 시작·종료 시각
- duration
- original/current channel
- caller/target service
- selectedInstanceId
- retry/failover/circuit
- policyVersion

상세:

- response headers 표
- formatted body
- raw body
- JSON tree
- XML tree
- text/fixed-length 전문 해석
- copy
- 허용된 다운로드
- masking 상태

바로가기:

- 거래 로그
- segment timeline
- selected instance
- 오류 상세
- 감사 로그
- 정책 상세

모든 테스트 실행은 실행자·사유·입력 hash·결과·transactionGlobalId를 감사 기록한다.

---

### ADM 로그 원본·포맷 조회

ADM 거래 로그 화면은 운영자가 읽기 좋은 포맷과 원본 구조 보기를 모두 제공한다.

권장 탭:

- 요약
- 표준/확장 헤더
- 요청 전문
- 응답 전문
- 오류
- segment timeline
- 원본 raw

포맷 기능:

- JSON pretty/tree/collapse
- XML pretty/tree
- fixed-length layout 기반 field 해석
- 일반 text line number
- keyword search와 highlight
- request/response header 표
- 긴 전문 lazy loading
- 전체보기
- copy
- 허용된 다운로드
- 원본/포맷 전환
- transactionGlobalId·segment·실행 ID 링크

`원본 보기`는 원래 순서·개행·구조를 보존하는 의미이며 보안을 우회하는 의미가 아니다.

기본 원본 보기:

- masking 적용
- 구조 보존
- 원본 hash 표시 가능

고권한 보안 원문 보기:

- 별도 permission
- 사유 입력
- 필요 시 재인증
- 조회 감사
- 다운로드 감사
- 화면 watermark 또는 운영자 표시
- 노출 시간 제한
- 개인정보·secret 정책 준수

원본 저장값을 포맷 결과로 덮어쓰지 않는다.

---

### ADM/BAM 통계와 거래·채널 관제

ADM/BAM은 실제 거래 로그·metric에서 다음 통계를 제공한다.

#### 성공·오류 통계

- 전체 거래 건수
- 성공 건수·성공률
- 오류 건수·오류율
- business/system/security/routing 오류 구분
- errorCode별 건수
- HTTP status별 건수
- timeout·retry·failover·circuit 발생
- 평균·P50·P90·P95·P99 latency
- TPS
- unknown/reconciliation 건수

#### 거래별 통계

- standardExecutionId
- 거래명
- O/S/B
- owner/target domain
- 처리 건수
- 성공/오류
- latency
- channel distribution
- selected instance distribution
- 최근 오류
- 추세

#### 채널별 통계

- originalChannelCode
- currentChannelCode
- logical/physical name
- 요청 건수
- 성공/오류
- 거래 수
- TPS
- latency
- target service
- top 거래
- 오류 code
- 비활성·폐기 이후 차단 건수

#### 거래×채널 교차 통계

- 거래별 channel 분포
- 채널별 거래 분포
- 허용되지 않은 channel 접근 시도
- inactive channel 접근 시도
- caller-channel binding 실패
- target mismatch
- JUT 사용 현황
- `S` 외부 접근 차단

필터:

- 기간
- 시간 단위
- 실행 ID
- O/S/B
- domain/service/instance
- original/current channel
- 성공/오류
- errorCode
- transactionGlobalId

통계 카드·차트·표의 합계가 동일 조건에서 일치해야 한다. 상세 drill-down으로 실제 거래 로그를 조회할 수 있어야 한다.

---

### 거래 정책 파일 Export·Import와 환경 승격

개발 환경에서 자동 발견·테스트가 완료된 거래 정책을 환경 독립 파일로 Export하고 TEST/STG/PROD ADM에 파일 업로드하여 승인결재 후 사전 등록·적용할 수 있어야 한다.

파일 업로드를 기본 방식으로 사용한다.

이유:

- 원본 파일 증적 보존
- checksum·signature 검증
- 승인 대상 고정
- diff 재현
- rollback 근거
- 환경 간 동일 package 승격

#### Export package

환경 독립 필드만 포함한다.

- schemaVersion
- manifestVersion
- sourceCommit
- generatedAt
- generatedBy
- standardExecutionId
- executionType
- executionName/description
- ownerDomain/featureCode
- visibility/internalOnly
- HTTP method/URI/operationId
- request/response schema fingerprint
- allowed original channels
- allowed caller channels
- allowed caller services
- target domain/service logical ID
- permission code
- audit/masking/idempotency policy code
- source policy version
- transaction count
- content checksum

제외:

- DB PK
- 실제 URL/IP/port
- instanceId
- password/token/secret
- 인증서 원문
- DB schema credential
- 환경별 clientId
- 환경별 활성 상태
- 긴급 차단
- 승인자
- 운영 override

파일 형식은 JSON을 기본으로 하되 schema를 명시하고 UTF-8, deterministic ordering, checksum 계산이 가능하게 한다. 필요 시 ZIP package에 manifest, schema, 서명, 설명 파일을 포함할 수 있다.

#### Import 저장

업로드한 원본 파일을 보존한다.

- 원본 파일명
- 저장 파일 ID
- content type
- size
- checksum
- uploader
- uploadedAt
- source environment
- manifestVersion
- storage location
- malware/secret scan result
- validation result
- approval request ID
- final apply status

파일은 attachment/file storage 표준을 재사용하고 다운로드 감사와 retention 정책을 적용한다.

#### Import 처리

1. 파일 업로드
2. 확장자·size·content type 검증
3. malware·secret scan
4. schemaVersion 검증
5. checksum·signature 검증
6. duplicate manifest 검증
7. rollback version 검증
8. channel/permission/policy code 존재 검증
9. 현재 환경과 dry-run diff
10. 신규·변경·동일·삭제·충돌 분류
11. 영향도 표시
12. 승인결재 요청
13. 승인 후 사전 등록 또는 적용
14. 적용 결과와 evidence 기록
15. rollback package 보관

업로드 즉시 실행 정책에 반영하지 않는다.

#### 사전 등록

소스 배포 전에 거래 정책을 등록할 수 있어야 한다.

상태 개념:

- `PRE_REGISTERED`
- `SOURCE_NOT_DEPLOYED`
- `DISCOVERED`
- `MATCHED`
- `READY`
- `ACTIVE`
- `MISMATCH`
- `RETIRED`

사전 등록된 거래는 조회·검토 가능하지만 실제 handler가 없거나 metadata가 일치하지 않으면 실행할 수 없다.

소스 배포 후 scanner가 다음을 대조한다.

- execution ID
- O/S/B
- owner domain
- feature code
- HTTP method/URI
- operationId
- schema fingerprint
- internal/public
- policy version

일치하면 `MATCHED/READY`, 불일치하면 `MISMATCH`로 처리하고 자동 활성하지 않는다.

#### Diff 화면

- 기존값
- 업로드값
- 운영 override
- 최종 예상값
- 적용 여부
- 충돌 사유
- 영향 거래
- 영향 채널
- 영향 caller/target
- source deployed 여부

대량 package는 전체 요약과 거래별 상세를 모두 제공한다.

---

### ADM 전 기능 공통 변경관리·승인결재 원칙

승인결재는 배치, 거래 정책 import 또는 특정 메뉴에만 붙이는 부가 기능이 아니다. ADM에서 운영 상태·정책·설정·데이터·실행 결과에 영향을 주는 모든 변경과 위험 실행은 공통 변경관리·승인결재 표준을 적용한다.

사용자가 개별 메뉴를 하나씩 지적해야만 승인결재를 추가하지 않는다. Codex는 최신 ADM/BAM 메뉴, Controller, Service, API, DB 변경 경로를 전수 inventory하여 변경·실행 기능을 다음 기준으로 분류하고, 승인 대상 누락을 찾아 스스로 보완한다.

#### 변경·실행 기능 분류

모든 ADM action은 최소 다음 중 하나로 명시적으로 분류한다.

- `READ_ONLY`: 조회·검색·상세·통계
- `LOW_RISK_UPDATE`: 설명·표시 순서 등 제한적 변경
- `CONTROLLED_UPDATE`: 운영 정책·설정·binding·활성 상태 변경
- `HIGH_RISK_UPDATE`: 보안·권한·채널 차단·route·서비스 제어·대량 설정 변경
- `CONTROLLED_EXECUTION`: rerun, replay, reconciliation, compensation, file resend 등 운영 실행
- `DESTRUCTIVE_ACTION`: 삭제·폐기·초기화·대량 취소
- `EMERGENCY_ACTION`: 긴급 차단·강제 전환·장애 대응

분류 결과와 기본 승인 정책은 DB/설정/스펙에 정본화하고 ADM에서 확인할 수 있어야 한다. action 분류 없이 직접 변경되는 운영 API를 남기지 않는다.

#### 적용 대상

최소 다음 ADM/BAM 기능을 전수 확인한다.

- 채널 등록·수정·비활성·폐기
- client/service-channel binding
- 거래별 허용 채널과 caller/target 정책
- 거래 catalog 운영 활성·비활성
- 거래 정책 파일 import·사전 등록·활성
- 서비스·인스턴스 등록 정책
- active/standby, draining, maintenance
- instance weight·priority·capacity
- Gateway route·snapshot publish/apply/rollback
- timeout·retry·circuit·failover 정책
- scheduler 등록·주기·활성 상태
- batch job 등록·수정·중지·폐기
- batch parameter·business date·concurrency
- batch instance profile과 worker 설정
- center-cut 설정
- rerun·restart·재수행
- broker·DLQ replay
- outbox/inbox 재처리
- unknown result reconciliation
- saga compensation
- file resend·재수신·재처리
- 오류 code/message catalog
- masking·audit·download 정책
- 사용자·그룹·역할·메뉴·permission
- approval policy 자체 변경
- 운영 파일 업로드·적용
- 운영 데이터 보정 또는 상태 변경
- 고객사·환경별 운영 configuration
- 기타 source와 API 조사에서 발견되는 운영 영향 action

위 목록에 없다는 이유로 승인 통제를 생략하지 않는다.

#### 직접 저장 금지

승인 대상 action은 화면이나 API에서 운영 정본 테이블을 즉시 갱신하지 않는다.

```text
현재 적용값
변경 요청값
승인된 적용 예정값
실제 적용값
```

을 분리한다.

기본 흐름:

```text
편집
→ validation
→ 변경 요청 snapshot 생성
→ 승인 정책 판단
→ 자동승인 또는 승인 대기
→ 적용 시점 도달
→ version 재확인
→ 적용
→ runtime 반영 확인
→ APPLIED
```

승인 대상인데 Controller 또는 Service가 정본 repository를 직접 update하는 경로가 있으면 완료가 아니다.

#### 권한 모델

최소 권한을 분리한다.

- 조회 권한
- 수정 요청 작성 권한
- 수정 요청 취소 권한
- 승인 권한
- 자동승인 사용 권한
- 적용 권한
- 긴급 적용 권한
- rollback 권한
- 승인 정책 관리 권한
- 첨부 원본 조회 권한
- 감사 조회 권한

수정 권한이 있다고 승인 권한이 자동 부여되지 않는다. 승인 권한이 있다고 모든 기능에서 자동승인이 허용되지 않는다.

#### 저장과 동시에 승인

승인 권한과 해당 requestType의 자동승인 사용 권한이 있는 사용자에게만 다음 UI를 제공한다.

```text
☑ 저장과 동시에 승인
```

이 체크박스는 다음 조건을 모두 만족할 때만 노출·활성화한다.

```text
현재 profile에서 자동승인 허용
AND 해당 기능·action이 자동승인 허용
AND 사용자가 수정·승인·자동승인 권한 보유
AND self-approval 정책이 허용
AND 위험도 제한 충족
```

선택 시에도 요청 이력을 생략하지 않는다.

```text
REQUESTED
→ AUTO_APPROVED
→ SCHEDULED 또는 APPLYING
→ APPLIED
```

요청자, 승인자, 승인 방식, before/after, 사유, 적용 시점, 결과를 모두 기록한다.

자동승인 권한이 없는 사용자는 체크박스를 볼 수 없거나 disabled 상태로 이유를 안내한다. 저장하면 통합 승인결재 센터로 요청이 상신된다.

#### 환경별 기본 정책

- `local`: 권한과 property가 허용되면 자동승인 가능
- `dev`: 권한과 property가 허용되면 자동승인 가능
- `stg`: 기본은 수동 승인, 명시적 정책으로 제한적 자동승인 가능
- `prod`: 일반 자동승인 기본 금지, requestType별 수동 승인 필수

prod에서 설정 오류로 자동승인이 켜지지 않도록 fail-safe를 구현한다.

긴급 기능은 무승인 우회가 아니라 별도 `EMERGENCY_ACTION` 정책을 사용한다.

- 고권한
- 긴급 사유
- 추가 인증 가능
- 즉시 적용
- 사후 승인 또는 사후 검토
- 강한 audit
- 경보
- 자동 만료 또는 복구 계획

#### 적용 시점

모든 변경 요청은 기능 성격에 맞게 다음 적용 방식을 지원할 수 있어야 한다.

- `IMMEDIATE`: 승인 후 즉시
- `NEXT_EXECUTION`: 다음 실행부터
- `SCHEDULED_AT`: 지정 일시부터
- `AFTER_CURRENT_COMPLETES`: 현재 실행 완료 후
- `ON_RESTART`: 재기동 후
- `ON_NEW_INSTANCE`: 신규 인스턴스부터
- `MANUAL_APPLY`: 승인 후 관리자가 별도 적용

적용 시점은 승인자가 확인할 수 있어야 하며 승인 후 임의로 바꾸지 못한다. 변경하려면 재승인한다.

#### 적용 예약과 effective dating

설정은 version과 적용 기간을 가진다.

- effectiveFrom
- effectiveTo
- version
- approvalRequestId
- status

예:

```text
현재 parameterVersion = 3
승인된 신규 parameterVersion = 4
effectiveFrom = 2026-07-17T00:00:00+09:00
```

지정 시각 전 실행은 version 3, 이후 신규 실행은 version 4를 사용한다.

#### 실행 snapshot

배치·scheduler·worker·center-cut·broker·file transfer·reconciliation·saga·운영 재처리는 시작 시 승인된 설정 snapshot을 고정한다.

최소 기록:

- standardExecutionId
- executionId/jobExecutionId
- definitionVersion
- parameterVersion
- policyVersion
- instanceProfileVersion
- approvalRequestId
- effectiveAt
- transactionGlobalId
- snapshotChecksum

실행 중인 작업의 설정을 중간에 바꾸지 않는다. 승인된 변경은 다음 실행, 신규 instance 또는 명시적 재수행부터 적용한다.

#### 생성된 인스턴스와 기존 인스턴스

신규 인스턴스가 승인 대기 상태라면 관리자가 승인한 후 정책을 적용하고 readiness 확인 후 활성화한다.

```text
DISCOVERED
→ PENDING_APPROVAL
→ APPROVED
→ APPLYING
→ READY
→ ACTIVE
```

이미 실행 중인 인스턴스는 적용 방식에 따라 다음처럼 처리한다.

- 즉시 적용 가능한 안전 항목: 승인 후 runtime refresh
- 재기동 필요: `ON_RESTART`
- 신규 실행부터: `NEXT_EXECUTION`
- 현재 실행 보호 필요: `AFTER_CURRENT_COMPLETES`
- 신규 instance만: `ON_NEW_INSTANCE`

#### 관리자 직접 수정

관리자가 직접 수정하더라도 승인·적용 이력을 생략하지 않는다.

자동승인을 선택한 경우 화면 결과를 단계별로 표시한다.

```text
변경 요청 저장 완료
자동승인 완료
적용 예약 완료
실제 적용 완료 또는 대기
적용 version
적용 예정 시각
```

단순히 `수정 완료`로 표시하지 않는다.

#### 승인자가 판단할 정보

승인 화면은 최소 다음을 제공한다.

- 기능·메뉴
- action
- 대상 ID와 이름
- profile
- 변경 전·후 diff
- 변경 사유
- 요청자
- 영향 서비스·인스턴스·거래·채널
- 최근 호출량·성공률·오류율
- 현재 실행 중 여부
- 다음 실행 예정 시각
- 적용 방식·적용 예정 시각
- validation 결과
- 정책 충돌
- rollback 가능 여부
- 첨부 파일
- 과거 변경 이력
- 관련 transactionGlobalId
- 예상 최종 상태

#### 통합 승인결재 센터

ADM에 기능별로 흩어진 결재함이 아니라 통합 메뉴를 제공한다.

```text
승인결재
 ├─ 나의 요청
 ├─ 승인 대기
 ├─ 처리 중
 ├─ 예약 적용
 ├─ 적용 완료
 ├─ 적용 실패
 ├─ 반려·취소·만료
 ├─ 긴급 변경
 └─ 승인 정책 관리
```

검색·필터:

- requestType
- 기능·메뉴
- action
- 대상 ID
- profile
- 위험도
- 요청자
- 승인자
- 승인 상태
- 적용 상태
- 요청 기간
- 적용 예정일
- 긴급 여부

각 승인 요청에서 원래 메뉴 상세로 이동하고, 원래 메뉴에서도 승인 요청과 적용 상태로 이동할 수 있어야 한다.

#### 메뉴별 pending change 표시

승인 대상 메뉴의 목록과 상세에는 다음을 표시한다.

- 현재 적용 version
- 승인 대기 변경 존재
- 승인 완료·적용 대기
- 적용 예정 시각
- 적용 실패
- 충돌
- 마지막 승인자
- 마지막 적용 시각

동일 대상에 승인 대기 요청이 있을 때 중복 수정 정책을 정의한다.

- 기존 요청 수정
- 기존 요청 취소 후 재요청
- 후속 version으로 직렬화
- 충돌 차단

#### 승인 정책 관리

공통 승인 정책에는 최소 다음을 정의한다.

- requestType
- targetType
- action
- profile
- riskLevel
- approvalRequired
- autoApprovalAllowed
- autoApprovalRoles
- selfApprovalAllowed
- requiredApprovalSteps
- sequential/parallel/quorum
- effectiveModeAllowed
- emergencyAllowed
- attachmentRequired
- reasonRequired
- expiry
- applyHandler
- rollbackHandler
- enabled
- version

승인 정책 자체의 변경도 승인 대상이다.

#### 첨부와 증적

파일 import뿐 아니라 필요 기능에서 승인 요청에 증적 파일을 첨부할 수 있어야 한다.

- 원본 파일 보관
- checksum
- uploader
- content type
- size
- scan 결과
- 다운로드 감사
- retention
- approvalRequestId 연결

#### API·계층 규칙

운영 변경 API는 공통 approval orchestration을 통과한다.

권장 계층:

```text
ADM Controller
→ Change Request Application Service
→ Approval Policy Port
→ Approval Workflow Engine
→ Target Apply Port
→ 대상 capability adapter
```

금지:

- 화면별 approval 로직 복제
- 메뉴별 임시 approval table
- 승인 전 운영 정본 직접 update
- UI에서만 승인 제어
- Spring Event만으로 핵심 상태 전이 처리
- 승인됐다는 이유로 적용 성공 처리

#### 적용 handler

각 기능은 공통 승인 엔진에 다음 contract를 제공한다.

- validateRequest
- buildBeforeSnapshot
- buildAfterSnapshot
- calculateImpact
- apply
- verifyApplied
- rollback
- formatSummary

공통 엔진은 기능별 업무 로직을 직접 소유하지 않는다.

#### 완료 gate

Codex는 ADM의 모든 mutation endpoint와 action button을 inventory한다.

최종 보고에 다음 표를 포함한다.

- 메뉴
- API
- action
- riskLevel
- approvalRequired
- autoApproval 조건
- 적용 방식
- apply handler
- audit
- test/evidence
- 상태

qualityGate는 최소 다음을 탐지한다.

- 승인 대상 mutation endpoint가 approval orchestration을 우회
- prod 자동승인 허용
- 권한 없는 자동승인
- self-approval 정책 위반
- before/after snapshot 누락
- 적용 시점 누락
- 승인과 적용 상태 혼합
- 실행 snapshot version 누락
- 운영 정본 직접 update
- 승인 대상 메뉴의 audit 누락
- 없는 approval evidence로 완료 기록

이 전수 inventory와 runtime evidence 없이 ADM 공통 승인결재를 완료로 기록하지 않는다.

---

### PFW 공통 승인결재 엔진

거래 정책 import뿐 아니라 ADM/BAM 전체의 운영 영향 변경·위험 실행에서 재사용하는 공통 승인결재 capability로 설계한다. 위 `ADM 전 기능 공통 변경관리·승인결재 원칙`의 전수 적용 기준을 따른다.

특정 화면 내부의 임시 approval table이나 Spring Event만으로 구현하지 않는다.

#### 승인결재 대상 추상화

- approvalRequestId
- requestType
- targetType
- targetId
- targetVersion
- requestedAction
- requestPayload snapshot
- before snapshot
- after/diff snapshot
- attachment/file IDs
- requester
- requestedAt
- reason
- status
- currentStep
- policyId
- appliedAt
- result
- transactionGlobalId

#### 상태

최소 다음을 지원한다.

- `DRAFT`
- `REQUESTED`
- `IN_REVIEW`
- `APPROVED`
- `REJECTED`
- `CANCELLED`
- `EXPIRED`
- `APPLYING`
- `APPLIED`
- `APPLY_FAILED`
- `ROLLED_BACK`

승인과 실제 적용을 분리한다.

```text
APPROVED
→ 적용 가능 상태

APPLIED
→ 대상 시스템에 실제 반영 완료
```

적용 실패를 승인 성공으로 숨기지 않는다.

#### 결재선

- 단일 승인
- 순차 다단계 승인
- 병렬 승인
- N명 중 M명 승인
- 역할 기반 승인자
- 본인 승인 금지
- 요청자·승인자 분리
- 대리 승인
- 만료·재요청
- 반려 후 수정 재상신

운영 profile·대상 중요도·requestType별 결재 정책을 정의한다.

예:

```text
DEV 일반 정책 import
→ 1인 승인 또는 자동 승인 가능

STG 정책 import
→ 개발 책임자 + 운영 담당자

PROD 정책 import
→ 요청자와 분리된 운영 승인자 + 보안/책임자
```

실제 조직에 맞게 설정 가능하게 하되 prod에서 무승인 적용은 기본 금지한다.

#### 적용 안전성

- optimistic locking
- 대상 version 재확인
- 승인 이후 대상이 변경되면 stale 승인 처리
- 동일 package 중복 적용 방지
- idempotency
- partial apply 방지
- atomic 또는 명시적 transaction boundary
- 적용 실패 상세
- 재시도
- rollback
- before/after audit
- notification hook
- outbox 사용 가능

핵심 상태 전이는 DB 정본과 상태 machine으로 관리하고 Spring Event를 중심 기술로 사용하지 않는다.

#### ADM 승인결재 UI

- 요청함
- 처리함
- 완료함
- 반려/취소
- requestType·상태·요청자·기간 검색
- attachment 원본 조회
- diff
- 영향도
- 승인 의견
- 반려 사유
- 결재선과 현재 단계
- 적용 상태
- rollback
- audit timeline

권한과 화면 메뉴는 공통 승인결재 capability에 연결한다.

---

### ADM/BAM 전 화면 반응형 표준

ADM과 BAM을 포함한 모든 운영 UI는 반응형이 필수다.

지원 viewport:

- 일반 desktop
- 작은 desktop/notebook
- tablet landscape/portrait
- mobile

#### layout

- header/left navigation은 폭에 따라 compact·collapse·drawer로 전환
- 검색 조건은 넓은 화면에서 가로, 좁은 화면에서 자동 줄바꿈
- summary card는 화면 폭에 따라 column 수 자동 조정
- 표는 핵심 컬럼 우선, column visibility, 가로 스크롤, 상세 drawer 지원
- 좁은 화면에서는 필요한 경우 row summary/card 방식
- modal은 viewport를 넘지 않고 mobile에서는 full-screen 또는 bottom sheet 가능
- chart legend·tooltip·축 label이 겹치지 않게 조정
- 버튼과 checkbox는 touch target 확보
- 고정 px width와 절대좌표 기반 핵심 layout 금지

#### 채널 선택 반응형

채널 checkbox/chip 수를 특정 개수로 고정하지 않는다.

- available width에 따라 자동 column 수 계산
- channel name 길이에 따라 적절한 최소 폭
- 긴 이름 말줄임과 tooltip
- 줄바꿈 시 정렬 유지
- desktop에서 화면 낭비 없이 다수 표시
- mobile에서 한 항목이 지나치게 좁아지지 않게 1~2열 또는 자동 적응
- 선택/비선택/disabled 상태 명확화

#### 검증

최소 다음 viewport의 browser screenshot과 기능 evidence를 남긴다.

- 1440×900 또는 동등 desktop
- 1024×768 또는 동등 tablet/작은 desktop
- 768×1024 tablet portrait
- 390×844 또는 동등 mobile

주요 화면:

- 대시보드
- 거래 catalog
- 거래별 채널 선택
- 채널 관리
- 거래 테스트
- 로그 원본/포맷
- 통계
- 정책 import/diff
- 승인결재

단순히 viewport에서 페이지가 열린다는 사실이 아니라 검색, 선택, modal, drawer, table, upload, approval action이 실제 동작해야 한다.

---

### 채널·거래 테스트·정책 승격 필수 SQL/API/OpenAPI

신규 기능은 다음 전체 묶음으로 구현한다.

- PFW port·policy engine·snapshot compiler
- ADM application/service/repository/controller
- MariaDB table/index/constraint
- split SQL
- Flyway
- `00_all_install.sql`
- `00_all_install_and_smoke.sql`
- `99_smoke_check.sql`
- REST API
- Swagger/OpenAPI
- ADM/BAM UI
- 권한·메뉴 seed
- audit
- tests
- XYZ/BAT EDU 또는 적절한 기능별 EDU
- sample coverage
- 기능 matrix
- evidence/report/gap/index

API 예시 범위:

- channel CRUD/status/history
- client/service-channel binding
- execution channel policy 조회·수정
- policy snapshot version/status
- export package 생성·다운로드
- import upload·validate·dry-run·diff
- approval request·approve·reject·cancel
- pre-registration·match·activate
- transaction test preflight·execute·result
- log raw/formatted 조회
- transaction/channel statistics

모든 API는 `@Tag`, `@Operation`, 고유 `operationId`, request/response schema, 권한, 표준 header, 표준 오류 response를 제공한다.

---

### 채널·거래 테스트·승격 필수 테스트와 evidence

#### 채널

- trim·uppercase 정규화
- 중복 코드
- description 긴 값
- active/disabled/retired
- 비활성 채널 전 거래 차단
- 물리 삭제 제한
- client-channel binding 성공·실패
- service-channel binding 성공·실패
- 위조 header 차단
- original channel 불변
- current channel segment 전환
- target service mismatch
- JUT profile/property 정책

#### 성능

- 대량 채널·거래 policy compile
- 메모리 snapshot lookup
- request당 DB/Redis/ADM 호출 없음
- atomic snapshot swap
- last-known-good
- multi-instance version 일치
- stale snapshot 관제
- load failure rollback

#### 거래 테스트

- property off 시 bean/API/menu/permission 비활성
- prod 강제 비활성
- O URL/ID header/ID URI
- S Local/Remote 내부 호출
- S 외부 우회 차단
- B 접수·상태
- JUT 자동 header
- preflight 실패 차단
- formatted/raw 결과
- timeline·log 연결
- audit

#### Export/Import

- 환경 독립 필드만 포함
- 민감정보 미포함
- deterministic checksum
- 원본 파일 저장
- schema/signature/checksum
- duplicate/replay
- dry-run diff
- pre-registration
- source matching
- mismatch
- approval
- apply
- apply failure
- rollback
- 운영 override 보존

#### 승인결재

- 단일·다단계·병렬
- 본인 승인 금지
- stale target version
- 중복 승인
- expiry
- reject/cancel
- approved와 applied 분리
- optimistic lock
- idempotent apply
- audit
- 다른 운영 기능 재사용 contract test

#### UI/통계/로그

- desktop/tablet/mobile
- 가로형 channel selection
- 많은 채널 자동 줄바꿈
- inactive channel 표시
- 로그 JSON/XML/text/fixed-length
- raw/format 전환
- masking
- 원문 권한·감사
- 성공/오류·거래별·채널별·교차 통계
- chart와 table 합계 정합성
- drill-down

실제 source·SQL·API·browser·runtime evidence가 없으면 해당 기능을 완료로 기록하지 않는다.

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

## 1.10 CPF 전체 상용 프레임워크 횡단 기능 전수 검토와 보강

이번 작업은 ADM·BZA 화면 또는 이번 대화에서 직접 언급한 기능만 구현하는 작업이 아니다. `CPF_FINAL_TARGET_REQUIREMENTS.md`를 정본으로 PFW, CMN, ACC, MBR, EXS, BAT, BZA, ADM, XYZ, 생성 주제영역, Gateway, broker, DB, file, batch, scheduler, worker, UI, 설치·배포·운영·감사·문서 전체를 전수 검토한다.

사용자가 기능을 하나씩 질문한 뒤에야 요구사항을 추가하지 않는다. 상용 Core Business Platform Framework로서 일반적으로 필요하고 CPF 목표와 일치하는 기능은 Codex가 먼저 inventory하고 구현·보강·착수·후순위로 분류한다.

다음 기준을 지킨다.

- 이미 구현됐으면 source·test·SQL·API·UI·evidence를 확인하고 부족한 계층만 보강한다.
- 일부 구현이면 `부분 구현`으로 남기고 이번 범위에서 실질적인 진도를 낸다.
- 미구현이면 목표에서 삭제하지 않고 구현 또는 명시적 착수 범위로 둔다.
- 대규모 기능을 문서만 작성해 완료 처리하지 않는다.
- 특정 제품을 과도하게 고정하지 않고 port/engine/reference adapter 구조를 우선한다.
- 고객사·환경·배포 topology가 달라도 적용 가능한 중립적 표준을 만든다.
- 모든 신규 공통 capability는 PFW ownership, 업무 확장은 CMN/업무 주제영역 ownership을 지킨다.
- 기능별 source + test + EDU + OpenAPI + SQL + ADM/BAM + evidence 묶음을 적용한다.

### 1.10.1 전체 capability inventory

최소 다음 영역을 현재 source와 목표 문서에서 전수 inventory한다.

1. 실행·거래
2. 서비스 호출·라우팅
3. registry·health·multi-instance
4. transactionGlobalId·segment·timeline
5. 표준/확장 header와 channel
6. security·permission·audit·masking
7. idempotency·outbox·inbox
8. broker·DLQ·replay
9. saga·compensation
10. unknown result·reconciliation
11. batch·scheduler·worker·center-cut
12. file transfer·attachment·archive·compression
13. SQL·Flyway·설치·migration
14. ADM·BAM·BZA 운영 UI
15. error/message
16. configuration·profile·secret
17. observability·metric·trace·alert
18. cache·resource·concurrency
19. API·event·schema compatibility
20. backup·restore·DR
21. privacy·retention·purge
22. deployment·upgrade·rollback
23. developer SDK·generator·EDU
24. quality·security supply chain
25. documentation·runbook·supportability

inventory 결과를 기능별 상태, owner, source, runtime, SQL, OpenAPI, EDU, evidence, gap으로 기록한다.

### 1.10.2 중앙 설정 관리와 설정 수명주기

CPF 전체 설정은 단순 `application.yml` 분산 값이나 ADM DB row로 끝내지 않는다.

최소 다음 유형을 구분한다.

- build-time 설정
- startup-time 설정
- runtime 변경 가능 설정
- 환경별 binding
- 고객사별 설정
- 기능별 policy
- secret reference
- 긴급 override

필수 기능:

- 설정 key catalog
- type·default·required·range·allowed value
- owner module
- profile 허용 범위
- restart 필요 여부
- hot reload 가능 여부
- 민감 여부
- version
- effectiveFrom/effectiveTo
- approval policy
- before/after
- rollback
- instance별 loaded version
- configuration drift 탐지

ADM에서 현재값, desired value, 승인값, 실제 instance 적용값을 구분한다.

설정 변경은 앞서 정의한 공통 승인결재·예약 적용·version·audit 체계를 사용한다.

설정 값이 잘못됐을 때:

- 기동 전 validation
- unsafe default 방지
- 부분 instance 적용 차단
- last-known-good 유지
- checksum
- rollback
- stale instance 경고

를 지원한다.

### 1.10.3 Secret·credential·key 수명주기

PFW의 credential/secret provider port를 실제 운영 가능한 수준으로 보강한다.

대상:

- DB credential
- broker credential
- SFTP/FTP/SSH credential
- service token
- client secret
- API key
- encryption key
- signing key
- certificate
- truststore/keystore reference

필수 원칙:

- source·SQL·evidence·로그에 원문 기록 금지
- 환경변수 또는 secret provider reference 사용
- secret value와 metadata 분리
- 만료일
- rotation 예정일
- version
- 활성·이전 key 동시 허용 기간
- revocation
- certificate expiry 관제
- 적용 instance 확인
- secret 조회 audit
- 민감값 masking

key/credential rotation은 무중단 이중 version 전환이 가능해야 한다.

```text
신규 key 등록
→ 승인
→ dual-read/dual-verify
→ 신규 key primary 전환
→ 이전 key grace period
→ 폐기
```

실 provider가 없으면 reference adapter와 deterministic test를 제공하되 실 연동은 `미검증`으로 기록한다.

### 1.10.4 인증·세션·서비스 신원 보강

사용자 인증과 서비스 간 인증을 분리한다.

사용자 영역:

- password policy
- 잠금
- 실패 횟수
- session/token expiry
- refresh/revoke
- 동시 세션 정책
- 강제 로그아웃
- 비밀번호 변경 이력
- 휴면·퇴직·비활성 계정
- 관리자 계정 강화
- 필요 시 MFA 확장 port

서비스 영역:

- mTLS 또는 signed service token
- short-lived identity
- audience/issuer
- clock skew
- nonce/replay 방지
- serviceId·instanceId binding
- certificate/token rotation
- internal header 재생성
- trust domain

인증 성공만으로 권한을 부여하지 않고 menu/button/API/transaction/data scope 권한을 별도로 검증한다.

### 1.10.5 데이터 권한과 범위 통제

역할 기반 권한 외에 필요한 경우 다음 확장점을 제공한다.

- 조직 범위
- 업무도메인 범위
- 고객사 범위
- 채널 범위
- 거래 범위
- 데이터 소유자 범위
- 조회/등록/수정/삭제/승인/다운로드/원문조회 구분
- row-level filter port
- field-level masking
- purpose-based access reason

권한 시뮬레이션과 실제 API 결과가 일치해야 한다.

### 1.10.6 감사·부인방지·변조 탐지

감사는 단순 CRUD log가 아니다.

최소 감사 대상:

- 로그인·로그아웃·실패
- 권한·역할·메뉴 변경
- 승인·반려·자동승인
- 정책·설정 변경
- 거래 테스트
- 원문 조회
- 다운로드
- 재처리·재전송
- 긴급 조작
- secret metadata 변경
- 인스턴스·route 제어
- 데이터 보정

감사 필드:

- actor
- delegated actor
- action
- target
- before/after
- reason
- approvalRequestId
- transactionGlobalId
- source IP/client/service
- timestamp
- result
- error code
- checksum 또는 hash chain 확장점

감사 로그는 일반 업무 수정 API로 변경·삭제할 수 없고 retention·archive 정책을 가진다.

### 1.10.7 Observability 표준

로그만으로 완료 처리하지 않고 metric, trace, event, health를 연결한다.

공통 metric:

- request count
- success/error
- latency
- TPS
- concurrent execution
- queue depth
- thread/connection pool
- DB pool
- retry
- timeout
- circuit state
- failover
- broker lag
- outbox backlog
- DLQ
- scheduler delay
- batch duration
- file transfer throughput
- reconciliation backlog
- policy snapshot version

trace/segment:

- standardExecutionId
- transactionGlobalId
- segmentId
- parentSegmentId
- original/current channel
- caller/target
- selectedInstanceId
- retry attempt
- broker message ID
- batch/job execution ID

OpenTelemetry 또는 동등 표준으로 확장 가능한 port를 제공한다. 특정 collector가 없을 때도 core가 깨지지 않게 한다.

### 1.10.8 SLI·SLO·알림·운영 이벤트

CPF는 상태를 보여주는 것에 그치지 않고 운영자가 대응할 수 있게 한다.

지원 대상:

- availability
- error rate
- latency
- timeout
- circuit open
- failover
- instance down/stale
- queue backlog
- DLQ 증가
- batch 지연·실패
- scheduler missed
- file transfer 실패
- reconciliation backlog
- certificate expiry
- disk usage
- DB pool exhaustion
- policy/config drift
- approval/apply failure

알림 기능:

- severity
- deduplication
- suppression
- maintenance window
- escalation
- acknowledgement
- resolved
- notification channel port
- webhook/email/SMS 등 reference adapter 가능
- runbook link
- 관련 거래·instance drill-down

ADM/BAM에서 active alert와 이력을 조회한다.

### 1.10.9 자원 격리와 과부하 보호

Service Call Engine과 실행 주체에 다음을 검토·보강한다.

- timeout
- retry budget
- circuit breaker
- bulkhead
- concurrency limit
- queue capacity
- rate limit
- quota
- backpressure
- load shedding
- deadline propagation
- cancellation
- thread pool isolation
- connection pool limit
- per-service/per-transaction 정책
- priority execution
- graceful degradation

retry storm과 cascading failure를 방지한다.

- exponential backoff
- jitter
- retryable classification
- max elapsed time
- caller deadline 초과 시 retry 금지
- idempotency 확인
- shared retry budget

ADM에서 현재 제한값, 사용량, 거부 건수, 변경 이력을 확인한다.

### 1.10.10 Cache 표준

캐시가 필요한 기능은 도메인별 임의 Map으로 구현하지 않는다.

PFW cache port와 reference adapter를 검토한다.

- local cache
- distributed cache 확장
- key namespace
- TTL
- max size
- eviction
- negative cache
- stampede 방지
- cache aside
- invalidate
- versioned key
- tenant/domain isolation
- metric
- 장애 시 fallback

권한·secret·긴급 차단처럼 즉시성 요구가 높은 값은 stale cache 허용 여부를 명시한다.

### 1.10.11 시간·날짜·영업일 표준

분산 시스템에서 system clock 직접 사용을 남발하지 않는다.

- Clock port
- timezone
- 표준 저장 시각
- 표시 timezone
- business date
- 영업일 calendar
- 휴일
- cut-off time
- DST 가능성
- clock skew
- 만료·예약 적용
- 테스트용 fixed clock

CPF 표준 profile과 배포 지역이 달라도 일관되게 동작해야 한다.

### 1.10.12 API·event·file schema versioning

외부·내부 계약 변경에 대한 호환성 정책을 제공한다.

REST:

- API versioning 전략
- additive change
- breaking change
- deprecation
- sunset
- backward compatibility
- consumer contract test

broker/event:

- event type
- schema version
- producer version
- consumer compatibility
- unknown field
- duplicate
- ordering
- poison message
- replay compatibility

file/fixed-length:

- layout ID
- layout version
- effective date
- charset
- line ending
- record count
- checksum
- backward parser
- migration rule

OpenAPI, catalog, sample, test, 문서를 함께 변경한다.

### 1.10.13 DB schema migration과 데이터 migration

Flyway DDL 적용만으로 충분하지 않은 변경을 다룬다.

- expand/contract migration
- backward compatible schema
- online migration
- 대량 backfill
- resumable migration
- validation query
- checksum
- dry-run
- rollback 또는 forward-fix
- old/new application coexistence
- lock·timeout
- migration progress
- failure resume

운영 데이터 migration도 승인결재·실행 ID·감사·evidence와 연결한다.

### 1.10.14 데이터 보존·archive·파기

로그·감사·거래·첨부·배치·메시지·파일·승인 자료에 retention 정책을 제공한다.

- data category
- retention period
- archive period
- legal hold 확장
- purge
- anonymization
- attachment delete
- backup 반영
- audit
- dry-run
- 승인
- batch execution
- purge evidence

물리 삭제가 필요한 경우 참조 무결성과 과거 감사 조회 영향을 검증한다.

### 1.10.15 개인정보·민감정보 보호

- 분류 metadata
- 수집 최소화
- masking
- encryption at rest 확장
- encryption in transit
- secure temporary file
- clipboard/copy 제한 가능
- download watermark
- 원문 권한
- 조회 사유
- 데이터 export audit
- 테스트 데이터 비식별화
- 로그 민감정보 scanner

민감정보가 오류 메시지, metric label, trace tag, 파일명에 포함되지 않게 한다.

### 1.10.16 Backup·Restore·재해복구 확장

CPF 자체가 DB 제품 백업 엔진을 구현할 필요는 없지만 운영 가능한 계약과 검증 절차를 제공한다.

- backup 대상 inventory
- DB
- file/attachment
- configuration
- policy package
- encryption key metadata
- approval/audit
- broker offset 또는 복구 전략
- RPO/RTO metadata
- restore runbook
- restore validation
- point-in-time recovery 고려
- multi-region/DR 확장점
- failover/failback
- 복구 후 checksum·정합성 점검

restore 검증 없이 backup 성공만으로 완료 처리하지 않는다.

### 1.10.17 배포·업그레이드·무중단 전환

지원:

- executable bootJar
- embedded/external Tomcat
- rolling deployment
- blue/green 또는 canary 확장
- readiness
- startup probe
- graceful shutdown
- draining
- in-flight request 보호
- scheduler/batch 중복 실행 방지
- worker lease handoff
- broker consumer rebalancing 고려
- DB old/new version coexistence
- config/policy version 호환
- rollback

애플리케이션 버전, build SHA, schema version, policy version을 ADM에서 확인한다.

### 1.10.18 장애 격리·복구·Chaos 검증

단위 성공만 확인하지 않고 통제 가능한 장애 주입 테스트를 제공한다.

- target timeout
- connection refused
- partial instance failure
- registry stale
- DB unavailable
- broker unavailable
- file server unavailable
- disk full simulation
- malformed message
- duplicate message
- slow consumer
- policy snapshot load failure
- clock skew
- shutdown during execution

실 운영 인프라를 파괴하지 않는 deterministic adapter와 test profile을 사용한다.

검증:

- fallback
- circuit
- retry budget
- failover
- unknown result
- reconciliation
- last-known-good
- graceful recovery
- alert
- timeline

### 1.10.19 정합성 검사와 reconciliation 확장

업무별 reconciliation뿐 아니라 프레임워크 운영 정합성 검사도 제공한다.

- source catalog vs DB catalog
- route vs handler
- policy vs instance loaded version
- channel vs binding
- approval vs applied state
- outbox vs broker
- inbox vs consumer result
- file metadata vs physical file
- attachment DB vs storage
- batch execution vs lock/lease
- scheduler expected vs actual
- audit event completeness
- evidence index vs file

ADM에서 검사 실행, 결과, 재처리, 예외 승인, 이력을 제공한다.

### 1.10.20 멀티고객·다중 인스턴스 확장 경계

CPF가 즉시 완전한 SaaS multi-tenancy를 강제하지 않더라도 다음 경계를 깨지 않게 한다.

- customer/tenant context port
- data partition strategy 확장
- cache namespace
- message key
- file path isolation
- permission scope
- configuration override
- metric cardinality
- audit
- noisy-neighbor 제한

고객사별 customization이 PFW를 fork하거나 업무 engine을 복제하지 않도록 extension point를 제공한다.

### 1.10.21 국제화·문자·locale

- UTF-8 기본
- locale message
- timezone
- number/date formatter
- charset conversion
- fixed-length 전문 charset
- 다국어 오류 메시지
- Unicode normalization
- 파일 BOM 정책
- 제어문자 검증

한글 중심 UI를 유지하되 확장 가능한 구조로 만든다.

### 1.10.22 개발자 경험과 SDK

개발자가 CPF 표준을 쉽게 지키게 한다.

- starter/autoconfiguration
- annotation
- typed value object
- test fixture
- mock/reference adapter
- test client
- JUT default
- OpenAPI helper
- header/context helper
- execution ID generator/validator
- error catalog validator
- policy manifest generator
- local all-run
- sample project
- create-domain
- migration guide
- troubleshooting

개발자가 내부 class를 직접 호출하지 않고 port를 사용하도록 compile/ArchUnit gate를 제공한다.

### 1.10.23 API 사용성·운영 편의

공통 목록 API:

- pagination
- stable sorting
- filtering
- period search
- max page size
- cursor 확장
- field selection 제한
- export 비동기화
- large result protection

공통 변경 API:

- idempotency key
- optimistic lock
- request reason
- approval
- dry-run
- diff
- version
- standard error

대량 작업:

- preview
- 대상 건수
- chunk
- progress
- cancel
- partial failure
- retry
- 결과 파일
- audit

### 1.10.24 파일·첨부 보안 강화

- filename normalization
- path traversal
- symlink
- extension allowlist
- content type sniffing
- max size
- checksum
- duplicate
- malware scan port
- quarantine
- secure temp
- atomic move
- partial upload
- resumable 확장
- retention
- download token
- one-time token
- range download 검토
- download audit

압축 파일:

- zip slip
- zip bomb
- max entries
- expanded size
- nested archive
- encrypted archive 정책

### 1.10.25 메시징 품질

- message ID
- correlation ID
- execution ID
- schema version
- producer
- timestamp
- original/current channel
- idempotency key
- retry count
- delivery attempt
- trace context

처리:

- ack/nack
- visibility timeout
- ordering key
- partition key
- poison message
- DLQ reason
- replay authorization
- replay filter
- rate limit
- duplicate suppression
- consumer lag
- graceful shutdown

broker adapter 간 의미 차이를 PFW 표준 상태로 정규화한다.

### 1.10.26 Saga·보상 표준 완성도

Saga는 이름만 있는 상태 machine으로 끝내지 않는다.

- saga definition/version
- participant
- step order
- command
- result
- timeout
- retry
- compensation
- compensation failure
- manual intervention
- idempotency
- unknown
- reconciliation
- parent/child transactionGlobalId
- state persistence
- lock/concurrency
- outbox/inbox
- event/message
- ADM timeline
- approval for manual compensation
- EDU

동기 호출과 비동기 메시지 혼합 Saga를 모두 확장 가능하게 한다.

### 1.10.27 Scheduler·Batch 운영 품질

- timezone
- business date
- holiday
- misfire
- overlap policy
- concurrency
- dependency
- calendar
- blackout window
- maintenance window
- catch-up
- manual trigger
- parameter version
- approval
- effective date
- restart/rerun
- execution snapshot
- ghost
- lease
- graceful shutdown
- progress
- ETA
- skipped reason
- SLA delay alert

job definition과 execution instance를 분리한다.

### 1.10.28 운영 대시보드와 runbook

ADM/BAM에서 장애 상태만 보여주지 않고 다음 행동으로 연결한다.

- 관련 서비스
- 인스턴스
- 거래
- 채널
- route
- 최근 배포
- 최근 설정 변경
- 승인 요청
- alert
- runbook
- 로그
- timeline
- 재처리
- rollback

runbookId 또는 링크를 오류·alert·capability에 연결할 수 있어야 한다.

### 1.10.29 공급망 보안과 빌드 품질

최소 다음을 검토한다.

- dependency version
- 취약점 scan
- SBOM 생성
- license inventory
- checksum
- reproducible build 확장
- artifact provenance
- secret scan
- source encoding
- generated file 검증
- test report
- coverage 기준
- static analysis
- dependency locking 또는 version catalog
- 금지 repository
- snapshot dependency 정책

취약점 scan 도구가 없으면 실행 명령·adapter·미검증 상태를 남긴다.

### 1.10.30 성능·용량 기준

기능 존재만 확인하지 않고 대표 경로에 용량 목표와 성능 evidence를 둔다.

대상:

- Policy Engine
- Service Call Engine
- execution catalog
- transaction log
- metric aggregation
- batch claim
- scheduler poll
- outbox/inbox
- broker consumer
- file transfer
- ADM list/search/export
- approval bulk package
- channel×transaction policy

최소 측정:

- throughput
- latency
- memory
- DB query count
- index usage
- connection/thread utilization
- large data pagination
- cache hit
- policy snapshot load
- cold/warm startup

하드코딩된 비현실적 수치 대신 재현 가능한 benchmark profile과 결과를 제공한다.

### 1.10.31 접근성·브라우저·UI 일관성

ADM/BAM/BZA 전체에 적용한다.

- responsive
- keyboard
- focus
- label
- contrast
- screen reader 기본 semantics
- loading
- empty
- error
- retry
- permission denied
- session expired
- unsaved changes
- destructive confirm
- timezone/date
- large table
- mobile drawer
- chart table alternative
- browser console error 0 목표

지원 browser 범위와 viewport를 문서화하고 실제 browser E2E evidence를 남긴다.

### 1.10.32 기능 플래그와 단계적 활성화

위험 기능은 배포와 활성화를 분리할 수 있다.

- feature key
- owner
- profile
- enabled
- effective period
- percentage/target 확장
- service/domain/channel scope
- approval
- audit
- kill switch
- instance applied version

보안 우회나 prod 금지 기능을 일반 feature flag만으로 활성화할 수 없게 한다.

### 1.10.33 라이선스·제품화 확장점

CPF 상용 솔루션 목표를 위해 제품화 경계를 검토한다.

- product/version
- edition/capability catalog 확장
- customer configuration
- license verification port
- offline environment 고려
- license 만료 시 안전 동작
- 기능 제한과 데이터 접근 분리
- 개인정보 없는 사용량 metric
- support bundle
- diagnostic export
- 민감정보 제거

실제 과금·라이선스 서버 구축은 이번 범위에 무리하게 포함하지 않되 구조를 막지 않는다.

### 1.10.34 Support bundle과 진단 자료

운영 장애 분석을 위해 권한 기반 support bundle을 제공할 수 있다.

포함 가능:

- application version
- JVM/OS metadata
- active profile
- sanitized configuration
- policy/config version
- health
- thread summary
- pool status
- recent error summary
- instance registry
- route snapshot
- evidence checksum

금지:

- password
- token
- secret
- 개인정보 원문
- 전체 DB dump

생성·다운로드·만료·감사를 적용한다.

### 1.10.35 전체 기능 문서 정본화 기준

README는 왜 CPF를 사용하는지 제품 관점에서 설명한다.

상세 스펙과 가이드는 모든 핵심 capability에 대해 다음을 제공한다.

- 목적
- 문제
- architecture
- ownership
- normal/failure flow
- configuration
- security
- audit
- metric/alert
- ADM/BAM
- API
- SQL
- EDU
- test
- limitation
- extension
- runbook

최종 정본화는 구현 안정 후 수행한다. 현재 단계에서는 변경되는 구조를 반복 DOCX/PDF로 만들지 않는다.

### 1.10.36 전체 영역 우선순위 판정

전수 검토에서 발견된 항목을 다음으로 분류한다.

#### 필수 완료 범위

이번 마일스톤의 핵심 기능과 직접 연결되고 source·SQL·API·UI·test·evidence까지 완료 가능한 항목.

#### 보강 범위

기존 구현이 있으나 runtime·보안·운영·문서·evidence가 부족한 항목.

#### 착수 범위

대규모이지만 architecture/port/schema/reference adapter/대표 EDU까지 실질적으로 시작해야 하는 항목.

#### 후순위·제외 범위

외부 인프라·상용 제품·운영 조직 결정이 필요해 이번에 실제 완료할 수 없는 항목. 목표에서 삭제하지 않고 이유·선행조건·재현 명령을 남긴다.

단순히 항목 수가 많다는 이유로 모두 문서 착수로 돌리지 않는다. 기존 구현과 연결되는 높은 가치 범위는 실제 개발한다.

### 1.10.37 전체 영역 검증 완료 기준

각 capability는 해당되는 다음 증적이 있어야 완료 후보가 된다.

- source implementation
- layer wiring
- unit test
- contract test
- integration test
- negative test
- SQL/Flyway/all_install
- MariaDB runtime
- broker/file server runtime
- multi-instance
- browser
- OpenAPI
- EDU
- sample coverage
- performance
- security
- failure/recovery
- evidence
- report/matrix/index 정합성

외부 환경이 없어 실행하지 못한 검증은 `미검증`으로 유지한다.

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

다음 전역 누락도 탐지한다.

- mutation API 승인 우회
- runtime 설정 version·적용 instance 불일치
- secret 원문 source/log/evidence 포함
- transactionGlobalId·executionId·channel 누락
- API/event/file schema version 누락
- 운영 변경 before/after·reason·audit 누락
- retry에 backoff/jitter/deadline/idempotency 고려 누락
- 신규 thread/connection pool의 상한·metric 누락
- 대량 목록 무제한 조회
- file/archive 보안 검증 누락
- retention·purge 대상의 승인·감사 누락
- 배치·scheduler 실행 snapshot version 누락
- 신규 capability의 EDU/OpenAPI/evidence 누락
- README/스펙과 source·API 불일치

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
- 배치·비동기·Gateway·ADM·BAM
- 채널 레지스트리와 거래별 채널 정책
- original/current channel과 caller/target service
- JUT 개발·테스트 채널
- 고성능 policy snapshot
- 거래 테스트 콘솔
- 정책 파일 Export/Import와 환경 승격
- 사전 등록·source matching
- 공통 승인결재
- 로그 원본/포맷 조회
- 거래·채널 성공/오류 통계
- Saga·compensation·idempotency·outbox/inbox·unknown/reconciliation 등 CPF 구현 패턴
- embedded/external Tomcat
- local/dev/stg/prod 실행·배포
- domain/feature-first 개발 구조
- 실제 확인된 설치·실행 명령

README는 완제품 문서처럼 간결하고 전문적으로 작성하되, 미구현·미검증 기능을 사실과 다르게 구현 완료라고 단정하지 않는다. 구현 상태와 gap은 report/matrix에서만 관리한다.

기술 스펙과 개발·운영 가이드는 CPF가 제공하는 패턴과 capability를 체계적으로 설명한다.

최소 문서화 대상:

- Local Facade/Remote Facade Proxy
- Service Call Engine
- O/S/B 실행 ID
- URI·ID 이중 호출
- channel registry와 거래별 channel policy
- original/current channel propagation
- caller/target validation
- policy snapshot
- Gateway optional data plane
- timeout/retry/circuit/failover
- idempotency
- outbox/inbox
- broker/DLQ/replay
- Saga/compensation
- unknown result/reconciliation
- scheduler/worker/batch/center-cut
- file transfer/archive/compression
- security/permission/audit/masking
- error code/message
- transaction test
- policy promotion/import/approval
- ADM/BAM 운영관제

각 패턴 문서는 다음 공통 목차를 따른다.

1. 목적과 해결 문제
2. 적용 대상
3. 사용하면 안 되는 경우
4. architecture와 ownership
5. 정상 흐름
6. 실패 흐름
7. transactionGlobalId/segment
8. 상태·SQL
9. idempotency
10. timeout/unknown/retry
11. 재처리·보상
12. ADM/BAM
13. 오류 코드
14. configuration
15. source example
16. EDU
17. test
18. 운영 주의사항

문서만 작성하고 구현·테스트를 생략하지 않는다. 최종 문서는 실제 source, API, SQL, ADM 메뉴와 일치해야 한다.

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
12. ADM/BZA/BAM 주요 화면별 desktop·tablet·mobile 브라우저 검증 표와 screenshot 경로
13. URI·ID 호출, `S` 내부 전용, 표준 오류 runtime 검증 표
14. 채널 마스터·거래별 채널·client/service binding 검증 표
15. 거래 테스트 property/profile/permission 검증 표
16. 정책 Export/Import·사전 등록·source matching·승인결재 검증 표
17. policy snapshot version·checksum·instance 적용 현황
18. 로그 raw/formatted·통계 drill-down 검증 표
19. ADM 전체 mutation endpoint·메뉴 action 승인 적용 inventory
20. 자동승인·수동승인·예약 적용·실행 snapshot·rollback 검증 표
21. CPF 전체 capability inventory와 owner·상태·evidence·gap
22. 설정·secret·관측·알림·자원격리·호환성·retention·DR 검토 결과
23. API/event/file schema compatibility 검증 표
24. 배포·upgrade·rollback·graceful shutdown 검증 표
25. 성능·용량·대량조회 benchmark 결과
26. 공급망 보안·SBOM·dependency·secret scan 결과
27. 다음 gap

최종 보고에서 다음 표현을 금지한다.

- 파일이 있으므로 완료
- 테스트가 있으므로 완료
- Swagger UI가 열리므로 완료
- sampleId가 있으므로 완료
- 환경이 없어 실행하지 않았지만 완료
- qualityGate가 통과했으므로 전체 완료
- ACC/EXS를 삭제했으므로 cleanup 완료

이번 작업은 기존 구현을 파괴하거나 목표를 낮추는 정리 작업이 아니라, CPF를 실제 개발·배포·운영·감사할 수 있는 상용 framework 구조로 끌어올리는 작업이다.
