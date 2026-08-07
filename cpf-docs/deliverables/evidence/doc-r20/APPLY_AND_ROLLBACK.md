# R20 적용과 Rollback

1. Repository Root에서 R20 ZIP을 풀어 동일 경로를 덮어쓴다.
2. `cpf-docs/deliverables/evidence/doc-r20/DELETE_MANIFEST.txt`의 exact path만 삭제한다.
3. `git status --short`, `git diff --name-status`, `git diff --stat`, `git diff --check`, `git ls-files --others --exclude-standard`를 확인한다.
4. README가 공식 Guide/설계 PDF만 참조하고 보호 이미지가 남아 있는지 확인한다.
5. Commit/Push는 사용자 승인 절차에 따라 별도로 수행한다.

Rollback은 적용 전 Commit/백업에서 R20 수정 파일을 복원하고 `doc-r20` 신규 Evidence만 exact path로 제거한다. `git clean`, wildcard 삭제, 광범위 reset/restore를 사용하지 않는다.
