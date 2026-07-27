# CPF Unverified Scenario List — 2026-07-27 / Change Set B

아래는 **구현 미완료 목록이 아니다.** 현재 ChatGPT 실행환경에 필요한 외부 Runtime이 없어 실행하지 못한 검증만 기록한다.

| ID | 상태 | 필요한 환경 | 실행/검증 |
|---|---|---|---|
| UV-B-001 | 미검증 | Java 25 + Gradle 9.1 | `clean test assemble --no-daemon --max-workers=1` |
| UV-B-002 | 미검증 | PowerShell 7 | `check-admin-data-safety.ps1`, 관련 quality gate |
| UV-B-003 | 미검증 | MariaDB 기존 V58/V59/V60 DB | V61 upgrade + ADM/BZA runtime probe |
| UV-B-004 | 미검증 | MariaDB | V61 rollback → reapply, fake role/data loss 0 |
| UV-B-005 | 미검증 | MariaDB clean | Fresh install latest == migration final contract |
| UV-B-006 | 미검증 | ADM/BZA Runtime | DB down/write fault/duplicate/concurrent expectedVersion/partial row 0 |
| UV-B-007 | 미검증 | Browser | masked list, raw deny/allow/reason/audit/no-store, explicit clear, conflict |
| UV-A-001 | 미검증 | Java25/Gradle/PowerShell | Aggregate Quality Build → staging → manifest → promote/rollback |
| UV-A-002 | 미검증 | Standalone Generated Domain | LOCAL_DEV/OFFLINE consumption + bootJar/bootWar hash |
| UV-A-003 | 미검증 | Nexus/Artifactory | REMOTE auth/timeout/immutable/no local fallback |

실제 실행 시 Evidence에는 최신 Commit SHA, Profile, DB/Browser/Java/Gradle version, 명령, 시작·종료시각, 결과와 민감정보 제거 여부를 저장한다.
