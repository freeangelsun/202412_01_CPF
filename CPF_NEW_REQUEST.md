# CPF_REQUEST_007: 온라인 거래 메타 정본화 / Fps 레거시 정리 / 로그 정책 기본 모델 / ADM 필수 운영 메뉴 매트릭스

## 0. 이번 작업 범위 고정

이번 작업은 배치 쪽으로 치우치지 않도록 CPF 전체 운영 프레임워크의 온라인 거래 축과 ADM 기본 운영 메뉴 축을 정렬하는 작업이다.

이번 작업은 아래 범위로 고정한다.

```text id="h8jcps"
선택 범위:
- @CpfTransaction 정본화
- Fps/FPS/FpsTransaction 레거시 정리
- 온라인 거래 메타 자동 등록 기반 설계 및 최소 구현
- pfw_transaction_meta 테이블 추가 또는 실제 부재 시 명확한 설계/미구현 분리
- 로그 정책 기본 모델 설계 및 최소 테이블 추가 검토
- pfw_log_policy / pfw_log_policy_override / pfw_log_policy_audit 기본 구조 검토 및 필요 시 추가
- ADM 거래 메타 조회 API 최소 구현 또는 현황 판정
- ADM 로그 정책 관리 API skeleton 또는 현황 판정
- ADM 필수 운영 메뉴 현황표 세분화
- ADM Cache 상태 조회/관리 메뉴를 필수 운영 메뉴 후보로 등록
- BAT runtime smoke의 V10 progress fallback 잔여 경로 정리 또는 strict 검증 옵션 추가
- README / 개발 가이드 / 관리자 가이드 / 운영 매뉴얼 / SQL 가이드 / 기능 매트릭스 / 리포트 반영
```

이번 작업에서 아래 항목은 구현하지 않는다.
필요하면 `CPF_STABILIZATION_REPORT.html`의 다음 보강 후보로만 기록한다.

```text id="x9gx5e"
이번 실행 제외:
- ADM 필수 운영 메뉴 전체 구현
- ADM Cache refresh/clear 전체 구현
- ADM 로그 정책 override 전체 구현
- ADM 오류/감사/마스킹/다운로드 관제 전체 구현
- 온라인 거래 로그/오류/감사 관제 전체 E2E
- 배치 운영 정책 전체 구현
- 온디맨드 배치 구현
- 센터컷 기본 구현체 구현
- Redis/Kafka/MQ 실 broker 검증
- ADM 브라우저 실제 클릭 자동화
```

요청 파일은 작업 대상으로 수정하지 않는다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 `CPF_STABILIZATION_REPORT.html`에만 기록한다.

---

## 1. 이번 작업의 핵심 의도

CPF는 배치만 완성되어서는 안 된다.
온라인 거래, 로그 정책, ADM 운영 메뉴, 감사, 권한, 캐시, 설정, 배치 관제가 균형 있게 맞아야 한다.

이번 작업의 목적은 아래와 같다.

```text id="bm41wd"
- 온라인 Controller/API 단위 거래 메타 기준을 정본화한다.
- @CpfTransaction을 표준으로 확정한다.
- FPS/Fps/FpsTransaction 레거시 명칭을 정리한다.
- 거래 메타 자동 등록과 로그 정책의 DB 기준을 세운다.
- ADM 필수 운영 메뉴 후보를 매트릭스에 세분화한다.
- ADM Cache 관제 메뉴를 필수 운영 메뉴 후보로 명시한다.
- 배치 V10 smoke fallback 잔여 이슈를 닫거나 strict 검증으로 분리한다.
```

이번 작업은 “온라인 전체 구현 완료”가 아니라 “온라인 거래와 ADM 운영 메뉴의 중심축을 세우는 작업”이다.

---

## 2. 상위 아키텍처 전제

아래 책임 경계를 유지한다.

```text id="lks5g8"
PFW:
- 공통 framework/library
- 독립 프로세스가 아님
- @CpfTransaction 표준 annotation 제공
- transactionGlobalId / traceId / spanId 기준 제공
- 온라인 거래 메타 scan/upsert 기준 제공
- 로그 정책 기본 모델 제공
- 파일 로그/DB 로그/감사/오류 기준 제공

ADM:
- 운영/관리 콘솔
- 거래 메타, 로그 정책, 오류 로그, 감사 로그, 캐시, 설정, 코드, 메시지, 배치 관제 관리
- 운영 override는 기간/사유/변경자/감사 이력을 가진다.

BAT:
- 독립 batch worker
- 배치 Job/Step 실행 주체
- 온라인 거래 메타 구현을 침범하지 않는다.

업무 모듈:
- Controller/API에 @CpfTransaction 적용
- 거래 ID와 거래 논리명 제공
- 업무 DB를 PFW/ADM이 직접 침범하지 않는다.
```

센터컷 전제도 유지한다.

```text id="h3w3n6"
센터컷 전제:
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않는다.
- BAT는 향후 기본 센터컷 구현체를 제공한다.
- 업무 주제영역은 커스텀 모수/item/result 테이블과 adapter로 연동한다.
```

---

## 3. 먼저 현재 상태 판정

작업 시작 전 실제 소스/SQL/문서 기준으로 아래를 판정한다.

```text id="aav7mz"
현재 상태 판정 항목:
- @CpfTransaction 존재 여부
- @CpfTransaction 적용 Controller/API 현황
- FpsTransaction 존재 여부
- Fps/FPS/fps 문자열 잔존 위치
- TransactionHeaderValidationInterceptor가 어떤 annotation을 기준으로 검증하는지
- LoggingAspect가 어떤 annotation을 기준으로 거래 ID/거래명을 수집하는지
- TransactionContextFilter의 transactionGlobalId/trace/span 처리 상태
- pfw_transaction_meta 테이블 존재 여부
- pfw_log_policy 테이블 존재 여부
- pfw_log_policy_override 테이블 존재 여부
- pfw_log_policy_audit 테이블 존재 여부
- pfw_error_log 테이블 존재 여부
- pfw_audit_log 테이블 존재 여부
- ADM 거래 메타 조회 API 존재 여부
- ADM 로그 정책 관리 API 존재 여부
- ADM 필수 운영 메뉴가 기능 매트릭스에 세분화되어 있는지
- ADM Cache 관제 메뉴가 별도 행으로 있는지
- smoke-bat-runtime.ps1의 heartbeat fallback 경로 존재 여부
```

상태값은 아래만 사용한다.

```text id="bz2wej"
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

Codex 완료 메시지나 과거 리포트만 믿지 말고 실제 파일 기준으로 확인한다.

---

## 4. CpfTransaction 정본화 / Fps 레거시 정리

`@CpfTransaction`을 CPF 표준 거래 annotation으로 확정한다.

필수 기준:

```text id="b4on8t"
- @CpfTransaction이 정본 annotation이다.
- Controller/API 거래 메타는 @CpfTransaction 기준으로 선언한다.
- 거래 ID는 필수다.
- 거래 논리명은 필수다.
- TransactionHeaderValidationInterceptor는 @CpfTransaction 기준으로 검증한다.
- LoggingAspect는 @CpfTransaction 기준으로 businessTransactionId/name을 수집한다.
- Swagger/OpenAPI 설명과 거래 메타가 충돌하지 않는다.
```

Fps 레거시 정리 기준:

```text id="cweacm"
- FpsTransaction 사용처를 검색한다.
- FpsTransaction이 더 이상 사용되지 않으면 제거한다.
- 바로 제거가 어렵다면 @Deprecated bridge로 두고 리포트에 사유와 제거 계획을 기록한다.
- FPS/Fps/fps 문자열은 실제 소스/SQL/문서/스크립트에서 검색한다.
- 레거시 명칭 잔존이 필요한 경우 사유를 리포트에 기록한다.
- 깨진 주석/mojibake가 있으면 UTF-8 기준으로 정리한다.
```

완료 불인정:

```text id="ng6znx"
- @CpfTransaction과 @FpsTransaction이 혼재
- Interceptor와 Aspect가 서로 다른 annotation 기준 사용
- FpsTransaction 주석 깨짐 방치
- FPS 레거시 명칭이 문서/소스에 남았는데 사유 없음
```

---

## 5. 온라인 거래 메타 자동 등록 기반

이번 작업에서 온라인 거래 메타 자동 등록을 최소 구현하거나, 구현 범위가 크면 설계와 테이블/스캐너 skeleton까지 만든다.

필수 목표:

```text id="hvm1j5"
- Controller/API 단위 거래 메타를 수집한다.
- @CpfTransaction.id와 name을 수집한다.
- HTTP method, path, controller class, handler method를 수집한다.
- 모듈, 업무영역, 활성 여부를 관리한다.
- WAS 기동 후 RequestMapping/annotation scan 기반으로 upsert한다.
- 사라진 거래는 inactive 처리할 수 있는 구조를 둔다.
- scan 실패가 애플리케이션 기동 실패로 이어지지 않도록 한다.
- 실패는 운영 로그 또는 리포트에 남긴다.
```

권장 테이블:

```text id="vzqvdu"
pfw_transaction_meta:
- transaction_id
- transaction_name
- module_code
- domain_code
- http_method
- api_path
- controller_class
- handler_method
- swagger_operation_id
- log_policy_key
- sensitive_yn
- masking_policy_key
- active_yn
- first_detected_at
- last_detected_at
- last_scanned_at
- created_at
- created_by
- updated_at
- updated_by
```

테이블을 추가하면 아래를 반드시 반영한다.

```text id="i95sif"
- Flyway migration
- all_install SQL
- smoke SQL
- SQL 가이드
- 기능 구현 매트릭스
- CPF_STABILIZATION_REPORT.html
```

이번 작업에서 완전 구현이 어렵다면 아래처럼 명확히 분리한다.

```text id="ly5br1"
허용:
- 테이블 + DTO + Repository + Service skeleton
- scan service skeleton
- ADM 조회 API skeleton
- 실제 scan E2E는 다음 요청으로 분리

불허:
- 문서만 작성하고 구현 완료로 기록
- Controller annotation만 보고 자동 등록 완료로 기록
```

---

## 6. 로그 정책 기본 모델

온라인과 배치 모두 같은 우선순위 기준을 공유해야 한다.

로그 정책 우선순위:

```text id="q6zzxj"
1. ADM 활성 override
2. DB 운영 정책
3. application.yml 기본값
4. CPF 기본값
```

필수 로그 정책 대상:

```text id="b9ff3c"
- ONLINE_TRANSACTION
- BATCH_JOB
- BATCH_STEP
- EXS_INBOUND
- EXS_OUTBOUND
```

권장 테이블:

```text id="imr8g0"
pfw_log_policy:
- policy_id
- target_type
- target_id
- file_log_level
- db_log_enabled_yn
- db_log_level
- request_body_save_yn
- response_body_save_yn
- error_stack_save_yn
- masking_policy_key
- active_yn
- created_at
- created_by
- updated_at
- updated_by

pfw_log_policy_override:
- override_id
- policy_id
- target_type
- target_id
- file_log_level
- db_log_enabled_yn
- db_log_level
- effective_start_at
- effective_end_at
- reason
- requested_by
- approved_by
- active_yn
- created_at
- created_by
- updated_at
- updated_by

pfw_log_policy_audit:
- audit_id
- action_type
- target_type
- target_id
- before_value
- after_value
- reason
- operator_id
- transaction_global_id
- created_at
```

이번 작업에서 전체 override 구현까지 하지 않아도 된다.
다만 DB 모델, 우선순위, ADM 메뉴 후보, 다음 구현 범위를 명확히 남긴다.

완료 불인정:

```text id="n9dvoo"
- 온라인 로그 정책과 배치 로그 정책을 별개로 설계
- ADM override 기간/사유/감사 기준 없음
- yml 기본값과 DB 정책 우선순위 없음
- 로그 정책 테이블 없이 완료 처리
```

---

## 7. ADM 거래 메타 / 로그 정책 API 최소 기준

이번 작업에서 가능하면 아래 API를 최소 구현한다.

```text id="qxzepy"
ADM 거래 메타 API:
- GET /adm/api/transactions
- GET /adm/api/transactions/{transactionId}
- POST /adm/api/transactions/scan
- PATCH /adm/api/transactions/{transactionId}/inactive

ADM 로그 정책 API:
- GET /adm/api/log-policies
- GET /adm/api/log-policies/{policyId}
- POST /adm/api/log-policies
- PATCH /adm/api/log-policies/{policyId}
- POST /adm/api/log-policy-overrides
- PATCH /adm/api/log-policy-overrides/{overrideId}/disable
```

이번 요청에서 전체 구현이 어렵다면 API skeleton과 DTO/Service/Repository 범위를 분리해 리포트에 남긴다.
Controller만 만들고 하드코딩 응답이면 완료로 기록하지 않는다.

필수 구성:

```text id="krw49k"
- Controller/API
- Service
- Repository/Mapper
- DTO
- Validation
- Swagger Tag/Operation/Schema
- 권한/감사 기준
- 테스트
```

---

## 8. ADM 필수 운영 메뉴 현황표 세분화

CPF는 범용 운영형 프레임워크이므로 ADM 필수 운영 메뉴가 기능 매트릭스에 세분화되어야 한다.

이번 작업에서 모든 메뉴를 구현하지 않는다.
단, 아래 메뉴 후보를 `specs/기능_구현_매트릭스.html`에 세분화해서 상태 판정한다.

```text id="cpd78u"
시스템 운영:
- 시스템 health
- 인스턴스 상태
- datasource 상태
- runtime profile
- build/version 정보

프레임워크 cache:
- cache 목록 조회
- cache key/count/size/ttl 조회
- cache hit/miss/eviction 통계
- cache refresh
- cache clear
- cache reload
- cache 변경 감사 로그

거래/로그:
- 온라인 거래 메타 조회
- 거래 상세
- 거래별 로그 정책
- 파일 로그 레벨 조회/변경
- DB 로그 저장 여부 조회/변경
- 로그 정책 override
- override 기간/사유/변경자/감사 이력

오류/감사:
- 오류 로그 조회
- 오류 조치 상태 변경
- 오류 조치 이력
- 감사 로그 조회
- 마스킹 감사 조회
- 다운로드 감사 조회

권한/보안:
- 운영자 관리
- 역할 관리
- 메뉴 권한
- 버튼 권한
- API 권한
- 다운로드 권한
- MFA 설정
- IP allowlist
- 계정 잠금/해제
- 로그인 이력

배치/BAT:
- 배치 Job 목록
- Job 상세
- 실행 이력
- Step 이력
- 수동 실행
- 강제수행
- 중지/재실행
- 파라미터 snapshot
- worker 상태
- lock 상태
- ghost candidate
- ghost 조치
- heartbeat/progress 조회

스케줄/업무일:
- 스케줄 조회/등록/수정
- 스케줄 활성/비활성
- 영업일 관리
- 휴일 정책
- 수행 가능 시간

외부연계:
- 기관/endpoint 관리
- 외부 호출 로그
- 외부 오류/재시도
- mock/fallback 상태
- Redis/Kafka/MQ disabled/mock/fallback 상태

공통 설정:
- 공통 코드
- 메시지
- 환경 설정
- 알림 설정
- 운영 리포트
```

각 메뉴별 매트릭스 컬럼:

```text id="quqd2l"
- 대분류
- 기능명
- 상태
- package
- Controller/API
- Service
- Repository/Mapper
- DTO
- DB 테이블
- ADM UI/API wrapper
- Swagger
- EDU
- 테스트
- 검증 명령
- 실제 결과
- 미검증 사유
- 다음 보완
```

완료는 증거가 있을 때만 사용한다.

---

## 9. ADM Cache 메뉴 기본 요건

ADM Cache 메뉴는 필수 운영 메뉴 후보로 등록한다.

이번 작업에서 전체 구현하지 않아도 된다.
다만 다음 구현을 위한 기본 요건을 문서와 매트릭스에 남긴다.

```text id="a74z7u"
ADM Cache 필수 기능:
- cache manager 목록 조회
- cache name 목록 조회
- cache key count 조회
- cache estimated size 조회
- hit count / miss count / eviction count 조회
- ttl 또는 expire 정책 조회
- cache refresh
- cache clear by cache name
- cache clear all 제한
- 운영 사유 필수
- 권한 체크
- 감사 로그 저장
- 다중 인스턴스 반영 기준
```

다중 인스턴스 반영 기준:

```text id="l0ewx1"
우선순위:
1. DB 기반 cache invalidation event
2. Redis/Kafka/MQ event
3. local instance only + 명확한 미검증 기록
```

실 Redis/Kafka/MQ가 없으면 실연동 성공으로 기록하지 않는다.
disabled/mock/fallback profile과 향후 실연동 절차를 문서화한다.

---

## 10. BAT runtime smoke fallback 정리

`scripts/smoke-bat-runtime.ps1`의 heartbeat 검증은 V10 컬럼 기반 strict 검증이 가능해야 한다.

필수 기준:

```text id="cz0j6f"
- 기본 smoke는 V10 컬럼 기반 processed_count/progress_rate/last_heartbeat_at을 확인한다.
- V10 컬럼이 없으면 기본 smoke는 실패해야 한다.
- 구버전 DB 호환 fallback이 필요하면 별도 옵션으로 분리한다.
- fallback 옵션을 사용한 경우 리포트에 fallback 검증이라고 기록한다.
```

허용 예:

```text id="l3v6v5"
- scripts/smoke-bat-runtime.ps1 기본값: strict V10 검증
- 옵션: -AllowLegacyFallback
```

완료 불인정:

```text id="e6v8zo"
- fallback 경로가 기본 smoke에 남아 있음
- fallback으로 성공했는데 V10 컬럼 검증 성공으로 기록
```

---

## 11. SQL / Flyway / all_install 기준

SQL 변경 시 아래를 모두 반영한다.

```text id="lvx7d1"
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

센터컷 대량 모수/item/result 테이블은 이번 작업에서 만들지 않는다.
PFW에 센터컷 대량 테이블을 고정 추가하지 않는다.

---

## 12. 문서 반영 기준

아래 문서를 실제 구현 결과에 맞춰 갱신한다.

```text id="zpdueq"
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

## 13. 테스트 기준

필수 테스트를 추가 또는 보강한다.

```text id="cavgav"
거래 annotation 테스트:
- @CpfTransaction 없는 Controller 차단 또는 정책 검증
- @CpfTransaction.id 형식 오류 차단
- @CpfTransaction.name 누락 차단
- TransactionHeaderValidationInterceptor가 @CpfTransaction 기준으로 동작

거래 메타 테스트:
- @CpfTransaction scan
- transaction_meta upsert
- 동일 거래 재스캔 idempotent
- 사라진 거래 inactive 처리

로그 정책 테스트:
- target_type ONLINE_TRANSACTION
- target_type BATCH_JOB
- target_type BATCH_STEP
- override 기간 validation
- override 사유 누락 차단

레거시 정리 테스트:
- FpsTransaction 사용처 없음 또는 deprecated bridge 확인
- FPS/Fps/fps 문자열 잔존 검사
```

가능하면 `check-feature-evidence.ps1`에 아래 marker를 추가한다.

```text id="q16puo"
- CpfTransaction 정본화
- pfw_transaction_meta
- pfw_log_policy
- ADM 필수 운영 메뉴 매트릭스
- ADM Cache 메뉴 후보
- BAT smoke strict V10
```

---

## 14. 검증 명령

가능한 범위에서 아래 명령을 실행한다.

```powershell id="k5c2yr"
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-bat-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

SQL 변경이 있으면 MariaDB 적용 검증을 수행한다.
실행하지 않은 검증은 성공으로 기록하지 않는다.

---

## 15. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 아래 형식으로 기록한다.

```text id="sa4dfe"
[온라인 거래 메타 정본화 / Fps 레거시 정리 / 로그 정책 기본 모델 / ADM 필수 운영 메뉴 매트릭스]

현재 상태 판정:
- @CpfTransaction:
- FpsTransaction:
- FPS/Fps/fps 잔존:
- 거래 메타 테이블:
- 로그 정책 테이블:
- ADM 거래 메타 API:
- ADM 로그 정책 API:
- ADM Cache 메뉴:
- BAT smoke fallback:

개발 기능:
- annotation 정리:
- transaction meta:
- log policy:
- ADM API:
- 매트릭스:
- 문서:
- smoke script:

테스트:
- :pfw:test:
- :adm:test:
- 전체 test:
- qualityGate:
- smoke-bat-runtime:
- smoke-adm-runtime:
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

실행하지 않은 검증은 성공으로 기록하지 않는다.

---

## 16. 완료 기준

아래가 모두 충족되어야 이번 작업을 완료로 기록한다.

```text id="eo25zx"
- @CpfTransaction이 정본 annotation으로 확정되어 있다.
- FpsTransaction 사용 여부가 정리되어 있다.
- FPS/Fps/fps 레거시 잔존이 정리되거나 사유가 기록되어 있다.
- TransactionHeaderValidationInterceptor와 LoggingAspect가 같은 annotation 기준으로 동작한다.
- pfw_transaction_meta 또는 동등 거래 메타 구조가 추가되거나 skeleton/미구현이 명확히 분리되어 있다.
- 로그 정책 기본 모델과 우선순위가 SQL/문서/매트릭스에 반영되어 있다.
- ADM 필수 운영 메뉴 후보가 기능 구현 매트릭스에 세분화되어 있다.
- ADM Cache 상태 조회/관리 메뉴가 필수 후보로 반영되어 있다.
- BAT runtime smoke의 V10 fallback 경로가 기본값에서 제거되거나 strict 옵션으로 분리되어 있다.
- README/specs/기능 매트릭스/리포트가 실제 구현 상태와 일치한다.
- 실행하지 않은 검증을 성공으로 기록하지 않는다.
```

---

## 17. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text id="jjqn3h"
- @CpfTransaction과 @FpsTransaction이 혼재
- FpsTransaction 깨진 주석 또는 레거시 명칭 방치
- Interceptor와 Aspect가 서로 다른 annotation 기준 사용
- 거래 메타 테이블 없이 자동 등록 완료로 기록
- 로그 정책 테이블 없이 로그 정책 완료로 기록
- ADM 필수 운영 메뉴 후보를 뭉뚱그려 한 줄로 기록
- ADM Cache 메뉴 후보 누락
- BAT smoke fallback으로 성공했는데 V10 strict 검증 성공으로 기록
- 문서에는 완료라고 되어 있으나 테스트 없음
- SQL 변경이 있는데 Flyway/all_install/SQL 가이드 반영 없음
- 실행하지 않은 검증을 성공으로 기록
```

---

## 18. 다음 보강 후보로만 기록할 항목

이번 작업 이후 다음 보강 후보는 실제 판정 결과를 기준으로 정렬한다.
기본 우선순위는 아래와 같다.

```text id="w1bkwv"
1. 온라인 거래 메타 자동 등록 E2E 완성
2. 온라인/배치 로그 정책 override ADM 관리 완성
3. ADM Cache 관제 / refresh / clear / 감사 로그 완성
4. ADM 오류/감사/마스킹/다운로드 관제 완성
5. ADM 배치 운영 정책 / 강제수행 / lock / 파라미터 / BAT EDU
6. ADM 브라우저 실제 클릭 자동화
7. 온디맨드 배치
8. 센터컷 기본 구현체 + 업무별 커스텀 모수/item/result 테이블 연동
9. Redis/Kafka/MQ mock/fallback 및 실 broker 미검증 절차
10. 트랜잭션 가이드 정본화
```
