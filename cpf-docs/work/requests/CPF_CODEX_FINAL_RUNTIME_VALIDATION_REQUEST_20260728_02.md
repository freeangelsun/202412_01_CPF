# CPF Codex Final Runtime Validation and Repair Request — 20260728_02

## 목표

`CPF_20260728_02_FINAL_COMPLETION_ROOT_OVERLAY.zip` 적용 후 최신 master 전체를 대상으로 통합 실행 검증을 수행하고, 발견되는 결함을 같은 작업에서 수리한다.

## 우선 정본

1. `CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
3. `cpf-docs/work/current/CPF_REMAINING_REQUIREMENT_MATRIX_20260728_02.md`
4. `cpf-docs/work/handover/CPF_CHATGPT_TO_CODEX_HANDOVER_20260728_02.md`
5. `cpf-tools/verification/20260728_01/qa-source/*`
6. 실제 Source, SQL, Test, Config, Frontend, Script

## 필수 실행

1. `gradlew.bat clean test assemble --no-daemon`
2. Source architecture, dependency, secret, route, DB parity Gate
3. MariaDB/PostgreSQL/Oracle install·upgrade·rollback
4. ADM Runtime Change Center 권한별 Browser E2E
5. Gateway small/large/multipart/range/conditional/timeout/retry/target-down
6. Runtime Control single/multi-instance, duplicate operation, optimistic conflict, partial ACK, restart recovery, drift, rollback
7. Batch Scheduler/Worker/Center-Cut/Host-Agent 정책 반영과 장애 복구
8. Generator ACC/MBR/EXS 생성·검증·build
9. QA Inventory 1,214건 및 Scenario 201건 최신 Commit 재판정

## 수리 원칙

- 실패 발견 시 문서만 수정하지 않는다.
- Owner Module, Public API/SPI/Internal 경계를 지킨다.
- SQL 변경은 canonical/source/runtime/migration/rollback/checksum과 3개 Vendor를 함께 처리한다.
- 실제 Consumer 없는 Interface나 정책 객체를 완료 처리하지 않는다.
- 실행하지 않은 검증은 성공으로 기록하지 않는다.
- 기존 성공 기능 회귀를 함께 검증한다.

## Evidence

각 실행에는 기준 SHA, 명령, Java/Gradle/Profile/DB Vendor, 시작·종료 시각, 결과, 민감정보 제거 여부를 남긴다.
