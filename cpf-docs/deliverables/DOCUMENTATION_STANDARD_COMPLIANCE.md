# CPF 공식 문서 지침 준수 검수

기준 Source SHA: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`

이 검수는 키워드 존재만으로 PASS 처리하지 않는다. 사용자의 최신 직접 지시와 문서별 지침을 기준으로 실제 사용 흐름, Source 근거, 실패·복구·검증, 시각 품질을 반복 검수했고 FAIL 발견 시 편집 단계로 회귀했다.

## 최종 Gate 결과

| Gate | 결과 | 근거 |
|---|---:|---|
| README 내용 Gate | 12/12 PASS | 소개→구조→선택→Gateway/Batch→Quick Start→공식 문서→License |
| 02~07 문서별 심층 지침 Gate | 121/121 PASS | 02 24/24 · 03 22/22 · 04 20/20 · 05 18/18 · 06 15/15 · 07 22/22 |
| 지침 원문 무결성 | 8/8 PASS | IMMUTABLE_SHA256SUMS.txt 대조 |
| 전 페이지 시각 QA | 136/136 PASS | DOCX 최종 render의 모든 페이지 직접 검수 |
| 접근성 | 6/6 PASS | a11y high/medium/low 모두 0 |
| PDF 독립 재렌더 | 6/6 PASS | 최종 PDF 페이지 수 일치 |
| README 로컬 링크/이미지 | 14/14 PASS | 누락 0 |
| README↔07 Architecture | PASS | SHA-256 `33e2ac38c83c4121d70f107d0f98b8b1d7c9f77f47a838942521b5342be689eb` / 07 media match 1 |
| 제작 메타정보 제거 | PASS | 사용자 문서 visible 제작일 0, DOCX creator/modified/created property 0 |
| 금지/혼선 표현 | PASS | com.customer, MySQL/MSSQL/H2 공식지원, TODO/placeholder 0 |

## 문서별 내용 Gate

| 문서 | Gate | 판정 | 실사용 흐름 |
|---|---:|---|---|
| README | 12/12 | PASS | 30초 이해 → 전체 구조 → 구성 선택 → Gateway/Batch → 최소 시작 → 공식 문서 |
| 02 프레임워크 개발자 가이드 | 24/24 | PASS | 목적→Profile/Provider→build.gradle→공통 기능 선택→Tutorial→실패/Test→Cookbook/Reference→운영 인계 |
| 03 배치 개발자 가이드 | 22/22 | PASS | 처리 방식 선택→Job/Step 구현→Parameter/TX/Checkpoint→실패/Restart→Partition/Worker→Scheduler/Reconcile→운영 인계 |
| 04 운영자 매뉴얼 | 20/20 | PASS | 증상 찾기→검색/목록/상세→권한/상태→Action→복구→정상화→Audit/Evidence |
| 05 배치 운영 가이드 | 18/18 | PASS | Job/Execution 조회→실패 범위→Stop/Restart/Reprocess/Reconcile→Worker/Scheduler→대사→정상화 |
| 06 Gateway 개발·사용 가이드 | 15/15 | PASS | 선택 여부→Route/Target/Policy→Draft/Apply→Test→Runtime 정합성→운영 인계 |
| 07 Specification | 22/22 | PASS | API/DTO/Annotation/Config/State/SPI/DB→정확한 계약→오류/경계→Verification/Reference |

## 사용자 직접 지시 반영

- 기능/API 이름 나열이 아니라 실제 사용자가 무엇을 고르고 어떻게 셋업·검증하는지 앞에 배치했다.
- 표는 선택·비교·Reference에 필요한 경우만 사용하고 내용에 맞춰 열 폭을 조정했다.
- Generated Domain은 업무 Domain 개념으로 설명하고 Batch는 별도 선택 Capability/Runtime 축으로 구분했다.
- 문서 안의 내부 개발 검토 ID를 제거하고 Source/UX 문제는 별도 개발 검토 문서로 분리했다.
- README 최하단에 Community & Evaluation License를 반영했다.
- 사용자 매뉴얼에 작성/수정/생성 일시를 표시하지 않고 DOCX 메타정보에서도 해당 항목을 제거했다.

## 최종 판정

**개발 GPT 문서 작성·자체검수 Gate: PASS.** 개발 Source 자체의 개선 요청은 `CPF_DOCUMENTATION_TO_DEVELOPMENT_REVIEW.md`와 `CPF_DEVELOPER_USABILITY_REVIEW.md`로 분리되어 있으며 문서 Gate의 False Green으로 숨기지 않았다. QA의 최종 상태 판정은 별도다.
