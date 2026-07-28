# CPF ChatGPT Session Handover — 20260729_01

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 기준 SHA: `9e5d1676a9ccba55fedf4dfb633a9e710f487a02`
- 사용자 1차 QA Closure Overlay Push 확인
- ChatGPT는 Commit, Push, Branch, Tag를 생성하지 않음

## 이번 추가 보완

### Notification Durable Outbox 만료 Lease 복구

기존 구현은 Worker가 `PROCESSING` 상태를 Claim한 뒤 종료되면 `lease_until`이 만료되어도 후보 조회가 `READY`, `RETRY`만 대상으로 하므로 해당 건이 영구 고착되는 문제가 있었다.

수정 후 `processDue()` 시작 시 만료된 `PROCESSING`을 별도 DB Transaction에서 `UNKNOWN_RESULT`로 격리한다.

- 자동 재발송 금지
- `last_error_code=LEASE_EXPIRED_UNKNOWN_RESULT`
- Lease와 다음 시도 시각 제거
- CAS Version 증가
- 운영자가 Provider 이력과 operationId를 대조한 뒤 명시적 Retry 또는 Cancel 수행

Provider가 성공했지만 결과 저장 전에 Worker가 종료됐을 수 있으므로 자동 `RETRY`는 중복 알림 위험이 있어 허용하지 않는다.

### Static Gate 보강

`check-notification-portable-sql.ps1`이 기존 `AdmNotificationService`와 Controller만 검사하고 신규 `AdmNotificationOutboxService`를 검사하지 않던 누락을 수정했다.

- Outbox의 공식 3 DB 비호환 SQL 검사
- Core Internal/Common 직접 의존 검사
- 만료 Lease → UNKNOWN_RESULT 복구 Marker 검사
- 자동 RETRY 회귀 차단

## 변경 파일

- `cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmNotificationOutboxService.java`
- `cpf-admin/src/test/java/com/cpf/admin/opr/service/AdmNotificationOutboxServiceTest.java`
- `cpf-tools/scripts/check-notification-portable-sql.ps1`
- `cpf-tools/verification/20260729_01/IMPLEMENTATION_REPORT.md`
- `cpf-docs/work/handover/CPF_CHATGPT_SESSION_HANDOVER_20260729_01.md`

## 적용 후 우선 검증

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-notification-portable-sql.ps1; .\gradlew.bat :cpf-admin:test --tests "com.cpf.admin.opr.service.AdmNotificationOutboxServiceTest" --no-daemon --stacktrace
```

전체 검증은 최신 Clean Worktree에서 기존 Closure 실행기를 사용한다.

## 미검증

현재 ChatGPT 환경에는 Repository 전체 Checkout, PowerShell, Gradle Dependency Cache, 실제 DB가 없어 다음은 직접 실행하지 못했다.

- PowerShell Gate 실행
- Java 25 Gradle Compile/Test
- MariaDB/PostgreSQL/Oracle Runtime SQL
- 실제 Worker Crash/Lease Expiry 다중 Instance 시나리오

따라서 위 항목은 최신 사용자 Push SHA에서 Evidence를 생성해 판정해야 한다.
