# CPF QA38 Handover

- Base SHA: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
- Branch: `master`
- Requirement: 156건, 개발 완료 156건
- Verification: 완료 40건, 미검증 116건
- Delete Manifest: 160개
- Empty Directory Manifest: 24개
- Apply: `cpf-tools/scripts/apply-qa38-root-overlay.ps1`
- Verify: `cpf-tools/scripts/verify-qa38-starter-closure.ps1`
- Codex request: `cpf-docs/work/codex/qa38/CPF_CODEX_QA38_FINAL_INDEPENDENT_VALIDATION_REQUEST.md`

Clean `master`에서 적용한다. 최신 HEAD는 기준 SHA의 후손이어야 하며 QA38 관리 경로와 Commit 변경 경로가 겹치면 적용 Script가 중단한다. 보호 경로는 복사·삭제하지 않는다. 적용 Script가 Source 복사와 exact Legacy 삭제를 모두 수행하므로 삭제 상태를 포함해 사용자가 Commit·Push한다.

- 최종 원격 확인 SHA: `99fefc6346c70406cbac5c59ad33d0c069166c2f` (기준 SHA 후손, QA38 관리 경로 중첩 0건)
