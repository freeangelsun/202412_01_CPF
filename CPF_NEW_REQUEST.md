# CPF 다음 작업 요청서 — evidence 재검증 가능화, ADM browser click, 실패 거래 trace, EXS 송수신 로그, CMN 전문 사전 확장

## 0. CPF 최종 목표 기준

작업 시작 전에 repo 루트의 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 반드시 확인한다.

이번 작업의 기준은 다음이다.

* 소스만 있으면 완료가 아니다.
* runtime smoke 결과는 실제 evidence로 확인 가능해야 한다.
* `build/...` 로컬 산출물만 리포트에 적고 검수자가 확인할 수 없으면 재확인 필요다.
* 정적 UI marker smoke와 browser click 검증은 구분한다.
* 기존 개발 DB 확인과 신규 빈 MariaDB full install 검증은 구분한다.
* EXS external logs는 `pfw_transaction_segment.external_*` 후보 조회만으로 완료가 아니다.
* 실행하지 않은 검증은 완료가 아니라 미검증이다.
* 민감정보 원문은 소스, 로그, 리포트, evidence 어디에도 남기지 않는다.

## 1. 이번 작업 목적

직전 작업에서 완료로 보고한 runtime/MariaDB smoke 결과를 검수 가능하게 정리하고, 아직 남은 ADM browser click, OpenAPI runtime, ADM 단독/권한 runtime, 실패 복합 거래 trace, EXS 실제 송수신 로그 원장, CMN fixed-length 전문 사전 확장을 한 번에 진행한다.

이번 작업은 단순 기능 추가가 아니라 “운영자가 실제 장애/실패 거래를 transactionGlobalId로 추적하고, ADM에서 거래 그룹/timeline/header/external log를 클릭해 확인할 수 있는 상태”로 끌어올리는 것이 목적이다.

## 2. 필수 제한

아래는 반드시 지킨다.

* Git commit 금지
* Git push 금지
* branch 생성 금지
* 민감정보 원문 기록 금지
* Authorization, Bearer token, X-Api-Key, password, secret, credential, signature 원문 기록 금지
* 실행하지 않은 검증 완료 기록 금지
* 별도 변경파일 목록 산출물 생성 금지
* `CPF_NEW_REQUEST.md` 수정 금지
* `CPF_FINAL_TARGET_REQUIREMENTS.md` 수정 금지
* build 산출물만 근거로 완료 기록 금지
* 정적 UI marker smoke를 browser click 완료로 기록 금지
* `pfw_transaction_segment.external_*` 후보 조회만으로 EXS 송수신 로그 완료 처리 금지

중요: 이번 작업에서 `CPF_NEW_REQUEST.md`는 절대 수정하지 않는다. 직전 작업에서는 실제 최신 commit에 `CPF_NEW_REQUEST.md` 변경이 포함되어 있었으므로, 이번 완료 보고에 “요청서 미수정 여부”를 반드시 명시한다.

## 3. 필수 완료 범위

### 3.1 evidence 검수 가능화

직전 작업에서 리포트에 기록한 아래 evidence는 현재 GitHub master에서 직접 확인할 수 없다.

* `build/runtime-smoke/standard-header-e2e-result.json`
* `build/runtime-smoke/composite-transaction-runtime-result.json`
* `build/runtime-smoke/adm-transaction-group-runtime-result.json`
* `build/sql-smoke/mariadb-full-install-result.json`
* `build/runtime-smoke/run-local-services-composite-rerun.job.log`

이번 작업에서는 민감정보를 제거한 검수용 evidence snapshot을 repo 안의 문서/evidence 경로에 남긴다.

권장 경로:

```text
specs/evidence/20260703_04/
```

필수 파일:

```text
specs/evidence/20260703_04/standard-header-e2e-result.sanitized.json
specs/evidence/20260703_04/composite-transaction-runtime-result.sanitized.json
specs/evidence/20260703_04/adm-transaction-group-runtime-result.sanitized.json
specs/evidence/20260703_04/mariadb-full-install-result.sanitized.json
specs/evidence/20260703_04/run-local-services-composite-rerun.sanitized.log
```

evidence 작성 기준:

* access token 원문 제거
* Authorization 원문 제거
* password 원문 제거
* API key 원문 제거
* secret/credential/signature 계열 제거
* DB password 원문 제거
* 개인식별 가능 값은 마스킹
* transactionGlobalId, segment count, module flow, status, API path, table/index/check count는 검수 가능하게 유지
* evidence에는 “검증 결과를 이해하기 위한 최소 정보”만 남긴다.

`CPF_STABILIZATION_REPORT.md`와 `specs/기능_구현_매트릭스.html`의 evidence 경로도 `build/...`만 쓰지 말고 sanitized evidence 경로를 함께 기록한다.

### 3.2 MariaDB full install 검증 재확정

`smoke-mariadb-full-install.ps1`는 `CPF_DB_ROOT_PASSWORD`가 없으면 미검증으로 빠지는 구조다. 이번 작업에서는 반드시 실제 실행 여부를 명확히 한다.

필수:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1 -RequireRun
```

완료 조건:

* 신규 빈 MariaDB 또는 검증 전용 DB에서 실행
* 기존 개발 DB 일부 확인으로 대체 금지
* `00_all_install_and_smoke.sql` 실행 성공
* `99_smoke_check.sql` 실행 성공
* `50_framework_seed_data.sql` 재실행 성공
* seed idempotent 확인
* `pfw_transaction_segment` 테이블 존재 확인
* 필수 컬럼 count 확인
* 필수 index count 확인
* legacy `pfw_center_cut_*` 테이블 잔존 여부 확인
* 결과 sanitized evidence 저장

DB 초기화 위험 때문에 실행하지 못하면 `mariadb-full-install`은 완료가 아니라 미검증으로 남긴다.

### 3.3 ADM browser click smoke 폐쇄

정적 UI marker smoke만으로는 ADM 화면 완료가 아니다. 이번 작업에서 실제 browser click smoke를 수행한다.

필수 실행:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-ui.ps1 -BrowserClick -RequireBrowserClick
```

필수 검증 시나리오:

* ADM 로그인
* 거래 그룹 메뉴 클릭
* 거래 그룹 목록 조회 버튼 클릭
* 검색조건 입력

  * transactionGlobalId
  * status
  * failureYn
  * moduleCode
  * externalInstitutionCode
  * apiPath
* 거래 그룹 row 선택
* 상세 탭 전환

  * 요약
  * Timeline
  * Segments
  * 표준 헤더
  * 확장 헤더
  * External Logs
  * 원본 JSON
* 민감 헤더 원문 미노출 확인
* 실패 거래가 있는 경우 실패 구간 표시 확인
* screenshot 또는 sanitized browser evidence 저장

브라우저 자동화 환경이 없어 실행하지 못하면 `adm-browser-click`은 미검증으로 남기고, 필요한 도구와 사유를 기록한다.

### 3.4 OpenAPI runtime smoke 폐쇄

OpenAPI runtime은 아직 미검증으로 남아 있다. 이번 작업에서 앱 기동 후 OpenAPI JSON을 확인한다.

필수 실행:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1 -RequireRuntime
```

필수 확인:

* ADM `/v3/api-docs`
* ACC `/v3/api-docs`
* MBR `/v3/api-docs`
* EXS `/v3/api-docs`
* XYZ `/v3/api-docs`
* 신규 API 문서 노출

  * `/adm/api/transaction-groups`
  * `/adm/api/transaction-groups/{transactionGlobalId}`
  * `/segments`
  * `/timeline`
  * `/headers`
  * `/external-logs`
  * ACC/MBR/EXS composite EDU API
* schema 누락 여부
* 민감 header 예시 원문 노출 여부

실행하지 못하면 `openapi-runtime`은 미검증이다.

### 3.5 ADM 단독 runtime / permission runtime 재검증

복합 거래 smoke와 별도로 ADM 단독 runtime, 권한 runtime을 재실행한다.

필수 실행:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-runtime.ps1 -BuildBeforeRun
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-permission-runtime.ps1
```

필수 확인:

* health
* OpenAPI
* 주요 조회 API
* transaction group API
* permission write API
* ADM_VIEWER 차단
* ADM_ADMIN 허용
* 권한 변경 감사 로그
* 운영자 조치 감사 사유
* 민감정보 원문 미노출

### 3.6 실패 복합 거래 trace 샘플 추가

현재 복합 거래는 성공 경로 중심이다. 장애 원인 추적 프레임워크라면 실패 구간 조회가 반드시 필요하다.

필수 추가:

* ACC → MBR → EXS 실패 경로
* EXS timeout 또는 명시적 실패 응답 샘플
* ACC → MBR → EXS → MBR → ACC 중첩 실패 후보
* failureCode 저장
* failureMessageMasked 저장
* failedSegmentId 저장
* failedModuleCode 저장
* ADM 거래 그룹 목록에서 실패 여부 표시
* ADM timeline에서 실패 구간 강조
* external logs에서 실패 외부기관/외부거래ID 조회

필수 smoke:

```text
scripts/smoke-composite-transaction-failure-runtime.ps1
scripts/smoke-adm-transaction-group-failure-runtime.ps1
```

완료 조건:

* 실패 거래 transactionGlobalId 생성
* segment 중 최소 1개 failure_yn = Y
* failureCode 존재
* failureMessageMasked 존재
* 민감 payload 원문 미노출
* ADM 목록/상세/timeline에서 실패 구간 조회 가능

### 3.7 ADM 거래 그룹 검색조건 보정

현재 `userId`, `operatorId` 검색이 `customer_no_masked`에 매핑되는 문제가 있다. 의미를 분리한다.

필수 보정:

* `X-User-Id`는 고객번호가 아니다.
* `X-Customer-No`가 고객번호다.
* `X-Member-No`가 회원번호다.
* `X-Operator-Id`가 운영 조치자다.

선택 구현 방식:

1. `pfw_transaction_segment`에 masked 컬럼 추가

   * `user_id_masked`
   * `operator_id_masked`
   * 필요 시 `client_app_id`, `caller_service`
2. 또는 header snapshot 제한 검색으로 처리

   * 단, 목록/상세 표시와 검색 필드 의미는 문서화해야 한다.

필수:

* 잘못된 `customer_no_masked` 재사용 제거
* 검색조건/목록/상세/마스킹 대상 분류 문서 반영
* SQL/Flyway/all_install/smoke_check 동기화
* MariaDB full install smoke에 신규 컬럼 반영

### 3.8 EXS 실제 송수신 로그 원장 착수/구현

현재 ADM external logs는 `pfw_transaction_segment.external_*` 기반 후보 조회에 가깝다. 이번 작업에서는 EXS 실제 송수신 로그 원장을 구현한다.

필수 테이블 후보:

```text
exs_exchange_log
exs_exchange_message_log
```

필수 컬럼 후보:

* exchangeLogId
* transactionGlobalId
* transactionSegmentId
* institutionCode
* endpointCode
* apiPath
* httpMethod
* requestAt
* responseAt
* durationMs
* requestHeaderMasked
* responseHeaderMasked
* requestPayloadMasked
* responsePayloadMasked
* responseCode
* httpStatus
* externalTransactionId
* retryCount
* timeoutMs
* status
* failureCode
* failureMessageMasked
* createdAt

필수 구현:

* EXS REST 송신/수신 로그 저장
* 고정길이 전문 송수신 로그 저장 후보 구조
* 민감 header/payload 원문 저장 금지
* transactionGlobalId 연결
* transactionSegmentId 연결
* externalTransactionId 연결
* ADM `/external-logs`가 `pfw_transaction_segment` 후보가 아니라 EXS 원장도 함께 조회
* EXS 로그 상세 API 후보 추가
* 실패/재처리 후보 상태값 정의

SQL 필수:

* split SQL
* Flyway
* `00_all_install.sql`
* `00_all_install_and_smoke.sql`
* `99_smoke_check.sql`

이번 작업에서 전문 송수신 전체 완성까지 못 하면 상태는 `부분 구현`으로 남긴다. 단 REST 송수신 로그 원장은 최소 1개 runtime smoke까지 닫는다.

### 3.9 CMN fixed-length 전문 사전 DB화 착수

CMN fixed-length 엔진은 반복부/group/type/error가 보강되었지만, 운영형 프레임워크 기준으로는 layout/field/group 사전과 ADM 조회가 필요하다.

필수 착수:

* layout registry DB 테이블 설계
* field spec DB 테이블 설계
* group spec DB 테이블 설계
* masking policy DB 테이블 설계
* sample layout seed
* fixed-length parse/format fixture
* CMN 단위 테스트
* EXS 전문 송수신 샘플과 연결 가능한 adapter 기준

테이블 후보:

```text
cmn_fixed_length_layout
cmn_fixed_length_field
cmn_fixed_length_group
cmn_fixed_length_masking_policy
```

필수 필드 후보:

* layoutId
* institutionCode
* messageCode
* direction
* version
* charset
* totalLength
* headerLength
* bodyLength
* trailerLength
* fieldName
* displayName
* startPosition
* length
* byteLength
* type
* requiredYn
* paddingChar
* align
* scale
* format
* sensitiveYn
* maskingType
* groupId
* repeatCountField
* enabledYn

이번 작업에서 ADM 관리 화면까지 완성하지 못하면 `부분 구현`으로 남긴다.

### 3.10 EDU mapper DB slice 재검증

남아 있는 `edu-mapper-db-slice`를 닫는다.

필수 실행:

```text
.\gradlew.bat :xyz:test --tests "*XyzQueryEducationMapperSliceTest*" --offline --no-daemon --console=plain
```

필수 확인:

* `CPF_XYZ_EDU_MAPPER_DB_USERNAME`
* `CPF_XYZ_EDU_MAPPER_DB_USER`
* fixture `xyz_edu_query_fixture.sql`
* CRUD/search/sort/paging fixture 정상
* offset/keyset paging 정상
* whitelist sort 정상
* validation 실패 케이스 정상

실행하지 못하면 미검증으로 기록한다.

## 4. 보강 범위

### 4.1 ADM timeline UX 고도화

가능하면 거래 그룹 timeline을 단순 JSON이 아니라 운영자가 보기 쉬운 형태로 보강한다.

* segment tree
* callDepth indent
* MAIN/SUB/EXTERNAL role badge
* duration bar
* failure segment 강조
* 외부기관/외부거래ID 표시
* header snapshot 탭 분리
* external log 상세 보기

### 4.2 EXS timeout/retry/failure smoke

가능하면 EXS timeout/retry 실패 경로를 smoke로 검증한다.

* timeout 설정
* retry count
* 실패 응답 코드 매핑
* failureCode
* failureMessageMasked
* ADM external logs 조회
* 재처리 후보 상태

### 4.3 표준/확장 헤더 정책 보강

확장 헤더 `X-Cpf-Ext-*` 기준을 더 강하게 만든다.

* 허용 key
* 차단 key
* token/auth/api-key/secret/password/credential/signature 우회 차단
* 저장 대상/검색 대상/표시 대상 분리
* ADM header snapshot 조회 시 원문 미노출

## 5. 착수 범위

가능하면 아래도 착수한다.

* Redis/Kafka/MQ 실 broker 연동 smoke 초안
* broker 장애 시 DB fallback 시나리오
* BAT worker heartbeat/ghost/lock 다음 마일스톤 준비
* ADM 마스킹 해제 권한/감사 고도화
* EXS 재처리 운영자 조치 API 후보

착수만 한 항목은 완료로 기록하지 않는다.

## 6. 후순위 / 제외 범위

이번 작업에서 완료 강제하지 않는다.

* Redis/Kafka/MQ 실 broker 전체 완료
* 다중 인스턴스 동시성 검증 전체 완료
* 최종 문서 정본화
* HTML 디자인 전면 개편
* 전체 OpenAPI 문서 품질 개선
* BAT 전체 운영 기능 확장

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
.\gradlew.bat test --offline --no-daemon --console=plain
```

runtime:

```text
.\gradlew.bat runLocalServices -PcpfRunServices=ACC,MBR,ADM,EXS --no-daemon --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-failure-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-failure-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-runtime.ps1 -BuildBeforeRun
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-permission-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-ui.ps1 -BrowserClick -RequireBrowserClick
```

MariaDB:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build-all-install-sql.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1 -RequireRun
```

quality gate:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

## 8. 완료 기준

이번 작업 완료는 아래를 만족해야 한다.

* `CPF_NEW_REQUEST.md` 미수정 확인
* `CPF_FINAL_TARGET_REQUIREMENTS.md` 미수정 또는 수정 시 명확한 사유 기록
* sanitized evidence가 repo에서 검수 가능
* composite runtime smoke 성공 evidence 확인 가능
* ADM transaction group runtime smoke 성공 evidence 확인 가능
* MariaDB full install smoke 성공 evidence 확인 가능
* ADM browser click 성공 또는 미검증 사유 기록
* OpenAPI runtime smoke 성공 또는 미검증 사유 기록
* ADM 단독/권한 runtime smoke 재검증
* 실패 복합 거래 trace runtime smoke 성공
* ADM에서 실패 구간 조회 가능
* EXS 실제 송수신 로그 원장 최소 REST 경로 구현
* CMN fixed-length 전문 사전 DB화 착수
* EDU mapper DB slice 재검증
* 민감정보 원문 미노출
* SQL/Flyway/all_install/smoke_check 정합성 확인
* `CPF_STABILIZATION_REPORT.md`와 기능 매트릭스 상태값 일치

## 9. 완료 불인정 기준

아래는 완료로 인정하지 않는다.

* evidence가 `build/...`에만 있고 repo에서 검수 불가
* `CPF_NEW_REQUEST.md`를 수정했는데 미수정으로 보고
* runtime 미실행인데 runtime 완료 기록
* MariaDB full install을 기존 개발 DB 일부 확인으로 대체
* browser click 미실행인데 ADM UI 완료 기록
* `pfw_transaction_segment.external_*`만으로 EXS 송수신 로그 완료 기록
* 실패 거래 샘플 없이 장애 추적 완료 기록
* userId/operatorId를 customerNo 컬럼에 매핑
* 민감 헤더 원문이 evidence에 포함
* 기능 매트릭스와 리포트 상태값 불일치

## 10. 완료 보고 양식

완료 보고는 아래 형식으로 작성한다.

```text
1. 작업 요약
2. 수정하지 않은 파일 확인
   - CPF_NEW_REQUEST.md
   - CPF_FINAL_TARGET_REQUIREMENTS.md
3. 실제 수정 파일 요약
4. 필수 완료 범위 결과
   - 항목 / 상태 / evidence / 남은 확인
5. 보강 범위 결과
6. 착수 범위 결과
7. 미검증 항목
   - 실행하지 않은 명령
   - 사유
   - 다음 필요 조건
8. 실패 항목
   - 실패 명령
   - 실패 로그 요약
   - 조치 여부
9. sanitized evidence 경로
10. SQL/Flyway/all_install 정합성 결과
11. Runtime smoke 결과
12. ADM browser click 결과
13. OpenAPI runtime 결과
14. EXS 송수신 로그 결과
15. CMN fixed-length 전문 사전 결과
16. 민감정보/마스킹 확인 결과
17. CPF_STABILIZATION_REPORT.md 및 기능 매트릭스 반영 여부
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

이번 작업 후 분기는 아래 기준으로 판단한다.

* evidence와 browser click이 닫히면: EXS 전문/REST 송수신 완성 + CMN 전문 사전 ADM 관리로 이동
* 실패 거래 trace가 닫히면: 재처리/보상거래/운영자 조치 감사로 이동
* MariaDB full install이 실패하면: SQL/Flyway/all_install 정합성 보정을 최우선
* browser click이 계속 미검증이면: 다음 작업도 ADM browser click 폐쇄가 최우선
* EXS 송수신 로그가 부분 구현이면: EXS 로그 원장과 ADM 상세 조회를 다음 필수 완료 범위로 유지
* CMN 전문 사전이 착수에 머물면: DB 사전 + fixture + ADM 조회를 다음 작업으로 유지
