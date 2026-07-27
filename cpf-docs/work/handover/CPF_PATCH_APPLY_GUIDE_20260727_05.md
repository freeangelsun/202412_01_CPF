# CPF 20260727_05 Root Patch 적용 Guide

## 기준
- 대상 기준 SHA: `00780dc14ef621578f6f7ca61ef1d0c9973c60e6`
- Patch는 Repository Root 상대경로 구조다.
- 적용 전 현재 master와 local 변경을 확인한다.
- 기존 사용자 변경을 덮어쓰지 말고 충돌 시 파일별 merge한다.

## 적용 후 저비용 확인

```text
git diff --check
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-admin-data-safety.ps1
```

Java25 환경:

```text
gradlew clean test assemble --no-daemon --max-workers=1
```

DB는 `CPF_ADMIN_DATA_SAFETY_GUIDE.md`의 V61 Runbook을 따른다.

사용자 승인 없이 자동 Commit/Push하지 않는다.
