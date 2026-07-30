# CPF 20260730 Final Overlay 적용 Runbook

## 기준

- Branch: `master`
- Overlay 기준 SHA: `fae7aa9643f646db4bcbcf665d13b8f3b809e8c8`
- ZIP은 Repository Root 상대경로 구조다.
- Script는 Commit, Push, Branch를 생성하지 않는다.

## 적용 한 줄 명령

```powershell
$z="$HOME\Downloads\CPF_20260730_FULL_IMPLEMENTATION_FINAL_OVERLAY.zip"; $t="$env:TEMP\CPF_20260730_FINAL"; Remove-Item $t -Recurse -Force -ErrorAction SilentlyContinue; Expand-Archive $z $t -Force; Copy-Item "$t\*" "C:\dev\projects\jck\202412_01_CPF" -Recurse -Force; cd "C:\dev\projects\jck\202412_01_CPF"; pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\apply-cpf-20260730-final-overlay.ps1 -RepoRoot (Get-Location).Path -RunLowCostGates
```

기준 SHA가 달라졌다면 `-AllowDifferentBase`를 관성적으로 사용하지 않는다. 새 `master`와 Overlay 충돌을 먼저 검수한다.

## Source Commit 이후 전체 검증

```powershell
cd "C:\dev\projects\jck\202412_01_CPF"; pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-cpf-20260730-full-implementation.ps1 -RepoRoot (Get-Location).Path -RecordEvidence
```

DB Lifecycle Profile이 준비된 경우 `-RunDatabaseLifecycle -DatabaseProfilePath <profile>`을 추가한다.

## 적용 후 확인

```powershell
cd "C:\dev\projects\jck\202412_01_CPF"; git diff --check; git status --short; git diff --stat
```

## 삭제되는 Stale 파일

적용 Script는 Archive 사본을 확인한 뒤 과거 Active 요청서 4개, 체크포인트 Handover, 체크포인트 Root Manifest 3개를 제거한다. 제품 Root에는 `build.gradle` 변경 외 Overlay 운영 파일을 남기지 않는다.
