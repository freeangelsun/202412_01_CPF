# CONTINUITY

다음 세션은 Source ZIP `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_205036.zip` / SHA-256 `A5B7844665F4AC3BDAEC601389B306CEBD6F0407AD1C07930C40170611DB7A07`와 현재 Local Working Tree가 같은 기준인지 먼저 확인한다. Harness는 `cpf-docs/governance/documentation-harness/` v2.3.0 현행본 하나만 사용한다. Documentation 변경은 `PATCH_FIRST`로 수행하고, 검수된 좋은 영역을 이유 없이 Fresh redesign하지 않는다. 새 사용자 Finding은 산출물만 임시 수정하지 말고 공통 원인을 Harness/Validator에 먼저 반영한 뒤 영향 범위만 보정한다.

공통 품질 기준은 독자 니즈 우선, 실제로 표가 필요한 데이터만 표 사용, 의미 대칭인 경우에만 균등 열폭, 그 외 내용/역할 기반 열폭, 표 Header 1줄, H1 한 줄 우선, 메뉴/단락 전환의 충분한 세로 여백, Figure의 실제 삽입 Render safe-area/overlap/crop/semantic completeness 검수, PDF-only 사용자 Navigation이다.

집/회사 공통 명령은 Repository root를 `git rev-parse --show-toplevel`로 자동 탐지하고 Downloads는 `$HOME\Downloads`를 사용한다. PC별 `C:`/`D:` 고정 경로를 기본 명령에 하드코딩하지 않는다.

Supplied Source의 `cpf-docs/work/evidence/codex/current/**` 장경로는 Documentation 변경 외 선행 이슈다. 사용자 승인 없이 Evidence를 삭제하지 않으며 Documentation artifact path PASS와 repo-wide baseline path debt를 구분한다.
