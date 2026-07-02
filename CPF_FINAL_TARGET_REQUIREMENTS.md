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
