# CPF QA31 개발 지시 프롬프트

최신 `master`를 가져온 뒤 현재 HEAD와 기준 SHA `693cc77bde4c830b78ca1408dec7e34ef84cd11d`의 차이를 먼저 확인하라. 사용자가 별도 Push를 했다면 최신 HEAD를 실제 개발 기준으로 사용하되, 시작 SHA와 종료 Working Tree SHA를 모두 기록하라.

다음 문서와 CSV를 검수 기준으로 읽고 **원본을 수정하지 말라**.

- `cpf-docs/work/current/CPF_20260730_06_QA31_DEVELOPMENT_REMEDIATION_REQUEST.md`
- `cpf-docs/quality/CPF_20260730_QA31_DEFECT_REGISTER.csv`
- `cpf-docs/quality/CPF_20260730_QA31_REQUIREMENT_MATRIX.csv`
- `cpf-docs/quality/CPF_20260730_QA31_SCENARIO_MATRIX.csv`
- `cpf-docs/governance/CPF_AI_DEVELOPMENT_QA_CONTINUITY_STANDARD.md`

개발 전에 1차로 Source·SQL·Test·Runtime Consumer를 확인하고 결함을 Root Cause와 수직 Slice 기준으로 효과적으로 재정리하라. 결함 개수를 줄여 보이게 합치거나 정본 범위를 임의 축소하지 말고, 중복 결함만 같은 Root Cause 아래 묶어라. 간단한 사전 리뷰를 `cpf-docs/work/review/CPF_20260730_QA31_PRE_DEVELOPMENT_REVIEW.md`로 남긴 뒤 P0부터 실제 개발하라.

Interface·DTO·Controller·SQL Table·화면·Test 파일 생성만으로 완료 처리하지 말라. UI → API → Owner → DB → Runtime Consumer → 실패·복구 → Audit/Ledger → UI 결과가 실제로 연결돼야 한다. 실행하지 않은 Build·DB·Redis·Multi-instance·Browser 검증은 PASS나 완료로 기록하지 말라.

ADM은 기존 관리 메뉴 구조와 운영 편의성을 최대한 살리고, 기본 CRUD 수준보다 검색·상태·영향도·실시간 진행·일괄조치·승인·복구·관련 Timeline을 실사용 가능한 수준으로 구현하고 Browser E2E로 검증하라.

EDU는 `cpf-reference`를 중심으로 CPF Public API와 Framework 기능을 가능한 한 폭넓고 상세하게 사용하는 개발자 샘플로 구현하라. Sample은 제품 Runtime과 분리하고 기본 운영 Profile에서 자동 활성화하지 말라. 성공 예제만 만들지 말고 Validation·Conflict·Timeout·Retry·Unknown·Recovery·Permission 실패 예제와 자동 Test를 포함하라.

README와 Guide는 별도 AI가 작업 중이므로 원칙적으로 관여하지 말라. 모든 `README*`, `cpf-docs/guides/**`, `cpf-tools/README.md`는 기능 개발에 꼭 필요한 최소 수정 외에는 변경하지 말고 Codex에도 같은 경계를 전달하라.

개발 종료 시 다음을 수행하라.

1. 자체검토 Result Matrix와 미검증 목록 작성
2. 실행 명령·Profile·시작/종료 시각·Exit Code·Expected/Actual·Evidence 경로 기록
3. 변경 파일과 영향 Module, Migration, API, Test, Runtime Consumer를 요약
4. 해결하지 못한 항목은 숨기지 말고 `미구현/부분 구현/미검증/실패/재확인 필요`로 기록
5. `cpf-tools/scripts/package-cpf-qa31-development-result.ps1`를 사용하거나 동일 규칙으로 **프로젝트 Root 상대경로 ZIP** 생성
6. ZIP에는 변경 Source·SQL·Test·QA 결과·Evidence·Manifest만 포함하고 `.git`, Secret, Credential, Build Cache는 제외
7. 사용자에게 실제 다운로드 가능한 ZIP 링크 제공
8. Commit·Push·Branch·Tag·PR은 사용자 명시 승인 전 생성하지 말 것
