# OPEN ISSUES

## 미검증 - Windows PowerShell replay
- 이 Linux container에는 `pwsh`/Windows PowerShell이 없어 `.ps1` Validator 자체 실행은 하지 못했다.
- 동일 규칙의 Python Harness/README/Visual/DOCX/Fixture Validator는 실제 실행해 PASS했다.
- 적용 후 `cpf-docs/deliverables/documentation/VERIFY.ps1`을 Windows에서 실행하면 된다.

## Baseline 분리 이슈 - 기존 장경로
- supplied Source의 `cpf-docs/work/evidence/codex/current/**` 일부 장경로는 이번 Documentation 변경 이전부터 존재할 수 있다.
- Documentation 산출물/Harness가 만든 파일은 집/회사 기준 absolute path 150자 Gate를 통과했다.
- 기존 Evidence는 사용자 승인 없이 삭제하지 않았다.

위 두 항목 외 이번 Documentation Overlay에 남아 있는 검증 가능한 실패는 0건이다.
