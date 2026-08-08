# CPF AI 개발·QA·세션 연속성 표준

- 중앙 현행화 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)
- 목적: ChatGPT, Developer GPT, QA A/B, Codex와 후속 세션이 Repository 정본을 기준으로 동일한 완료 정의를 사용하게 한다.

## 1. 시작 기준

모든 세션은:
1. 최신 `origin/master`
2. exact SHA
3. Working Tree
4. `cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md`
5. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
6. `cpf-docs/work/v9i/final-control/REVIEW_INDEX.md`
순서로 확인한다.

과거 대화·완료 보고·Evidence를 current SHA 성공으로 자동 승계하지 않는다.

## 2. 역할은 고정 분리한다

### 중앙 관리자
- 프로젝트 목표·Canonical Requirement·Architecture 계약·Module Ownership 관리
- 프로젝트 관련 문서 상호 정합성 현행화
- QA A/B 결과 Merge 및 충돌 판정
- Current Control/Handover/Continuity 정리
- 과거 중복·Garbage 파일 Delete Manifest 관리
- 개발/QA가 발견한 Canonical 모호성 최종 판정

### Product Developer GPT
- Product Source/SQL/API/SPI/Consumer/Test/Config/Frontend/OpenAPI/Generated Client/Generator/Runtime Gate 구현
- 자기 개발 결과/Evidence 작성
- Canonical 모호성은 Alignment Request로 보고
- 중앙 정본과 QA 원본을 임의 변경하지 않음

### QA A / QA B
- 둘 다 동일한 CPF 전체 범위를 독립 전수검수
- 개발/상대 QA 판정 승계 금지
- Source→Consumer→Runtime→Evidence까지 독립 판정
- 정본 모순은 Finding으로 보고하고 중앙 정본을 임의 수정하지 않음

### Documentation Finalization
- README·Guide·고객 PDF/DOCX 산출물 전담
- Product Source 변경 금지

## 3. 완료 판정

Interface/DTO/Controller/Test/SQL/화면/Compile 존재만으로 완료가 아니다.

완료는:
`호출자 → 인증/권한 → API/SPI → Owner → DB/State → Consumer → 실패/UNKNOWN/복구 → Audit/운영조회 → 실제 결과`
가 current exact SHA에서 연결되고 필요한 Runtime Evidence까지 있어야 한다.

상태:
- 미구현
- 부분 구현
- 미검증
- 실패
- 재확인 필요
- 완료

`개발 완료·Runtime 미검증`은 Product Source 개발 상태와 Release 검증 상태를 분리하여 기록하며 최종 Release PASS로 계산하지 않는다.

## 4. Canonical 분모

- Canonical Product Requirement: **169**
- Legacy Alias: 8, 중복 집계 금지
- QA Finding/Scenario/Work Item/93 원장/56 Finding/31 Action은 분모가 아니다.

## 5. QA 원본 불변성

`cpf-docs/work/v9i/qa/final-a/**`, `final-b/**`는 현재 Final QA 원본이다.
개발GPT가 QA Finding 삭제, Severity 하향, Acceptance 완화, PASS 변경을 하지 않는다.

## 6. 고객 문서 분리

Product Developer와 Source QA가 직접 수정하지 않는다.

- `README.md`
- `cpf-docs/guides/**`
- `cpf-docs/deliverables/**`
- `cpf-docs/assets/manuals/**`
- `cpf-docs/assets/readme/**`
- `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`

Product 변경 영향은 `DOCUMENT_IMPACT.csv`로 전달한다.

## 7. ADM/BZA/EDU

ADM은 플랫폼 운영 Control Plane, BZA는 고객 업무 관리자 Product, EDU는 `cpf-reference`의 adopter 예제다.
ADM Product 기능을 EDU에 복제하거나 EDU를 ADM 구현 통로로 취급하지 않는다.

## 8. Final QA

Final Candidate에서는 QA A와 QA B 모두:
- Canonical 169
- 전체 Source/SQL/API/Test/Config/Frontend/Script
- ADM/BZA/EDU/Generator
- DB3
- Transaction/Logging/FileLog
- Security/Permission/Audit
- Runtime 13축
- Evidence exact SHA
를 독립 전수검수한다.

Final QA에서 “이전에 검수한 영역”이라는 이유로 생략하지 않는다.

## 9. 세션 인수인계

세션 종료 시 필요한 현재 상태는 stable Current Control에 흡수한다.
날짜/QA 번호별 Handover를 계속 누적하지 않는다.
과거 상세 기록은 Git History로 보존하고 Working Tree에서는 중앙 Delete Manifest에 따라 정리한다.

## 10. Git 안전

사용자 승인 없이 Commit/Push/Branch/Tag/PR/Reset/Restore/Stash/Clean/Delete를 수행하지 않는다.
삭제는 중앙 exact-path Manifest와 사용자 실행 명령을 사용한다.
