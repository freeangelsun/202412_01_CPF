# Project Control Currentization & Garbage Cleanup Review

- Basis SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)
- Scope: 중앙 Project Canonical / Final Control / superseded Work control·session·review·evidence
- Product Source 변경: **0**
- README/Guide/Deliverable/Documentation Standard 변경: **0**
- Protected Docker path 변경: **0**

## 중앙 현행화

- Canonical Requirement Count를 **169**로 통일
- TransactionId를 **정식 거래 기동 Channel/System 최초 생성 → 동일 transactionId End-to-End 승계·보존**으로 명시
- 비신뢰 spoof/replay와 정식 Channel propagation을 분리
- `CPF_FINAL_TARGET_REQUIREMENTS.md`를 영구 최상위 제품 정본으로 확정
- Current 진입점을 `cpf-docs/work/v9i/final-control/REVIEW_INDEX.md`로 통일
- 중앙/개발GPT/QA A·B/Documentation 역할 경계를 고정
- Final Developer instruction의 blanket inbound transactionId 재생성 해석 제거
- top-level Governance의 162/QA38/stale workflow/status 표현을 현행화

## Garbage Cleanup

`PROJECT_CONTROL_DELETE_MANIFEST.csv`에 **874개 exact tracked file path**를 등록했다.
Wildcard 또는 Directory recursive delete는 사용하지 않는다.

정리 범위:
- 과거 root/current Work Request 및 QA38/QA39 Control
- V7.1 / V9 이전 Control package
- 날짜별 Review/Handover
- 과거 Parallel QA/DEV session package
- 과거 `work/development`, `work/evidence`, `work/qa`, `work/r6i-dev`
- 과거 `work/archive`, `work/history`, `work/state`, `work/requests`, `work/continuity`, `work/overlay`
- 과거 QA35~38 / Requirement-Rebase Codex 결과
- 과거 `work/manifest` package/hash/delete 산출물
- 과거 monolithic root Work package/status 산출물

삭제해도 과거 내용은 Git History에 남는다.

## 현재 Final 때문에 의도적으로 유지

- cpf-docs/work/v9i/final-control/** — 현재 중앙 정본
- cpf-docs/work/v9i/final-dev-request/** — 현재 개발 지침
- cpf-docs/work/v9i/qa/final-a/**, final-b/** — 현재 독립 QA 원본
- cpf-docs/work/v9i/dev-r6j14/** — 07_05 Product 개발 provenance
- cpf-docs/work/v9i/qa/r6j/** — 현재 56 Finding 통합 provenance
- cpf-docs/work/v9i/results/** — current Final 대용량 결과 Dataset
- cpf-docs/work/CPF_REQUIREMENT_MATRIX.csv / CPF_SCENARIO_MATRIX.csv / CPF_SOURCE_FINDINGS.csv / CPF_PUBLIC_SURFACE_CATALOG.csv / CPF_STARTER_VALUE_CATALOG.csv — Final 개발 종료 전 입력 참조 가능성이 있어 보수적으로 유지
- cpf-docs/work/runbooks/DB_RESET_CLEAN_INSTALL_RUNBOOK.md — 유효 운영 Runbook
- Product Source/SQL/Test/Config/Frontend/Verification scripts — Consumer 이관 전 이름만으로 삭제하지 않음

## Empty Directory Cleanup

파일 삭제 후 실제로 비어 있는 경우에만 제거하도록 **71개 exact directory path**를 등록했다.
`Remove-Item -Recurse`는 사용하지 않는다. 파일이 하나라도 남으면 `KEEP(non-empty)`로 유지한다.

## 안전장치

`APPLY_PROJECT_CONTROL_CLEANUP.ps1`은:
- Repository Root 확인
- HEAD가 `4870b20733875c3955f93846307fa5041e6f6c22`인지 확인
- exact relative file path만 허용
- wildcard / rooted path / `..` 차단
- 고객문서·Governance·현재 Final Control·Final QA·Docker 보호 경로 삭제 차단
- 삭제 대상에 uncommitted change가 있으면 즉시 중단
- 디렉터리는 empty일 때만 exact path 삭제
- 마지막에 `git status --short` 출력

Overlay 적용 후 Cleanup을 수행하고, 검증 후 사용자만 Commit/Push한다.
