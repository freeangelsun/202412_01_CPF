# CPF Codex Exact-SHA Final Validation Request

## 역할

Codex는 개발자가 아니라 독립 검수자다. 최신 `master`의 실제 Source, SQL, API, Frontend, Test, Script, 문서, Evidence를 양방향 추적한다. 과거 작업 보고를 성공 근거로 사용하지 않는다.

## 시작 조건

- 사용자 Push 완료
- Local/Remote `master` SHA 동일
- Working Tree Clean
- Source SHA 40자리 기록

## 필수 검수

1. Root `clean test assemble`
2. ADM/BZA Typecheck, ESLint, Vitest, Production Build
3. 모든 저비용 Architecture, Ownership, Public Boundary, SQL, Secret, Route, Generator, Hygiene Gate
4. MariaDB 기존 DB Backup→Upgrade→Verify→Rollback→Reapply→Restore와 별도 Clean Install
5. PostgreSQL·Oracle Install/Migration/Upgrade/Rollback/Runtime Query
6. 임의 Generated Domain 2개 Create→Verify→Test→JAR/WAR→Remove와 MBR Golden Parity
7. Redis 정상·Down·Timeout·Recovery·Invalidation·Multi-instance·Lock/Fencing
8. Gateway Connection Test, Health, Load Balance, Retry/Failover, Target-down, Unknown Result, Trace
9. Batch Multi-worker, Center-Cut, Lease/Fencing, File Watch, Approved Shell, Retry/Recovery
10. ADM/BZA Browser E2E와 READ/WRITE/DELETE/위험조치 Negative Test
11. 495개 통합 Matrix와 2,715개 Verification Ledger exact-SHA Evidence 폐쇄
12. Repository Hygiene와 Local/Remote SHA 재확인

## 완료 금지 조건

- 실행하지 않은 검증을 PASS로 기록
- 일부 Test 또는 정적 검색만으로 Runtime 완료 처리
- Matrix/Ledger의 미검증 행을 Evidence 없이 일괄 완료 처리
- DB Vendor 하나의 결과를 다른 Vendor에 승계
- 실패를 Skip 또는 정상 0건으로 변환
- 사용자 승인 없는 Commit/Push/Branch/PR

## 결과 형식

각 항목에 Requirement ID, Source SHA, 실행 명령, 환경/Profile, 시작·종료 시각, Exit Code, 실제 결과, Evidence 경로, 민감정보 제거 여부를 기록한다. 허용 상태는 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`만 사용한다.
