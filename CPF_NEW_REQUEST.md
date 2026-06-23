# CPF_REQUEST_008: 로그 정책 런타임 적용 / Override 전파 / 거래 메타 E2E Smoke / 리포트 HTML 정본화

## 0. 이번 작업 범위 고정

이번 작업은 온라인 거래 메타와 로그 정책 기본 모델이 추가된 이후, 실제 런타임에서 로그 정책이 적용되는지 검증 가능한 수준으로 연결하는 작업이다.

이번 작업은 아래 범위로 고정한다.

```text id="p8nwsu"
선택 범위:
- pfw_log_policy / pfw_log_policy_override 런타임 적용
- LoggingAspect에서 온라인 거래별 로그 정책 적용
- TransactionLogListener 또는 TransactionLogService에서 DB 로그 저장 여부 적용
- Batch Job/Step listener에서 배치 Job/Step 로그 정책 적용 기준 연결
- 로그 정책 우선순위 적용
- 로그 정책 조회 cache 및 refresh/invalidation 기준 구현
- ADM 로그 정책 변경 시 runtime cache 반영 기준 구현
- 거래 메타 scan → DB upsert → ADM 조회 E2E smoke 검증
- check-feature-evidence.ps1 신규 기능 marker 보강
- CPF_STABILIZATION_REPORT.html을 실제 HTML 문서 형식으로 정본화
- README / 개발 가이드 / 관리자 가이드 / 운영 매뉴얼 / SQL 가이드 / 기능 매트릭스 / 리포트 반영
```

이번 작업에서 아래 항목은 구현하지 않는다.
필요하면 `CPF_STABILIZATION_REPORT.html`의 다음 보강 후보로만 기록한다.

```text id="g9hife"
이번 실행 제외:
- ADM 로그 정책 전체 UX 완성
- ADM 브라우저 실제 클릭 자동화
- ADM 오류/감사/마스킹/다운로드 통합 관제 전체 구현
- ADM Cache hit/miss/ttl/eviction/clear 전체 구현
- 배치 강제수행/lock/pause/resume 운영 정책 전체 구현
- 온디맨드 배치 구현
- 센터컷 기본 구현체 구현
- Redis/Kafka/MQ 실 broker 검증
- 전체 트랜잭션 가이드 정본화
```

요청 파일은 작업 대상으로 수정하지 않는다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 `CPF_STABILIZATION_REPORT.html`에만 기록한다.

---

## 1. 이번 작업의 핵심 의도

직전 작업에서 아래 기반이 추가되었다.

```text id="pb76wr"
- @CpfTransaction 정본화
- FpsTransaction 레거시 정리
- pfw_transaction_meta 기본 모델
- 거래 메타 scan/upsert 기반
- ADM 거래 메타 API
- pfw_log_policy / pfw_log_policy_override / pfw_log_policy_audit 기본 모델
- ADM 로그 정책 API
- BAT smoke strict V10 옵션
- ADM Cache summary/refresh 부분 구현
```

하지만 아직 핵심 미완료가 남아 있다.

```text id="d8wjcz"
남은 핵심:
- 로그 정책이 실제 LoggingAspect에 적용되는지
- DB 로그 저장 여부가 실제 TransactionLogListener/Service에 적용되는지
- override 기간/우선순위가 실제 런타임에서 평가되는지
- 정책 변경 후 cache가 반영되는지
- 거래 메타 scan 결과가 실제 DB에 저장되고 ADM에서 조회되는지
- feature evidence gate가 신규 기능을 제대로 검증하는지
- CPF_STABILIZATION_REPORT.html이 실제 HTML 형식인지
```

이번 작업은 “모델/API 있음” 단계에서 “런타임 적용/검증 가능” 단계로 올리는 작업이다.

---

## 2. 상위 아키텍처 전제

아래 책임 경계를 유지한다.

```text id="syeiea"
PFW:
- 로그 정책 런타임 평가의 공통 기준 제공
- transactionGlobalId / traceId / spanId 기준 제공
- LoggingAspect / TransactionLogListener / Batch listener에 정책 적용 기준 제공
- 정책 조회 cache 및 invalidation 기준 제공
- ADM 화면 책임을 직접 소유하지 않음

ADM:
- 로그 정책 조회/등록/수정/override 관리
- override 기간/사유/변경자/감사 이력 관리
- 정책 변경 후 runtime 반영 요청 또는 cache refresh 요청
- 운영자가 확인 가능한 API/UI wrapper 제공

BAT:
- Batch Job/Step 실행 주체
- Batch listener에서 Job/Step 로그 정책을 적용받음
- 온라인 거래 정책 구현을 침범하지 않음

온라인 업무 모듈:
- Controller/API에 @CpfTransaction 적용
- 거래별 로그 정책 대상
- 거래 메타 scan/upsert 대상
```

센터컷 전제도 유지한다.

```text id="zhaqpy"
센터컷 전제:
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않는다.
- BAT는 향후 기본 센터컷 구현체를 제공한다.
- 업무 주제영역은 커스텀 모수/item/result 테이블과 adapter로 연동한다.
```

---

## 3. 먼저 현재 상태 판정

작업 시작 전 실제 소스/SQL/문서 기준으로 아래를 판정한다.

```text id="nze8yr"
현재 상태 판정 항목:
- pfw_transaction_meta 테이블 존재 여부
- pfw_log_policy 테이블 존재 여부
- pfw_log_policy_override 테이블 존재 여부
- pfw_log_policy_audit 테이블 존재 여부
- CpfTransactionMetaScanner 존재 여부
- 거래 메타 startup scan 동작 방식
- ADM 거래 메타 API 존재 여부
- ADM 로그 정책 API 존재 여부
- LoggingAspect가 로그 정책을 실제 조회/적용하는지
- TransactionLogListener 또는 TransactionLogService가 DB 저장 여부를 정책 기반으로 판단하는지
- Batch listener가 Job/Step 로그 정책을 적용하는지
- 로그 정책 cache 존재 여부
- ADM 정책 변경 시 cache refresh/invalidation 존재 여부
- check-feature-evidence.ps1이 신규 기능을 검증하는지
- CPF_STABILIZATION_REPORT.html이 실제 HTML 문서 형식인지
```

상태값은 아래만 사용한다.

```text id="vfb4a1"
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

Codex 완료 메시지나 과거 리포트만 믿지 말고 실제 파일 기준으로 확인한다.

---

## 4. 로그 정책 우선순위 런타임 적용

로그 정책은 온라인/배치 모두에서 같은 우선순위를 가져야 한다.

```text id="jon0hx"
로그 정책 우선순위:
1. ADM 활성 override
2. DB 운영 정책
3. application.yml 기본값
4. CPF 기본값
```

필수 구현 또는 보강 대상:

```text id="i23p7w"
- LogPolicyResolver 또는 동등한 공통 service
- LogPolicyCache 또는 동등한 cache
- LogPolicyTargetType enum 또는 동등 코드
- ONLINE_TRANSACTION 정책 조회
- BATCH_JOB 정책 조회
- BATCH_STEP 정책 조회
- override 기간 effective_start_at / effective_end_at 평가
- active_yn 평가
- target_id 미일치 시 기본 정책 fallback
- 정책 미존재 시 yml 기본값 fallback
- 정책 평가 실패 시 업무 응답 실패로 전파하지 않고 안전 fallback
```

정책 대상 기준:

```text id="ryzbby"
ONLINE_TRANSACTION:
- target_id = transactionId

BATCH_JOB:
- target_id = jobId

BATCH_STEP:
- target_id = jobId + stepName 또는 별도 stepId 정책
```

정책 평가 결과에는 최소 아래가 있어야 한다.

```text id="u5kmm9"
- targetType
- targetId
- fileLogLevel
- dbLogEnabled
- dbLogLevel
- requestBodySaveYn
- responseBodySaveYn
- errorStackSaveYn
- maskingPolicyKey
- resolvedSource
- overrideId
- policyId
```

---

## 5. LoggingAspect 런타임 적용 기준

`LoggingAspect`는 `@CpfTransaction`의 거래 ID를 기준으로 온라인 거래 로그 정책을 조회하고 적용해야 한다.

필수 기준:

```text id="p9jfsz"
- @CpfTransaction.id를 transactionId로 사용
- ONLINE_TRANSACTION 정책 조회
- dbLogEnabled = N이면 DB 거래 로그 저장 event를 발생시키지 않거나 Listener에서 저장하지 않음
- requestBodySaveYn = N이면 request body 저장하지 않음
- responseBodySaveYn = N이면 response body 저장하지 않음
- errorStackSaveYn = N이면 stack trace 저장하지 않음
- maskingPolicyKey가 있으면 기존 마스킹 정책과 연결
- 정책 평가 실패 시 yml/CPF 기본값으로 fallback
- 정책 적용 여부를 debug 또는 운영 로그로 추적 가능
```

완료 불인정:

```text id="d0w6ar"
- 로그 정책 테이블은 있지만 LoggingAspect가 사용하지 않음
- dbLogEnabled=N인데 DB 거래 로그가 저장됨
- request/response 저장 정책이 무시됨
- override가 active 기간인데 반영되지 않음
```

---

## 6. TransactionLogListener / Service 저장 제어 기준

`TransactionLogListener` 또는 `TransactionLogService`는 정책 결과에 따라 DB 저장 여부를 제어해야 한다.

필수 기준:

```text id="hbdcyf"
- 정책에서 dbLogEnabled=N이면 pfw_transaction_log 저장 안 함
- 저장하지 않은 경우에도 업무 응답에는 영향 없음
- 저장하지 않은 사유를 trace/debug 수준으로 확인 가능
- 오류 상황에서 errorStackSaveYn 정책 적용
- 민감정보는 기존 마스킹 기준 유지
- DB 저장 실패는 업무 응답 실패로 전파하지 않음
```

가능하면 테스트로 확인한다.

```text id="p3553p"
테스트:
- dbLogEnabled=Y → 거래 로그 저장
- dbLogEnabled=N → 거래 로그 미저장
- requestBodySaveYn=N → request body 미저장
- responseBodySaveYn=N → response body 미저장
- errorStackSaveYn=N → stack trace 미저장
```

---

## 7. Batch Job/Step 로그 정책 적용 기준

Batch도 온라인과 같은 로그 정책 모델을 사용해야 한다.

필수 기준:

```text id="6lz4h6"
- BATCH_JOB 정책 조회
- BATCH_STEP 정책 조회
- Job 시작/종료/실패 로그 저장 여부 적용
- Step 시작/종료/실패 로그 저장 여부 적용
- Job/Step별 DB 로그 저장 ON/OFF 적용
- Job/Step별 로그 레벨 기준 문서화
```

이번 작업에서 전체 batch log 저장 구조가 없다면 아래처럼 처리한다.

```text id="dko6cz"
허용:
- Batch listener에서 정책 resolver를 호출하고 결과를 운영 메타/로그에 반영할 수 있는 hook 구현
- 실제 배치 로그 DB 저장은 부분 구현 또는 다음 보강으로 분리

불허:
- 배치 로그 정책 테이블만 만들고 런타임 적용 완료로 기록
```

---

## 8. 로그 정책 cache / refresh / invalidation 기준

정책 조회는 매 요청마다 DB를 직접 때리지 않도록 cache를 둔다.
단, 운영자가 ADM에서 override를 바꾸면 반영 가능해야 한다.

필수 기준:

```text id="6h5koq"
- 정책 조회 cache
- cache key: targetType + targetId
- ttl 또는 명시적 refresh 기준
- ADM 정책 변경 시 해당 target cache evict
- ADM override 등록/비활성화 시 cache evict
- 전체 cache refresh API 또는 내부 service
- cache refresh/clear 감사 로그
```

다중 인스턴스 기준:

```text id="zbiovq"
우선순위:
1. DB 기반 cache invalidation event
2. Redis/Kafka/MQ event
3. local instance only + 명확한 미검증 기록
```

현재 실 Redis/Kafka/MQ가 없으면 실연동 성공으로 기록하지 않는다.
mock/fallback/disabled profile로 검증하고, 실 broker 미검증은 리포트에 남긴다.

---

## 9. ADM 로그 정책 runtime 반영 API 기준

ADM 로그 정책 API는 정책 등록/수정/override 후 runtime cache 반영 기준을 가져야 한다.

필수 기준:

```text id="cjxkkf"
- 정책 등록 시 audit 기록
- 정책 수정 시 audit 기록
- override 등록 시 audit 기록
- override 비활성화 시 audit 기록
- 정책 변경 후 cache evict 또는 refresh 호출
- cache refresh 실패 시 정책 변경은 성공/실패 기준 명확화
- 운영 사유 필수
- 기간 validation
- active override 중복 처리 기준
```

가능하면 ADM Cache 메뉴와 연결한다.

```text id="te5bc5"
ADM Cache 연계:
- 로그 정책 cache 목록 조회
- 특정 거래 로그 정책 cache clear
- 전체 로그 정책 cache refresh
- 운영 사유 필수
- 감사 로그 기록
```

이번 작업에서 전체 Cache 메뉴 구현이 크면 최소 로그 정책 cache refresh만 구현하고, 전체 Cache 관제는 다음 요청으로 분리한다.

---

## 10. 거래 메타 E2E smoke 기준

이번 작업에서는 거래 메타 scan이 실제 DB에 저장되고 ADM에서 조회되는지 검증한다.

필수 검증:

```text id="wkis9a"
- ADM 또는 테스트 앱 기동
- ApplicationReadyEvent 기반 거래 메타 scan 수행
- @CpfTransaction 적용 API가 pfw_transaction_meta에 upsert됨
- 동일 앱 재기동 또는 재스캔 시 idempotent
- 사라진 거래 inactive 처리 service 검증
- ADM /adm/api/transactions 목록에서 조회
- ADM /adm/api/transactions/{transactionId} 상세 조회
```

실제 runtime smoke가 어렵다면 통합 테스트로 대체할 수 있다.
단, 대체 검증이면 리포트에 명확히 구분한다.

완료 불인정:

```text id="zj5te5"
- scanner 소스만 있고 DB upsert 검증 없음
- ADM API만 있고 scan 결과 조회 검증 없음
- 문서에는 E2E라고 쓰고 실제로는 단위 테스트만 수행
```

---

## 11. check-feature-evidence.ps1 보강

현재 feature evidence gate가 신규 기능을 충분히 검증하지 못할 수 있다.
이번 작업에서 신규 기능 marker를 추가한다.

필수 marker 후보:

```text id="fj1eoe"
- CpfTransactionMetaScanner
- CpfTransactionMetaRepository
- CpfTransactionMetaAutoConfiguration
- pfw_transaction_meta
- pfw_log_policy
- pfw_log_policy_override
- pfw_log_policy_audit
- AdmTransactionMetaController
- AdmLogPolicyController
- LogPolicyResolver 또는 동등 service
- LogPolicyCache 또는 동등 service
- LoggingAspect policy 적용
- TransactionLogListener policy 적용
- BAT smoke strict V10
- CPF_STABILIZATION_REPORT.html 실제 HTML 구조
```

feature evidence gate는 파일 존재만이 아니라 가능한 경우 핵심 문자열도 확인한다.

예:

```text id="qjzljp"
- dbLogEnabled
- requestBodySaveYn
- responseBodySaveYn
- effectiveStartAt
- effectiveEndAt
- cache evict
- cache refresh
- @CpfTransaction
```

---

## 12. CPF_STABILIZATION_REPORT.html HTML 정본화

`CPF_STABILIZATION_REPORT.html`은 실제 HTML 문서여야 한다.
확장자만 `.html`이고 Markdown 형태면 완료로 보지 않는다.

필수 기준:

```text id="mjjzq7"
- <!DOCTYPE html>
- <html lang="ko">
- <head>
- <meta charset="UTF-8">
- <title>
- <body>
- h1/h2/h3 구조
- table 또는 section 구조
- 상태값 badge 또는 텍스트
- 실행 명령과 결과 구분
- 미구현/미검증 구분
- 다음 조치 구분
```

리포트에는 아래를 명확히 구분한다.

```text id="dk8en8"
- 실제 확인한 것
- Codex가 주장한 것
- 직접 실행한 검증
- 직접 실행하지 못한 검증
- 미구현
- 미검증
- 실패
- 다음 조치
```

완료 불인정:

```text id="ie3e1k"
- .html 확장자지만 Markdown 문서
- 실행하지 않은 검증을 성공으로 기록
- 미구현 항목을 숨김
```

---

## 13. SQL / Flyway / all_install 기준

이번 작업에서 SQL 변경이 있으면 아래를 모두 반영한다.

```text id="mdmm3u"
- Flyway migration 추가
- all_install SQL 반영
- smoke SQL 반영
- table comment / column comment
- 공통 감사 컬럼
- FK/index
- SQL 가이드 반영
- 기능 매트릭스 반영
- CPF_STABILIZATION_REPORT.html 반영
```

SQL 변경 후 가능하면 실제 MariaDB 적용 검증을 수행한다.
drop/create 성격의 전체 설치 SQL을 실행하지 않으면 미검증으로 남긴다.

센터컷 대량 모수/item/result 테이블은 이번 작업에서 만들지 않는다.

---

## 14. 문서 반영 기준

아래 문서를 실제 구현 결과에 맞춰 갱신한다.

```text id="r8o8kf"
README.md
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/운영_매뉴얼.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
CPF_STABILIZATION_REPORT.html
```

문서에는 “구현 완료”, “부분 구현”, “미구현”, “미검증”을 분리해서 기록한다.

---

## 15. 테스트 기준

필수 테스트를 추가 또는 보강한다.

```text id="w35bck"
로그 정책 resolver 테스트:
- override active 기간이면 override 적용
- override 기간 밖이면 DB 정책 적용
- DB 정책 없으면 yml 기본값 적용
- yml 기본값 없으면 CPF 기본값 적용
- targetType/targetId별 cache key 분리

온라인 로그 정책 테스트:
- dbLogEnabled=Y → 거래 로그 저장
- dbLogEnabled=N → 거래 로그 미저장
- requestBodySaveYn=N → request body 미저장
- responseBodySaveYn=N → response body 미저장
- errorStackSaveYn=N → stack trace 미저장

거래 메타 E2E 테스트:
- scanAndUpsert
- idempotent 재스캔
- inactive 처리
- ADM 목록 조회
- ADM 상세 조회

ADM 로그 정책 테스트:
- 정책 등록/수정 audit
- override 등록 audit
- override disable audit
- reason 누락 차단
- 기간 validation
- cache evict 호출

Batch 정책 hook 테스트:
- BATCH_JOB 정책 조회
- BATCH_STEP 정책 조회
- listener에서 resolver 호출
```

---

## 16. 검증 명령

가능한 범위에서 아래 명령을 실행한다.

```powershell id="kix44r"
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat :bat:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

.\gradlew.bat :adm:bootJar --offline
.\gradlew.bat :bat:bootJar --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-bat-runtime.ps1

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

가능하면 거래 메타 E2E smoke 스크립트를 추가한다.

```powershell id="ny57rl"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-transaction-meta-runtime.ps1
```

추가하지 못하면 사유를 리포트에 기록한다.

실행하지 않은 검증은 성공으로 기록하지 않는다.

---

## 17. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 아래 형식으로 기록한다.

```text id="o0l05b"
[로그 정책 런타임 적용 / Override 전파 / 거래 메타 E2E Smoke / 리포트 HTML 정본화]

현재 상태 판정:
- 로그 정책 테이블:
- ADM 로그 정책 API:
- LoggingAspect 정책 적용:
- TransactionLogListener 정책 적용:
- Batch listener 정책 적용:
- 정책 cache:
- 거래 메타 E2E:
- feature evidence:
- 리포트 HTML 구조:

개발 기능:
- LogPolicyResolver:
- LogPolicyCache:
- LoggingAspect:
- TransactionLogListener:
- Batch listener:
- ADM policy API:
- Cache refresh/invalidation:
- Transaction meta smoke:
- Feature evidence:
- Report HTML:

테스트:
- :pfw:test:
- :adm:test:
- :bat:test:
- 전체 test:
- qualityGate:
- smoke-adm-runtime:
- smoke-bat-runtime:
- transaction-meta smoke:
- check scripts:

미구현:
- 미구현 항목:
- 다음 요청 후보:

미검증:
- 미검증 항목:
- 미검증 사유:

아키텍처 영향:
- PFW:
- ADM:
- BAT:
- 온라인 거래:
- 센터컷 확장성:

최종 판정:
- 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요
```

---

## 18. 완료 기준

아래가 모두 충족되어야 이번 작업을 완료로 기록한다.

```text id="fcoyq1"
- 로그 정책 우선순위가 런타임에서 적용된다.
- LoggingAspect가 ONLINE_TRANSACTION 정책을 조회한다.
- dbLogEnabled=N이면 온라인 거래 로그가 DB에 저장되지 않는다.
- requestBodySaveYn/responseBodySaveYn/errorStackSaveYn 정책이 적용된다.
- override 기간/사유/active 상태가 평가된다.
- 정책 조회 cache가 존재한다.
- ADM 정책 변경 시 cache evict 또는 refresh가 수행된다.
- Batch Job/Step listener에서 BATCH_JOB/BATCH_STEP 정책 hook이 동작한다.
- 거래 메타 scan 결과가 DB에 upsert되고 ADM에서 조회된다.
- 거래 메타 재스캔이 idempotent하다.
- 사라진 거래 inactive 처리 기준이 있다.
- check-feature-evidence가 신규 기능 핵심 marker를 검증한다.
- CPF_STABILIZATION_REPORT.html이 실제 HTML 문서 형식이다.
- README/specs/기능 매트릭스/리포트가 실제 구현 상태와 일치한다.
- 실행하지 않은 검증을 성공으로 기록하지 않는다.
```

---

## 19. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text id="upma4n"
- 로그 정책 테이블만 있고 런타임 적용 없음
- LoggingAspect가 정책을 조회하지 않음
- dbLogEnabled=N인데 거래 로그가 저장됨
- override 기간이 무시됨
- 정책 cache가 없음
- ADM 정책 변경 후 cache 반영 기준 없음
- Batch Job/Step 정책 hook 없음
- 거래 메타 scanner만 있고 DB upsert/ADM 조회 검증 없음
- feature evidence가 신규 기능을 검증하지 않음
- CPF_STABILIZATION_REPORT.html이 Markdown 형태임
- 문서에는 완료라고 되어 있으나 테스트 없음
- 실행하지 않은 검증을 성공으로 기록
```

---

## 20. 다음 보강 후보로만 기록할 항목

이번 작업 이후 다음 보강 후보는 실제 판정 결과를 기준으로 정렬한다.
기본 우선순위는 아래와 같다.

```text id="s2cqyr"
1. ADM 거래/오류/감사 통합 관제 UX
2. ADM Cache 관제 / hit-miss / ttl / eviction / clear / 다중 인스턴스 전파
3. ADM 배치 운영 정책 / 강제수행 / lock / 파라미터 / BAT EDU
4. ADM 브라우저 실제 클릭 자동화
5. 온디맨드 배치
6. 센터컷 기본 구현체 + 업무별 커스텀 모수/item/result 테이블 연동
7. Redis/Kafka/MQ mock/fallback 및 실 broker 미검증 절차
8. 트랜잭션 가이드 정본화
```
