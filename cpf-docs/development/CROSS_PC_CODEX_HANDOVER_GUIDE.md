# CPF 3-Way Handover Guide

## 1. 대상

CPF는 다음 세 작업 주체가 같은 Repository를 이어서 작업한다.

- 회사 PC Codex
- 집 PC Codex
- ChatGPT

Git은 Source 공유 수단이고, Continuity/Decision/Review 문서는 **작업 문맥과 검증 상태 공유 수단**이다. Local DB, 미커밋 Worktree, IDE 상태는 자동 공유되지 않는다.

## 2. 시작할 때 반드시 읽는 순서

```text
1. git status
2. git rev-parse HEAD / origin/master
3. cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md
4. cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md
5. cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md
6. cpf-docs/work/state/CPF_CODEX_DECISION_LOG.md
7. cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md
8. cpf-docs/governance/CPF_CANONICAL_PATH_AND_ROLE_MAP.md
9. 최신 cpf-docs/work/review/* Handover
10. 실제 Source/Diff/Evidence
```

문서가 실제 Git과 다르면 실제 Git을 기준으로 판정하고 문서를 현행화한다.

## 3. 시작 Gate

반드시 확인한다.

```powershell
git status
git rev-parse HEAD
git rev-parse origin/master
git diff --check
```

- dirty Worktree를 임의 reset/restore/clean하지 않는다.
- 다른 PC에서 생성된 WIP라면 먼저 Continuity/Handover를 확인한다.
- 사용자가 승인하지 않은 commit/push/branch를 만들지 않는다.

## 4. Requirement 단위 기록

주요 작업마다 다음을 남긴다.

- Requirement ID
- 기존 구현 판정: 유지/보완/확장/교체/제거
- Module/Package/Data Owner
- 변경 Source/SQL/API/UI/Test/Guide
- 설계 이유와 대안
- 실제 실행 명령
- 완료/부분 구현/미구현/미검증/실패/재확인 필요
- Evidence 경로
- 남은 Blocker

## 5. DB 변경 인계

DB 변경은 반드시 Vendor/source 정본부터 시작한다.

```text
DB Source SSOT
→ generated Vendor Pack
→ new migration/rollback
→ Java Mapper/Repository
→ Service/API/UI
→ fresh install/upgrade/runtime
→ Evidence
```

Historical Migration을 checksum 맞춤 목적으로 수정하지 않는다.

DB Credential은 문서/Evidence에 기록하지 않는다.

## 6. Generated Domain 인계

다음 정보를 함께 남긴다.

- DomainName/SystemCode
- 생성 명령과 Capability
- Manifest 경로
- DB bootstrap 여부
- CRUD/Build/Runtime 결과
- 사용자 소유 파일 유무
- 삭제 Dry-Run 결과
- 재생성 parity 결과

MBR/ACC/EXS 같은 이름을 Generator 고정 목록으로 사용하지 않는다.

## 7. transactionId 정책

- 동일 업무 흐름: transactionId 승계
- 내부 독립 기동: Core가 신규 34자리 transactionId 생성
- 호출 계층: segmentId/parentSegmentId
- 실행 정의: standardExecutionId

후속 작업자가 과거 별도 Global 거래 ID 개념을 다시 도입하지 않도록 Review/Handover에 검색 결과를 남긴다.

## 8. PC별 환경

HOME/COMPANY를 별도 기록한다.

- JDK/Gradle/Node/npm
- MariaDB Client/Server Version
- DB Schema 설치 상태
- 마지막 Fresh Install/Runtime
- Browser/Frontend 검증 상태
- Blocker

한 PC의 Runtime 성공을 다른 PC의 성공으로 승계하지 않는다.

## 9. 중단 전

크레딧/세션/PC 이동 전에 새 범위를 시작하지 말고 다음부터 갱신한다.

1. `CPF_CODEX_CONTINUITY_STATE.md`
2. 최신 Review/Handover
3. Requirement 상태
4. 실제 실패/성공 명령
5. 다음 작업자가 첫 번째로 실행할 명령

## 10. 최종 종료

- Build/Test/DB/Runtime/Browser 중 실행하지 않은 항목은 `미검증`
- Stale Evidence/임시 로그/Generated smoke 잔재 제거
- Canonical Path 위반 확인
- Root 문서 `README.md` 외 잔존 확인
- 사용자 승인 후에만 commit/push
