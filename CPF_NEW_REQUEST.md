# CPF_M8_CENTER_CUT_ADM_OBSERVABILITY_REQUEST — ADM Center-Cut 관제 API/UI 및 Runtime Smoke 요청

## 0. CPF 최종 목표

이번 작업은 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 최상위 목표 기준서로 따른다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 전체 목표 기준서이며, 이번 작업에서 모든 항목을 구현하라는 범위 문서가 아니다. Codex는 이번 요청서 범위를 우선 수행하되, 구현 과정에서 CPF 최종 목표와 충돌하지 않도록 설계한다.

이번 작업에서 특히 지킬 기준은 아래와 같다.

```text
1. center-cut은 PFW 표준 계약, BAT 기본 모수/실행 메타, 업무 DB adapter, ADM 관제로 분리한다.
2. PFW는 업무 item/result 저장소를 직접 소유하지 않는다.
3. ADM은 운영자가 center-cut 상태, target, result, parent/child transactionGlobalId, 실패 사유를 조회할 수 있어야 한다.
4. 실행하지 않은 검증은 완료로 기록하지 않는다.
5. 문서 정본화보다 기능 구현과 runtime/API smoke를 우선한다.
```

---

## 1. 이번 작업 목적

이번 작업 목적은 이전 마일스톤에서 구현한 XYZ 업무 DB 기반 center-cut adapter를 ADM 운영 콘솔에서 조회할 수 있게 만드는 것이다.

이번 작업 범위는 아래다.

```text
1. ADM center-cut 관제 API 추가
2. ADM center-cut 관제 UI 최소 추가
3. job 모수, parameter, target, result 조회
4. 성공/실패/처리중/대기 건수 조회
5. parent/child transactionGlobalId 조회
6. 실패 사유 조회
7. ADM runtime 또는 OpenAPI smoke 추가/실행
8. 리포트와 기능 매트릭스 상태값 최소 갱신
```

이번 작업은 Redis/Kafka/MQ 실 broker, 전체 브라우저 클릭 정본화, 최종 문서 정본화 작업이 아니다.

---

## 2. 필수 제한

```text
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증 완료 기록 금지
별도 변경파일 목록 산출물 생성 금지
문서 포맷 작업만으로 기능 완료 처리 금지
HTML 문서 신규 작성 금지
HTML 문서 직접 수정 지양
현재 마일스톤 목적과 무관한 대규모 리팩터링 금지
```

`CPF_NEW_REQUEST.md`는 사용자 요청 원본으로 보고 기능 검수 대상에서 제외한다.

---

## 3. 작업 시작 전 기준 기록

작업 시작 전에 아래를 확인하고 `CPF_STABILIZATION_REPORT.md`에 기록한다.

```powershell
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

safe.directory 문제로 실패하면 `git -c safe.directory=...`로 보조 확인하고 사유를 기록한다.

---

## 4. 현재 center-cut 구조 확인

먼저 현재 구현을 실제 파일 기준으로 확인한다.

확인 대상:

```text
pfw/src/main/java/cpf/pfw/common/batch/centercut/*
bat center-cut provider/handler/repository/job/step
xyz/src/main/java/cpf/xyz/edu/centercut/*
xyz/src/main/java/cpf/xyz/edu/service/XyzCenterCutEducationService.java
xyz/src/main/java/cpf/xyz/edu/controller/XyzCenterCutEducationController.java
specs/sql/35_bat_schema.sql
specs/sql/40_business_modules_schema.sql
specs/sql/migration/flyway/V18__xyz_center_cut_sample.sql
specs/sql/00_all_install.sql
specs/sql/99_smoke_check.sql
scripts/smoke-center-cut-adapter.ps1
CPF_STABILIZATION_REPORT.md
specs/기능_구현_매트릭스.html
```

확인할 기준:

```text
1. PFW가 업무 item/result 저장소를 직접 소유하지 않는지
2. BAT가 center-cut job/parameter 기본 모수를 소유하는지
3. XYZ가 target/result 업무 테이블을 소유하는지
4. ADM이 어느 DB를 어떤 방식으로 조회해야 하는지
```

---

## 5. ADM Center-Cut 관제 API

### 5.1 API 후보

아래 API를 추가한다. 기존 ADM path 규칙이 있으면 그 규칙을 우선한다.

```text
GET /adm/api/center-cut/jobs
GET /adm/api/center-cut/jobs/{centerCutJobId}
GET /adm/api/center-cut/jobs/{centerCutJobId}/parameters
GET /adm/api/center-cut/jobs/{centerCutJobId}/summary
GET /adm/api/center-cut/jobs/{centerCutJobId}/targets
GET /adm/api/center-cut/jobs/{centerCutJobId}/results
GET /adm/api/center-cut/results/{resultId}
```

### 5.2 조회 대상

기본 조회 대상:

```text
pfwDB.bat_center_cut_job
pfwDB.bat_center_cut_parameter
xyzDB.xyz_center_cut_sample_target
xyzDB.xyz_center_cut_sample_result
```

주의:

```text
1. ADM이 PFW 업무 로직을 직접 수행하지 않는다.
2. ADM은 운영 조회와 조치 요청만 담당한다.
3. XYZ 업무 DB 직접 조회가 필요하면 read-only repository/mapper로 분리하고, 향후 다른 업무 adapter가 추가될 수 있는 구조를 고려한다.
4. PFW가 업무 item/result 저장소를 다시 소유하는 구조로 회귀하지 않는다.
```

### 5.3 응답 필드

job 목록/상세:

```text
centerCutJobId
batchJobId
centerCutJobName
providerKey
handlerKey
chunkSize
retryLimit
useYn
description
createdAt
updatedAt
```

summary:

```text
centerCutJobId
readyCount
runningCount
successCount
failedCount
skippedCount
retryRequestedCount
stopRequestedCount
totalCount
lastStartedAt
lastCompletedAt
```

target/result:

```text
targetId
businessKey
businessDate
statusCode
retryCount
parentTransactionGlobalId
childTransactionGlobalId
startedAt
completedAt
lastErrorMessage
resultId
resultStatus
resultMessage
resultPayloadMasked
```

민감정보 기준:

```text
resultPayload는 기본 마스킹 또는 요약값으로 표시한다.
원문 payload를 그대로 운영 화면에 노출하지 않는다.
```

---

## 6. ADM UI 최소 반영

기존 ADM static UI 구조를 확인하고, 최소한 아래 화면/버튼/테이블을 추가한다.

```text
Center-Cut 메뉴 또는 Batch 하위 Center-Cut 섹션
job 목록
job 상세
summary 카드
target/result 목록
상태 필터
실패 사유 표시
parent/child transactionGlobalId 표시
새로고침 버튼
```

이번 작업에서는 고급 운영자 조치 버튼은 필수 아님.

보류 가능:

```text
강제 재실행
중지 요청
lock 강제 해제
2인 승인
브라우저 클릭 상세 UX 정본화
```

단, 버튼을 만들지 않았으면 리포트에 미구현으로 남긴다.

---

## 7. ADM Runtime / OpenAPI Smoke

가능하면 ADM runtime을 실제로 기동하고 OpenAPI 또는 API smoke를 수행한다.

후보 스크립트:

```text
scripts/smoke-adm-center-cut-runtime.ps1
```

검증 항목:

```text
1. ADM 앱 기동
2. center-cut jobs API 200
3. job 상세 API 200
4. summary API 200
5. targets API 200
6. results API 200
7. CPF_XYZ_CENTER_CUT_SAMPLE_JOB 조회 가능
8. 성공 3건/실패 1건 또는 현재 DB 상태 기준 count 조회 가능
9. parent/child transactionGlobalId 응답 포함
10. 실패 사유 응답 포함
11. 민감 payload 원문 노출 없음
```

OpenAPI smoke 후보:

```text
scripts/smoke-openapi.ps1
```

주의:

```text
ADM runtime을 실제 기동하지 못하면 adm-runtime/openapi-runtime 완료로 기록하지 않는다.
정적 파일 존재만으로 browser click 완료 처리하지 않는다.
```

---

## 8. SQL / DB 변경 기준

이번 작업은 가능하면 신규 테이블을 만들지 않는다.
ADM 조회 API는 기존 `bat_center_cut_*`, `xyz_center_cut_sample_*`를 읽는 구조를 우선한다.

DB 변경이 필요하면 아래를 함께 반영한다.

```text
split SQL
Flyway
00_all_install.sql
00_all_install_and_smoke.sql
99_smoke_check.sql
```

한 경로에만 반영하면 완료가 아니다.

---

## 9. 실행 검증 요구사항

가능한 범위에서 아래를 실행한다.

```powershell
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat :xyz:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-center-cut-adapter.ps1 -RequireRun
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-center-cut-runtime.ps1

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake

.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

실행하지 못한 검증은 미검증으로 기록한다.

---

## 10. 리포트 / 기능 매트릭스 반영

`CPF_STABILIZATION_REPORT.md`와 `specs/기능_구현_매트릭스.html`에 아래를 최소 반영한다.

```text
1. ADM center-cut API 구현 여부
2. ADM center-cut UI 최소 반영 여부
3. ADM runtime smoke 실행 여부
4. OpenAPI runtime smoke 실행 여부
5. center-cut job/summary/target/result 조회 검증 결과
6. parent/child transactionGlobalId 조회 여부
7. 실패 사유 조회 여부
8. 민감 payload 원문 노출 여부
9. 실행하지 않은 검증의 미검증 사유
10. 남은 운영자 조치 기능
```

문서 정본화, HTML 디자인, 가이드 장문 보강은 이번 범위가 아니다.

---

## 11. 완료 기준

아래가 충족되어야 이번 작업 완료로 본다.

```text
1. ADM center-cut job 목록 API가 동작한다.
2. ADM center-cut job 상세 API가 동작한다.
3. ADM center-cut summary API가 동작한다.
4. target/result 목록 API가 동작한다.
5. CPF_XYZ_CENTER_CUT_SAMPLE_JOB을 조회할 수 있다.
6. 성공/실패/대기/처리중 count를 조회할 수 있다.
7. parent/child transactionGlobalId를 조회할 수 있다.
8. 실패 사유를 조회할 수 있다.
9. result payload 원문 노출을 통제한다.
10. ADM UI에 center-cut 조회 진입점이 최소 추가된다.
11. ADM runtime 또는 API smoke 결과가 기록된다.
12. 실행하지 않은 검증은 완료로 기록하지 않는다.
13. 리포트와 기능 매트릭스 상태값이 실제 파일과 일치한다.
14. Git commit / push / branch 생성 없음.
```

---

## 12. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

```text
1. ADM API 없이 UI 정적 문구만 추가
2. job 목록만 있고 target/result 조회 없음
3. parent/child transactionGlobalId 조회 없음
4. 실패 사유 조회 없음
5. result payload 원문 무제한 노출
6. ADM이 업무 처리를 직접 수행하는 구조
7. PFW가 업무 item/result 저장소를 다시 소유하는 구조
8. runtime/API smoke 미실행인데 완료 기록
9. 정적 UI marker만으로 browser click 완료 기록
10. 리포트와 기능 매트릭스 상태 불일치
11. 민감정보 원문 기록
12. Git commit / push / branch 생성
13. 별도 변경파일 목록 산출물 생성
```

---

## 13. 완료 보고 양식

```text
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- 사용자 요청 원본 파일:

2. ADM center-cut API
- jobs:
- detail:
- parameters:
- summary:
- targets:
- results:
- result detail:

3. ADM UI
- 메뉴:
- 목록:
- 상세:
- 상태 필터:
- 실패 사유:
- transactionGlobalId 표시:

4. Runtime/OpenAPI smoke
- ADM runtime:
- OpenAPI:
- center-cut API smoke:
- browser click:

5. 실행 검증
- :adm:test:
- :xyz:test:
- 전체 test:
- smoke-center-cut-adapter:
- smoke-adm-center-cut-runtime:
- check-sql-standard:
- check-feature-evidence:
- check-html-docs:
- check-utf8:
- qualityGate:

6. 상태값 반영
- ADM center-cut 관제:
- ADM runtime:
- OpenAPI runtime:
- ADM browser click:
- center-cut adapter:
- standard-header-e2e:

7. 남은 작업
- 운영자 조치 버튼:
- 강제 재실행/중지:
- browser click:
- Redis/Kafka/MQ:
- EDU/XYZ 실전 샘플:
- CMN 고정길이 전문:
```

---

## 14. 다음 마일스톤 분기

이번 작업이 완료되면 다음은 아래 중 하나로 간다.

```text
1. ADM browser click 및 운영 UX 보강
2. EDU/XYZ 실전 샘플 CRUD/paging/validation/facade/outbound 확대
3. CMN 고정길이 전문 처리
4. EXS 전문 송수신
```

분기 기준:

```text
1. ADM runtime/OpenAPI가 실패하면 ADM smoke 보정 먼저 진행
2. ADM center-cut 관제가 완료되면 browser click 또는 EDU/XYZ 실전 샘플로 진행
3. ADM 환경 제약이 명확하면 API smoke까지만 닫고 browser click은 미검증 유지 후 기능 개발로 전진
```
