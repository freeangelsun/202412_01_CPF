# CPF QA39 Developer Implementation Report / Self Review Template

개발 GPT는 이 Template을 복사해 아래 두 파일을 실제 내용으로 작성한다.

- `CPF_QA39_DEVELOPER_IMPLEMENTATION_REPORT.md`
- `CPF_QA39_DEVELOPER_SELF_REVIEW.md`

## A. Implementation Report 필수 항목

### 1. Baseline
- Repository/Branch
- 시작 exact SHA / 종료 exact SHA
- Working Tree 상태
- 작업 시간과 환경(JDK/Gradle/Node/DB/Provider)

### 2. Requirement 결과
Requirement ID별로 `development_status`와 `verification_status`를 분리한다. 변경 파일, 핵심 Class/API/SQL/Test/Consumer, Acceptance, Evidence를 한 행에 연결한다.

### 3. Architecture 결정
- Owner Module/Package
- Public API / Extension SPI / Internal Adapter
- 의존성 방향과 Consumer
- 6 Profile/7 Group 및 Provider resolved lock
- 편의성·확장성 가치가 OSS 직접 사용보다 나은 근거

### 4. 변경 Manifest
Root-relative path, 변경 목적, Requirement, 영향도, 신규/수정/삭제/이동, 주요 line range를 기록한다.

### 5. 삭제 결과
- exact 삭제 path
- 삭제 전 Consumer/대체 위치
- Core API·Config·Test·BOM·Catalog·Generator·문서·Evidence 정리
- 잔여참조 검색 명령과 0건 결과
- 빈 폴더·Dead Code·Stale Evidence 결과
- 실행한 PowerShell 한 줄 명령과 출력

### 6. 실행 명령과 실제 결과
명령마다 Profile, 환경, 시작/종료 시각, Exit Code, 실제 결과, log/evidence 위치를 기록한다. 실행하지 않은 검증은 `미검증`으로 적는다.

### 7. 실패·미완료·재확인
숨기지 않고 원인, 영향, 재현 명령, 다음 조치를 기록한다.

### 8. Evidence
exact SHA, 파일 Hash, 민감정보 제거, 현재 Commit 유효성을 기록한다.

## B. Developer Self Review 필수 항목

개발 보고를 그대로 반복하지 않는다. 구현 종료 후 독립적으로 다음을 다시 확인한다.

- QA 최종요건과 자체요건 충돌 여부 및 우선순위 준수
- 예상 변경과 실제 변경의 차이
- 유지/삭제/내부화 판정 준수
- Public API의 OSS 타입 누출과 얇은 Wrapper 잔존
- Provider 교체와 고객 SPI 확장성
- Source/API/SQL/Test/Config/Frontend/Script/문서/Evidence 정합성
- Consumer 없는 추상화·Dead Code·빈 폴더·Stale Evidence
- 회귀 위험과 보호한 기존 성공 기능
- 직접 실행하지 않은 검증
- QA가 확인할 핵심 파일·라인·명령·기대 결과

## C. Codex 검수 최적화 산출물

`REVIEW_INDEX.md`, `CHANGE_MANIFEST.csv`, `REQUIREMENT_STATUS.csv`, `TEST_AND_EVIDENCE.md`, `OPEN_ISSUES.md`, `PACKAGE_MANIFEST.json`을 Developer Report/Self Review와 일치시킨다. QA가 같은 전체 탐색을 반복하지 않도록 범위·제외·근거·남은 의문을 명확히 한다.
