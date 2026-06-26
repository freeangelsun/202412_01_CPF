# CPF_REQUEST_009: V13 권한 seed 실제 DB 적용 / ADM 거래 메타 E2E smoke 완료 / 로그 정책 효과 검증 / 전체 test timeout 분리

## 0. 이번 작업 범위 고정

이번 작업은 직전 작업에서 남은 실패/미검증 항목을 닫는 안정화 작업이다.

직전 작업에서 로그 정책 런타임 적용, ADM 로그 정책 cache refresh/clear API, 거래 메타 smoke script, V13 권한 seed 파일은 반영되었다.
하지만 ADM 거래 메타 runtime smoke가 403으로 실패했고, 전체 `gradlew test --offline`은 timeout으로 성공 처리되지 않았다.

이번 작업은 아래 범위로 고정한다.

```text
선택 범위:
- V13__adm_runtime_policy_permission_seed.sql 실제 local MariaDB 적용
- ADM 거래 메타 scan API 403 원인 해소
- 거래 메타 scan → pfw_transaction_meta upsert → 목록 조회 → 상세 조회 E2E smoke 성공
- smoke-adm-runtime.ps1 전체 성공 상태 복구
- 로그 정책 실제 효과 검증
- dbLogEnabled / requestBodySaveYn / responseBodySaveYn / errorStackSaveYn 정책 적용 검증
- override active 기간 / 만료 기간 / fallback 검증
- 전체 gradlew test timeout 원인 분리
- README / 관리자 가이드 / 운영 매뉴얼 / SQL 가이드 / 기능 매트릭스 / CPF_STABILIZATION_REPORT.html 반영
```

이번 작업에서 아래 항목은 구현하지 않는다.

```text
이번 실행 제외:
- 신규 ADM UX 대규모 구현
- ADM 브라우저 실제 클릭 자동화
- ADM Cache hit/miss/ttl/eviction/clear 전체 구현
- 배치 강제수행/lock/pause/resume 운영 정책 전체 구현
- 온디맨드 배치 구현
- 센터컷 기본 구현체 구현
- Redis/Kafka/MQ 실 broker 검증
- 트랜잭션 가이드 전체 정본화
```

요청 파일은 작업 대상으로 수정하지 않는다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 `CPF_STABILIZATION_REPORT.html`에만 기록한다.

---

## 1. 이번 작업의 핵심 의도

이번 작업은 새 기능 구현이 아니라 **실패한 검증을 성공 상태로 닫는 작업**이다.

직전 리포트 기준 핵심 미완료는 아래다.

```text
남은 핵심 실패/미검증:
- smoke-adm-runtime.ps1 안의 거래 메타 scan API가 403으로 실패
- V13 권한 seed가 local MariaDB에 실제 적용되지 않음
- 거래 메타 scan → upsert → 목록/상세 조회 E2E가 실패 상태
- 로그 정책 런타임 적용 소스는 들어갔지만 정책별 실제 DB 저장/미저장 효과 검증 부족
- 전체 gradlew test --offline이 timeout으로 성공 처리되지 않음
```

이번 작업은 이 항목을 닫는 데 집중한다.

---

## 2. 상위 아키텍처 전제

아래 책임 경계를 유지한다.

```text
PFW:
- @CpfTransaction, 거래 메타 scan/upsert, 로그 정책 resolver/cache, LoggingAspect, TransactionLogService 공통 기준 제공
- ADM 화면 책임을 직접 소유하지 않음

ADM:
- 거래 메타 조회/scan/inactive API 제공
- 로그 정책 조회/등록/수정/override/cache refresh 관리
- 권한/버튼/API 권한 seed와 운영 감사 관리

BAT:
- Batch runtime smoke와 Job/Step listener 정책 hook 유지
- 이번 작업에서 배치 운영 정책 신규 기능을 확장하지 않음
```

센터컷 전제도 유지한다.

```text
센터컷 전제:
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않는다.
- BAT는 향후 기본 센터컷 구현체를 제공한다.
- 업무 주제영역은 커스텀 모수/item/result 테이블과 adapter로 연동한다.
```

---

## 3. 먼저 현재 상태 판정

작업 시작 전 실제 소스/SQL/문서/DB 기준으로 아래를 판정한다.

```text
현재 상태 판정 항목:
- V13__adm_runtime_policy_permission_seed.sql 파일 존재 여부
- V13 seed가 local MariaDB에 적용되어 있는지
- ADM 거래 메타 scan API 권한 seed 존재 여부
- ADM 거래 메타 목록/상세/scan/inactive API 권한 seed 존재 여부
- ADM 로그 정책 cache refresh/clear 권한 seed 존재 여부
- smoke-transaction-meta-runtime.ps1 실패 원인
- smoke-adm-runtime.ps1 전체 실패 원인
- pfw_transaction_meta 테이블 존재 여부
- 거래 메타 scan 시 DB upsert 여부
- ADM 목록/상세 조회 가능 여부
- 로그 정책 resolver/cache 런타임 적용 상태
- LoggingAspect 정책 적용 상태
- TransactionLogService 정책 저장 제어 상태
- 전체 gradlew test timeout 원인
```

상태값은 아래만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

Codex 완료 메시지나 과거 리포트만 믿지 말고 실제 파일/DB 기준으로 확인한다.

---

## 4. V13 권한 seed 실제 DB 적용

직전 작업에서는 V13 권한 seed 파일은 생성되었지만 local MariaDB에 적용되지 않아 거래 메타 scan API가 403으로 실패했다.

이번 작업에서는 V13 seed를 실제 local MariaDB에 적용한다.

필수 검증:

```text
- V13__adm_runtime_policy_permission_seed.sql 실제 적용
- 적용 전/후 권한 row count 확인
- ADM 거래 메타 메뉴 권한 확인
- ADM 거래 메타 버튼 권한 확인
- ADM 거래 메타 scan API 권한 확인
- ADM 로그 정책 cache refresh/clear 권한 확인
- seed 재실행 idempotent 확인
```

주의:

```text
- 비밀번호는 리포트에 기록하지 않는다.
- root/migration/app 계정 권한 구분을 지킨다.
- 적용 명령과 결과는 리포트에 기록한다.
- 적용하지 못하면 성공으로 기록하지 않는다.
```

---

## 5. ADM 거래 메타 E2E smoke 완료

이번 작업의 핵심 완료 기준은 거래 메타 E2E smoke 성공이다.

필수 흐름:

```text
1. ADM runtime 기동
2. ADM health 성공
3. ADM login 성공
4. /adm/api/transactions/scan 호출 성공
5. @CpfTransaction 적용 API scan
6. pfw_transaction_meta upsert 확인
7. /adm/api/transactions 목록 조회 성공
8. /adm/api/transactions/{transactionId} 상세 조회 성공
9. 동일 scan 재실행 idempotent 확인
10. smoke-adm-runtime.ps1 전체 성공
```

필수 확인:

```text
- 403이 더 이상 발생하지 않는다.
- scan 결과에 inserted/updated/skipped 또는 동등 결과가 나온다.
- pfw_transaction_meta에 transaction_id, transaction_name, api_path, controller_class, handler_method가 저장된다.
- ADM 목록 API에서 scan 결과가 조회된다.
- ADM 상세 API에서 단건이 조회된다.
```

완료 불인정:

```text
- 권한 seed만 적용하고 거래 메타 scan smoke를 재실행하지 않음
- scan API만 성공하고 DB upsert를 확인하지 않음
- 목록 조회만 하고 상세 조회를 확인하지 않음
- smoke-adm-runtime.ps1이 여전히 실패하는데 완료로 기록
```

---

## 6. 로그 정책 실제 효과 검증

직전 작업에서 로그 정책 resolver/cache, LoggingAspect, TransactionLogService 연결은 소스 기준으로 반영되었다.
이번 작업에서는 실제 정책 효과를 검증한다.

필수 테스트/검증:

```text
ONLINE_TRANSACTION 정책:
- dbLogEnabled=Y → 거래 로그 저장
- dbLogEnabled=N → 거래 로그 미저장
- requestBodySaveYn=N → request body 미저장
- responseBodySaveYn=N → response body 미저장
- errorStackSaveYn=N → stack trace 미저장
```

Override 검증:

```text
- active override 기간이면 override 적용
- override 기간 전이면 DB 정책 적용
- override 기간 만료 후 DB 정책 적용
- DB 정책 없으면 yml 기본값 적용
- yml 기본값 없으면 CPF 기본값 적용
```

Cache 검증:

```text
- 정책 조회 후 cache hit 가능
- ADM 정책 수정 시 해당 target cache evict 또는 refresh
- override 등록 시 해당 target cache evict 또는 refresh
- override disable 시 해당 target cache evict 또는 refresh
```

검증 방식:

```text
허용:
- 단위 테스트
- 통합 테스트
- runtime smoke
- DB row count 확인

불허:
- 소스만 보고 효과 검증 완료 처리
- Controller/API만 보고 완료 처리
```

---

## 7. smoke-adm-runtime.ps1 보강

`smoke-adm-runtime.ps1`는 ADM health/OpenAPI/batch API뿐 아니라 거래 메타 smoke까지 포함하므로 전체 성공 기준을 명확히 한다.

필수 기준:

```text
- health 성공
- OpenAPI 성공
- batch API smoke 성공
- transaction meta scan 성공
- transaction meta list 성공
- transaction meta detail 성공
- cleanup 성공
```

거래 메타 smoke가 실패하면 `smoke-adm-runtime.ps1` 전체는 실패로 기록한다.

단, 브라우저 클릭 자동화는 이번 범위가 아니므로 런타임 부재 시 SKIPPED로 기록한다.

---

## 8. 전체 gradlew test timeout 분리

직전 작업에서 모듈별 test는 통과했지만 전체 `gradlew test --offline`은 장시간 timeout으로 성공 처리되지 않았다.

이번 작업에서는 timeout 원인을 분리한다.

필수 확인:

```text
- 전체 test timeout 발생 여부
- 어느 모듈/테스트에서 오래 걸리는지 식별
- hanging test 여부 확인
- smoke/runtime test가 unit test에 섞여 timeout을 유발하는지 확인
- 테스트 병렬/순차 실행 영향 확인
```

가능하면 수정한다.

```text
허용 수정:
- 장시간 smoke 성격 테스트를 unit test에서 분리
- timeout이 큰 테스트에 profile/tag 부여
- test task에서 runtime smoke 제외
- integrationTest 또는 smoke script로 분리
```

완료 기준:

```text
- 전체 gradlew test --offline 성공
```

완료가 어렵다면:

```text
- 실패/timeout 테스트명
- 원인
- 임시 우회 여부
- 다음 조치
```

를 리포트에 명확히 기록한다.
성공하지 않았으면 성공으로 기록하지 않는다.

---

## 9. SQL / Flyway / all_install 기준

이번 작업에서 SQL 변경 또는 seed 적용이 있으면 아래 기준을 따른다.

```text
- Flyway migration 파일 확인
- 실제 MariaDB 적용
- seed idempotent 확인
- all_install SQL 반영 여부 확인
- smoke SQL 반영 여부 확인
- SQL 가이드 반영
- 기능 매트릭스 반영
- CPF_STABILIZATION_REPORT.html 반영
```

전체 설치 SQL은 drop/create 성격이면 자동 실행하지 않아도 된다.
실행하지 않았으면 미검증으로 기록한다.

---

## 10. 문서 반영 기준

아래 문서를 실제 결과에 맞게 최소 갱신한다.

```text
README.md
specs/관리자_가이드.html
specs/운영_매뉴얼.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
CPF_STABILIZATION_REPORT.html
```

문서에는 아래를 분리해서 기록한다.

```text
- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 다음 보강
```

---

## 11. feature evidence gate 보강

필요하면 `scripts/check-feature-evidence.ps1`에 이번 실패 방지 marker를 추가한다.

권장 marker:

```text
- V13__adm_runtime_policy_permission_seed.sql
- ADM_TRANSACTION_META_SCAN
- LOG_POLICY_CACHE_REFRESH
- LOG_POLICY_CACHE_CLEAR
- smoke-transaction-meta-runtime.ps1
- pfw_transaction_meta
- dbLogEnabled
- requestBodySaveYn
- responseBodySaveYn
- errorStackSaveYn
```

feature evidence는 실제 E2E 성공을 대체하지 않는다.
E2E 검증은 smoke/test 결과로 따로 기록한다.

---

## 12. 검증 명령

가능한 범위에서 아래 명령을 실행한다.

```powershell
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat :bat:test --offline
.\gradlew.bat test --offline

.\gradlew.bat :adm:bootJar --offline
.\gradlew.bat :bat:bootJar --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-transaction-meta-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-bat-runtime.ps1

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1

.\gradlew.bat qualityGate --offline
```

실행하지 않은 검증은 성공으로 기록하지 않는다.

---

## 13. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 아래 형식으로 기록한다.

```text
[V13 권한 seed 실제 DB 적용 / ADM 거래 메타 E2E smoke 완료 / 로그 정책 효과 검증 / 전체 test timeout 분리]

현재 상태 판정:
- V13 seed 적용 전 상태:
- 거래 메타 scan 403 원인:
- 로그 정책 효과 검증 전 상태:
- 전체 test timeout 상태:

DB 적용:
- 적용 SQL:
- 적용 계정:
- 적용 명령:
- 적용 결과:
- seed idempotent:

거래 메타 E2E:
- scan:
- DB upsert:
- 목록 조회:
- 상세 조회:
- 재스캔 idempotent:
- smoke-adm-runtime:

로그 정책 효과 검증:
- dbLogEnabled=Y:
- dbLogEnabled=N:
- requestBodySaveYn=N:
- responseBodySaveYn=N:
- errorStackSaveYn=N:
- override active:
- override expired:
- fallback:

테스트:
- :pfw:test:
- :adm:test:
- :bat:test:
- 전체 test:
- smoke-adm-runtime:
- smoke-transaction-meta-runtime:
- smoke-bat-runtime:
- qualityGate:
- check scripts:

미구현:
- 미구현 항목:

미검증:
- 미검증 항목:
- 미검증 사유:

실패:
- 실패 항목:
- 실패 원인:

다음 조치:
- 다음 요청 후보:

최종 판정:
- 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요
```

---

## 14. 완료 기준

아래가 모두 충족되어야 이번 작업을 완료로 기록한다.

```text
- V13 권한 seed가 실제 local MariaDB에 적용되었다.
- seed 재실행 idempotent가 확인되었다.
- ADM 거래 메타 scan API 403이 해소되었다.
- 거래 메타 scan → pfw_transaction_meta upsert가 확인되었다.
- ADM 거래 메타 목록 조회가 성공했다.
- ADM 거래 메타 상세 조회가 성공했다.
- smoke-adm-runtime.ps1이 거래 메타 smoke 포함 전체 성공했다.
- dbLogEnabled=N이면 거래 로그가 DB에 저장되지 않는 것이 검증되었다.
- requestBodySaveYn=N이면 request body가 저장되지 않는 것이 검증되었다.
- responseBodySaveYn=N이면 response body가 저장되지 않는 것이 검증되었다.
- errorStackSaveYn=N이면 stack trace가 저장되지 않는 것이 검증되었다.
- active override와 expired override의 정책 적용 차이가 검증되었다.
- 전체 gradlew test --offline이 성공하거나 timeout 원인이 명확히 분리되었다.
- CPF_STABILIZATION_REPORT.html에 성공/실패/미검증이 분리 기록되었다.
```

---

## 15. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text
- V13 seed 파일만 있고 실제 DB 적용 없음
- scan API 403이 남아 있음
- scan API만 성공하고 DB upsert 확인 없음
- 목록 조회만 하고 상세 조회 없음
- smoke-adm-runtime.ps1이 실패하는데 성공으로 기록
- 로그 정책 소스만 보고 실제 효과 검증을 하지 않음
- dbLogEnabled=N 검증 없음
- request/response/errorStack 저장 제외 검증 없음
- override 기간 검증 없음
- 전체 test timeout을 성공처럼 기록
- 실행하지 않은 검증을 성공으로 기록
```

---

## 16. 다음 보강 후보로만 기록할 항목

이번 작업 이후 다음 보강 후보는 실제 결과를 기준으로 정렬한다.

기본 후보:

```text
1. ADM 거래/오류/감사 통합 관제 UX
2. ADM Cache 관제 / hit-miss / ttl / eviction / clear / 다중 인스턴스 전파
3. ADM 배치 운영 정책 / 강제수행 / lock / 파라미터 / BAT EDU
4. ADM 브라우저 실제 클릭 자동화
5. 온디맨드 배치
6. 센터컷 기본 구현체 + 업무별 커스텀 모수/item/result 테이블 연동
7. Redis/Kafka/MQ mock/fallback 및 실 broker 미검증 절차
8. 트랜잭션 가이드 정본화
```
