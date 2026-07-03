# CPF_FINAL_TARGET_REQUIREMENTS.md

# CPF 최종 목표 기준서 — 실무 적용 가능한 운영형 표준 프레임워크

## 0. 문서 목적

이 문서는 CPF(CoreFlow Platform Framework)의 최종 목표, 설계 철학, 전체 기능 범위, 품질 기준, 검증 기준, 문서 기준을 정의하는 최상위 기준서다.

CPF는 단순 샘플 프로젝트, 공통 유틸 모음, 문서성 프레임워크가 아니다.
CPF는 금융권을 포함한 범용 업무 시스템 프로젝트에서 개발자와 운영자가 신뢰하고 사용할 수 있는 **운영형 공통 프레임워크**를 목표로 한다.

CPF는 실제 프로젝트에서 반복되는 아래 영역을 표준화한다.

```text
개발 표준
운영 표준
보안 표준
권한 표준
로그/감사 표준
표준 헤더/거래 context
DB/SQL 표준
외부 연계 표준
고정길이 전문 처리 표준
배치/BAT 표준
center-cut 대량 처리 표준
테스트/검증 표준
OpenAPI/개발자 경험
ADM 운영 콘솔
EDU/XYZ 실전 예제
최종 문서 정본화
```

Codex는 매 작업 시작 전에 이 문서를 확인한다.
요청서에 명시되지 않은 내용이라도 CPF 최종 목표 달성에 필요하다고 판단되는 개발, 수정, 테스트, SQL, smoke, evidence, 리포트, 문서 보강은 선제적으로 수행할 수 있다.

단, 모든 선제 보강은 아래 제한을 반드시 지킨다.

```text
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증을 완료로 기록 금지
별도 변경파일 목록 산출물 생성 금지
문서 포맷 작업만으로 기능 완료 처리 금지
현재 마일스톤 목적과 무관한 대규모 리팩터링 금지
```

---

# 1. CPF 최종 비전

CPF는 신규 업무 시스템을 시작할 때 바로 사용할 수 있는 기준 프레임워크를 목표로 한다.

최종적으로 CPF는 아래 질문에 모두 “예”라고 답할 수 있어야 한다.

```text
신규 프로젝트의 기반 프레임워크로 사용할 수 있는가?
개발자가 EDU/XYZ를 복사해서 신규 업무 기능을 만들 수 있는가?
표준 헤더가 수신, 검증, 거래 context 생성, 로그 저장, ADM 조회, outbound 전파까지 연결되는가?
운영자가 ADM에서 거래, 오류, 감사, 권한, 배치, 캐시, 정책, 보안 상태를 조회하고 조치할 수 있는가?
BAT worker가 독립 실행되고 heartbeat, ghost, lock, job, step이 관제되는가?
center-cut 대량 처리 표준이 제공되는가?
REST 연계와 고정길이 전문 연계가 모두 표준화되어 있는가?
DB를 신규 빈 MariaDB에 설치해도 정상 동작하는가?
OpenAPI, runtime smoke, browser click, qualityGate, DB 검증 증적이 남는가?
장애가 발생했을 때 transactionGlobalId 기준으로 원인 추적이 가능한가?
문서와 실제 구현이 일치하는가?
실행하지 않은 검증을 완료로 포장하지 않는가?
기능이 늘어나도 공통 구조가 깨지지 않는가?
```

CPF는 단순 기능 목록이 아니라, 아래 품질을 함께 만족해야 한다.

```text
개발자가 신뢰할 수 있는 프레임워크
운영자가 사용할 수 있는 프레임워크
실제 프로젝트에 적용 가능한 프레임워크
장애와 변경에 강한 프레임워크
확장 가능한 프레임워크
문서와 구현이 일치하는 프레임워크
```

---

# 2. 상태값 기준

모든 기능, 리포트, 기능 매트릭스, 완료 보고는 아래 6개 상태값만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

## 2.1 완료

아래가 실제로 확인된 상태다.

```text
소스 구현
계층 연결
SQL/Flyway/all_install 반영
테스트
runtime smoke 또는 필요한 검증
리포트 반영
기능 매트릭스 반영
```

## 2.2 부분 구현

핵심 구조는 있으나 아래 중 일부가 빠진 상태다.

```text
Repository/Mapper 없음
SQL 미반영
runtime 미검증
ADM 조회 미연결
감사 로그 미연결
테스트 부족
smoke 미구현
문서/매트릭스 일부 불일치
```

## 2.3 미구현

요구 기능이 아직 실제 소스로 구현되지 않은 상태다.

## 2.4 미검증

구현은 있거나 있을 수 있으나 실행 검증을 하지 않은 상태다.

예:

```text
소스는 있으나 Gradle test 미실행
runtime smoke 미실행
신규 MariaDB full install 미실행
브라우저 클릭 미실행
실 broker 미검증
```

## 2.5 실패

실행했으나 테스트, 빌드, runtime, smoke, SQL, qualityGate가 실패한 상태다.

## 2.6 재확인 필요

아래처럼 판단이 필요한 상태다.

```text
리포트와 실제 파일 불일치
기능 매트릭스 상태값 불일치
Codex 보고와 GitHub master 불일치
설계 소유권 판단 필요
evidence 경로 오류
상태값 과장 의심
```

## 2.7 완료 불인정 공통 기준

아래는 완료로 인정하지 않는다.

```text
소스만 있고 테스트 없음
Controller만 있고 Service/Repository/DTO/SQL 없음
문서만 있고 실제 구현 없음
실행하지 않은 검증을 성공으로 기록
mock 테스트만 있고 runtime 검증이 필요한 기능을 완료 처리
정적 UI marker smoke를 browser click 완료로 기록
기존 개발 DB 일부 확인을 신규 빈 MariaDB full install 완료로 기록
Source-Level 테스트를 Runtime E2E 완료로 기록
embedded/mock broker 검증을 실 broker 검증으로 기록
리포트와 기능 매트릭스 상태 불일치
evidence 경로가 실제 파일과 다름
```

---

# 3. 모든 기능의 공통 설계 원칙

## 3.1 확장성 / 변경 가능성

모든 기능은 현재 샘플 하나만 동작하도록 만들지 않는다.
신규 업무, 신규 채널, 신규 기관, 신규 배치, 신규 전문, 신규 정책, 신규 테이블, 신규 연계가 추가될 수 있다는 전제로 설계한다.

공통 기준:

```text
샘플 API/job만 맞춘 하드코딩 금지
업무명, 테이블명, path, 기관 코드, 전문 코드에 강결합 금지
interface / adapter / provider / handler / registry / spec / config 기반 확장 구조 우선
PFW는 공통 계약과 표준 인터페이스를 제공
업무별 구현은 BAT/CMN/EXS/MBR/BIZADM/EDU/XYZ 또는 업무 adapter가 담당
업무 item/result/target 데이터는 PFW에 고정하지 않음
확장 지점이 아직 구현되지 않았으면 완료가 아니라 부분 구현으로 기록
확장성을 이유로 과도하게 복잡하게 만들지 않음
현재 필요한 최소 구현과 향후 확장 지점을 명확히 분리
다른 업무가 붙을 때 PFW core를 계속 고치지 않아도 되는 구조 지향
```

완료 불인정:

```text
현재 샘플에만 맞춘 if/else 분기
특정 업무명/테이블명/path에 강하게 결합
확장 가능하다고 문서만 있고 interface/adapter/spec 구조 없음
다른 업무 모듈이 붙으면 PFW를 수정해야 하는 구조
PFW가 업무 item/result 저장소를 직접 소유
```

## 3.2 운영 가능성

기능은 “API가 동작한다”만으로 완료가 아니다.
운영자가 상태를 조회하고, 실패 원인을 찾고, 필요한 경우 조치할 수 있어야 한다.

주요 기능은 가능하면 아래를 함께 고려한다.

```text
상태 조회
상세 조회
실패 이력
재처리 가능 여부
운영자 조치
감사 로그
권한 통제
ADM 조회
smoke 검증
장애 시나리오
```

## 3.3 보안 / 감사 / 마스킹 기본 내장

보안은 후순위 부가기능이 아니다.

```text
민감정보 원문 로그 금지
Authorization/token/API key 원문 저장 금지
다운로드 감사
마스킹 해제 감사
권한 변경 감사
운영자 조치 감사
전문 원문 조회 통제
secret 하드코딩 금지
기본 비밀번호 하드코딩 금지
```

## 3.4 검증 정직성

실행하지 않은 검증은 완료가 아니다.

```text
Source-Level 테스트 완료와 Runtime E2E 완료를 구분한다.
mock 테스트와 실제 runtime smoke를 구분한다.
기존 개발 DB 확인과 신규 빈 MariaDB 설치 검증을 구분한다.
정적 UI marker smoke와 browser click 검증을 구분한다.
embedded/mock broker와 실 Redis/Kafka/MQ 검증을 구분한다.
```

---

# 4. 문서 원칙

## 4.1 중간 문서

중간 개발/검수 단계에서는 HTML 문서를 신규 작성하거나 직접 수정하지 않는다.

```text
신규 문서는 Markdown(.md) 원본으로 작성한다.
기존 HTML 문서를 수정해야 하면 동일 목적의 .md 문서로 전환한다.
기능 매트릭스처럼 check script/evidence와 연결된 HTML은 전환 시 스크립트도 함께 맞춘다.
문서 디자인, CSS, HTML 태그 정합성은 중간 완료 기준이 아니다.
내용 정확성, 구현 상태, 실행 검증 여부, 미검증 사유, evidence 경로를 우선한다.
최종 정본화 단계에서 md 원본 기준으로 PDF/HTML/DOCX를 생성한다.
```

## 4.2 최종 문서 후보

```text
README.md
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_DEVELOPER_GUIDE.md
CPF_OPERATION_GUIDE.md
CPF_STANDARD_HEADER_GUIDE.md
CPF_DB_SQL_GUIDE.md
CPF_ADM_GUIDE.md
CPF_BAT_GUIDE.md
CPF_EXS_MESSAGE_GUIDE.md
CPF_EDU_XYZ_GUIDE.md
CPF_TROUBLESHOOTING_GUIDE.md
CPF_RELEASE_UPGRADE_GUIDE.md
```

## 4.3 문서 완료 기준

```text
실제 구현과 문서가 일치
실행 검증 결과와 문서가 일치
미구현/미검증/실패 항목 숨기지 않음
문서에서 API path를 추정하지 않고 Controller 기준으로 작성
SQL 경로와 실제 파일 경로 일치
```

---

# 5. 모듈 책임

## 5.1 PFW

프레임워크 핵심 공통 모듈이다.

주요 책임:

```text
표준 헤더
확장 헤더
TransactionContext
transactionGlobalId
trace/span context
공통 응답
공통 오류
공통 예외
공통 validation
공통 로그
공통 감사
마스킹 공통
권한 필터 공통
보안 공통
CpfWebClient
CpfRestClient
outbound 헤더 전파
batch 공통 인터페이스
center-cut 표준 인터페이스
idempotency 표준
outbox/event 표준 후보
observability 공통
health 공통
테스트 유틸
```

PFW가 소유하면 안 되는 것:

```text
업무 도메인 테이블
업무 item/result 저장소
기관별 전문 layout
업무별 center-cut 대상 테이블
특정 업무 전용 로직
```

## 5.2 CMN

프로젝트 공통 업무 보조 모듈이다.

주요 책임:

```text
채번
공통 코드
공통 메시지
파일
다운로드
업로드
알림
템플릿
마스킹 helper
validation helper
고정길이 전문 처리 엔진
전문 layout/field spec
전문 parser/formatter
전문 fixture helper
업무 adapter 보조
```

## 5.3 ADM

운영 콘솔 모듈이다.

주요 책임:

```text
운영자 관리
역할 관리
메뉴 관리
버튼 권한
API 권한
로그인 이력
거래 로그 조회
오류 로그 조회
감사 로그 조회
표준/확장 헤더 snapshot 조회
다운로드 감사
마스킹 조회 감사
배치 job/step 조회
worker/heartbeat 조회
ghost candidate 조회
lock 조회
center-cut 조회
전문 송수신 조회
외부 연계 조회
캐시 상태 조회
정책 조회
보안 상태 조회
운영자 조치
browser click 검증 대상
```

ADM은 단순 API 모음이 아니라 운영자가 실제로 상태를 보고 조치하는 콘솔이다.

## 5.4 BAT

독립 실행 가능한 배치 worker/application 모듈이다.

주요 책임:

```text
BAT standalone bootJar
worker instance 등록
heartbeat
heartbeat timeout
ghost 감지
job execution
step execution
parameter 관리
lock 획득/해제
stale lock 감지
중지 요청
재실행 요청
강제 실행 요청
operator action
operator action audit
center-cut 기본 구현체
ADM 관제 연동
```

## 5.5 EXS

대외연계 기본 구현체 모듈이다.

주요 책임:

```text
대외기관 관리
endpoint 관리
REST 연계
고정길이 전문 송수신
timeout
retry
circuit breaker 후보
fallback 후보
OAuth/JWT/token 구조
API key 관리 기준
송신 로그
수신 로그
응답 코드 매핑
전문 마스킹 로그
transactionGlobalId 전파
표준/확장 헤더 전파
ADM 조회
```

## 5.6 MBR

회원 기본 구현체 모듈이다.

주요 책임:

```text
회원 기본 조회
회원 상태
회원 식별자 표준
memberNo/customerNo/userId 의미 분리
회원 마스킹
회원 감사 위치
타 모듈 Facade 연동
EDU/XYZ 샘플 연계
```

## 5.7 BIZADM

업무 관리자 기본 구현체 모듈이다.

주요 책임:

```text
업무 관리자 샘플
업무 권한 적용 예
메뉴/버튼/API 권한 적용 예
다운로드 권한
마스킹 권한
감사 로그
ADM과 업무 관리자 역할 분리
```

## 5.8 EDU/XYZ

개발자가 복사해 신규 기능을 만들 수 있는 실전형 표준 예제 모듈이다.

주요 책임:

```text
단건 조회
목록 조회
등록
수정
삭제 또는 상태 변경
offset paging
keyset paging
검색
정렬
DTO validation
Mapper/XML/SQL fixture
다른 주제영역 Facade 연동
권한 적용
마스킹 위치
감사 로그 위치
표준 헤더 조회
확장 헤더 조회/전파
CpfWebClient outbound
CpfRestClient outbound
고정길이 전문 처리 예제
center-cut 예제
테스트와 smoke
```

---

# 6. 필수 기능 영역

## 6.1 아키텍처 / 계층 표준

필수 기능:

```text
멀티모듈 Gradle 구조
Controller / Service / Repository / Mapper / DTO / Validation 계층
API path 규칙
패키지 네이밍
클래스 네이밍
request/response DTO 규칙
업무 모듈 간 직접 DB JOIN 금지
Facade / Service / API wrapper 연동
공통 테스트 패턴
SQL 파일 배치 규칙
smoke script 규칙
evidence 관리 규칙
기능 매트릭스 관리 규칙
```

완료 기준:

```text
신규 개발자가 EDU/XYZ를 보고 동일한 구조로 업무 기능을 만들 수 있음
모듈 책임이 섞이지 않음
계층 누락 없이 Controller/API, Service, Repository/Mapper, DTO, SQL, Test가 연결됨
```

---

## 6.2 표준 헤더 / 거래 Context

필수 기능:

```text
표준 헤더 정의
REQUIRED / RECOMMENDED / OPTIONAL / INTERNAL_ONLY / FORBIDDEN_TO_LOG_RAW
transactionGlobalId 생성/검증
traceId/spanId
originalChannelCode/currentChannelCode
X-Client-IP 산정
X-User-Id / X-Customer-No / X-Member-No / X-Operator-Id 의미 분리
inbound header
resolved header
response header
outbound header
HeaderContext
TransactionContext
HeaderExtractor
HeaderValidator
HeaderMutator
HeaderSnapshot
HeaderPropagator
CpfWebClient / CpfRestClient 연동
민감 헤더 원문 로그 금지
ADM inbound/resolved/outbound/response 조회
```

중요 의미 기준:

```text
X-User-Id는 고객번호가 아니다.
X-Customer-No는 고객번호다.
X-Member-No는 회원번호다.
X-Operator-Id는 ADM 운영 조치자다.
X-Original-Channel-Code는 최초 유입 채널이다.
X-Channel-Code는 현재 처리 채널이다.
X-Client-IP는 trusted proxy/gateway/LB/WAF 기준으로 산정한다.
Authorization, X-Api-Key, token류는 원문 로그 저장 금지다.
```

완료 기준:

```text
표준 헤더 정의와 실제 filter/interceptor가 연결됨
필수 헤더 검증 가능
거래 context 생성 가능
거래 로그/header snapshot 저장 가능
ADM 조회 가능
outbound 자동 전파 가능
```

완료 불인정:

```text
문서에만 표준 헤더 존재
request.getHeader 직접 사용만 존재
거래 context 미생성
ADM 조회 없음
outbound 전파 없음
민감 헤더 원문 저장 가능성 있음
```

---

## 6.3 확장 헤더

확장 헤더는 개발팀이 CPF 표준 naming rule에 맞춰 추가하고, 프레임워크 공통 API로 조회/수정/전파하는 개발 표준이다.

기준:

```text
X-Cpf-Ext-* naming rule
X-Cpf-Ext-1 ~ X-Cpf-Ext-5 기본 예약형
X-Cpf-Ext-{Key} 이름 기반 확장
사전 등록된 이름만 허용하지 않음
naming rule에 맞으면 CPF 확장 헤더로 인식
모든 확장 헤더는 기본 OPTIONAL
확장 헤더 무조건 마스킹 금지
Authorization/token/API key류 우회 저장/전파 금지
HeaderExtractor/Validator/Context/Mutator/Snapshot/Propagator 처리
CpfWebClient/CpfRestClient outbound 전파
ADM 거래 로그/header snapshot 조회
EDU/XYZ 확장 헤더 예제
```

예:

```text
X-Cpf-Ext-Campaign-Id
X-Cpf-Ext-Partner-Code
X-Cpf-Ext-Legacy-Trace
X-Cpf-Ext-Experiment-Group
```

완료 불인정:

```text
X-Cpf-Ext-* naming rule 없이 5개 상수만 추가
확장 헤더를 사전 등록된 이름만 허용하도록 제한
확장 헤더를 무조건 마스킹
공통 API 없이 request.getHeader/header.set 직접 사용
outbound 전파 구조 없음
ADM 거래 로그에서 확장 헤더 확인 불가
EDU/XYZ 예제 없음
```

---

## 6.4 공통 응답 / 오류 / 메시지

필수 기능:

```text
공통 API 응답 포맷
성공/실패 응답
business error / system error
errorCode / messageCode / message / detail
validation error
field error
traceId / transactionGlobalId 응답 포함
GlobalExceptionHandler
업무 예외 BaseException
시스템 예외 BaseException
메시지 코드 관리
다국어 확장 가능 구조
ADM 오류 로그 조회
오류 발생 시 거래 로그와 연결
```

완료 불인정:

```text
Controller별 제각각 응답
예외 stacktrace 노출
오류 코드 없이 문자열만 반환
오류 로그와 transactionGlobalId 연결 없음
```

---

## 6.5 Validation / Paging / Search

필수 기능:

```text
DTO validation
공통 validation annotation
코드값 검증
날짜/기간 검증
금액/수량 검증
정렬 필드 whitelist
검색 조건 검증
paging size 제한
request body 크기 제한
offset paging
keyset paging
total count 정책
sort whitelist
default sort
날짜 범위 검색
상태 검색
대량 조회 제한
다운로드 연계 기준
```

완료 기준:

```text
잘못된 요청이 표준 오류 응답으로 반환
field error 구조 일관
목록 API가 무제한 조회를 하지 않음
EDU/XYZ에 offset/keyset/search/sort 예제 존재
```

---

## 6.6 로그 / 감사 / 추적성

필수 기능:

```text
거래 로그
오류 로그
감사 로그
API access log
다운로드 감사
마스킹 조회 감사
권한 변경 감사
운영자 조치 감사
배치 조치 감사
외부 연계 송수신 로그
전문 송수신 로그
로그 레벨 정책
DB 로그 저장 여부 정책
파일 로그와 DB 로그 병행 정책
민감정보 마스킹
원문 저장 금지 헤더/필드
transactionGlobalId 기준 로그 연결
ADM 조회/상세/필터/다운로드
```

완료 기준:

```text
거래/오류/감사 로그 분리
transactionGlobalId로 연결 조회 가능
민감정보 원문 저장 차단
ADM에서 운영자가 조회 가능
```

---

## 6.7 보안 / 인증 / 권한

필수 기능:

```text
로그인
로그아웃
비밀번호 변경
비밀번호 초기화
계정 잠금/해제
로그인 실패 횟수
세션 또는 token 관리
운영자 관리
역할 관리
메뉴 권한
버튼 권한
API 권한
다운로드 권한
마스킹 해제 권한
고위험 기능 권한
권한 변경 감사
API 권한 필터
보안 헤더
CORS/CSRF 정책 검토
secret 하드코딩 금지
기본 비밀번호 하드코딩 금지
환경변수 주입 기준
```

완료 기준:

```text
역할별 메뉴/버튼/API 권한이 실제 runtime에서 동작
권한 변경 감사 로그 저장
ADM_ADMIN / ADM_VIEWER 같은 권한 차단/허용 검증
```

---

## 6.8 DB / SQL / Flyway / all_install

필수 기능:

```text
테이블 네이밍
컬럼 네이밍
PK/FK/INDEX 규칙
공통 컬럼
논리 삭제
상태 코드
optimistic lock/version
MyBatis Mapper
XML SQL
동적 SQL
SQL 주석/COMMENT
split SQL
Flyway migration
00_all_install.sql
00_all_install_and_smoke.sql
신규 빈 MariaDB 설치 검증
seed data
smoke SQL
SQL idempotent 가능 범위
DB 표준 가이드
```

완료 불인정:

```text
split SQL에만 있고 Flyway/all_install에 없음
Flyway에만 있고 all_install에 없음
기존 개발 DB에서만 확인
신규 빈 MariaDB 설치 미검증인데 완료 기록
```

---

## 6.9 트랜잭션 / 동시성 / 잠금 / 멱등성

필수 기능:

```text
Spring transaction 표준
read-only transaction 기준
propagation 기준
rollback 기준
optimistic lock
pessimistic lock 사용 기준
distributed lock 또는 DB lock
lock timeout
stale lock 감지
중복 요청 방지
idempotency key
재처리 가능 거래
배치 lock과 업무 lock 분리
```

완료 기준:

```text
중복 실행/중복 요청 방지 구조 존재
lock 이력과 timeout 조회 가능
배치/업무 lock 구분
```

---

## 6.10 외부 연계 / EXS

필수 기능:

```text
대외기관 관리
endpoint 관리
timeout 정책
retry 정책
circuit breaker 후보
fallback 후보
OAuth/JWT/token
API key 관리 기준
송신 로그
수신 로그
요청/응답 전문 마스킹
실패 재처리
외부 거래 ID
transactionGlobalId 전파
표준 헤더 outbound 전파
확장 헤더 outbound 전파
CpfWebClient wrapper
CpfRestClient wrapper
mock downstream 테스트
EXS ADM 조회
```

완료 기준:

```text
직접 WebClient/RestClient 하드코딩이 아니라 CPF wrapper 사용
timeout/retry/log/header propagation 공통 적용
송수신 이력 ADM 조회 가능
```

---

## 6.11 고정길이 전문 처리

금융권/레거시/대외기관 연계에서 필수적인 전문 처리 공통 기능이다.

필수 기능:

```text
고정길이 전문 layout 정의
LayoutSpec / FieldSpec
byte length 기반 parser
byte length 기반 formatter
charset 처리
UTF-8 / MS949 / EUC-KR / ASCII
header/body/trailer 구조
field type converter
STRING / NUMBER / DECIMAL / DATE / DATETIME / CODE / AMOUNT / FILLER
padding / alignment
required 검증
length 검증
numeric/date/code 검증
checksum/recordCount 검증 후보
전문 masking
parse result
format result
parse-format round trip test
테스트 fixture helper
전문 sample builder
기관별 전문 송수신
전문 ID / 업무 코드 / 기관 코드 관리
요청 전문 조립
응답 전문 파싱
timeout / retry / 재처리
송수신 이력 저장
응답 코드 → CPF 오류 코드 매핑
마스킹 전문 로그
파싱 실패 로그
ADM 전문 송수신 조회
EDU/XYZ 전문 예제
```

전문 Layout 필수 속성:

```text
layoutId
layoutName
institutionCode
messageCode
messageName
direction
version
charset
totalLength
headerLength
bodyLength
trailerLength
enabledYn
description
```

FieldSpec 필수 속성:

```text
fieldName
displayName
startPosition
length
byteLength
type
requiredYn
defaultValue
paddingChar
align
scale
format
codeGroup
sensitiveYn
maskingType
description
```

완료 불인정:

```text
substring index 하드코딩
char length 기준 파싱
parser만 있고 formatter 없음
formatter만 있고 parser 없음
validation 없음
padding/align 기준 없음
마스킹 없이 원문 전문 로그 저장
송수신 로그와 transactionGlobalId 미연결
EXS/EDU 예제 없음
```

---

## 6.12 BAT / 배치

필수 기능:

```text
BAT standalone application
:bat:bootJar
BAT jar 독립 실행
worker 등록
instanceId 관리
heartbeat
heartbeat timeout
ghost candidate 감지
job execution
step execution
job parameter
lock 획득/해제
stale lock 감지
재실행 요청
중지 요청
강제 실행 요청
operator action
operator action audit
실패 재처리
batch ADM 조회
batch runtime smoke
```

완료 기준:

```text
BAT jar 단독 기동
worker 등록
heartbeat 저장
ghost candidate 조회
ADM에서 worker/job/step/lock 조회
```

---

## 6.13 Center-Cut

필수 기능:

```text
center-cut job definition
center-cut parameter
center-cut item
center-cut result
target provider
item state repository
handler
retry
skip
stop request
force request
parent transactionGlobalId
child transactionGlobalId
item별 성공/실패 기록
ADM center-cut 조회
EDU center-cut 예제
```

역할:

```text
PFW: 표준 인터페이스/상태/로그/감사/오류 기준
BAT: 기본 모수/item/result 구현체
업무 모듈: 업무별 target/item/result adapter
ADM: 상태 조회와 운영자 조치
```

완료 불인정:

```text
PFW가 업무 item/result 저장소를 고정 소유
메모리 sample만 있고 DB 기반 구조 없음
parent/child transactionGlobalId 기준 없음
ADM 조회 없음
EDU 예제 없음
```

---

## 6.14 캐시 / 설정 / 정책

필수 기능:

```text
공통 설정 조회
코드 관리
메시지 관리
정책 관리
로그 정책
캐시 상태 조회
캐시 refresh
다중 인스턴스 refresh 전파
TTL 정책
local cache / remote cache
Redis 연동 후보
ADM 캐시/정책 조회
정책 변경 감사
```

---

## 6.15 파일 / 다운로드 / 업로드

필수 기능:

```text
파일 업로드 공통
파일 다운로드 공통
파일 메타 관리
파일 크기 제한
확장자 제한
MIME 검증
저장소 abstraction
다운로드 권한
다운로드 감사
개인정보 포함 파일 통제
마스킹 다운로드
대용량 다운로드 기준
임시 파일 cleanup
ADM 다운로드 이력 조회
```

---

## 6.16 알림 / 메시지 / 템플릿

필수 기능:

```text
공통 메시지 코드
다국어 메시지 확장 가능 구조
알림 템플릿
이메일 adapter
문자 adapter
알림톡 adapter 후보
메신저 adapter 후보
발송 이력
실패 이력
재발송 기준
템플릿 변수 검증
개인정보 마스킹
ADM 발송 이력 조회
```

---

## 6.17 채번 / ID 생성

필수 기능:

```text
transactionGlobalId 생성
업무 채번
일자 기반 채번
prefix 기반 채번
sequence 기반 채번
동시성 안전성
채번 실패 처리
채번 이력 조회
CMN 채번 API
EDU 채번 사용 예제
```

---

## 6.18 Health / Observability

필수 기능:

```text
health endpoint
readiness
liveness
DB 연결 상태
cache 상태
외부 연계 상태
batch worker 상태
build version
instanceId
moduleCode
serverId
metrics
slow transaction
error rate
timeout count
retry count
batch failed count
ghost candidate count
ADM observability 조회
OpenTelemetry 연계 후보
```

---

## 6.19 OpenAPI / 개발자 경험

필수 기능:

```text
OpenAPI 자동 생성
Swagger UI
공통 헤더 문서화
확장 헤더 예제
공통 오류 응답 문서화
권한 필요 여부 문서화
request/response 예제
EDU/XYZ API 샘플
local 실행 가이드
smoke 실행 가이드
```

---

## 6.20 Runtime / 배포 / 환경

필수 기능:

```text
profile 분리
local/dev/test/prod 설정
환경변수 주입
secret 하드코딩 금지
bootJar
Docker 실행 검토
포트 충돌 처리
graceful shutdown
startup validation
migration 실행 기준
로그 경로 기준
instanceId/serverId 주입
runtime smoke
cleanup 검증
```

---

## 6.21 테스트 / 품질 게이트

필수 기능:

```text
module unit test
service test
mapper/sql test
controller test
integration test
runtime smoke
OpenAPI smoke
browser click smoke
DB smoke
qualityGate
UTF-8/mojibake check
evidence check
test fixture
실패/미검증 기록
리포트/매트릭스 정합성
```

---

## 6.22 ADM 운영 콘솔

ADM은 단순 API 모음이 아니라 CPF 운영 콘솔이다.

필수 기능:

```text
운영자 관리
역할 관리
메뉴 관리
버튼 권한 관리
API 권한 관리
로그인 이력
권한 변경 감사
거래 로그 조회
거래 상세 조회
inbound/resolved/outbound/response 헤더 조회
확장 헤더 조회
오류 로그 조회
오류 상세 조회
감사 로그 조회
다운로드 감사 조회
마스킹 조회 감사
배치 job/step 조회
worker/heartbeat 조회
ghost candidate 조회
lock 조회
operator action 조회/요청
cache 상태 조회
policy 상태 조회
security 상태 조회
OpenAPI smoke
browser click smoke
```

---

## 6.23 EDU/XYZ 실전 샘플

EDU/XYZ는 CPF의 개발 표준을 보여주는 가장 중요한 예제다.

필수 기능:

```text
단건 조회
목록 조회
등록
수정
삭제 또는 상태 변경
offset paging
keyset paging
검색
정렬
DTO validation
Mapper/XML/SQL fixture
다른 주제영역 Facade 연동
권한 적용
마스킹 위치
감사 로그 위치
표준 헤더 조회
확장 헤더 조회/전파
CpfWebClient outbound
CpfRestClient outbound
고정길이 전문 처리 예제
center-cut 예제
테스트와 smoke
```

완료 불인정:

```text
mbr/exs 테이블 직접 JOIN
for-loop 단건 반복 호출
outbound 표준 헤더 수동 하드코딩
mock 테스트만 있음
Mapper/XML/SQL fixture 없음
실제 개발자가 복사해 쓰기 어려운 예제
```

---

# 7. 고급 운영/확장 목표

아래 영역은 CPF를 장기적으로 더 완성도 높은 운영형 프레임워크로 만들기 위한 확장 목표다.
초기 마일스톤에서 모두 구현하지 않더라도, 구조적으로 고려해야 한다.

## 7.1 API Versioning / Deprecation

```text
API version path 또는 header 전략
v1/v2 공존 기준
deprecated API 표시
deprecated 예정일/종료일
OpenAPI deprecated 표시
API 권한과 version 연결
하위 호환성 깨짐 기록
API 변경 이력
client 영향도
EDU/XYZ versioned API 예제
```

## 7.2 Idempotency / Outbox / Event

```text
idempotency key 표준
중복 요청 감지
중복 요청 결과 재사용
업무 이벤트 표준 모델
outbox table 표준
event publish 상태
event retry
event 실패/보류 상태
transactionGlobalId와 event 연결
ADM event/outbox 조회
EXS/MQ/Kafka 연계 가능 구조
```

## 7.3 Workflow / Approval / Operator Action

```text
운영자 조치 요청
승인/반려
2인 승인 후보
고위험 작업 승인
강제 실행 승인
권한 변경 승인 후보
마스킹 해제 승인 후보
다운로드 승인 후보
승인 이력
감사 로그 연결
ADM workflow 조회
```

대상 후보:

```text
배치 강제 실행
ghost 확정 처리
lock 강제 해제
권한 변경
API 권한 변경
마스킹 해제
민감 파일 다운로드
```

## 7.4 Rule / Policy Engine

```text
정책 코드
정책 값
적용 범위
적용 모듈
유효 기간
우선순위
enabled 여부
정책 변경 이력
정책 캐시
정책 refresh
ADM 정책 조회
정책 변경 감사
```

적용 후보:

```text
로그 저장 정책
마스킹 정책
다운로드 제한
배치 timeout
retry 횟수
API rate limit
파일 업로드 제한
전문 원문 저장 여부
```

## 7.5 Multi-Tenancy / Institution / Channel

```text
tenantId 또는 institutionCode 기준 분리 후보
channelCode 기준 정책 분리
operator 권한 범위 분리
API 권한 범위 분리
데이터 조회 범위 제한
로그/감사 tenant 식별
batch tenant scope
EXS 기관별 endpoint 분리
설정/정책 tenant override 후보
EDU/XYZ tenant-aware 예제 후보
```

## 7.6 Rate Limit / Quota / Throttling

```text
API rate limit 후보
기관별 quota 후보
사용자/운영자별 제한 후보
batch 동시 실행 제한
EXS endpoint별 TPS 제한
retry 폭주 방지
throttle 상태 로그
ADM rate limit 상태 조회 후보
```

## 7.7 Resilience / Fault Injection / Failure Test

```text
DB timeout 시나리오
EXS timeout 시나리오
retry exhaustion 시나리오
batch worker kill 시나리오
heartbeat 중단 시나리오
ghost candidate 발생 시나리오
lock stale 시나리오
downstream 500/timeout mock
circuit breaker 후보
fallback 후보
장애 시 smoke/test
```

## 7.8 Performance / Load / Benchmark

```text
API 응답 시간 기준 후보
목록 조회 paging 성능 기준
keyset paging 예제
batch 처리량 기준
center-cut 처리량 측정 후보
고정길이 전문 parser/formatter benchmark 후보
DB index 검증
N+1 / 반복 단건 호출 방지
대량 다운로드 기준
smoke와 performance test 구분
```

## 7.9 Data Lifecycle / Retention / Purge

```text
거래 로그 보존 기간
오류 로그 보존 기간
감사 로그 보존 기간
전문 송수신 로그 보존 기간
파일 보존 기간
batch execution 보존 기간
purge job
archive 후보
purge 감사 로그
ADM 보존 정책 조회
```

주의:

```text
감사 로그와 보안 로그는 임의 삭제되지 않도록 별도 정책이 필요하다.
```

## 7.10 Data Privacy / Classification

```text
필드 보안 등급 후보
개인정보 필드 분류
민감정보 필드 분류
마스킹 rule
원문 저장 금지 rule
마스킹 해제 권한
마스킹 해제 감사
다운로드 개인정보 포함 여부 기록
전문 필드 masking
ADM 민감정보 조회 통제
```

## 7.11 Encryption / Key / Secret Management

```text
secret 하드코딩 금지
환경변수 주입
secret file 주입 후보
Vault/KMS 연계 후보
암호화 helper
단방향 hash helper
비밀번호 hash 기준
key rotation 후보
암호화 대상 필드 후보
설정값 마스킹
로그 secret masking
```

## 7.12 Feature Flag / Rollout / Rollback

```text
feature flag 후보
module별 기능 on/off
channel별 기능 on/off
tenant별 기능 on/off 후보
rollout percentage 후보
rollback 기준
ADM feature flag 조회 후보
변경 감사
```

## 7.13 Developer Experience / CLI / Code Generator

```text
신규 업무 모듈 생성 가이드
Controller/Service/Mapper/DTO 템플릿
EDU/XYZ 복사 기준
code generator 후보
CLI 후보
API scaffold 후보
Mapper/XML scaffold 후보
SQL fixture scaffold 후보
테스트 fixture scaffold 후보
local smoke 실행 가이드
```

## 7.14 Compatibility / Upgrade / Migration

```text
CPF version 관리
module version 관리
DB migration version
breaking change 기록
upgrade guide
deprecated API 정리
migration script
backward compatibility 기준
release note
changelog
```

## 7.15 API / Module Catalog

```text
API catalog
module catalog
batch job catalog
center-cut job catalog
전문 layout catalog
error code catalog
message code catalog
policy catalog
header catalog
dependency map
```

## 7.16 Operations Runbook / Playbook

```text
DB 장애 대응
EXS timeout 대응
batch ghost 대응
lock stale 대응
권한 오류 대응
표준 헤더 오류 대응
전문 파싱 오류 대응
OpenAPI smoke 실패 대응
MariaDB install 실패 대응
browser click 실패 대응
```

---

# 8. 마일스톤 계획

## M3-3. PFW 표준 헤더 확장 구조

```text
X-Cpf-Ext-* naming rule
X-Cpf-Ext-1 ~ X-Cpf-Ext-5
X-Cpf-Ext-{Key}
HeaderExtractor/Validator/Context/Mutator/Snapshot/Propagator 보강
CpfWebClient/CpfRestClient 영향 확인
EDU/XYZ 확장 헤더 예제
Source-Level test
```

## M3-4. PFW 공통 응답/오류/validation/paging

```text
공통 응답 포맷
공통 오류 포맷
GlobalExceptionHandler
validation error 표준
paging/search/sort 표준
EDU/XYZ 적용 예제
OpenAPI 예제 반영
```

## M4. BAT standalone / heartbeat / ghost

```text
BAT bootJar
BAT jar runtime
worker 등록
heartbeat 저장
heartbeat timeout
ghost candidate 산정
ADM worker/heartbeat/ghost 조회
smoke-bat-runtime
```

## M4-2. Center-Cut 기본 구현체

```text
PFW center-cut 표준 인터페이스
BAT center-cut 기본 모수/item/result
provider/handler/repository
parent/child transactionGlobalId
ADM center-cut 관제
EDU center-cut 예제
```

## M5. EDU/XYZ 실전 샘플 완성

```text
CRUD
offset/keyset paging
검색/정렬
validation
Mapper/XML/SQL fixture
Facade 연동
권한/마스킹/감사 위치
표준 헤더/확장 헤더
CpfWebClient/CpfRestClient
고정길이 전문 예제
center-cut 예제
```

## M6. CMN 공통 기능

```text
채번
파일
다운로드
알림
메시지
템플릿
마스킹 helper
validation helper
코드/설정 helper
```

## M6-1. CMN 고정길이 전문 처리

```text
LayoutSpec/FieldSpec
FixedLengthMessageParser
FixedLengthMessageFormatter
charset/byte length
padding/align/type conversion
validation
masking
fixture helper
parser/formatter round trip test
```

## M6-2. EXS 전문 송수신

```text
대외기관/endpoint
전문 요청 조립
전문 응답 파싱
송수신 로그
응답 코드 매핑
retry/timeout
EXS mock endpoint
ADM 전문 송수신 조회
```

## M7. MBR / BIZADM / EXS 기본 구현체

```text
MBR 회원 기본 구현체
BIZADM 업무 관리자 기본 구현체
EXS 대외연계 기본 구현체
권한/감사/마스킹/다운로드 적용 예
타 모듈 Facade 연동
```

## M8. ADM 운영 콘솔 보강

```text
browser click 검증
UI 메뉴 이동
거래 상세
헤더 snapshot 조회
확장 헤더 표시
오류 로그 상세
감사 로그 상세
배치 worker/heartbeat/ghost 조회
operator action 버튼
다운로드 감사
마스킹 감사
cache/policy/security 상태
```

## M9. 표준 헤더 Runtime E2E

```text
실제 API 호출
표준 헤더 수신
필수 헤더 검증
TransactionContext 생성
거래 로그 저장
ADM 조회
outbound 호출
mock downstream 서버에서 헤더 수신
확장 헤더 전파 확인
민감 헤더 원문 로그 금지 확인
```

## M10. DB/Flyway/all_install 신규 MariaDB 검증

```text
split SQL 정합성
Flyway migration 정합성
all_install 정합성
신규 빈 MariaDB 설치
seed data
smoke SQL
idempotent 가능 범위
migration 누락 확인
```

## M11. Redis/Kafka/MQ / 다중 인스턴스 검증

```text
Redis 실연동
Kafka/MQ 실연동 후보
다중 인스턴스 cache refresh
로그 정책 전파
broker 장애 fallback
retry/reprocess
운영 로그/감사 로그
```

## M12. 고급 운영/확장 기능 단계

```text
API versioning
outbox/event
workflow/approval
policy engine
multi-tenancy
rate limit/quota
observability/SLO
fault injection
performance benchmark
data retention
privacy classification
encryption/key management
feature flag
developer CLI/code generator
upgrade/migration guide
API/module catalog
operations runbook
```

## M13. 최종 문서 정본화

```text
md 원본 정리
README 정본화
개발가이드
운영가이드
표준헤더가이드
DB/SQL가이드
ADM 가이드
BAT 가이드
EDU/XYZ 가이드
EXS/전문처리 가이드
장애 대응 가이드
배포/환경 가이드
release/upgrade guide
PDF/HTML/DOCX 배포본 생성
```

---

# 9. Codex 작업 기준

Codex는 매 작업 시작 시 이 문서를 확인한다.

작업 기준:

```text
현재 요청서의 목적을 먼저 수행한다.
요청서에 없더라도 CPF 최종 목표에 필요한 보강은 선제적으로 수행한다.
선제 보강은 완료 보고에 이유와 검증 결과를 기록한다.
실행하지 않은 검증은 미검증으로 기록한다.
실패는 숨기지 않고 실패로 기록한다.
문서 포맷보다 실제 구현과 검증을 우선한다.
모든 구현은 확장성/변경 가능성을 고려한다.
기능별 상태는 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요 중 하나로 기록한다.
```

---

# 10. 최종 품질 기준

CPF가 실무 적용 가능한 운영형 표준 프레임워크로 인정받으려면 아래를 만족해야 한다.

```text
개발자가 EDU/XYZ를 복사해 실제 신규 기능을 만들 수 있다.
운영자가 ADM에서 장애 원인과 조치 상태를 확인할 수 있다.
표준 헤더와 확장 헤더가 runtime에서 수신/검증/로그/전파/ADM 조회된다.
권한, 감사, 마스킹, 다운로드 통제가 실제 runtime에서 동작한다.
BAT worker가 독립 실행되고 heartbeat/ghost/lock/job/step이 관제된다.
center-cut이 대량 처리 표준으로 동작한다.
EXS가 REST/고정길이 전문 연계를 모두 표준 처리한다.
신규 빈 DB 설치가 성공한다.
smoke, qualityGate, OpenAPI, browser click, DB 검증 증적이 있다.
실패/미검증을 숨기지 않는다.
문서와 실제 구현이 일치한다.
확장성/변경 가능성을 고려한 interface/adapter/spec 구조가 있다.
개발자 경험, 운영자 경험, 보안 기준이 모두 고려되어 있다.
운영 장애 대응 runbook이 있다.
버전업, 호환성, migration 기준이 있다.
성능, 장애, 보존, 보안, 확장 목표가 명시되어 있다.
```

CPF의 최종 목표는 단순히 기능이 많은 프레임워크가 아니다.

CPF의 최종 목표는 다음이다.

```text
개발자가 신뢰할 수 있는 프레임워크
운영자가 사용할 수 있는 프레임워크
실제 프로젝트에 적용 가능한 프레임워크
장애와 변경에 강한 프레임워크
확장 가능한 프레임워크
문서와 구현이 일치하는 프레임워크
```

---

# 11. 20260703 보강 기준 — 복합 거래 Trace / ADM 거래 그룹 조회 / 운영 검색조건 상세

> 이 섹션은 기존 `6.2 표준 헤더 / 거래 Context`, `6.6 로그 / 감사 / 추적성`, `6.10 외부 연계 / EXS`, `6.22 ADM 운영 콘솔`, `6.23 EDU/XYZ 실전 샘플`, `8. 마일스톤 계획`을 보강한다.  
> 이 기준은 이후 요청서와 검수에서 우선 적용한다.  
> 특히 ADM 거래 조회 기능은 처음부터 목록/검색/상세/마스킹/권한/감사/수행시간/헤더 검색을 포함해 설계해야 하며, 나중에 덧붙이는 방식으로 만들지 않는다.

## 11.1 현재 최신 확인 상태 기준

현재 최신 확인 commit 기준은 `004cc44 / 20260703_01`이다.

완료/완료권으로 확인된 범위:

```text
CPF_FINAL_TARGET_REQUIREMENTS.md 최상위 기준서 편입
표준 헤더/확장 헤더 X-Cpf-Ext-* 구조
MariaDB full install 실검증
표준 헤더 Runtime E2E
center-cut PFW/BAT 소유권 보정
XYZ 업무 DB 기반 center-cut adapter
ADM Center-Cut 관제 API/UI/runtime smoke
XYZ EDU DB 기반 CRUD/validation/search/sort/paging/fixture/DB slice
ADM runtime smoke -BuildBeforeRun
ADM permission runtime smoke
CMN fixed-length 전문 skeleton
```

아직 미구현/부분 구현/미검증으로 남은 주요 범위:

```text
복합 거래 trace 표준
ACC/MBR/EXS 복합 거래 샘플
transactionGlobalId 기준 ADM 거래 그룹 목록/상세/timeline 조회
ADM 거래 목록/검색/상세의 표준 헤더 및 확장 헤더 검색조건
CMN fixed-length 전문 엔진 완성
EXS REST/고정길이 전문 송수신과 ADM 송수신 조회
ADM browser click
최신 기준 전체 MariaDB full install 회귀
Redis/Kafka/MQ broker 실연동
```

---

# 12. 복합 거래 Trace 표준 상세

## 12.1 목적

CPF는 단일 API 로그를 남기는 프레임워크가 아니라, 하나의 업무 거래가 여러 주제영역을 지나며 처리되는 전체 흐름을 운영자가 추적할 수 있는 프레임워크여야 한다.

필수 목표:

```text
transactionGlobalId 하나로 전체 거래 검색
구간별 transactionSegmentId 관리
parentSegmentId 기반 거래 트리 구성
MAIN / SUB / SHARED / EXTERNAL 등 거래 역할 구분
ACC / MBR / EXS / CMN / BAT / ADM / XYZ 모듈 구간 식별
전체 수행시간과 구간별 수행시간 측정
실패 구간과 실패 사유 식별
ADM에서 거래 그룹 목록/상세/timeline 조회
표준/확장 헤더 snapshot과 거래 segment 연결
외부 연계 송수신 로그와 업무 거래 연결
민감 헤더/민감 payload 원문 노출 금지
```

## 12.2 필수 거래 식별자

| 항목 | 의미 | 필수 여부 | 비고 |
|---|---|---:|---|
| transactionGlobalId | 전체 업무 거래 묶음 ID | 필수 | 모든 segment에서 동일 |
| rootTransactionGlobalId | 최초 시작 거래 ID | 권장 | 비동기/재처리 확장 대비 |
| parentTransactionGlobalId | 부모 업무 거래 ID | 권장 | center-cut child, async event 대비 |
| transactionSegmentId | 현재 구간 ID | 필수 | 구간별 고유값 |
| parentSegmentId | 상위 구간 ID | 필수 | timeline/tree 구성 |
| sequenceNo | 거래 내 발생 순번 | 필수 | 목록/상세 정렬 기준 |
| callDepth | 호출 깊이 | 필수 | tree indentation 기준 |
| transactionRole | MAIN/SUB/SHARED/EXTERNAL 등 | 필수 | 업무 의미 구분 |
| direction | INBOUND/OUTBOUND/INTERNAL/RESPONSE | 필수 | 호출 방향 구분 |
| moduleCode | 현재 처리 모듈 | 필수 | ACC/MBR/EXS 등 |
| sourceModuleCode | 호출 주체 모듈 | 필수 | ACC→MBR에서 ACC |
| targetModuleCode | 호출 대상 모듈 | 필수 | ACC→MBR에서 MBR |
| apiPath | API path 또는 연계 path | 권장 | ADM 검색/상세 기준 |
| transactionName | 거래명 | 권장 | 운영자 표시명 |
| startedAt | 구간 시작시각 | 필수 | 수행시간 산정 |
| endedAt | 구간 종료시각 | 필수 | 수행시간 산정 |
| durationMs | 구간 수행시간 | 필수 | slow 구간 찾기 |
| status | 구간 상태 | 필수 | SUCCESS/FAILED/RUNNING 등 |
| failureYn | 실패 여부 | 필수 | 빠른 필터 |
| failureCode | 실패 코드 | 권장 | 오류 코드 검색 |
| failureMessageMasked | 실패 메시지 마스킹 | 권장 | 원문 민감정보 금지 |
| externalInstitutionCode | 외부기관 코드 | EXS 필수 | EXS 연계 검색 |
| externalTransactionId | 외부 거래 ID | EXS 권장 | 대외 거래 추적 |
| traceId/spanId | 기술 trace 식별자 | 권장 | OpenTelemetry/로그 연계 |

## 12.3 transactionRole 표준

| Role | 의미 | 예시 |
|---|---|---|
| MAIN | 최초 유입 또는 업무 주거래 | ACC 고객 거래 시작 |
| SUB | MAIN 처리 중 내부 하위 거래 | ACC가 MBR 조회 호출 |
| SHARED | 공통/공유 조회 성격 거래 | MBR 회원 조회, CMN 코드 조회 |
| EXTERNAL | 외부기관/외부 API/전문 송수신 | EXS 기관 송신 |
| BATCH | 배치 job/step 거래 | BAT job 실행 |
| CENTER_CUT_PARENT | center-cut 전체 job 거래 | 대량 처리 parent |
| CENTER_CUT_CHILD | center-cut item 1건 거래 | 대량 처리 child item |
| OPERATOR_ACTION | 운영자 조치 거래 | lock 강제해제, 재실행 요청 |
| SYSTEM | 시스템 내부 관리 거래 | heartbeat, health |

기준:

```text
MAIN은 transactionGlobalId를 시작하는 구간이다.
SUB는 MAIN 또는 다른 SUB의 하위 업무 구간이다.
SHARED는 업무 주거래는 아니지만 원인 분석에 필요한 공유 서비스 구간이다.
EXTERNAL은 EXS 또는 외부기관 호출 구간이다.
CENTER_CUT_PARENT/CHILD는 transactionGlobalId parent/child 흐름과 segment 흐름을 함께 남긴다.
OPERATOR_ACTION은 운영자 ID, 권한, 사유, 결과, 감사 로그와 연결해야 한다.
```

## 12.4 direction 표준

| Direction | 의미 |
|---|---|
| INBOUND | 현재 모듈이 요청을 수신한 구간 |
| OUTBOUND | 현재 모듈이 다른 모듈/외부로 요청을 송신한 구간 |
| INTERNAL | 모듈 내부 처리 구간 |
| RESPONSE | 응답 반환 구간 |
| ASYNC_PUBLISH | 이벤트/메시지 발행 구간 |
| ASYNC_CONSUME | 이벤트/메시지 소비 구간 |
| BATCH_STEP | 배치 step 처리 구간 |

## 12.5 필수 복합 거래 샘플

### 패턴 A — ACC orchestration

```text
ACC MAIN inbound
  → ACC OUTBOUND to MBR
    → MBR SHARED/SUB inbound
    ← MBR response
  → ACC resume/internal
  → ACC OUTBOUND to EXS
    → EXS EXTERNAL send/receive
    ← EXS response
← ACC final response
```

목표:

```text
ACC가 전체 거래의 주체다.
MBR은 회원 조회/검증 shared 또는 sub 거래다.
EXS는 외부기관 호출 external 거래다.
transactionGlobalId는 모든 구간에서 동일하다.
segmentId/parentSegmentId/sequenceNo/callDepth로 흐름이 재구성된다.
ADM 목록에서 moduleFlowText = ACC → MBR → EXS 로 표시된다.
```

### 패턴 B — nested delegation

```text
ACC MAIN inbound
  → ACC OUTBOUND to MBR
    → MBR SUB inbound
      → MBR OUTBOUND to EXS
        → EXS EXTERNAL send/receive
        ← EXS response
    ← MBR response
← ACC response
```

목표:

```text
ACC가 시작했지만 EXS 호출은 MBR 내부에서 발생한다.
ADM timeline에서 MBR 하위에 EXS 구간이 보인다.
실패 시 실패 구간이 MBR인지 EXS인지 명확히 구분된다.
```

### 패턴 C — shared + external 혼합

```text
ACC MAIN 거래
  → MBR SHARED 회원 조회
  → CMN SHARED 코드/정책 조회
  → EXS EXTERNAL 외부기관 호출
```

목표:

```text
메인 거래와 공유 조회를 구분한다.
공유 조회 실패가 전체 실패인지 부분 실패인지 정책으로 구분한다.
운영자가 shared 구간의 지연도 확인할 수 있다.
```

## 12.6 구현 위치 기준

```text
PFW: segment/role/direction 표준, context, propagation, log contract
ACC: 최초 MAIN 거래 샘플, orchestration 샘플
MBR: 회원 조회/검증 SHARED/SUB 샘플
CMN: 코드/정책 SHARED 샘플 후보
EXS: REST/전문 EXTERNAL 샘플, 송수신 로그
ADM: transactionGlobalId 그룹 목록/상세/timeline 조회
EDU/XYZ: 개발자가 복사 가능한 복합 거래 예제와 smoke
```

금지:

```text
ACC가 MBR 테이블 직접 조회 금지
MBR이 EXS 테이블 직접 조회 금지
EXS 송수신 원문 민감정보 저장 금지
표준 헤더 수동 하드코딩 금지
transactionGlobalId만 같고 segment/role/duration이 없는 구조 금지
```

---

# 13. ADM 거래 그룹 조회 상세 기준

## 13.1 핵심 원칙

ADM 거래 로그 목록은 단건 로그 나열이 아니라 `transactionGlobalId` 기준 거래 그룹 목록이어야 한다.

목표:

```text
운영자가 거래ID 하나로 전체 흐름을 찾을 수 있다.
목록에서 시작/종료/상태/전체 소요시간/실패 구간을 바로 볼 수 있다.
상세에서 ACC→MBR→EXS 같은 호출 흐름을 timeline으로 볼 수 있다.
표준 헤더와 확장 헤더 snapshot을 볼 수 있다.
외부기관 송수신 로그를 같은 화면에서 연결 조회할 수 있다.
민감 헤더와 민감 payload는 원문 노출되지 않는다.
검색조건은 테이블과 header snapshot에 존재하는 운영 조회 가치 컬럼을 선제 분류한다.
```

## 13.2 ADM 거래 그룹 메뉴 구조

권장 메뉴:

```text
ADM
└─ 거래 관제
   ├─ 거래 그룹 조회
   ├─ 거래 상세/timeline
   ├─ 오류 거래 조회
   ├─ 지연 거래 조회
   ├─ 외부 연계 거래 조회
   ├─ 표준/확장 헤더 조회
   └─ 거래 조회 감사
```

배치/center-cut/전문/외부연계는 별도 메뉴가 있더라도 transactionGlobalId로 거래 그룹 조회와 연결되어야 한다.

## 13.3 거래 그룹 목록 API

후보 API:

```text
GET /adm/api/transaction-groups
```

역할:

```text
transactionGlobalId 기준으로 거래를 그룹화해 목록 조회한다.
segment 단건 로그가 여러 건 있어도 목록에서는 거래 1건으로 보인다.
검색조건/정렬/페이징은 운영 조회 기준으로 제공한다.
```

필수 request parameter 후보:

```text
fromDateTime
fromDateTimeTo 또는 toDateTime
transactionGlobalId
transactionSegmentId
status
failureYn
failedModuleCode
failedSegmentId
transactionRole
moduleCode
includedModuleCode
sourceModuleCode
targetModuleCode
direction
customerNo
memberNo
userId
operatorId
channelCode
originalChannelCode
clientIp
institutionCode
externalInstitutionCode
externalTransactionId
apiPath
transactionName
errorCode
durationMsFrom
durationMsTo
slowOnly
externalOnly
centerCutOnly
batchOnly
keyword
page 또는 cursor
size
sort
```

검색조건 설계 기준:

```text
테이블에 컬럼이 있고 운영자가 장애 분석에 사용할 수 있으면 검색 후보로 분류한다.
표준 헤더 snapshot에 있는 주요 값은 검색 후보로 분류한다.
확장 헤더는 보안 정책에 맞춰 제한 검색한다.
민감 헤더는 원문 검색/표시를 금지한다.
```

필수 목록 response field:

```text
transactionGlobalId
transactionName
apiPath
originModuleCode
moduleFlowText                  예: ACC → MBR → EXS
rolesText                       예: MAIN / SHARED / EXTERNAL
segmentCount
externalCallCount
overallStatus
failureYn
failedModuleCode
failedSegmentId
failedSegmentName
failureCode
failureMessageMasked
startedAt
endedAt
totalDurationMs
slowestSegmentId
slowestModuleCode
slowestDurationMs
customerNoMasked
memberNoMasked
userIdMasked
operatorId
channelCode
originalChannelCode
clientIpMasked
externalInstitutionCode
externalTransactionId
lastErrorAt
createdAt
updatedAt
```

목록 UI 필수 표시 컬럼:

```text
거래ID
거래명/API
최초 모듈
호출 흐름
상태
실패 여부
실패 구간
시작시간
종료시간
전체 소요시간
최장 지연 구간
고객번호/회원번호 또는 마스킹 값
채널/최초채널
외부기관/외부거래ID
```

목록 UI 권장 기능:

```text
기본 정렬: startedAt desc
상태 quick filter: 전체/성공/실패/부분실패/처리중
지연 quick filter: 1초 이상/3초 이상/5초 이상/10초 이상
외부연계 quick filter
실패 거래만 보기
transactionGlobalId 복사 버튼
상세 이동 버튼
검색조건 초기화
검색조건 접기/펼치기
컬럼 표시 설정 후보
```

## 13.4 거래 그룹 상세 API

후보 API:

```text
GET /adm/api/transaction-groups/{transactionGlobalId}
```

필수 response field:

```text
transactionGlobalId
rootTransactionGlobalId
transactionName
originModuleCode
moduleFlowText
overallStatus
failureYn
failedSegmentId
failedModuleCode
failureCode
failureMessageMasked
startedAt
endedAt
totalDurationMs
segmentCount
externalCallCount
batchYn
centerCutYn
operatorActionYn
customerNoMasked
memberNoMasked
userIdMasked
operatorId
channelCode
originalChannelCode
clientIpMasked
requestId
externalRequestId
institutionCode
externalTransactionId
summaryMessage
```

상세 화면 섹션:

```text
1. 전체 요약
2. 거래 흐름 timeline
3. 구간별 수행시간
4. 표준 헤더 snapshot
5. 확장 헤더 snapshot
6. 요청/응답 요약
7. 오류 상세
8. EXS 외부연계 송수신 로그
9. 배치/center-cut child 연결
10. 감사 로그
11. 운영자 조치 이력
```

## 13.5 Segment 목록 API

후보 API:

```text
GET /adm/api/transaction-groups/{transactionGlobalId}/segments
```

필수 field:

```text
transactionSegmentId
parentSegmentId
sequenceNo
callDepth
transactionRole
direction
moduleCode
sourceModuleCode
targetModuleCode
apiPath
transactionName
status
failureYn
failureCode
failureMessageMasked
startedAt
endedAt
durationMs
requestSummaryMasked
responseSummaryMasked
standardHeaderSnapshotId
extensionHeaderSnapshotId
externalLogId
batchExecutionId
centerCutJobId
centerCutItemId
traceId
spanId
parentSpanId
```

UI 기준:

```text
segment 목록은 sequenceNo 순서로 표시한다.
callDepth에 따라 들여쓰기 또는 tree 구조로 표시한다.
실패 구간은 강조한다.
가장 오래 걸린 구간은 별도 표시한다.
EXTERNAL 구간은 기관/endpoint/응답코드/소요시간을 같이 표시한다.
SHARED 구간은 공통 조회임을 표시한다.
```

## 13.6 Timeline API

후보 API:

```text
GET /adm/api/transaction-groups/{transactionGlobalId}/timeline
```

응답 구조 후보:

```text
transactionGlobalId
overallStatus
totalDurationMs
nodes[]
  transactionSegmentId
  parentSegmentId
  label
  moduleCode
  transactionRole
  direction
  status
  durationMs
  startedAt
  endedAt
  failureYn
  children[]
```

표시 예:

```text
[ACC] MAIN inbound / 120ms
  └─ [ACC→MBR] OUTBOUND / 80ms
      └─ [MBR] SHARED member lookup / 45ms
          └─ [MBR→EXS] EXTERNAL / 850ms
              └─ [EXS] response / 20ms
  └─ [ACC] final response / 30ms
```

## 13.7 표준 헤더 조회/검색

후보 API:

```text
GET /adm/api/transaction-groups/{transactionGlobalId}/headers
GET /adm/api/transaction-groups/{transactionGlobalId}/segments/{segmentId}/headers
```

표준 헤더 검색 후보:

```text
X-Transaction-Id 또는 transactionGlobalId
X-Request-Id
X-External-Request-Id
X-User-Id
X-Customer-No
X-Member-No
X-Operator-Id
X-Channel-Code
X-Original-Channel-Code
X-Client-IP
X-Institution-Code
X-External-Transaction-Id
traceparent
tracestate
correlationId
callerService
```

표준 헤더 snapshot 표시 기준:

```text
inbound header
resolved header
outbound header
response header
각각의 생성/변경 시점
마스킹 적용 여부
차단/누락/검증 실패 항목
```

주의:

```text
Authorization 원문 표시 금지
Cookie/Set-Cookie 원문 표시 금지
X-Api-Key 원문 표시 금지
token/secret/password/credential/signature 계열 원문 표시 금지
```

## 13.8 확장 헤더 조회/검색

확장 헤더 기준:

```text
X-Cpf-Ext-1 ~ X-Cpf-Ext-5
X-Cpf-Ext-{Key}
```

검색 후보:

```text
extHeaderKey
extHeaderValueMasked 또는 exact value hash
X-Cpf-Ext-1
X-Cpf-Ext-2
X-Cpf-Ext-3
X-Cpf-Ext-4
X-Cpf-Ext-5
```

설계 기준:

```text
확장 헤더는 기본 OPTIONAL이다.
확장 헤더를 무조건 마스킹하지 않는다.
단, key 이름이나 value가 token/auth/api-key/secret/password/credential/signature 계열이면 원문 저장/전파/검색 금지 또는 마스킹한다.
검색 가능한 확장 헤더와 표시 가능한 확장 헤더를 정책으로 분리한다.
value 전체 인덱싱이 부담되면 key + hash + masked value 구조를 고려한다.
```

## 13.9 외부 연계 로그 연결

후보 API:

```text
GET /adm/api/transaction-groups/{transactionGlobalId}/external-logs
GET /adm/api/external-logs
GET /adm/api/external-logs/{externalLogId}
```

필수 field:

```text
externalLogId
transactionGlobalId
transactionSegmentId
institutionCode
endpointId
externalTransactionId
messageCode
method
urlMasked
requestStartedAt
responseEndedAt
durationMs
requestStatus
responseStatus
httpStatus
externalResponseCode
externalResponseMessageMapped
retryCount
timeoutMs
failureCode
failureMessageMasked
requestHeaderMasked
responseHeaderMasked
requestPayloadMasked
responsePayloadMasked
```

기준:

```text
EXS 송수신 로그는 transactionGlobalId/segmentId와 연결한다.
외부기관 응답코드는 CPF 오류코드와 매핑한다.
원문 전문/원문 payload는 권한 없이 노출하지 않는다.
전문 원문 조회가 필요하면 별도 권한/감사/마스킹 해제 정책이 필요하다.
```

## 13.10 권한/마스킹/감사

ADM 거래 조회 권한 후보:

```text
TRANSACTION_READ
TRANSACTION_DETAIL_READ
TRANSACTION_HEADER_READ
TRANSACTION_PAYLOAD_READ
TRANSACTION_PAYLOAD_UNMASK
EXTERNAL_LOG_READ
EXTERNAL_PAYLOAD_READ
EXTERNAL_PAYLOAD_UNMASK
TRANSACTION_DOWNLOAD
```

마스킹 기준:

```text
목록은 기본 마스킹
상세도 기본 마스킹
원문 조회는 기본 금지
마스킹 해제는 별도 권한 + 사유 입력 + 감사 로그
다운로드는 별도 권한 + 다운로드 감사
```

감사 로그 필수 후보:

```text
거래 상세 조회 감사 후보
민감 헤더 조회 감사
민감 payload 조회 감사
마스킹 해제 감사
다운로드 감사
운영자 조치 감사
검색조건 포함 여부 후보
조회 대상 transactionGlobalId
운영자 ID
조회 시각
IP
사유
```

## 13.11 페이징/정렬/성능

목록 조회 기준:

```text
기본 page size 제한
최대 page size 제한
기본 정렬 startedAt desc
sort whitelist 필수
무제한 조회 금지
기간 조건 없는 대량 조회 제한
대량 다운로드는 별도 job 또는 제한 기준 필요
```

권장 index 후보:

```text
transactionGlobalId
startedAt
status
failureYn
moduleCode
sourceModuleCode
targetModuleCode
transactionRole
customerNo/memberNo/userId/operatorId
channelCode/originalChannelCode
externalInstitutionCode/externalTransactionId
durationMs
apiPath
```

헤더 검색 index 후보:

```text
transactionGlobalId
headerName
headerValueHash
headerValueMasked prefix 후보
segmentId
snapshotType
createdAt
```

성능 기준:

```text
목록 화면은 그룹 집계 기준으로 timeout이 나지 않아야 한다.
상세는 transactionGlobalId 단건 기준으로 segment/header/external log를 조회한다.
대량 기간 검색은 size 제한과 기간 제한을 둔다.
slow transaction 조회는 durationMs index를 고려한다.
```

## 13.12 ADM UI 완료 기준

ADM 거래 그룹 조회 UI는 아래가 있어야 완료권이다.

```text
거래 그룹 메뉴 진입점
검색 영역
목록 테이블
상세 이동
timeline 표시
segment 상세 표시
header snapshot 표시
실패 구간 표시
전체/구간별 소요시간 표시
민감정보 마스킹 표시
권한 없음/조회 실패 표준 오류 처리
정적 marker smoke
가능하면 browser click smoke
```

미완료/부분 구현 기준:

```text
API만 있고 UI 없음 → 부분 구현
UI 정적 영역만 있고 API 연결 없음 → 부분 구현
목록은 있으나 transactionGlobalId 그룹이 아니라 단건 로그 나열 → 부분 구현 또는 실패
시작/종료/상태/소요시간/실패구간 없음 → 완료 불인정
검색조건 없이 전체 목록만 조회 → 완료 불인정
헤더 snapshot이 상세와 연결되지 않음 → 부분 구현
민감 헤더 원문 노출 가능성 있음 → 실패
```

---

# 14. ADM 운영 기능 전체 보강 목록

ADM은 CPF에서 가장 중요한 운영 접점이다. 다음 기능을 장기 목표로 관리한다.

## 14.1 거래 관제

```text
거래 그룹 목록
거래 상세
segment timeline
구간별 수행시간
표준 헤더 snapshot
확장 헤더 snapshot
오류 상세
외부 연계 로그
전문 송수신 로그
감사 로그 연결
운영자 조치 이력
slow transaction 조회
failed transaction 조회
partial failed 조회
running/stuck transaction 후보
```

## 14.2 오류 관제

```text
오류 목록
오류 상세
오류코드 검색
거래ID 연결
module 검색
API path 검색
발생 시간 검색
처리 상태
조치자
조치 내용
재처리 가능 여부
오류 빈도 통계 후보
```

## 14.3 감사 관제

```text
로그인 감사
권한 변경 감사
API 권한 변경 감사
메뉴/버튼 권한 변경 감사
마스킹 해제 감사
다운로드 감사
운영자 조치 감사
배치 조치 감사
전문 원문 조회 감사
민감 헤더 조회 감사
```

## 14.4 권한 관제

```text
운영자 목록
역할 목록
메뉴 권한
버튼 권한
API 권한
다운로드 권한
마스킹 권한
고위험 기능 권한
권한 테스트 smoke
ADM_ADMIN/ADM_VIEWER 기준 runtime 검증
```

## 14.5 배치/BAT 관제

```text
worker 목록
worker 상세
heartbeat 상태
ghost candidate
job execution 목록
step execution 목록
parameter 조회
lock 조회
stale lock
중지 요청
재실행 요청
강제 실행 요청
operator action audit
```

## 14.6 Center-Cut 관제

```text
center-cut job 목록
parameter 조회
target 조회
result 조회
summary count
ready/running/success/failed/skipped/retry/stop 상태
parent/child transactionGlobalId
item별 실패 사유
payload masking
운영자 조치 후보
```

## 14.7 EXS/전문 관제

```text
기관 목록
endpoint 목록
송신 로그
수신 로그
전문 송신 로그
전문 수신 로그
timeout/retry 이력
응답 코드 매핑
외부거래ID 검색
기관코드 검색
messageCode 검색
payload masking
전문 원문 조회 권한/감사 후보
```

## 14.8 Cache/Policy/Security 관제

```text
cache 상태
cache refresh
정책 목록
정책 상세
정책 변경 이력
보안 설정 조회
secret 노출 점검 후보
로그 정책 조회
마스킹 정책 조회
feature flag 후보
```

---

# 15. SQL/DB 보강 기준 — ADM 거래 조회 중심

## 15.1 거래 그룹 저장 구조 후보

선호 구조:

```text
pfw_transaction_group
pfw_transaction_segment
pfw_transaction_header_snapshot
pfw_transaction_extension_header_snapshot
pfw_transaction_error
exs_external_exchange_log
adm_operator_action_log
```

단, 기존 테이블이 있으면 기존 구조를 최대한 살리고 migration으로 확장한다.

## 15.2 pfw_transaction_group 후보 컬럼

```text
transaction_global_id PK
root_transaction_global_id
transaction_name
api_path
origin_module_code
module_flow_text
overall_status
failure_yn
failed_segment_id
failed_module_code
failure_code
failure_message_masked
started_at
ended_at
total_duration_ms
segment_count
external_call_count
customer_no_masked
member_no_masked
user_id_masked
operator_id
channel_code
original_channel_code
client_ip_masked
external_institution_code
external_transaction_id
created_at
updated_at
```

## 15.3 pfw_transaction_segment 후보 컬럼

```text
transaction_segment_id PK
transaction_global_id
parent_segment_id
sequence_no
call_depth
transaction_role
direction
module_code
source_module_code
target_module_code
api_path
transaction_name
status
failure_yn
failure_code
failure_message_masked
started_at
ended_at
duration_ms
trace_id
span_id
parent_span_id
standard_header_snapshot_id
extension_header_snapshot_id
external_log_id
batch_execution_id
center_cut_job_id
center_cut_item_id
created_at
updated_at
```

## 15.4 header snapshot 후보 컬럼

```text
snapshot_id PK
transaction_global_id
transaction_segment_id
snapshot_type       INBOUND / RESOLVED / OUTBOUND / RESPONSE
header_name
header_value_masked
header_value_hash
sensitive_yn
blocked_yn
created_at
```

주의:

```text
header value 원문 저장 금지
검색이 필요한 값은 hash 또는 normalized value 정책 검토
민감 헤더는 원문/검색값 모두 제한
```

---

# 16. 다음 요청서 우선순위 보강

다음 요청서는 아래를 필수 축으로 삼는다.

```text
1. PFW 복합 거래 trace 표준 최소 구현
2. ACC/MBR/EXS 복합 거래 샘플 최소 1개 runtime smoke
3. transactionGlobalId 기준 ADM 거래 그룹 목록/상세/timeline API
4. ADM 목록 필수 필드: 시작시간, 종료시간, 상태, 소요시간, 실패 구간, 호출 흐름
5. ADM 검색조건: 테이블/header snapshot 기반 운영 조회 가치 컬럼 선제 분류
6. 표준 헤더/확장 헤더 검색과 마스킹 정책
7. 구간별 수행시간과 slow/failure segment 표시
8. CMN fixed-length 전문 엔진 보강은 착수/보강 범위
9. EXS 송수신 로그 연결은 후보 설계 또는 부분 구현
```

완료 기준:

```text
transactionGlobalId 하나로 전체 복합 거래가 조회된다.
segmentId/parentSegmentId/role/direction/module/duration이 기록된다.
ADM 거래 그룹 목록이 단건 로그 나열이 아니라 그룹 조회다.
목록에 시작/종료/상태/전체 소요시간/실패 구간이 보인다.
검색조건/정렬/페이징이 있다.
표준 헤더 주요 필드가 검색/상세 후보로 반영된다.
민감 헤더/민감 payload 원문 노출이 없다.
Runtime smoke와 ADM smoke가 있다.
실행하지 않은 검증은 미검증으로 기록한다.
```

완료 불인정:

```text
transactionGlobalId만 있고 segment/timeline 없음
목록이 로그 단건 나열임
수행시간/시작/종료/상태/실패구간 없음
검색조건이 부족하거나 header 검색이 없음
sort whitelist 없음
민감 헤더 원문 노출 가능
ACC/MBR/EXS 복합 거래 샘플 runtime 검증 없음
ADM UI/API 상태를 완료로 과장
SQL/Flyway/all_install 누락
리포트/기능 매트릭스 불일치
```

---

# 17. 최종 목표 추가 후보 — 기존 문서에 더 보강할 기능

아래는 기존 기준서에 더 명시하면 좋은 후보들이다.

## 17.1 Transaction Analytics 후보

```text
거래 처리량 추이
평균/최대/percentile 소요시간
모듈별 오류율
기관별 timeout 건수
API별 slow ranking
실패 구간 ranking
center-cut 실패 item ranking
batch 실패 job ranking
```

초기에는 통계 화면까지 구현하지 않아도, 로그 구조는 통계 확장이 가능해야 한다.

## 17.2 운영자 저장 검색조건 후보

```text
운영자 개인별 저장 검색조건
공용 검색조건
즐겨찾기 필터
최근 검색조건
실패 거래 quick filter
slow 거래 quick filter
외부연계 거래 quick filter
```

## 17.3 Alert/Notification 연계 후보

```text
slow transaction threshold 알림
EXS timeout 알림
batch ghost 알림
lock stale 알림
center-cut failed threshold 알림
broker 장애 알림
ADM 알림 이력 조회
```

## 17.4 SLO/SLA 후보

```text
API별 목표 응답시간
기관별 timeout 기준
batch job 목표 완료시간
center-cut 처리량 기준
오류율 기준
SLO 위반 로그
ADM SLO 위반 조회 후보
```

## 17.5 Data Export 후보

```text
거래 목록 다운로드
오류 목록 다운로드
감사 로그 다운로드
EXS 송수신 목록 다운로드
개인정보 포함 여부 표시
다운로드 권한
다운로드 사유
다운로드 감사
대량 다운로드 비동기 job 후보
```

## 17.6 운영 조치 후보

```text
거래 재처리 요청 후보
EXS 재송신 요청 후보
batch 재실행
center-cut item retry
lock 강제 해제
ghost 확정 처리
cache refresh
policy reload
마스킹 해제 승인
2인 승인
운영자 조치 감사
```

## 17.7 Security Hardening 후보

```text
관리자 IP 제한 후보
MFA 후보
권한 상승 감사
휴면 운영자 잠금
비밀번호 정책
세션 timeout
동시 로그인 정책
API 권한 누락 점검
민감정보 스캔 smoke 후보
```

---

# 18. 검수자가 반드시 확인할 항목

검수 시 아래를 빠뜨리지 않는다.

```text
Codex 보고와 실제 GitHub master 일치 여부
작업 범위 관련 파일 전수 확인
Controller-Service-Repository/Mapper-DTO-SQL-Test 연결
SQL split/Flyway/all_install/smoke 정합성
기능 매트릭스 상태값 과장 여부
CPF_STABILIZATION_REPORT.md와 실제 파일 일치 여부
evidence 경로 실제 존재 여부
실행한 검증과 미실행 검증 분리
Runtime smoke 로그 또는 결과 파일 존재 여부
민감정보 원문 기록 여부
ADM UI가 API와 실제 연결되는지 여부
거래 목록이 그룹 조회인지 단건 나열인지 여부
검색조건과 목록 필드가 운영 가치 컬럼을 반영했는지 여부
표준/확장 헤더 검색과 마스킹이 반영됐는지 여부
```

---

# 19. 새 세션 인수인계 요약

새 세션에서 이어갈 때는 아래 요약을 우선 적용한다.

```text
최신 확인 commit: 004cc44 / 20260703_01
완료권: 기준서, 표준/확장 헤더, MariaDB full install, standard-header E2E, center-cut 소유권, XYZ center-cut adapter, ADM center-cut 관제, XYZ EDU DB CRUD, ADM BuildBeforeRun/permission runtime, CMN fixed-length skeleton
다음 핵심: 복합 거래 trace 표준 + ACC/MBR/EXS 샘플 + ADM transactionGlobalId 그룹 목록/상세/timeline + ADM 검색조건/헤더검색/수행시간/실패구간 + CMN 전문 엔진 보강
요청서 크기: 기존보다 1.5~2배 가능
검증 강도: 낮추지 않음
완료 판정: 실행 증적/파일/SQL/smoke/리포트/매트릭스 기준
```
