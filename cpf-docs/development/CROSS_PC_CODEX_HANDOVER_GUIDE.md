# CPF 3-Way Handover Guide

## 1. 역할

회사 PC Codex, 집 PC Codex, ChatGPT가 같은 Repository를 이어서 작업한다. Git은 Source 공유 수단이고 Current Handover는 **현재 Source identity와 실제 검증 상태를 연결**하는 수단이다. 과거 Handover/Review가 Current Target을 다시 정의하지 않는다.

## 2. 시작 순서

```text
1. git status / git rev-parse HEAD / origin/master
2. cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md
3. cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md
4. cpf-docs/governance/CPF_CANONICAL_PATH_AND_ROLE_MAP.md
5. cpf-docs/governance/development-harness/current/CPF_CURRENT_WORK_REQUEST.md
6. cpf-docs/governance/development-harness/current/REQUIREMENT_STATUS.csv
7. cpf-docs/deliverables/TEST_AND_EVIDENCE.md
8. cpf-docs/deliverables/OPEN_ISSUES.md
9. cpf-docs/governance/development-harness/current/CPF_DEVELOPMENT_HANDOVER.md
10. 실제 Source/Diff/Test/Runtime Evidence
```

문서와 실제 Source가 다르면 Source Gap을 등록하며 Final Target을 Source 수준으로 낮추지 않는다.

## 3. 시작 Gate

```powershell
git status
git rev-parse HEAD
git rev-parse origin/master
git diff --check
```

- dirty Worktree를 임의 reset/restore/clean하지 않는다.
- 사용자 승인 없는 commit/push/branch/tag/history rewrite를 하지 않는다.
- 다른 PC WIP가 있으면 Current Handover와 실제 diff를 먼저 대조한다.

## 4. Requirement 단위 인계

각 작업은 Requirement/Defect ID, Owner, 변경 Source/SQL/API/UI/Test/Config/Guide, Consumer/호출경로, 실패·복구·UNKNOWN, 실제 실행 명령, 개발 상태, 검증 상태, Evidence, 남은 조건을 기록한다. 개발 GPT/Codex/QA의 원장 컬럼 수정 권한을 섞지 않는다.

## 5. Runtime Evidence

한 PC의 PASS를 다른 PC나 다른 SHA에 자동 승계하지 않는다. Java/Node/npm, DB3, Redis/Valkey, Multi-WAS, Browser, Docker 등 환경을 함께 기록한다. 미실행은 `미검증`이고 READY/PLANNED는 PASS가 아니다.

## 6. 로컬 통합 테스트 표준

기본 명령은 `Tee-Object`로 콘솔 진행상황과 로그 저장을 동시에 수행한다. 종료 시 PASS/FAIL, ExitCode, 시작/완료 시각, 로그 전체 경로와 가능한 실패 Task 수를 출력한다. Current Handover에 최신 한 줄 명령, 로그 경로, 정상 기대 결과, 실패 시 전달할 로그 파일명을 항상 유지한다.

## 7. 중단/세션 이동 전

`cpf-docs/governance/development-harness/current/CPF_DEVELOPMENT_HANDOVER.md` 하나를 현행화한다. 새 날짜/Session/Checkpoint Handover를 추가로 만들지 않는다. 다음 세션이 첫 번째로 실행할 명령과 현재 실패 조건을 반드시 남긴다.

## 8. 최종 종료

Build/Test/DB/Runtime/Browser 중 실행하지 않은 항목은 `미검증`으로 남긴다. Stale Evidence와 중복 Current 문서를 제거하고 Canonical Path/링크/Verifier가 삭제된 과거 문서를 요구하지 않는지 확인한다. commit/push는 사용자 승인 후에만 수행한다.
