# CPF ChatGPT Session Handover — 20260729_03

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Stage 2 작업 기준 SHA: `9e5d1676a9ccba55fedf4dfb633a9e710f487a02`
- 1차 Overlay 시작 기준 SHA: `84b4672e9e8b61ea067bb52b85b838a0b95e44b1`
- ChatGPT는 Commit·Push·Branch·Tag를 생성하지 않았다.
- 사용자가 Root Overlay를 적용하고 직접 Push한다.

## 2. 범위 정정

P0 Ledger 18건과 ADM Runtime Control 14개 Capability는 우선순위이며 전체 범위가 아니다.

최종 대상:

- 기존 요구사항 1,749건
- 기존 실행 시나리오 369건
- 기존 합계 2,118건
- 신규 QA 병합 후 요구사항 2,328건
- 신규 QA 병합 후 시나리오 387건
- 병합 총계 2,715건

1차 16개는 요구사항 수가 아니라 구현 묶음이며 최신 master 회귀 검수 대상이다.

## 3. Stage 2 실제 변경

1. Root `build.gradle`의 `targetProject` Closure Scope 오류 수정
2. Notification Portable SQL Gate의 Service/Outbox Owner 오판 수정
3. 만료 PROCESSING Lease를 UNKNOWN_RESULT로 격리
4. Retry·Cancel expectedVersion CAS와 HTTP 409
5. Notification 운영 조회에 operationId·requestHash·retry·lease·version·error 노출
6. Raw JSON형 Notification 화면을 운영 Table·조치 동선으로 변경
7. Provider Attempt 불변 이력 `cpf_notification_delivery_attempt`
8. MariaDB·PostgreSQL·Oracle V68/R68
9. Attempt API와 ADM Timeline
10. Provider 메시지 민감정보 Redaction
11. 기존 2,118건 + 신규 QA 병합 원장 2,715건
12. Architecture·Generated Domain·Menu/UI·Garbage Matrix Exporter
13. Integrated Architecture/UI/Hygiene Gate
14. CI와 Final Closure에 병합 Gate 연결
15. Notification 인증·expectedVersion·HTTP 409 회귀 Test
16. Lease 복구·정상 완료의 Parent → Attempt 잠금 순서 통일
17. 병합 원장 2,715개 ID 중복 및 기존 2,118개 ID 보존 Gate
18. `cpf-batch` 중첩 Runtime Module까지 Matrix 탐색 확대
19. ADM/BZA Raw JSON `<pre>` 자동 차단
20. Codex 전체 검수·보완 개발 요청서

## 4. 반드시 재실행할 사용자 실패

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-notification-portable-sql.ps1
.\gradlew.bat :cpf-admin:test --tests "com.cpf.admin.opr.service.AdmNotificationOutboxServiceTest" --no-daemon --stacktrace
```

기존 실패:

- `Portable notification marker missing: new String[] {"delivery_id"}`
- `Could not get unknown property 'targetProject' for project ':cpf-common'`

## 5. 적용 후 우선 검증

```powershell
git diff --check
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-integrated-architecture-ui-hygiene.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1
.\gradlew.bat help --no-daemon --stacktrace
```

## 6. 미검증

이번 ChatGPT 환경에서는 다음을 실행하지 못했다.

- pwsh Gate
- Java 25·Gradle 9.1 전체 Build
- Oracle·PostgreSQL·MariaDB
- ADM/BZA Browser
- Multi-process·Multi-instance
- 실제 Email·SMS·Broker·SFTP Provider

따라서 Stage 2는 Source·SQL·Test·Gate 보완 산출물이며 전체 2,715건 완료 Evidence가 아니다. 실제 수행한 정적 검증은 `cpf-tools/verification/20260729_02/STATIC_VALIDATION_REPORT.json`에 기록했다.

## 7. 다음 세션/Codex 시작점

`cpf-docs/work/requests/CPF_CODEX_FULL_QA_CLOSURE_REQUEST_20260729.md`를 그대로 사용한다.

Codex는 이번 세션 변경만 보지 않고 직전 Codex 검수 이후 누적 전체와 병합 원장 2,715건을 최신 Push SHA에서 검수·보완 개발한다.

완료 이외 상태가 남으면 전체 완료로 보고하지 않는다.

## 신규 QA 계수 보정

- 신규 QA 원문 Bullet 590개 중 Section 15 판정 분류 4개와 Section 18 허용 상태 6개는 독립 Requirement가 아니므로 `EXCLUDED_METADATA`로 보존하고 병합 원장에서는 제외했다.
- 기존 Requirement와 확인된 중복 1개는 `MERGED` 처리했다.
- 따라서 신규 고유 Requirement는 579건, 신규 Scenario는 18건이며 최종 병합 원장은 Requirement 2,328건 + Scenario 387건 = 2,715건이다.
- 중복 후보 5건은 임의 삭제하지 않고 Codex가 Root Cause·실제 Consumer 기준으로 재확정한다.

## 8. 전달 Artifact

- Root Overlay: `CPF_20260729_FULL_QA_STAGE2_ROOT_OVERLAY.zip`
- 구성: Repository Root 상대경로, 별도 상위 Directory 없음
- 적용 안내: `cpf-tools/verification/20260729_02/README_APPLY.md`
- Overlay 파일 목록: `cpf-tools/verification/20260729_02/OVERLAY_CONTENTS.txt`
- 개별 파일 Hash: `cpf-tools/verification/20260729_02/OVERLAY_FILES.sha256`
- ZIP SHA-256은 ChatGPT 전달 메시지와 별도 `.sha256` 파일에서 확인한다.

현재 병합 원장은 거짓 완료 방지를 위해 2,715건 전부 `미검증` 상태다. Overlay 적용 직후 Final Closure를 먼저 성공시키려 하지 말고, 정적 Gate와 Build 구성 실패를 우선 수리한 뒤 Codex 전수 검수·보완 개발과 Evidence 갱신을 완료하고 Final Closure를 실행한다.
