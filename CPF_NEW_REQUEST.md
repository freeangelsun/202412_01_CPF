# CPF_REQUEST_006: 프레임워크 균형 게이트 / V10 실제 DB 적용 / 온라인·배치 공통 운영 기준 정렬

## 0. 이번 작업 범위 고정

이번 작업은 BAT 쪽으로 치우친 최근 작업 흐름을 프레임워크 전체 관점에서 다시 맞추는 작업이다.

이번 작업은 신규 대형 기능 구현이 아니라 아래 세 가지를 닫는 작업으로 범위를 고정한다.

```text id="z2vqm8"
선택 범위:
1. Flyway V10 실제 MariaDB 적용 및 V10 컬럼 기반 BAT heartbeat/progress 검증
2. 온라인 거래 메타 / 로그 정책 / 감사 기준의 현재 상태 판정
3. 온라인·배치·ADM·PFW 공통 운영 기준 정렬 및 문서/매트릭스/리포트 반영
```

이번 작업에서 아래 항목은 구현하지 않는다.
필요하면 `CPF_STABILIZATION_REPORT.html`의 다음 보강 후보로만 기록한다.

```text id="y5mkv5"
이번 실행 제외:
- ADM 배치 운영 정책 전체 구현
- 배치 강제수행/lock 정책/pause/resume 전체 구현
- 온라인 거래 메타 자동 등록 전체 구현
- 온라인/배치 로그 정책 override 전체 구현
- ADM 로그/오류/감사 관제 전체 구현
- 온디맨드 배치 구현
- 센터컷 기본 구현체 구현
- 센터컷 모수/item/result 테이블 구현
- 업무별 커스텀 센터컷 adapter 구현
- ADM 브라우저 실제 클릭 자동화
- Redis/Kafka/MQ 실 broker 검증
```

요청 파일은 작업 대상으로 수정하지 않는다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 `CPF_STABILIZATION_REPORT.html`에만 기록한다.

---

## 1. 이번 작업의 핵심 의도

최근 작업으로 BAT 독립 모듈, BAT runtime smoke, heartbeat/progress/ghost detection 기반이 생겼다.
하지만 프레임워크는 배치만 잘 되면 완성되는 것이 아니다.

CPF는 온라인 거래, 배치, ADM 운영, 로그 정책, 감사, 권한, Swagger, 문서, EDU가 함께 맞아야 한다.

따라서 이번 작업은 아래 중심을 잡는다.

```text id="vk4ldf"
- 배치 기준이 PFW/ADM에 과하게 섞이지 않았는지 확인
- 온라인 거래 메타와 로그 정책이 방치되지 않았는지 확인
- transactionGlobalId 기준이 온라인/배치/EXS에 일관되는지 확인
- 로그 정책이 파일 로그/DB 로그/yml/DB/ADM override 구조로 정리되어 있는지 확인
- 기능 구현 매트릭스가 실제 구현 상태를 균형 있게 보여주는지 확인
- 다음 요청들이 한쪽으로 치우치지 않도록 후속 우선순위를 재정렬
```

이번 작업은 “많이 구현”이 아니라 “중심을 잃지 않게 기준을 고정”하는 작업이다.

---

## 2. 상위 아키텍처 전제

아래 책임 경계를 모든 문서와 리포트에서 일관되게 유지한다.

```text id="eqtr7q"
PFW:
- 공통 framework/library 성격
- 독립 프로세스가 아님
- 온라인/배치 공통 transactionGlobalId 기준 제공
- 표준 응답/오류/헤더 기준 제공
- Batch 공통 API, JobRepository 연계 메타 기준 제공
- 거래 메타, 로그 정책, 감사 기준의 공통 모델 제공
- 실제 BAT runtime 또는 ADM 운영 화면 책임을 소유하지 않음

BAT:
- 독립 실행 가능한 Spring Boot batch worker
- PFW Batch 공통 API 사용
- Spring Batch 표준 JobRepository 사용
- Job/Step 실행 주체
- heartbeat/progress/ghost 기반 실행 runtime
- 향후 온디맨드/센터컷 실행 기반

ADM:
- 운영/관리 콘솔
- 온라인 거래 메타, 로그 정책, 감사, 배치 관제, 권한, 운영 조치 관제
- 배치 Job/Step 실행 로직을 직접 소유하지 않음
- 로그 정책 override는 기간/사유/감사 이력이 있는 운영 조치로 관리

온라인 업무 모듈:
- Controller/API 단위 거래 메타 필요
- 거래ID와 거래 논리명 필요
- transactionGlobalId 생성/전파 기준 필요
- 표준 오류/응답/감사/로그 정책 적용 대상

EXS:
- 외부 송수신 거래도 transactionGlobalId와 parent/child 기준 필요
- 외부 호출 로그, 오류, 재시도, 감사 기준 필요
```

센터컷 전제도 유지한다.

```text id="ldo2eo"
센터컷 전제:
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않는다.
- PFW는 표준 인터페이스, 상태, 오류, 로그, 감사, transactionGlobalId 기준만 제공한다.
- BAT는 향후 기본 센터컷 모수/item/result 테이블과 기본 구현체를 제공한다.
- 업무 주제영역은 커스텀 모수/item/result 테이블을 추가할 수 있어야 한다.
- 업무별 테이블은 CenterCutTargetProvider / CenterCutHandler / ResultProvider adapter로 연동한다.
```

---

## 3. 먼저 현재 상태 판정

작업 시작 전 실제 소스/SQL/문서 기준으로 현재 상태를 판정한다.

아래 항목을 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```text id="pdeho0"
현재 상태 판정 항목:
- V10 migration 실제 DB 적용 여부
- BAT heartbeat/progress가 fallback 없이 V10 컬럼 기반으로 검증 가능한지
- 온라인 거래 메타 관련 package/source 존재 여부
- @CpfTransaction 또는 동등한 거래 annotation 존재 여부
- Controller/API 거래ID/거래 논리명 적용 현황
- RequestMapping 스캔 기반 거래 메타 자동 등록 존재 여부
- pfw_transaction_meta 또는 동등 테이블 존재 여부
- pfw_log_policy 또는 동등 로그 정책 테이블 존재 여부
- pfw_log_policy_override 또는 동등 ADM override 테이블 존재 여부
- pfw_transaction_log / pfw_error_log / pfw_audit_log 또는 동등 테이블 존재 여부
- ADM 거래 메타/로그 정책 관리 API/UI 존재 여부
- 온라인 거래 로그/오류 로그/감사 로그 연결 상태
- 배치 Job/Step 로그 정책과 온라인 거래 로그 정책의 관계
- Swagger/OpenAPI와 거래 메타 정합성 상태
- README/specs/기능 매트릭스에서 온라인/배치 균형 상태
```

상태값은 아래만 사용한다.

```text id="cx1fzi"
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

Codex 완료 메시지나 과거 리포트만 믿지 말고 실제 파일 기준으로 확인한다.

---

## 4. V10 실제 DB 적용 검증

직전 작업에서 V10 migration은 추가되었지만, local MariaDB에는 V10 컬럼이 적용되지 않아 runtime smoke가 fallback 경로로 성공했다.
이번 작업에서는 V10을 실제 DB에 적용하고 fallback이 아닌 V10 컬럼 기반으로 검증한다.

필수 확인 대상:

```text id="wkrk3i"
pfw_batch_execution:
- total_count
- processed_count
- success_count
- failure_count
- retry_count
- progress_rate
- tps
- avg_elapsed_ms
- max_elapsed_ms
- last_heartbeat_at
- current_step_name

pfw_batch_step_execution:
- total_count
- processed_count
- success_count
- failure_count
- retry_count
- progress_rate
- tps
- avg_elapsed_ms
- max_elapsed_ms
- last_heartbeat_at
```

필수 검증:

```text id="m1bz5v"
- V10 migration 실제 적용
- information_schema 기준 컬럼 존재 확인
- BAT heartbeat Job 실행
- processed_count 실제 컬럼 값 증가 확인
- progress_rate 실제 컬럼 값 확인
- last_heartbeat_at 실제 컬럼 값 갱신 확인
- step execution progress 값 확인
- smoke-bat-runtime에서 fallback 경로가 아니라 V10 컬럼 기반 검증 수행
```

Flyway Gradle task가 없으면 성공으로 기록하지 않는다.
대체 방법으로 V10 SQL을 실제 DB에 적용했다면 적용 명령, 결과, 적용 기준을 리포트에 기록한다.

---

## 5. 온라인 거래 메타 현재 상태 판정

이번 작업에서 온라인 거래 메타 전체 구현을 완료하지 않는다.
다만 현재 상태를 실제 파일 기준으로 판정하고, 부족한 항목을 다음 요청으로 분리할 수 있게 만든다.

필수 판정 항목:

```text id="u5r38b"
- Controller/API 단위 거래ID 관리 방식
- 거래 논리명 관리 방식
- transactionGlobalId 생성 위치
- transactionGlobalId 응답 헤더/로그 전파 여부
- 표준 요청 헤더 기준
- 표준 응답/오류 구조
- 온라인 API 오류 로그 저장 여부
- 온라인 API 감사 로그 저장 여부
- Swagger Operation과 거래 메타 연결 여부
- RequestMapping 스캔 또는 annotation scan 존재 여부
- WAS 기동 후 거래 메타 upsert 구조 존재 여부
- 사라진 거래 inactive 처리 여부
```

온라인 거래 메타가 미구현이면 미구현으로 기록한다.
문서에만 있으면 문서만 존재한다고 기록한다.
Controller annotation만 있고 DB upsert가 없으면 부분 구현으로 기록한다.

---

## 6. 로그 정책 현재 상태 판정

파일 로그와 DB 로그 정책이 온라인/배치 모두에서 균형 있게 설계되어 있는지 확인한다.

필수 판정 항목:

```text id="z02me0"
- application.yml 또는 profile yml의 기본 로그 정책
- 파일 로그 기본 정책
- DB 로그 저장 정책
- 온라인 거래별 DB 로그 ON/OFF 가능 여부
- 배치 Job별 DB 로그 ON/OFF 가능 여부
- 배치 Step별 DB 로그 ON/OFF 가능 여부
- ADM override 구조 존재 여부
- override 기간/사유/변경자/감사 이력 존재 여부
- 로그 정책 우선순위 문서화 여부
```

로그 정책 우선순위는 아래 기준으로 맞춘다.

```text id="7lxvsu"
1. ADM 활성 override
2. DB 운영 정책
3. application.yml 기본값
4. CPF 기본값
```

이번 작업에서 로그 정책 전체 구현을 하지 않는다.
다만 실제 구현 상태와 문서 상태가 다르면 리포트와 매트릭스에 반영한다.

---

## 7. 온라인·배치 공통 운영 기준 정렬

문서와 매트릭스에서 온라인과 배치가 아래 공통 축을 공유하도록 정리한다.

```text id="sqlodp"
공통 축:
- transactionGlobalId
- traceId / spanId
- 표준 응답
- 표준 오류
- 표준 헤더
- 권한
- 감사 로그
- 오류 로그
- 파일 로그
- DB 로그
- 로그 정책 override
- Swagger/OpenAPI
- EDU 샘플
- ADM 관제
```

온라인과 배치의 차이도 명확히 적는다.

```text id="i0gixn"
온라인:
- Controller/API 단위 거래 메타
- RequestMapping/annotation scan
- 거래별 로그/오류/감사 연결
- 짧은 요청/응답 트랜잭션

배치:
- Job/Step 단위 메타
- JobExecutionId / StepExecutionId
- workerId / serverInstanceId
- heartbeat / progress / ghost
- 장시간 실행 트랜잭션
```

---

## 8. 기능 구현 매트릭스 보강

`specs/기능_구현_매트릭스.html`는 배치만 상세하고 온라인이 비어 있으면 안 된다.
아래 항목을 실제 상태 기준으로 행을 추가하거나 보강한다.

```text id="h55wei"
필수 행:
- 온라인 거래 메타
- 온라인 거래 메타 자동 등록
- 온라인 transactionGlobalId
- 온라인 오류 로그
- 온라인 감사 로그
- 온라인 파일 로그 정책
- 온라인 DB 로그 정책
- ADM 로그 정책 override
- 배치 Job 로그 정책
- 배치 Step 로그 정책
- BAT heartbeat/progress
- BAT ghost detection
- ADM 배치 관제
- ADM 거래/오류/감사 관제
```

상태값은 실제 기준으로 기록한다.

```text id="kvixxv"
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

완료는 증거가 있을 때만 사용한다.
미구현 항목을 숨기지 않는다.

---

## 9. 문서 반영 기준

이번 작업에서 문서는 균형 기준을 중심으로 최소 갱신한다.

필수 반영:

```text id="1k6o8j"
README.md:
- 현재 CPF 균형 상태 요약
- 온라인/배치 공통 기준 요약
- 다음 요청 우선순위 요약

specs/프레임워크_구성_가이드.html:
- PFW/BAT/ADM/온라인 모듈 책임 경계
- transactionGlobalId 공통 기준
- 온라인/배치 운영 메타 차이

specs/개발_가이드.html:
- 온라인 거래 메타와 배치 메타의 개발 기준 연결
- Controller/API 거래ID 기준
- 배치 Job/Step 기준

specs/배치_개발_가이드.html:
- V10 실제 DB 적용 결과
- heartbeat/progress 컬럼 기반 검증 결과
- 온라인 기준과 다른 점 정리

specs/관리자_가이드.html 또는 운영_매뉴얼.html:
- ADM에서 온라인 거래/배치 관제를 어떻게 분리해서 보는지 정리

specs/SQL_가이드.html:
- V10 실제 DB 적용 결과
- 온라인 거래/로그 정책 테이블 현재 상태 판정
- 배치 메타 테이블과 온라인 거래 메타 테이블 관계

specs/기능_구현_매트릭스.html:
- 온라인/배치 공통 항목 균형 있게 반영

CPF_STABILIZATION_REPORT.html:
- 실제 확인 결과와 미구현/미검증 항목 기록
```

문서에 없는 기능을 완료로 쓰지 않는다.
구현 없는 문서는 “설계/기준 문서”로 구분한다.

---

## 10. 테스트 / 검증 명령

이번 작업은 V10 실제 DB 적용 검증과 상태 판정 중심이다.
가능한 범위에서 아래를 실행한다.

```powershell id="uizkw1"
.\gradlew.bat :bat:test --offline
.\gradlew.bat :bat:bootJar --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-bat-runtime.ps1

.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

가능하면 ADM runtime smoke도 재실행한다.

```powershell id="8woccc"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
```

V10 DB 적용 검증은 실제 MariaDB 기준으로 수행한다.
실제 MariaDB에 적용하지 못했으면 성공으로 기록하지 않는다.

---

## 11. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 아래 형식으로 기록한다.

```text id="mhakbq"
[프레임워크 균형 게이트 / V10 실제 DB 적용 / 온라인·배치 공통 운영 기준 정렬]

현재 상태 판정:
- V10 실제 DB 적용:
- BAT heartbeat/progress fallback 제거 여부:
- 온라인 거래 메타:
- 온라인 로그 정책:
- 온라인 오류/감사 로그:
- ADM 온라인 거래 관제:
- 배치 메타/로그 정책:
- 문서/매트릭스 균형 상태:

실제 확인:
- 확인한 소스:
- 확인한 SQL:
- 확인한 문서:
- 실행한 명령:
- 실제 결과:

보강 내용:
- README:
- 프레임워크 구성 가이드:
- 개발 가이드:
- 배치 개발 가이드:
- 관리자/운영 매뉴얼:
- SQL 가이드:
- 기능 구현 매트릭스:

미구현:
- 미구현 항목:
- 다음 요청 후보:

미검증:
- 미검증 항목:
- 미검증 사유:

아키텍처 영향:
- PFW:
- BAT:
- ADM:
- 온라인 거래:
- 센터컷 확장성:

최종 판정:
- 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요
```

실행하지 않은 검증은 성공으로 기록하지 않는다.
상태 판정과 구현 완료를 혼동하지 않는다.

---

## 12. 완료 기준

아래가 모두 충족되어야 이번 요청을 완료로 기록한다.

```text id="c6g7wl"
- V10 migration이 실제 MariaDB에 적용되었다.
- pfw_batch_execution V10 컬럼이 information_schema 기준으로 확인되었다.
- pfw_batch_step_execution V10 컬럼이 information_schema 기준으로 확인되었다.
- BAT heartbeat Job이 fallback 없이 V10 컬럼 기반으로 progress/heartbeat를 검증했다.
- 온라인 거래 메타 현재 상태가 실제 소스/SQL 기준으로 판정되었다.
- 온라인 로그 정책 현재 상태가 실제 소스/SQL 기준으로 판정되었다.
- 기능 구현 매트릭스에 온라인/배치 공통 항목이 균형 있게 반영되었다.
- README와 specs 문서가 온라인/배치/ADM/PFW 책임 경계를 일관되게 설명한다.
- 미구현/미검증 항목이 숨겨지지 않았다.
- 다음 요청 우선순위가 프레임워크 전체 균형 관점에서 정리되었다.
```

---

## 13. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text id="3fvssi"
- V10 실제 DB 적용 없이 완료 처리
- fallback 경로로 smoke가 성공했는데 V10 컬럼 검증 성공으로 기록
- 온라인 거래 메타를 확인하지 않고 배치만 정리
- 온라인 로그 정책을 확인하지 않고 배치 로그만 정리
- 기능 구현 매트릭스가 배치 중심으로만 갱신됨
- 미구현 항목을 숨김
- 문서 기준과 실제 소스/SQL 상태가 다름
- 이번 요청 범위를 넘어 온라인 거래 메타 전체 구현 또는 로그 정책 전체 구현을 시작함
- 이번 요청 범위를 넘어 배치 운영 정책/센터컷/온디맨드 구현을 시작함
- 실행하지 않은 검증을 성공으로 기록
```

---

## 14. 다음 보강 후보 정렬 기준

이번 작업 이후 다음 요청 후보는 실제 판정 결과를 기준으로 정렬한다.
다만 기본 방향은 아래 순서를 따른다.

```text id="j49i06"
1. 온라인 거래 메타 자동 등록 + 거래 로그 정책 최소 구현
2. ADM 배치 운영 정책 / 등록 옵션 / 파라미터 / Lock / BAT EDU
3. ADM 거래/오류/감사/로그 관제 연결
4. ADM 브라우저 실제 클릭 자동화
5. 온디맨드 배치
6. 센터컷 기본 구현체 + 업무별 커스텀 모수/item/result 테이블 연동
7. BAT EDU / XYZ EDU 추가 샘플 보강
8. 트랜잭션 가이드 정본화
9. Redis/Kafka/MQ mock/fallback 및 실 broker 미검증 절차
```

온라인 거래 쪽이 너무 미구현이면 1번을 먼저 한다.
배치 운영 정책이 더 큰 리스크로 확인되면 2번을 먼저 한다.
둘 다 비슷하면 온라인 거래 메타를 먼저 닫아 프레임워크 균형을 맞춘다.

센터컷 후속 작업 기준은 계속 유지한다.

```text id="bo0ilv"
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않음
- BAT는 기본 센터컷 모수/item/result 테이블과 기본 구현체 제공
- 업무 주제영역은 커스텀 모수/item/result 테이블을 추가할 수 있음
- 업무별 테이블은 CenterCutTargetProvider / CenterCutHandler / ResultProvider adapter로 연동
- ADM은 기본 테이블/커스텀 테이블 여부와 관계없이 공통 관제 제공
```
