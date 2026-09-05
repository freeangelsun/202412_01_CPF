# CPF Open Git — User Git Commands

> Release Tool은 아래 명령을 실행하지 않습니다. 모든 Release Gate PASS와 사용자 검토 후 Open Git에서만 직접 실행합니다.
> `cpf-release/` 결과는 CPF Private master Commit/Push 대상이 아닙니다.

## PowerShell

```powershell
Set-Location -LiteralPath 'C:\dev\projects\jck\202412_01_CPF\cpf-release\open-git'
git status --short
git diff --check
$branch=(git branch --show-current).Trim(); if([string]::IsNullOrWhiteSpace($branch)){throw 'OPEN GIT BRANCH NOT RESOLVED'}
git add -A
git diff --cached --check
git status --short
git commit -m "CPF Open Git $(Get-Date -Format yyyyMMdd_HHmmss)"
git push origin $branch
Write-Host "PUSHED_SHA=$((git rev-parse HEAD).Trim())"
git status --short
```

## POSIX shell

```bash
cd -- 'C:\dev\projects\jck\202412_01_CPF\cpf-release\open-git'
git status --short
git diff --check
branch=$(git branch --show-current); test -n "$branch"
git add -A
git diff --cached --check
git status --short
git commit -m "CPF Open Git $(date +%Y%m%d_%H%M%S)"
git push origin "$branch"
printf 'PUSHED_SHA=%s\n' "$(git rev-parse HEAD)"
git status --short
```
