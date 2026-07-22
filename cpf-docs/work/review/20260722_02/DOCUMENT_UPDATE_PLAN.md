# 문서 갱신 계획

## 1. 정본 원칙

| 역할 | 정본 |
|---|---|
| 최상위 제품 목표 | CPF_FINAL_TARGET_REQUIREMENTS.md |
| 현재 전체 작업 | cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md |
| Gap | cpf-docs/review/CPF_GAP_MATRIX.md |
| 검수/완료 기준 | cpf-docs/review/CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md |
| 안정화 Report | cpf-docs/review/CPF_STABILIZATION_REPORT.md |
| Evidence Index | cpf-docs/evidence/CPF_EVIDENCE_INDEX.md |
| Architecture | cpf-docs/architecture |
| Developer/Generator/EDU | cpf-docs/development |
| API | cpf-docs/api |
| Install/Deploy/Operator/Recovery | cpf-docs/operations |
| Security | cpf-docs/security |
| Migration/Release | cpf-docs/releases |

## 2. Root 정책

Root 유지:

- README.md
- CPF_FINAL_TARGET_REQUIREMENTS.md
- settings.gradle
- build.gradle
- Wrapper
- 공식 Module·Tool·Deploy·Docs

Root 이동:

- Current Work Request
- Gap
- Stabilization
- Evidence Index
- Review Guide

경로 변경 시 Script·Link·CI·Gradle을 함께 수정한다.

## 3. README

포함:

- 제품 소개
- Module
- Architecture
- Quick Start
- Documentation Link
- Supported Environment

제외:

- 진행률
- Commit
- Gap
- Codex Report
- 날짜별 Evidence
- 완료 주장

License Badge는 최종 사용·배포 정책 확정 전 제거 또는 중립 문구로 둔다.

## 4. Guide 갱신 Trigger

기능 변경 시:

- Source
- JavaDoc
- OpenAPI
- EDU
- Developer Guide
- Operator Guide
- Migration Guide
- README Link

를 함께 확인한다.

## 5. DOCX/PDF

현재 단계:

- Markdown 정본
- 기존 DOCX Stale 검수
- Quality Gate 필수 제거

최종 단계:

- Source/API/SQL/UI 안정
- Markdown Freeze
- DOCX/PDF 생성
- 내용 일치 검증
- Version·Commit 표시
