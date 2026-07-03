# CPF_M9_M8_M6_BIG_TRACE_REQUEST — 복합 거래 Trace 표준 / ADM 거래 그룹 조회 / CMN 전문 엔진 보강 요청

## 0. CPF 최종 목표

이번 작업은 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 최상위 목표 기준서로 따른다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 CPF의 최종 목표, 설계 원칙, 상태값, 완료 기준, 완료 불인정 기준을 정의한 상위 기준서다. Codex는 이번 요청서 범위를 우선 수행하되, 구현 과정에서 CPF 최종 목표와 충돌하지 않도록 설계한다.

이번 요청은 개발 진도를 빠르게 내기 위해 평소보다 넓게 구성한다. 단, 필수 완료 범위를 먼저 닫고, 보강/착수 범위는 가능 범위에서 진행한다. 완료하지 못한 항목은 완료로 포장하지 말고 `부분 구현`, `미구현`, `미검증`, `재확인 필요`로 기록한다.

이번 작업의 핵심은 단순 CRUD 추가가 아니다. CPF가 운영형 프레임워크가 되기 위해 필요한 **복합 거래 흐름 추적**, **transactionGlobalId 그룹 조회**, **구간별 수행시간**, **ADM 거래 timeline 조회**를 구축하는 것이다.

---

## 1. 이번 작업 목적

이번 작업 목적은 네 가지다.

```text id="10e22p"
1. ACC/MBR/EXS를 지나는 복합 거래 추적 표준을 만든다.
2. transactionGlobalId 기준으로 전체 거래를 그룹 조회할 수 있게 한다.
3. ADM에서 목록/상세/timeline/구간별 수행시간을 운영자가 볼 수 있게 한다.
4. CMN fixed-length 전문 skeleton을 다음 단계 엔진 수준으로 보강한다.
```

이번 작업 우선순위는 아래다.

```text id="65kcvj"
필수 완료:
- PFW 거래 segment/role 표준 최소 구현
- ACC→MBR→EXS 복합 거래 샘플 최소 1개 runtime smoke
- transactionGlobalId 단일 검색으로 전체 구간 조회
- ADM 거래 그룹 목록/상세/timeline API
- 전체 수행시간/구간별 수행시간/실패 구간 조회

보강 범위:
- ACC→MBR→ACC→EXS 패턴과 ACC→MBR→EXS→MBR→ACC 패턴 둘 다 source-level 제공
- ADM 거래 그룹 UI 최소 섹션
- standard-header-e2e 재실행 또는 재검증

착수/확장 범위:
- CMN fixed-length 반복부/타입 변환/오류코드 skeleton 보강
- EXS 송수신 로그 연결 후보 설계
```

---

## 2. 필수 제한

```text id="3c8f8f"
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증 완료 기록 금지
별도 변경파일 목록 산출물 생성 금지
문서 포맷 작업만으로 기능 완료 처리 금지
HTML 신규 문서 작성 금지
현재 마일스톤 목적과 무관한 대규모 리팩터링 금지
```

`CPF_NEW_REQUEST.md`는 사용자 요청 원본으로 보고 기능 검수 대상에서 제외한다. 수정하지 않는다.

---

## 3. 작업 시작 전 기준 기록

작업 시작 전에 아래를 확인하고 `CPF_STABILIZATION_REPORT.md`에 기록한다.

```powershell id="2xbcww"
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

기록할 내용:

```text id="snefp6"
1. 작업 시작 branch
2. 작업 시작 HEAD SHA
3. 작업 시작 origin/master SHA
4. 작업 시작 status --short
5. CPF_NEW_REQUEST.md 변경 여부와 사유
6. 이전 작업 미검증 항목
```

---

## 4. 필수 완료 범위 A — PFW 복합 거래 Trace 표준

### 4.1 기본 개념

기존 `transactionGlobalId`는 전체 거래 묶음 ID로 유지한다. 여기에 구간 추적을 위한 segment 개념을 추가한다.

필수 개념:

```text id="n8lgb2"
transactionGlobalId       전체 거래 묶음 ID
transactionSegmentId      현재 구간 ID
parentSegmentId           상위 구간 ID
rootTransactionGlobalId   최초 거래 ID
parentTransactionGlobalId 부모 거래 ID
transactionRole           MAIN / SUB / SHARED / EXTERNAL / BATCH / CENTER_CUT_PARENT / CENTER_CUT_CHILD / OPERATOR_ACTION
moduleCode                ACC / MBR / EXS / CMN / ADM / BAT / XYZ
sourceModuleCode          호출한 모듈
targetModuleCode          호출 대상 모듈
direction                 INBOUND / OUTBOUND / INTERNAL / RESPONSE
callDepth                 호출 깊이
sequenceNo                거래 내 순번
startedAt                 구간 시작시각
endedAt                   구간 종료시각
durationMs                구간 수행시간
status                    SUCCESS / FAILED / PARTIAL_FAILED / RUNNING
failureYn                 실패 여부
failureCode               실패 코드
failureMessageMasked      실패 메시지 마스킹
```

### 4.2 구현 방식

대규모 리팩터링을 피하고 최소 구현으로 시작한다.

허용 방식:

```text id="jx04fz"
1. 기존 거래 로그 테이블에 컬럼 추가
2. 별도 pfw_transaction_segment 테이블 추가
3. 기존 log detail JSON에 segment metadata 추가
```

선호 방식:

```text id="lf8ny6"
pfw_transaction_segment 또는 동등한 segment 저장 구조를 만든다.
```

단, 기존 표준 헤더 Runtime E2E와 center-cut parent/child transactionGlobalId 구조를 깨면 안 된다.

### 4.3 SQL 반영

DB 변경이 있으면 아래 경로를 모두 반영한다.

```text id="a0tn36"
split SQL
Flyway migration
00_all_install.sql
00_all_install_and_smoke.sql
99_smoke_check.sql
test fixture
```

한 경로에만 있으면 완료가 아니다.

---

## 5. 필수 완료 범위 B — 복합 거래 샘플

### 5.1 필수 샘플 패턴

아래 두 패턴 중 최소 1개는 runtime smoke까지 완료한다. 나머지 1개는 source-level 또는 test 수준까지 제공한다.

```text id="xkqzxj"
패턴 A:
ACC → MBR → ACC → EXS → ACC

패턴 B:
ACC → MBR → EXS → MBR → ACC
```

가능하면 두 패턴 모두 runtime smoke를 수행한다.

### 5.2 샘플 의미

패턴 A는 ACC가 orchestration 주체인 구조다.

```text id="fw0ape"
ACC MAIN inbound
→ ACC OUTBOUND to MBR
→ MBR SHARED/SUB processing
→ ACC resume
→ ACC OUTBOUND to EXS
→ EXS EXTERNAL processing
→ ACC final response
```

패턴 B는 MBR 내부에서 EXS를 호출하는 중첩 구조다.

```text id="x3jrzb"
ACC MAIN inbound
→ ACC OUTBOUND to MBR
→ MBR SUB inbound
→ MBR OUTBOUND to EXS
→ EXS EXTERNAL processing
→ EXS response
→ MBR response
→ ACC response
```

### 5.3 구현 위치

후보 모듈:

```text id="8lob46"
acc: 최초 거래 시작 샘플
mbr: 회원 조회/검증 shared/sub 거래 샘플
exs: 외부기관 mock 송수신 샘플
xyz/edu: 개발자가 복사할 수 있는 복합 거래 교육 API
```

주의:

```text id="w1cdsh"
ACC가 MBR 테이블 직접 조회 금지
MBR이 EXS 테이블 직접 조회 금지
EXS 송수신 로그를 원문 민감정보 그대로 저장 금지
표준 헤더 수동 하드코딩 금지
CpfWebClient 또는 CpfRestClient wrapper 사용
```

### 5.4 샘플 API 후보

기존 path 규칙이 있으면 기존 규칙을 우선한다.

```text id="mna1bb"
POST /acc/edu/composite/member-then-external
POST /acc/edu/composite/member-calls-external
GET  /xyz/edu/transactions/composite-sample
```

응답에는 아래를 포함한다.

```text id="iu219g"
transactionGlobalId
rootTransactionGlobalId
segments count
module flow
overallStatus
overallDurationMs
failedSegmentId
```

---

## 6. 필수 완료 범위 C — ADM 거래 그룹 목록/상세/timeline

ADM 거래 조회는 단건 로그 나열이 아니라 **transactionGlobalId 기준 그룹 목록**이어야 한다.

### 6.1 API 후보

기존 ADM transaction API가 있으면 호환 유지 후 확장한다.

```text id="ts31ie"
GET /adm/api/transaction-groups
GET /adm/api/transaction-groups/{transactionGlobalId}
GET /adm/api/transaction-groups/{transactionGlobalId}/segments
GET /adm/api/transaction-groups/{transactionGlobalId}/timeline
GET /adm/api/transaction-groups/{transactionGlobalId}/headers
GET /adm/api/transaction-groups/{transactionGlobalId}/external-logs
```

기존 `/adm/api/transactions`가 거래 메타 API로 사용 중이면 혼동을 피한다.
거래 메타는 `@CpfTransaction` API 정의 조회이고, 거래 그룹은 runtime 거래 로그 조회다. 둘의 의미를 분리한다.

### 6.2 목록 검색조건

사용자가 말하지 않아도 운영형 프레임워크 기준으로 아래 검색조건을 제공한다.

```text id="jhu8d1"
거래일자/시간 범위
transactionGlobalId
최초 모듈 originModuleCode
포함 모듈 includedModuleCode
sourceModuleCode
targetModuleCode
transactionRole
direction
status
failureYn
failedModuleCode
failedSegmentId
durationMsFrom
durationMsTo
customerNo
memberNo
userId
operatorId
channelCode
originalChannelCode
externalInstitutionCode
externalTransactionId
apiPath
transactionName
limit
cursor 또는 page
sort
```

정렬은 whitelist만 허용한다.

후보 sort:

```text id="ab82gy"
startedAtDesc
durationDesc
statusAsc
failedFirst
moduleAsc
```

### 6.3 목록 표시 필드

ADM 목록은 운영자가 한눈에 장애 원인을 볼 수 있어야 한다.

```text id="8u0zow"
transactionGlobalId
transactionName
originModuleCode
moduleFlowText       예: ACC → MBR → EXS
rolesText            예: MAIN / SHARED / EXTERNAL
overallStatus
failureYn
failedModuleCode
failedSegmentName
failureMessageMasked
startedAt
endedAt
totalDurationMs
segmentCount
externalCallCount
customerNoMasked
memberNoMasked
channelCode
originalChannelCode
externalInstitutionCode
externalTransactionId
```

### 6.4 상세/timeline 표시 필드

상세는 segment/timeline 구조로 제공한다.

```text id="yb83ku"
transactionSegmentId
parentSegmentId
sequenceNo
callDepth
moduleCode
sourceModuleCode
targetModuleCode
transactionRole
direction
apiPath
transactionName
status
startedAt
endedAt
durationMs
requestHeaderSnapshotMasked
responseHeaderSnapshotMasked
extensionHeaderSnapshotMasked
failureCode
failureMessageMasked
externalInstitutionCode
externalTransactionId
```

timeline 응답은 트리 구조 또는 flat + parentSegmentId 구조 둘 중 하나를 제공한다.

예시:

```text id="9kfru2"
ACC MAIN inbound / 120ms
└─ ACC → MBR OUTBOUND / 80ms
   └─ MBR SHARED processing / 45ms
      └─ MBR → EXS EXTERNAL / 850ms
         └─ EXS response / 20ms
└─ ACC final response / 30ms
```

### 6.5 마스킹/권한/감사

ADM 조회에는 아래 기준을 적용한다.

```text id="3pkedr"
민감 헤더 원문 노출 금지
Authorization, token, api-key, secret, password 계열 원문 노출 금지
고객번호/회원번호는 정책에 따라 마스킹 가능해야 함
운영자 권한별 상세 조회 통제
거래 상세 조회 감사 로그 후보 기록
다운로드 기능은 이번 범위에서 제외하되, 추후 다운로드 감사 대상 후보로 기록
```

---

## 7. 필수 완료 범위 D — Runtime Smoke

### 7.1 신규 smoke 후보

```powershell id="iphk5y"
scripts/smoke-composite-transaction-runtime.ps1
scripts/smoke-adm-transaction-group-runtime.ps1
```

### 7.2 smoke 검증 항목

복합 거래 smoke는 아래를 확인한다.

```text id="s6cgk2"
1. ACC 시작 API 호출 성공
2. transactionGlobalId 생성 또는 수신
3. MBR 구간 생성
4. EXS 구간 생성
5. transactionGlobalId가 모든 구간에서 동일
6. transactionSegmentId가 구간별로 다름
7. parentSegmentId 관계 존재
8. MAIN / SUB 또는 SHARED / EXTERNAL role 존재
9. 전체 수행시간 totalDurationMs 존재
10. 구간별 durationMs 존재
11. 실패 구간이 없으면 SUCCESS
12. 실패 시 failedSegmentId와 failureMessageMasked 존재
```

ADM 거래 그룹 smoke는 아래를 확인한다.

```text id="5s9lxu"
1. /adm/api/transaction-groups 목록 200
2. 방금 발생한 transactionGlobalId 검색 가능
3. 목록에 moduleFlowText 표시
4. 목록에 totalDurationMs 표시
5. 상세에 segments 존재
6. timeline에 parent/child 구조 존재
7. 민감 헤더 원문 미노출
8. 검색조건 최소 5개 이상 동작 확인
9. sort whitelist 동작 확인
```

---

## 8. 보강 범위 — standard-header-e2e 재검증

가능하면 아래를 재실행한다.

```powershell id="m0myho"
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-standard-header-e2e.ps1
```

재실행 시 확인할 것:

```text id="xdc95v"
표준 헤더 수신
확장 헤더 수신
차단 확장 헤더 거부
TransactionContext 생성
DB log/header snapshot 저장
outbound mock downstream 전파
민감 헤더 원문 미저장
ADM 조회 가능 여부
```

실행하지 못하면 `미검증`으로 기록한다.

---

## 9. 보강 범위 — CMN fixed-length 전문 엔진 확장

이번 요청의 주력은 복합 거래 trace이므로 CMN 전문은 가능한 범위에서 보강한다.

### 9.1 보강 후보

```text id="nhbrmp"
반복부 group field skeleton
타입별 parse/format 변환
DATE/NUMBER/DECIMAL validation
오류 코드/필드 오류 구조
전문 사전 Layout registry skeleton
민감 필드 masking policy 확장
parse/format error result 표준화
```

### 9.2 완료 범위 구분

아래는 이번에 완료하면 좋지만, 못 하면 `부분 구현`으로 남긴다.

```text id="vnbpnu"
FixedLengthLayoutRegistry
FixedLengthGroupSpec
FixedLengthMessageError
FixedLengthMessageException
FixedLengthTypeConverter
```

CMN fixed-length 전체 완료로 기록하려면 parser/formatter/반복부/타입 변환/오류코드/test가 모두 있어야 한다. skeleton만 있으면 `부분 구현`이다.

---

## 10. SQL / Flyway / all_install 기준

DB 변경이 있으면 반드시 아래를 함께 반영한다.

```text id="o6i66n"
specs/sql split SQL
Flyway migration
00_all_install.sql
00_all_install_and_smoke.sql
99_smoke_check.sql
test fixture
```

체크 기준:

```text id="x6y7uw"
한쪽에만 있으면 완료 아님
기존 개발 DB에서만 성공하면 신규 MariaDB full install 완료 아님
비밀번호/민감정보 원문 기록 금지
기존 거래 로그/표준 헤더 E2E 깨지면 완료 아님
```

---

## 11. 실행 검증 요구사항

필수 실행:

```powershell id="zgfy95"
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :acc:test --offline --no-daemon --console=plain
.\gradlew.bat :mbr:test --offline --no-daemon --console=plain
.\gradlew.bat :exs:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat :cmn:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-composite-transaction-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-transaction-group-runtime.ps1

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake

.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

가능하면 실행:

```powershell id="jhak78"
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-standard-header-e2e.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-runtime.ps1 -BuildBeforeRun
```

실행하지 못한 검증은 `미검증`으로 기록한다.

---

## 12. 리포트 / 기능 매트릭스 반영

`CPF_STABILIZATION_REPORT.md`와 `specs/기능_구현_매트릭스.html`에 아래를 최소 반영한다.

```text id="8kw917"
1. 복합 거래 trace 표준 구현 여부
2. transactionSegmentId / parentSegmentId 구현 여부
3. MAIN / SUB / SHARED / EXTERNAL role 구현 여부
4. ACC→MBR→EXS 샘플 구현 여부
5. runtime smoke 실행 여부
6. ADM transaction group 목록 API 구현 여부
7. ADM transaction group 상세/timeline API 구현 여부
8. 전체 수행시간/구간별 수행시간 표시 여부
9. 검색조건/정렬/paging 구현 여부
10. 민감정보 마스킹 확인 여부
11. CMN fixed-length 보강 범위
12. 미실행 검증과 실패 항목
```

문서 정본화, HTML 디자인, 장문 가이드 정리는 이번 범위가 아니다.

---

## 13. 완료 기준

아래가 충족되어야 이번 작업을 완료로 본다.

```text id="9pj32p"
1. transactionGlobalId 기준으로 복합 거래 전체가 묶인다.
2. 구간별 transactionSegmentId가 존재한다.
3. parentSegmentId로 거래 흐름 관계를 알 수 있다.
4. MAIN / SUB 또는 SHARED / EXTERNAL role이 기록된다.
5. moduleCode/sourceModuleCode/targetModuleCode가 기록된다.
6. 전체 수행시간과 구간별 수행시간이 기록된다.
7. ACC→MBR→EXS 복합 거래 샘플 최소 1개가 runtime smoke로 검증된다.
8. ADM 거래 그룹 목록에서 transactionGlobalId 기준 그룹 조회가 가능하다.
9. ADM 상세/timeline에서 구간별 상세 조회가 가능하다.
10. 검색조건/정렬/paging이 있다.
11. 민감 헤더/민감 payload 원문 노출이 없다.
12. 관련 test/smoke/qualityGate/check가 통과한다.
13. 리포트와 기능 매트릭스 상태값이 실제 파일과 일치한다.
```

---

## 14. 완료 불인정 기준

```text id="5et6hh"
1. transactionGlobalId만 같고 segment/role/duration이 없음
2. 로그가 단건 나열만 되고 그룹/timeline 조회가 안 됨
3. ACC→MBR→EXS 샘플이 source만 있고 runtime smoke가 없음
4. ADM 목록에 전체 수행시간/실패 구간이 없음
5. 검색조건 없이 전체 목록만 조회함
6. sort field를 사용자 입력 그대로 SQL에 붙임
7. 민감 헤더/토큰/API key/전문 원문이 노출됨
8. MBR/EXS 테이블 직접 JOIN으로 주제영역 경계 위반
9. standard-header E2E를 깨뜨림
10. 실행하지 않은 검증을 완료로 기록
11. CMN fixed-length skeleton을 전체 전문 엔진 완료로 기록
12. Git commit / push / branch 생성
13. 별도 변경파일 목록 산출물 생성
```

---

## 15. 완료 보고 양식

```text id="j33i0k"
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- CPF_NEW_REQUEST.md 변경 상태/사유:

2. 복합 거래 trace 표준
- transactionGlobalId:
- transactionSegmentId:
- parentSegmentId:
- transactionRole:
- module/source/target:
- direction:
- duration:
- SQL/Flyway/all_install 반영:

3. 복합 거래 샘플
- 패턴 A ACC→MBR→ACC→EXS:
- 패턴 B ACC→MBR→EXS→MBR→ACC:
- runtime smoke:
- 실패/부분 구현:

4. ADM 거래 그룹 조회
- 목록 API:
- 상세 API:
- segments API:
- timeline API:
- headers API:
- external logs API:
- 검색조건:
- 정렬/paging:
- UI 반영:

5. 마스킹/권한/감사
- 민감 헤더 원문 미노출:
- 고객/회원/기관 정보 마스킹:
- 권한 통제:
- 조회 감사:

6. CMN fixed-length 보강
- 반복부:
- 타입 변환:
- 오류 코드:
- Layout registry:
- 테스트:

7. 실행 검증
- :pfw:test:
- :acc:test:
- :mbr:test:
- :exs:test:
- :adm:test:
- :cmn:test:
- 전체 test:
- composite transaction smoke:
- ADM transaction group smoke:
- standard-header-e2e:
- smoke-adm-runtime -BuildBeforeRun:
- check-sql-standard:
- check-feature-evidence:
- check-html-docs:
- check-utf8:
- qualityGate:

8. 상태값
- complex-transaction-trace:
- transaction-segment-log:
- adm-transaction-group-list:
- adm-transaction-timeline:
- standard-header-e2e:
- cmn-fixed-length-engine:
- mariadb-full-install:
- adm-browser-click:
- broker-real-integration:

9. 남은 작업
- EXS 송수신 로그 연결:
- CMN 전문 엔진 완성:
- ADM UI browser click:
- MariaDB full install 회귀:
- Redis/Kafka/MQ:
```

---

## 16. 다음 마일스톤 분기

이번 작업이 완료되면 다음은 아래 중 하나로 간다.

```text id="iq15gk"
1. EXS 전문/REST 송수신과 ADM 외부연계 로그 조회
2. CMN fixed-length 전문 엔진 완성
3. ADM 거래 그룹 UI/browser click 고도화
4. MariaDB full install 회귀 검증
5. Redis/Kafka/MQ broker 실연동 검증
```

분기 기준:

```text id="b821ur"
복합 거래 trace가 완료되면 EXS 송수신 로그 연결로 간다.
ADM 거래 그룹 API만 되고 UI가 약하면 ADM UI/browser click으로 간다.
CMN fixed-length가 많이 진척되면 전문 엔진 완성으로 간다.
환경성 검증은 안 되면 미검증으로 두고 기능 개발을 전진한다.
```
