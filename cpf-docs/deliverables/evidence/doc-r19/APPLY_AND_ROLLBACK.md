# R19 Apply and Rollback

## 적용
Repository Root에서 R19 ZIP을 `Expand-Archive -Force`로 덮어쓴 뒤 아래 exact deletion command를 실행한다.

```powershell
$m='cpf-docs/deliverables/evidence/doc-r19/DELETE_MANIFEST.txt'; Get-Content -LiteralPath $m | Where-Object { $_ -and -not $_.StartsWith('#') } | ForEach-Object { if (Test-Path -LiteralPath $_ -PathType Leaf) { Remove-Item -LiteralPath $_ -Force } }; @('cpf-docs/deliverables/evidence/doc-r18','cpf-docs/deliverables/evidence/doc-r15') | Sort-Object { $_.Length } -Descending | ForEach-Object { if ((Test-Path -LiteralPath $_ -PathType Container) -and -not (Get-ChildItem -LiteralPath $_ -Force | Select-Object -First 1)) { Remove-Item -LiteralPath $_ -Force } }
```

삭제 대상은 `DELETE_MANIFEST.txt`의 64개 exact path뿐이다. README가 사용하는 16개 이미지, Source/SQL/API/Config/Frontend/Test, 다른 작업자의 변경, 전체 미추적 파일은 삭제하지 않는다. `git clean`, wildcard 전체 삭제, `reset --hard`, `restore .`를 사용하지 않는다.

적용 후 확인:

```powershell
git status --short; git diff --name-status; git diff --stat; git diff --check; git ls-files --others --exclude-standard
```

## Rollback
적용 전 보관본에서 R19의 MODIFIED 파일을 되돌리고, `doc-r19` NEW 파일만 exact path로 제거한다. 삭제 Manifest로 제거한 기존 파일이 필요하면 적용 전 Repository 상태 또는 기준 Commit `3ed676061246c9db3e44f29e254c0393ecca3929`에서 해당 exact path만 복원한다. 광범위 Restore/Clean은 사용하지 않는다.
