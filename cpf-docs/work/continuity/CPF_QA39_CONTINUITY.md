# CPF QA39 다음 검수 인수인계

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 적용 전 Base SHA: `4aea798c913787e86341809e2cef2b9495cbf7ba`
- 회사 노트북 기준 Project Root: `D:\WORK_CPF\202412_01_CPF`
- 회사 노트북 기준 Download Root: `D:\11096\Downloads`
- 환경 사전점검: `cpf-tools/verification/qa39/test-qa39-company-laptop-environment.ps1`
- 적용 스크립트: `cpf-tools/scripts/apply-qa39-final-corrective.ps1`
- 저비용 Gate: `cpf-tools/verification/qa39/invoke-qa39-low-cost-gates.ps1`
- 전체 검증: `cpf-tools/verification/qa39/invoke-qa39-final-validation.ps1`
- Matrix: `cpf-docs/work/matrix/CPF_QA39_REQUIREMENT_SCENARIO_RESULT_MATRIX.csv`
- Evidence: `cpf-docs/evidence/qa39/`
- Delete Manifest: `cpf-docs/work/manifest/DELETE_MANIFEST.csv`

## 적용 후 반드시 확인할 것

1. 적용 전 HEAD가 `4aea798c913787e86341809e2cef2b9495cbf7ba`인지 확인.
2. 다른 작업자 변경이 있으면 적용기가 중단하며 임의 stash/restore/clean 금지.
3. R3는 Python을 사용하지 않으며 Java 17+ source launcher로 적용·저비용 Gate를 실행한다.
4. 적용 후 `git diff --check`와 저비용 Gate를 한 번 실행.
5. Java25 → Frontend → DB → Runtime → Supply-chain 순서로 전체 검증을 각 한 번 실행.
6. 모든 Evidence가 적용 후 exact SHA와 exit 0을 가질 때만 QA39 완료 처리.

Codex는 독립 검수자이며 기능을 재설계하지 않는다. Source 결함과 Environment Blocker를 분리한다.
