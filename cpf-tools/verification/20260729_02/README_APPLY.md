# CPF Stage 2 Root Overlay 적용·검증

## 적용 기준

- 기준 master: `9e5d1676a9ccba55fedf4dfb633a9e710f487a02`
- ZIP은 Repository Root 상대경로이며 별도 상위 폴더가 없다.
- 적용 전 로컬 변경을 별도로 보존한다.
- 이 Overlay는 2차 구현·정적 Gate·통합 QA 원장·Codex 요청서를 제공하며, 적용만으로 전체 QA 완료가 되지 않는다.

## 1. 적용 직후 확인

```powershell
git status --short
git diff --check
git diff --stat
```

## 2. 사용자 실행 실패 재검증

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-notification-portable-sql.ps1
.\gradlew.bat help --no-daemon --stacktrace
.\gradlew.bat :cpf-admin:test --tests "com.cpf.admin.opr.service.AdmNotificationOutboxServiceTest" --no-daemon --stacktrace
```

## 3. 통합 정적 Gate

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-integrated-architecture-ui-hygiene.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1
```

이 단계에서 실패하면 Codex 전수 검수 전에 Root Cause를 먼저 수정한다.

## 4. Push 후 Codex 전수 검수·보완 개발

사용자가 Overlay를 적용하고 Push한 최신 `master` SHA에서 다음 요청서를 사용한다.

`cpf-docs/work/requests/CPF_CODEX_FULL_QA_CLOSURE_REQUEST_20260729.md`

Codex 대상은 이번 Overlay만이 아니다.

- 직전 Codex 검수 이후 누적된 모든 변경
- 20260728 1차 QA Closure 구현 묶음의 회귀
- 20260729 2차 보완
- 기존 QA 2,118건
- 신규 QA 병합 후 최종 원장 2,715건
- P0 Ledger 18건
- ADM Runtime Control 14개 Capability
- 실제 Build·DB·Browser·다중 인스턴스·장애 복구 Evidence

## 5. 최종 Closure 실행 시점

현재 병합 원장 2,715건은 거짓 완료를 막기 위해 전부 `미검증`으로 시작한다. 따라서 Overlay 적용 직후 Final Closure를 실행하면 마지막 원장 검증 단계에서 의도적으로 실패한다.

Codex가 실제 검수·보완을 마치고 다음을 모두 갱신한 뒤 실행한다.

- 각 QA 항목의 최신 상태
- Requirement별 Source·SQL·API·Test·Runtime·Evidence 연결
- 최신 Push SHA의 Build·DB·Browser·다중 인스턴스 Evidence
- 완료 이외 상태 제거

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\invoke-cpf-final-closure.ps1 `
  -DatabaseProfilePath .\profiles\mariadb.json,.\profiles\postgresql.json,.\profiles\oracle.json `
  -RunGitHubGovernance
```

Profile 경로는 사용자의 실제 Secret 없는 Profile 경로로 교체한다. Secret 원문은 Git이나 Evidence에 저장하지 않는다.
