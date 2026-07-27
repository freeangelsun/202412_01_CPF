# CPF Final Remaining Requirement Matrix

기준: master `2daef3b7d2f82745d42d9b19804dde4bcac60edb` + 2026-07-28 Completion Patch

| 축 | 구현 상태 | 검증 상태 | 최종 판정 조건 |
|---|---|---|---|
| ADM Session / Permission fail-closed | 완료 | 미검증 | DB down/timeout 행동 Test + Runtime 503/거부 Evidence |
| ADM 운영자 상태/Role/Password → Session revoke | 완료 | 미검증 | 정상/동시성/DB failure/UNKNOWN_RESULT 재처리 |
| ADM/BZA Raw PII Data Safety | 완료 | 미검증 | 최소 Projection + Audit + Browser zeroization E2E |
| ADM/BZA operationId 멱등 계약 | 완료 | 미검증 | timeout/응답유실/동시 재시도 Runtime |
| BZA 로그인 Transaction/Refresh Session | 완료 | 미검증 | 성공/실패/재시도/Token rotation/multi-instance |
| Gateway 표준 Retry/Failover Consumer | 완료 | 미검증 | A down→B 성공, 4xx 무재시도, retryable status |
| Core Public API/SPI Boundary | 완료 | 미검증 | compile + architecture ownership gate |
| Generated Domain Golden Template | 완료 | 미검증 | remove/regenerate parity + Boot4 build/runtime |
| BAT Public Boundary/JobPack 연결 | 완료 | 미검증 | Scheduler/Worker/Center-Cut multi-instance runtime |
| Spring Boot 4.1 / Java25 / Gradle9.1 | 완료 | 미검증 | full clean test/assemble/bootJar/bootWar/external WAS |
| DB Vendor 정책 3종 | 완료 | 미검증 | MariaDB/PostgreSQL/Oracle lifecycle Runtime Evidence |
| MySQL/MSSQL 제거 | 완료 | 미검증 | repository full search + generator/tool negative gate |
| Runtime Query Contract | 완료 | 미검증 | Source↔SQL orphan 0 + 3 Vendor param/result parity |
| Repository Hygiene / Historical Docs | 완료 | 미검증 | hygiene/document link/current-truth gate |

`부분 구현` 또는 `미구현` 상태로 다음 개발 범위에 남긴 항목은 없다. 실행환경 의존 검증만 `미검증`으로 유지한다.
