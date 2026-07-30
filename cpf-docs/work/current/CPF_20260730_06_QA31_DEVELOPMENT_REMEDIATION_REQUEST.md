# CPF QA31 개발·보완·자체검토 요청서

## 1. 기준선과 판정

- Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 독립 검토 기준 SHA: `693cc77bde4c830b78ca1408dec7e34ef84cd11d`
- QA30 주장: Requirement 708, Scenario 218, 통합 Matrix 926, 결함 56개 폐쇄
- QA31 1차 판정: **대량 Source 보완은 인정하나 전체 완료·56개 완전 폐쇄는 승인하지 않음**
- 이유: Source 재개방 결함, 완료 원장과 Source 충돌, exact-SHA Evidence 부재, Java25/3DB/Redis/Multi-instance/Browser 미실행

이 요청서는 Product Source·SQL·Test·Runtime·QA Evidence 검수용이다. README·Guide는 별도 AI가 병행 작업 중이므로 원칙적으로 범위에서 제외한다.

## 2. 개발 전 필수 리뷰

개발자는 코드를 바꾸기 전에 다음을 수행한다.

1. 최신 `master` HEAD, Working Tree, 기준 SHA와의 Diff 확인
2. `CPF_20260730_QA31_DEFECT_REGISTER.csv`의 각 결함을 Source에서 재현
3. 중복 결함은 Root Cause 단위로 묶되 원본 ID를 유지
4. 각 Root Cause를 사용자 흐름 기준 수직 Slice로 재정렬
5. 영향 Module·API·DB·Migration·Runtime Consumer·UI·Test·Topology 작성
6. 실제 결함, Evidence 부족, 환경 미보유, 개선사항을 구분
7. 결과를 `cpf-docs/work/review/CPF_20260730_QA31_PRE_DEVELOPMENT_REVIEW.md`로 기록

사전 리뷰는 짧아도 되지만 다음 내용을 반드시 포함한다.

- 그대로 재현된 결함
- 이미 해결돼 보이나 Evidence가 없는 항목
- 동일 Root Cause로 통합할 항목
- 개발 순서와 회귀 위험
- 이번 개발에서 제외할 Post-GA
- README·Guide 제외 확인

## 3. 개발 순서

1. QA 상태·Evidence Gate
2. Gateway Route/Proxy/Apply
3. Gateway Health/Test/Ledger
4. Gateway Control Security/Approval
5. Batch/File/Shell Runtime
6. ADM 운영 편의성과 기능 완결
7. BZA 영향 회귀
8. EDU Comprehensive Developer Sample
9. Java25·3DB·Redis·Multi-instance·Browser exact-SHA Gate

P0를 닫기 전에 관련 없는 범용 Framework 추상화를 추가하지 않는다.

## 4. Root Cause별 필수 개발

### 4.1 상태·Evidence

- 최신 exact SHA가 아닌 완료 주장을 자동 차단한다.
- 926개 통합 Matrix를 Final Gate가 직접 읽는다.
- 완료 행은 실제 Evidence JSON 경로, SHA, Command, Exit Code를 가진다.
- 구형 Gateway 모델 잔존 시 Gate가 실패한다.
- 실행하지 않은 Browser·DB·Redis·Multi-instance는 완료로 승격하지 않는다.

### 4.2 Gateway

- 실제 Ingress Path와 Target Path를 구분하고 Wildcard/Path Variable/Query/Rewrite를 보존한다.
- ACK되지 않은 신규 Candidate는 외부 Route로 활성화하지 않는다.
- NETWORK/TCP/TLS/APPLICATION/GATEWAY_E2E Connection Test를 실제 분리 실행한다.
- TCP 성공만으로 Application Health `UP`을 만들지 않는다.
- Retry/Failover 각 Attempt를 별도 원장 행으로 기록한다.
- APPROVE/ACTIVATE/BLOCK/RETIRE를 Approval Owner 경계로 통일한다.
- Gateway Control HMAC에 Body Hash와 Audience를 포함한다.
- Multi-instance Replay를 공유 Nonce Store 또는 Instance-bound Signature로 차단한다.
- ADM Remote Gateway 호출에 설정 기반 Timeout과 Typed Unknown/Error를 적용한다.

### 4.3 Batch·File·Shell

- Map/DTO Payload를 Canonical JSON으로 직렬화한다.
- Published Definition Version/Checksum을 Scheduler→Worker→Executor까지 고정한다.
- File Ready→Claim→Execute→Complete/Fail→Release→Restart/Reconcile를 Dispatcher와 원장에 연결한다.
- Remote Scan/Watch/Claim 지원 여부를 Capability로 명확히 하고 미지원이면 완료 처리하지 않는다.
- Shell Exit Code, stdout/stderr, Truncation, Duration, Artifact Hash, Unknown을 상세 원장에 보존한다.
- Secret은 일반 Parameter 파일과 분리해 승인 Provider의 FD/stdin 등 안전 경계로 전달한다.
- Process Tree 종료와 Signature Trust Store를 실제 OS/Runtime 환경에서 검증한다.

### 4.4 ADM 운영 UI

ADM의 기존 메뉴 구조와 역할을 살리면서 실제 운영 효율을 높인다. 단순 카드·버튼 추가가 아니라 Backend·Permission·Runtime 결과와 연결한다.

필수 사용자 결과:

- 운영자가 Gateway, Service Registry, Batch, Log/Trace, Approval, Recovery를 메뉴 이동 최소화로 조회·조치
- 상태 Source와 마지막 갱신 시각을 보고 정보 신뢰도 판단
- 목록에서 Detail, Timeline, 관련 Transaction/Trace/Incident로 이동
- 위험 조치 전에 영향도·대상·현재/예상 상태를 Preview
- Async 작업의 진행·부분 성공·실패·재시도를 실시간 확인
- 미설치 Capability를 빈 목록이나 정상 상태로 위장하지 않음
- Browser Refresh/Deep Link 후에도 검색조건과 선택 Context를 안전하게 복원
- 직접 URL/API 접근도 Permission과 Approval로 차단

상세 요구는 Requirement Matrix의 `QA31-ADM-*`를 따른다.

### 4.5 EDU 개발자 Sample

`cpf-reference`/REF는 단순 Hello World가 아니라 CPF 기능을 학습·검증하는 실행 가능한 Developer Reference다.

- CPF Public API와 SPI를 실제 사용
- 성공·실패·복구 예제를 함께 제공
- Local/Remote, DB-less/DB, Async/Sync 배치 비교
- Sample 전용 Profile/Seed로 기본 운영 설치와 분리
- 실제 Secret·외부 Credential·개인정보를 포함하지 않음
- Sample API, Runtime Consumer, Test와 Evidence가 일치
- 기능별 Sample이 서로 독립 실행 가능하고 전체 통합 Flow도 제공
- 기존 README/Guide는 수정하지 않고 Source·Test·OpenAPI Example·Sample Metadata에 집중

상세 요구는 Requirement Matrix의 `QA31-EDU-*`를 따른다.

## 5. 자체검토

각 Requirement는 다음을 확인한다.

- 호출 주체와 실제 Consumer
- Local/Remote 및 설치/미설치
- 성공·Validation·Permission·Conflict·Timeout·Unknown
- Retry·Recovery·Reconcile
- DB State와 Audit/Ledger
- UI 표시와 직접 API 결과
- 회귀 Test
- exact-SHA Evidence

다음 중 하나라도 있으면 완료 금지다.

- Test에서만 사용하는 구현
- Interface만 있고 Product Consumer 없음
- Candidate/Stub/Temporary 구현이 운영 Profile에서 활성
- 신규·구형 모델 병존
- Table만 있고 Producer/Consumer 없음
- 오류를 빈 목록·SUCCESS·UNKNOWN 하나로 평탄화
- 최신 SHA가 아닌 Evidence
- 실행하지 않은 환경을 PASS 처리
- Request Matrix를 수정해 Acceptance를 완화

## 6. 개발 완료 산출물

다음 파일을 별도 생성한다.

- `cpf-docs/work/review/CPF_20260730_QA31_PRE_DEVELOPMENT_REVIEW.md`
- `cpf-docs/work/review/CPF_20260730_QA31_DEVELOPMENT_COMPLETION_REPORT.md`
- `cpf-docs/quality/CPF_20260730_QA31_RESULT_MATRIX.csv`
- `cpf-docs/quality/CPF_20260730_QA31_UNRESOLVED_REGISTER.csv`
- `cpf-docs/evidence/current/*.json`
- `cpf-docs/work/handover/CPF_20260730_QA31_DEVELOPMENT_HANDOVER.md`
- `cpf-docs/work/current/CPF_20260730_QA31_CODEX_REVIEW_READY.md`

원본 요청 문서와 원본 Matrix는 변경하지 않는다.

## 7. 완료 ZIP

개발 종료 후 프로젝트 Root를 기준으로 ZIP을 생성해 사용자에게 다운로드 링크를 제공한다.

ZIP 포함:

- 실제 변경 Source/SQL/Test/Frontend
- Completion Report, Result Matrix, Unresolved Register
- current Evidence
- 변경 파일 SHA-256 Manifest
- Base/Head/Working Tree 정보

ZIP 제외:

- `.git/**`
- `build/**`, `node_modules/**`, IDE Cache
- Password, Token, Secret, Private Key, 실제 Credential
- 무관한 README·Guide 병행 변경
- 사용자가 요청하지 않은 Commit/Branch/Tag/PR Metadata

개발 AI는 ZIP을 제공했다고 완료 선언하지 않는다. 사용자가 ZIP을 적용하고 Push한 뒤 후속 QA가 새 exact SHA에서 다시 검증한다.

## 8. Commit·Push 정책

사용자 명시 승인 전 Commit·Push·Branch·Tag·PR을 생성하지 않는다. 사용자가 직접 Push하면 후속 세션은 최신 master SHA를 다시 확인한다.
