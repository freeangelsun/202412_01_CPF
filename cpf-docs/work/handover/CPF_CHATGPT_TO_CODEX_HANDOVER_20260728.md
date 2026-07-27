# CPF 2026-07-28 Final Completion Handover

## 기준
- Base master: `2daef3b7d2f82745d42d9b19804dde4bcac60edb` / `20260727_05`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 공식 DB: MariaDB / PostgreSQL / Oracle. MySQL / MSSQL 지원 제거.

## 구현 완료 범위
- ADM Session/Permission DB fail-open 제거와 Session Store readiness 분리.
- 운영자 상태/Role/비밀번호 변경 Session 무효화, revoke 실패 UNKNOWN_RESULT 재처리.
- ADM/BZA Raw PII 최소 Projection, 감사 선행, 이유 Sanitization, Browser zeroization.
- ADM 운영자 생성, BZA bootstrap 관리자 생성, BZA 로그인 operationId 멱등 계약.
- BZA 로그인 성공/실패 Transaction 분리와 Refresh Token 단일 활성 Session 회전.
- BZA 상태/Role/Permission/UserRole 변경 Refresh Session 무효화.
- Gateway를 Public Service Call Executor를 통해 Core 표준 Retry/Failover Engine 실제 Consumer로 이관.
- 4xx 무재시도, 408/425/429/5xx 및 통신실패만 retry/failover.
- Core Vendor SQL Catalog / Gateway 계약을 Public API/SPI로 정리하고 외부 `core.common.*` 직접 import 제거.
- Generated Domain Boot4 Golden Template + Batch Tasklet/Chunk/JobPack Provider 연결.
- BAT Scheduler/Worker/Center-Cut/Control Server의 Legacy SQL Catalog 의존 이관.
- Spring Boot 4.1 계열 starter/BOM/Generator/Golden Reference 정리.
- Runtime Query Source↔SQL 및 3 Vendor parameter/result alias parity Gate.
- PostgreSQL/Oracle Canonical Schema/Seed Generator, lifecycle bundle, DB별 Flyway baseline/rollback, CLI Runner 추가.
- Root `qualityGate`에 Official DB Readiness와 Runtime Query Contract Gate 연결.
- MySQL/sqlserver Vendor Tree 삭제 지시 및 제품 선택계약 제거.

## 반드시 보호할 것
- 직전 `20260727_05`의 ADM/BZA Data Safety, V61, 외부화 SQL 성공범위.
- 기존 MariaDB lifecycle과 생성형 Domain 사용자 소유영역 보호.
- BAT lease/fencing/takeover 및 Core Service Call reliability 계약.

## 검증 상태
현재 작업환경은 Java 21이며 PowerShell/실제 DB/Browser 환경이 없어 full Runtime 검증을 실행하지 않았다. 정적 검증 결과만 Evidence에 기록한다. 다음 작업자는 `CPF_CURRENT_WORK_REQUEST.md`의 통합 검증을 최신 적용 Commit에서 실행한다.

실행하지 않은 검증을 PASS로 기록하지 않는다.
