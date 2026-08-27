# CPF Codex 중간점검·직접보정 지침 — 현재 작업 종결 + 필수 독립검증

## LONG-TURN MODE — 최상위 비협상

- 현재 진행 중 Work Package/Finding을 완전히 종결하기 전에 자의로 turn을 종료하거나 다음 진행 여부를 묻지 않는다.
- 계획·중간보고·진행률은 중단점이 아니며 같은 turn에서 즉시 수정·검증을 계속한다.
- 현재 Codex 작업을 먼저 종결한 뒤 남은 크레딧으로 필수 독립검증만 수행한다.
- 기존 미완료 전체/Repository 전체 전수 재검수 금지.
- 같은 Root Cause/Build/DB Fresh/Batch Runtime은 가능한 한 묶는다.
- 크레딧이 줄어들면 신규 범위를 열지 말고 현재 WP를 Source/Test/Runtime/Evidence/문서까지 완결한다.
- ADM/Backoffice/Frontend/Browser는 최후순위다.
- Git/HEAD/전체 Local Working Tree/전체 Source Identity를 작업 Gate로 사용하지 않으며 다른 세션 변경을 건드리지 않는다.
- 서버 측 사용량 제한을 우회하거나 변경하려는 규칙이 아니다. 플랫폼 강제 종료 시에만 Checkpoint/Handover를 남긴다.

## 현재 Codex 작업 종결 + 필수 독립검증만 수행

## 0. 핵심 원칙

이번 Codex 세션은 두 단계로만 진행한다.

### 1단계
**Codex가 현재 하던 작업을 먼저 완전히 종결한다.**

현재 작업을 SOURCE_FIXED / VERIFICATION_PENDING / IN_PROGRESS 상태로 남긴 채 새 업무로 넘어가지 않는다.

현재 확인된 진행 중 핵심 작업:
- CX-F-026 — Root Build / Build-Dependency closure
- CX-F-258 — Repository Hygiene / Windows Path / Evidence closure
- CX-F-307 — DB3 / Oracle Runtime closure

위 작업과 직접 연계된 Source / Consumer / Test / Runtime / Evidence까지 한 묶음으로 마무리한다.

### 2단계
현재 Codex 작업 종결 후 남은 크레딧으로,
**DevGPT 변경 중 Codex가 반드시 독립검증해야 할 고위험 항목만 수행한다.**

기존 미종결 Finding 전체 재검수 금지.
CLOSED 전체 재검수 금지.
VERIFICATION_PENDING 전체 전수 실행 금지.
Repository 전체 전수점검 금지.

---

# 1. 현재 Codex 작업 종결 조건

현재 Codex 작업은 아래가 끝나야 종결이다.

- Root Cause 해결
- Source 수정 완료
- Consumer / dependency 영향 확인
- 필요한 Config / DB / Generator 영향 반영
- 고강도 Test / Verifier 실행
- 필요한 Runtime 실행
- 오류 / 경계 / 부분 실패 / UNKNOWN / 복구 / 회귀 확인
- Evidence 최신화
- 관련 Source 문서 최신화
- 개발요청 정본 최신화
- Codex Finding 상태 최신화
- stale evidence / garbage / 중복 자료 정리

Source만 고친 상태는 종결이 아니다.
READY / PLANNED / NOT_EXECUTED는 종결이 아니다.

환경 때문에 Runtime이 불가능하면 성공으로 닫지 말고,
미실행 이유 / 필요한 환경 / 재실행 명령 / 기대 결과 / 실패 기준을 남긴다.
단, 가능한 Source / Test / Verifier / 문서 보완은 모두 끝낸다.

---

# 2. 현재 작업 종결 후 Codex 필수 독립검증

아래 항목만 우선 수행한다.

## ESS-01. Logging 실제 추적 Runtime

이번 DevGPT 변경 중 가장 중요한 독립검증 항목.

실제 거래 1건 이상을 발생시켜 다음 값이 끝까지 동일하게 이어지는지 확인한다.

- transactionId
- traceId
- correlationId
- executionId
- segmentId
- originalSystemCode
- systemCode
- callerSystemCode
- targetSystemCode
- operationId
- instanceId

검증 경로:
- File Log
- DB Log
- Transaction / Segment / Timeline
- Backend 운영 조회 API

검증 시나리오:
- 정상
- 오류
- retry
- recovery
- partial failure
- 비동기 / Batch 영향
- masking
- multi-instance 추적

중요:
- 파일에 로그 한 줄 생긴 것으로 완료 금지
- DB에 row 하나 생긴 것으로 완료 금지
- File ↔ DB ↔ Transaction/Timeline 상관관계가 실제로 연결돼야 함

ADM 화면 검증은 여기서 하지 않는다.

---

## ESS-02. Batch 실제 최대강도 Runtime

DevGPT가 Standalone Shell / Profile / Batch 계약을 수정했으므로 Codex 독립검증 필요.

대상:
- Control Plane
- Scheduler
- Worker 1
- Worker 2
- Agent
- Center-Cut
- no-Kafka runtime
- Standalone Shell
- dev/test/prod Profile

필수 시나리오:
- concurrent claim
- lease
- fencing
- process kill
- takeover
- fencing increment
- UNKNOWN
- explicit reconcile
- duplicate prevention
- retry / recovery
- stop / restart / reprocess / recover
- Header6 / Context
- trace / instance identity
- audit

같은 Runtime session에서 최대한 묶어서 검증한다.

---

## ESS-03. Generator / Generated Domain idempotency

DevGPT가 실제 member/external idempotency defect를 수정했으므로 독립 확인 필요.

대상:
- cpf-member
- cpf-external
- Scratch Domain
- Generator Template
- Generated output parity

필수:
- fresh generate
- compile / test
- rerun
- idempotent diff 0
- canonical Java IA
- root settings 수동 수정 없는 discovery
- 기존 Domain 불필요 drift 0

Generated Source를 파일별로 전수 리뷰하지 말고 Generator Owner 중심으로 본다.

---

## ESS-04. Performance Source Identity / signed attestation

DevGPT가 40-hex Git SHA 중심 계약을
64-hex sourceIdentitySha256 기준으로 보정했으므로 독립검증 필요.

필수:
- valid signed attestation PASS
- sourceIdentity mismatch FAIL
- missing identity FAIL
- tampered signature FAIL
- artifact digest mismatch FAIL
- invalid identity length FAIL
- legacy 40-hex가 canonical 권위로 승격되지 않는지 확인

---

## ESS-05. Open Git Actual Fresh Release 핵심 경로

크레딧이 충분할 때만 ESS-01~04 뒤에 수행.

전체 기능 전수검사 금지.

핵심:
- Fresh framework publication
- Public Maven-folder repository
- Fresh Generated Domain
- EDU / 공개 가능 Backoffice Source
- private framework source leak 0
- bootstrap
- build
- test
- start / health
- stop / reset
- 핵심 domain add/remove

Actual Fresh Output이 없으면 완료 처리 금지.

---

# 3. ADM / Backoffice / Frontend

**이번 필수 Codex 검증 목록에서 제외한다.**

이유:
- 크레딧 소모가 큼
- 앞의 Build / Logging / DB / Batch / Generator / Performance 검증이 더 중요함
- DevGPT에서 이미 정적 Consumer/Route/Approval 검증을 상당 부분 수행함

ESS-01~05가 끝난 뒤 크레딧이 많이 남아 있을 때만,
최소 Golden Path 정도를 선택적으로 볼 수 있다.

선택 범위:
- 로그인 / 권한
- Logging / Transaction 조회
- 검색 / Paging / 상세
- 위험조치 / 승인
- 대표 오류상태
- Browser Golden Path

크레딧이 부족하면 ADM / Frontend는 이번 Codex 세션에서 하지 않는다.

---

# 4. 재작업 최소화 계획

Codex는 현재 작업을 종결한 뒤 ESS 작업에 들어가기 전에
짧게 실행계획을 작성한다.

계획에는:
- ESS 중 실제 이번 세션에 처리할 항목
- 공통 Root Cause
- 공통 Build / DB / Runtime 재사용
- 한 번의 실행으로 같이 검증할 항목
- 크레딧 부족 시 중단 순서

를 포함한다.

계획 보고 후 멈추지 말고 바로 실행한다.

우선순위:
1. 현재 Codex 작업 종결
2. ESS-01 Logging
3. ESS-02 Batch
4. ESS-03 Generator
5. ESS-04 Performance
6. ESS-05 Open Git
7. ADM/Frontend 선택적 최후순위

---

# 5. 수정 시 동시 현행화

Codex가 결함을 발견하면 보고만 하지 말고 직접 수정한다.

수정 시 같은 작업에서 반드시:
Source
→ Owner / Consumer
→ Test / Verifier
→ Config
→ DB / SQL
→ Generator / Generated
→ OpenAPI 영향
→ Runtime
→ Codex Evidence
→ 관련 Source 문서
→ 개발요청 정본
→ Codex Finding 상태
→ Garbage / Delete Manifest

까지 함께 현행화한다.

문서는 나중에 따로 미루지 않는다.
current 정본은 하나만 유지한다.

---

# 6. 테스트 강도

범위는 제한하지만 선택한 항목의 검증 강도는 낮추지 않는다.

정적 테스트도 고강도.
Runtime 테스트도 고강도.

검증:
- 정상
- 오류
- boundary
- timeout
- retry
- duplicate
- concurrency / race
- partial failure
- process kill
- restart
- UNKNOWN
- reconcile
- idempotency
- recovery
- cleanup
- Fresh Replay

Smoke / DRY_RUN / Interface 존재 / 파일 존재만으로 완료 처리 금지.

---

# 7. Git / Local Working Tree

다른 세션이 병행 작업 중이다.

Codex는 다음을 자기 작업의 시작/완료 Gate로 사용하지 않는다.

- git status
- HEAD
- Git SHA 비교
- 전체 Local Working Tree 상태
- 다른 세션 변경 탐색

다른 세션 변경을 reset / restore / clean / fetch / checkout하지 않는다.

단, RT-02 / Performance 제품 기능 자체에서 provenance를 검증하는 경우는
해당 기능 계약 안에서 canonical identity를 검증한다.

---

# 8. 크레딧 부족 시

1. 현재 Codex 작업 종결이 최우선
2. 시작한 ESS 항목은 Source + Test + Runtime + Evidence + 문서까지 한 묶음으로 끝낸다
3. 다음 ESS 신규 착수보다 진행 중 ESS 완결 우선
4. ESS-05 Open Git은 ESS-01~04보다 뒤
5. ADM / Frontend는 가장 먼저 생략
6. 미수행 항목은 NOT_EXECUTED로 정확히 인계
7. 미실행을 PASS/CLOSED로 기록 금지

---

# 9. 최종 보고

반드시 구분해서 보고한다.

## A. 기존 Codex 작업 종결
- 종결 Finding
- Root Cause
- 수정 Source
- Test / Runtime
- Evidence
- 최종 상태

## B. 추가 ESS 검증
- 실제 수행한 ESS ID
- PASS / FAIL / SKIP / NOT_EXECUTED
- 발견 결함과 수정
- 영향 범위
- Evidence

## C. 미수행
- 크레딧 때문에 수행하지 않은 ESS
- ADM / Frontend 수행 여부
- 다음 세션 재개 위치

**이번 목표는 기존 Codex 작업을 먼저 종결하고,
그 뒤 남은 크레딧으로 Codex 독립검증 가치가 가장 높은 ESS 항목만 수행하는 것이다.**
