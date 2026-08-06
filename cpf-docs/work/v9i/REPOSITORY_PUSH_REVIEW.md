# Repository Push 점검 결과

## 판정

현재 `master` HEAD `1b35d84801e256e3e6d7e4482918817ec82865dd`는 개발GPT 통합 결과를 Push한 Commit으로 확인했다.
기준 `2a013663090d4e430a15983ad7269f8e86c5ef58`보다 1 Commit 앞선 상태다.

**현재 판정: QA 검수 시작 가능 / CPF 최종 완료·Release 판정 불가**

## 확인된 양호 사항

- 짧은 정본 경로 `cpf-docs/work/v9i`가 존재한다.
- 상태 Dataset은 47,745 unique exact ID, 중복 Primary 0으로 기록돼 있다.
- Provenance·Evidence·Request 연결의 고아 및 Hash 불일치가 0으로 기록돼 있다.
- Target Runtime 미실행 항목을 Runtime PASS로 기록하지 않았다.
- QA 상태를 임의로 통과 처리하지 않았다.
- 분할 원장 구조는 개별 파일 크기와 행 수를 제한한다.

## 수정이 필요한 관리 문서

### R-01 — 중복 Workspace

Commit에 다음 두 Root가 동시에 들어 있다.

- 긴 구형 Root: `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/CPF-V9-FINAL-INTEGRATION-CLOSURE-20260806/REV-001/sessions/DEVGPT-V9-INTEGRATION`
- 짧은 정본 Root: `cpf-docs/work/v9i`

긴 Root는 동일 통합 Dataset의 중복본이며 Windows 경로 문제를 재발시킬 수 있다.
짧은 Root를 정본으로 유지하고 긴 Root만 안전 검산 후 삭제한다.

### R-02 — 진행률 산식

`results/PROGRESS_STATUS.csv`의 Integration Request는 전체 32, 완료 30,
차단 2인데 완료율이 100%로 기록됐다. DevGPT Closure 기준 완료율은 **93.75%**다.
“처리 상태 분류 완료율”을 별도로 100%로 표현할 수 있으나 Closure 완료율과 혼용하면 안 된다.

### R-03 — 물리 정본 표시

`results/REQUIREMENT_STATUS.csv`는 헤더만 있다.
실제 47,745행은 Index와 4개 Part에 있다. 따라서 “CSV 한 파일”이 아니라
`REQUIREMENT_STATUS_INDEX.csv + PART_001~004`가 물리 정본이라는 점을 명시해야 한다.

### R-04 — Push 이후 SHA

기존 문서는 Overlay 기준 SHA `2a013663090d4e430a15983ad7269f8e86c5ef58`만 기록한다.
현재 QA 기준 Commit `1b35d84801e256e3e6d7e4482918817ec82865dd`를 별도 `reviewed_commit_sha`로 기록해야 한다.

## Product 검수 재요청

### P1 — Spring Runtime wiring 미입증

`AdmIntegrationClosureService`에는 `@Service`가 없고, 신규
`CpfDataQualityOperations`, `CpfTimeOperations`, `CpfWebhookOperations`
구현도 Component/AutoConfiguration Bean 등록이 보이지 않는다.

기존 Evidence의 ADM 검증 명령은 “framework stubs를 사용한 javac compile”이다.
실제 Spring ApplicationContext 시작으로 Controller 의존성 주입 성공을 검증해야 한다.

### P1 — 승인 Boolean 신뢰

데이터 품질 정정 API는 요청의 `approved` Boolean을 그대로 Service에 전달한다.
위험 조치 승인은 클라이언트 Boolean이 아니라 서버에서 검증된 권한·승인 ID·상태로 판정해야 한다.

### P1 — Target Runtime 미실행

아래는 여전히 직접 실행 필요 상태다.

- Java 25 / Gradle 9.1 전체 Compile·Test·Publication
- Oracle·PostgreSQL·MariaDB 실제 Install·Migration·Verify·Rollback
- 실제 Browser·Playwright Release Matrix
- Broker·다중 Process·분리 WAS·Process Kill

## CI

검토 Commit에 연결된 GitHub combined status와 workflow run이 조회되지 않았다.
따라서 GitHub CI 성공을 근거로 사용할 수 없다.
