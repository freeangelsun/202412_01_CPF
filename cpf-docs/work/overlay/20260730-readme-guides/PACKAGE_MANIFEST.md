# CPF README·가이드 문서 패키지 Manifest

- 작성일: 2026-07-30
- 기준 Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 분석 기준 HEAD: `7f34635781416bf74352e56deab6c5e388ad55a3`
- 문서 작성 기준: CPF 최종 요건을 모두 충족한 완성 제품
- Commit/Push/Branch 생성: 없음

## 덮어쓰기 문서

- `README.md`
- `cpf-docs/guides/CPF_DEVELOPER_GUIDE.md`
- `cpf-docs/guides/CPF_FOUNDATION_API_GUIDE.md`
- `cpf-docs/guides/CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md`
- `cpf-docs/guides/CPF_GENERATOR_TOOL_GUIDE.md`
- `cpf-docs/guides/CPF_ADMIN_OPERATOR_GUIDE.md`
- `cpf-docs/guides/CPF_BIZ_ADMIN_GUIDE.md`
- `cpf-docs/guides/CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md`
- `cpf-docs/guides/CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md`
- `cpf-docs/guides/CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md`
- `cpf-docs/guides/CPF_DATABASE_TOOL_GUIDE.md`
- `cpf-docs/guides/DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md`
- `cpf-docs/guides/CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md`
- `cpf-docs/guides/CPF_SECURITY_DR_RETENTION_GUIDE.md`
- `cpf-docs/guides/CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md`
- `cpf-docs/guides/CPF_TOOLS_GUIDE.md`
- `cpf-docs/guides/CPF_TOOL_REFERENCE.md`
- `cpf-docs/guides/CPF_EDU_COVERAGE_GUIDE.md`

## 신규 문서

- `cpf-docs/guides/CPF_GATEWAY_OPERATIONS_GUIDE.md`
- `cpf-docs/guides/CPF_ASYNC_MESSAGING_AND_COMPENSATION_GUIDE.md`
- `cpf-docs/guides/CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md`
- `cpf-docs/guides/CPF_CONFIGURATION_AND_RUNTIME_POLICY_GUIDE.md`
- `cpf-docs/guides/CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md`
- `cpf-docs/guides/CPF_TEST_AND_EVIDENCE_GUIDE.md`

## 패키지 도구

- `README_APPLY.md`
- `delete-obsolete-document-artifacts.ps1`
- `verify-readme-guide-package.ps1`

## 삭제 후보

삭제 Script는 Archive 사본을 확인한 후 과거 Active 문서를 삭제한다. Root Overlay 잔재는 존재할 때만 삭제한다.

- `CPF_20260730_OVERLAY_APPLY_README.md`
- `CPF_OVERLAY_MANIFEST.json`
- `CPF_OVERLAY_SHA256SUMS.txt`
- `cpf-docs/work/current/CPF_20260730_01_FINAL_CLOSURE_REQUIREMENT_INTAKE_BASELINE.md`
- `cpf-docs/work/current/CPF_CHATGPT_DIRECT_FULL_IMPLEMENTATION_REQUEST_20260730.md`
- `cpf-docs/work/current/CPF_CODEX_FINAL_REVIEW_DOCUMENT_STRATEGY.md`
- `cpf-docs/work/current/CPF_CODEX_FINAL_REVIEW_REQUEST.md`
- `cpf-docs/work/handover/CPF_20260730_CHATGPT_DIRECT_IMPLEMENTATION_CHECKPOINT_HANDOVER.md`

## 적용 원칙

1. Repository Root에 압축을 풀어 같은 경로에 덮어쓴다.
2. 삭제 Script를 실행한다.
3. 정적 검증 Script와 Repository 문서 Link Gate를 실행한다.
4. Git Diff를 검토한다.
5. 사용자가 직접 Commit한다.
