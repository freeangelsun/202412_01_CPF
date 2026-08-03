# DEVELOPMENT OPEN ISSUES

| ID | 상태 | 내용 | 완료 조건 |
|---|---|---|---|
| DEV-001 | 미완료 | 전체 `10,375` Requirement의 Source·Consumer·Runtime 독립 전수검수 미수행 | exact SHA fresh clone에서 194 Work Package 전체 Gate·Build·Runtime 완료 |
| DEV-002 | 미검증 | Java 25 Gradle 전체 Build/Test 및 Publication 미실행 | `./gradlew` 전체 Gate 성공과 실패 0건 Evidence |
| DEV-003 | 미검증 | Oracle·PostgreSQL·MariaDB 실제 install/upgrade/rollback/drift 미실행 | 공식 3 Vendor Runtime 결과와 사전·사후 상태 Evidence |
| DEV-004 | 미검증 | Batch Process Kill·재기동·다중 Worker·Metadata DB Runtime 미실행 | UNKNOWN→reconcile, 중복방지, fencing, 재기동 Runtime PASS |
| DEV-005 | 미검증 | ADM Browser E2E·권한별 Route/Menu/API 차단 미실행 | Chromium/Playwright에서 권한·401/403/404/409/429/500/503 PASS |
| DEV-006 | 미적용 | Root Overlay가 사용자 Repository에 아직 적용되지 않음 | exact baseline SHA에 적용 후 `git diff --check`와 status 확인 |
| DEV-007 | 미Push | Commit/Push 미수행 | 모든 미검증 항목 해소 후 사용자 승인·검증 기준으로 수행 |

삭제·정리 대상은 없다.
