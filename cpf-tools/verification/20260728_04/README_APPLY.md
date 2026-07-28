# CPF QA Closure Root Overlay 적용

기준 Commit: `84b4672e9e8b61ea067bb52b85b838a0b95e44b1`

ZIP 내부가 Repository Root 상대경로이므로 프로젝트 Root에 압축을 풀어 덮어쓴다.

## 적용 전

```powershell
git status --short; git rev-parse HEAD
```

Base Commit이 다르면 먼저 최신 master와 Overlay 변경 충돌을 검토한다.

## 적용 직후 정적 Gate

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1
```

## 전체 Closure

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\invoke-cpf-final-closure.ps1 -DatabaseProfilePath .\profiles\mariadb.json,.\profiles\postgresql.json,.\profiles\oracle.json -RunGitHubGovernance
```

## Push 전 확인

```powershell
git status --short; git diff --check; git diff --stat
```

ChatGPT는 Commit·Push·Branch를 생성하지 않았다.
