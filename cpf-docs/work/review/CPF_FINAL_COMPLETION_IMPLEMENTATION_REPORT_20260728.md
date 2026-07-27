# CPF Final Completion Implementation Review — 2026-07-28

## 기준

- Base master: `2daef3b7d2f82745d42d9b19804dde4bcac60edb` (`20260727_05`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 공식 DB: MariaDB / PostgreSQL / Oracle
- MySQL / Microsoft SQL Server: 제품 지원 및 선택 경로 제거

## 이번 통합 작업에서 닫은 범위

### ADM Security / Data Safety

- DATABASE Session Store를 인증 정본으로 고정하고 DB 장애 시 Memory 우회 제거.
- Permission DB 장애와 미등록 ADM API를 fail-closed 처리.
- 운영자 상태/Role/비밀번호 변경과 Session revoke를 같은 책임 경계로 연결.
- revoke 결과불명은 CPF `UNKNOWN_RESULT` 운영 재처리 흐름에 연결.
- Session Store readiness를 별도 운영 상태로 노출.
- 일반 운영자 생성 시 Backend Role 동시부여 차단.
- 운영자 생성 `operationId` 멱등 및 결과조회 계약 보강.
- 상태 Transition Matrix/CAS 보강.
- Raw PII를 전용 최소 DTO/Projection으로 축소하고 Audit 성공 전 원문 응답 금지.
- Raw 조회 사유 Sanitization과 ADM Browser zeroization 적용.

### BZA Authentication / Authorization / PII

- Bootstrap 관리자 생성 `operationId` 영속 멱등 계약과 V62 추가.
- 로그인 `operationId`, 성공 Transaction, 실패 기록 Transaction, Refresh Token rotation 계약과 V63 추가.
- 응답유실 재시도 시 중복 활성 Refresh Session을 남기지 않는 흐름 보강.
- 상태/Role/Permission/UserRole 변경 시 영향 사용자 Refresh Session 즉시 폐기.
- Raw 직원 연락처는 필요한 4개 필드 Projection만 조회하고 Audit 성공 후 최소 DTO 반환.
- BZA Raw Modal은 조회 시작/실패/닫기/Route 변경/Unmount에서 원문 zeroization.
- Legacy Backoffice Approval SQL Consumer 부재를 확인하고 3 Vendor Dead SQL 삭제 대상으로 정리.

### Gateway / Core Public Boundary

- Gateway를 `CpfServiceCallExecutor` Public API를 통해 Core 표준 Service Call Engine의 실제 Consumer로 연결.
- Retry/Failover 정책을 일반 4xx 즉시 종료, 408/425/429/5xx 및 통신실패만 retryable로 보정.
- Gateway Route/Auth/Header/Instance Identity와 Vendor SQL Catalog 경계를 Public API/SPI로 정리.
- 외부 Module의 `com.cpf.core.common.*` 직접 import를 제거.

### Generated Domain / BAT

- Spring Boot 4.1 Golden Template로 Generated Domain build 계약 정리.
- Generated Batch Job에 Tasklet/Chunk/restart/checkpoint/retry JobPack Provider 연결.
- `BusinessJobProvider → JobPackCatalog` 실제 발견 경로 보강.
- BAT Scheduler/Worker/Center-Cut/Control Server의 Legacy Core Internal SQL Catalog 의존을 Public Catalog Provider로 이관.
- MBR/ACC/REF 고정 Root 가정을 줄이고 Generated Domain federation 경계를 정리.

### Spring Boot 4.1 / Build

- Java 25 + Gradle 9.1 + Spring Boot 4.1 기준으로 WebMVC/JDBC Batch/Boot Flyway/관련 starter와 Generator/Golden Reference를 정리.
- Boot 3 starter/version active residue를 제거.
- Root `qualityGate`에 Official DB Readiness와 Runtime Query Contract Gate를 연결.

### DB 3 Vendor / Query Contract

- 공식 Vendor를 MariaDB/PostgreSQL/Oracle 3종으로 정본화.
- MySQL/SQL Server Vendor Tree 및 Runtime Template 선택/재생성 경로 삭제.
- Canonical Schema/Seed에서 PostgreSQL/Oracle Vendor-native Source를 생성하는 Tool 보강.
- PostgreSQL/Oracle에서 MariaDB `USE` 및 logical-DB qualifier를 제거.
- Profile의 논리 DB별 접속정보를 이용하는 Vendor CLI Runner 추가.
- PostgreSQL/Oracle migration을 8개 logical DB별 Flyway baseline/rollback으로 분리.
- V61 rollback은 Exact Rollback 정책으로 정리하고 V62/V63과 checksum 계약 보강.
- Platform/BAT Runtime Query Sync를 3 Vendor 전용 Canonical pipeline에 연결.
- Source↔SQL orphan, parameter/result alias parity Gate 추가.
- TransactionLog/TransactionSegment MyBatis를 3 Vendor Native Mapper로 관리.
- PostgreSQL/Oracle Runtime Query compile-smoke 도구 추가.
- Git-tracked DB Profile/Generator의 평문 `devDefault` Secret 제거.

## Review 결과

이번 패치의 구현 가능한 Source/SQL/Generator/Tool/Frontend 계약은 의도적으로 `부분 구현` 또는 `미구현`으로 남기지 않았다. 다만 실제 제품 환경이 필요한 검증은 실행하지 않았으므로 `미검증` 상태를 유지한다.

정적 Evidence: `cpf-docs/evidence/20260728-final-completion/STATIC_VALIDATION.md`

## 금지 사항

- 이 Report만으로 Runtime PASS 처리 금지.
- PostgreSQL/Oracle을 MariaDB 문자열 치환본으로 취급 금지.
- MySQL/MSSQL Vendor 선택 경로 재도입 금지.
- ADM/BZA DB 장애를 Memory/default permission으로 우회 금지.
- 사용자 승인 없는 Commit/Push/Branch 생성 금지.
