# CPF 통합 검증 실행 계획

## 목적
여러 개발 작업마다 동일 DB/Runtime/Browser 검증을 반복하지 않고, 구현을 누적한 뒤 기준 Commit에서 한 번에 재현한다. 다른 PC의 성공 결과는 현재 PC의 성공으로 승계하지 않는다.

## 실행 전
1. `check-work-context.ps1`
2. `git status`, `HEAD`, `origin/master`, 작업 Diff 확인
3. `check-r10-product-standard.ps1`
4. Generated Domain baseline에 `cpf-external`이 없는지 확인

## 통합 검증 순서
1. Repository/Architecture/Secret/JavaDoc/OpenAPI/Frontend route/UTF-8/SQL 정적 Gate
2. `sync-database-artifacts.ps1` + Existing Generated Domain parity
3. Gradle clean/test/assemble
4. ADM/BZA npm test/build
5. Empty DB Install → Schema/Index/FK/Seed verify
6. Reinstall/Upgrade/Rollback
7. EXS `external/EXS` create → verify → remove
8. 다른 arbitrary Generated Domain create/remove/regenerate parity
9. Local/Remote ServiceCall, transactionId/Header, File/DB Log
10. Batch/Scheduler/CMN Calendar, Worker/Lease/Ghost, Center-Cut
11. Unknown/DLQ/Compensation/Reconciliation
12. ADM/BZA Browser E2E
13. Multi-instance/target-down/retry/failover
14. Evidence sanitize/commit 대상 검토
15. Post-run repository hygiene

## 사람 확인을 한 번에 모을 항목
- ADM/BZA 반응형 화면과 권한별 메뉴
- 위험조치 승인/Break-glass UX
- 로그 Raw/Formatted/Timeline 탐색성
- Calendar 관리→Batch 수행 시뮬레이션 일치
- 설치/복구 Guide 실제 명령 가독성

`verify-full-product.ps1 -WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll`이 최종 진입점이며 SKIPPED가 있으면 완료가 아니다.
