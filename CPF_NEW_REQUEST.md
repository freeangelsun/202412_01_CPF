# CPF 잔여 기능 완성 및 기능별 전수 테스트 요청서

## 1. 작업 목표

CPF(CoreFlow Platform Framework)의 현재 `부분 구현`, `미검증`, `재확인 필요` 상태로 남아 있는 기능을 실제 운영 가능한 수준으로 보강한다.

이번 작업은 문서만 보강하거나, 일부 테스트만 추가하거나, Controller만 추가하고 완료 처리하는 작업이 아니다.
기능별로 실제 package, Java source, Controller/API, Service, Repository/Mapper, DTO, SQL/Flyway, ADM UI/API wrapper, Swagger, EDU 샘플, 테스트, 검증 결과가 연결되어야 한다.

기능을 완료로 표시하려면 반드시 기능별 테스트 증적을 남긴다.
테스트하지 않은 기능은 성공으로 기록하지 않는다.
일부만 개발한 기능은 완료가 아니라 `부분 구현` 또는 `미검증`으로 남긴다.

## 2. 이번 요청에서 반복하지 않을 항목

아래 항목은 현재 기준으로 처리된 것으로 보고 반복 요청하지 않는다.

```text
- BIZADM 운영 코드 sample 폴더 제거
- EXS 운영 코드 sample 폴더 제거
- BIZADM/EXS SampleController 제거
- EXS token refresh 사유 검증을 DB 저장 전에 수행하도록 수정한 항목
- BIZADM/EXS/ADM 단위 테스트 일부 추가
- 기능 증거 gate 추가 자체
- MariaDB smoke 실행 결과 기록 자체
```

단, 위 항목도 관련 기능을 통합 테스트할 때 함께 검증 결과는 남긴다.

## 3. 절대 원칙

```text
1. 개발하지 않은 기능을 개발했다고 기록하지 않는다.
2. 일부만 개발한 기능을 완료라고 기록하지 않는다.
3. 테스트하지 않은 기능을 검증 성공으로 기록하지 않는다.
4. Controller만 있고 Service/Repository/SQL/테스트가 없으면 완료가 아니다.
5. 문서만 있고 실제 소스가 없으면 완료가 아니다.
6. 하드코딩 응답, sample, in-memory 운영 저장소는 운영 구현 완료로 보지 않는다.
7. 완료 기능은 실제 package와 파일 경로를 명확히 기록한다.
8. 기능별 테스트 결과를 하나하나 CPF_STABILIZATION_REPORT.html에 남긴다.
9. 실행하지 못한 검증은 성공으로 쓰지 말고 미검증 사유를 기록한다.
10. Codex 완료 메시지와 CPF_STABILIZATION_REPORT.html 내용이 서로 다르면 안 된다.
```

프로그램 수정, 기능 추가/삭제/변경, SQL/API/UI/Swagger/EDU/검증 기준 변경이 발생하면 관련 README와 specs 정본 문서를 반드시 최신화한다.

문서는 실제 구현 기준 정본이어야 하며, 문서만 봐도 기능, API, 소스 위치, DB 테이블, UI, Swagger, EDU 샘플, 테스트 방법, 검증 결과, 미검증 사유를 파악할 수 있어야 한다.

## 4. 기능별 완료 조건

각 기능은 아래 항목이 모두 연결되어야 `완료`로 기록할 수 있다.

```text
- 담당 모듈
- 실제 package 경로
- 실제 Java 소스 파일
- Controller/API
- Service
- Repository 또는 Mapper
- DTO
- SQL/Flyway/all_install 반영
- DB 테이블과 주요 컬럼 COMMENT
- ADM UI 또는 API wrapper
- Swagger Tag/Operation/Schema
- EDU 샘플 또는 개발 가이드 연결
- 단위 테스트
- 통합 테스트 또는 API 테스트
- DB 저장/조회 검증
- 오류/권한/검증 실패 테스트
- 검증 명령
- 실제 검증 결과
```

위 항목 중 하나라도 빠지면 완료가 아니다.
빠진 항목은 실제 개발하거나, 이번 작업에서 완료할 수 없으면 사유와 다음 조치를 남긴다.

## 5. 기능별 전수 테스트 방식

각 기능은 가능한 범위에서 아래 테스트를 수행한다.

### 5.1 단위 테스트

Service, Validator, Policy, DTO validation, Mapper 보조 로직은 단위 테스트를 작성한다.

테스트 케이스:

```text
- 정상 케이스
- 필수값 누락
- 잘못된 상태값
- 권한 없음
- 중복 데이터
- 존재하지 않는 ID
- 만료 token
- 실패/예외 케이스
```

권장 명령:

```powershell
.\gradlew.bat test --offline
```

### 5.2 Repository/Mapper/SQL 테스트

DB를 사용하는 기능은 Mapper/Repository 테스트 또는 SQL smoke로 검증한다.

확인 기준:

```text
- insert 성공
- select 성공
- update 성공
- delete 또는 상태 변경 성공
- FK/index 위반 케이스 확인
- 중복키/필수값 오류 확인
- 공통 감사 컬럼 저장 확인
```

가능하면 MariaDB 기준으로 확인한다.

```powershell
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
```

### 5.3 API 테스트

Controller가 있는 기능은 API 호출로 테스트한다.

확인 기준:

```text
- 정상 요청
- 필수 헤더 누락
- transactionGlobalId 누락/전파
- 필수 파라미터 누락
- 권한 없음
- 존재하지 않는 ID
- 상태 변경 성공
- 상태 변경 실패
- 표준 오류 응답
```

권장 방식:

```powershell
curl -i http://localhost:<port>/v3/api-docs
curl -i http://localhost:<port>/<api-path>
```

### 5.4 ADM UI/API wrapper 테스트

ADM에서 운영자가 봐야 하는 기능은 UI 또는 API wrapper 기준으로 확인한다.

확인 기준:

```text
- 메뉴 표시
- 목록 조회
- 상세 조회
- 등록/수정/상태 변경
- 권한 없는 버튼 숨김 또는 비활성
- 서버 API 권한 차단
- 오류 메시지 표시
- 감사 로그 저장
```

브라우저 클릭을 수행하지 못하면 `브라우저 클릭 미검증`으로 남긴다.
단순 정적 marker 확인을 브라우저 검증 성공으로 기록하지 않는다.

### 5.5 Swagger/OpenAPI 테스트

앱을 기동한 뒤 OpenAPI 문서를 확인한다.

확인 기준:

```text
- /v3/api-docs 응답
- /swagger-ui/index.html 접근
- 주요 Controller @Tag 표시
- 주요 API @Operation 표시
- 요청/응답 DTO schema 표시
- 표준 오류 응답 schema 표시
```

앱을 기동하지 못하면 OpenAPI 성공으로 기록하지 않는다.

### 5.6 감사/로그 테스트

로그와 감사 기능은 실제 DB 저장 여부를 확인한다.

확인 기준:

```text
- transactionGlobalId 저장
- 사용자/운영자 ID 저장
- 처리 시각 저장
- 요청 URI/API 저장
- 처리 결과 저장
- 오류 메시지 저장
- 변경 전/후 값 저장
- 다운로드 사유 저장
- 마스킹 해제 사유 저장
```

## 6. PFW 기능 보강 및 테스트

PFW는 CPF 프레임워크 코어 영역이다.

### 6.1 표준 응답/오류/예외

현재 상태: 부분 구현.

개발/검증 대상:

```text
- 표준 성공 응답 DTO
- 표준 오류 응답 DTO
- 공통 예외
- 업무 예외
- 시스템 예외
- Validation 예외
- 전역 Exception Handler
- 오류 코드/메시지 DB 연계
- Swagger 오류 schema
- EDU 오류 예제
```

필수 테스트:

```text
- 정상 응답 테스트
- Validation 오류 테스트
- 업무 예외 테스트
- 시스템 예외 테스트
- 존재하지 않는 리소스 오류 테스트
- 표준 오류 code/message/transactionGlobalId 포함 여부 테스트
- OpenAPI JSON에서 표준 오류 schema 확인
```

완료 기준:

```text
- 주요 API가 표준 응답/오류 구조를 사용한다.
- 오류 유형별 응답이 구분된다.
- Swagger와 EDU에 예시가 있다.
- 테스트 결과가 리포트에 남는다.
```

### 6.2 transactionGlobalId / trace / 표준 헤더

현재 상태: 구현 검증이나 운영 로그 지속 확인 필요.

필수 테스트:

```text
- transactionGlobalId 헤더가 있는 요청
- transactionGlobalId 헤더가 없는 요청
- 응답 헤더 전파 확인
- 거래 로그 저장 확인
- 오류 로그 저장 확인
- EXS 수신/송신 로그 연계 확인
- 배치 실행 로그 연계 확인
```

### 6.3 PFW 거래 로그/감사 로그

현재 상태: 부분 구현.

개발/검증 대상:

```text
- 거래 로그 저장
- 오류 로그 저장
- 감사 로그 저장
- 요청/응답 메타 저장
- 사용자/운영자/모듈/서버/시간 기록
- 민감정보 마스킹
- ADM 조회 API 연계
- 브라우저 로그 상세 탭 검증
- 원문 보기 권한 검증
```

필수 테스트:

```text
- 정상 API 호출 후 거래 로그 저장 확인
- 오류 API 호출 후 오류 로그 저장 확인
- 권한 변경 후 감사 로그 저장 확인
- 다운로드 후 감사 로그 저장 확인
- 마스킹 적용 확인
- ADM 로그 상세 API 또는 화면 확인
```

### 6.4 PFW Batch 공통 API

현재 상태: 부분 구현.

개발/검증 대상:

```text
- Batch 실행 요청 모델
- Batch 실행 결과 모델
- CpfBatchLauncher
- batch lock
- 중복 실행 방지
- 재실행 가능 여부 판단
- 실행 전/후 이벤트
- 실패/지연/미수행 알림 이벤트
- JobExecutionId와 pfw_batch_execution 연결
- StepExecutionId와 pfw_batch_step 연결
- transactionGlobalId 전파
- ADM 배치 관제 API 연계
- EDU 배치 예제
```

필수 테스트:

```text
- 정상 Job 실행
- Step 성공 기록
- Job 실패 기록
- 중복 실행 차단
- 재실행 가능 여부 판단
- JobExecutionId 저장 확인
- StepExecutionId 저장 확인
- transactionGlobalId 저장 확인
- ADM 배치 관제 조회 확인
```

## 7. CMN 기능 보강 및 테스트

현재 상태: 채번, 업무 알림 로그, 업무 로그 모두 부분 구현.

개발/검증 대상:

```text
- 업무 공통 채번
- 주문번호/신청번호/문서번호/접수번호 발급 구조
- prefix/suffix/자리수/일자 패턴
- reset cycle 정책
- MariaDB 격리수준 기준 동시성 검증
- 발급 이력
- 업무 알림 요청
- 업무 알림 처리 이력
- 업무 이벤트 로그
- 업무 상태 변경 이력
- 업무 공통 Validation
- 업무 공통 파일/전문/마스킹 보조 유틸
- WebClient adapter
- Redis/Kafka/MQ mock/fallback adapter
- EDU CMN 샘플
```

필수 테스트:

```text
- 채번 정상 발급
- 업무키별 채번 분리
- 일자 패턴 적용
- 중복 발급 방지
- 발급 이력 저장
- 업무 알림 요청 저장
- 업무 이벤트 로그 저장
- 업무 상태 변경 이력 저장
- MariaDB 기준 동시성 또는 격리수준 검증
```

완료 기준:

```text
- CMN은 PFW 표준 기능을 원천 관리하지 않는다.
- 업무 공통 기능이 DB 기반으로 동작한다.
- 테스트 결과가 있다.
```

## 8. ADM 기능 보강 및 테스트

ADM은 운영자가 실제로 조회, 조치, 감사할 수 있어야 한다.

### 8.1 운영자/역할/메뉴/버튼/API 권한

현재 상태: 부분 구현.

개발/검증 대상:

```text
- 운영자 CRUD
- 운영자 잠금/해제
- 비밀번호 초기화
- 역할 CRUD
- 메뉴 권한
- 버튼 권한
- API 권한
- 다운로드 권한
- 권한 변경 전/후 이력
- 권한 변경 감사 로그
- 서버 API 권한 차단
- UI 버튼 숨김/비활성
- 버튼 비활성 사유 UX
```

필수 테스트:

```text
- 운영자 등록/조회/수정/잠금
- 역할 등록/수정
- 메뉴 권한 부여/회수
- 버튼 권한 부여/회수
- API 권한 부여/회수
- 다운로드 권한 부여/회수
- 권한 없는 API 호출 차단
- 권한 변경 감사 로그 저장
- 브라우저 또는 API 통합 테스트
```

### 8.2 회원 관리와 회원 권한

현재 상태: 부분 구현.

필수 테스트:

```text
- 회원 목록 조회
- 회원 상세 조회
- 회원 상태 변경
- 회원 잠금/해제
- 회원 탈퇴/복구 가능 여부
- 회원 권한 부여/회수
- 권한 변경 이력 저장
- ADM 회원 화면/API wrapper 확인
```

### 8.3 로그 관제

현재 상태: 부분 구현.

필수 테스트:

```text
- 거래 로그 목록 조회
- transactionGlobalId 검색
- 오류 로그 조회
- 오류 상태 변경
- 담당자 지정
- 조치 메모 저장
- 조치 이력 저장 확인
- JSON pretty print 표시 확인
- 고정길이 전문 포맷 표시 확인
- 다운로드 권한 확인
```

### 8.4 배치 관제

현재 상태: 부분 구현.

개발/검증 대상:

```text
- Job 목록
- Job 상세
- Execution 이력
- Step 이력
- 수동 실행
- 재실행
- 중지 요청
- 실패 사유 조회
- 배치 알림 이력
- 영업일 수행 시뮬레이션
- 수행 가능 시간
- 다음 수행일 계산
- 선행/트리거 관계도
- 수행대기 인스턴스 UX
```

필수 테스트:

```text
- Job 목록 조회
- Job 실행 이력 조회
- Step 이력 조회
- 수동 실행
- 실패 Job 재실행
- 중지 요청
- 실패 사유 조회
- 영업일 시뮬레이션
- 선행/트리거 관계 조회
```

### 8.5 캐시/메시지/코드/설정

현재 상태: 부분 구현.

필수 테스트:

```text
- 캐시 refresh 요청
- 캐시 refresh 실패 이력 저장
- 메시지 등록/수정/조회
- 코드 등록/수정/조회
- 응답코드 등록/수정/조회
- 설정 등록/수정/조회
- 변경 감사 diff 저장
- 브라우저 편집 검증
```

### 8.6 다운로드 감사/마스킹 감사

현재 상태: 부분 구현.

필수 테스트:

```text
- 다운로드 권한 있음
- 다운로드 권한 없음
- 다운로드 사유 없음
- CSV 다운로드 E2E
- 다운로드 감사 저장
- 마스킹 적용 조회
- 원문 보기 권한 있음
- 원문 보기 권한 없음
- 마스킹 해제 사유 없음
- 마스킹 해제 감사 저장
```

## 9. BIZADM 기본 구현체 보강 및 테스트

현재 상태: 부분 구현.

개발/검증 대상:

```text
- 업무 관리자 로그인 API 통합 테스트
- 로그인 실패 이력 저장
- refresh token hash 저장 검증
- token 원문 미저장 검증
- 고객 등록/조회/수정
- 상품 등록/조회/수정
- 주문 등록/조회/상태 변경
- 프로젝트 설정 조회/수정
- 권한 없는 API 차단
- 다운로드 감사 저장
- 마스킹 해제 감사 저장
- ADM 관제 wrapper 확인
- Swagger 문서화 확인
```

완료 기준:

```text
- BIZADM은 실제 DB 기반으로 동작한다.
- sample/hardcoding이 아니다.
- ADM에서 관제 또는 wrapper 접근이 가능하다.
- 인증/refresh token/권한/감사까지 통합 테스트가 있다.
```

## 10. MBR 회원 기본 구현체 보강 및 테스트

현재 상태: 부분 구현.

개발/검증 대상:

```text
- 회원 등록
- 회원 조회
- 회원 수정
- 회원 상태 변경
- 로그인
- refresh token hash 저장
- 로그인 성공/실패 이력
- 회원 잠금
- 휴면
- 탈퇴
- 권한/역할 연결
- 회원 권한 만료
- 회원 마스킹
- 회원 다운로드 감사
- ADM 회원 관제 wrapper
- Swagger 문서화
```

필수 테스트:

```text
- 회원 등록
- 회원 조회
- 회원 수정
- 회원 상태 변경
- 로그인 성공
- 로그인 실패
- refresh token hash 저장 확인
- 회원 잠금
- 휴면 처리
- 탈퇴 처리
- 권한 만료 처리
- 마스킹 적용 조회
- 회원 다운로드 감사 저장
- API 통합 테스트
```

## 11. EXS 대외연계 기본 구현체 보강 및 테스트

현재 상태: 부분 구현.

이미 수정된 항목은 반복하지 않는다.

```text
- token refresh 사유 누락 시 token hash와 event 저장 차단
- token 원문 미노출
- token hash 저장
- retry 사유 필수
- inbound 공식 URI 기본값
```

추가 보강/검증 대상:

```text
- 기관 등록/조회/수정
- 채널 등록/조회/수정
- endpoint 등록/조회/수정
- 인증 프로파일 등록/조회
- token refresh API 통합 테스트
- token event history 조회
- routing rule 등록/조회/수정
- inbound 수신 거래 로그 선저장
- outbound 송신 거래 로그 선저장
- message log 저장
- 사용 중지 기관 차단
- 사용 중지 채널 차단
- 사용 중지 endpoint 차단
- 성공 상태 갱신
- 실패 상태 갱신
- retryable 판단
- 재처리 요청 저장
- retry worker 또는 재처리 실행 흐름 검증
- 재처리 결과 저장
- 재처리 감사 로그 저장
- 실제 외부기관 adapter mock 검증
- ADM 관제 wrapper 확인
- Swagger 문서화 확인
```

완료 기준:

```text
- inbound/outbound 요청이 DB에 선저장된다.
- transaction log와 message log가 남는다.
- routing rule과 기관/채널/endpoint 통제가 적용된다.
- 재처리 요청/실행/결과/감사가 남는다.
- 실기관 연동이 없으면 mock/fallback 기준으로 검증한다.
```

## 12. BAT/Batch 실행 구현체 보강 및 테스트

현재 상태: 부분 구현.

개발/검증 대상:

```text
- PFW CpfBatchLauncher 사용
- 표준 Job/Step 예제
- 업무 Service/Facade 호출 배치
- Job parameter 표준화
- transactionGlobalId 전파
- JobExecutionId 저장
- StepExecutionId 저장
- 실패 처리
- 재실행 처리
- 중복 실행 방지
- ADM 배치 관제 연결
- Spring Batch JobRepository 시나리오
- scheduler 실기동 또는 mock 검증
```

필수 테스트:

```text
- Job 정상 실행
- Step 정상 실행
- Job 실패 처리
- 재실행 처리
- 중복 실행 방지
- JobExecutionId 저장 확인
- StepExecutionId 저장 확인
- ADM 배치 이력 조회
```

완료 기준:

```text
- 독립 JobRepository를 임의 생성하지 않는다.
- PFW 배치 공통 API를 통해 실행된다.
- ADM에서 실행/실패/재실행 이력을 볼 수 있다.
```

## 13. EDU 개발 교육 샘플 보강 및 테스트

현재 상태: 부분 구현.

개발/검증 대상:

```text
- CRUD 샘플
- 표준 응답 샘플
- 표준 오류 샘플
- transactionGlobalId 샘플
- 권한 검사 샘플
- 감사 로그 샘플
- CMN 채번 샘플
- CMN 업무 알림/업무 로그 샘플
- PFW 배치 공통 API 샘플
- EXS 호출 샘플
- 마스킹 샘플
- 다운로드 감사 샘플
- 개발자가 따라 할 수 있는 주석 보강
```

필수 테스트:

```text
- EDU CRUD API 호출
- 표준 응답 확인
- 표준 오류 확인
- transactionGlobalId 확인
- CMN 채번 호출
- PFW 배치 API 호출
- EXS 호출 mock
- 마스킹 샘플 확인
- 다운로드 감사 샘플 확인
```

## 14. Swagger/OpenAPI 전수 테스트

현재 상태: 앱 미기동으로 미검증.

확인 대상:

```text
- ADM API
- BIZADM API
- MBR API
- EXS API
- EDU API
- PFW 공통 API 중 노출 대상
```

필수 테스트:

```text
- 앱 기동
- /v3/api-docs 호출
- /swagger-ui/index.html 접근
- 주요 API Tag 확인
- 주요 Operation 확인
- 요청/응답 schema 확인
- 표준 오류 schema 확인
```

실행하지 못하면 앱별로 미검증 사유를 남긴다.

## 15. ADM 실제 브라우저 클릭 검증

현재 상태: 정적 smoke만 성공, 실제 브라우저 클릭 미검증.

확인 대상:

```text
- 로그인 화면
- 대시보드
- 운영자/권한
- 회원 관리
- 거래 로그
- 오류 로그
- 배치 관제
- 알림/캐시/메시지/코드/설정
- 다운로드 감사
- BIZADM 관제
- EXS 관제
```

필수 기록:

```text
- 기동 방식
- 접속 URL
- 확인한 메뉴
- 성공한 클릭
- 실패한 클릭
- 오류 메시지
- 미검증 사유
```

브라우저 클릭을 수행하지 않았으면 성공으로 쓰지 않는다.

## 16. Redis/Kafka/MQ mock/fallback 테스트

현재 상태: 실 broker 미검증.

실제 broker가 없으면 실연동 성공을 완료 기준으로 삼지 않는다.
대신 mock/fallback 테스트를 수행한다.

필수 테스트:

```text
- Redis 미설정 상태 앱 기동
- Kafka 미설정 상태 앱 기동
- MQ 미설정 상태 앱 기동
- 알림 발송 mock/fallback
- 동적 로그레벨 전파 mock/fallback
- 캐시 refresh mock/fallback
- 실패 이력 저장
```

리포트에는 아래를 구분한다.

```text
- mock/fallback으로 검증한 항목
- 실 broker가 없어 미검증인 항목
- 실 broker 연결 시 실행할 검증 절차
```

## 17. SQL/Flyway/Mapper 정합성 전수 테스트

현재 상태: MariaDB smoke는 성공 기록이 있으나, 기능별 통합 테스트 확대가 필요하다.

필수 확인:

```text
- Java에서 사용하는 모든 테이블 존재
- Mapper method와 XML SQL 일치
- 신규 테이블 COMMENT 존재
- 공통 감사 컬럼 존재
- FK/index 존재
- all_install 반영
- all_install_and_smoke 반영
- Flyway baseline 반영
- app/migration 계정 권한 분리 확인
```

필수 테스트:

```powershell
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
```

실행하지 못하면 사유를 남긴다.

## 18. 40_business_sample_schema.sql 명칭 정리 여부 결정

현재 `specs/sql/40_business_sample_schema.sql` 파일명은 남아 있다.
운영 코드 sample 폴더와는 별개로, SQL 파일명이 운영 기본 구현체 성격과 맞는지 결정한다.

처리 기준:

```text
A. 운영 도메인 성격에 맞게 파일명 변경
   예: 40_business_domain_schema.sql 또는 40_business_modules_schema.sql

B. 이번 단계에서 변경하지 않으면 보류 사유와 다음 조치를 리포트에 기록
```

파일명을 변경하는 경우 아래 참조를 모두 수정한다.

```text
- specs/sql/00_all_install.sql
- specs/sql/00_all_install_and_smoke.sql
- specs/sql/migration/flyway/V1__cpf_baseline_install.sql
- README.md
- specs/SQL_가이드.html
- specs/기능_구현_매트릭스.html
- CPF_STABILIZATION_REPORT.html
```

## 19. README/specs 문서 최신화

대상 문서:

```text
README.md
specs/index.html
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
```

각 기능별로 아래를 문서에 반영한다.

```text
- 기능 목적
- 담당 모듈
- package 경로
- 주요 소스 파일
- Controller/API
- Service
- Repository/Mapper
- DTO
- SQL/Flyway/테이블
- ADM UI/API wrapper
- Swagger Tag
- EDU 샘플
- 테스트 방법
- 실제 테스트 결과
- 미검증 사유
```

문서에 완료라고 쓰는 항목은 실제 구현과 테스트 근거가 있어야 한다.

## 20. 전체 실행 명령

가능한 범위에서 아래를 실행한다.

```powershell
.\gradlew.bat clean qualityGate --offline
.\gradlew.bat compileJava --offline
.\gradlew.bat test --offline

.\gradlew.bat :bizadm:test --offline --tests cpf.bizadm.operation.service.BizAdmOperationServiceTest
.\gradlew.bat :exs:test --offline --tests cpf.exs.operation.service.ExsOperationServiceTest
.\gradlew.bat :adm:test --offline --tests cpf.adm.opr.service.AdmDownloadServiceTest
.\gradlew.bat :mbr:test --offline --tests cpf.mbr.bse.controller.MbrControllerValidationTest

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sample-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-legacy-name.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-security-seed-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-transaction-id-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
```

가능하면 앱을 기동하고 아래를 실행한다.

```powershell
.\gradlew.bat runLocalServices --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1
node --version
node --check adm/src/main/resources/static/adm/adm.js
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
```

실행하지 못한 항목은 성공으로 기록하지 않는다.

## 21. 기능별 테스트 증적 기록 방식

`CPF_STABILIZATION_REPORT.html`에는 기능별로 아래 형식으로 기록한다.

```text
[기능명]
담당 모듈:
package:
Controller/API:
Service:
Repository/Mapper:
DTO:
SQL 테이블:
ADM UI/API wrapper:
Swagger:
EDU 샘플:
테스트 종류:
테스트 명령:
테스트 데이터:
정상 케이스 결과:
오류 케이스 결과:
권한 케이스 결과:
DB 확인 결과:
최종 상태:
미검증 사유:
다음 조치:
```

완료 기능은 테스트 증적이 있어야 한다.
테스트 증적이 없으면 완료로 기록하지 않는다.

## 22. CPF_STABILIZATION_REPORT.html 기록 기준

`CPF_STABILIZATION_REPORT.html`에는 작업 결과를 정확하고 확실하게 기록한다.

요청사항별로 아래를 빠짐없이 작성한다.

```text
- 실제 확인한 내용
- 개발한 기능
- package 경로
- 생성/수정한 소스 파일
- Controller/API
- Service
- Repository/Mapper
- DTO
- SQL/Flyway/테이블
- ADM UI/API wrapper
- Swagger
- EDU 샘플
- 테스트 명령
- 테스트 결과
- 실패 사유
- 미검증 사유
- 남은 보완 사항
- 최종 판정
```

완료하지 않은 항목을 완료로 기록하지 않는다.
실행하지 않은 테스트를 성공으로 기록하지 않는다.
Codex 완료 메시지와 리포트 내용이 서로 다르게 작성되지 않도록 한다.

## 23. 완료 기준

아래를 모두 만족해야 완료다.

```text
- 기능 구현 매트릭스의 부분 구현 항목이 실제 개발/테스트로 완료되거나, 남은 사유가 명확히 기록된다.
- 개발했다고 보고한 기능은 실제 package와 소스 파일이 존재한다.
- Controller, Service, Repository/Mapper, DTO, SQL이 연결되어 있다.
- ADM에서 운영자가 조회/조치할 수 있는 기능은 UI/API wrapper가 있다.
- Swagger/OpenAPI 문서가 있다.
- EDU 샘플 또는 개발 가이드 연결이 있다.
- 기능별 정상/오류/권한/DB 확인 테스트 결과가 있다.
- 전체 qualityGate, compileJava, test 결과가 있다.
- README/specs 문서가 실제 구현과 일치한다.
- 기능 구현 매트릭스가 실제 상태를 반영한다.
- CPF_STABILIZATION_REPORT.html에 기능별 증적이 정확히 남아 있다.
```

## 24. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text
- 일부만 개발하고 완료라고 보고
- Controller만 만들고 완료라고 보고
- 문서만 만들고 완료라고 보고
- 테스트 없이 완료라고 보고
- 테스트 명령 없이 검증 성공이라고 보고
- 하드코딩 응답으로 완료라고 보고
- sample 구조를 운영 구현체로 사용
- in-memory 저장소를 운영 저장소처럼 사용
- package나 소스 파일이 없는데 기능 완료라고 기록
- SQL 없이 DB 기능 완료라고 기록
- 앱 미기동인데 OpenAPI 성공이라고 기록
- 브라우저 검증 없이 ADM UI 성공이라고 기록
- mock/fallback 검증 없이 Redis/Kafka/MQ 대응 완료라고 기록
- CPF_STABILIZATION_REPORT.html과 실제 소스가 다름
```

## 25. 최종 보고 형식

작업 완료 후 아래 형식으로 보고한다.

```text
[전체 판정]
완료:
부분 구현:
미구현:
미검증:
실패:
재확인 필요:

[기능별 개발 및 테스트 결과]
기능명:
담당 모듈:
package:
Controller:
Service:
Repository/Mapper:
DTO:
SQL:
UI/API wrapper:
Swagger:
EDU:
테스트 명령:
정상 테스트:
오류 테스트:
권한 테스트:
DB 확인:
최종 판정:

[PFW]
개발 기능:
테스트 결과:
미검증:

[CMN]
개발 기능:
테스트 결과:
미검증:

[ADM]
개발 기능:
UI/API wrapper:
브라우저 또는 API 테스트:
미검증:

[BIZADM]
개발 기능:
테스트 결과:
미검증:

[MBR]
개발 기능:
테스트 결과:
미검증:

[EXS]
개발 기능:
테스트 결과:
미검증:

[BAT]
개발 기능:
테스트 결과:
미검증:

[EDU]
개발 샘플:
테스트 결과:
가이드 연결:

[SQL/Flyway]
변경 파일:
MariaDB 테스트 결과:
미검증:

[Swagger/OpenAPI]
기동 앱:
확인 URL:
결과:
미검증:

[ADM 브라우저 클릭]
기동 방식:
접속 URL:
확인 메뉴:
성공:
실패:
미검증:

[Redis/Kafka/MQ]
mock/fallback 테스트:
실 broker 미검증:
다음 조치:

[40_business_sample_schema.sql]
변경 여부:
보류 사유:
참조 수정:

[문서]
README:
index:
프레임워크 구성 가이드:
개발 가이드:
관리자 가이드:
SQL 가이드:
기능 구현 매트릭스:

[리포트]
CPF_STABILIZATION_REPORT.html 기록 완료 여부:
기능별 테스트 증적 기록 여부:
미검증/실패 사유 기록 여부:
```
