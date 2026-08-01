# CPF 20260801_03 신규 세션 인수인계

## 1. 현재 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 최신 SHA: `23a16f35a5633ce1317920468a69fef00c1a6a41`
- Parent SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- 최신 Commit은 373개 파일, 92,148줄 규모의 개발 체크포인트다.
- Git Commit/Push는 사용자가 수행했으며 다음 AI는 사용자 명시 승인 없이 Git 쓰기 작업을 하지 않는다.

## 2. 현재 판정

- 전체 완료: 아님
- 독립 검수: 실패
- 최우선 P0: Root `build.gradle` 복구
- Frontend: Source/Release OpenAPI Scope, Generated Client 생성, npm 정책 정합 필요
- Evidence: 현재 SHA와 불일치
- EDU: 신규 대량 구현 요청이 아니라 기존 Reference Source/Test의 실제 32 Feature 연결 검증이 우선

## 3. 반드시 먼저 읽을 파일

1. `cpf-docs/work/review/CPF_20260801_03_POST_PUSH_INDEPENDENT_REVIEW.md`
2. `cpf-docs/work/current/CPF_20260801_03_SELF_DEVELOPMENT_REMEDIATION_REQUEST.md`
3. `cpf-docs/quality/CPF_20260801_03_SELF_DEVELOPMENT_BACKLOG.csv`
4. 최상위 목표 정본 `CPF_FINAL_TARGET_REQUIREMENTS.md` 실제 최신 경로
5. 기존 QA36 EDU Catalog/Matrix와 20260801_01 Completion/Evidence

## 4. 핵심 발견

- Root `build.gradle`이 BZA WAR/Frontend Build 파일 내용으로 덮어써졌다.
- Root Build 공통 계약 약 1,972줄이 삭제됐다.
- `cpf-tools/build/gradle-plugin`, `platform-bom`도 삭제됐다.
- CI는 Source OpenAPI에 Release Scope를 기본 적용할 수 있다.
- Generated Client가 삭제됐으나 Clean CI의 Verify 전에 생성이 보장되지 않는다.
- `.npmrc`는 Strict Peer와 Legacy Peer를 동시에 켠 상태다.
- 완료 보고/Evidence는 Parent SHA를 기준으로 하며 현재 Push SHA와 다르다.
- 완료 보고의 README/Guide 미수정 진술과 실제 Commit 변경이 모순된다.

## 5. EDU 인수인계

`cpf-reference`에는 기존 CRUD, Header, Transaction, Service Call, Messaging, Security, Batch, Logging, Audit, Failure, Validation Source/Test가 존재한다. 이번 변경은 EDU Source를 대량 추가한 것이 아니라 Catalog·Matrix·Gate를 추가했다.

다음 AI는 EDU를 처음부터 다시 만들지 않는다. 먼저 32 Feature Catalog의 Source/Test/Public Contract Glob를 최신 전체 Repository에서 해석하고, 실제로 누락된 Feature만 Source와 Test를 추가한다. Runtime Evidence가 없는 항목은 완료로 바꾸지 않는다.

## 6. 작업 금지

- README 및 README 연결 Manual/Guide 수정
- 문서 행만 추가하고 구현 완료 선언
- 사용자 PC에서 npm·Playwright 검증 반복 요청
- 사용자 승인 없는 Commit/Push/Branch/Reset/Restore/Stash/Clean
- Parent SHA Evidence 승계
- 일부 P0만 고치고 전체 완료 선언

## 7. 산출물

다음 개발 세션은 Source·SQL·Test·Config·CI 보정, 사전/사후 리뷰, 자체 Backlog 결과, Handover, Matrix, Sanitized Evidence, Codex 검수 요청, Changed/Delete Manifest, 파일 SHA-256을 Root Overlay ZIP으로 제공한다.
