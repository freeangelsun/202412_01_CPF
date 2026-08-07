# R18 Apply and Rollback

## 적용 전
Repository Root에서 기존 변경을 먼저 보호한다.

```powershell
git status --short; git diff --name-status; git diff --stat; git ls-files --others --exclude-standard
```

## 적용

```powershell
Expand-Archive -LiteralPath .\CPF_DOCUMENTATION_PRODUCT_R18_64049044.zip -DestinationPath . -Force
```

이 ZIP은 Repository Root 상대경로를 그대로 포함한다. README 1개를 갱신하고 현재 기준 Commit에 없는 Markdown Guide 8개, 설계 Markdown 5개, `doc-r18` Evidence를 추가한다. 기존 PDF/DOCX·Source·SQL·API·Config·Frontend·Script·Test는 삭제하지 않는다.

## 적용 후 확인

```powershell
git status --short; git diff --name-status; git diff --stat; git diff --check; git ls-files --others --exclude-standard
```

## Rollback
적용 전에 보관한 `README.md`만 원복하고, `cpf-docs/deliverables/evidence/doc-r18/CHANGE_MANIFEST.csv`에서 `NEW`로 표시된 exact path만 제거한다. 기존 PDF/DOCX, 다른 작업자의 변경, Source 또는 전체 미추적 파일을 삭제하지 않는다. `git clean`, `git reset --hard`, `git restore .`를 사용하지 않는다.
