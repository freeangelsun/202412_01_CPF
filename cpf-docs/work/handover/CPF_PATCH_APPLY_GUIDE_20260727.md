# CPF Patch Apply Guide — 2026-07-27

## 기준

이 Patch는 `master`의 `fb95e15f90856adcff39040a50b128aa40f5ef43` (`20260727_01`)을 기준으로 작성됐다.

Git Commit/Push를 포함하지 않는다.

## 적용

ZIP 내부 경로는 Repository Root 상대경로다.
Repository Root에 압축을 풀어 동일 경로에 덮어쓴다.

적용 전에 현재 작업 Tree가 `fb95e15f...` 이후 추가 수정된 상태라면 무조건 덮어쓰지 말고 `git diff`로 충돌 파일을 먼저 확인한다.

적용 후 최소 확인:

```text
git status --short
git diff -- build.gradle cpf-admin cpf-biz-admin cpf-batch cpf-tools cpf-docs
```

`git reset --hard`, `git clean -fd`, 광범위 `git restore`는 사용하지 않는다.

## 적용 직후 권장 저비용 검증

Windows/PowerShell 7 환경:

```text
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-admin-contact-model.ps1
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-bza-safe-defaults.ps1
```

그 다음 Codex는 `CPF_CODEX_2ND_REVIEW_CHECKLIST_20260727.md`의 집중 검증만 먼저 수행한다.

DB는 Source SQL을 수동 복사해 맞추지 않는다.
Canonical Source를 확인한 뒤 공식 `sync-database-artifacts.ps1`로 Generated DB Artifact를 재생성하고 Diff를 검토한다.

## 중요 미검증

ChatGPT 환경에서는 다음을 실제 실행하지 못했다.

- Java 25 Gradle compile/test
- local Maven publication
- Generated Domain standalone build
- bootJar/bootWar package verification
- PowerShell AST
- full frontend build
- DB artifact sync
- MariaDB V59/V60 lifecycle

따라서 위 항목은 Patch 적용만으로 완료가 아니다.
