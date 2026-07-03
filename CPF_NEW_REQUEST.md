# CPF 다음 작업 요청서 — 복합 거래 trace runtime 폐쇄, ADM 거래 그룹 UI, MariaDB 회귀, CMN/EXS 보강

## 0. CPF 최종 목표 기준

작업 시작 전에 repo 루트의 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 반드시 확인한다.

CPF는 단순 샘플이나 문서 모음이 아니라, 금융권을 포함한 범용 업무 시스템에서 실제 프로젝트에 적용 가능한 운영형 공통 프레임워크다.

이번 작업의 최상위 판단 기준은 다음이다.

* 소스만 있으면 완료가 아니다.
* Source-Level 테스트와 Runtime E2E는 구분한다.
* 정적 UI marker smoke와 browser click 검증은 구분한다.
* 기존 개발 DB 확인과 신규 빈 MariaDB full install은 구분한다.
* 실행하지 않은 검증은 완료가 아니라 미검증이다.
* Codex 보고는 실제 파일, 테스트, SQL, smoke, evidence, 리포트와 일치해야 한다.

## 1. 이번 작업 목적

직전 작업에서 추가된 PFW 복합 거래 segment 표준, ACC/MBR/EXS 복합 거래 샘플, ADM transaction group API를 runtime 기준으로 닫는다.

이번 작업은 단순 추가 구현이 아니라, transactionGlobalId 하나로 실제 runtime 거래 구간이 DB에 저장되고, ADM에서 그룹 목록/상세/timeline/header/external logs로 조회되는지 확인하는 것이 핵심이다.

동시에 ADM 거래 그룹 UI를 연결하고, MariaDB full install 회귀 검증을 수행하며, CMN fixed-length 전문 엔진과 EXS 송수신 로그 보강을 다음 단계로 진전시킨다.

## 2. 필수 제한

아래는 반드시 지킨다.

* Git commit 금지
* Git push 금지
* branch 생성 금지
* 민감정보 원문 기록 금지
* Authorization, Bearer token, X-Api-Key, password, secret, credential 원문 로그/리포트/evidence 기록 금지
* 실행하지 않은 검증을 완료로 기록 금지
* 별도 변경파일 목록 산출물 생성 금지
* 문서 포맷 정리만으로 기능 완료 처리 금지
* mock/embedded 검증을 실 runtime 또는 실 broker 검증으로 기록 금지

## 3. 필수 완료 범위

### 3.1 복합 거래 runtime smoke 폐쇄

아래 서비스를 로컬 runtime으로 기동한 뒤 검증한다.

* ACC 8080
* MBR 8081
* ADM 8090
* EXS 8092

필수 실행:

* `scripts/smoke-composite-transaction-runtime.ps1`
* `scripts/smoke-adm-transaction-group-runtime.ps1`

필수 검증 조건:

* ACC → MBR → ACC → EXS → ACC 흐름이 runtime에서 성공해야 한다.
* transactionGlobalId가 생성되어야 한다.
* transactionSegmentId가 최소 3개 이상 생성되어야 한다.
* ACC, MBR, EXS segment가 모두 확인되어야 한다.
* segmentId 중복이 없어야 한다.
* 전체 status가 SUCCESS여야 한다.
* DB의 `pfw_transaction_segment`에 실제 row가 저장되어야 한다.
* ADM transaction group API에서 동일 transactionGlobalId로 목록/상세/segments/timeline/headers/external-logs 조회가 가능해야 한다.
* response/evidence에 Bearer token, Authorization 원문, password, secret, api-key 원문이 없어야 한다.

완료로 인정하려면 runtime smoke 결과 JSON 또는 로그 경로를 `CPF_STABILIZATION_REPORT.md`에 기록한다.

### 3.2 ADM 거래 그룹 UI 연결

ADM 화면에 transactionGlobalId 기준 거래 그룹 조회 UI를 연결한다.

필수 화면 구성:

* 거래 그룹 목록
* 거래 그룹 상세
* segment timeline
* 구간별 수행시간
* 실패 구간 표시
* 표준 헤더 snapshot
* 확장 헤더 snapshot
* external logs 탭
* 검색조건 영역
* 정렬/페이징 기본 동작

목록 필수 필드:

* transactionGlobalId
* 거래명/API
* 최초 모듈
* 호출 흐름
* 시작시간
* 종료시간
* 전체 소요시간
* 상태
* 실패 여부
* 실패 구간
* 고객번호/회원번호/사용자ID/운영자ID 후보
* 채널/최초채널
* 외부기관
* 외부거래ID

검색조건 필수 후보:

* 거래일시 from/to
* transactionGlobalId
* transactionSegmentId
* 상태
* 실패 여부
* moduleCode
* sourceModuleCode
* targetModuleCode
* transactionRole
* direction
* 고객번호
* 회원번호
* 사용자ID
* 운영자ID
* 채널
* 최초채널
* 외부기관
* 외부거래ID
* API path
* 거래명
* 오류코드
* 소요시간 범위
* 표준 헤더 주요 컬럼
* 확장 헤더 제한 검색 후보

민감정보 기준:

* Authorization 원문 노출 금지
* X-Api-Key 원문 노출 금지
* token/secret/password/credential/signature 계열 원문 노출 금지
* 확장 헤더 `X-Cpf-Ext-*` 중 민감 우회 키는 저장/전파/검색/노출 금지
* 목록/상세/검색/숨김/마스킹 대상 필드를 소스와 문서에 분리 기록

검증:

* 정적 UI marker smoke 수행
* 가능한 경우 browser click smoke 수행
* browser click을 실행하지 못하면 완료가 아니라 미검증으로 기록하고 사유를 남긴다.

### 3.3 MariaDB full install 회귀 검증

신규 빈 MariaDB 기준 full install 회귀를 수행한다.

필수 확인:

* split SQL
* Flyway migration
* `00_all_install.sql`
* `00_all_install_and_smoke.sql`
* `99_smoke_check.sql`
* `pfw_transaction_segment` 생성 여부
* FK/index/unique/idempotent seed 정합성
* ADM transaction group 조회에 필요한 컬럼 존재 여부
* header snapshot/extension header/external institution/external transaction id 컬럼 존재 여부

필수 실행:

* 기존 full install smoke script 또는 동등한 신규 빈 DB 검증 스크립트
* `scripts/check-sql-standard.ps1`
* `scripts/build-all-install-sql.ps1`

주의:

* 기존 개발 DB에서 일부 테이블만 확인한 것은 full install 완료로 인정하지 않는다.
* DB 초기화 위험 때문에 실행하지 못한 경우 `mariadb-full-install`은 미검증으로 남긴다.

### 3.4 상태값 과장 보정

`CPF_STABILIZATION_REPORT.md`와 기능 매트릭스의 상태값을 CPF 기준에 맞춘다.

반드시 보정할 항목:

* `complex-transaction-trace`는 runtime smoke 전까지 완료가 아니라 부분 구현 또는 미검증이다.
* `transaction-segment-log`는 SQL과 MyBatis 경로만으로 완료 처리하지 말고, DB 저장 runtime 증거가 있어야 완료다.
* `pfw_transaction_segment SQL`은 MariaDB full install 전까지 SQL 소스 반영 완료와 full install 미검증을 분리한다.
* `CMN fixed-length engine`은 skeleton/registry/type converter 수준과 완성형 엔진 수준을 분리한다.
* ADM API 추가와 ADM UI/browser click 완료를 분리한다.

### 3.5 UTF-8/mojibake 정리 및 gate 보강

현재 소스에 mojibake로 보이는 주석이 남아 있으므로 전수 확인한다.

필수:

* `MbrMemberClientService.java`의 깨진 주석 정리
* 다른 Java/SQL/MD/HTML 파일의 mojibake 검색
* `scripts/check-utf8.ps1 -CheckMojibake`가 해당 유형을 잡도록 보강
* 보강 후 `check-utf8` 재실행
* 발견한 mojibake는 원문을 리포트에 길게 붙이지 말고 파일명/대상만 기록

## 4. 보강 범위

### 4.1 표준 헤더 E2E 재실행

가능하면 최신 기준으로 `scripts/smoke-standard-header-e2e.ps1`를 재실행한다.

확인:

* 수신 헤더 검증
* TransactionContext 생성
* transactionGlobalId 생성/전파
* root/parent/segment header 전파
* X-Original-Channel-Code와 X-Channel-Code 구분
* X-User-Id, X-Customer-No, X-Member-No 의미 분리
* 민감 헤더 원문 저장 금지

실행하지 못하면 미검증으로 기록한다.

### 4.2 CMN fixed-length 전문 엔진 보강

skeleton을 실제 프로젝트에서 재사용 가능한 수준으로 한 단계 보강한다.

보강 대상:

* 반복부 group parse/format
* 필드 타입 변환
* padding/trim
* length validation
* 필드별 오류코드
* layout registry
* layout key/version 관리
* masking policy
* 샘플 fixture
* 단위 테스트

이번 작업에서 DB 기반 전문 사전까지 모두 완성하지 못해도 된다. 단, skeleton 완료와 엔진 완성을 혼동하지 않는다.

### 4.3 EXS 송수신 로그 기반 보강

EXS REST/전문 연계 운영 조회 기반을 착수한다.

보강 대상:

* 기관 코드
* endpoint 코드
* timeout/retry 설정
* 요청/응답 로그
* 응답 코드 매핑
* externalTransactionId
* transactionGlobalId 연결
* ADM external logs 조회 연결
* 민감 payload/header 마스킹

이번 작업에서는 최소 REST 송수신 로그 기반을 우선하고, 고정길이 전문 송수신 완성은 착수 또는 후순위로 남겨도 된다.

## 5. 착수 범위

아래는 가능하면 착수한다.

* ACC → MBR → EXS → MBR → ACC 흐름의 runtime smoke 확장
* ADM timeline tree UI 고도화
* 실패 거래 샘플 추가
* 실패 구간 표시와 failureCode/failureMessageMasked runtime 검증
* external logs 상세 화면 연결
* CMN fixed-length layout registry를 EXS 전문 송수신 샘플과 연결

착수만 한 항목은 완료로 기록하지 않는다.

## 6. 후순위 / 제외 범위

이번 작업에서 완료 강제하지 않는다.

* Redis/Kafka/MQ 실 broker 연동
* broker 장애 시나리오
* 다중 인스턴스 동시성 검증
* 대규모 문서 정본화
* HTML 디자인 전면 개편
* BAT unrelated 기능 확장
* 최종 문서 전체 재작성

단, 작업 중 영향을 준 경우 상태값과 미검증 사유를 기록한다.

## 7. 필수 검증 명령

가능한 범위에서 아래를 실행한다.

```text
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :cmn:test --offline --no-daemon --console=plain
.\gradlew.bat :acc:test --offline --no-daemon --console=plain
.\gradlew.bat :mbr:test --offline --no-daemon --console=plain
.\gradlew.bat :exs:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat :xyz:test --offline --no-daemon --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

runtime 서비스 기동 후 가능한 경우 아래를 실행한다.

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-ui.ps1 -BrowserClick
```

MariaDB full install 가능 환경이면 아래를 실행한다.

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1
```

## 8. 완료 기준

이번 작업 완료는 아래를 만족해야 한다.

* PFW segment 저장이 runtime DB에서 확인됨
* composite runtime smoke 성공
* ADM transaction group runtime smoke 성공
* ADM 목록/상세/segments/timeline/headers/external-logs API runtime 검증 성공
* ADM UI가 API와 연결됨
* UI marker smoke 성공
* browser click을 실행했으면 성공 증거 기록
* MariaDB full install을 실행했으면 성공 증거 기록
* SQL/Flyway/all_install/smoke_check 정합성 확인
* 민감정보 원문 노출 없음
* mojibake 정리 및 UTF-8 gate 재실행
* 리포트와 기능 매트릭스 상태값 일치
* 실행하지 않은 검증은 미검증으로 기록

## 9. 완료 불인정 기준

아래는 완료로 인정하지 않는다.

* runtime 서비스 미기동 상태에서 composite trace 완료 처리
* API response에 segmentId가 있다는 이유만으로 DB 저장 완료 처리
* ADM API만 있고 UI 연결이 없는데 ADM 화면 완료 처리
* UI marker만 있고 browser click을 완료 처리
* 기존 개발 DB 확인만으로 MariaDB full install 완료 처리
* Source-Level 테스트만으로 Runtime E2E 완료 처리
* check script 미실행 상태에서 gate 완료 처리
* 민감 헤더/토큰 원문이 evidence에 포함됨
* 상태값이 리포트와 기능 매트릭스에서 다름

## 10. 완료 보고 양식

완료 보고는 아래 형식으로 작성한다.

```text
1. 작업 요약
2. 실제 수정 파일 요약
3. 필수 완료 범위 결과
   - 항목 / 상태 / 근거 / evidence / 남은 확인
4. 보강 범위 결과
5. 착수 범위 결과
6. 미검증 항목
   - 실행하지 않은 명령
   - 실행하지 못한 사유
   - 다음에 필요한 조건
7. 실패 항목
   - 실패 명령
   - 실패 로그 요약
   - 조치 여부
8. SQL/Flyway/all_install 정합성 결과
9. Runtime smoke 결과
10. ADM UI/browser click 결과
11. 민감정보/마스킹 확인 결과
12. CPF_STABILIZATION_REPORT.md 및 기능 매트릭스 반영 여부
```

상태값은 반드시 아래 6개만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

## 11. 다음 마일스톤 분기

이번 작업 후 다음 분기는 아래 기준으로 판단한다.

* runtime smoke와 ADM UI가 닫히면: EXS 전문/REST 송수신 로그와 CMN fixed-length 엔진 완성으로 이동
* runtime smoke가 실패하면: segment 전파/DB 저장/ADM 조회 실패 구간을 먼저 보정
* MariaDB full install이 실패하면: SQL/Flyway/all_install 정합성 보정을 최우선
* ADM UI/browser click이 미검증이면: 다음 작업의 최우선은 ADM browser click 폐쇄
* CMN fixed-length가 skeleton에 머물면: 반복부/오류코드/layout registry/fixture 보강을 계속 진행
