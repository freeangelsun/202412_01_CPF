# Codex 중단 작업 재시작 Prompt

아래 내용을 회사 PC 또는 집 PC의 Codex에 한 번에 전달한다.

---

기준 Repository는 `https://github.com/freeangelsun/202412_01_CPF`, 기준 Branch는 `master`다.

이 작업은 신규 작업이 아니라 이전 Codex 세션이 대규모 Module·Package·DB 표준화 도중 중단된 WIP를 이어서 완성하는 작업이다. 이전 세션 대화, 완료 보고 또는 로컬 상태를 기억한다고 가정하지 마라.

현재 PC는 회사 PC 또는 집 PC일 수 있으나 공통 조건은 다음과 같다.

- 최신 `master`를 기준으로 작업한다.
- CPF가 소유하는 MariaDB Schema와 Table은 없는 초기 상태다.
- 기존 DB Dump, 과거 Flyway 적용 상태, Build Cache와 과거 Runtime Evidence에 의존하지 않는다.
- 2026-07-22 Repository 정리에서 Stale Evidence, Generated Matrix, 조기 DOCX, 하위 README, 조기 Release Notes와 IDE 설정을 의도적으로 제거했다. 이를 전체 Revert하지 않는다.
- 이번에 복구된 Root 문서와 `cpf-docs` Guide가 현재 작업 기준이다.

가장 먼저 다음 절차를 수행하라.

1. `git fetch origin`을 실행한다.
2. `git status --short --branch`, `git rev-parse HEAD`, `git rev-parse origin/master`, `git log -5 --oneline --decorate`, `git diff --check`를 확인한다.
3. Worktree가 Clean이고 Local이 Behind일 때만 `git pull --ff-only`를 사용한다. Dirty 또는 Diverged면 reset/revert/checkout/clean을 하지 말고 차이를 보고한다.
4. `CPF_CURRENT_WORK_REQUEST.md` 전체를 읽는다.
5. `CPF_FINAL_TARGET_REQUIREMENTS.md` 전체를 읽는다.
6. `CPF_GAP_MATRIX.md`, `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`, `CPF_STABILIZATION_REPORT.md`, `CPF_EVIDENCE_INDEX.md`를 읽는다.
7. `cpf-docs/work/review/20260722_03`의 검수·Architecture Decision과 역할별 Architecture/Developer/API/Security/Installation/Migration/Deployment/Operator/Recovery/Generator/EDU Guide를 확인한다.
8. 현재 HEAD, OS, JDK, Node, npm, PowerShell, MariaDB와 시작시각을 새 Evidence에 기록한다. Secret과 내부 접속정보는 마스킹한다.
9. Windows에서는 PowerShell 7 `pwsh`를 우선 사용하고 PowerShell 5.1의 UTF-8 손상을 반복하지 않는다.

작업 원칙:

- 최신 Git 전체와 실제 Runtime을 기준으로 이전 작업의 완료, 부분 구현, 미구현, 미검증, 실패, 재확인 필요를 다시 판정한다.
- 사용자 승인 전 Commit, Push와 Branch 생성을 하지 않는다.
- 기존 Applied Flyway Migration을 근거 없이 수정하지 않는다.
- 광범위한 PFW→CPF, XYZ→REF 전체 문자열 치환을 다시 수행하지 않는다.
- `package-lock.json`, Flyway Checksum, Hash, Binary와 Encoding 손상을 먼저 확인한다.
- 삭제된 DOCX/Generated/Evidence를 다시 복원하지 말고 Quality Gate의 생성 책임과 실행 시점을 정비한다.
- 회사·집 PC 모두 빈 DB이므로 정본 Install/Provision/Seed/Verify 구조로 Schema, Table, Constraint, Index, Product Meta와 권한을 처음부터 재생성한다.
- `cmnDB`는 최소 Sample Table 1개만 둔다.
- 고정길이 전문 범용 Engine은 `cpf-core`, 기관별 Layout/Mapping/Adapter는 `cpf-external`이 소유한다.
- 업무 채번은 Core/Common 기본 기능이 아니며 BZA는 선택형 Customization Sample만 제공한다. 온라인 업무가 BZA에 의존하지 않게 한다.
- Batch, Scheduler, Agent, Runner, Worker와 Center-Cut은 `cpf-batch`가 소유한다.
- Source, SQL, API, UI, Test, Guide와 Evidence를 한 제품 상태로 맞춘다.
- 실행하지 않은 검증은 성공으로 기록하지 않는다. DB나 Browser를 직접 확인할 수 없으면 `미검증` 또는 `재확인 필요`로 남기고 실행 명령과 확인 절차를 제출한다.

작업 순서는 `CPF_CURRENT_WORK_REQUEST.md`의 Phase를 따르되, 먼저 Baseline/Encoding/Lockfile/Flyway/Quality Gate 회귀를 안정화하고 그 다음 Ownership, DB Empty Install, Runtime, ADM/BZA, Multi-instance/Recovery, Generator와 최종 문서·Evidence를 완성하라.

완료 보고에는 요구사항별 상태, 변경 Source/SQL/API/UI, 실행 명령, 실제 결과, 실패·미검증, 회귀, Stale Evidence 정리와 남은 Gap을 포함하라. Codex 보고만으로 완료라고 주장하지 마라.

---
