# CPF Document Governance Guide

- 중앙 정책 현행화 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)

## 1. 정본 원칙

- Root `README.md`: 유일한 제품 README
- `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`: 최상위 목표
- Architecture: 구조, Ownership와 계약
- Developer/API/Security/Operations Guide: 역할별 사용법
- Work/Review: 작업 요청, 검수와 진행
- Evidence: 실행 근거
- Generated: 재생성 가능한 파생 자료
- Release: 실제 Release가 있을 때만 생성

## 2. 중복 금지

하위 Module, `cpf-docs`, `scripts`, `deploy`에 README를 반복하지 않습니다. 유효 내용은 역할별 Guide로 통합합니다.

설치와 Migration, Operator와 Recovery, API와 Security, 전체 Deployment와 Frontend Deployment는 서로 다른 책임을 유지합니다. 같은 내용을 여러 파일에 복제하지 않습니다.

## 3. 작성 시점

프로젝트 목표·Governance·Architecture·Requirement·Current Control 같은 **프로젝트 통제 문서**는 Markdown 정본을 사용할 수 있습니다.

반면 공식 고객 Guide는 `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`의 최종 형식 정책을 따릅니다. 현재 Final 단계에서 공식 Guide의 인도 형식은 DOCX/PDF이며, README는 Markdown 정본으로 유지합니다. 고객 Guide Authoring Markdown의 보존/삭제는 Documentation Finalization에서 해당 표준과 Delete Manifest에 따라 처리합니다.

## 4. Source 정합성

Guide의 명령, Module, Package, Property, API, SQL, Menu, Permission과 Runtime 결과는 실제 Source와 일치해야 합니다. 미구현 기능을 사용 가능하다고 쓰지 않습니다.

## 5. Generated와 Evidence

Generated Matrix는 Script, Tool Version과 기준 Commit을 포함합니다. Evidence는 Raw/Sanitized, command, profile, environment, time, result와 secret review를 포함합니다. 둘 다 최상위 요구사항 정본이 아닙니다.

## 6. Link와 Encoding

- UTF-8
- 한글 깨짐 검사
- 상대 Link 검증
- 삭제/이동 시 Script, Build, CI와 Guide Link 동시 수정
- 날짜별 복제본과 임시 Package Manifest를 제품 Repository에 남기지 않음

## 7. Root 정리

Current Project Control은 `cpf-docs/work/v9i/final-control/**`의 안정된 진입점으로 수렴합니다. 날짜·QA 번호·세션별 과거 Work/Review 복제본은 유효 결론을 Current Canonical에 흡수한 뒤 Git History로 보존하고 exact Delete Manifest로 Working Tree에서 제거합니다.

경로 이동·삭제가 필요한 경우 Script/Build/CI/Canonical Link를 함께 검토하며, 중앙 관리자가 Current/History 경계를 판정합니다.
