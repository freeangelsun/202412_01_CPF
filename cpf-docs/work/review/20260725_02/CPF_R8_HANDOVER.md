# CPF R8 Cross-PC / Codex Handover — 2026-07-25

## Baseline
- master: `512e5f2c7f32ba21ef6be570b2efa3dbcbd7a482`
- R8는 overlay ZIP이며 ChatGPT가 commit/push/branch를 수행하지 않았다.
- Final Target: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Requirement review: `cpf-docs/work/review/20260725_02/CPF_R8_REQUIREMENT_REVIEW.md` — canonical 162개.

## 적용 원칙
1. ZIP의 `PROJECT_OVERLAY`는 프로젝트 Root와 동일 상대경로다.
2. 단순 복사만 하면 삭제/이동/Generator 생성이 누락되므로 **APPLY 사용을 권장**한다.
3. APPLY는 old MariaDB source SHA 충돌 검사→vendor source 이동, Root compose/log residue 정리, stale R7 UI/ADM owner 파일 삭제, overlay 적용, BAT import 교정, EXS Golden Generator 생성/verify, DB artifact sync, R8 static gate를 수행한다.
4. fixed `cpf-external`은 복구하지 않는다. 최종 `cpf-external`이 존재한다면 `manifest/generator-ownership.json`을 가진 Generated Domain이어야 한다.
5. APPLY는 DB drop/reset, Git commit/push/branch를 하지 않는다.

## 검증 명령
- Static: `pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-full-product.ps1 -Root "C:\dev\projects\jck\202412_01_CPF" -StaticOnly -Profile local`
- Build: `pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-full-product.ps1 -Root "C:\dev\projects\jck\202412_01_CPF" -Profile local`
- Full: `pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-full-product.ps1 -Root "C:\dev\projects\jck\202412_01_CPF" -WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll -Profile local`

`SKIPPED`는 PASS가 아니다. Full Runner는 raw output을 임시 파일에 수집한 뒤 민감정보를 정제하여 `cpf-docs/work/review/20260725_02/evidence/full-verification-*.sanitized.log`로 저장한다.

## 적용 후 첫 확인
1. `git status --short`와 `git diff --check`.
2. `cpf-tools/db/source`가 없어야 하고 `cpf-tools/db/vendor/mariadb/source`가 정본이어야 한다.
3. Root `docker-compose.local.yml`과 runtime `logs` residue가 없어야 한다.
4. `cpf-external/manifest/generator-ownership.json` 확인.
5. ADM 24 / BZA 27 lazy feature route Gate PASS.
6. `sync-database-artifacts.ps1` PASS.

## DB 주의
직전 세션에서 `batDB`가 부분 생성되었을 가능성이 있다. Full DB init은 destructive reset을 자동 수행하지 않으므로 drift/partial DB가 있으면 실패가 정상이다. 원인 확인 후 **batDB만 명시적으로 정리**하고 `initialize-cpf-database.ps1 -All -RequireRun`을 다시 실행한다. 앞쪽 정상 DB를 불필요하게 삭제하지 않는다.

## 보호 대상
- Historical migration(V6/V29)을 checksum 맞추기 목적으로 임의 수정하지 않는다.
- Standard Header/transactionId/segment/trace, ServiceCall, Broker, Fixed-Length, File/Attachment/Masking 성공 기반을 Greenfield 재작성하지 않는다.
- ADM/BZA Approval을 하나의 공통 업무 Engine/Table로 합치지 않는다.
- ADM은 BAT/REF/MBR/ACC Owner DB를 직접 접근하지 않는다.
- EXS 전용 Generator branch/template을 만들지 않는다.

## 다음 검수 우선순위
1. APPLY 후 전체 diff 및 static gate.
2. Gradle + ADM/BZA npm test/build.
3. DB sync/drift/manifest + Fresh/Upgrade path.
4. partial `batDB` 처리 후 `-All -RequireRun`.
5. `external/EXS` create/verify/DB/build/runtime/remove/regenerate parity.
6. ADM/BZA Approval concurrent/idempotent/SoD/delegation/expiry scenarios.
7. BAT Center-Cut multi-instance/stop/rate/lease/fencing/failure/unknown recovery.
8. Browser E2E.
9. Evidence 기준으로 162 matrix 상태 갱신.
10. Core legacy Batch compatibility class 물리 제거 가능 여부를 전체 compile/consumer 기준으로 재판정.
