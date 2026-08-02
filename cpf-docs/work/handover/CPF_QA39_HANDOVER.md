# CPF QA39 Final Handover

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Reviewed exact SHA: `9a9634eb1f28071d47c205cc35227b6d013a4536`
- Overall: `development_status=부분 구현`, `verification_status=실패`
- Runtime/DB/Frontend/Supply-chain: `미검증`
- Requirement: 42
- Scenario: 31
- Public target: 6 Profile+7 Group
- Complete removal: 11 modules
- Replace/internalize old paths: 12
- Actual source deletion in QA package: 0

## Start

1. `cpf-docs/work/current/CPF_QA39_FINAL_DEVELOPMENT_REQUIREMENTS.md`
2. `cpf-docs/quality/CPF_QA39_FINAL_REQUIREMENT_MATRIX.csv`
3. `cpf-docs/work/manifest/CPF_QA39_DELETE_WORK_ITEMS.csv`
4. `cpf-docs/work/review/CPF_QA39_DEVELOPER_REPORT_AND_SELF_REVIEW_TEMPLATE.md`
5. `cpf-docs/work/codex/qa39/CODEX_START_HERE.md`

## Critical rule

QA 개발요건과 자체 개발요건이 충돌하면 QA 개발요건이 우선한다. 이전 자체요건의 미등록 7개 모듈 제품화와 Resilience/Feature Flag 보호는 폐기한다.

## Final work delivery

개발 GPT는 구현 Source와 함께 Developer Implementation Report, Self Review, exact-SHA Evidence, Delete Manifest/cleanup log, Codex review package를 남긴다. QA가 반복 탐색하지 않도록 변경 파일·라인·명령·기대 결과를 명시한다.

## 정리 인수인계

루트 QA 파일과 중복 Current/Handover/Continuity 문서는 유지하지 않는다. 즉시 정리 대상은 `cpf-docs/work/manifest/CPF_QA39_REPOSITORY_CLEANUP_PATHS.txt`, 제품 재구성 후 삭제 대상은 `CPF_QA39_FINAL_DELETE_PATHS.txt`를 사용한다.
