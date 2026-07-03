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


---

# 20. 20260703+ 최종 목표 대확장 기준 — 운영형 프레임워크 완성도 보강

> 이 섹션 이후는 기존 최종 목표 기준서를 확장한 상세 기준이다.  
> 기존 기준서의 상위 원칙은 유지하되, 실제 운영형 프레임워크라면 처음부터 고려해야 하는 DB 로그, 파일 로그, 동적 로그 레벨, 배치 재수행 로그, 신규 주제영역 온보딩 생성기, 개발자 경험, ADM 운영 콘솔, 검증/evidence 기준을 더 상세하게 정의한다.

## 20.1 확장 기준의 목적

CPF는 기능이 하나씩 붙는 샘플 프로젝트가 아니라, 신규 업무 시스템을 만들 때 반복되는 개발/운영/검증/보안/로그/권한/배치/연계/문서/온보딩 구조를 표준화하는 프레임워크다.

따라서 최종 목표 기준서는 다음 역할을 한다.

```text
1. Codex 작업 요청서의 상위 기준
2. ChatGPT 검수자의 완료 판정 기준
3. 신규 기능 설계 시 누락 방지 체크리스트
4. 운영형 프레임워크의 기능 백로그
5. 개발자/운영자/보안 담당자가 공통으로 보는 기준 문서
6. 구현/테스트/SQL/smoke/evidence/문서 정합성 기준
```

## 20.2 개발 진도와 운영성의 균형 원칙

운영성 요건이 중요하다고 해서 기능 개발이 멈추면 안 된다. 반대로 기능 개발 속도를 위해 로그/감사/권한/검증을 빼면 CPF 목표와 맞지 않는다.

요청서와 마일스톤은 항상 아래 구조를 따른다.

```text
필수 완료 범위: 실제 개발 진도를 내야 하는 기능 구현
공통 완료 기준: 로그/감사/마스킹/SQL/evidence/검증 등 모든 기능에 붙는 운영 기준
보강 범위: 완성도를 높이는 운영/편의 기능
착수 범위: 다음 마일스톤과 연결되는 기반 작업
후순위/제외 범위: 이번에 완료로 강제하지 않는 항목
```

판정 원칙:

```text
운영성 요건은 기능 개발을 막는 별도 작업이 아니라 기능별 완료 기준에 포함한다.
과도한 운영 고도화는 보강/착수/후순위로 분리한다.
필수 완료 범위가 닫히지 않으면 전체 완료로 보지 않는다.
보강/착수 범위를 못 했다고 필수 완료를 실패로 만들지는 않지만, 상태는 분리 기록한다.
```


---

# 21. CPF 로그/감사/추적성 최종 표준

## 21.1 로그 표준의 목표

CPF 로그 표준은 단순히 `log.info()`를 남기는 것이 아니다. 운영자가 장애 시점에 다음 질문에 답할 수 있어야 한다.

```text
어느 거래가 실패했는가?
어느 transactionGlobalId인가?
어느 moduleCode에서 시작되었는가?
어느 segment에서 실패했는가?
어느 이중화 서버/인스턴스가 처리했는가?
어느 서버가 오류 응답을 보냈는가?
어느 외부기관/endpoint에서 지연 또는 실패가 발생했는가?
DB 로그와 파일 로그가 같은 거래를 가리키는가?
ADM에서 보는 timeline과 파일 로그 grep 결과가 일치하는가?
운영자가 동적으로 TRACE를 켰다면 어떤 정책으로, 누가, 얼마 동안 켰는가?
TRACE에서도 민감정보 원문이 남지 않는가?
```

## 21.2 DB 로그와 파일 로그의 역할 분리

| 구분 | 역할 | 주요 사용 시점 |
|---|---|---|
| DB 로그 | ADM 조회, 검색, 통계, 감사, 운영자 조치 이력, 장기 분석 | 정상 운영, 운영 콘솔 조회, 감사 대응 |
| 파일 로그 | 서버 장애 분석, DB 저장 실패 시 보조 증적, 긴급 grep, 인스턴스별 원인 분석 | 장애/긴급 대응, DB unavailable, 현장 서버 로그 확인 |
| sanitized evidence | 검수자가 민감정보 없이 실행 결과 확인 | 리뷰/검수/품질 게이트 |

기준:

```text
DB 로그만 있으면 부분 구현이다.
파일 로그만 있으면 부분 구현이다.
DB 로그와 파일 로그가 transactionGlobalId 기준으로 연결되어야 완료권이다.
ADM 거래 timeline과 파일 로그 grep 결과가 동일 transactionGlobalId/segmentId 기준으로 이어져야 한다.
민감정보 원문이 DB/파일/evidence 어디에도 남으면 실패다.
```

## 21.3 로그 파일명 표준

파일 로그는 단일 `cpf-application.log`에 모든 것을 섞지 않는다. 또한 특정 주제영역만 예외 파일명을 만들지 않는다.

표준 파일명 패턴:

```text
cpf-{moduleCode}-{logType}.log
```

권장 디렉터리 구조:

```text
logs/
  pfw/
    cpf-pfw-application.log
    cpf-pfw-transaction.log
    cpf-pfw-integration.log
    cpf-pfw-audit.log
    cpf-pfw-error.log
  acc/
    cpf-acc-application.log
    cpf-acc-transaction.log
    cpf-acc-integration.log
    cpf-acc-audit.log
    cpf-acc-error.log
  mbr/
    cpf-mbr-application.log
    cpf-mbr-transaction.log
    cpf-mbr-integration.log
    cpf-mbr-audit.log
    cpf-mbr-error.log
  exs/
    cpf-exs-application.log
    cpf-exs-transaction.log
    cpf-exs-integration.log
    cpf-exs-audit.log
    cpf-exs-error.log
  adm/
    cpf-adm-application.log
    cpf-adm-transaction.log
    cpf-adm-integration.log
    cpf-adm-audit.log
    cpf-adm-error.log
  bat/
    cpf-bat-application.log
    cpf-bat-transaction.log
    cpf-bat-integration.log
    cpf-bat-audit.log
    cpf-bat-batch.log
    cpf-bat-error.log
```

금지/지양:

```text
모든 로그를 cpf-application.log 하나에만 남기는 구조 지양
cpf-external.log처럼 EXS 특수 전용 파일명 지양
acc.log, exs.log처럼 logType 없는 단순 모듈 파일명 지양
업무 모듈 서버에 다른 업무 모듈 내부 로그 파일 생성 금지
```

예:

```text
ACC WAS: cpf-pfw-*, cpf-acc-* 로그만 존재
EXS WAS: cpf-pfw-*, cpf-exs-* 로그만 존재
ACC가 EXS를 호출해도 ACC WAS에 cpf-exs-* 파일이 생기면 안 됨
```

## 21.4 logType 표준

| logType | 목적 | 파일 예시 |
|---|---|---|
| application | 일반 애플리케이션 진행 로그 | cpf-acc-application.log |
| transaction | 온라인/배치/운영자 조치의 거래 흐름 로그 | cpf-acc-transaction.log |
| integration | 내부 API/외부기관/REST/전문/MQ/Kafka/Redis 연계 로그 | cpf-exs-integration.log |
| audit | 운영자 조치/권한/마스킹/다운로드/정책 변경 감사 | cpf-adm-audit.log |
| batch | 배치 job/step/item/chunk/worker 실행 로그 | cpf-bat-batch.log |
| error | 예외/실패/장애 분석 로그 | cpf-acc-error.log |
| security | 로그인/권한차단/보안 위반 후보 로그 | cpf-adm-security.log 후보 |
| performance | slow transaction/slow query/timeout 후보 로그 | cpf-pfw-performance.log 후보 |

## 21.5 파일 로그 포맷

권장 포맷은 JSON Lines다. 한 줄이 하나의 JSON event가 되어야 한다.

필수 공통 필드:

```text
timestamp
level
loggerName
threadName
logType
eventType
env
profile
moduleCode
serverId
instanceId
hostName
hostIp
port
processId
containerId
podName
appVersion
buildVersion
transactionGlobalId
transactionSegmentId
parentSegmentId
traceId
spanId
correlationId
apiPath
httpMethod
status
failureCode
failureMessageMasked
durationMs
```

필수 마스킹 필드 후보:

```text
customerNoMasked
memberNoMasked
userIdMasked
operatorIdMasked
clientIpMasked
requestHeaderMasked
responseHeaderMasked
requestPayloadMasked
responsePayloadMasked
```

예시:

```json
{
  "timestamp": "2026-07-03T18:10:22.123+09:00",
  "level": "INFO",
  "logType": "TRANSACTION",
  "eventType": "TRANSACTION_SEGMENT_END",
  "env": "prod",
  "profile": "prod",
  "moduleCode": "ACC",
  "serverId": "ACC-AP01",
  "instanceId": "acc-8080-01",
  "hostName": "acc-ap01",
  "hostIp": "10.10.1.11",
  "port": 8080,
  "appVersion": "20260703_04",
  "transactionGlobalId": "20260703181022001ACC0000001",
  "transactionSegmentId": "ACC-SEG-0002",
  "parentSegmentId": "ACC-SEG-0001",
  "transactionRole": "SUB",
  "direction": "OUTBOUND",
  "sourceModuleCode": "ACC",
  "targetModuleCode": "EXS",
  "apiPath": "/exs/api/transfer",
  "httpMethod": "POST",
  "status": "SUCCESS",
  "durationMs": 128,
  "failureCode": null,
  "failureMessageMasked": null
}
```

## 21.6 이벤트 타입 표준

거래:

```text
TRANSACTION_START
TRANSACTION_END
TRANSACTION_FAIL
TRANSACTION_SEGMENT_START
TRANSACTION_SEGMENT_END
TRANSACTION_SEGMENT_FAIL
TRANSACTION_CONTEXT_CREATED
TRANSACTION_CONTEXT_PROPAGATED
```

연계:

```text
INTEGRATION_REQUEST
INTEGRATION_RESPONSE
INTEGRATION_RESPONSE_ERROR
INTEGRATION_TIMEOUT
INTEGRATION_RETRY
INTEGRATION_RETRY_EXHAUSTED
INTEGRATION_CIRCUIT_OPEN
INTEGRATION_FALLBACK
```

배치:

```text
BATCH_WORKER_START
BATCH_WORKER_HEARTBEAT
BATCH_WORKER_GHOST_CANDIDATE
BATCH_JOB_START
BATCH_JOB_END
BATCH_JOB_FAIL
BATCH_STEP_START
BATCH_STEP_END
BATCH_STEP_FAIL
BATCH_ITEM_START
BATCH_ITEM_END
BATCH_ITEM_FAIL
BATCH_RERUN_REQUEST
BATCH_RERUN_START
BATCH_RERUN_END
```

감사/운영자 조치:

```text
ADM_OPERATOR_ACTION
ADM_PERMISSION_CHANGE
ADM_MENU_PERMISSION_CHANGE
ADM_API_PERMISSION_CHANGE
MASKING_ACCESS
MASKING_UNMASK_REQUEST
DOWNLOAD_REQUEST
DOWNLOAD_COMPLETE
LOG_LEVEL_POLICY_CREATE
LOG_LEVEL_POLICY_APPLY
LOG_LEVEL_POLICY_EXPIRE
LOG_LEVEL_POLICY_ROLLBACK
```

보안:

```text
LOGIN_SUCCESS
LOGIN_FAIL
LOGOUT
ACCOUNT_LOCK
ACCOUNT_UNLOCK
PASSWORD_CHANGE
PASSWORD_RESET
ACCESS_DENIED
SENSITIVE_ACCESS_DENIED
```

## 21.7 이중화/다중 인스턴스 필수 기준

모든 DB 로그와 파일 로그에는 실행 인스턴스 식별자가 포함되어야 한다.

필수:

```text
serverId
instanceId
hostName
hostIp
port
processId
containerId
podName
profile/env
appVersion/buildVersion
startupTime
servingRole(active/standby/worker/scheduler)
```

원격 호출 시 추가 필드:

```text
sourceServerId
sourceInstanceId
sourceHostName
sourcePort
targetServerId
targetInstanceId
targetHostName
targetPort
remoteServerId
remoteInstanceId
remoteHostName
remotePort
remoteAppVersion
```

예: ACC가 EXS 호출 후 EXS AP02가 실패 응답을 반환한 경우 ACC 로그에 남길 요약:

```json
{
  "eventType": "OUTBOUND_RESPONSE_ERROR",
  "moduleCode": "ACC",
  "sourceServerId": "ACC-AP01",
  "sourceInstanceId": "acc-8080-01",
  "targetModuleCode": "EXS",
  "targetServerId": "EXS-AP02",
  "targetInstanceId": "exs-8092-02",
  "transactionGlobalId": "20260703181022001ACC0000001",
  "transactionSegmentId": "ACC-SEG-0002",
  "httpStatus": 500,
  "responseCode": "EXS_TIMEOUT",
  "status": "FAIL",
  "failureCode": "EXS_TIMEOUT",
  "failureMessageMasked": "EXS call failed: timeout after 3000ms",
  "durationMs": 3012
}
```

기준:

```text
ACC 로그에는 EXS 내부 stack trace를 복제하지 않는다.
ACC 로그에는 EXS 호출 결과 요약, 응답 인스턴스 식별자, 응답 코드, 소요시간, timeout/retry 여부를 남긴다.
EXS 내부 원인은 EXS WAS의 cpf-exs-* 로그에서 같은 transactionGlobalId로 확인한다.
ADM timeline은 각 segment의 moduleCode뿐 아니라 serverId/instanceId/hostName도 표시한다.
```

## 21.8 응답 헤더 기반 원격 인스턴스 식별

L4/LB 뒤에 있는 target instance를 source 모듈이 알 수 있도록 CPF 표준 응답 헤더 후보를 둔다.

후보:

```text
X-Cpf-Module-Code
X-Cpf-Server-Id
X-Cpf-Instance-Id
X-Cpf-Host-Name
X-Cpf-App-Version
X-Cpf-Build-Version
X-Transaction-Global-Id
X-Transaction-Segment-Id
```

주의:

```text
내부망 운영 진단용 헤더는 외부 노출 여부를 profile/security policy로 통제한다.
외부 고객 채널로 그대로 내려가면 안 되는 헤더는 gateway에서 제거한다.
```

## 21.9 로그 환경파일 설정 표준

DB 로그/파일 로그/로그 타입/마스킹/동적 로그 레벨은 profile별 환경파일에서 제어 가능해야 한다.

예시:

```yaml
cpf:
  logging:
    db:
      enabled: true
      transaction-enabled: true
      audit-enabled: true
      error-enabled: true
      integration-enabled: true
      batch-enabled: true
      fail-policy: WARN_CONTINUE

    file:
      enabled: true
      json-line-enabled: true
      base-path: ./logs
      file-pattern: "{moduleCode}/cpf-{moduleCode}-{logType}.log"
      application-enabled: true
      transaction-enabled: true
      integration-enabled: true
      audit-enabled: true
      error-enabled: true
      batch-enabled: true
      rolling:
        max-file-size: 100MB
        max-history-days: 30
        total-size-cap: 10GB

    module:
      module-code: ACC
      server-id: ACC-AP01
      instance-id: acc-8080-01

    masking:
      enabled: true
      raw-sensitive-log-enabled: false
      block-raw-headers:
        - Authorization
        - X-Api-Key
        - Cookie
        - Set-Cookie
      block-keywords:
        - token
        - secret
        - password
        - credential
        - signature

    dynamic-level:
      enabled: true
      trace-boost-enabled: true
      max-ttl-minutes: 30
      require-operator-reason: true
      require-audit: true
      allow-root-logger-change: false
```

필수 설정 key 후보:

```text
cpf.logging.db.enabled
cpf.logging.db.transaction-enabled
cpf.logging.db.audit-enabled
cpf.logging.db.error-enabled
cpf.logging.db.integration-enabled
cpf.logging.db.batch-enabled
cpf.logging.db.fail-policy
cpf.logging.file.enabled
cpf.logging.file.json-line-enabled
cpf.logging.file.base-path
cpf.logging.file.file-pattern
cpf.logging.file.application-enabled
cpf.logging.file.transaction-enabled
cpf.logging.file.integration-enabled
cpf.logging.file.audit-enabled
cpf.logging.file.error-enabled
cpf.logging.file.batch-enabled
cpf.logging.file.rolling.max-file-size
cpf.logging.file.rolling.max-history-days
cpf.logging.file.rolling.total-size-cap
cpf.logging.masking.enabled
cpf.logging.masking.raw-sensitive-log-enabled
cpf.logging.dynamic-level.enabled
cpf.logging.dynamic-level.trace-boost-enabled
cpf.logging.dynamic-level.max-ttl-minutes
cpf.logging.dynamic-level.require-operator-reason
cpf.logging.dynamic-level.allow-root-logger-change
```

## 21.10 profile별 기본값 기준

| 항목 | local | dev | test | prod |
|---|---:|---:|---:|---:|
| DB transaction log | 선택 | ON 권장 | ON | ON |
| DB audit log | 선택 | ON | ON | ON, OFF 제한 |
| DB error log | ON | ON | ON | ON |
| file transaction log | ON | ON | ON | ON |
| file application log | ON | ON | ON | ON |
| file audit log | 선택 | ON | ON | ON, OFF 제한 |
| file error log | ON | ON | ON | ON |
| raw payload log | OFF | OFF | OFF | OFF |
| masking | ON | ON | ON | ON, OFF 금지 |
| dynamic trace boost | ON | ON | ON | ON, TTL/감사 필수 |
| root logger dynamic change | 제한 | 제한 | 제한 | 금지 |

운영 guardrail:

```text
prod에서 audit/error/transaction 추적 로그 완전 비활성화 금지 또는 승인/감사 필수
prod에서 masking 비활성화 금지
prod에서 raw payload/header 원문 저장 금지
prod에서 dynamic TRACE는 TTL/사유/감사/대상 조건 없이는 금지
```

## 21.11 DB 로그 실패와 파일 로그 실패 정책

DB 로그 저장 실패 시:

```text
업무 거래를 무조건 실패시키면 안 되는 경우가 많다.
DB 로그 실패는 cpf-{moduleCode}-error.log 또는 cpf-pfw-error.log에 남긴다.
로그 저장 실패 횟수/마지막 실패 시각을 health/ADM에서 조회 가능해야 한다.
단, 감사 로그처럼 법적/보안상 필수인 로그는 fail-closed 정책 후보를 둘 수 있다.
```

파일 로그 실패 시:

```text
파일 경로 권한 오류/디스크 full/rolling 실패를 health에서 감지한다.
파일 로그 실패도 ADM 진단에서 확인 가능해야 한다.
운영환경에서 파일 로그가 꺼졌거나 실패 중이면 경고 상태로 표시한다.
```

정책 후보:

```text
WARN_CONTINUE: 경고 후 업무 계속
FAIL_TRANSACTION: 해당 거래 실패
FAIL_OPERATOR_ACTION: 운영자 조치 차단
DEGRADE_TO_FILE_ONLY: DB 로그 실패 시 파일 로그만 유지
DEGRADE_TO_DB_ONLY: 파일 로그 실패 시 DB 로그만 유지
```

## 21.12 로그 rolling / retention / purge

파일 로그:

```text
일자별 rolling
size 기반 rolling
moduleCode/logType별 보존 기간
압축 보관 후보
디스크 사용량 cap
장애 분석용 최근 N일 보존
감사 로그 별도 장기 보존 후보
```

DB 로그:

```text
거래 로그 보존 기간
오류 로그 보존 기간
감사 로그 보존 기간
외부연계 로그 보존 기간
전문 송수신 로그 보존 기간
배치 실행 로그 보존 기간
archive/purge job
purge 감사 로그
ADM 보존 정책 조회
```

주의:

```text
감사 로그와 보안 로그는 임의 삭제되지 않도록 별도 정책을 둔다.
보존 기간 정책 변경은 ADM 감사 대상이다.
```

## 21.13 ADM 로그 설정 조회/진단

ADM에서 조회 가능해야 하는 항목:

```text
moduleCode별 DB 로그 활성화 상태
moduleCode별 파일 로그 활성화 상태
serverId/instanceId별 로그 경로
현재 log level
동적 trace boost 정책 목록
만료 예정 정책
적용 실패 정책
로그 파일 쓰기 가능 여부
로그 디스크 사용량 후보
마스킹 정책 활성화 상태
민감 로그 차단 상태
최근 로그 저장 실패 이력
```

API 후보:

```text
GET /adm/api/logging/config
GET /adm/api/logging/config/modules
GET /adm/api/logging/runtime-levels
GET /adm/api/logging/health
GET /adm/api/logging/policies
GET /adm/api/logging/policies/{policyId}
POST /adm/api/logging/policies
POST /adm/api/logging/policies/{policyId}/disable
```

## 21.14 로그 검증 smoke

필수 smoke 후보:

```text
scripts/smoke-file-log-standard.ps1
scripts/smoke-db-log-standard.ps1
scripts/smoke-transaction-log-grep.ps1
scripts/smoke-dynamic-log-level-runtime.ps1
scripts/smoke-batch-dynamic-log-runtime.ps1
scripts/smoke-log-config-profile.ps1
```

검증 기준:

```text
runtime 거래 실행 후 DB pfw_transaction_segment 조회 성공
동일 transactionGlobalId로 cpf-{moduleCode}-transaction.log grep 성공
연계 호출 후 cpf-{moduleCode}-integration.log grep 성공
오류 발생 시 cpf-{moduleCode}-error.log grep 성공
운영자 조치 시 cpf-adm-audit.log와 DB audit log 연결 확인
민감정보 원문이 로그/evidence에 없는지 검사
prod profile guardrail 위반 시 startup validation 또는 smoke 실패
```


---

# 22. ADM 동적 로그 레벨 / 거래 단위 Trace Boost 표준

## 22.1 목적

운영 중 장애 분석을 위해 전체 WAS/root logger를 DEBUG/TRACE로 올리면 로그가 폭증하고 민감정보 노출 위험이 커진다. CPF는 특정 조건에 맞는 거래/배치/인스턴스만 일시적으로 상세 로그를 남기는 기능을 제공해야 한다.

기능명 후보:

```text
ADM 동적 로그 레벨 제어
거래 단위 Trace Boost
조건 기반 상세 로그 정책
Batch Trace Boost
```

## 22.2 핵심 원칙

```text
전체 root logger 변경 금지
전체 WAS DEBUG/TRACE 장시간 유지 금지
특정 거래/조건/서버/인스턴스/job/step/rerun만 대상
TTL 기반 자동 원복 필수
수동 원복 가능
운영자 사유 필수
ADM 감사 로그 필수
파일 로그와 DB policy/history 연결 필수
DEBUG/TRACE에서도 민감정보 원문 노출 금지
```

## 22.3 온라인 거래 Trace Boost 조건

조건 후보:

```text
transactionGlobalId
transactionSegmentId
moduleCode
serverId
instanceId
apiPath
httpMethod
status
failureCode
durationMs >= N
customerNoMasked/memberNoMasked 후보
channelCode
originalChannelCode
institutionCode
endpointCode
externalTransactionId
sourceModuleCode
targetModuleCode
```

예:

```text
moduleCode=ACC AND apiPath=/acc/api/composite/demo AND durationMs>=3000 AND ttl=10m
transactionGlobalId=20260703181022001ACC0000001 AND level=TRACE AND ttl=5m
moduleCode=EXS AND institutionCode=DEMO_BANK AND failureCode=TIMEOUT AND level=DEBUG AND ttl=30m
```

## 22.4 배치 Trace Boost 조건

배치 재수행 또는 특정 배치 장애 분석 시 사용한다.

조건 후보:

```text
jobName
jobExecutionId
stepName
stepExecutionId
runId
rerunId
workerInstanceId
serverId
instanceId
partitionNo
chunkNo
itemId
status
failureCode
durationMs >= N
```

예:

```text
jobExecutionId=JOB-20260703-0001 AND level=TRACE AND untilJobEnd=true
jobName=settlementJob AND stepName=calculateFeeStep AND rerunId=RERUN-0003 AND level=DEBUG
workerInstanceId=bat-worker-01 AND jobName=centerCutJob AND ttl=30m
```

## 22.5 정책 DB 후보

테이블 후보:

```text
adm_log_level_policy
adm_log_level_policy_target
adm_log_level_policy_history
adm_log_level_runtime_state
adm_log_level_policy_audit
```

주요 컬럼:

```text
policy_id
policy_name
policy_type                TRANSACTION / BATCH / MODULE / LOGGER / INTEGRATION
module_code
server_id
instance_id
logger_name
level                      INFO / DEBUG / TRACE
condition_json
condition_summary_masked
target_type
target_value_masked
started_at
expires_at
until_job_end_yn
enabled_yn
status                     READY / APPLIED / EXPIRED / ROLLED_BACK / FAILED
created_by
created_reason
approved_by
applied_at
rollback_at
failure_reason_masked
created_at
updated_at
```

## 22.6 ADM UI/API 기준

ADM 메뉴 후보:

```text
ADM > 운영 정책 > 로그 레벨 정책
ADM > 거래 관제 > 거래 단위 Trace Boost
ADM > 배치 관제 > 배치 Trace Boost
ADM > 감사 > 로그 레벨 변경 감사
```

필수 기능:

```text
현재 로그 레벨 조회
moduleCode/serverId/instanceId별 runtime level 조회
정책 생성
정책 적용
정책 만료/자동 원복
수동 원복
정책 이력 조회
적용 실패 조회
운영자 사유 입력
권한 통제
감사 로그
```

API 후보:

```text
GET /adm/api/log-level-policies
GET /adm/api/log-level-policies/{policyId}
POST /adm/api/log-level-policies
POST /adm/api/log-level-policies/{policyId}/rollback
GET /adm/api/log-level-runtime-states
GET /adm/api/log-level-audits
POST /adm/api/transactions/{transactionGlobalId}/trace-boost
POST /adm/api/batch/job-executions/{jobExecutionId}/trace-boost
```

## 22.7 파일 로그와의 연결

Trace Boost 적용 시 파일 로그에는 아래가 남아야 한다.

```text
traceBoostPolicyId
traceBoostAppliedYn
traceBoostLevel
traceBoostConditionMatchedYn
operatorId
policyExpiresAt
```

예:

```json
{
  "eventType": "TRACE_BOOST_DETAIL",
  "level": "TRACE",
  "traceBoostPolicyId": "LOGPOL-20260703-0001",
  "traceBoostAppliedYn": "Y",
  "transactionGlobalId": "20260703181022001ACC0000001",
  "moduleCode": "ACC",
  "requestHeaderMasked": {},
  "requestPayloadMasked": {},
  "responseHeaderMasked": {},
  "responsePayloadMasked": {}
}
```

## 22.8 완료 기준

```text
ADM API/UI 구현
DB policy/history/audit 테이블 반영
SQL/Flyway/all_install 반영
파일 로그에 traceBoostPolicyId 기록
특정 transactionGlobalId만 DEBUG/TRACE 증가 검증
특정 jobExecutionId/rerunId만 DEBUG/TRACE 증가 검증
전체 root logger 미변경 검증
TTL 만료 후 자동 원복 검증
수동 원복 검증
운영자 감사 로그 검증
민감정보 원문 미노출 검증
runtime smoke/evidence 기록
```

완료 불인정:

```text
root logger 전체 변경만 구현
TTL 없음
운영자 사유 없음
감사 로그 없음
파일 로그 evidence 없음
배치 미지원
민감정보 원문 노출
```


---

# 23. BAT 배치 로그/재수행/Trace Boost 상세 기준

## 23.1 목적

BAT는 온라인 거래와 다른 운영 특성을 가진다. 장시간 실행, worker 이중화, job/step/item/chunk/partition, 재수행, 강제 실행, 중지 요청, ghost, lock이 모두 추적되어야 한다.

## 23.2 배치 로그 필수 식별자

```text
jobName
jobExecutionId
stepName
stepExecutionId
runId
rerunId
workerId
workerInstanceId
serverId
instanceId
partitionNo
chunkNo
itemId
centerCutJobId
centerCutItemId
transactionGlobalId
parentTransactionGlobalId
childTransactionGlobalId
traceBoostPolicyId
logLevelApplied
```

## 23.3 배치 파일 로그 표준

파일 예:

```text
cpf-bat-application.log
cpf-bat-batch.log
cpf-bat-transaction.log
cpf-bat-error.log
cpf-bat-audit.log
```

배치 이벤트:

```text
BATCH_JOB_REQUESTED
BATCH_JOB_START
BATCH_JOB_END
BATCH_JOB_FAIL
BATCH_STEP_START
BATCH_STEP_END
BATCH_STEP_FAIL
BATCH_ITEM_START
BATCH_ITEM_END
BATCH_ITEM_FAIL
BATCH_LOCK_ACQUIRE
BATCH_LOCK_RELEASE
BATCH_LOCK_STALE
BATCH_STOP_REQUEST
BATCH_FORCE_RUN_REQUEST
BATCH_RERUN_REQUEST
BATCH_RERUN_START
BATCH_RERUN_END
BATCH_TRACE_BOOST_APPLIED
BATCH_TRACE_BOOST_ROLLBACK
```

## 23.4 배치 재수행 로그 레벨 제어

필수 기능:

```text
재수행 요청 시 logLevel 지정 후보
특정 rerunId/jobExecutionId만 DEBUG/TRACE 적용
해당 실행 종료 시 자동 원복
workerInstanceId 단위 적용 가능
job/step 단위 적용 가능
itemId/chunkNo 후보 조건 적용 가능
운영자 사유/감사 필수
```

예:

```text
ADM에서 settlementJob 재수행 요청
재수행 옵션: traceLevel=DEBUG, targetStep=calculateFeeStep, expiresWhenJobEnds=true
BAT worker가 해당 jobExecutionId/rerunId에만 DEBUG 상세 로그 기록
다른 배치 job은 INFO 유지
```

## 23.5 BAT DB 로그 후보

테이블 후보:

```text
bat_worker_instance
bat_worker_heartbeat
bat_job_execution
bat_step_execution
bat_item_execution
bat_job_parameter
bat_job_lock
bat_operator_action
bat_trace_boost_policy_link
```

## 23.6 ADM BAT 관제 필수 표시

```text
worker 목록/상세
heartbeat 상태
ghost candidate
job execution 목록
step execution 목록
item 실패 목록
lock 상태
stale lock
재수행 이력
강제 실행 이력
중지 요청 이력
Trace Boost 적용 여부
적용 로그 레벨
실행 서버/인스턴스
파일 로그 경로 후보
```

## 23.7 완료 기준

```text
BAT job/step/item DB 로그 저장
BAT 파일 로그 저장
jobExecutionId 기준 DB/파일 로그 연결
재수행 시 rerunId 기록
해당 rerunId만 동적 DEBUG/TRACE 적용 가능
ADM에서 Trace Boost 적용 상태 확인
TTL 또는 job end 자동 원복
operator action audit 저장
runtime smoke 검증
```


---

# 24. 신규 주제영역 온보딩 생성기 / Domain Scaffold 표준

## 24.1 목적

CPF는 신규 업무 주제영역을 추가할 때 개발자가 기존 모듈을 복사한 뒤 build/settings/yml/package/SQL/권한/ADM 메뉴/로그 설정을 일일이 수정하는 방식에 의존하면 안 된다.

목표:

```text
명령 한 번으로 신규 주제영역 개발자 소스 영역 생성
PFW module registry/로그/헤더/trace 정책 자동 대응
ADM 메뉴/API/버튼 권한 seed 후보 자동 생성
SQL/Flyway/all_install/smoke_check 자동 반영 후보 생성
테스트/smoke/README/기능 매트릭스 초안 생성
```

## 24.2 실행 위치

소스 생성은 ADM 운영 화면에서 직접 수행하지 않는다.

기준:

```text
소스 생성 = 관리자급 개발자의 로컬 워크스페이스 또는 CI 개발 도구
운영 등록/조회/상태 확인 = ADM
```

이유:

```text
ADM 운영 서버가 Git repo를 직접 수정하면 안 된다.
settings.gradle/build.gradle/SQL/Flyway 생성은 개발 형상관리 영역이다.
생성 결과는 개발자가 Git diff로 리뷰해야 한다.
배포 후 ADM은 등록 상태/권한/로그/health를 확인한다.
```

## 24.3 명령 예시

PowerShell:

```powershell
powershell -File scripts/create-domain.ps1 `
  -ModuleCode LON `
  -ModuleName loan `
  -DisplayName "대출" `
  -BasePackage cpf.lon `
  -Port 8095 `
  -TablePrefix lon `
  -OwnerTeam "LoanTeam" `
  -GenerateAdmSeeds `
  -GenerateSql `
  -GenerateSmoke
```

Gradle task:

```bash
./gradlew createDomain \
  -PmoduleCode=LON \
  -PmoduleName=loan \
  -PdisplayName=대출 \
  -PbasePackage=cpf.lon \
  -Pport=8095 \
  -PtablePrefix=lon \
  -PgenerateAdmSeeds=true \
  -PgenerateSql=true \
  -PgenerateSmoke=true
```

## 24.4 입력값 표준

필수:

```text
moduleCode            예: LON
moduleName            예: loan
displayName           예: 대출
basePackage           예: cpf.lon
port                  예: 8095
tablePrefix           예: lon
```

권장:

```text
ownerTeam
businessAreaCode
defaultChannelCode
defaultMenuGroup
adminMenuName
apiBasePath
sampleEntityName
sampleTableName
generateCrudYn
generateAdmSeedYn
generateSqlYn
generateFlywayYn
generateSmokeYn
generateOpenApiYn
```

검증:

```text
moduleCode 중복 금지
port 중복 경고
packageName 규칙 검증
tablePrefix 규칙 검증
예약어 금지
기존 디렉터리 존재 시 중단 또는 --force 필요
Git working tree dirty 경고 후보
```

## 24.5 생성 대상 — 개발자 소스 영역

```text
{moduleName}/build.gradle
{moduleName}/src/main/java/{basePackage}/
  {Module}Application.java
  bse/controller
  bse/service
  bse/facade
  bse/mapper
  bse/dto
  bse/validation
  config
{moduleName}/src/main/resources/application-{moduleName}.yml
{moduleName}/src/main/resources/mybatis/mapper/{moduleName}/
{moduleName}/src/test/java/{basePackage}/
{moduleName}/README.md
```

기본 샘플:

```text
SampleController
SampleService
SampleFacade
SampleMapper
SampleRequest
SampleResponse
SampleSearchRequest
SampleCreateRequest
SampleUpdateRequest
SampleValidation
Mapper XML
DB fixture
Controller test
Service test
Mapper DB slice test 후보
runtime smoke 초안
```

## 24.6 생성 대상 — PFW 관리 설정

```text
PFW module registry seed
moduleCode/displayName/basePackage/defaultPort 등록 후보
standard header filter 대상 등록
TransactionContext 대상 moduleCode 등록
transaction segment moduleCode 후보 등록
파일 로그 appender 설정 후보
동적 로그 레벨 정책 대상 등록
마스킹 정책 후보
감사 정책 후보
OpenAPI group 후보
health module registry 후보
```

## 24.7 생성 대상 — ADM 운영 관리 seed

```text
ADM 메뉴 seed
ADM API 권한 seed
ADM 버튼 권한 seed
ADM 다운로드 권한 seed 후보
ADM 마스킹 권한 seed 후보
ADM 운영 조회 메뉴 후보
ADM 동적 로그 레벨 대상 moduleCode 등록
ADM transaction group 검색 moduleCode 후보
ADM health/온보딩 상태 조회 후보
```

예: 대출 LON 생성 시 후보:

```text
대출 관리
대출 목록 조회
대출 상세 조회
대출 등록
대출 수정
대출 삭제 또는 상태 변경
대출 다운로드
대출 마스킹 해제
대출 API 권한
대출 버튼 권한
대출 감사 조회
```

## 24.8 생성 대상 — SQL/Flyway/all_install

```text
split SQL: ddl/{module}/, seed/{module}/ 후보
Flyway migration 후보
00_all_install.sql 반영 후보
00_all_install_and_smoke.sql 반영 후보
99_smoke_check.sql 반영 후보
sample table
sample audit/history 후보
ADM menu/API/button permission seed
PFW module registry seed
공통 코드 seed 후보
```

주의:

```text
자동 생성기가 all_install을 직접 수정할 경우 중복/순서/재실행 idempotent 검증 필요
안전하지 않으면 generated patch 파일로 생성하고 개발자가 반영하도록 할 수 있음
어느 방식이든 check-sql-standard에서 검증 가능해야 함
```

## 24.9 생성 대상 — 로그/환경파일

```text
application-{module}.yml
moduleCode/serverId/instanceId 설정 예시
log file pattern
DB log enabled 기본값
file log enabled 기본값
transaction/integration/audit/error log enabled 기본값
masking enabled 기본값
trace boost 대상 등록 후보
```

생성 파일 예:

```text
logs/{moduleName}/cpf-{moduleCodeLower}-application.log
logs/{moduleName}/cpf-{moduleCodeLower}-transaction.log
logs/{moduleName}/cpf-{moduleCodeLower}-integration.log
logs/{moduleName}/cpf-{moduleCodeLower}-audit.log
logs/{moduleName}/cpf-{moduleCodeLower}-error.log
```

## 24.10 생성 대상 — 테스트/smoke/evidence

```text
:module:test 성공 가능 구조
Controller test
Service test
Mapper DB slice test 후보
standard header smoke 초안
CRUD runtime smoke 초안
OpenAPI smoke 후보
file log grep smoke 후보
DB log smoke 후보
feature evidence placeholder
```

## 24.11 ADM 온보딩 현황 화면

ADM은 소스를 직접 생성하지 않고 배포 후 상태를 확인한다.

기능:

```text
moduleCode 목록
신규 module registry 반영 여부
health 상태
OpenAPI 노출 여부
메뉴 seed 반영 여부
API 권한 seed 반영 여부
버튼 권한 seed 반영 여부
로그 정책 등록 여부
마스킹 정책 등록 여부
감사 정책 등록 여부
파일 로그 경로 상태
DB 로그 활성화 상태
smoke/evidence 최신 상태
```

API 후보:

```text
GET /adm/api/module-onboarding
GET /adm/api/module-onboarding/{moduleCode}
GET /adm/api/module-onboarding/{moduleCode}/health
GET /adm/api/module-onboarding/{moduleCode}/permissions
GET /adm/api/module-onboarding/{moduleCode}/logging
GET /adm/api/module-onboarding/{moduleCode}/smoke-results
```

## 24.12 완료 기준

```text
create-domain 명령 존재
신규 모듈 생성 성공
settings.gradle/build.gradle 반영 또는 patch 생성
application yml 생성
Controller/Service/Mapper/DTO/Validation 생성
SQL/Flyway/all_install/smoke_check 반영 또는 patch 생성
PFW module registry seed 생성
ADM menu/API/button permission seed 생성
로그 설정 생성
테스트/smoke 초안 생성
생성된 모듈 compile/test 성공
기존 모듈 복사 후 수동 문자열 치환 방식이 아님
```

완료 불인정:

```text
폴더만 생성
소스만 생성하고 PFW/ADM/SQL 미반영
settings.gradle 수동 수정 필요하지만 문서화 없음
기존 모듈 복사 후 치환만 수행
생성 후 빌드 실패
ADM/PFW 관리 영역과 moduleCode 불일치
```


---

# 25. PFW Core 고도화 상세 백로그

## 25.1 표준 Header/Context runtime chain

필수 구성:

```text
HeaderDefinitionRegistry
HeaderExtractor
HeaderValidator
HeaderNormalizer
HeaderMasker
HeaderSnapshotWriter
TransactionContextFactory
TransactionContextHolder
TransactionSegmentManager
OutboundHeaderPropagator
ResponseHeaderWriter
MdcContextWriter
```

완료 기준:

```text
inbound 요청에서 header 추출
REQUIRED/OPTIONAL/INTERNAL_ONLY/FORBIDDEN_TO_LOG_RAW 분류
TransactionContext 생성
MDC 자동 반영
DB segment 생성
파일 로그 context 반영
outbound 호출 시 전파
response header 기록
ADM header snapshot 조회
```

## 25.2 공통 Client Wrapper

대상:

```text
CpfWebClient
CpfRestClient
CpfFeignClient 후보
CpfHttpExchangeClient 후보
```

필수:

```text
표준/확장 header 자동 전파
timeout/retry 정책 적용
request/response masked logging
transactionSegment 생성
integration log 생성
remote instance response header 수집
error mapping
metric 기록
```

완료 불인정:

```text
업무 코드가 직접 WebClient.builder() 사용
header 수동 하드코딩
timeout/retry 제각각
로그/segment 미생성
민감정보 원문 로그 위험
```

## 25.3 공통 오류/예외 체계

필수:

```text
CpfBusinessException
CpfSystemException
CpfValidationException
CpfSecurityException
CpfIntegrationException
CpfBatchException
CpfErrorCode
CpfErrorResponse
GlobalExceptionHandler
ErrorLogWriter
FailureSegmentUpdater
```

기준:

```text
오류 발생 시 transactionGlobalId 응답 포함
failureCode/failureMessageMasked 저장
stacktrace 원문 응답 금지
파일 error log와 DB error log 연결
ADM 오류 상세 조회 연결
```

## 25.4 정책/설정 Registry

필수 후보:

```text
ModuleRegistry
PolicyRegistry
MaskingPolicyRegistry
LoggingPolicyRegistry
HeaderPolicyRegistry
PermissionPolicyRegistry
IntegrationEndpointRegistry
FixedLengthLayoutRegistry
BatchJobRegistry
CenterCutRegistry
```

기준:

```text
DB 기반과 yml 기반을 분리/우선순위 정의
local/dev/prod profile 차이 허용
ADM에서 조회 가능
정책 변경 감사
캐시 refresh 가능
```

## 25.5 Observability Hook

필수:

```text
slow transaction detector
error rate counter
timeout counter
retry counter
integration latency recorder
batch duration recorder
center-cut item duration recorder
```

후보:

```text
Micrometer metric
OpenTelemetry span
Prometheus export
ADM observability summary
```


---

# 26. ADM 운영 콘솔 상세 기능 사전

## 26.1 ADM 기본 구조

ADM은 단순 CRUD 관리자가 아니라 CPF 운영 콘솔이다.

필수 공통 화면 패턴:

```text
목록
상세
검색조건
정렬 whitelist
페이징
상태 badge
실패 사유
권한별 버튼 제어
마스킹 표시
감사 로그 연결
다운로드 권한/감사
수행시간 표시
trace/evidence 연결
```

## 26.2 ADM 메뉴 전체 후보

```text
대시보드
거래 관제
오류 관제
연계 관제
전문 관제
배치 관제
Center-Cut 관제
운영자/권한 관리
로그/Trace Boost 관리
정책/캐시 관리
보안 관리
파일/다운로드 감사
마스킹/민감정보 감사
모듈 온보딩 현황
OpenAPI/Smoke 현황
DB/Flyway 설치 현황
시스템 Health
운영 Runbook
```

## 26.3 거래 관제 고도화

필수:

```text
transactionGlobalId 그룹 목록
거래 상세/timeline
segment tree
duration bar
failure segment 강조
serverId/instanceId 표시
source/target module 표시
remote instance 표시
표준 header snapshot
확장 header snapshot
integration logs
batch/center-cut child 연결
operator action 연결
file log grep helper 후보
```

## 26.4 로그/Trace Boost 관리

필수:

```text
현재 runtime log level 조회
module/server/instance별 로그 상태
DB/file log 활성화 상태
Trace Boost 정책 생성/해제
거래 단위 Trace Boost
배치 job/step Trace Boost
TTL/자동 원복
수동 원복
정책 감사
적용 실패 이력
```

## 26.5 모듈 온보딩 현황

필수:

```text
moduleCode 목록
module displayName
module health
port
profile
appVersion
OpenAPI 노출 여부
PFW registry 등록 여부
ADM menu seed 반영 여부
API permission seed 반영 여부
button permission seed 반영 여부
DB/Flyway 반영 여부
file log 설정 여부
DB log 설정 여부
smoke 결과
```

## 26.6 권한/감사 운영

필수:

```text
운영자 목록/상세
역할 목록/상세
메뉴 권한
버튼 권한
API 권한
다운로드 권한
마스킹 해제 권한
고위험 조치 권한
권한 변경 감사
권한 테스트 smoke 결과
ADM_VIEWER 차단/ADM_ADMIN 허용 검증
```

## 26.7 UX 기준

```text
운영자가 장애 시 3분 내 거래를 찾을 수 있는 검색조건 제공
목록 기본 정렬은 최신순
실패/지연 quick filter 제공
transactionGlobalId 복사 버튼
관련 파일 로그 grep command 후보 표시
민감정보는 기본 마스킹
권한 없으면 탭/버튼 숨김 또는 disabled
실패 API는 표준 오류 표시
```


---

# 27. EXS / Integration 최종 상세 기준

## 27.1 Integration 유형 표준

```text
INTERNAL_API        모듈 간 REST/API 호출
EXTERNAL_REST       외부기관 REST 호출
FIXED_LENGTH        고정길이 전문 송수신
MQ                  MQ 송수신
KAFKA               Kafka publish/consume
REDIS               Redis cache/pubsub/lock 후보
FILE_TRANSFER       파일 송수신 후보
SFTP                SFTP 송수신 후보
BATCH_LINK          배치 파일/연계 후보
```

## 27.2 EXS 송수신 로그 원장

테이블 후보:

```text
exs_exchange_log
exs_exchange_message_log
exs_exchange_retry_log
exs_exchange_payload_snapshot
```

필수 컬럼:

```text
exchange_log_id
transaction_global_id
transaction_segment_id
module_code
server_id
instance_id
institution_code
endpoint_code
integration_type
protocol_type
api_path
http_method
request_started_at
response_ended_at
duration_ms
timeout_ms
retry_count
http_status
external_response_code
mapped_cpf_error_code
external_transaction_id
status
failure_code
failure_message_masked
request_header_masked
response_header_masked
request_payload_masked
response_payload_masked
created_at
```

## 27.3 ACC → EXS 오류 응답 처리

ACC 호출 측 로그 필수:

```text
sourceModuleCode=ACC
targetModuleCode=EXS
direction=OUTBOUND
apiPath
httpStatus
responseCode
failureCode
failureMessageMasked
durationMs
retryCount
timeoutYn
remoteServerId/remoteInstanceId 후보
responsePayloadMasked summary
```

EXS 처리 측 로그 필수:

```text
moduleCode=EXS
sourceModuleCode=ACC
institutionCode
endpointCode
externalTransactionId
실제 외부기관 호출 결과
실패 원인 상세 masked
```

기준:

```text
ACC는 EXS 내부 stacktrace를 복제하지 않는다.
EXS 내부 원인은 EXS 로그/DB 원장에서 transactionGlobalId로 추적한다.
ADM은 ACC outbound segment와 EXS inbound/external segment를 timeline으로 연결한다.
```

## 27.4 timeout/retry/circuit/fallback

필수:

```text
timeout 정책
retry 정책
retry backoff
retry 대상 오류코드
retry 제외 오류코드
circuit breaker 후보
fallback 후보
retry exhaustion 로그
ADM retry 이력 조회
```

## 27.5 민감정보

금지:

```text
Authorization 원문
API key 원문
token 원문
secret/password/credential/signature 원문
계좌번호 전체
주민번호 전체
카드번호 전체
전문 원문 무권한 노출
```

원문 전문 조회가 필요한 경우:

```text
별도 권한
사유 입력
승인 후보
조회 감사
마스킹 해제 감사
다운로드 감사
보존 기간 제한
```


---

# 28. CMN Fixed-Length 전문 엔진 최종 상세 기준

## 28.1 엔진 구성

```text
FixedLengthLayoutRegistry
FixedLengthLayoutSpec
FixedLengthGroupSpec
FixedLengthFieldSpec
FixedLengthParser
FixedLengthFormatter
FixedLengthValidator
FixedLengthMasker
FixedLengthTypeConverter
FixedLengthErrorMapper
FixedLengthFixtureBuilder
```

## 28.2 DB 사전 후보

```text
cmn_fixed_length_layout
cmn_fixed_length_group
cmn_fixed_length_field
cmn_fixed_length_masking_policy
cmn_fixed_length_code_mapping
cmn_fixed_length_fixture
```

## 28.3 반복부 처리

필수:

```text
fixed repeat count
repeat count field 기반 반복
nested group 후보
optional group 후보
group length validation
group item index 오류 표시
```

## 28.4 타입/검증

타입:

```text
STRING
NUMBER
DECIMAL
DATE
DATETIME
TIME
CODE
AMOUNT
BOOLEAN_FLAG
FILLER
BINARY 후보
```

검증:

```text
byte length
charset
required
numeric
decimal scale
date format
code group
padding
alignment
record count
checksum 후보
```

## 28.5 parse/format 결과

```text
successYn
layoutId
messageCode
fieldValues
maskedValues
errors[]
rawLength
expectedLength
charset
parseDurationMs
```

## 28.6 EXS 연계

```text
EXS endpoint와 layout 연결
요청 전문 format
응답 전문 parse
응답 코드 매핑
전문 송수신 로그
전문 마스킹
ADM 전문 조회
```


---

# 29. Security / Privacy / Compliance 상세 기준

## 29.1 민감정보 분류

```text
인증정보: Authorization, token, API key, secret, credential, signature
개인식별정보: 고객번호, 회원번호, 사용자ID, 전화번호, 이메일, IP
금융정보: 계좌번호, 카드번호, 거래금액, 대출번호 후보
전문 원문: 외부기관 요청/응답 전문
운영정보: 운영자ID, 권한 변경 이력, 마스킹 해제 사유
```

## 29.2 마스킹 정책

```text
목록 기본 마스킹
상세 기본 마스킹
원문 조회 기본 금지
마스킹 해제는 권한+사유+감사
다운로드는 권한+사유+감사
TRACE/DEBUG에서도 마스킹 유지
```

## 29.3 Secret 관리

```text
소스 하드코딩 금지
yml 평문 금지 후보
환경변수 주입
secret file 주입 후보
Vault/KMS 후보
로그 secret masking
설정 조회 시 masking
```

## 29.4 보안 smoke 후보

```text
민감 header 원문 로그 검사
payload 원문 로그 검사
기본 비밀번호 검사
secret keyword scan
권한 없는 API 접근 차단
마스킹 해제 감사 검증
다운로드 감사 검증
```


---

# 30. 품질 게이트 / Evidence 최종 기준

## 30.1 Evidence 원칙

```text
실행한 검증만 evidence로 기록
민감정보 제거된 sanitized evidence 저장
build/ 로컬 산출물만 리포트에 적고 repo에서 확인 불가하면 재확인 필요
검수자가 열 수 있는 경로 제공
리포트/기능 매트릭스/evidence 경로 일치
```

권장 경로:

```text
specs/evidence/{yyyymmdd}_{seq}/
```

파일 후보:

```text
standard-header-e2e-result.sanitized.json
composite-transaction-runtime-result.sanitized.json
adm-transaction-group-runtime-result.sanitized.json
mariadb-full-install-result.sanitized.json
file-log-grep-result.sanitized.txt
dynamic-log-level-result.sanitized.json
batch-trace-boost-result.sanitized.json
adm-browser-click-result.sanitized.json
openapi-runtime-result.sanitized.json
```

## 30.2 필수 검증 축

```text
Gradle unit/module tests
Mapper DB slice
Runtime smoke
OpenAPI smoke
ADM browser click
MariaDB full install
SQL standard
UTF-8/mojibake
feature evidence
file log grep
DB log query
security/masking scan
qualityGate
```

## 30.3 완료 보고 정합성

완료 보고는 반드시 다음을 분리한다.

```text
직접 실행 완료
소스 레벨 확인
Codex 로컬 실행 주장
미실행/미검증
실패
재확인 필요
```

금지:

```text
실행하지 않은 검증 성공 기록
Codex 보고만으로 완료 처리
경로 없는 evidence 기록
상태값 과장
```


---

# 31. 추가 고급 프레임워크 기능 후보

아래 항목은 CPF가 범용 이상 운영형 프레임워크로 성장하기 위해 장기적으로 관리할 기능 후보이다. 초기 마일스톤에서 모두 완료하지 않아도 되지만, 설계상 막히지 않도록 고려한다.

## 31.1 API Gateway / Edge 연계 후보

```text
trusted proxy header 처리
X-Forwarded-For 기준 clientIp 산정
gateway request id 수신
gateway timeout/error 매핑
gateway에서 내부 진단 header 제거
rate limit 연계
WAF 차단 이력 연계 후보
```

## 31.2 Service Discovery / Routing 후보

```text
moduleCode → endpoint mapping
profile별 endpoint
active/standby endpoint
health 기반 routing 후보
blue/green endpoint 후보
canary endpoint 후보
ADM routing 조회
```

## 31.3 Feature Flag / Rollout

```text
module별 기능 on/off
channel별 기능 on/off
operator별 기능 on/off 후보
percentage rollout
rollback
변경 감사
ADM 조회
```

## 31.4 Workflow / Approval

```text
고위험 운영자 조치 승인
2인 승인 후보
배치 강제 실행 승인
lock 강제 해제 승인
마스킹 해제 승인
다운로드 승인
승인 이력/감사
```

## 31.5 Outbox/Event

```text
outbox table
event publish 상태
event retry
event dead-letter 후보
transactionGlobalId 연결
ADM event 조회
Kafka/MQ 연계
```

## 31.6 Data Retention / Archive / Purge

```text
거래 로그 보존
감사 로그 보존
전문 로그 보존
파일 로그 보존
배치 로그 보존
archive job
purge job
purge 감사
보존 정책 ADM 조회
```

## 31.7 Performance / Benchmark

```text
API latency percentile
slow query 후보
N+1 탐지 후보
Mapper 성능 fixture
고정길이 parser benchmark
batch throughput
center-cut throughput
EXS timeout rate
```

## 31.8 Runbook / Troubleshooting

```text
거래 실패 분석 절차
EXS timeout 분석 절차
DB 로그 저장 실패 대응
파일 로그 저장 실패 대응
동적 Trace Boost 사용 절차
배치 ghost 대응
lock stale 대응
MariaDB full install 실패 대응
ADM browser click 실패 대응
```

## 31.9 Developer Portal 후보

```text
module catalog
API catalog
header catalog
error code catalog
message code catalog
batch job catalog
fixed-length layout catalog
sample generator guide
local setup guide
smoke guide
```

## 31.10 Upgrade / Compatibility

```text
CPF version
module version
DB migration version
breaking change 기록
deprecated API
upgrade guide
rollback guide
release note
```


---

# 32. 요청서 작성 시 누락 방지 체크리스트

새 요청서에는 가능한 한 아래를 선제 반영한다.

## 32.1 기능 구현 체크

```text
Controller/API
Service
Facade/Adapter
Repository/Mapper
DTO
Validation
SQL/Flyway/all_install
Test
Smoke
OpenAPI
README/Guide
CPF_STABILIZATION_REPORT
기능 매트릭스
Evidence
```

## 32.2 운영성 체크

```text
DB 로그
파일 로그
transactionGlobalId 연결
serverId/instanceId
ADM 조회
권한
마스킹
감사
검색조건
정렬/페이징
실패 구간
수행시간
동적 로그 레벨 영향
```

## 32.3 검증 체크

```text
Source-Level test
Runtime smoke
MariaDB full install
Browser click
OpenAPI runtime
File log grep
DB log query
Security scan
Mojibake check
qualityGate
```

## 32.4 완료 불인정 체크

```text
소스만 있음
SQL 한 경로만 반영
Runtime 미실행
파일 로그 미검증
DB 로그 미검증
ADM 미연결
민감정보 원문 가능성
상태값 과장
Evidence 경로 오류
```


---

# 33. 모듈별 상세 완성도 체크리스트

## 33.1 ACC

필수 완성도 항목:

```text
계좌/업무 주거래 orchestration
MBR/EXS 호출 샘플
outbound integration log
복합 거래 MAIN segment
실패 응답 요약 로그
DB 로그/파일 로그/ADM 조회/evidence 연결
권한/마스킹/감사 기준
runtime smoke와 미검증 분리
```

완료 불인정:

```text
소스만 있고 SQL/test/smoke 없음
ADM 운영 조회 없음
파일 로그 grep 검증 없음
민감정보 원문 가능성 있음
```

## 33.2 MBR

필수 완성도 항목:

```text
회원 식별자 의미 분리
memberNo/customerNo/userId 분리
회원 조회 shared segment
마스킹 정책
타 모듈 facade
DB 로그/파일 로그/ADM 조회/evidence 연결
권한/마스킹/감사 기준
runtime smoke와 미검증 분리
```

완료 불인정:

```text
소스만 있고 SQL/test/smoke 없음
ADM 운영 조회 없음
파일 로그 grep 검증 없음
민감정보 원문 가능성 있음
```

## 33.3 EXS

필수 완성도 항목:

```text
기관/endpoint 관리
REST 송수신
고정길이 전문 송수신
retry/timeout
EXS 원장/ADM 조회
DB 로그/파일 로그/ADM 조회/evidence 연결
권한/마스킹/감사 기준
runtime smoke와 미검증 분리
```

완료 불인정:

```text
소스만 있고 SQL/test/smoke 없음
ADM 운영 조회 없음
파일 로그 grep 검증 없음
민감정보 원문 가능성 있음
```

## 33.4 BAT

필수 완성도 항목:

```text
standalone worker
job/step/item
heartbeat/ghost/lock
rerun/force/stop
batch Trace Boost
DB 로그/파일 로그/ADM 조회/evidence 연결
권한/마스킹/감사 기준
runtime smoke와 미검증 분리
```

완료 불인정:

```text
소스만 있고 SQL/test/smoke 없음
ADM 운영 조회 없음
파일 로그 grep 검증 없음
민감정보 원문 가능성 있음
```

## 33.5 ADM

필수 완성도 항목:

```text
운영 콘솔
거래/오류/감사/권한/로그 정책
browser click
운영자 조치
온보딩 현황
DB 로그/파일 로그/ADM 조회/evidence 연결
권한/마스킹/감사 기준
runtime smoke와 미검증 분리
```

완료 불인정:

```text
소스만 있고 SQL/test/smoke 없음
ADM 운영 조회 없음
파일 로그 grep 검증 없음
민감정보 원문 가능성 있음
```

## 33.6 CMN

필수 완성도 항목:

```text
채번/코드/메시지
파일/알림/템플릿
fixed-length engine
masking helper
validation helper
DB 로그/파일 로그/ADM 조회/evidence 연결
권한/마스킹/감사 기준
runtime smoke와 미검증 분리
```

완료 불인정:

```text
소스만 있고 SQL/test/smoke 없음
ADM 운영 조회 없음
파일 로그 grep 검증 없음
민감정보 원문 가능성 있음
```

## 33.7 XYZ/EDU

필수 완성도 항목:

```text
복사 가능한 실전 예제
CRUD/search/sort/paging
Mapper/XML/SQL fixture
권한/마스킹/감사
runtime smoke
DB 로그/파일 로그/ADM 조회/evidence 연결
권한/마스킹/감사 기준
runtime smoke와 미검증 분리
```

완료 불인정:

```text
소스만 있고 SQL/test/smoke 없음
ADM 운영 조회 없음
파일 로그 grep 검증 없음
민감정보 원문 가능성 있음
```

## 33.8 PFW

필수 완성도 항목:

```text
표준 헤더/context
segment/log/audit/masking
client wrapper
policy registry
observability hook
DB 로그/파일 로그/ADM 조회/evidence 연결
권한/마스킹/감사 기준
runtime smoke와 미검증 분리
```

완료 불인정:

```text
소스만 있고 SQL/test/smoke 없음
ADM 운영 조회 없음
파일 로그 grep 검증 없음
민감정보 원문 가능성 있음
```


---

# 34. 최종 정리 — CPF 완료의 의미

CPF에서 완료는 “기능이 대충 동작한다”가 아니다.

CPF 완료는 다음을 의미한다.

```text
개발자가 신규 업무를 만들 수 있다.
운영자가 장애를 추적할 수 있다.
DB 로그와 파일 로그가 함께 남는다.
transactionGlobalId로 전체 흐름을 찾을 수 있다.
이중화 서버 중 어느 인스턴스가 처리했는지 보인다.
특정 거래/배치만 TRACE로 올려 운영 대응할 수 있다.
신규 주제영역은 생성기로 일관성 있게 온보딩할 수 있다.
ADM에서 상태/권한/감사/로그/정책을 조회하고 조치할 수 있다.
SQL/Flyway/all_install이 맞다.
MariaDB 신규 설치가 된다.
runtime smoke/browser click/OpenAPI/evidence가 있다.
실패/미검증을 숨기지 않는다.
```

따라서 이후 모든 요청서와 검수는 본 기준서의 상세 기준을 상위 원칙으로 삼는다.

---

# 35. 환경/Profile/Runtime 설정 최종 상세 기준

## 35.1 Profile 분리

CPF는 local/dev/test/stage/prod profile을 명확히 분리한다.

```text
local: 개발자 로컬 실행, embedded/mock 허용, 로그 상세 기본 허용
dev: 개발 서버, 실제 MariaDB 후보, 외부연계 mock/개발기관 혼용
test: 통합 테스트, runtime smoke, browser click, full install 검증
stage: 운영 유사 환경, prod guardrail 대부분 적용
prod: 운영 환경, 민감정보 원문 금지, 감사/거래 로그 비활성화 제한
```

## 35.2 Startup Validation

기동 시 검증해야 하는 항목:

```text
moduleCode 설정 존재
serverId/instanceId 설정 존재
port 중복 가능성 경고 후보
DB 연결 가능 여부
Flyway migration 상태
필수 테이블 존재 여부
로그 경로 쓰기 가능 여부
파일 로그 rolling 설정 유효성
masking enabled 여부
prod에서 raw-sensitive-log-enabled=false 여부
prod에서 audit/error/transaction log 비활성화 여부 guardrail
secret placeholder 미치환 여부
OpenAPI group 설정 여부
```

실패 정책:

```text
FATAL: 기동 중단
WARN: 경고 후 기동
ADM_DIAGNOSTIC: ADM 진단에 표시
```

## 35.3 환경변수 표준 후보

```text
CPF_MODULE_CODE
CPF_SERVER_ID
CPF_INSTANCE_ID
CPF_PROFILE
CPF_APP_VERSION
CPF_BUILD_VERSION
CPF_DB_URL
CPF_DB_USERNAME
CPF_DB_PASSWORD
CPF_LOG_BASE_PATH
CPF_LOG_DB_ENABLED
CPF_LOG_FILE_ENABLED
CPF_MASKING_ENABLED
CPF_TRACE_BOOST_ENABLED
CPF_ADM_ADMIN_INITIAL_PASSWORD 후보 금지 또는 bootstrap only
```

## 35.4 운영환경 금지 기준

```text
prod에서 기본 관리자 비밀번호 하드코딩 금지
prod에서 secret 평문 로그 금지
prod에서 raw payload 원문 로그 금지
prod에서 masking disabled 금지
prod에서 audit log disabled 금지 또는 승인/감사 필수
prod에서 root logger TRACE 장시간 유지 금지
```


---

# 36. CI/CD / Release / Upgrade 상세 기준

## 36.1 CI 품질 게이트

CI에서 실행 후보:

```text
compileJava
unit test
module test
mapper test
SQL standard check
Flyway validation
all_install build check
UTF-8/mojibake check
secret scan 후보
OpenAPI generation check
feature evidence check
qualityGate
```

## 36.2 Release 산출물

```text
module bootJar
migration scripts
all_install SQL
release note
upgrade guide
rollback guide
OpenAPI spec
smoke script bundle
sanitized evidence
CPF_STABILIZATION_REPORT.md
기능 매트릭스
```

## 36.3 Upgrade 기준

```text
CPF version
module version
DB schema version
migration 적용 순서
breaking change 여부
deprecated API
deprecated property
config rename guide
rollback 가능 여부
data migration 여부
```

## 36.4 Rollback 기준

```text
application rollback
DB rollback 가능/불가 명시
feature flag rollback
policy rollback
migration irreversible 여부
operator action 감사
```


---

# 37. 데이터/DB/SQL 상세 확장 기준

## 37.1 DB 공통 컬럼 후보

```text
created_at
created_by
updated_at
updated_by
deleted_yn
deleted_at
version
status
remark
trace_id
transaction_global_id 후보
```

## 37.2 SQL 파일 구조 후보

```text
db/sql/ddl/{module}/
db/sql/index/{module}/
db/sql/seed/{module}/
db/sql/smoke/{module}/
db/migration/Vxx__*.sql
00_all_install.sql
00_all_install_and_smoke.sql
99_smoke_check.sql
```

## 37.3 SQL 검증 기준

```text
split SQL과 Flyway 정합성
Flyway와 all_install 정합성
all_install과 smoke_check 정합성
idempotent seed 가능성
테이블 comment/컬럼 comment
index 누락 여부
운영 검색조건 index 후보
foreign key 정책
charset/collation
```

## 37.4 Migration 원칙

```text
기존 데이터 보존
컬럼 추가 시 nullable/default 검토
대량 테이블 alter 위험 검토
rollback 가능 여부 기록
운영 영향도 기록
MariaDB 버전 호환성 검토
```


---

# 38. 성능/부하/용량 기준

## 38.1 API 성능 기준 후보

```text
단건 조회 목표 응답시간
목록 조회 목표 응답시간
검색조건 없는 대량 조회 제한
max page size 제한
slow query threshold
slow transaction threshold
```

## 38.2 로그 용량 기준

```text
transaction log TPS별 예상 용량
integration log payload summary 크기 제한
TRACE boost 최대 TTL
TRACE boost 동시 정책 수 제한
파일 로그 total size cap
DB 로그 보존 기간별 용량 추정 후보
```

## 38.3 Batch 성능 기준

```text
job 목표 완료시간
step 목표 완료시간
chunk size 기준
partition 수 기준
center-cut 처리량
retry/skip 비율
```

## 38.4 Performance Smoke 후보

```text
목록 API 1만건 fixture 조회
keyset paging 성능
transaction group 검색 성능
fixed-length parser 1만건 benchmark
batch item 1만건 처리 후보
```


---

# 39. 장애 대응 / Resilience / Fault Injection 기준

## 39.1 장애 시나리오 후보

```text
DB connection timeout
DB deadlock
DB log insert 실패
파일 로그 경로 권한 오류
디스크 full 후보
EXS timeout
EXS 500 응답
EXS 응답 지연
retry exhaustion
circuit open
BAT worker kill
heartbeat 중단
ghost candidate 발생
lock stale
Redis down
Kafka/MQ down
OpenAPI runtime 실패
ADM permission 실패
```

## 39.2 Fault Injection 기능 후보

```text
개발/test profile에서만 활성화
특정 endpoint timeout 유도
특정 module error 유도
특정 batch step fail 유도
특정 integration retry exhaustion 유도
ADM fault injection 조회/실행 후보
```

## 39.3 Runbook 연결

각 장애 유형은 runbook과 연결되어야 한다.

```text
증상
확인할 ADM 화면
확인할 DB 테이블
확인할 파일 로그
grep 명령 후보
재처리 가능 여부
운영자 조치
주의할 민감정보
```


---

# 40. 권한/보안/운영자 UX 상세 기준

## 40.1 권한 체계

```text
ROLE 기반 권한
menu permission
button permission
api permission
data scope 후보
download permission
unmask permission
high-risk action permission
trace boost permission
batch force-run permission
lock release permission
```

## 40.2 권한 테스트 기준

```text
ADM_ADMIN 허용
ADM_VIEWER 차단
권한 없는 API 403
권한 없는 버튼 숨김/disabled
권한 변경 후 즉시/캐시 refresh 반영
권한 변경 감사
```

## 40.3 고위험 기능

```text
마스킹 해제
전문 원문 조회
다운로드
배치 강제 실행
lock 강제 해제
ghost 확정 처리
Trace Boost TRACE 적용
정책 변경
권한 변경
```

필수:

```text
사유 입력
권한 확인
감사 로그
필요 시 2인 승인 후보
```


---

# 41. 개발자 경험 / 표준 템플릿 상세 기준

## 41.1 개발자가 바로 이해해야 하는 것

```text
신규 API 추가 방법
신규 Mapper 추가 방법
신규 SQL 추가 방법
표준 헤더 사용 방법
TransactionContext 사용 방법
CpfWebClient 사용 방법
로그 남기는 방법
마스킹 적용 방법
권한 적용 방법
감사 로그 남기는 방법
smoke 추가 방법
기능 매트릭스 반영 방법
```

## 41.2 템플릿 후보

```text
controller template
service template
mapper template
dto template
validation template
sql ddl template
sql seed template
test template
smoke template
README template
ADM menu seed template
permission seed template
log policy template
```

## 41.3 금지 패턴

```text
request.getHeader 직접 남발
RestTemplate/WebClient 직접 사용
Mapper XML 없이 임시 in-memory 처리
SQL fixture 없음
무제한 목록 조회
민감정보 log.info 출력
업무 모듈 간 직접 DB JOIN
권한 체크 없이 ADM API 추가
```


---

# 42. ADM 화면별 필수 공통 컴포넌트 기준

## 42.1 목록 화면

```text
검색조건
quick filter
page size
sort whitelist
상태 badge
실패 badge
마스킹 표시
상세 이동
다운로드 버튼 권한 제어
```

## 42.2 상세 화면

```text
요약 카드
상태/실패 사유
구간별 시간
관련 로그 링크/명령 후보
header snapshot
payload masked summary
감사 이력
운영자 조치
```

## 42.3 운영자 조치 화면

```text
조치 종류
대상 식별자
사유 필수
권한 확인
실행 전 확인
실행 결과
감사 로그
rollback 가능 여부
```

## 42.4 Browser Click 기준

```text
로그인
메뉴 이동
검색조건 입력
조회 버튼 클릭
목록 row 선택
상세 탭 이동
권한 없는 버튼 확인
민감정보 미노출 확인
오류 메시지 확인
```


---

# 43. OpenAPI / API Catalog 상세 기준

## 43.1 OpenAPI 필수

```text
공통 header 문서화
확장 header 예제
공통 오류 응답
권한 필요 여부
마스킹 응답 예제
paging/search/sort 파라미터
transactionGlobalId 응답 예제
```

## 43.2 API Catalog 후보

```text
moduleCode
apiPath
httpMethod
summary
description
requiredRole
permissionCode
ownerTeam
deprecatedYn
version
smokeYn
lastVerifiedAt
```

## 43.3 ADM API Catalog 화면

```text
API 목록
module별 필터
권한 연결 상태
OpenAPI 노출 여부
runtime smoke 결과
deprecated 표시
```


---

# 44. Catalog / Registry 최종 후보

CPF는 운영자가 현재 시스템 구성을 조회할 수 있도록 catalog/registry를 제공한다.

```text
Module Catalog
API Catalog
Batch Job Catalog
Center-Cut Job Catalog
EXS Institution Catalog
EXS Endpoint Catalog
Fixed-Length Layout Catalog
Error Code Catalog
Message Code Catalog
Policy Catalog
Header Catalog
Permission Catalog
Log Policy Catalog
Feature Flag Catalog
```

각 catalog 공통 필드:

```text
code
name
description
moduleCode
enabledYn
ownerTeam
createdAt
updatedAt
version
status
```


---

# 45. 최종 마일스톤 확장 계획

## M14. 로그/Trace Boost 표준 완성

```text
cpf-{moduleCode}-{logType}.log 표준
DB 로그/파일 로그 환경 설정
이중화 인스턴스 식별자
file log grep smoke
동적 Trace Boost 온라인 거래
동적 Trace Boost 배치
ADM 로그 정책 화면
```

## M15. 신규 주제영역 온보딩 생성기

```text
create-domain CLI/Gradle task
개발자 소스 생성
PFW registry seed
ADM menu/API/button seed
SQL/Flyway/all_install patch
테스트/smoke 초안
ADM 온보딩 현황
```

## M16. EXS 송수신 원장/전문 연계 완성

```text
EXS exchange log 원장
REST 송수신 runtime smoke
fixed-length 송수신 runtime smoke
ADM external logs 상세
retry/timeout/failure smoke
```

## M17. BAT 운영/재수행/Trace Boost 완성

```text
job/step/item 상세
rerun/force/stop
worker ghost/lock
batch trace boost
ADM browser click
```

## M18. 운영자 조치/승인/Runbook

```text
고위험 조치
2인 승인 후보
runbook 연결
조치 감사
rollback 후보
```

## M19. 최종 운영 검증 패키지

```text
신규 MariaDB full install
전체 runtime E2E
ADM browser click 전체
OpenAPI runtime
file log grep
DB log query
security masking scan
release evidence package
```

