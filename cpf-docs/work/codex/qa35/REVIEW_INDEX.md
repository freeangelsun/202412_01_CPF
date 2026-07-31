# QA35 Codex Review Index

## 목적
최신 Source 전체를 처음부터 반복 탐색하지 않고 QA35의 확정 결함과 변경 결과만 검수한다.

## 기준
- Start SHA: `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`
- Defects: 46
- Requirements: 55
- ADM Routes: 59
- EDU Feature Baseline: 32

## 읽는 순서
1. `../../review/CPF_20260801_QA34_POST_PUSH_INDEPENDENT_SOURCE_REVIEW.md`
2. `../../../quality/CPF_20260801_QA35_DEFECT_REGISTER.csv`
3. `../../../quality/CPF_20260801_QA35_REQUIREMENT_MATRIX.csv`
4. `../../../quality/CPF_20260801_QA35_ADM_MENU_FUNCTION_BASELINE.csv`
5. `../../../quality/CPF_20260801_QA35_EDU_FEATURE_COVERAGE_BASELINE.csv`
6. `TEST_AND_EVIDENCE.md`
7. `OPEN_ISSUES.md`

## 최초 확인 파일
반복적인 전역 검색 전에 아래 파일만 우선 비교한다.

- `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- `cpf-admin/frontend/openapi/cpf-openapi.json`
- `cpf-biz-admin/frontend/openapi/cpf-openapi.json`
- `cpf-admin/frontend/src/generated/.cpf-openapi-source.json`
- `cpf-admin/frontend/scripts/validate-openapi.mjs`
- `cpf-admin/frontend/scripts/verify-generated-client.mjs`
- `cpf-admin/frontend/scripts/write-generated-marker.mjs`
- `cpf-admin/frontend/package.json`
- `cpf-admin/frontend/src/app/routes.ts`
- `cpf-admin/frontend/src/state/createAdmState.ts`
- `cpf-admin/frontend/e2e/route-quality.spec.ts`
- `cpf-tools/scripts/verify-cpf-qa34-source-closure.py`
- `cpf-tools/scripts/verify-cpf-qa34-frontend-runtime.ps1`
- `cpf-tools/scripts/generate-cpf-supply-chain-evidence.ps1`

BZA는 ADM과 계약이 동일한지 diff 중심으로 확인한다.

## 검수 단계
1. exact SHA/clean tree 확인
2. QA35 Source 변경 Manifest 확인
3. Frontend deterministic preflight만 먼저 실행
4. ADM 59 Route Matrix coverage gate 실행
5. EDU Requirement/Public Contract coverage gate 실행
6. 변경 영역 관련 Unit/Contract Test 실행
7. 환경 준비가 완료된 경우에만 최종 Runtime Wrapper 1회 실행
8. 실패 시 실패 단계만 재실행하고 전체 탐색·전체 Runtime 반복 금지

## PASS 금지
- marker/script 존재만 확인
- Route/menu 이름만 확인
- EDU sample 이름만 확인
- Exit 0만 보고 Requirement 완료 처리
- 실제 실행하지 않은 Runtime을 PASS 처리

## ADM 최소 기능선 추가 검수 순서
1. `../../../quality/CPF_20260801_QA35_ADM_SCREENSHOT_EVIDENCE_INDEX.csv`
2. `../../../quality/CPF_20260801_QA35_ADM_LEGACY_MINIMUM_CAPABILITY_MATRIX.csv`
3. `../../../quality/CPF_20260801_QA35_ADM_TARGET_MENU_ARCHITECTURE.csv`
4. `../../../quality/CPF_20260801_QA35_ADM_SCREEN_QUALITY_ACCEPTANCE.csv`
5. `../../review/CPF_20260801_QA35_ADM_LEGACY_MINIMUM_DETAILED_REVIEW.md`

Capability 이름과 유사한 Route가 있다는 이유만으로 PASS하지 않는다.
실제 API·Permission·Interaction·Runtime Result를 확인한다.
