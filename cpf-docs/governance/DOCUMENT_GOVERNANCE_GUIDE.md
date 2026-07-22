# CPF Document Governance Guide

## 1. 정본 원칙

- Root `README.md`: 유일한 제품 README
- `CPF_FINAL_TARGET_REQUIREMENTS.md`: 최상위 목표
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

구현이 변하는 동안 Markdown을 정본으로 사용합니다. DOCX/PDF는 기능, API, SQL, UI와 구조가 안정된 최종 제품화 단계에서 생성합니다.

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

Work Request, Gap, Stabilization, Evidence Index는 최종적으로 `cpf-docs/work`, `review`, `governance`, `evidence`의 정본 위치로 이동합니다. 이동은 단순 파일 이동이 아니라 모든 Script/Gradle/CI Link를 함께 보정한 뒤 수행합니다.
