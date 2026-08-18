# CPF 문서 정본 Index

- Currentization 기준: 고정 과거 SHA를 현재값으로 사용하지 않는다. 실제 실행·검증 시 최신 `origin/master` exact SHA를 동적으로 확인하고, History 정리 전 필요한 현재 Requirement/Decision/Evidence를 Canonical 파일에 흡수한다.
- 적용 후 기준 SHA: 본 문서 Overlay가 반영된 successor `master` exact SHA
- 목적: Developer GPT·Codex·QA·중앙 관리자가 하나의 Current-State 정본만 사용하고 구 Steering/완료보고/세션 문서를 다시 정본으로 승계하지 않게 한다.

## 1. 정본 우선순위

1. `CPF_FINAL_TARGET_REQUIREMENTS.md` — 최상위 Framework 목표와 Target Architecture
2. `CPF_REQUIREMENT_CONTINUITY_LEDGER.md` — Requirement 연속성·Supersession·정책 변경 이력
3. `CPF_REPOSITORY_SURFACE_INDEX.md` — Root/Product/Generated Domain 경계
4. `CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md` — Base/Common/Web/Capability/Profile/Provider 정본
5. `CPF_GENERATED_DOMAIN_LIFECYCLE_POLICY.md` — 고객 Generated Domain·DB·Starter 소비 정본
6. `CPF_CANONICAL_PATH_AND_ROLE_MAP.md` — Current 경로·역할 진입점
7. `CPF_DOCUMENT_CONTROL_POLICY.md` — Current-State/삭제/정책전파 통제
8. `CPF_AI_DEVELOPMENT_QA_CONTINUITY_STANDARD.md` — AI 역할·Source 검수·5분 진행률 표준
9. `../architecture/ARCHITECTURE_GUIDE.md` — 구현 Architecture 설명
10. `../development/DEVELOPER_GUIDE.md`, `../development/GENERATOR_GUIDE.md`, `../development/EDU_GUIDE.md`
   - 개발 선택 Reference: `../development/CPF_DEVELOPER_GOLDEN_PATH.md`, `../development/CPF_STARTER_QUICK_SELECT.md`, `../development/CPF_PUBLIC_FUNCTION_TOP_100.md`, `../development/CPF_BATCH_DEVELOPER_TOP_50.md`
11. `../work/CPF_CURRENT_WORK_REQUEST.md` — 현재 단일 개발 요청
12. `../work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md` — 다음 Developer GPT 강제 실행 지침
13. `../work/current/CPF_REQUIREMENT_CONTROL_REGISTER.xlsx` — Requirement Control Dashboard/Current Architecture Sheet
14. `../work/CPF_SOURCE_FINDINGS.csv`, `../work/REQUIREMENT_STATUS.csv`, `../work/OPEN_ISSUES.md`
15. 실제 Source·SQL·Config·Generator·Frontend·Test와 exact-SHA Evidence

QA Requirement가 위 Target을 더 구체화하면 QA Requirement를 우선하되 상위 Framework 목표를 약화하지 않는다. 과거 완료·Evidence는 최신 SHA의 PASS로 자동 승계하지 않는다.

## 2. 이번 Current Architecture의 명시적 Supersession

다음 과거 정책은 더 이상 Current가 아니다.

| 과거 정책 | Current Target |
|---|---|
| `cpf-common` = 고객 업무 공통 독립 Product Domain | 독립 `cpf-common` Root 제거. CPF Common Product Service를 `cpf-starters/common` Capability로 이관 |
| 고객 공통 확장을 `cpf-common` Source에 적치 | 고객 특화 공통은 고객 Repository의 선택형 `<customer>-common` |
| `cmnDB`, `admDB`, `batDB` 독립 Platform DB | CPF 소유 관계형 데이터는 기본 `CPF_PLATFORM_DB`(`cpfDB`)로 통합. Ownership은 Table Prefix/Metadata로 유지 |
| Generated Domain마다 `mbrDB`, `accDB` 등 DB/Schema 생성 | Customer Business DB/Schema는 통합하고 `MBR_*`, `ACC_*`, `PRD_*` Table Prefix로 Domain Ownership 구분 |
| `cpf-member`/`cpf-external`을 CPF Product Module/Public Artifact로 취급하거나 Generated Domain을 transient-only로 검증 | `cpf-member/`(MBR)+`cpf-external/`(EXS)은 동일 Canonical Generator의 **retained Generated Customer Domain 회귀 Root**로 유지한다. 둘을 Product Module/Public BOM/Publication에 넣는 것은 금지하며 제3 임의 Domain만 transient genericity 검증에 사용한다. |
| Public Profile Artifact에 `cpf-starter-profile-*` 사용 | 개발자 공개 Artifact는 `cpf-starter-<profile>` 형식으로 단순화 |
| Capability별 `*/core` 물리 Module | `cpf-core` 외 `core` 이름의 Capability Module 금지. `api/runtime/provider/autoconfigure/internal` 역할명 사용 |
| Common/업무 오류를 Enum 추가로 확장 | Framework reserved fallback만 Core에 고정. 업무 Error/Message는 DB Catalog에서 동적 등록·해석 |

## 2.1 공식 산출물 Publication Surface

사용자 관점 공식 문서는 **README 포함 7종**으로 고정한다. 기능별 새 Guide를 계속 늘리지 않고 기존 Owner 문서에 흡수한다.

```text
README.md
cpf-docs/guides/
  02_프레임워크_개발자_가이드.docx/.pdf
  03_배치_개발자_가이드.docx/.pdf
  04_운영자_매뉴얼.docx/.pdf
  05_배치_운영_가이드.docx/.pdf
  06_Gateway_개발_사용_가이드.docx/.pdf
  07_Specification_기술_명세.docx/.pdf

cpf-docs/deliverables/
  아키텍처설계서.docx/.pdf
  기술사양서.docx/.pdf
  기술표준서.docx/.pdf
  데이터베이스표준서.docx/.pdf
  산출물목록.docx/.pdf
```

설계 산출물 5종은 공식 사용자 가이드 7종의 개수에 포함하지 않는다. QA·Requirement·Evidence·Governance는 내부 관리 자료이며 README 사용자 Navigation에 노출하지 않는다.

### 산출물 현행화 원칙

- 개발 중 전달되는 최신 Steering/개발요건은 산출물 작업에서는 **최종 구현 완료 상태의 Target Contract**로 반영한다. 진행 상태 문구를 사용자 문서에 옮기지 않는다.
- QA/Source 검토의 목적은 결함 보고서를 사용자 문서에 싣는 것이 아니라 **실제 Class/API/Annotation/Starter/Config/Consumer를 확인하여 장점·Golden Path·예제·운영 사용법을 정확히 갱신**하는 데 있다.
- Public API 이름이 바뀌면 README, Owner Guide, Specification, 설계 산출물, 예제, 표/도식, Handover까지 같은 Cycle에서 대조한다.
- 기존 문서를 축약하거나 상세 Reference를 잃지 않는다. 새 기능은 적정 Owner 장에 흡수한다.

## 3. Current Work 진입점

- 현재 개발 요청: `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md`
- Developer GPT 실행지침: `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
- 현재 Finding: `cpf-docs/work/CPF_SOURCE_FINDINGS.csv`
- 현재 역할 상태: `cpf-docs/work/REQUIREMENT_STATUS.csv`
- 검증/Evidence: `cpf-docs/work/TEST_AND_EVIDENCE.md`
- 삭제 정본: `cpf-docs/work/CPF_DELETE_MANIFEST.csv`
- 리뷰 진입점: `cpf-docs/work/REVIEW_INDEX.md`
- 인수인계: `cpf-docs/work/HANDOVER.md`

## 4. 문서 Current-State 규칙

동일 목적 문서의 `*_REV*`, `*_R1*`, `*_SESSION*`, `*_YYYYMMDD*`, `*_FINAL_FINAL*`, Checkpoint 복제본을 만들지 않는다. 정책이 바뀌면 기존 Current 파일을 직접 현행화하고, 대체된 문서/일회성 Script는 `CPF_DELETE_MANIFEST.csv`에 Root 상대경로로 등록한다. Repository는 Git History 보존을 전제로 하지 않는다. 삭제 전 현재도 필요한 Requirement/Decision/Evidence를 Canonical 파일에 먼저 병합한다.

## 5. 통합 개발·검수 역할 경계

- 현재 마무리 운영에서는 개발 담당이 Source/SQL/API/Test/Config/Frontend/Generator 구현, 1차 개발검수, 정본 currentization까지 함께 수행한다.
- 정본 변경은 Source와의 불일치·새로 확정된 상위 요구·Architecture 정합성을 반영하기 위한 경우에만 수행하며 Acceptance 약화는 금지한다.
- `REQUIREMENT_STATUS.csv`의 역사적 Codex/QA Evidence는 감사 이력으로 보존하고 임의 PASS를 생성하지 않는다.
- 동일 목적의 Current 문서는 버전별 복제하지 않고 역할별 canonical 파일 하나를 직접 갱신한다.
- 세션별 Final/Revision/Checkpoint 문서는 active Current 영역에 남기지 않는다. History 정리 여부와 무관하게 필요한 현재 정보는 Canonical/Current 파일에 먼저 병합한다.
- Active Evidence는 현재 baseline의 실행 결과만 안정된 `cpf-docs/work/evidence/current/` 경로에 유지한다. 과거 SHA PASS, `UNKNOWN` SHA, 실패한 과거 Runtime 로그는 현재 Evidence와 경쟁시키지 않으며 필요한 판단만 Current 문서에 승계한 뒤 제거한다.

## Canonical Write Responsibility

실제 Framework 계약을 변경한 작업자는 영향을 받는 Final Target/Architecture/Current Request/Developer Instruction을 함께 현행화한다. 단 다음은 금지한다.

- 구현 편의를 위한 Requirement 삭제·완화
- 미검증 Runtime을 PASS로 문서화
- 과거 Evidence SHA를 현재 SHA 성공으로 승계
- 같은 역할의 문서를 `_FINAL`, `_REV`, 날짜, 세션 번호로 복제
- 파생 Requirement Dataset을 수동 조작해 Canonical Requirement와 불일치시키는 행위

## Derived Requirement Dataset Currentization Rule

`CPF_REQUIREMENT_MASTER.csv`, `CPF_EXECUTION_SEQUENCE.csv`, 분할 `*.parts/**`, `CPF_REQUIREMENT_CONTROL_REGISTER.xlsx`는 Canonical Requirement에서 파생되는 관리 Dataset이다. Developer GPT가 수동 행 삽입·삭제·재생성으로 현행화하지 않는다.

Derived Dataset drift를 발견하면 Developer GPT는 `OPEN_ISSUES.md`와 Evidence에 정확히 기록하고 Source 개발을 계속한다. 중앙관리/QA가 Canonical decomposition pipeline으로 master/parts/index/count/hash를 원자적으로 재생성한다.


## 6. Current-State 표현 규칙

과거 경로/Artifact 이름은 Supersession 표, `removed*` 목록, forbidden/negative gate처럼 **명시적 과거·금지 Context**에서만 허용한다. Current Target/Current Source 설명에서 이미 제거된 `cpf-common`, `cpf-starters/foundation`, `cpf-tools/generator/golden/member`, `cpf-starter-profile-*`를 앞으로 생성/이관할 경로처럼 서술하면 문서 정합성 Gate 실패다. `cpf-member/`와 `cpf-external/`은 retained Generated Customer Domain Root로만 허용하며 Product Module/Public Artifact로 서술하면 실패다.


## Currentization SHA 의미

- `currentization_review_baseline_sha`: Canonical 문서 현행화 시 비교한 기준 SHA이며 영구 현재값이 아니다.
- `execution_source_sha`: 각 통합 개발·검수 시작 시 최신 `origin/master`에서 동적으로 확인한다.
- `verified_sha`: Build/Test/Runtime/Evidence를 실제 실행한 exact SHA다.

`currentization_source_sha`를 이후 세션의 영구적인 "현재 master"로 해석하지 않는다. 완료 판정과 Evidence는 `verified_sha`를 기준으로 한다.

## Generated Customer Domain Current Target

Current Target은 다음 두 Root Generated Customer Domain을 동일 Domain-neutral Generator로 실제 생성·유지하는 것이다.

```text
cpf-member/   = MBR
cpf-external/ = EXS
```

둘은 CPF Product Module/Public Artifact가 아니며, 동일 Canonical Schema·Naming Strategy·Generator Engine·Template으로 생성한다. `CUSTOMER_BUSINESS_DB`, API+Batch, Sample Transaction, DB3, Build/Test를 함께 검증하고 최종 결과물에 포함한다.

Legacy single-canary/transient-only 정책은 Current Target이 아니다. 단, 제3 임의 Domain은 genericity 추가 검증용 transient output으로 생성 후 cleanup할 수 있다.

## 공식 사용자 Publication Surface

공식 사용자 문서는 **README 포함 7종**을 기준으로 한다.

1. README
2. 02 프레임워크 개발자 가이드
3. 03 배치 개발자 가이드
4. 04 운영자 매뉴얼
5. 05 배치 운영 가이드
6. 06 Gateway 개발·사용 가이드
7. 07 Specification / 기술 명세

설계 산출물(아키텍처설계서·기술사양서·기술표준서·데이터베이스표준서·산출물목록)은 인수/설계 목적의 별도 산출물이며 사용자 가이드 개수에 포함하지 않는다. 과거 세분화 가이드는 Current Publication Surface가 아니며 유효 내용은 위 Owner 문서에 흡수한다.
