# CPF Codex 최종 검수 요청 문서 작성 기준

## 1. 목적

이 문서는 ChatGPT가 CPF 개발을 완료한 뒤 Codex가 적은 크레딧으로도 핵심 위험을 효과적으로 검수할 수 있도록, 검수 범위·우선순위·변경 영향도·실행 명령·완료 금지 조건을 구조화하기 위한 정본 템플릿이다.

Codex의 역할은 원칙적으로 다음으로 제한한다.

- 최신 `master` Exact-SHA 독립 검수
- ChatGPT 개발 결과의 회귀·누락·False Green 탐지
- Source/API/SQL/Test/Evidence 정합성 확인
- 실패 항목과 재현 명령 보고

Codex가 대규모 신규 개발을 다시 수행하도록 요청하지 않는다. 검수 중 발견된 결함은 Root Cause, 영향 범위, 재현 절차를 명확히 반환하게 하고, 실제 보완개발은 ChatGPT가 담당한다.

---

## 2. 문서 생성 시점

다음 시점마다 이 문서를 누적 갱신한다.

1. QA 신규 요구 수신 후 기존 요구와 통합할 때
2. Architecture 또는 Canonical DB 정책 변경 시
3. Public API/SPI 또는 Generator 계약 변경 시
4. ADM/BZA/BAT/Redis/File/Runtime Control 등 고위험 기능 변경 시
5. 전체 개발 완료 직전
6. 최종 Commit·Push 후 Exact-SHA 검수 요청 직전

---

## 3. 검수 그룹과 우선순위

### P0-A. 이전 Codex 미완료 검수 복구

이전 Codex 작업이 크레딧 제한으로 중단된 지점과 그 이후 변경 영향을 먼저 검수한다.

- Runtime Query `PORTABLE_ONLY` Gate
- UTF-8/BOM 정책 Gate
- QA Ledger Closure
- Final Completion Gate
- Current/Handover/Evidence Exact-SHA
- 중단 당시 WIP 파일의 최종 상태
- 중단 후 ChatGPT가 수정한 동일 영역의 회귀

검수 방법:

- 이전 중단 Checkpoint와 최신 SHA Diff 비교
- 중단 당시 미완료 명령 재실행
- 임시 우회·부분 구현·False Green 제거 확인

### P0-B. 신규 개발 직접 영향도 검수

신규 또는 대규모 변경 파일을 Owner Module별로 묶어 검수한다.

각 변경 묶음마다 다음을 기록한다.

- Requirement ID
- 변경 목적
- Owner Module
- Public API/SPI/Internal 구분
- 실제 Consumer
- 변경 Source/SQL/Frontend/Script/Test
- 정상·오류·경계·부분 실패
- Security/Audit/Masking
- DB/Generator 영향
- 회귀 보호 항목
- 실행 Evidence

### P0-C. Architecture·Ownership·Boundary

- `cpf-core`, `cpf-common`, `cpf-admin`, `cpf-biz-admin`, `cpf-batch` Ownership
- Public API/SPI/Internal 직접 Import
- 역방향·순환 의존
- 동일 JVM/Remote 양쪽 계약
- Consumer 없는 Interface
- Dead Code·중복 구현
- 고정 Domain 가정 제거

### P0-D. DB·Migration·Generator

- Oracle/PostgreSQL/MariaDB 3종 Parity
- Canonical Source와 파생 Artifact Drift
- Install/Upgrade/Rollback/Re-apply
- Historical Migration 불변성
- Runtime Query Pack
- Schema Manifest·Checksum
- Generated Domain Golden Parity
- 임의 Domain Create/Verify/Build/Remove
- 사용자 수정 영역 보호

### P0-E. Runtime·장애·다중 인스턴스

- Service Call Local/Remote
- Timeout/Retry/Circuit/Failover
- Result Unknown·Recovery·Compensation
- Redis 장애·복구·Invalidation·Lock/Fencing
- BAT Multi-worker·Center-Cut·Lease/Fencing
- Runtime Control Delivery·Partial/Unknown·Rollback/Reconcile
- File Job 중단복구·멱등성

### P0-F. 보안·권한·운영 UI

- ADM/BZA Role별 READ/WRITE/DELETE
- 위험 Action별 권한·사유·승인·감사
- 버튼 미노출과 Backend 403
- Raw JSON 기본 UI 잔존 여부
- Typed Form·상태·오류·Partial Failure 표현
- Menu/Organization Recursive Tree
- 민감정보 로그·Evidence·화면 노출

### P1-A. 전체 Build·Test·Gate

- Root Clean Build/Test/Assemble
- ADM/BZA Frontend Build·Lint·Test·Typecheck
- Architecture/Dependency/Security/SQL/Generator/Hygiene Gate
- Final Completion Gate
- Test 삭제·Assertion 약화·Gate 우회 여부

### P1-B. 문서·Evidence·추적성

- Current Request
- Final Development Report
- Final Handover
- Continuity State
- Decision Log
- Final Target 162
- Enterprise Requirement 816
- QA Scenario 387
- Master Ledger 2,715
- Evidence Index

각 Evidence는 다음을 포함해야 한다.

- Exact SHA
- 명령
- 환경/Profile
- 시작·종료 시각
- Expected/Actual
- Exit Code
- 관련 Requirement/Scenario
- 민감정보 제거 여부

### P2. Release·Hygiene

- Repository Root 최소 구조
- build/tmp/log/zip/patch/bak/empty directory 제거
- Offline Bundle
- Fresh Install/Upgrade/Rollback rehearsal
- Backup/Restore/DR
- README·Guide·최종 문서 일치

---

## 4. 변경 영향도 표준 분류

각 개발 항목은 다음 영향도 중 하나 이상으로 분류한다.

- `ARCH`: Module·Package·Dependency·Public Boundary
- `API`: Controller·DTO·Header·Error·OpenAPI
- `RUNTIME`: Transaction·Async·Retry·Recovery·Multi-instance
- `SEC`: Authentication·Authorization·Audit·Masking
- `DB`: Schema·Migration·Seed·Runtime Query·Vendor
- `GEN`: Generator·Golden Domain·Generated Artifact
- `UI`: ADM/BZA Route·State·Component·Permission UX
- `BAT`: Batch·Scheduler·Worker·Center-Cut
- `FILE`: File·Attachment·CSV·XLSX·Streaming
- `OPS`: Runtime Control·Health·Metrics·Operator Control
- `DOC`: Guide·EDU·JavaDoc·Evidence·Handover

Codex 검수 요청서에서는 변경 파일을 단순 나열하지 않고 영향도 코드별로 묶는다.

---

## 5. 검수 순서 최적화

Codex 크레딧을 절약하기 위해 다음 순서로 진행한다.

1. 기준 SHA·Working Tree·문서 SHA 확인
2. P0 Static Gate 실행
3. 변경 영향도별 Targeted Test
4. DB/Generator Canonical 검증
5. Frontend Build·권한·Raw JSON 정적 검수
6. Runtime 가능 범위 검증
7. 전체 Build·Final Gate
8. Evidence·Matrix 정합 확인
9. 실패 항목만 Source 근거와 재현 명령으로 보고

같은 명령을 반복 실행하지 않도록 각 단계의 성공 결과와 로그 경로를 문서에 미리 적는다.

---

## 6. Codex 검수 요청서 필수 구성

최종 Codex 문서는 다음 순서로 작성한다.

1. 기준 Repository·Branch·Exact SHA
2. Codex 역할과 수정 금지 또는 허용 범위
3. 이전 미완료 검수 항목
4. 신규 개발 변경 요약
5. 변경 영향도 Matrix
6. P0/P1/P2 검수 그룹
7. 실행할 명령과 예상 결과
8. 반드시 확인할 Source 경로
9. 기존 성공 기능 회귀 보호 목록
10. Evidence 위치
11. 완료 금지 조건
12. 최종 응답 형식

---

## 7. 완료 금지 조건

다음 중 하나라도 있으면 Codex는 완료 판정을 해서는 안 된다.

- 이전 미완료 검수 항목 누락
- 최신 SHA가 아닌 Evidence
- Current/Handover/Matrix SHA 불일치
- Build/Test/Gate 실패
- `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요` 잔존
- Runtime 기능을 Source 존재만으로 완료 처리
- 하나의 DB Vendor 결과로 3종 완료 주장
- Static Test로 Browser/Redis/Multi-instance 완료 주장
- Test 삭제·Assertion 약화·예외 삼킴
- Raw JSON·임시 UI·Mock 전용 제품 구현
- Consumer 없는 Interface
- Stale Evidence·빈 로그·실행 명령 없는 PASS
- Working Tree Dirty
- Local HEAD와 `origin/master` 불일치

---

## 8. 최종 운영 원칙

- ChatGPT가 개발·문서·Evidence를 완료한다.
- Codex는 독립 검수와 실패 탐지에 집중한다.
- Codex 요청서는 길이를 줄이되 검수 범위는 줄이지 않는다.
- 상세 Requirement와 Evidence는 Repository 문서 경로로 참조한다.
- 검수 명령은 중복 없이 우선순위 순으로 제공한다.
- 실패 시 Codex는 직접 광범위한 재개발보다 Root Cause와 재현 정보를 반환한다.
