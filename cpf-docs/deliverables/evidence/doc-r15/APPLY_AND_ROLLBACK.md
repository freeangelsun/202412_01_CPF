# 적용·삭제·Rollback

## 적용
Repository Root에서 ZIP을 그대로 해제한다. 적용 전 `git status --short`, `git diff --name-status`, `git ls-files --others --exclude-standard`로 기존 변경을 확인한다.

## 교체 삭제
`DELETE_MANIFEST.txt`의 기존 Markdown 13개는 PDF/DOCX 전환에 따라 제거 대상이다. 전체 미추적 파일이나 다른 작업자의 변경을 삭제하지 않는다.

## Rollback
적용 전에 보존한 기존 Markdown 13개와 README를 exact path로 복원하고, 이번 작업의 PDF/DOCX 26개 및 `cpf-docs/deliverables/evidence/doc-r15`만 제거한다. `git reset --hard`, `git clean`, `git restore .`는 사용하지 않는다.
